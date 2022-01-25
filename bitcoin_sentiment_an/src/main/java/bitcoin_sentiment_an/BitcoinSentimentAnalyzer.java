package bitcoin_sentiment_an;

public class BitcoinSentimentAnalyzer {
/*
 enum TypeSentiment {
  VERY_NEGATIVE(0), NEGATIVE(1), NEUTRAL(2), POSITIVE(3), VERY_POSITIVE(4);

  int index;

  private TypeSentiment(int index) {
   this.index = index;
  }

  public static TypeSentiment fromIndex(int index) {
   for (TypeSentiment typeSentiment: values()) {
    if (typeSentiment.index == index) {
     return typeSentiment;
    }
   }

   return TypeSentiment.NEUTRAL;
  }
 }

 public static List < Status > searchTweets(String keyword) {
  List < Status > tweets = Collections.emptyList();

  ConfigurationBuilder cb = new ConfigurationBuilder();
  cb.setDebugEnabled(true).setOAuthConsumerKey("tFdLqp8i8yPhjHFzS23CXyG2v")
   .setOAuthConsumerSecret("NzS6W0gXnBAsSccRx9LYiAmzeAQQZAsmalUnxrwdiCRxdvHgX4")
   .setOAuthAccessToken("1355823438-c9WEtruG1XXbkMkp0DmziqE3HCCoZ20qumlmf8k")
   .setOAuthAccessTokenSecret("rFCUNfO4SLKebMBaA71HGA6Jg7ipVx7tUOemuNGNSRxTi");
  TwitterFactory tf = new TwitterFactory(cb.build());
  Twitter twitter = tf.getInstance();

  Query query = new Query(keyword + " -filter:retweets -filter:links -filter:replies -filter:images");
  query.setCount(1000);
  query.setLocale("en");
  query.setLang("en");;

  try {
   QueryResult queryResult = twitter.search(query);
   tweets = queryResult.getTweets();
  } catch (TwitterException e) {
      e.printStackTrace();
  }

  return tweets;

 }

 public static TypeSentiment analyzeSentiment(String text) {
  Properties props = new Properties();
  props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
  StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
  int mainSentiment = 0;

  if (text != null && text.length() > 0) {
   int longest = 0;
   Annotation annotation = pipeline.process(text);

   for (CoreMap sentence: annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
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
 }



 public static void main(String[] args) {
  HashMap < TypeSentiment, Integer > sentiments = new HashMap < BitcoinSentimentAnalyzer.TypeSentiment, Integer > ();
  List < Status > list = searchTweets("bitcoin");

  for (Status status: list) {
   String text = status.getText();
   TypeSentiment sentiment = analyzeSentiment(text);
   Integer value = sentiments.get(sentiment);

   if (value == null) {
    value = 0;
   }

   value++;
   sentiments.put(sentiment, value);
  }

  int size = list.size();
  System.out.println("Sentiments about Bitcoin on " + size + " tweets");

  for (Entry < TypeSentiment, Integer > entry: sentiments.entrySet()) {
   System.out.println(entry.getKey() + " => " + (entry.getValue() * 100) / size + " %");
  }
 }

*/
}