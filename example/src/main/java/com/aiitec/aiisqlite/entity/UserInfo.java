package com.aiitec.aiisqlite.entity;

import com.aiitec.openapi.db.annotation.Column;
import com.aiitec.openapi.db.annotation.Index;
import com.aiitec.openapi.db.annotation.Table;
import com.aiitec.openapi.db.annotation.Unique;

@Table("user_info")
public class UserInfo {

    private String faceplus;
    private Integer organizationId;
    private String types;
    @Column("sn")
    private String serialNumber;
    private String passTime;

    private String featureImg;
    @Index("department")
    private String department;
    @Unique
    // value 索引名称， orderBy 排序（可忽略）
    @Index(value = "userId", orderBy = "DESC")
    private Integer userId;

    // 这个索引  username 包含两个字段 (name,username)
    @Index("username")
    private String name;
    @Index("username")
    private String username;
    @Index("age")
    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFaceplus() {
        return faceplus;
    }

    public void setFaceplus(String faceplus) {
        this.faceplus = faceplus;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPassTime() {
        return passTime;
    }

    public void setPassTime(String passTime) {
        this.passTime = passTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatureImg() {
        return featureImg;
    }

    public void setFeatureImg(String featureImg) {
        this.featureImg = featureImg;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
