package com.app.studiomusic.Common;

public class MultipleRequests {

    private int requestsCount, finishedRequests;

    public MultipleRequests() {
        requestsCount = 0;
        finishedRequests = 0;
    }

    public void setNumberOfRequests(int num) {
        if (requestsCount > 0) return;
        requestsCount = num;
    }

    public void finishRequest() {
        if (finishedRequests == requestsCount) return;
        finishedRequests++;
    }

    public boolean allDone() {
        return finishedRequests == requestsCount;
    }

}
