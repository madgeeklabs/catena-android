package es.goofyahead.mykeys.models;

import java.io.Serializable;

/**
 * Created by goofyahead on 7/12/14.
 */
public class Device implements Serializable{

    private String name;
    private int cost;

    public Device (String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
