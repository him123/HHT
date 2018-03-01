package com.ae.benchmark.models;

/**
 * Created by Muhammad Umair on 14-09-2017.
 */

public class Advertisement {

    String id;
    String customerId;
    String salesOrg;
    String distChannel;
    String picture;
    String pictureData;


    public Advertisement() {
    }

    public Advertisement(String id, String customerId, String salesOrg, String distChannel, String picture, String pictureData) {
        this.id = id;
        this.customerId = customerId;
        this.salesOrg = salesOrg;
        this.distChannel = distChannel;
        this.picture = picture;
        this.pictureData = pictureData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSalesOrg() {
        return salesOrg;
    }

    public void setSalesOrg(String salesOrg) {
        this.salesOrg = salesOrg;
    }

    public String getDistChannel() {
        return distChannel;
    }

    public void setDistChannel(String distChannel) {
        this.distChannel = distChannel;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPictureData() {
        return pictureData;
    }

    public void setPictureData(String pictureData) {
        this.pictureData = pictureData;
    }
}
