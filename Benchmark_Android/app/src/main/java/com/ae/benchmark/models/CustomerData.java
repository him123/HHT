package com.ae.benchmark.models;

/**
 * Created by eheuristic on 12/2/2016.
 */

public class CustomerData
{
    public CustomerData(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

   public CustomerData()
    {

    }

    String id;
    String name;
    String address;
    public String getIsCredit() {
        return isCredit;
    }
    public void setIsCredit(String isCredit) {
        this.isCredit = isCredit;
    }
    String isCredit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
