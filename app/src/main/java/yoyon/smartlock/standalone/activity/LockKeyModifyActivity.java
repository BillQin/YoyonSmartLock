package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.yoyon.ynblelib.clj.data.BleDevice;
import com.yoyon.ynblelib.yoyon.YnBleKeyPropertyType;
import com.yoyon.ynblelib.yoyon.YnBleManager;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.model.YoyonKey;
import yoyon.smartlock.standalone.utils.DialogManager;
import yoyon.smartlock.standalone.utils.PopupWindowFactory;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2018/7/5.
 */

public class LockKeyModifyActivity extends Activity implements View.OnClickListener{
    private YoyonKey yoyonKey;
    private BleDevice bleDevice;
    private boolean originalProperty;
    private boolean isModifyPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_key_modify);
        YoyonUtils.setTransparentStatusBar(this);
        getDataFromLastActivity();
        initComponent();
    }
    private void getDataFromLastActivity(){
        yoyonKey = (YoyonKey) getIntent().getSerializableExtra("YoyonKey");
        bleDevice = getIntent().getParcelableExtra("BleDevice");
    }

    private void initComponent(){
        LinearLayout returnLayout = findViewById(R.id.lockKeyModifyActivity_returnLayout);
        LinearLayout deleteLayout = findViewById(R.id.lockKeyModifyActivity_deleteLayout);

        RelativeLayout modifyNameLayout = findViewById(R.id.lockKeyModifyActivity_modifyName);
        RelativeLayout modifyPropertyLayout = findViewById(R.id.lockKeyModifyActivity_modifyProperty);
        RelativeLayout modifyPasswordLayout = findViewById(R.id.lockKeyModifyActivity_modifyPassword);

        returnLayout.setOnClickListener(this);
        deleteLayout.setOnClickListener(this);
        modifyNameLayout.setOnClickListener(this);
        modifyPropertyLayout.setOnClickListener(this);
        modifyPasswordLayout.setOnClickListener(this);

        if(yoyonKey.getType() != YoyonKey.PASSWORD_KEY){
            modifyPasswordLayout.setVisibility(View.GONE);
        }
        if(yoyonKey.getType() == YoyonKey.BLUETOOTH_KEY){
            modifyPropertyLayout.setVisibility(View.GONE);
            if(yoyonKey.getRole() == YoyonKey.ADMIN){
                deleteLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lockKeyModifyActivity_returnLayout:
                finish();
                break;
            case R.id.lockKeyModifyActivity_deleteLayout:
                if(YnBleManager.getInstance().isAuthed(bleDevice)) {
                    showConfirmDeleteKeyPopupWindow();
                }else{
                    Toast.makeText(this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.lockKeyModifyActivity_modifyName:
                name = yoyonKey.getName();
                PopupWindowFactory.getInstance().showBluetoothDeviceModifyKeyNamePopupwindow(this, name, nameTextWatcher, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        yoyonKey.setName(name);
                        YoyonKeyDao.getInstance(LockKeyModifyActivity.this).updateKey(yoyonKey);
                        Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.modify_success),Toast.LENGTH_SHORT).show();
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                    }
                });
                break;
            case R.id.lockKeyModifyActivity_modifyProperty:
                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                    originalProperty = (yoyonKey.getRole() == YoyonKey.ADMIN);
                    property = originalProperty;
                    PopupWindowFactory.getInstance().showBluetoothDeviceModifyPropertyPopupwindow(LockKeyModifyActivity.this, originalProperty, isManagerListener, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(property == originalProperty){
                                PopupWindowFactory.getInstance().dismissPopupWindow();
                            }else{
                                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                                    DialogManager.getInstance().showLoadingDialog(LockKeyModifyActivity.this,getResources().getString(R.string.saving),true,false);
                                    YnBleManager.getInstance().bleCmdModifyUser(yoyonKey.getIndexNumber(),yoyonKey.getType(),property ? YnBleKeyPropertyType.ADMIN: YnBleKeyPropertyType.NOT_ADMIN,null,true);
                                }else{
                                    Toast.makeText(LockKeyModifyActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                                    PopupWindowFactory.getInstance().dismissPopupWindow();
                                }
                            }
                        }
                    });
                }else{
                    Toast.makeText(LockKeyModifyActivity.this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.lockKeyModifyActivity_modifyPassword:
                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                    PopupWindowFactory.getInstance().showBluetoothDeviceModifyPasswordPopupwindow(LockKeyModifyActivity.this, passwordTextWatcher, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            InputMethodManager imm = ( InputMethodManager ) view.getContext( ).getSystemService( Context.INPUT_METHOD_SERVICE );
                            if ( imm.isActive( ) ) {
                                imm.hideSoftInputFromWindow( view.getWindowToken( ) , 0 );
                            }
                            if(YnBleManager.getInstance().isAuthed(bleDevice)){
                                isModifyPassword = true;
                                YnBleManager.getInstance().bleCmdModifyUser(yoyonKey.getIndexNumber(),yoyonKey.getType(),yoyonKey.getRole(),password,true);
                            }else{
                                Toast.makeText(LockKeyModifyActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                                PopupWindowFactory.getInstance().dismissPopupWindow();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LockKeyModifyActivity.this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    private boolean property;
    private CompoundButton.OnCheckedChangeListener isManagerListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            property = b;
        }
    };
    private String name;
    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            name = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private String password;
    private TextWatcher passwordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            password = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void showConfirmDeleteKeyPopupWindow(){
        PopupWindowFactory.getInstance().showWarningPopupWindow(LockKeyModifyActivity.this, getResources().getString(R.string.tips), getResources().getString(R.string.delete_key_tips), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupWindowFactory.getInstance().dismissPopupWindow();
                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                    DialogManager.getInstance().showLoadingDialog(LockKeyModifyActivity.this,getResources().getString(R.string.deleting),true,false);
                    YnBleManager.getInstance().bleCmdClearUser((short) yoyonKey.getIndexNumber(),true);
                }else{
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.device_disconnect_do_it_reconnect),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("BleDeviceConnectSuccess");

        filter.addAction("ModifyUser.EOK");
        filter.addAction("ModifyUser.EPWDEXIST");
        filter.addAction("ModifyUser.EADMINFULL");
        filter.addAction("ModifyUser.EFAIL");

        filter.addAction("DeleteBluetoothDeviceKeySuccess");
        filter.addAction("DeleteBluetoothDeviceKeyOnlyAdmin");
        filter.addAction("DeleteBluetoothDeviceKeyFail");
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
    protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case "BleDeviceConnectSuccess":
                    LockKeyModifyActivity.this.bleDevice = intent.getParcelableExtra("BleDevice");
                    break;
                case "DeleteBluetoothDeviceKeyOnlyAdmin":
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.delete_only_admin),Toast.LENGTH_SHORT).show();
                    break;
                case "DeleteBluetoothDeviceKeyFail":
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.delete_fail),Toast.LENGTH_SHORT).show();
                    break;
                case "ModifyUser.EOK":
                    if(isModifyPassword){
                        PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.modify_password_success));
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockKeyModifyActivity.this,1);
                    }else{
                        int keyId = intent.getIntExtra("KeyId",-1);
                        int keyType = intent.getIntExtra("KeyType",-1);
                        int keyProperty = intent.getIntExtra("KeyProperty",0);

                        YoyonKey yoyonKey = YoyonKeyDao.getInstance(LockKeyModifyActivity.this).getTheKeyOfLock(bleDevice.getMac(),keyId);
                        if(yoyonKey == null){
                            yoyonKey = new YoyonKey();
                            yoyonKey.setAttachLockMac(bleDevice.getMac());
                            yoyonKey.setIndexNumber(keyId);
                            yoyonKey.setType(keyType);
                        }
                        yoyonKey.setName(name);
                        yoyonKey.setRole(keyProperty);
                        YoyonKeyDao.getInstance(LockKeyModifyActivity.this).updateKey(yoyonKey);
                        DialogManager.getInstance().dismissLoadingDialog();
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.modify_success),Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "ModifyUser.EPWDEXIST":
                    PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.modify_fail_password_exist));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockKeyModifyActivity.this,2);
                    break;
                case "ModifyUser.EADMINFULL":
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.add_admin_full),Toast.LENGTH_SHORT).show();
                    break;
                case "ModifyUser.EFAIL":
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.modify_fail_only_admin),Toast.LENGTH_SHORT).show();
                    break;
                case "DeleteBluetoothDeviceKeySuccess":
                    DialogManager.getInstance().dismissLoadingDialog();
                    Toast.makeText(LockKeyModifyActivity.this,getResources().getString(R.string.delete_success),Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
