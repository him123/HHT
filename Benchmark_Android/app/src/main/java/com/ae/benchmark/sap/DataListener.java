package com.ae.benchmark.sap;
/**
 * Created by Rakshit on 17-Jan-17.
 */
public interface DataListener {
    void onProcessingComplete();
    void onProcessingComplete(String source);
}
