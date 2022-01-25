package com.bitcoin.analyzer.util;

public enum TypeSentiment {
    VERY_NEGATIVE(0), NEGATIVE(1), NEUTRAL(2), POSITIVE(3), VERY_POSITIVE(4);

    int index;

    private TypeSentiment(int index) {
        this.index = index;
    }

    public static TypeSentiment fromIndex(int index) {
        for (TypeSentiment typeSentiment : values()) {
            if (typeSentiment.index == index) {
                return typeSentiment;
            }
        }

        return TypeSentiment.NEUTRAL;
    }
}