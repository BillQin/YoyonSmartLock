package yoyon.smartlock.standalone.model;

import java.io.Serializable;

/**
 * Created by QinBin on 2019/1/8.
 */

public class YoyonKey implements Serializable{
    public static final int ADMIN = 1;
    public static final int SHARE = 0;

    public static final int UNDEFINE_KEY = 0;
    public static final int FINGERPRINT_KEY = 1;
    public static final int PASSWORD_KEY = 2;
    public static final int RFIC_KEY = 3;
    public static final int PHYSICS_KEY = 4;
    public static final int REMOTE_KEY = 5;
    public static final int BLUETOOTH_KEY = 6;
    public static final int FACE_KEY = 7;
    private String attachLockMac;       //关联的锁的mac地址
    private String name;                //钥匙名称
    private int indexNumber;                  //钥匙编号
    private int type;                   //钥匙类型
    private int role;                   //钥匙权限

    public String getAttachLockMac() {
        return attachLockMac;
    }

    public void setAttachLockMac(String attachLockMac) {
        this.attachLockMac = attachLockMac;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
