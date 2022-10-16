package yoyon.smartlock.standalone.utils;

/**
 * Created by QinBin on 2018/8/9.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

//import cn.bmob.v3.BmobQuery;
//import cn.bmob.v3.exception.BmobException;
//import cn.bmob.v3.listener.QueryListener;
import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.model.UpdateInfo;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2016/12/15 12:03
 * @detail 版本号工具类
 */
public class VersionUtils {
    /**
     * 版本号对比
     *
     * @param oldVersion
     * @param newVersion
     * @return error : 返回-2 既传入版本号格式有误
     * oldVersion > newVersion  return -1
     * oldVersion = newVersion  return 0
     * oldVersion < newVersion  return 1
     */
    public static int compareVersions(String oldVersion, String newVersion) {
        //返回结果: -2 ,-1 ,0 ,1
        int result = 0;
        String matchStr = "[0-9]+(\\.[0-9]+)*";
        oldVersion = oldVersion.trim();
        newVersion = newVersion.trim();
        //非版本号格式,返回error
        if (!oldVersion.matches(matchStr) || !newVersion.matches(matchStr)) {
            return -2;
        }
        String[] oldVs = oldVersion.split("\\.");
        String[] newVs = newVersion.split("\\.");
        int oldLen = oldVs.length;
        int newLen = newVs.length;
        int len = oldLen > newLen ? oldLen : newLen;
        for (int i = 0; i < len; i++) {
            if (i < oldLen && i < newLen) {
                int o = Integer.parseInt(oldVs[i]);
                int n = Integer.parseInt(newVs[i]);
                if (o > n) {
                    result = -1;
                    break;
                } else if (o < n) {
                    result = 1;
                    break;
                }
            } else {
                if (oldLen > newLen) {
                    for (int j = i; j < oldLen; j++) {
                        if (Integer.parseInt(oldVs[j]) > 0) {
                            result = -1;
                        }
                    }
                    break;
                } else if (oldLen < newLen) {
                    for (int j = i; j < newLen; j++) {
                        if (Integer.parseInt(newVs[j]) > 0) {
                            result = 1;
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }
    /**
     * 获取当前本地apk的版本
     *
     * @param mContext
     * @return
     */
    public static String getVersionName(Context mContext) {
        String versionName = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionName = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    public static void checkUpdate(final Activity context, final boolean isInitiative){
        Toast.makeText(context,context.getResources().getString(R.string.latest_version),Toast.LENGTH_SHORT).show();
        /*
        //查找Person表里面id为6b6c11c537的数据
        BmobQuery<UpdateInfo> bmobQuery = new BmobQuery<>();
        bmobQuery.getObject("YOgq888K", new QueryListener<UpdateInfo>() {
            @Override
            public void done(final UpdateInfo updateInfo, BmobException e) {
                if(updateInfo != null){
                    String oldVersionName = VersionUtils.getVersionName(context);
                    String newVersionName = updateInfo.getVersionName();
                    boolean forceUpdate = updateInfo.isForceUpdate();
                    final String updateLink = updateInfo.getUpdateLink();
                    int result = VersionUtils.compareVersions(oldVersionName,newVersionName);
                    switch (result){
                        case 1://oldVersion < newVersion
                            if(forceUpdate){
                                new AlertDialog.Builder(context)
                                        .setTitle(context.getResources().getString(R.string.too_low_version))
                                        .setMessage(context.getResources().getString(R.string.new_version)+newVersionName+"\n"+context.getResources().getString(R.string.current_version)+"+oldVersionName")
                                        .setCancelable(false)
                                        .setPositiveButton(context.getResources().getString(R.string.go_to_download), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent();
                                                intent.setData(Uri.parse(updateLink));//Url 就是你要打开的网址
                                                intent.setAction(Intent.ACTION_VIEW);
                                                context.startActivity(intent); //启动浏览器
                                                context.finish();
                                            }
                                        }).create().show();
                            }else{
                                new AlertDialog.Builder(context)
                                        .setTitle(context.getResources().getString(R.string.low_version))
                                        .setMessage(context.getResources().getString(R.string.new_version)+newVersionName+"\n"+context.getResources().getString(R.string.current_version)+"+oldVersionName")
                                        .setNegativeButton(context.getResources().getString(R.string.cancel),null)
                                        .setPositiveButton(context.getResources().getString(R.string.go_to_download), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent();
                                                intent.setData(Uri.parse(updateLink));//Url 就是你要打开的网址
                                                intent.setAction(Intent.ACTION_VIEW);
                                                context.startActivity(intent); //启动浏览器
                                            }
                                        }).create().show();
                            }
                            break;
                        case 0://oldVersion = newVersion
                            if(isInitiative){
                                Toast.makeText(context,context.getResources().getString(R.string.latest_version),Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case -1://oldVersion > newVersion
                            if(isInitiative){
                                Toast.makeText(context,context.getResources().getString(R.string.latest_version),Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case -2:
                        default:
                            break;
                    }
                }
            }
        });
         */
    }
}