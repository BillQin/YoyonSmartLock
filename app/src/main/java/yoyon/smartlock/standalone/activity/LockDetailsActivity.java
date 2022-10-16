package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.yoyon.ynblelib.clj.BleManager;
import com.yoyon.ynblelib.clj.data.BleDevice;
import com.yoyon.ynblelib.clj.data.BleScanState;
import com.yoyon.ynblelib.clj.exception.BleException;
import com.yoyon.ynblelib.clj.utils.HexUtil;
import com.yoyon.ynblelib.yoyon.YnBleHistoryType;
import com.yoyon.ynblelib.yoyon.YnBleKeyOperation;
import com.yoyon.ynblelib.yoyon.YnBleKeyType;
import com.yoyon.ynblelib.yoyon.YnBleManager;
import com.yoyon.ynblelib.yoyon.YnBleOptions;
import com.yoyon.ynblelib.yoyon.YnBleUtils;
import com.yoyon.ynblelib.yoyon.callback.YnBleConnectCallback;
import com.yoyon.ynblelib.yoyon.callback.YnBleScanAndConnectCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleClearUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleEnrollUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleForceSyncUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGenRandTblCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetOptionsCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetUserStatisticsCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetVersionCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleIndicateCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleModifyUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleRequestAuthCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSetOptionsCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSyncHistoryCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSyncTimeBatteryCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSyncUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleUnBindCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleUnLockCallback;
import com.yoyon.ynblelib.yoyon.model.YnBleTempPswRand;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.LockOperationLogListViewAdapter;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.db.YoyonLockDao;
import yoyon.smartlock.standalone.db.YoyonOperationLogDao;
import yoyon.smartlock.standalone.model.YoyonKey;
import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.model.YoyonOperationLog;
import yoyon.smartlock.standalone.utils.PopupWindowFactory;
import yoyon.smartlock.standalone.utils.YoyonHash;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by user on 17/10/19.
 */

public class LockDetailsActivity extends Activity implements View.OnClickListener{
    private YoyonLock yoyonLock;
    private int battery;

    private int totalUser;
    private BleDevice bleDevice;
    private ArrayList<Integer> forceSyncUserIDArray;
    private boolean isNotifyTempPasswordRemain = false;
    private boolean isForceScanBeforeConnect = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_details);
        YoyonUtils.setTransparentStatusBar(this);
        getIntentData();
        initComponent();
        updateLockInfo();
        YnBleManager.getInstance().init(getApplication(),"YOYON",yoyonLock.getUserKey());
        setYnBleCommandCallback();
        if(checkBle()){
            scanAndConnect();
        }
    }

    private void getIntentData(){
        yoyonLock = (YoyonLock) getIntent().getSerializableExtra("yoyonLock");
    }
    private void initComponent(){
        initActionBar();
        initCardView();
        initOperationLogListView();
    }

    private void initActionBar(){
        LinearLayout returnLayout = findViewById(R.id.lockDetailsActivity_return);
        LinearLayout settingLayout = findViewById(R.id.lockDetailsActivity_setting);
        returnLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
    }

    private TextView lock_name;
    private ImageView lock_onlineStatePoint;
    private TextView lock_onlineStateText;
    private TextView lock_powerText;
    private ProgressBar lock_powerProgressBar;
    private LinearLayout unlockLayout;
    private void initCardView(){
        LinearLayout keyManageLayout = findViewById(R.id.lockDetailsActivity_keyManage);
        LinearLayout tempPasswordLayout = findViewById(R.id.lockDetailsActivity_tempPassword);
        if(!yoyonLock.getCapability().isTempPasswordCapability()){
            tempPasswordLayout.setVisibility(View.GONE);
        }
        if(yoyonLock.getRole() == YoyonLock.SHARE){
            keyManageLayout.setAlpha(0.5f);
            tempPasswordLayout.setAlpha(0.5f);
        }
        keyManageLayout.setOnClickListener(this);
        tempPasswordLayout.setOnClickListener(this);

        ImageView lock_rename = findViewById(R.id.lockDetailsActivity_lockRename);
        unlockLayout = findViewById(R.id.lockDetailsActivity_unlock);
        lock_rename.setOnClickListener(this);
        unlockLayout.setOnClickListener(this);

        lock_name = findViewById(R.id.lockDetailsActivity_lockName);
        lock_onlineStatePoint = findViewById(R.id.lockDetailsActivity_lockOnlineStatePoint);
        lock_onlineStateText = findViewById(R.id.lockDetailsActivity_lockOnlineStateText);
        lock_powerText = findViewById(R.id.lockDetailsActivity_lockPowerText);
        lock_powerProgressBar = findViewById(R.id.lockDetailsActivity_lockPowerProgress);
    }

    private ListView operationLogListView;
    private LockOperationLogListViewAdapter lockOperationLogListViewAdapter;
    private List<YoyonOperationLog> yoyonOperationLogs;
    private RefreshLayout refreshLayout_operationLog;
    private LinearLayout noLogTipsLayout;
    private void initOperationLogListView(){
        operationLogListView = findViewById(R.id.lockDetailsActivity_log);
        refreshLayout_operationLog = findViewById(R.id.lockDetailsActivity_reFreshLayout);
        refreshLayout_operationLog.setRefreshHeader(new ClassicsHeader(this));//设置Header
        refreshLayout_operationLog.setRefreshFooter(new ClassicsFooter(this));//设置Footer
        refreshLayout_operationLog.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                pullToRefresh();
            }
        });
        refreshLayout_operationLog.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                pullToLoad();
            }
        });
        yoyonOperationLogs = YoyonOperationLogDao.getInstance(this).getAllOperationLogOfLock(yoyonLock.getMac());
        lockOperationLogListViewAdapter = new LockOperationLogListViewAdapter(this, yoyonOperationLogs);
        operationLogListView.setAdapter(lockOperationLogListViewAdapter);

        noLogTipsLayout = findViewById(R.id.lockDetailsActivity_noLogTips);
        if(yoyonOperationLogs.size() > 0){
            noLogTipsLayout.setVisibility(View.GONE);
        }else{
            noLogTipsLayout.setVisibility(View.VISIBLE);
        }
    }


    private void updateLockInfo(){
        //显示锁名称
        if(yoyonLock.getName() == null || yoyonLock.getName().isEmpty()){
            lock_name.setText(R.string.bluetooth_smart_lock);
        }else{
            lock_name.setText(yoyonLock.getName());
        }
        //显示电池电量
        lock_powerText.setText(getResources().getString(R.string.battery)+":"+battery+"%");
        lock_powerProgressBar.setProgress(battery);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lockDetailsActivity_return:
                finish();
                break;
            case R.id.lockDetailsActivity_setting: {
                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                    YnBleManager.getInstance().bleCmdGetOptions(true);
                }
                Intent intent = new Intent(this, LockSettingActivity.class);
                intent.putExtra("YoyonLock", yoyonLock);
                intent.putExtra("BleDevice", bleDevice);
                startActivity(intent);
            }
            break;
            case R.id.lockDetailsActivity_unlock:
                if(YnBleManager.getInstance().isAuthed(bleDevice)){
                    YnBleManager.getInstance().bleCmdUnLock(true);
                    unlockLayout.setEnabled(false);
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            unlockLayout.setEnabled(true);
                        }
                    }, 1000);
                }else{
                    Toast.makeText(this,getResources().getString(R.string.after_connect),Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.lockDetailsActivity_lockRename:
                showRenameDialog();
                break;
            case R.id.lockDetailsActivity_keyManage:
            {
                if(yoyonLock.getRole() == YoyonLock.ADMIN){
                    Intent intent = new Intent(this, LockManageKeyActivity.class);
                    intent.putExtra("YoyonLock", yoyonLock);
                    intent.putExtra("BleDevice", bleDevice);
                    startActivity(intent);
                }else{
                    //分享设备不支持该功能
                    Toast.makeText(this,getResources().getString(R.string.shareUserNonsupport),Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.lockDetailsActivity_tempPassword:
                if(yoyonLock.getRole() == YoyonLock.ADMIN){
                    if(YnBleManager.getInstance().isAuthed(bleDevice)){
                        getDeviceTempPasswordRemain(true);
                    }else{
                        getDeviceTempPasswordRandom();
                    }
                }else{
                    //分享设备不支持该功能
                    Toast.makeText(this,getResources().getString(R.string.shareUserNonsupport),Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

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
    private void showRenameDialog(){
        name = lock_name.getText().toString();
        PopupWindowFactory.getInstance().showBluetoothDeviceModifyLockNamePopupwindow(this, name, nameTextWatcher, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name == null || name.isEmpty()){
                    Toast.makeText(LockDetailsActivity.this,R.string.device_name_null_tips,Toast.LENGTH_SHORT).show();
                }else{
                    yoyonLock.setName(name);
                    YoyonLockDao.getInstance(LockDetailsActivity.this).updateLock(yoyonLock);
                    PopupWindowFactory.getInstance().dismissPopupWindow();
                    updateLockInfo();
                }
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(YnBleManager.getInstance().isAuthed(bleDevice)){
            if(yoyonLock.getCapability().isBatteryCheckCapability()){
                YnBleManager.getInstance().bleCmdSyncTimeBattery();
            }else{
                YnBleManager.getInstance().bleCmdGetUserStatistics();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BleManager.getInstance().getScanSate() == BleScanState.STATE_SCANNING ){
            try{
                YnBleManager.getInstance().cancelScan();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(YnBleManager.getInstance().isAuthed(bleDevice)){
            YnBleManager.getInstance().bleCmdCancle(true);
        }
        cancelAuthTimeoutTimer();
        YnBleManager.getInstance().disconnectAllDevice();
        YnBleManager.getInstance().destroy();
        unregisterReceiver(broadcastReceiver);
    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }


    protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch(blueState){
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            connect();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private void getDeviceTempPasswordRemain(final boolean forceWarning){
        String[] tempPasswordRandomArray = yoyonLock.getTempPasswordRandom().split("\\+");
        if(tempPasswordRandomArray.length <= 10 | forceWarning){
            String title;
            if(tempPasswordRandomArray.length == 0){
                title = getResources().getString(R.string.temp_password_connected_empty_tips);
            }else if(tempPasswordRandomArray.length <= 10){
                title = String.format(getResources().getString(R.string.temp_password_connected_remain_tips),tempPasswordRandomArray.length);
            }else{
                title = getResources().getString(R.string.temp_password_connected_tips1);
            }
            String body = getResources().getString(R.string.temp_password_connected_tips2);
            PopupWindowFactory.getInstance().dismissPopupWindow();
            PopupWindowFactory.getInstance().showWarningPopupWindow(LockDetailsActivity.this, title, body,getResources().getString(R.string.reset), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(YnBleManager.getInstance().isAuthed(bleDevice)){
                        YnBleManager.getInstance().bleCmdGenRandTbl(true);
                    }else {
                        Toast.makeText(LockDetailsActivity.this,getResources().getString(R.string.device_disconnect_do_it_reconnect),Toast.LENGTH_SHORT).show();
                    }
                    PopupWindowFactory.getInstance().dismissPopupWindow();
                }
            });
            isNotifyTempPasswordRemain = true;
        }
    }
    /**
     * 获取当前设备的TempPasswordRandom
     */
    private void getDeviceTempPasswordRandom(){
        String tempPasswordRandomSeed = yoyonLock.getTempPasswordSeed();
        String[] tempPasswordRandomArray = yoyonLock.getTempPasswordRandom().split("\\+");
        List<YnBleTempPswRand> ynBleTempPswRands = new ArrayList<>();
        for(int i =0; i<tempPasswordRandomArray.length; i++){
            YnBleTempPswRand ynBleTempPswRand = new YnBleTempPswRand();
            ynBleTempPswRand.setRand(tempPasswordRandomArray[i]);
            ynBleTempPswRands.add(ynBleTempPswRand);
        }
        if(ynBleTempPswRands.size() <= 0){
            String title = getResources().getString(R.string.temp_password_disconnect_empty_tips);
            String body = getResources().getString(R.string.temp_password_disconnect_reset_tips);
            PopupWindowFactory.getInstance().dismissPopupWindow();
            PopupWindowFactory.getInstance().showOneButtonWarningPopupwindow(LockDetailsActivity.this, title, body, new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }else{
            String rand = ynBleTempPswRands.get(ynBleTempPswRands.size()-1).getRand();
            ynBleTempPswRands.remove(ynBleTempPswRands.size()-1);
            byte[] result = YnBleUtils.genCkey(HexUtil.hexStringToBytes(tempPasswordRandomSeed), HexUtil.hexStringToBytes(rand));
            byte[] tempPasswordArray = new byte[13];
            for(int i = 0; i<13; i++){
                tempPasswordArray[i] =  YoyonHash.OTP_TA[result[i] & 0xff];
            }
            final String tempPassword = new String(tempPasswordArray);
            final int finalRemainRand = ynBleTempPswRands.size();
            PopupWindowFactory.getInstance().dismissPopupWindow();
            PopupWindowFactory.getInstance().showTempPasswordPopupWindow(LockDetailsActivity.this, tempPassword, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", tempPassword);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(LockDetailsActivity.this,getResources().getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                    PopupWindowFactory.getInstance().dismissPopupWindow();
                }
            }, new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    YoyonUtils.darkenBackground(1f,LockDetailsActivity.this);
                    if(finalRemainRand <= 10){
                        String title = String.format(getResources().getString(R.string.temp_password_connected_remain_tips),finalRemainRand);
                        String body = getResources().getString(R.string.temp_password_disconnect_reset_tips);
                        PopupWindowFactory.getInstance().dismissPopupWindow();
                        PopupWindowFactory.getInstance().showOneButtonWarningPopupwindow(LockDetailsActivity.this, title, body, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        });
                    }
                }
            });
            StringBuilder sb = new StringBuilder();
            for (YnBleTempPswRand ynBleTempPswRand:ynBleTempPswRands){
                sb.append(ynBleTempPswRand.getRand()).append("+");
            }
            yoyonLock.setTempPasswordRandom(sb.toString());
            YoyonLockDao.getInstance(LockDetailsActivity.this).updateLock(yoyonLock);
        }
    }

    /**
     * 下拉刷新
     */
    private void pullToRefresh(){
        refreshLayout_operationLog.finishRefresh(true);
    }
    /**
     * 上拉加载
     */
    private void pullToLoad(){
        refreshLayout_operationLog.finishLoadmore(true);
    }

    private boolean checkBle(){
        if(YnBleManager.getInstance().isSupportBle()){
            if(YnBleManager.getInstance().isBlueEnable()){
                return true;
            }else{
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enabler, 1);
                return false;
            }
        }else{
            return false;
        }
    }
    private Timer authTimeoutTimer;
    private TimerTask authTimeoutTask;
    private void initAuthTimeoutTimer(){
        cancelAuthTimeoutTimer();
        authTimeoutTimer = new Timer();
        authTimeoutTask = new TimerTask() {
            @Override
            public void run() {
                YnBleManager.getInstance().disconnectAllDevice();
            }
        };
        authTimeoutTimer.schedule(authTimeoutTask,5000);
    }
    private void cancelAuthTimeoutTimer(){
        if(authTimeoutTimer != null){
            authTimeoutTimer.cancel();
        }
    }
    private void scanAndConnect(){
        isForceScanBeforeConnect = false;
        YnBleManager.getInstance().scanAndConnect(yoyonLock.getMac(), 10000, new YnBleScanAndConnectCallback() {
            @Override
            public void onScanStarted(boolean success) {
                super.onScanStarted(success);
                if(!success && !isFinishing()){
                    connect();
                }
            }

            @Override
            public void onScanFinished(BleDevice scanResult) {
                super.onScanFinished(scanResult);
                if(!isFinishing()){
                    if(scanResult != null){
                        LockDetailsActivity.this.bleDevice = scanResult;
                    }else{
                        connect();
                    }
                }
            }

            @Override
            public void onStartConnect() {
                super.onStartConnect();
                if(!isFinishing()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lock_onlineStatePoint.setImageResource(R.drawable.offline_point);
                            lock_onlineStateText.setText(R.string.connecting);
                            lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOffline));
                        }
                    });
                    initAuthTimeoutTimer();
                }
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                super.onConnectFail(bleDevice, exception);
                if(!isFinishing()){
                    cancelAuthTimeoutTimer();
                    connect();
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                super.onConnectSuccess(bleDevice, gatt, status);
                if(!isFinishing()){
                    LockDetailsActivity.this.bleDevice = bleDevice;
                    Intent intent = new Intent();
                    intent.setAction("BleDeviceConnectSuccess");
                    intent.putExtra("BleDevice", bleDevice);
                    sendBroadcast(intent);
                    initAuthTimeoutTimer();
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                super.onDisConnected(isActiveDisConnected, device, gatt, status);
                if(!isFinishing()){
                    battery = 0;
                    updateLockInfo();

                    lock_onlineStatePoint.setImageResource(R.drawable.offline_point);
                    lock_onlineStateText.setText(R.string.disconnect);
                    lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOffline));

                    Intent intent = new Intent();
                    intent.setAction("BleDeviceDisconnect");
                    intent.putExtra("BleDevice", bleDevice);
                    sendBroadcast(intent);
                    if(isForceScanBeforeConnect){
                        scanAndConnect();
                    }else{
                        connect();
                    }
                    cancelAuthTimeoutTimer();
                }
            }
        });
    }
    private void connect(){
        if(YnBleManager.getInstance().isBlueEnable() & !isFinishing()) {
            YnBleManager.getInstance().connect(yoyonLock.getMac(), new YnBleConnectCallback() {
                @Override
                public void onStartConnect() {
                    super.onStartConnect();
                    if(!isFinishing()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lock_onlineStatePoint.setImageResource(R.drawable.offline_point);
                                lock_onlineStateText.setText(R.string.disconnect);
                                lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOffline));
                            }
                        });
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    if(!isFinishing()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lock_onlineStatePoint.setImageResource(R.drawable.offline_point);
                                lock_onlineStateText.setText(R.string.connecting);
                                lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOffline));
                                initAuthTimeoutTimer();
                            }
                        });
                    }
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    super.onConnectFail(bleDevice, exception);
                    if(!isFinishing()){
                        cancelAuthTimeoutTimer();
                        connect();
                    }
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    super.onConnectSuccess(bleDevice, gatt, status);
                    if(!isFinishing()){
                        LockDetailsActivity.this.bleDevice = bleDevice;
                        Intent intent = new Intent();
                        intent.setAction("BleDeviceConnectSuccess");
                        intent.putExtra("BleDevice", bleDevice);
                        sendBroadcast(intent);
                        initAuthTimeoutTimer();
                    }
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                    super.onDisConnected(isActiveDisConnected, device, gatt, status);
                    if(!isFinishing()){
                        battery = 0;
                        updateLockInfo();

                        lock_onlineStatePoint.setImageResource(R.drawable.offline_point);
                        lock_onlineStateText.setText(R.string.disconnect);
                        lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOffline));

                        Intent intent = new Intent();
                        intent.setAction("BleDeviceDisconnect");
                        intent.putExtra("BleDevice", bleDevice);
                        sendBroadcast(intent);
                        if(isForceScanBeforeConnect){
                            scanAndConnect();
                        }else{
                            connect();
                        }
                        cancelAuthTimeoutTimer();
                    }
                }
            });
        }
    }

    private void setYnBleCommandCallback(){
        YnBleManager.getInstance().setIndicateCallback(new YnBleIndicateCallback() {
            @Override
            public void onCharacteristicChangedSuccess() {
                YnBleManager.getInstance().bleCmdReqAuth();
                initAuthTimeoutTimer();
            }

            @Override
            public void onCharacteristicChangedFail() {
                cancelAuthTimeoutTimer();
            }
        });
        YnBleManager.getInstance().setRequestAuthCallback(new YnBleRequestAuthCallback() {
            @Override
            public void onSuccess() {
                cancelAuthTimeoutTimer();
                lock_onlineStatePoint.setImageResource(R.drawable.online_point);
                lock_onlineStateText.setText(R.string.connected);
                lock_onlineStateText.setTextColor(getResources().getColor(R.color.deviceOnline));
                updateLockInfo();
                if(yoyonLock.getCapability().isBatteryCheckCapability()){
                    YnBleManager.getInstance().bleCmdSyncTimeBattery();
                }else{
                    if(yoyonLock.getRole() == YoyonLock.ADMIN){
                        YnBleManager.getInstance().bleCmdGetUserStatistics();
                    }else if(yoyonLock.getRole() == YoyonLock.SHARE){
                        YnBleManager.getInstance().bleCmdGetOptions(true);
                    }
                }
            }

            @Override
            public void onFail() {
                new AlertDialog.Builder(LockDetailsActivity.this)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.tips))
                        .setMessage(getResources().getString(R.string.auth_fail))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).show();
            }
        });

        YnBleManager.getInstance().setSyncTimeBatteryCallback(new YnBleSyncTimeBatteryCallback() {
            @Override
            public void onSuccess(int battery) {
                LockDetailsActivity.this.battery = battery;
                updateLockInfo();
                if(yoyonLock.getRole() == YoyonLock.ADMIN){
                    YnBleManager.getInstance().bleCmdGetUserStatistics();
                }else if(yoyonLock.getRole() == YoyonLock.SHARE){
                    YnBleManager.getInstance().bleCmdGetOptions(true);
                }
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setGetUserStatisticsCallback(new YnBleGetUserStatisticsCallback() {
            @Override
            public void onSuccess(int totalUser, int unSyncUser) {
                if(unSyncUser == 0){
                    if(YoyonKeyDao.getInstance(LockDetailsActivity.this).getAllKeyOfLock(yoyonLock.getMac()).size() == totalUser){
                        YnBleManager.getInstance().bleCmdSyncHistory();
                    }else{
                        LockDetailsActivity.this.totalUser = totalUser;
                        forceSyncUserIDArray = new ArrayList<>();
                        YnBleManager.getInstance().bleCmdForceSyncUser();
                    }
                }else{
                    LockDetailsActivity.this.totalUser = totalUser;
                    YnBleManager.getInstance().bleCmdSyncUser();
                }
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setSyncUserCallback(new YnBleSyncUserCallback() {
            @Override
            public void onSuccess(int operation, int keyId, int keyProperty, int keyType) {
                if(operation == YnBleKeyOperation.ADD){
                    YoyonKey yoyonKey = new YoyonKey();
                    yoyonKey.setAttachLockMac(yoyonLock.getMac());
                    yoyonKey.setIndexNumber(keyId);
                    yoyonKey.setRole(keyProperty);
                    yoyonKey.setType(keyType);
                    YoyonKeyDao.getInstance(LockDetailsActivity.this).insertKey(yoyonKey);

                    Intent intent = new Intent();
                    intent.setAction("YoyonKeysChange");
                    sendBroadcast(intent);
                }else if(operation == YnBleKeyOperation.DELETE){
                    YoyonKeyDao.getInstance(LockDetailsActivity.this).deleteKey(yoyonLock.getMac(),keyId);

                    Intent intent = new Intent();
                    intent.setAction("YoyonKeysChange");
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onSyncFinish() {
                if(totalUser == YoyonKeyDao.getInstance(LockDetailsActivity.this).getAllKeyOfLock(yoyonLock.getMac()).size()){
                    YnBleManager.getInstance().bleCmdSyncHistory();
                }else{
                    forceSyncUserIDArray = new ArrayList<>();
                    YnBleManager.getInstance().bleCmdForceSyncUser();
                }
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setForceSyncUserCallback(new YnBleForceSyncUserCallback() {
            @Override
            public void onSuccess(int keyId, int keyProperty, int keyType) {
                List<Integer> yoyonKeysIndexArray = new ArrayList<>();
                for(YoyonKey yoyonKey:YoyonKeyDao.getInstance(LockDetailsActivity.this).getAllKeyOfLock(yoyonLock.getMac())){
                    yoyonKeysIndexArray.add(yoyonKey.getIndexNumber());
                }
                if(!yoyonKeysIndexArray.contains(keyId)){
                    YoyonKey yoyonKey = new YoyonKey();
                    yoyonKey.setAttachLockMac(yoyonLock.getMac());
                    yoyonKey.setIndexNumber(keyId);
                    yoyonKey.setType(keyType);
                    yoyonKey.setRole(keyProperty);
                    YoyonKeyDao.getInstance(LockDetailsActivity.this).insertKey(yoyonKey);
                }
                Intent intent = new Intent();
                intent.setAction("YoyonKeysChange");
                sendBroadcast(intent);
                forceSyncUserIDArray.add(keyId);
            }

            @Override
            public void onSyncFinish() {
                YnBleManager.getInstance().bleCmdSyncHistory();
                List<YoyonKey> yoyonKeys = YoyonKeyDao.getInstance(LockDetailsActivity.this).getAllKeyOfLock(yoyonLock.getMac());
                for(YoyonKey yoyonKey : yoyonKeys){
                    if (!forceSyncUserIDArray.contains(yoyonKey.getIndexNumber())) {
                        YoyonKeyDao.getInstance(LockDetailsActivity.this).deleteKey(yoyonLock.getMac(),yoyonKey.getIndexNumber());
                    }
                }
                Intent intent = new Intent();
                intent.setAction("YoyonKeysChange");
                sendBroadcast(intent);
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setSyncHistoryCallback(new YnBleSyncHistoryCallback() {
            @Override
            public void onSuccess(int historyType,int keyId, long timeStamp) {
                boolean isLogAbnormal = false;
                YoyonOperationLog yoyonOperationLog = new YoyonOperationLog();
                yoyonOperationLog.setAttachLockMac(yoyonLock.getMac());
                yoyonOperationLog.setAttachKeyIndex(keyId);
                yoyonOperationLog.setDate(YoyonUtils.stringToDate(String.valueOf(timeStamp)));
                switch (historyType){
                    //开锁
                    case YnBleHistoryType.FINGERPRINT_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FINGERPRINT);
                        break;
                    case YnBleHistoryType.PASSWORD_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.PASSWORD);
                        break;
                    case YnBleHistoryType.RFIC_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.RFIC);
                        break;
                    case YnBleHistoryType.KEY_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.KEY);
                        break;
                    case YnBleHistoryType.REMOTE_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.REMOTE);
                        break;
                    case YnBleHistoryType.BLUETOOTH_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.BLUETOOTH);
                        break;
                    case YnBleHistoryType.FACE_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FACE);
                        break;
                    case YnBleHistoryType.TEMP_PASSWORD_UNLOCK:
                        yoyonOperationLog.setType(2);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.TEMP_PASSWORD);
                        break;
                    //冻结
                    case YnBleHistoryType.FINGERPRINT_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FINGERPRINT);
                        break;
                    case YnBleHistoryType.PASSWORD_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.PASSWORD);
                        break;
                    case YnBleHistoryType.RFIC_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.RFIC);
                        break;
                    case YnBleHistoryType.KEY_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.KEY);
                        break;
                    case YnBleHistoryType.REMOTE_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.REMOTE);
                        break;
                    case YnBleHistoryType.BLUETOOTH_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.BLUETOOTH);
                        break;
                    case YnBleHistoryType.FACE_FREEZE:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FACE);
                        break;
                    case 53:
                        yoyonOperationLog.setType(13);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.UNDEFINE);
                        break;
                    //添加钥匙
                    case YnBleHistoryType.FINGERPRINT_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FINGERPRINT);
                        break;
                    case YnBleHistoryType.PASSWORD_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.PASSWORD);
                        break;
                    case YnBleHistoryType.RFIC_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.RFIC);
                        break;
                    case YnBleHistoryType.KEY_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.KEY);
                        break;
                    case YnBleHistoryType.REMOTE_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.REMOTE);
                        break;
                    case YnBleHistoryType.BLUETOOTH_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.BLUETOOTH);
                        break;
                    case YnBleHistoryType.FACE_ADD:
                        yoyonOperationLog.setType(8);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FACE);
                        break;
                    //删除钥匙
                    case YnBleHistoryType.FINGERPRINT_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FINGERPRINT);
                        break;
                    case YnBleHistoryType.PASSWORD_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.PASSWORD);
                        break;
                    case YnBleHistoryType.RFIC_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.RFIC);
                        break;
                    case YnBleHistoryType.KEY_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.KEY);
                        break;
                    case YnBleHistoryType.REMOTE_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.REMOTE);
                        break;
                    case YnBleHistoryType.BLUETOOTH_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.BLUETOOTH);
                        break;
                    case YnBleHistoryType.FACE_DELETE:
                        yoyonOperationLog.setType(15);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.FACE);
                        break;
                    //防撬警报
                    case YnBleHistoryType.ANTITHEFT_ALARM:
                        yoyonOperationLog.setType(14);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.UNDEFINE);
                        break;
                    case YnBleHistoryType.ELECTRON_SECURITY_ALARM:
                        yoyonOperationLog.setType(14);
                        yoyonOperationLog.setAttachKeyType(YnBleKeyType.KEY);
                        break;
                    default:
                        isLogAbnormal = true;
                        break;
                }

                if(!isLogAbnormal){
                    YoyonOperationLogDao.getInstance(LockDetailsActivity.this).insertOperationLog(yoyonOperationLog);
                    yoyonOperationLogs.clear();
                    yoyonOperationLogs.addAll(YoyonOperationLogDao.getInstance(LockDetailsActivity.this).getAllOperationLogOfLock(yoyonLock.getMac()));
                    lockOperationLogListViewAdapter.notifyDataSetChanged();
                    if(yoyonOperationLogs.size() > 0){
                        noLogTipsLayout.setVisibility(View.GONE);
                    }else{
                        noLogTipsLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSyncFinish() {
                YnBleManager.getInstance().bleCmdGetOptions(false);
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setEnrollUserCallback(new YnBleEnrollUserCallback() {
            @Override
            public void onSuccess(int keyId, int keyType, int keyProperty) {
                Intent intent = new Intent();
                intent.putExtra("KeyId",keyId);
                intent.putExtra("KeyType",keyType);
                intent.putExtra("KeyProperty",keyProperty);
                intent.setAction("EnrollUser.EOK");
                sendBroadcast(intent);
            }

            @Override
            public void onWaiting(int keyType) {
                Intent intent = new Intent();
                intent.putExtra("KeyType",keyType);
                intent.setAction("EnrollUser.EWAITING");
                sendBroadcast(intent);
            }

            @Override
            public void onTimeOut() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.ETTIMEOUT");
                sendBroadcast(intent);
            }

            @Override
            public void onContinue() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.ECONTINUE");
                sendBroadcast(intent);
            }

            @Override
            public void onBadQuality() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.EBADQUALITY");
                sendBroadcast(intent);
            }

            @Override
            public void onAdminFull(int keyType) {
                Intent intent = new Intent();
                intent.putExtra("KeyType",keyType);
                intent.setAction("EnrollUser.EADMINFULL");
                sendBroadcast(intent);
            }

            @Override
            public void onFingerprintExist() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.EFPEXIST");
                sendBroadcast(intent);
            }

            @Override
            public void onRFICExist() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.ERFICEXIST");
                sendBroadcast(intent);
            }

            @Override
            public void onPasswordExist() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.EPWDEXIST");
                sendBroadcast(intent);
            }

            @Override
            public void onFingerprintFull() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.EFPFULL");
                sendBroadcast(intent);
            }

            @Override
            public void onRFICEFull() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.ERFICFULL");
                sendBroadcast(intent);
            }

            @Override
            public void onPasswordFull() {
                Intent intent = new Intent();
                intent.setAction("EnrollUser.EPWDFULL");
                sendBroadcast(intent);
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setModifyUserCallback(new YnBleModifyUserCallback() {
            @Override
            public void onSuccess(int keyId, int keyType, int keyProperty) {
                Intent intent = new Intent();
                intent.setAction("ModifyUser.EOK");
                intent.putExtra("KeyId", keyId);
                intent.putExtra("KeyType", keyType);
                intent.putExtra("KeyProperty", keyProperty);
                sendBroadcast(intent);
            }

            @Override
            public void onAdminFull(int keyType) {
                Intent intent = new Intent();
                intent.setAction("ModifyUser.EADMINFULL");
                sendBroadcast(intent);
            }

            @Override
            public void onPasswordExist() {
                Intent intent = new Intent();
                intent.setAction("ModifyUser.EPWDEXIST");
                sendBroadcast(intent);
            }

            @Override
            public void onFail() {
                Intent intent = new Intent();
                intent.setAction("ModifyUser.EFAIL");
                sendBroadcast(intent);
            }
        });

        YnBleManager.getInstance().setClearUserCallback(new YnBleClearUserCallback() {
            @Override
            public void onSuccess(int keyId) {
                YoyonKeyDao.getInstance(LockDetailsActivity.this).deleteKey(yoyonLock.getMac(),keyId);
                Intent intent = new Intent();
                intent.setAction("DeleteBluetoothDeviceKeySuccess");
                sendBroadcast(intent);
            }

            @Override
            public void onDelOnlyAdmin() {
                Intent intent = new Intent();
                intent.setAction("DeleteBluetoothDeviceKeyOnlyAdmin");
                sendBroadcast(intent);
            }

            @Override
            public void onFail() {
                Intent intent = new Intent();
                intent.setAction("DeleteBluetoothDeviceKeyFail");
                sendBroadcast(intent);
            }
        });

        YnBleManager.getInstance().setUnBindCallback(new YnBleUnBindCallback() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent();
                intent.setAction("BluetoothDeviceUnbindSuccess");
                sendBroadcast(intent);
            }

            @Override
            public void onFail() {

            }
        });
        YnBleManager.getInstance().setUnLockCallback(new YnBleUnLockCallback() {
            @Override
            public void onSuccess() {
                isForceScanBeforeConnect = true;
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onFail() {

            }
        });

        YnBleManager.getInstance().setGetVersionCallback(new YnBleGetVersionCallback() {
            @Override
            public void onSuccess(String hardwareVersion, String softwareVersion) {

            }

            @Override
            public void onFail() {

            }
        });
        YnBleManager.getInstance().setGetOptionsCallback(new YnBleGetOptionsCallback() {
            @Override
            public void onSuccess(YnBleOptions ynBleOptions) {
                Intent intent = new Intent();
                intent.setAction("GetOptionsSuccess");
                intent.putExtra("YnBleOptions",ynBleOptions);
                sendBroadcast(intent);
                if(yoyonLock.getRole() == YoyonLock.ADMIN && !isNotifyTempPasswordRemain){
                    getDeviceTempPasswordRemain(false);
                }
            }

            @Override
            public void onFail() {
                if(yoyonLock.getRole() == YoyonLock.ADMIN && !isNotifyTempPasswordRemain){
                    getDeviceTempPasswordRemain(false);
                }
            }
        });

        YnBleManager.getInstance().setSetOptionsCallback(new YnBleSetOptionsCallback() {
            @Override
            public void onSuccess(YnBleOptions ynBleOptions) {
                Intent intent = new Intent();
                intent.setAction("SetOptionsSuccess");
                intent.putExtra("YnBleOptions",ynBleOptions);
                sendBroadcast(intent);
                YnBleManager.getInstance().bleCmdGetOptions(false);
            }

            @Override
            public void onFail() {
                Intent intent = new Intent();
                intent.setAction("SetOptionsFail");
                sendBroadcast(intent);
            }
        });
        YnBleManager.getInstance().setGenRandTblCallback(new YnBleGenRandTblCallback() {

            @Override
            public void onSuccess(String randSeed, List<YnBleTempPswRand> ynBleTempPswRands) {
                yoyonLock.setTempPasswordSeed(randSeed);
                StringBuilder sb = new StringBuilder();
                for (YnBleTempPswRand ynBleTempPswRand:ynBleTempPswRands){
                    sb.append(ynBleTempPswRand.getRand()).append("+");
                }
                yoyonLock.setTempPasswordRandom(sb.toString());
                YoyonLockDao.getInstance(LockDetailsActivity.this).updateLock(yoyonLock);
                Toast.makeText(LockDetailsActivity.this,getResources().getString(R.string.temp_password_reset_success),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail() {
                Toast.makeText(LockDetailsActivity.this,getResources().getString(R.string.temp_password_reset_fail),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
