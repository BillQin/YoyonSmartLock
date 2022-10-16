package yoyon.smartlock.standalone.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import yoyon.smartlock.standalone.R;


/**
 * Created by user on 17/10/17.
 */

public class DialogManager {
    private ProgressDialog loadingDialog;
    private AlertDialog messageDialog;
    public static DialogManager sInstance;
    public static synchronized DialogManager getInstance() {
        if (sInstance == null) {
            sInstance = new DialogManager();
        }
        return sInstance;
    }

    public void showLoadingDialog(Context context,boolean backCancelable, boolean outsideCancelable){
        showLoadingDialog(context,"",backCancelable,outsideCancelable,null);
    }

    public void showLoadingDialog(Context context,String message,boolean backCancelable, boolean outsideCancelable){
        showLoadingDialog(context,message,backCancelable,outsideCancelable,null);
    }

    public void showLoadingDialog(final Context context, final String message, final boolean backCancelable, final boolean outsideCancelable,final OnBackKeyDown onBackKeyDown){
        dismissLoadingDialog();
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingDialog = new ProgressDialog(context);
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
                loadingDialog.setCancelable(backCancelable);// 设置是否可以通过点击Back键取消
                loadingDialog.setCanceledOnTouchOutside(outsideCancelable);// 设置在点击Dialog外是否取消Dialog进度条
                if(message!=null && !message.equalsIgnoreCase("")){
                    loadingDialog.setMessage(message);
                }
                if(onBackKeyDown != null){
                    loadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                                onBackKeyDown.onClick();
                            }
                            return false;
                        }
                    });
                }
                loadingDialog.show();
            }
        });
    }

    public void dismissLoadingDialog(){
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    public interface OnBackKeyDown{
        void onClick();
    }

    public void showMessageDialog(Context context,String title){
        dismissMessageDialog();
        messageDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(R.string.ok, null).create();
        messageDialog.show();
    }

    public void showMessageDialog(Context context, String title, DialogInterface.OnClickListener listener){
        dismissMessageDialog();
        messageDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(R.string.ok, listener).create();
        messageDialog.show();
    }

    public void showMessageDialog(Context context,String title,String message){
        dismissMessageDialog();
        messageDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null).create();
        messageDialog.show();
    }

    public void dismissMessageDialog(){
        if(messageDialog != null && messageDialog.isShowing()){
            messageDialog.dismiss();
        }
    }
}
