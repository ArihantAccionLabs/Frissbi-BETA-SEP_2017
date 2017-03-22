package com.frissbi.models;

/**
 * Created by thrymr on 20/3/17.
 */

public class Notification {
    private String message;
    private Long groupId;
    private String groupName;
    private String groupAdmin;
    private String groupImageId;

    private Long friendId;
    private String status;
    private String friendName;
    private String friendImageId;
    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String getGroupImageId() {
        return groupImageId;
    }

    public void setGroupImageId(String groupImageId) {
        this.groupImageId = groupImageId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendImageId() {
        return friendImageId;
    }

    public void setFriendImageId(String friendImageId) {
        this.friendImageId = friendImageId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "message='" + message + '\'' +
                ", groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", groupAdmin='" + groupAdmin + '\'' +
                ", groupImageId='" + groupImageId + '\'' +
                ", friendId=" + friendId +
                ", status='" + status + '\'' +
                ", friendName='" + friendName + '\'' +
                ", friendImageId='" + friendImageId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
