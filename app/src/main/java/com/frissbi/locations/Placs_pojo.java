package com.frissbi.locations;

/**
 * Created by KNPL003 on 14-07-2015.
 */
public class Placs_pojo {

    public  String Placesname;
    public  String Placesvicinity;
    public String distance;
    public String UserLocationVotingID;
    boolean selected = false;
    public String Enabled;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getEnabled() {
        return Enabled;
    }

    public void setEnabled(String enabled) {
        Enabled = enabled;
    }

    public String getPlacesname() {
        return Placesname;
    }

    public void setPlacesname(String placesname) {
        Placesname = placesname;
    }

    public String getPlacesvicinity() {
        return Placesvicinity;
    }

    public void setPlacesvicinity(String placesvicinity) {
        Placesvicinity = placesvicinity;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getUserLocationVotingID() {
        return UserLocationVotingID;
    }

    public void setUserLocationVotingID(String userLocationVotingID) {
        UserLocationVotingID = userLocationVotingID;
    }
}
