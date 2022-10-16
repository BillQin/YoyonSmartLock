package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.yoyon.ynblelib.clj.data.BleDevice;
import com.yoyon.ynblelib.yoyon.YnBleCapability;
import com.yoyon.ynblelib.yoyon.YnBleKeyPropertyType;
import com.yoyon.ynblelib.yoyon.YnBleKeyType;
import com.yoyon.ynblelib.yoyon.YnBleManager;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.LockKeyListViewAdapter;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.model.YoyonKey;
import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.utils.PopupWindowFactory;
import yoyon.smartlock.standalone.utils.SoundPlayUtils;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2018/3/31.
 */

public class LockManageKeyActivity extends Activity implements View.OnClickListener{
    private YoyonLock yoyonLock;
    private BleDevice bleDevice;

    private List<YoyonKey> yoyonKeys;
    private ListView listView_keys;
    private LockKeyListViewAdapter mAdapter;
    private LinearLayout noKeyTips;
    private View btn_enrollPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YoyonUtils.setTransparentStatusBar(this);
        setContentView(R.layout.activity_lock_manage_key);
        getDataFromLastActivity();
        initComponent();
    }
    private void getDataFromLastActivity(){
        yoyonLock = (YoyonLock) getIntent().getSerializableExtra("YoyonLock");
        bleDevice = getIntent().getParcelableExtra("BleDevice");
    }
    private void initComponent(){
        LinearLayout returnLayout = findViewById(R.id.bluetoothDeviceKeyActivity_returnLayout);
        LinearLayout addKeyLayout = findViewById(R.id.bluetoothDeviceKeyActivity_addKeyLayout);
        listView_keys = findViewById(R.id.bluetoothDeviceKeyActivity_keyListView);

        returnLayout.setOnClickListener(this);
        addKeyLayout.setOnClickListener(this);
        listView_keys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                YoyonKey yoyonKey = (YoyonKey) parent.getItemAtPosition(position);
                switch (yoyonKey.getType()){
                    case YoyonKey.FINGERPRINT_KEY:
                    case YoyonKey.PASSWORD_KEY:
                    case YoyonKey.BLUETOOTH_KEY:
                    case YoyonKey.RFIC_KEY:
                        Intent intent = new Intent(LockManageKeyActivity.this, LockKeyModifyActivity.class);
                        intent.putExtra("YoyonKey", yoyonKey);
                        intent.putExtra("BleDevice", bleDevice);
                        startActivity(intent);
                    break;
                    case YoyonKey.PHYSICS_KEY:  //物理钥匙，无法操作
                        Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.lock_key),Toast.LENGTH_SHORT).show();

                        break;
                    default:
                        break;
                }
            }
        });
        noKeyTips = findViewById(R.id.bluetoothDeviceKeyActivity_noKeyTips);
        yoyonKeys = YoyonKeyDao.getInstance(this).getAllKeyOfLock(yoyonLock.getMac());
        if(yoyonKeys.size() > 0){
            noKeyTips.setVisibility(View.INVISIBLE);
        }else{
            noKeyTips.setVisibility(View.VISIBLE);
        }
        mAdapter = new LockKeyListViewAdapter(this,yoyonKeys);
        listView_keys.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bluetoothDeviceKeyActivity_returnLayout:
                finish();
                break;
            case R.id.bluetoothDeviceKeyActivity_addKeyLayout:
                PopupWindowFactory.getInstance().showBluetoothDeviceAddKeyPopupwindow(LockManageKeyActivity.this, null, yoyonLock.getCapability(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                            PopupWindowFactory.getInstance().showBluetoothDeviceAddFingerprintPopupwindow(LockManageKeyActivity.this, false,keyNameTextWatcher, keyIsManagerListener, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                                        YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.FINGERPRINT, keyIsManager ? YnBleKeyPropertyType.ADMIN : YnBleKeyPropertyType.NOT_ADMIN , null,true);
                                    }else{
                                        PopupWindowFactory.getInstance().dismissPopupWindow();
                                        Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, onPopupWindowDismissListener);
                        } else {
                            Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                            PopupWindowFactory.getInstance().showBluetoothDeviceAddPasswordPopupwindow(LockManageKeyActivity.this, false,keyNameTextWatcher, keyIsManagerListener, passwordTextWatcher, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                                        YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.PASSWORD, keyIsManager ? YnBleKeyPropertyType.ADMIN : YnBleKeyPropertyType.NOT_ADMIN, password,true);
                                        btn_enrollPassword = view;
                                        btn_enrollPassword.setEnabled(false);
                                        btn_enrollPassword.setAlpha(0.2f);
                                    }else{
                                        PopupWindowFactory.getInstance().dismissPopupWindow();
                                        Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, onPopupWindowDismissListener);
                        } else {
                            Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                            PopupWindowFactory.getInstance().showBluetoothDeviceAddRFICPopupwindow(LockManageKeyActivity.this, false,keyNameTextWatcher, keyIsManagerListener, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
                                    if (YnBleManager.getInstance().isAuthed(bleDevice)) {
                                        YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.RFIC, keyIsManager ? YnBleKeyPropertyType.ADMIN : YnBleKeyPropertyType.NOT_ADMIN, null,true);
                                    }else{
                                        PopupWindowFactory.getInstance().dismissPopupWindow();
                                        Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, onPopupWindowDismissListener);
                        } else {
                            Toast.makeText(LockManageKeyActivity.this, getResources().getString(R.string.after_connect), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                    }
                }, new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        YoyonUtils.darkenBackground(1f, LockManageKeyActivity.this);
                    }
                });
                break;
            default:
                break;
        }
    }
    int i = 0;
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction("BleDeviceConnectSuccess");

        filter.addAction("YoyonKeysChange");

        filter.addAction("EnrollUser.EOK");
        filter.addAction("EnrollUser.EWAITING");
        filter.addAction("EnrollUser.ETTIMEOUT");
        filter.addAction("EnrollUser.ECONTINUE");
        filter.addAction("EnrollUser.EBADQUALITY");
        filter.addAction("EnrollUser.EADMINFULL");
        filter.addAction("EnrollUser.EFPFULL");
        filter.addAction("EnrollUser.EFPEXIST");
        filter.addAction("SaveFingerprintSuccess");

        filter.addAction("EnrollUser.ERFICEXIST");
        filter.addAction("EnrollUser.ERFICFULL");
        filter.addAction("SaveRFICSuccess");

        filter.addAction("EnrollUser.EPWDFULL");
        filter.addAction("EnrollUser.EPWDEXIST");
        filter.addAction("SavePasswordSuccess");
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
            switch (action){
                case "BleDeviceConnectSuccess":
                    LockManageKeyActivity.this.bleDevice = intent.getParcelableExtra("BleDevice");
                    break;
                case "YoyonKeysChange":
                    yoyonKeys.clear();
                    yoyonKeys.addAll(YoyonKeyDao.getInstance(LockManageKeyActivity.this).getAllKeyOfLock(yoyonLock.getMac()));
                    mAdapter.notifyDataSetChanged();
                    if(yoyonKeys.size() == 0){
                        noKeyTips.setVisibility(View.VISIBLE);
                    }else{
                        noKeyTips.setVisibility(View.INVISIBLE);
                    }
                    break;
                case "EnrollUser.EOK": {
                    int id = intent.getExtras().getInt("KeyId");
                    int type = intent.getExtras().getInt("KeyType");
                    int property = intent.getExtras().getInt("KeyProperty");
                    switch (type){
                        case YnBleKeyType.FINGERPRINT: {
                            i = 4;
                            PopupWindowFactory.PopupWindowHandler handler = PopupWindowFactory.getInstance().new PopupWindowHandler();
                            Message message = new Message();
                            message.what = 1;
                            Bundle bundle1 = new Bundle();
                            bundle1.putFloat("From", 0.2f * i);
                            bundle1.putFloat("To", 0.2f * (i + 1));
                            message.setData(bundle1);
                            handler.sendMessage(message);

                            PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.add_fingerprint_success));
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                        }
                            break;
                        case YnBleKeyType.RFIC:
                        case YnBleKeyType.PASSWORD:
                            PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.add_fingerprint_success));
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,2);
                            break;
                        default:
                            break;
                    }
                    YoyonKey yoyonKey = new YoyonKey();
                    yoyonKey.setAttachLockMac(yoyonLock.getMac());
                    yoyonKey.setName(keyName);
                    yoyonKey.setIndexNumber(id);
                    yoyonKey.setType(type);
                    yoyonKey.setRole(property);
                    YoyonKeyDao.getInstance(LockManageKeyActivity.this).insertKey(yoyonKey);
                    yoyonKeys.clear();
                    yoyonKeys.addAll(YoyonKeyDao.getInstance(LockManageKeyActivity.this).getAllKeyOfLock(yoyonLock.getMac()));
                    mAdapter.notifyDataSetChanged();
                    if(yoyonKeys.size() == 0){
                        noKeyTips.setVisibility(View.VISIBLE);
                    }else{
                        noKeyTips.setVisibility(View.INVISIBLE);
                    }
                }
                break;
                case "EnrollUser.EWAITING": {
                    int type = intent.getExtras().getInt("KeyType");
                    switch (type) {
                        case YnBleKeyType.FINGERPRINT:
                            i = 0;
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this, 2);
                            SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER);
                            break;
                        case YnBleKeyType.RFIC:
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this, 1);
                            SoundPlayUtils.play(SoundPlayUtils.SWIPE_CARD);
                            break;
                        default:
                            break;
                    }
                }
                break;
                case "EnrollUser.ETTIMEOUT":{
                    PopupWindowFactory.getInstance().dismissPopupWindow();
                    Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.enroll_overtime),Toast.LENGTH_SHORT).show();
                }
                break;
                case "EnrollUser.ECONTINUE": {
                    PopupWindowFactory.PopupWindowHandler handler = PopupWindowFactory.getInstance().new PopupWindowHandler();
                    Message message = new Message();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("From", 0.2f * i);
                    bundle.putFloat("To", 0.2f * (i + 1));
                    i = i + 1;
                    message.setData(bundle);
                    handler.sendMessage(message);
                    SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER_AGAIN);
                }
                break;
                case "EnrollUser.EADMINFULL":{
                    int type = intent.getExtras().getInt("KeyType");
                    switch (type) {
                        case YnBleKeyType.FINGERPRINT:
                            PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.add_admin_full));
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,4);
                            break;
                        case YnBleKeyType.RFIC:
                            PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.add_admin_full));
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                            break;
                        case YnBleKeyType.PASSWORD:
                            PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.add_admin_full));
                            PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                            break;
                        default:
                            break;
                    }
                }
                break;
                case "EnrollUser.EFPEXIST":
                    Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.add_fingerprint_exist),Toast.LENGTH_SHORT).show();
                    YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.FINGERPRINT, keyIsManager ? YnBleKeyPropertyType.ADMIN : YnBleKeyPropertyType.NOT_ADMIN,null,true);
                    PopupWindowFactory.PopupWindowHandler handler = PopupWindowFactory.getInstance().new PopupWindowHandler();
                    i = 0;
                    Message message = new Message();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putFloat("From", i);
                    bundle.putFloat("To", i);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    SoundPlayUtils.play(SoundPlayUtils.EXIST);
                    break;
                case "EnrollUser.EFPFULL":
                    PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.add_fingerprint_full));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,4);
                    SoundPlayUtils.play(SoundPlayUtils.FINGER_FULL);
                    break;

                case "EnrollUser.EBADQUALITY":
                    Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.add_fingerprint_bad),Toast.LENGTH_SHORT).show();
                    SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER_AGAIN);
                    break;
                case "SaveFingerprintSuccess":
                    PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.add_fingerprint_success));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                    break;
                case "EnrollUser.ERFICEXIST":
                    Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.enroll_proximity_card_exist),Toast.LENGTH_SHORT).show();
                    YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.RFIC, keyIsManager ? YnBleKeyPropertyType.ADMIN : YnBleKeyPropertyType.NOT_ADMIN,null,true);
//                    PopupWindowFactory.getInstance().setFailText("感应卡已存在");
//                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(BluetoothDeviceKeyActivity.this,3);
                    SoundPlayUtils.play(SoundPlayUtils.EXIST);
                    break;
                case "EnrollUser.ERFICFULL":
                    PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.enroll_proximity_card_full));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                    SoundPlayUtils.play(SoundPlayUtils.PROXIMITY_CARD_FULL);
                    break;
                case "SaveRFICSuccess":
                    PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.enroll_proximity_card_success));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,2);
                    break;
                case "EnrollUser.EPWDEXIST":
                    btn_enrollPassword.setEnabled(true);
                    btn_enrollPassword.setAlpha(1f);
                    Toast.makeText(LockManageKeyActivity.this,getResources().getString(R.string.add_password_exist),Toast.LENGTH_SHORT).show();
                    break;
                case "EnrollUser.EPWDFULL":
                    PopupWindowFactory.getInstance().setFailText(getResources().getString(R.string.add_password_full));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,3);
                    break;
                case "SavePasswordSuccess":
                    PopupWindowFactory.getInstance().setSuccessText(getResources().getString(R.string.add_password_success));
                    PopupWindowFactory.getInstance().setViewPagerToTargetItem(LockManageKeyActivity.this,2);
                    break;
                default:
                    break;
            }
        }
    };

    private String keyName = "";
    private boolean keyIsManager = false;
    private String password = "";
    private TextWatcher keyNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            keyName = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
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
    private CompoundButton.OnCheckedChangeListener keyIsManagerListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            keyIsManager = b;
        }
    };

    private PopupWindow.OnDismissListener onPopupWindowDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            keyName = "";
            keyIsManager = false;
            YoyonUtils.darkenBackground(1f, LockManageKeyActivity.this);
            YnBleManager.getInstance().bleCmdCancle(false);
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        yoyonKeys.clear();
        yoyonKeys.addAll(YoyonKeyDao.getInstance(LockManageKeyActivity.this).getAllKeyOfLock(yoyonLock.getMac()));
        mAdapter.notifyDataSetChanged();
        if(yoyonKeys.size() == 0){
            noKeyTips.setVisibility(View.VISIBLE);
        }else{
            noKeyTips.setVisibility(View.INVISIBLE);
        }
    }
}
