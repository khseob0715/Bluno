package com.example.bluno.model;

import java.util.List;

public class LightModel {
    public String ShareUserName;           // 공유한 사람 이름
    public String ShareLightDescription;  // 공유된 조명 설명 & 이름 등등
    public List<Integer> SharePixel;
    public String ShareDate;
    public String ShareUserUid;
    public String timestamp;
    public String LigthImageUrl;
    public boolean bShare;


    public LightModel(){}

    public LightModel(String ShareUserName, String ShareLightDescription, List<Integer> SharePixel, String ShareDate, String ShareUserUid, String timestamp, boolean bShare, String LigthImageUrl){
        this.ShareUserName = ShareUserName;
        this.ShareLightDescription = ShareLightDescription;
        this.SharePixel = SharePixel;
        this.ShareDate = ShareDate;
        this.ShareUserUid = ShareUserUid;
        this.timestamp = timestamp;
        this.bShare = bShare;
        this.LigthImageUrl = LigthImageUrl;
    }

}
