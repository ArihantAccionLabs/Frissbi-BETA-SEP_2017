package com.frissbi.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by thrymr on 24/1/17.
 */

public class Meeting implements Parcelable {

    private Long meetingId;
    private String senderFirstName;
    private String senderLastName;
    private String date;
    private String fromTime;
    private String toTime;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private int meetingStatus;


    public Meeting() {

    }

    protected Meeting(Parcel in) {
        senderFirstName = in.readString();
        senderLastName = in.readString();
        date = in.readString();
        fromTime = in.readString();
        toTime = in.readString();
        description = in.readString();
        address = in.readString();
        meetingId = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        meetingStatus = in.readInt();
    }

    public static final Creator<Meeting> CREATOR = new Creator<Meeting>() {
        @Override
        public Meeting createFromParcel(Parcel in) {
            return new Meeting(in);
        }

        @Override
        public Meeting[] newArray(int size) {
            return new Meeting[size];
        }
    };

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public void setSenderFirstName(String senderFirstName) {
        this.senderFirstName = senderFirstName;
    }

    public String getSenderLastName() {
        return senderLastName;
    }

    public void setSenderLastName(String senderLastName) {
        this.senderLastName = senderLastName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getMeetingStatus() {
        return meetingStatus;
    }

    public void setMeetingStatus(int meetingStatus) {
        this.meetingStatus = meetingStatus;
    }


    @Override
    public String toString() {
        return "Meeting{" +
                "meetingId=" + meetingId +
                ", senderFirstName='" + senderFirstName + '\'' +
                ", senderLastName='" + senderLastName + '\'' +
                ", date='" + date + '\'' +
                ", fromTime='" + fromTime + '\'' +
                ", toTime='" + toTime + '\'' +
                ", description='" + description + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", meetingStatus=" + meetingStatus +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(senderFirstName);
        parcel.writeString(senderLastName);
        parcel.writeString(date);
        parcel.writeString(fromTime);
        parcel.writeString(toTime);
        parcel.writeString(description);
        parcel.writeString(address);
        parcel.writeLong(meetingId);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeInt(meetingStatus);
    }
}
