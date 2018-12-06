package com.example.bluno.model;

/**
 * Created by Aiden on 2018-10-30.
 */

public class UserModel {
    public String profileImageUrl;
    public String uid;

    public UserModel(){}

    public UserModel(String profileImageUrl, String uid){
        this.profileImageUrl = profileImageUrl;
        this.uid = uid;
    }
}
