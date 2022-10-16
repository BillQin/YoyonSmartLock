package yoyon.smartlock.standalone.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import yoyon.smartlock.standalone.MyApplication;


/**
 * Created by hsg on 14/10/2017.
 */

public class SharedPreferencesUtil {
    private static SharedPreferencesUtil instance;

    private SharedPreferences.Editor editor;
    private SharedPreferences prefer;

    public SharedPreferencesUtil() {
        this.prefer = PreferenceManager.getDefaultSharedPreferences(MyApplication.getmContext());
        this.editor = this.prefer.edit();
    }

    public static SharedPreferencesUtil getInstance() {
        if (instance == null) {
            synchronized (SharedPreferencesUtil.class) {
                if (instance == null) {
                    instance = new SharedPreferencesUtil();
                }
            }
        }

        return instance;
    }

    public void saveString(String name, String data) {
        this.editor.putString(name, data);
        this.editor.commit();
    }

    public String getString(String name) {
        return this.prefer.getString(name, null);
    }

    public void saveInt(String name,int data){
        this.editor.putInt(name, data);
    }

    public int getInt(String name){
        return this.prefer.getInt(name,0);
    }

    public void saveBoolean(String name,boolean data){
        this.editor.putBoolean(name,data);
        this.editor.commit();
    }

    public boolean getBoolean(String name){
        return this.prefer.getBoolean(name,false);
    }

    public void setRemainVerifyPatternPasswordTimes(int times){
        this.editor.putInt("RemainVerifyPatternPasswordTimes",times);
        this.editor.commit();
    }
    public void getRemainVerifyPatternPasswordTimes(){
        this.prefer.getInt("RemainVerifyPatternPasswordTimes",0);
    }
    public void setEnablePatternPassword(boolean enable){
        this.editor.putBoolean("EnablePatternPassword",enable);
        this.editor.commit();
    }
    public boolean isEnablePatternPassword(){
        return this.prefer.getBoolean("EnablePatternPassword",false);
    }
}