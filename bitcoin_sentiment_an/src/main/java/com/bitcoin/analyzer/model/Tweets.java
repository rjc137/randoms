package com.bitcoin.analyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Tweets {

    private List<Tweet> data = new ArrayList<Tweet>();

    private Meta meta;

    public List<Tweet> getData() {
        return data;
    }

    public void setData(List<Tweet> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Tweets [data=" + data + ", meta=" + meta + "]";
    }
}
