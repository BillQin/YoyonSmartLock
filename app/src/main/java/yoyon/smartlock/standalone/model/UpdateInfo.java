package yoyon.smartlock.standalone.model;

/**
 * Created by QinBin on 2018/8/9.
 */

public class UpdateInfo {
    private String versionName;
    private boolean forceUpdate;
    private String updateLink;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionCode) {
        this.versionName = versionCode;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getUpdateLink() {
        return updateLink;
    }

    public void setUpdateLink(String updateLink) {
        this.updateLink = updateLink;
    }
}
