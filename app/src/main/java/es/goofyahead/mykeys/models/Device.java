package es.goofyahead.mykeys.models;

import java.io.Serializable;

/**
 * Created by goofyahead on 7/12/14.
 */
public class Device implements Serializable{

    private String name;
    private int cost;
    private String imageUrl;
    private String gravatar;

    public Device (String name, int cost, String imageUrl, String gravatar) {
        this.name = name;
        this.cost = cost;
        this.imageUrl = imageUrl;
        this.gravatar = gravatar;
    }

    public String getGravatar() {
        return gravatar;
    }

    public void setGravatar(String gravatar) {
        this.gravatar = gravatar;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
