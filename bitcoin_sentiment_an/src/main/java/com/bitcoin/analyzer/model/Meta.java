package com.bitcoin.analyzer.model;

public class Meta {

    private int result_count;
    private String next_token;

    public int getResult_count() {
        return result_count;
    }

    public void setResult_count(int result_count) {
        this.result_count = result_count;
    }

    public String getNext_token() {
        return next_token;
    }

    public void setNext_token(String next_token) {
        this.next_token = next_token;
    }

    @Override
    public String toString() {
        return "Meta [result_count=" + result_count + ", next_token=" + next_token + "]";
    }

}
