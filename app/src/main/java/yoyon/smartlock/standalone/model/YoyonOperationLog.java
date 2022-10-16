package yoyon.smartlock.standalone.model;

/**
 * Created by QinBin on 2019/1/8.
 */

public class YoyonOperationLog {
    private String attachLockMac;
    private int attachKeyIndex;
    private int attachKeyType;
    private int type;
    private String date;
    private String content;

    public String getAttachLockMac() {
        return attachLockMac;
    }

    public void setAttachLockMac(String attachLockMac) {
        this.attachLockMac = attachLockMac;
    }

    public int getAttachKeyIndex() {
        return attachKeyIndex;
    }

    public void setAttachKeyIndex(int attachKeyIndex) {
        this.attachKeyIndex = attachKeyIndex;
    }

    public int getAttachKeyType() {
        return attachKeyType;
    }

    public void setAttachKeyType(int attachKeyType) {
        this.attachKeyType = attachKeyType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
