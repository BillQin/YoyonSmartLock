package yoyon.smartlock.standalone.model;

import com.yoyon.ynblelib.yoyon.YnBleCapability;

import java.io.Serializable;

/**
 * Created by QinBin on 2019/1/8.
 */

public class YoyonLock implements Serializable{
    public static final int ADMIN = 0;
    public static final int SHARE = 1;
    private String mac;                     //锁的mac地址
    private int role;                       //权限
    private String userKey;                 //用于配对的adminKey或shareKey
    private YnBleCapability capability;     //锁的能力
    private String name;                    //锁的名称
    private String tempPasswordSeed;
    private String tempPasswordRandom;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public YnBleCapability getCapability() {
        return capability;
    }

    public void setCapability(YnBleCapability capability) {
        this.capability = capability;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTempPasswordSeed() {
        return tempPasswordSeed;
    }

    public void setTempPasswordSeed(String tempPasswordSeed) {
        this.tempPasswordSeed = tempPasswordSeed;
    }

    public String getTempPasswordRandom() {
        return tempPasswordRandom;
    }

    public void setTempPasswordRandom(String tempPasswordRandom) {
        this.tempPasswordRandom = tempPasswordRandom;
    }
}
