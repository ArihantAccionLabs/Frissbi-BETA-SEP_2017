package com.frissbi.locations;

/**
 * Created by KNPL003 on 05-08-2015.
 */
public class Orzine_pojo {

public  String IsDefault;
public  String UserID;
public  String UserPreferredLocationID;
public  String Latitude;
public  String Longitude;
public  String LocationType;
public  String LocationName;

    public String getIsDefault() {
        return IsDefault;
    }

    public void setIsDefault(String isDefault) {
        IsDefault = isDefault;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }

    public String getLocationType() {
        return LocationType;
    }

    public void setLocationType(String locationType) {
        LocationType = locationType;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserPreferredLocationID() {
        return UserPreferredLocationID;
    }

    public void setUserPreferredLocationID(String userPreferredLocationID) {
        UserPreferredLocationID = userPreferredLocationID;
    }
}

