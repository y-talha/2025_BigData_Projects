package com.sau;

public enum Topic {
    ACCOUNTING(10),
    RESEARCH(20),
    SALES(30),
    OPERATIONS(40);

    private final int value;

    Topic(int value) {
        this.value = value;
    }

    public static String getTopic(int val) {
        for(Topic t : Topic.values()){
            if(t.value == val) return t.name();
        }
        return null;
    }
}
