package com.frissbi.app.models;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by thrymr on 27/2/17.
 */

public class FrissbiGroup extends SugarRecord implements Serializable {

    private Long groupId;
    private String name;
    private String image;
    private Long adminId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    @Override
    public String toString() {
        return "FrissbiGroup{" +
                "groupId=" + groupId +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", adminId=" + adminId +
                '}';
    }
}
