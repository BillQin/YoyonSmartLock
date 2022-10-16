package yoyon.smartlock.standalone.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.AppOpsManagerCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.yoyon.ynblelib.clj.data.BleDevice;
import com.yoyon.ynblelib.clj.exception.BleException;
import com.yoyon.ynblelib.yoyon.YnBleCapability;
import com.yoyon.ynblelib.yoyon.YnBleCommand;
import com.yoyon.ynblelib.yoyon.YnBleKeyPropertyType;
import com.yoyon.ynblelib.yoyon.YnBleKeyType;
import com.yoyon.ynblelib.yoyon.YnBleManager;
import com.yoyon.ynblelib.yoyon.callback.YnBleConnectCallback;
import com.yoyon.ynblelib.yoyon.callback.YnBleScanCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleAdminPairCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleEnrollUserCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGenRandTblCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetCapabilityCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetUserStatisticsCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleGetVersionCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleIndicateCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleRequestAuthCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSharePairCallback;
import com.yoyon.ynblelib.yoyon.callback.command.YnBleSyncTimeBatteryCallback;
import com.yoyon.ynblelib.yoyon.model.YnBleTempPswRand;

import java.lang.reflect.Method;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.ScannedLockListViewAdapter;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.db.YoyonLockDao;
import yoyon.smartlock.standalone.db.YoyonOperationLogDao;
import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.model.YoyonOperationLog;
import yoyon.smartlock.standalone.utils.DialogManager;
import yoyon.smartlock.standalone.utils.PopupWindowFactory;
import yoyon.smartlock.standalone.utils.SoundPlayUtils;
import yoyon.smartlock.standalone.utils.YoyonUtils;


/**
 * Created by QB on 2018/3/29.
 */

public class PairingLockActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ScannedLockListViewAdapter mAdapter;
    private int adminID;
    private BleDevice bleDevice;
    private YnBleCapability ynBleCapability;
    private byte pairType;
    private String userKey;
    private int userId;
    private boolean isUserEmpty;
    private String uuid;
    private String sdsShareKey;
    private String shareCmd;
    private String shareMac;

    private String randSeed;
    private List<YnBleTempPswRand> ynBleTempPswRands;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_lock);
        initComponent();
        YnBleManager.getInstance().init(getApplication(), "YOYON", null);
        YnBleManager.getInstance().enableLog(true);
        setYnBleCommandCallback();
        if (checkBle()) {
            scan();
        }
        int checkResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkResult == -1) {
            DialogManager.getInstance().showMessageDialog(this, "搜索蓝牙设备需获取定位定位权限", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent localIntent = new Intent();
                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    localIntent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(localIntent);
                    finish();
                }
            });
        }
    }
    private int checkOp(Context context, int op, String opString) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
//            Object object = context.getSystemService("appops");
            Class c = object.getClass();
            try {
                Class[] cArg = new Class[3];
                cArg[0] = int.class;
                cArg[1] = int.class;
                cArg[2] = String.class;
                Method lMethod = c.getDeclaredMethod("checkOp", cArg);
                return (Integer) lMethod.invoke(object, op, Binder.getCallingUid(), context.getPackageName());
            } catch (Exception e) {
                e.printStackTrace();
                if (Build.VERSION.SDK_INT >= 23) {
                    return AppOpsManagerCompat.noteOp(context, opString, context.getApplicationInfo().uid,
                            context.getPackageName());
                }
            }
        }
        return -1;
    }

    private void initComponent() {
        LinearLayout returnLayout = findViewById(R.id.pairingLockActivity_returnLayout);
        LinearLayout scanLayout = findViewById(R.id.pairingLockActivity_scanLayout);
        returnLayout.setOnClickListener(this);
        scanLayout.setOnClickListener(this);

        TextView textView_scan = findViewById(R.id.pairingLockActivity_scanTextView);
        textView_scan.setOnClickListener(this);

        mAdapter = new ScannedLockListViewAdapter(this);
        ListView listView_scannedBluetoothDevice = findViewById(R.id.pairingLockActivity_scannedLockListView);
        listView_scannedBluetoothDevice.setAdapter(mAdapter);
        listView_scannedBluetoothDevice.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pairingLockActivity_returnLayout:
                finish();
                break;
            case R.id.pairingLockActivity_scanLayout:
                if (checkBle()) {
                    IntentIntegrator intentIntegrator = new IntentIntegrator(PairingLockActivity.this);
                    intentIntegrator.createScanIntent();
                    new IntentIntegrator(PairingLockActivity.this)
                            .setOrientationLocked(false)
                            .setCaptureActivity(ScanActivity.class) // 设置自定义的activity是CustomActivity
                            .initiateScan(); // 初始化扫描
                }
                break;
            case R.id.pairingLockActivity_scanTextView:
                if (checkBle()) {
                    mAdapter.clearDevice();
                    mAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }

    private void scan() {
        YnBleManager.getInstance().scan(-1, new YnBleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                super.onScanStarted(success);
            }

            @Override
            public void onScanning(BleDevice result) {
                super.onScanning(result);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                super.onScanFinished(scanResultList);
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
                mAdapter.addDevice(bleDevice);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private boolean checkBle() {
        if (YnBleManager.getInstance().isSupportBle()) {
            if (YnBleManager.getInstance().isBlueEnable()) {
                return true;
            } else {
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enabler);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DialogManager.getInstance().dismissLoadingDialog();
        try {
            YnBleManager.getInstance().cancelScan();
            YnBleManager.getInstance().destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            scan();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            mAdapter.clearDevice();
                            mAdapter.notifyDataSetChanged();
                            YnBleManager.getInstance().cancelScan();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            break;
                        default:
                            break;
                    }
                    break;
            }
        }
    };

    private void connect(BleDevice bleDevice) {
        YnBleManager.getInstance().connect(bleDevice, new YnBleConnectCallback() {
            @Override
            public void onStartConnect() {
                super.onStartConnect();
                DialogManager.getInstance().dismissLoadingDialog();
                DialogManager.getInstance().showLoadingDialog(PairingLockActivity.this, getResources().getString(R.string.connecting_tips), true, false, new DialogManager.OnBackKeyDown() {
                    @Override
                    public void onClick() {
                        YnBleManager.getInstance().disconnectAllDevice();
                    }
                });
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                super.onConnectFail(bleDevice, exception);
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                super.onConnectSuccess(bleDevice, gatt, status);
                PairingLockActivity.this.bleDevice = bleDevice;
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                super.onDisConnected(isActiveDisConnected, device, gatt, status);
                DialogManager.getInstance().dismissLoadingDialog();
                Toast.makeText(PairingLockActivity.this, "断开连接", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setYnBleCommandCallback() {
        YnBleManager.getInstance().setIndicateCallback(new YnBleIndicateCallback() {
            @Override
            public void onCharacteristicChangedSuccess() {
                if (pairType == YnBleCommand.CMD_ADMIN_PAIR) {
                    YnBleManager.getInstance().bleCmdAdminPair();
                } else if (pairType == YnBleCommand.CMD_SHARE_PAIR) {
                    YnBleManager.getInstance().bleCmdSharePair(shareCmd);
                }
            }

            @Override
            public void onCharacteristicChangedFail() {

            }
        });
        YnBleManager.getInstance().setAdminPairCallback(new YnBleAdminPairCallback() {
            @Override
            public void onSuccess(String adminKey, int userId) {
                userKey = adminKey;
                PairingLockActivity.this.userId = userId;
                YnBleManager.getInstance().bleCmdReqAuth();
                DialogManager.getInstance().dismissLoadingDialog();
                DialogManager.getInstance().showLoadingDialog(PairingLockActivity.this, getResources().getString(R.string.connecting_tips), true, true);
            }

            @Override
            public void onWaiting() {
                DialogManager.getInstance().dismissLoadingDialog();
                DialogManager.getInstance().showLoadingDialog(PairingLockActivity.this, getResources().getString(R.string.waiting_auth_tips), true, false, new DialogManager.OnBackKeyDown() {
                    @Override
                    public void onClick() {
                        YnBleManager.getInstance().disconnectAllDevice();
                    }
                });
                SoundPlayUtils.play(SoundPlayUtils.AUTHORIZE);
            }

            @Override
            public void onIdentifyFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.authentication_failed_retry), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onIllegal() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.illegal_operation), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onTimeout() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.enroll_overtime), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.pair_fail), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });
        YnBleManager.getInstance().setSharePairCallback(new YnBleSharePairCallback() {
            @Override
            public void onSuccess(String shareKey, int userId) {
                userKey = shareKey;
                PairingLockActivity.this.userId = userId;
                YnBleManager.getInstance().bleCmdReqAuth();
            }

            @Override
            public void onIllegal() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onTimeout() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.over_share_time), Toast.LENGTH_SHORT).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onBleFull() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.share_user_full), Toast.LENGTH_SHORT).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.pair_fail), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });
        YnBleManager.getInstance().setRequestAuthCallback(new YnBleRequestAuthCallback() {
            @Override
            public void onSuccess() {
                if (pairType == YnBleCommand.CMD_ADMIN_PAIR) {
                    YnBleManager.getInstance().bleCmdGetUserStatistics();
                } else if (pairType == YnBleCommand.CMD_SHARE_PAIR) {
                    YnBleManager.getInstance().bleCmdGetCapability();
                }
            }

            @Override
            public void onFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.pair_fail), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });
        YnBleManager.getInstance().setGetUserStatisticsCallback(new YnBleGetUserStatisticsCallback() {
            @Override
            public void onSuccess(int totalUser, int unSyncUser) {
                if (totalUser > 1) {
                    isUserEmpty = false;
                } else {
                    isUserEmpty = true;
                }
                YnBleManager.getInstance().bleCmdGetCapability();
            }

            @Override
            public void onFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.pair_fail), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });

        YnBleManager.getInstance().setGetCapabilityCallback(new YnBleGetCapabilityCallback() {
            @Override
            public void onSuccess(YnBleCapability ynBleCapability) {
                if(pairType == YnBleCommand.CMD_ADMIN_PAIR){
                    PairingLockActivity.this.ynBleCapability = ynBleCapability;
                    if (ynBleCapability.isTempPasswordCapability()) {
                        YnBleManager.getInstance().bleCmdGenRandTbl(false);
                    } else {
                        if (isUserEmpty) {
                            DialogManager.getInstance().dismissLoadingDialog();
                            showAddKeyPopupWindow();
                        } else {
                            DialogManager.getInstance().dismissLoadingDialog();
                            addAdminLock();
                        }
                    }
                }else if(pairType == YnBleCommand.CMD_SHARE_PAIR){
                    YoyonLock yoyonLock;
                    if(YoyonLockDao.getInstance(PairingLockActivity.this).isTheLockExist(shareMac)){
                        yoyonLock = YoyonLockDao.getInstance(PairingLockActivity.this).getTheLock(shareMac);
                        yoyonLock.setUserKey(userKey);
                        YoyonLockDao.getInstance(PairingLockActivity.this).updateLock(yoyonLock);
                    }else{
                        yoyonLock = new YoyonLock();
                        yoyonLock.setMac(shareMac);
                        yoyonLock.setRole(YoyonLock.SHARE);
                        yoyonLock.setName(getResources().getString(R.string.bluetooth_smart_lock));
                        yoyonLock.setCapability(ynBleCapability);
                        yoyonLock.setUserKey(userKey);
                        YoyonLockDao.getInstance(PairingLockActivity.this).insertLock(yoyonLock);
                    }
                    startActivity(new Intent(PairingLockActivity.this,MainActivity.class));
                }

            }

            @Override
            public void onFail() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.pair_fail), Toast.LENGTH_LONG).show();
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });

        YnBleManager.getInstance().setGenRandTblCallback(new YnBleGenRandTblCallback() {
            @Override
            public void onSuccess(String randSeed, List<YnBleTempPswRand> ynBleTempPswRands) {
                PairingLockActivity.this.randSeed = randSeed;
                PairingLockActivity.this.ynBleTempPswRands = ynBleTempPswRands;
                if (isUserEmpty) {
                    DialogManager.getInstance().dismissLoadingDialog();
                    showAddKeyPopupWindow();
                } else {
                    DialogManager.getInstance().dismissLoadingDialog();
                    addAdminLock();
                }
            }

            @Override
            public void onFail() {
                if (isUserEmpty) {
                    DialogManager.getInstance().dismissLoadingDialog();
                    showAddKeyPopupWindow();
                } else {
                    DialogManager.getInstance().dismissLoadingDialog();
                    addAdminLock();
                }
            }
        });

        YnBleManager.getInstance().setEnrollUserCallback(new YnBleEnrollUserCallback() {
            @Override
            public void onSuccess(int keyId, int keyType, int keyProperty) {
                isAddAdminSuccess = true;
                switch (keyType) {
                    case YnBleKeyType.FINGERPRINT:
                        fingerprintSuccessTimes = 5;
                        PopupWindowFactory.PopupWindowHandler handler = PopupWindowFactory.getInstance().new PopupWindowHandler();
                        Message message = new Message();
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putFloat("From", 0.2f * fingerprintSuccessTimes);
                        bundle.putFloat("To", 0.2f * (fingerprintSuccessTimes + 1));
                        message.setData(bundle);
                        handler.sendMessage(message);
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(PairingLockActivity.this, 2);
                        break;
                    case YnBleKeyType.RFIC:
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(PairingLockActivity.this, 1);
                        break;
                    case YnBleKeyType.PASSWORD:
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(PairingLockActivity.this, 1);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onWaiting(int keyType) {
                switch (keyType) {
                    case YnBleKeyType.FINGERPRINT:
                        fingerprintSuccessTimes = 0;
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(PairingLockActivity.this, 1);
                        SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER);
                        break;
                    case YnBleKeyType.RFIC:
                        PopupWindowFactory.getInstance().setViewPagerToTargetItem(PairingLockActivity.this, 0);
                        SoundPlayUtils.play(SoundPlayUtils.SWIPE_CARD);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onContinue() {
                PopupWindowFactory.PopupWindowHandler handler = PopupWindowFactory.getInstance().new PopupWindowHandler();
                Message message = new Message();
                message.what = 1;
                Bundle bundle = new Bundle();
                bundle.putFloat("From", 0.2f * fingerprintSuccessTimes);
                bundle.putFloat("To", 0.2f * (fingerprintSuccessTimes + 1));
                fingerprintSuccessTimes = fingerprintSuccessTimes + 1;
                message.setData(bundle);
                handler.sendMessage(message);
                SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER_AGAIN);
            }

            @Override
            public void onBadQuality() {
                Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.add_fingerprint_bad), Toast.LENGTH_SHORT).show();
                SoundPlayUtils.play(SoundPlayUtils.PRESS_FINGER_AGAIN);
            }

            @Override
            public void onAdminFull(int keyType) {

            }

            @Override
            public void onFingerprintExist() {

            }

            @Override
            public void onRFICExist() {

            }

            @Override
            public void onPasswordExist() {

            }

            @Override
            public void onFingerprintFull() {

            }

            @Override
            public void onRFICEFull() {

            }

            @Override
            public void onPasswordFull() {

            }

            @Override
            public void onTimeOut() {
                PopupWindowFactory.getInstance().dismissPopupWindow();
                PopupWindowFactory.getInstance().showFailPopupWindow(PairingLockActivity.this, getResources().getString(R.string.pair_timeout));
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }

            @Override
            public void onFail() {
                PopupWindowFactory.getInstance().dismissPopupWindow();
                PopupWindowFactory.getInstance().showFailPopupWindow(PairingLockActivity.this, getResources().getString(R.string.pair_fail));
                DialogManager.getInstance().dismissLoadingDialog();
                YnBleManager.getInstance().disconnectAllDevice();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BleDevice itemBleDevice = (BleDevice) parent.getItemAtPosition(position);
        pairType = YnBleCommand.CMD_ADMIN_PAIR;
        connect(itemBleDevice);
    }

    private int fingerprintSuccessTimes;
    private boolean isActiveClose;
    private boolean isAddAdminSuccess;
    private PopupWindow.OnDismissListener onPopupWindowDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            YoyonUtils.darkenBackground(1f, PairingLockActivity.this);
            if (isAddAdminSuccess) {
                addAdminLock();
            } else {
                YnBleManager.getInstance().disconnectAllDevice();
            }
        }
    };
    private void addAdminLock(){
        if(YoyonLockDao.getInstance(getApplicationContext()).isTheLockExist(bleDevice.getMac())){
            YoyonLock yoyonLock = YoyonLockDao.getInstance(getApplicationContext()).getTheLock(bleDevice.getMac());
            yoyonLock.setUserKey(userKey);
            yoyonLock.setCapability(ynBleCapability);
            yoyonLock.setRole(YoyonLock.ADMIN);
            if(ynBleCapability.isTempPasswordCapability()){
                yoyonLock.setTempPasswordSeed(randSeed);
                StringBuilder sb = new StringBuilder();
                for (YnBleTempPswRand ynBleTempPswRand : ynBleTempPswRands) {
                    sb.append(ynBleTempPswRand.getRand()).append("+");
                }
                yoyonLock.setTempPasswordRandom(sb.toString());
            }
            YoyonLockDao.getInstance(getApplicationContext()).updateLock(yoyonLock);
        }else{
            YoyonLock yoyonLock = new YoyonLock();
            yoyonLock.setMac(bleDevice.getMac());
            yoyonLock.setName(getResources().getString(R.string.bluetooth_smart_lock));
            yoyonLock.setUserKey(userKey);
            yoyonLock.setCapability(ynBleCapability);
            yoyonLock.setRole(YoyonLock.ADMIN);
            if(ynBleCapability.isTempPasswordCapability()){
                yoyonLock.setTempPasswordSeed(randSeed);
                StringBuilder sb = new StringBuilder();
                for (YnBleTempPswRand ynBleTempPswRand : ynBleTempPswRands) {
                    sb.append(ynBleTempPswRand.getRand()).append("+");
                }
                yoyonLock.setTempPasswordRandom(sb.toString());
            }
            YoyonLockDao.getInstance(getApplicationContext()).insertLock(yoyonLock);
        }
        startActivity(new Intent(this,MainActivity.class));
    }
    private void showAddKeyPopupWindow() {
        isAddAdminSuccess = false;
        isActiveClose = true;
        PopupWindowFactory.getInstance().showBluetoothDeviceAddKeyPopupwindow(PairingLockActivity.this, getResources().getString(R.string.choose_ways_to_add_admin), ynBleCapability, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActiveClose = false;
                PopupWindowFactory.getInstance().dismissPopupWindow();
                if (YnBleManager.getInstance().isConnected(bleDevice)) {
                    PopupWindowFactory.getInstance().showBluetoothDeviceAddFingerprintPopupwindow(PairingLockActivity.this, true, null, null, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.FINGERPRINT, YnBleKeyPropertyType.ADMIN, null, false);
                        }
                    }, onPopupWindowDismissListener);
                } else {
                    Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActiveClose = false;
                PopupWindowFactory.getInstance().dismissPopupWindow();
                if (YnBleManager.getInstance().isConnected(bleDevice)) {
                    PopupWindowFactory.getInstance().showBluetoothDeviceAddPasswordPopupwindow(PairingLockActivity.this, true, null, null, passwordTextWatcher, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.PASSWORD, YnBleKeyPropertyType.ADMIN, password, false);
                            view.setEnabled(false);
                        }
                    }, onPopupWindowDismissListener);
                } else {
                    Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isActiveClose = false;
                PopupWindowFactory.getInstance().dismissPopupWindow();
                if (YnBleManager.getInstance().isConnected(bleDevice)) {
                    YnBleManager.getInstance().bleCmdEnrollUser(YnBleKeyType.RFIC, YnBleKeyPropertyType.ADMIN, null, false);
                    PopupWindowFactory.getInstance().showBluetoothDeviceAddRFICPopupwindow(PairingLockActivity.this, true, null, null, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    }, onPopupWindowDismissListener);
                } else {
                    Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.device_disconnect_do_it_reconnect), Toast.LENGTH_SHORT).show();
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
                YoyonUtils.darkenBackground(1f, PairingLockActivity.this);
                if (isActiveClose) {
                    YnBleManager.getInstance().disconnectAllDevice();
                }
            }
        });
    }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == ScanActivity.SCAN_GALLERY_SUCCESS || resultCode == ScanActivity.SCAN_CAMERA_SUCCESS) {
                String scanResult = "";
                if (resultCode == ScanActivity.SCAN_GALLERY_SUCCESS) {
                    scanResult = data.getStringExtra("qrCode");
                } else if (resultCode == ScanActivity.SCAN_CAMERA_SUCCESS) {
                    IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    scanResult = intentResult.getContents();
                }
                try {
                    shareCmd = scanResult.split(",")[1];
                    shareMac = scanResult.split(",")[2];
                    if(YoyonLockDao.getInstance(PairingLockActivity.this).isTheLockExist(shareMac)){
                        if(YoyonLockDao.getInstance(PairingLockActivity.this).getTheLock(shareMac).getRole() == YoyonLock.ADMIN){
                            PopupWindowFactory.getInstance().showOneButtonWarningPopupwindow(PairingLockActivity.this, getResources().getString(R.string.tips), getResources().getString(R.string.adminUserSharePairing), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    PopupWindowFactory.getInstance().dismissPopupWindow();
                                }
                            });
                        }else{
                            pairType = YnBleCommand.CMD_SHARE_PAIR;
                            BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(shareMac);
                            BleDevice bleDevice = new BleDevice(bluetoothDevice);
                            connect(bleDevice);
                        }
                    }else{
                        pairType = YnBleCommand.CMD_SHARE_PAIR;
                        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(shareMac);
                        BleDevice bleDevice = new BleDevice(bluetoothDevice);
                        connect(bleDevice);
                    }
                } catch (Exception e) {
                    Toast.makeText(PairingLockActivity.this, getResources().getString(R.string.invalid_qr_code), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
}