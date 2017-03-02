package com.frissbi.models;

import com.orm.SugarRecord;

/**
 * Created by thrymr on 27/2/17.
 */

public class Participant extends SugarRecord {

    private Long participantId;
    private String fullName;
    private String image;
    private boolean isAdmin;
    private Long groupId;

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "participantId=" + participantId +
                ", fullName='" + fullName + '\'' +
                ", image='" + image + '\'' +
                ", isAdmin=" + isAdmin +
                ", groupId=" + groupId +
                '}';
    }
}
