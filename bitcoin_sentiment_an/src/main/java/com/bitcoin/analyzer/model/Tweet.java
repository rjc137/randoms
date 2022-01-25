package com.bitcoin.analyzer.model;

public class Tweet {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Tweet [text=" + text + "]";
    }

}
