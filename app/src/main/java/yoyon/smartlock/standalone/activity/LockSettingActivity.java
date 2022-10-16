package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.yoyon.ynblelib.clj.data.BleDevice;
import com.yoyon.ynblelib.yoyon.YnBleManager;
import com.yoyon.ynblelib.yoyon.YnBleOptions;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.db.YoyonLockDao;
import yoyon.smartlock.standalone.db.YoyonOperationLogDao;
import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.utils.DialogManager;
import yoyon.smartlock.standalone.utils.PopupWindowFactory;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2018/6/29.
 */
public class LockSettingActivity extends Activity implements View.OnClickListener{
    private YoyonLock yoyonLock;
    private BleDevice bleDevice;
    private PopupWindow popupWindow;
    private Switch switch_passageway,switch_antiThief, switch_humanDetect,switch_keyDetect,
            switch_fingerprintSmartUpdate,switch_rfic,switch_unlockTone;
    private YnBleOptions ynBleOptions;
    private View changingView;
    private RelativeLayout passagewayLayout,antiThiefLayout,humanDetectLayout,keyDetectLayout,
            fingerprintSmartUpdateLayout,rficLayout,unlockToneLayout,languageLayout,volumeLayout;
    private TextView tv_language;
    private SeekBar volumeSeekBar;
    private int volumeSeekBarStartProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_setting);
        YoyonUtils.setTransparentStatusBar(this);
        getDataFromLastActivity();
        initComponent();
    }
    private void getDataFromLastActivity(){
        yoyonLock = (YoyonLock) getIntent().getSerializableExtra("YoyonLock");
        bleDevice = getIntent().getParcelableExtra("BleDevice");

    }
    private void initComponent(){
        LinearLayout returnLayout = findViewById(R.id.lockSettingActivity_returnLayout);
        LinearLayout shareLayout = findViewById(R.id.lockSettingActivity_shareLayout);
        RelativeLayout deleteLayout = findViewById(R.id.lockSettingActivity_deleteLayout);

        returnLayout.setOnClickListener(this);
        deleteLayout.setOnClickListener(this);

        if(yoyonLock.getRole() == YoyonLock.ADMIN){
            shareLayout.setOnClickListener(this);
        }else{
            shareLayout.setVisibility(View.GONE);
        }

        passagewayLayout = findViewById(R.id.settingActivity_passwayLayout);
        antiThiefLayout = findViewById(R.id.settingActivity_antiThiefLayout);
        humanDetectLayout = findViewById(R.id.settingActivity_humanDetectLayout);
        keyDetectLayout = findViewById(R.id.settingActivity_keyDetectLayout);
        fingerprintSmartUpdateLayout = findViewById(R.id.settingActivity_fingerprintSmartUpdateLayout);
        rficLayout = findViewById(R.id.settingActivity_rficLayout);
        unlockToneLayout = findViewById(R.id.settingActivity_unlockToneLayout);
        languageLayout = findViewById(R.id.settingActivity_languageLayout);
        volumeLayout = findViewById(R.id.settingActivity_volumeLayout);
        tv_language = findViewById(R.id.settingActivity_language);

        passagewayLayout.setOnClickListener(this);
        antiThiefLayout.setOnClickListener(this);
        humanDetectLayout.setOnClickListener(this);
        keyDetectLayout.setOnClickListener(this);
        fingerprintSmartUpdateLayout.setOnClickListener(this);
        rficLayout.setOnClickListener(this);
        unlockToneLayout.setOnClickListener(this);
        languageLayout.setOnClickListener(this);
        volumeLayout.setOnClickListener(this);

        if(yoyonLock.getCapability() != null){
            if(yoyonLock.getCapability().isPassagewayCapability()){
                passagewayLayout.setVisibility(View.VISIBLE);
            }else{
                passagewayLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isAntiThiefCapability()){
                antiThiefLayout.setVisibility(View.VISIBLE);
            }else{
                antiThiefLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isHumanDetectCapability()){
                humanDetectLayout.setVisibility(View.VISIBLE);
            }else{
                humanDetectLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isKeyDetectCapability()){
                keyDetectLayout.setVisibility(View.VISIBLE);
            }else{
                keyDetectLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isSetFingerprintSmartUpdateCapability()){
                fingerprintSmartUpdateLayout.setVisibility(View.VISIBLE);
            }else{
                fingerprintSmartUpdateLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isSetRFICCapability()){
                rficLayout.setVisibility(View.VISIBLE);
            }else{
                rficLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isSetVolumeCapability()){
                unlockToneLayout.setVisibility(View.VISIBLE);
            }else{
                unlockToneLayout.setVisibility(View.GONE);
            }
            if(yoyonLock.getCapability().isEnglishCapability() & yoyonLock.getCapability().isChineseCapability()){
                languageLayout.setVisibility(View.VISIBLE);
            }else {
                languageLayout.setVisibility(View.GONE);
            }

            if(yoyonLock.getCapability().isSetVolumeCapability()){
                volumeLayout.setVisibility(View.VISIBLE);
            }else {
                volumeLayout.setVisibility(View.GONE);
            }

        }

        switch_passageway = findViewById(R.id.settingActivity_passway);
        switch_antiThief = findViewById(R.id.settingActivity_antiThief);
        switch_humanDetect = findViewById(R.id.settingActivity_humanDetect);
        switch_keyDetect = findViewById(R.id.settingActivity_keyDetect);
        switch_fingerprintSmartUpdate = findViewById(R.id.settingActivity_fingerprintSmartUpdate);
        switch_rfic = findViewById(R.id.settingActivity_rfic);
        switch_unlockTone = findViewById(R.id.settingActivity_unlockTone);
        volumeSeekBar = findViewById(R.id.settingActivity_volumeSeekBar);

        switch_passageway.setEnabled(false);
        switch_antiThief.setEnabled(false);
        switch_humanDetect.setEnabled(false);
        switch_keyDetect.setEnabled(false);
        switch_fingerprintSmartUpdate.setEnabled(false);
        switch_rfic.setEnabled(false);
        switch_unlockTone.setEnabled(false);
        volumeSeekBar.setEnabled(false);

        passagewayLayout.setAlpha(0.4f);
        antiThiefLayout.setAlpha(0.4f);
        humanDetectLayout.setAlpha(0.4f);
        keyDetectLayout.setAlpha(0.4f);
        languageLayout.setAlpha(0.4f);
        fingerprintSmartUpdateLayout.setAlpha(0.4f);
        rficLayout.setAlpha(0.4f);
        unlockToneLayout.setAlpha(0.4f);
        volumeLayout.setAlpha(0.4f);

        switch_passageway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_passageway;
                ynBleOptions.setPassageway(switch_passageway.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        switch_antiThief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_antiThief;
                ynBleOptions.setAntiThief(switch_antiThief.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        switch_humanDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_humanDetect;
                ynBleOptions.setAntiThief(switch_humanDetect.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        switch_keyDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_keyDetect;
                ynBleOptions.setKeyDetect(switch_keyDetect.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });



        switch_fingerprintSmartUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_fingerprintSmartUpdate;
                ynBleOptions.setFingerprintSmartUpdate(switch_fingerprintSmartUpdate.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        switch_rfic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_rfic;
                ynBleOptions.setRfic(switch_rfic.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        switch_unlockTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = switch_unlockTone;
                ynBleOptions.setUnlockWarningTone(switch_unlockTone.isChecked());
                YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                showModifyLoadingDialog();
            }
        });
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                volumeSeekBarStartProgress = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(volumeSeekBarStartProgress != seekBar.getProgress()){
                    changingView = volumeSeekBar;
                    ynBleOptions.setVolumeSetting(seekBar.getProgress());
                    YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                    showModifyLoadingDialog();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lockSettingActivity_returnLayout:
                finish();
                break;
            case R.id.lockSettingActivity_shareLayout:
                final String shareCmd = YnBleManager.getInstance().genShareCmd(yoyonLock.getMac());
                Bitmap qrBitmap = encodeAsBitmap(","+shareCmd+","+yoyonLock.getMac());
                showQRCodePopupWindow(qrBitmap);
                break;
            case R.id.lockSettingActivity_deleteLayout:
                showUnbindPopupWindow();
                break;
            case R.id.settingActivity_passwayLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if (!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_antiThiefLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_humanDetectLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_keyDetectLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_fingerprintSmartUpdateLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_rficLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_unlockToneLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settingActivity_languageLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }else{
                    showSetLanguageDialog();
                }
                break;
            case R.id.settingActivity_volumeLayout:
                if(yoyonLock.getRole() != YoyonLock.ADMIN){
                    Toast.makeText(this,getResources().getString(R.string.non_admin_cannot_modify),Toast.LENGTH_SHORT).show();
                }else if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private void clearLockInfo(){
        YoyonKeyDao.getInstance(this).deleteKeyOfLock(yoyonLock.getMac());
        YoyonOperationLogDao.getInstance(this).deleteOperationLogOfLock(yoyonLock.getMac());
        YoyonLockDao.getInstance(this).deleteLock(yoyonLock.getMac());
    }
    private void showUnbindPopupWindow(){
        if(yoyonLock.getRole() == YoyonLock.ADMIN){
            if(YnBleManager.getInstance().isAuthed(bleDevice)){
                PopupWindowFactory.getInstance().showWarningPopupWindow(this, getResources().getString(R.string.tips), getResources().getString(R.string.delete_connected_device_tips), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        if(YnBleManager.getInstance().isAuthed(bleDevice)){
                            YnBleManager.getInstance().bleCmdUnBind(true);
                        }else{
                            Toast.makeText(LockSettingActivity.this,getResources().getString(R.string.device_disconnect_do_it_reconnect),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                PopupWindowFactory.getInstance().showWarningPopupWindow(this, getResources().getString(R.string.tips), getResources().getString(R.string.delete_disconnect_device_tips), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        clearLockInfo();
                        startActivity(new Intent(LockSettingActivity.this,MainActivity.class));
                    }
                });
            }
        }else{
            if(YnBleManager.getInstance().isAuthed(bleDevice)){
                PopupWindowFactory.getInstance().showWarningPopupWindow(this, getResources().getString(R.string.tips), getResources().getString(R.string.delete_connected_non_admin_device_tips), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        if(YnBleManager.getInstance().isAuthed(bleDevice)){
                            YnBleManager.getInstance().bleCmdUnBind(true);
                        }else{
                            Toast.makeText(LockSettingActivity.this,getResources().getString(R.string.device_disconnect_do_it_reconnect),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                PopupWindowFactory.getInstance().showWarningPopupWindow(this, getResources().getString(R.string.tips), getResources().getString(R.string.delete_disconnect_device_tips), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        clearLockInfo();
                        startActivity(new Intent(LockSettingActivity.this,MainActivity.class));
                    }
                });
            }
        }
    }
    private void showQRCodePopupWindow(Bitmap bitmap){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        View contentView = LayoutInflater.from(this).inflate(R.layout.popup_qrcode,null);
        TextView qrTips = contentView.findViewById(R.id.popup_qrcode_tips);
        qrTips.setText(R.string.device_share_tips);
        ImageView qrCore = contentView.findViewById(R.id.popup_qrcode_image);
        qrCore.setImageBitmap(bitmap);

        View parent = getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                YnBleManager.getInstance().setFakeConnect(false);
                YoyonUtils.darkenBackground(1f,LockSettingActivity.this);
            }
        });
        popupWindow.setTouchable(true); // 设置popupwindow可点击
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true); // 设置popupwindow外部可点击
        popupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        YoyonUtils.darkenBackground(0.5f,this);
        YnBleManager.getInstance().disconnectAllDevice();
        YnBleManager.getInstance().setFakeConnect(true);
    }
    private Bitmap encodeAsBitmap(String str){
        Bitmap bitmap = null;
        BitMatrix result;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            try {
                str = new String(str.getBytes("UTF-8"),"ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 550, 550);
            // 使用 ZXing Android Embedded 要写的代码
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e){
            e.printStackTrace();
        } catch (IllegalArgumentException iae){
            return null;
        }
        return bitmap;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(popupWindow!=null&&popupWindow.isShowing()){
            return false;
        }
        return super.dispatchTouchEvent(event);
    }

    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("BleDeviceConnectSuccess");
        filter.addAction("BleDeviceDisconnect");
        filter.addAction("BluetoothDeviceUnbindSuccess");
        filter.addAction("GetOptionsSuccess");
        filter.addAction("SetOptionsSuccess");
        filter.addAction("SetOptionsFail");
        filter.addAction("GetCapabilitySuccess");
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if(timer != null){
            timer.cancel();
        }
    }
    protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "BleDeviceConnectSuccess":
                    bleDevice = intent.getParcelableExtra("BleDevice");
                    if(ynBleOptions != null){
                        if(yoyonLock.getRole() == YoyonLock.ADMIN){
                            switch_passageway.setEnabled(true);
                            switch_antiThief.setEnabled(true);
                            switch_humanDetect.setEnabled(true);
                            switch_keyDetect.setEnabled(true);
                            switch_fingerprintSmartUpdate.setEnabled(true);
                            switch_rfic.setEnabled(true);
                            switch_unlockTone.setEnabled(true);
                            volumeSeekBar.setEnabled(true);
                            passagewayLayout.setAlpha(1.0f);
                            antiThiefLayout.setAlpha(1.0f);
                            humanDetectLayout.setAlpha(1.0f);
                            keyDetectLayout.setAlpha(1.0f);
                            fingerprintSmartUpdateLayout.setAlpha(1.0f);
                            rficLayout.setAlpha(1.0f);
                            unlockToneLayout.setAlpha(1.0f);
                            languageLayout.setAlpha(1.0f);
                            volumeLayout.setAlpha(1.0f);
                        }
                        switch_passageway.setChecked(ynBleOptions.isPassageway());
                        switch_antiThief.setChecked(ynBleOptions.isAntiThief());
                        switch_humanDetect.setChecked(ynBleOptions.isHumanDetect());
                        switch_keyDetect.setChecked(ynBleOptions.isKeyDetect());
                        switch_fingerprintSmartUpdate.setChecked(ynBleOptions.isFingerprintSmartUpdate());
                        switch_rfic.setChecked(ynBleOptions.isRfic());
                        switch_unlockTone.setChecked(ynBleOptions.isUnlockWarningTone());
                        volumeSeekBar.setProgress(ynBleOptions.getVolumeSetting());
                        if(ynBleOptions.getLanguage() == YnBleOptions.CHINESE){
                            tv_language.setText(R.string.chinese);
                        }else if(ynBleOptions.getLanguage() == YnBleOptions.ENGLISH){
                            tv_language.setText(R.string.english);
                        }
                    }
                    break;
                case "GetOptionsSuccess":
                    ynBleOptions = (YnBleOptions) intent.getSerializableExtra("YnBleOptions");
                    if(yoyonLock.getRole() == YoyonLock.ADMIN){
                        switch_passageway.setEnabled(true);
                        switch_antiThief.setEnabled(true);
                        switch_humanDetect.setEnabled(true);
                        switch_keyDetect.setEnabled(true);
                        switch_fingerprintSmartUpdate.setEnabled(true);
                        switch_rfic.setEnabled(true);
                        switch_unlockTone.setEnabled(true);
                        volumeSeekBar.setEnabled(true);
                        passagewayLayout.setAlpha(1.0f);
                        antiThiefLayout.setAlpha(1.0f);
                        humanDetectLayout.setAlpha(1.0f);
                        keyDetectLayout.setAlpha(1.0f);
                        fingerprintSmartUpdateLayout.setAlpha(1.0f);
                        rficLayout.setAlpha(1.0f);
                        unlockToneLayout.setAlpha(1.0f);
                        languageLayout.setAlpha(1.0f);
                        volumeLayout.setAlpha(1.0f);
                    }
                    switch_passageway.setChecked(ynBleOptions.isPassageway());
                    switch_antiThief.setChecked(ynBleOptions.isAntiThief());
                    switch_humanDetect.setChecked(ynBleOptions.isHumanDetect());
                    switch_keyDetect.setChecked(ynBleOptions.isKeyDetect());
                    switch_fingerprintSmartUpdate.setChecked(ynBleOptions.isFingerprintSmartUpdate());
                    switch_rfic.setChecked(ynBleOptions.isRfic());
                    switch_unlockTone.setChecked(ynBleOptions.isUnlockWarningTone());
                    volumeSeekBar.setProgress(ynBleOptions.getVolumeSetting());
                    if(ynBleOptions.getLanguage() == YnBleOptions.CHINESE){
                        tv_language.setText(R.string.chinese);
                    }else if(ynBleOptions.getLanguage() == YnBleOptions.ENGLISH){
                        tv_language.setText(R.string.english);
                    }
                    break;
                case "SetOptionsSuccess":
                    if(changingView == switch_passageway){
                        Toast.makeText(context,switch_passageway.isChecked()?R.string.always_unlock_on:R.string.always_unlock_off,Toast.LENGTH_SHORT).show();
                    }else if(changingView == switch_antiThief){
                        Toast.makeText(context,switch_antiThief.isChecked()?R.string.anti_thief_alarm_on:R.string.anti_thief_alarm_off,Toast.LENGTH_SHORT).show();
                    }else if(changingView == switch_humanDetect){
                        Toast.makeText(context,switch_humanDetect.isChecked()?R.string.human_detect_on:R.string.human_detect_off,Toast.LENGTH_SHORT).show();
                    }else if(changingView == switch_keyDetect){
                        Toast.makeText(context,switch_keyDetect.isChecked()?R.string.key_detect_on:R.string.key_detect_off,Toast.LENGTH_SHORT).show();
                    }else if(changingView == switch_fingerprintSmartUpdate){
                        Toast.makeText(context,switch_fingerprintSmartUpdate.isChecked()?R.string.fingerprint_smart_update_on:R.string.fingerprint_smart_update_off,Toast.LENGTH_SHORT).show();
                        if(switch_fingerprintSmartUpdate.isChecked()){
                            DialogManager.getInstance().showMessageDialog(context,context.getString(R.string.tips),context.getString(R.string.fingerprint_smart_update_warning));
                        }
                    }else if(changingView == switch_rfic){
                        Toast.makeText(context,switch_rfic.isChecked()?R.string.set_rfic_on:R.string.set_rfic_off,Toast.LENGTH_SHORT).show();
                        if(switch_rfic.isChecked()){
                            DialogManager.getInstance().showMessageDialog(context,context.getString(R.string.tips),context.getString(R.string.rfic_warning));
                        }
                    }else if(changingView == switch_unlockTone){
                        Toast.makeText(context,switch_unlockTone.isChecked()?R.string.unlock_warning_tone_on:R.string.unlock_warning_tone_off,Toast.LENGTH_SHORT).show();
                    }else if(changingView == volumeSeekBar){
                        Toast.makeText(context,R.string.set_volume_success,Toast.LENGTH_SHORT).show();
                    }else if(changingView == languageLayout){
                        if(ynBleOptions.getLanguage() == YnBleOptions.CHINESE){
                            Toast.makeText(context,R.string.set_lock_language_chinese,Toast.LENGTH_SHORT).show();
                        }else if(ynBleOptions.getLanguage() == YnBleOptions.ENGLISH){
                            Toast.makeText(context,R.string.set_lock_language_english,Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(timer != null){
                        timer.cancel();
                    }
                    if(languageDialog!=null){
                        languageDialog.dismiss();
                    }
                    DialogManager.getInstance().dismissLoadingDialog();
                    break;
                case "SetOptionsFail":
                    if(timer != null){
                        timer.cancel();
                    }
                    if(languageDialog!=null){
                        languageDialog.dismiss();
                    }
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockSettingActivity.this,getResources().getString(R.string.modify_fail),Toast.LENGTH_SHORT).show();
                    break;
                case "BleDeviceDisconnect":
                    switch_passageway.setEnabled(false);
                    switch_antiThief.setEnabled(false);
                    switch_humanDetect.setEnabled(false);
                    switch_keyDetect.setEnabled(false);
                    switch_fingerprintSmartUpdate.setEnabled(false);
                    switch_rfic.setEnabled(false);
                    switch_unlockTone.setEnabled(false);
                    volumeSeekBar.setEnabled(false);
                    passagewayLayout.setAlpha(0.4f);
                    antiThiefLayout.setAlpha(0.4f);
                    humanDetectLayout.setAlpha(0.4f);
                    keyDetectLayout.setAlpha(0.4f);
                    languageLayout.setAlpha(0.4f);
                    fingerprintSmartUpdateLayout.setAlpha(0.4f);
                    rficLayout.setAlpha(0.4f);
                    unlockToneLayout.setAlpha(0.4f);
                    volumeLayout.setAlpha(0.4f);
                    break;
                case "BluetoothDeviceUnbindSuccess":
                    clearLockInfo();
                    startActivity(new Intent(LockSettingActivity.this,MainActivity.class));
                    break;
                default:
                    break;
            }
        }
    };
    private Timer timer;
    private TimerTask task;
    private void showModifyLoadingDialog(){
        DialogManager.getInstance().showLoadingDialog(LockSettingActivity.this,getResources().getString(R.string.modifying),false,false);
        timer = new Timer();
        task  = new TimerTask() {
            @Override
            public void run() {
                DialogManager.getInstance().dismissLoadingDialog();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LockSettingActivity.this,getResources().getString(R.string.modify_fail),Toast.LENGTH_SHORT).show();
                        if(changingView == switch_passageway){
                            switch_passageway.setChecked(!switch_passageway.isChecked());
                            ynBleOptions.setPassageway(switch_passageway.isChecked());
                        }else if(changingView == switch_antiThief){
                            switch_antiThief.setChecked(!switch_antiThief.isChecked());
                            ynBleOptions.setAntiThief(switch_antiThief.isChecked());
                        }else if(changingView == switch_humanDetect){
                            switch_humanDetect.setChecked(!switch_humanDetect.isChecked());
                            ynBleOptions.setHumanDetect(switch_humanDetect.isChecked());
                        }else if(changingView == switch_keyDetect){
                            switch_keyDetect.setChecked(!switch_keyDetect.isChecked());
                            ynBleOptions.setHumanDetect(switch_keyDetect.isChecked());
                        }else if(changingView == switch_fingerprintSmartUpdate){
                            switch_fingerprintSmartUpdate.setChecked(!switch_fingerprintSmartUpdate.isChecked());
                            ynBleOptions.setFingerprintSmartUpdate(switch_fingerprintSmartUpdate.isChecked());
                        }else if(changingView == switch_rfic){
                            switch_rfic.setChecked(!switch_rfic.isChecked());
                            ynBleOptions.setRfic(switch_rfic.isChecked());
                        }else if(changingView == switch_unlockTone){
                            switch_unlockTone.setChecked(!switch_unlockTone.isChecked());
                            ynBleOptions.setUnlockWarningTone(switch_unlockTone.isChecked());
                        }else if(changingView == volumeSeekBar){
                            volumeSeekBar.setProgress(volumeSeekBarStartProgress);
                            ynBleOptions.setVolumeSetting(volumeSeekBarStartProgress);
                        }
                    }
                });
            }
        };
        timer.schedule(task,3000);
    }
    private void showModifyLanguageLoadingDialog(){
        DialogManager.getInstance().showLoadingDialog(LockSettingActivity.this,getResources().getString(R.string.modifying),false,false);
        timer = new Timer();
        task  = new TimerTask() {
            @Override
            public void run() {
                DialogManager.getInstance().dismissLoadingDialog();
                if(languageDialog !=null){
                    languageDialog.dismiss();
                }
            }
        };
        timer.schedule(task,3000);
    }
    private AlertDialog languageDialog;
    private void showSetLanguageDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_language,null,false);
        languageDialog = new AlertDialog.Builder(this).setView(view).create();

        RadioButton rb_chinese = view.findViewById(R.id.set_language_chinese);
        RadioButton rb_english = view.findViewById(R.id.set_language_english);
        Button btn_cancel = view.findViewById(R.id.set_language_cancel);
        Button btn_ok = view.findViewById(R.id.set_language_ok);

        if(ynBleOptions.getLanguage() == YnBleOptions.CHINESE){
            rb_chinese.setChecked(true);
        }else if(ynBleOptions.getLanguage() == YnBleOptions.ENGLISH){
            rb_english.setChecked(true);
        }
        rb_chinese.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    ynBleOptions.setLanguage(YnBleOptions.CHINESE);
                }
            }
        });
        rb_english.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    ynBleOptions.setLanguage(YnBleOptions.ENGLISH);
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                languageDialog.dismiss();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changingView = languageLayout;
                if(!YnBleManager.getInstance().isAuthed(bleDevice)){
                    Toast.makeText(LockSettingActivity.this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }else{
                    showModifyLanguageLoadingDialog();
                    YnBleManager.getInstance().bleCmdSetOptions(ynBleOptions,true);
                }
            }
        });
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4
        languageDialog.getWindow().setLayout((getWindow().getDecorView().getWidth()/4*3),LinearLayout.LayoutParams.WRAP_CONTENT);
        languageDialog.show();
    }
}
