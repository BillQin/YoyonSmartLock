package yoyon.smartlock.standalone.utils;

/**
 * Created by QinBin on 2018/8/10.
 */

import android.content.Context;
import android.text.TextUtils;


import java.util.List;

import yoyon.smartlock.standalone.R;

/**
 * Created by hsg on 14/10/2017.
 */

public class PatternHelper {
    private Context context;
    public static final int MAX_SIZE = 4;
    public  int maxTimes = 5;
    private static final String GESTURE_PWD_KEY = "gesture_pwd_key";

    private String message;
    private String storagePwd;
    private String tmpPwd;
    private int times;
    private boolean isFinish;
    private boolean isOk;

    public PatternHelper(Context context){
        this.context = context;
    }
    public void validateForSetting(List<Integer> hitList) {
        this.isFinish = false;
        this.isOk = false;

        if ((hitList == null) || (hitList.size() < MAX_SIZE)) {
            this.tmpPwd = null;
            this.message = getSizeErrorMsg();
            return;
        }

        //1. draw first time
        if (TextUtils.isEmpty(this.tmpPwd)) {
            this.tmpPwd = convert2String(hitList);
            this.message = getReDrawMsg();
            this.isOk = true;
            return;
        }

        //2. draw second times
        if (this.tmpPwd.equals(convert2String(hitList))) {
            this.message = getSettingSuccessMsg();
            saveToStorage(this.tmpPwd);
            this.isOk = true;
            this.isFinish = true;
        } else {
            this.tmpPwd = null;
            this.message = getDiffPreErrorMsg();
        }
    }

    public void validateForChecking(List<Integer> hitList) {
        this.isOk = false;

        if ((hitList == null) || (hitList.size() < MAX_SIZE)) {
            this.times++;
//            this.isFinish = this.times >= maxTimes;
            this.message = getPwdErrorMsg();
            return;
        }

        this.storagePwd = getFromStorage();
        if (!TextUtils.isEmpty(this.storagePwd) && this.storagePwd.equals(convert2String(hitList))) {
            this.message = getCheckingSuccessMsg();
            this.isOk = true;
            this.isFinish = true;
        } else {
            this.times++;
//            this.isFinish = this.times >= maxTimes;
            this.message = getPwdErrorMsg();
        }
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public boolean isOk() {
        return isOk;
    }

    private String getReDrawMsg() {
        return context.getResources().getString(R.string.set_unlock_pattern_again);
    }

    private String getSettingSuccessMsg() {
        return context.getResources().getString(R.string.set_unlock_pattern_done);
    }

    private String getCheckingSuccessMsg() {
        return context.getResources().getString(R.string.validation_gesture_password_success);
    }

    private String getSizeErrorMsg() {
        return String.format(context.getResources().getString(R.string.unlock_pattern_four_point_tips), MAX_SIZE);
    }

    private String getDiffPreErrorMsg() {
        return context.getResources().getString(R.string.set_unlock_pattern_different);
    }

    private String getPwdErrorMsg() {
        return context.getResources().getString(R.string.validation_gesture_password_fail);
    }

    private String convert2String(List<Integer> hitList) {
        return hitList.toString();
    }

    public void saveToStorage(String gesturePwd) {
        if(gesturePwd != null){
            final String encryptPwd = SecurityUtil.encrypt(gesturePwd);
            SharedPreferencesUtil.getInstance().saveString(GESTURE_PWD_KEY, encryptPwd);
        }else{
            SharedPreferencesUtil.getInstance().saveString(GESTURE_PWD_KEY, null);
        }
    }

    public String getFromStorage() {
        final String result = SharedPreferencesUtil.getInstance().getString(GESTURE_PWD_KEY);
        if(result == null){
            return null;
        }
        return SecurityUtil.decrypt(result);
    }

    public int getRemainTimes() {
        return (times < 5) ? (maxTimes - times) : 0;
    }

    public void setMaxTimes(int times){
        maxTimes = times;
    }
}