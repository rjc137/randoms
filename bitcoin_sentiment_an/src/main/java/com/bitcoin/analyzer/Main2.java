package com.bitcoin.analyzer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitcoin.analyzer.model.Tweets;
import com.bitcoin.analyzer.util.TypeSentiment;
import com.google.gson.Gson;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class Main2 {

    private static final Gson gson = new Gson();
    private static final int max_attempts = 300;
    private static final String max_results = "100";
    private static final String token_key = "bearer_token";
    private static final String keyword_key = "keyword";
    private static final String twitter_url_key = "twitter_url";
    private static final String how_far_back_key = "how_far_back";
    private static final String wait_between_calls_random_key = "wait_between_calls_random";

    private static final ResourceBundle bundle = ResourceBundle.getBundle("props");
    private static SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private static HttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec(CookieSpecs.STANDARD)
            .build())
        .build();

    private static Logger logger = LoggerFactory.getLogger(Main2.class);
    
    public static void main(String args[]) throws IOException, URISyntaxException, InterruptedException, ExecutionException {
        
        String bearerToken = bundle.getObject(token_key)
            .toString();
        
        List<String> listOfTweets = new ArrayList<>();
        
        for (int i = 0; i < 1; i++) {
            LocalDateTime startDateTime = LocalDateTime.now();
            logger.info("Cycle {} : Starting to Pull Tweets At : {}", i, startDateTime.toString() );
            LocalDateTime finishDateTime = startDateTime.plusMinutes(15);
            
            listOfTweets.addAll(search(startDateTime, finishDateTime, bundle.getString(keyword_key), bearerToken));
            
            logger.info("Finished Pulling Tweets at {}", LocalDateTime.now().toString());
            //Thread.sleep(1000L * 60L * 5L);
        }         
         
        HashMap<TypeSentiment, Integer> sentiments = new HashMap<TypeSentiment, Integer>();

        ExecutorService exec = Executors.newFixedThreadPool(10);
        
        List<CompletableFuture<TypeSentiment>> tweetAnalysisFutures = listOfTweets.stream()
                .map(tweet -> analyzeTweet(tweet, exec))
                .collect(Collectors.toList());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            tweetAnalysisFutures.toArray(new CompletableFuture[tweetAnalysisFutures.size()])
        );
        
        CompletableFuture<List<TypeSentiment>> allPageContentsFuture = allFutures.thenApply(v -> {
           return tweetAnalysisFutures.stream()
                   .map(tweetAnalysisFuture -> tweetAnalysisFuture.join())
                   .collect(Collectors.toList());
        });

        allPageContentsFuture.get().forEach(sentiment -> {
            Integer value = sentiments.get(sentiment);
            if (value == null) {
                value = 0;
            }
            value ++;
            sentiments.put(sentiment, value);
        });
        
        exec.shutdown();
        logger.info("all items processed");
        
        int size = listOfTweets.size();
        logger.info("Sentiments about Bitcoin on " + size + " tweets");

        for (Entry<TypeSentiment, Integer> entry : sentiments.entrySet()) {
            System.out.println(entry.getKey() + " => " + (entry.getValue() * 100) / size + " %");
        }
    }
    
    public static CompletableFuture<TypeSentiment> analyzeTweet(String text, ExecutorService exec) {
        return CompletableFuture.supplyAsync(() -> {
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            int mainSentiment = 0;

            if (text != null && text.length() > 0) {
                int longest = 0;
                Annotation annotation = pipeline.process(text);

                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                    int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                    String partText = sentence.toString();
                    if (partText.length() > longest) {
                        mainSentiment = sentiment;
                        longest = partText.length();
                    }

                }
            }
            return TypeSentiment.fromIndex(mainSentiment);
        }, exec);
    }
   
    private static List<String> search(LocalDateTime startDateTime, LocalDateTime finishDateTime, String searchString, String bearerToken) throws IOException, URISyntaxException, InterruptedException {
        String searchResponse = null;
        Tweets tweets = new Tweets();
        URIBuilder uriBuilder = getUriBuilder(startDateTime);
        List<String> listofTweets = new ArrayList<>();
        
        for (int i = 0; (i < max_attempts && LocalDateTime.now().isBefore(finishDateTime)); i++) {
            logger.info("It's been {} minutes since we started this cycle", Duration.between(LocalDateTime.now(), startDateTime).toMinutes());
            HttpResponse response = httpClient.execute(getHttpGet(uriBuilder));
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.info("Error Occured : {}", EntityUtils.toString(response.getEntity(), "UTF-8"));
                System.exit(0);
            }
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                searchResponse = EntityUtils.toString(entity, "UTF-8");
                Tweets resp = gson.fromJson(searchResponse, Tweets.class);
                resp.getData().forEach(tweet -> listofTweets.add(tweet.getText()));
                
                logger.info("{} Tweets pulled", resp.getData().size());
                if (resp.getMeta().getNext_token() == null || resp.getMeta().getNext_token().isEmpty() || resp.getMeta().getNext_token().isBlank()) {
                    return listofTweets;
                }
                uriBuilder = getUriBuilder(startDateTime);
                uriBuilder.addParameter("next_token", resp.getMeta().getNext_token());
                long sleepTime = getRandomSleepTime() * 1000L;
                logger.info("Sleeping for {} seconds", sleepTime/1000);
                Thread.sleep(sleepTime);
            }
        }
        return listofTweets;
    }

    private static URIBuilder getUriBuilder(LocalDateTime startDateTime) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(bundle.getString(twitter_url_key));
        ArrayList<NameValuePair> queryParameters;
        queryParameters = new ArrayList<NameValuePair>();
        queryParameters.add(new BasicNameValuePair("query", bundle.getString(keyword_key)));
        queryParameters.add(new BasicNameValuePair("max_results", max_results));
        queryParameters.add(new BasicNameValuePair("start_time", toRFC3339(startDateTime.minusHours(Long.valueOf(bundle.getString(how_far_back_key))))));
        queryParameters.add(new BasicNameValuePair("tweet.fields", "text"));
        uriBuilder.addParameters(queryParameters);
        return uriBuilder;
    }

    private static HttpGet getHttpGet(URIBuilder builder) throws URISyntaxException {
        HttpGet httpGet = new HttpGet(builder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bundle.getString(token_key)));
        httpGet.setHeader("Content-Type", "application/json");
        return httpGet;
    }

    private static int getRandomSleepTime() {
        return new Random().nextInt(Integer.parseInt(bundle.getString(wait_between_calls_random_key)));
    }
    
    private static String toRFC3339(LocalDateTime d) {
        return rfc3339.format(Date.from(d.atZone(ZoneId.systemDefault())
            .toInstant()))
            .replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

}