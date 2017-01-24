package com.frissbi.Frissbi_Pojo;

/**
 * Created by thrymr on 11/1/17.
 */

public class PlaceDetails {
    private String description;
    private String placeId;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return "PlaceDetails{" +
                "description='" + description + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
