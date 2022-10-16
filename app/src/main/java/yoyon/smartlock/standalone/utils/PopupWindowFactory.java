package yoyon.smartlock.standalone.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.yoyon.ynblelib.yoyon.YnBleCapability;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.PopupWindowViewPagerAdapter;

/**
 * Created by QinBin on 2018/6/29.
 */

public class PopupWindowFactory {
    public static PopupWindowFactory sInstance;
    private PopupWindow popupWindow;
    private NoManualSlideViewPager viewPager;
    private LottieAnimationView fingerprintAnimation;
    private TextView successText,failText;
    public static synchronized PopupWindowFactory getInstance() {
        if (sInstance == null) {
            sInstance = new PopupWindowFactory();
        }
        return sInstance;
    }

    public void showTempPasswordPopupWindow(final Activity activity, String tempPassword,View.OnClickListener copyListener,PopupWindow.OnDismissListener onDismissListener){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_temp_password,null);
        TextView tv_tempPassword = view.findViewById(R.id.popup_tempPassword_text);
        Button btn_copy = view.findViewById(R.id.popup_tempPassword_copy);
        Button btn_cancel = view.findViewById(R.id.popup_tempPassword_cancel);

        tv_tempPassword.setText(tempPassword);
        btn_copy.setOnClickListener(copyListener);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, parent.getWidth(), (parent.getHeight()/9)*5, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(onDismissListener);

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    public void showBluetoothDeviceAddFingerprintPopupwindow(final Activity activity,boolean isForceAdmin, TextWatcher nameTextWatcher, CompoundButton.OnCheckedChangeListener isManagerListener, View.OnClickListener tipsNextStepListener, PopupWindow.OnDismissListener onDismissListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
            }
        });
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(onDismissListener);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);
        List<View> views = new ArrayList<>();

        //输入钥匙信息界面
        if(!isForceAdmin){
            View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);

            final EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if(source.length()+dest.toString().length() > 10){
                        Toast.makeText(activity,R.string.key_name_oversize,Toast.LENGTH_SHORT).show();
                        return "";
                    }
                    return source;
                }
            };
            keyName.setFilters(filters);
            keyName.setHint(activity.getResources().getString(R.string.key_name_oversize));
            keyName.addTextChangedListener(nameTextWatcher);

            CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
            keyIsManager.setOnCheckedChangeListener(isManagerListener);

            Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
            keyInfoNextStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
                    setViewPagerToTargetItem(activity,1);
                }
            });
            views.add(keyInfo);
        }

        //录入指纹提示页面
        View enrollFingerprintTips = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_fingerprint_tips,null);
        Button tipsNextStep = enrollFingerprintTips.findViewById(R.id.popup_bluetoothdevice_add_fingerpring_nextStep);
        tipsNextStep.setOnClickListener(tipsNextStepListener);

        View enrollFingerprint = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_fingerprint_enroll,null);
        Button cancelEnroll = enrollFingerprint.findViewById(R.id.popup_bluetoothdevice_add_fingerpring_enroll_cancel);
        cancelEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });
        fingerprintAnimation = enrollFingerprint.findViewById(R.id.popup_bluetoothdevice_add_fingerpring_enroll_animotion);

        //成功页面
        View successView = LayoutInflater.from(activity).inflate(R.layout.popup_success,null);
        successText = successView.findViewById(R.id.popup_success_text);
        Button successDone = successView.findViewById(R.id.popup_success_done);
        successDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });
        //失败页面
        View failView = LayoutInflater.from(activity).inflate(R.layout.popup_fail,null);
        failText = failView.findViewById(R.id.popup_fail_text);
        Button failCancel = failView.findViewById(R.id.popup_fail_cancel);
        failCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });


        views.add(enrollFingerprintTips);
        views.add(enrollFingerprint);
        views.add(successView);
        views.add(failView);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    public void showBluetoothDeviceAddRFICPopupwindow(final Activity activity,boolean isForceAdmin, TextWatcher nameTextWatcher, CompoundButton.OnCheckedChangeListener isManagerListener, View.OnClickListener tipsNextStepListener, PopupWindow.OnDismissListener onDismissListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
            }
        });
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(onDismissListener);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();

        //输入钥匙信息界面
        if(!isForceAdmin){
            View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);

            final EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if(source.length()+dest.toString().length() > 10){
                        Toast.makeText(activity,R.string.key_name_oversize,Toast.LENGTH_SHORT).show();
                        return "";
                    }
                    return source;
                }
            };
            keyName.setFilters(filters);
            keyName.setHint(activity.getResources().getString(R.string.key_name_oversize));
            keyName.addTextChangedListener(nameTextWatcher);

            CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
            keyIsManager.setOnCheckedChangeListener(isManagerListener);

            Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
            keyInfoNextStep.setOnClickListener(tipsNextStepListener);
            views.add(keyInfo);
        }

        //录入感应卡提示页面
        View enrollRFIC = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_rfic_tips,null);
        Button cancelEnroll = enrollRFIC.findViewById(R.id.popup_bluetoothdevice_add_rfic_enroll_cancel);
        cancelEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });

        //成功页面
        View successView = LayoutInflater.from(activity).inflate(R.layout.popup_success,null);
        successText = successView.findViewById(R.id.popup_success_text);
        Button successDone = successView.findViewById(R.id.popup_success_done);
        successDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });
        //失败页面
        View failView = LayoutInflater.from(activity).inflate(R.layout.popup_fail,null);
        failText = failView.findViewById(R.id.popup_fail_text);
        Button failCancel = failView.findViewById(R.id.popup_fail_cancel);
        failCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });

        views.add(enrollRFIC);
        views.add(successView);
        views.add(failView);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    public void showBluetoothDeviceAddPasswordPopupwindow(final Activity activity,boolean isForceAdmin, TextWatcher nameTextWatcher, CompoundButton.OnCheckedChangeListener isManagerListener,TextWatcher passwordTextWatcher, View.OnClickListener passwordNextStepListener, PopupWindow.OnDismissListener onDismissListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
            }
        });
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(onDismissListener);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();
        //输入钥匙信息界面
        if(!isForceAdmin){
            View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);

            final EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if(source.length()+dest.toString().length() > 10){
                        Toast.makeText(activity,R.string.key_name_oversize,Toast.LENGTH_SHORT).show();
                        return "";
                    }
                    return source;
                }
            };
            keyName.setFilters(filters);
            keyName.setHint(activity.getResources().getString(R.string.key_name_oversize));
            keyName.addTextChangedListener(nameTextWatcher);

            CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
            keyIsManager.setOnCheckedChangeListener(isManagerListener);

            Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
            keyInfoNextStep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
                    setViewPagerToTargetItem(activity,1);
                }
            });
            views.add(keyInfo);
        }

        //输入密码页面
        View inputPasswordView = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_password,null);
        final EditText newPassword = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_new);
        final EditText confirmPassword = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_confirm);
        final TextView passwordTips = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_tips);
        final Button passwordNextStep = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_nextStep);
        CheckBox newPasswordVisible = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_new_visible);
        CheckBox confirmPasswordVisible = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_confirm_visible);
        newPasswordVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //从密码不可见模式变为密码可见模式
                    newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    //从密码可见模式变为密码不可见模式
                    newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                newPassword.setSelection(newPassword.getText().length());
            }
        });
        confirmPasswordVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //从密码不可见模式变为密码可见模式
                    confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    //从密码可见模式变为密码不可见模式
                    confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                confirmPassword.setSelection(confirmPassword.getText().length());
            }
        });
        passwordNextStep.setOnClickListener(passwordNextStepListener);
        passwordNextStep.setAlpha(0.2f);
        passwordNextStep.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(newPassword.getText().length() < 6 || confirmPassword.getText().length() < 6){
//                    passwordTips.setText("密码长度不能少于6位");
                    passwordTips.setText("");
                    passwordNextStep.setAlpha(0.2f);
                    passwordNextStep.setEnabled(false);
                }else{
                    if(!confirmPassword.getText().toString().equalsIgnoreCase(newPassword.getText().toString())){
                        passwordTips.setText(R.string.confirm_password_difference);
                        passwordNextStep.setAlpha(0.2f);
                        passwordNextStep.setEnabled(false);
                    }else{
                        passwordTips.setText("");
                        passwordNextStep.setAlpha(1f);
                        passwordNextStep.setEnabled(true);
                    }
                }
            }
        };
        newPassword.addTextChangedListener(textWatcher);
        confirmPassword.addTextChangedListener(textWatcher);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.length()+dest.toString().length() > 12){
                    Toast.makeText(activity,R.string.password_oversize,Toast.LENGTH_SHORT).show();
                    return "";
                }
                return source;
            }
        };
        newPassword.setFilters(filters);
        confirmPassword.setFilters(filters);
        confirmPassword.addTextChangedListener(passwordTextWatcher);


        //成功页面
        View successView = LayoutInflater.from(activity).inflate(R.layout.popup_success,null);
        successText = successView.findViewById(R.id.popup_success_text);
        Button successDone = successView.findViewById(R.id.popup_success_done);
        successDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });
        //失败页面
        View failView = LayoutInflater.from(activity).inflate(R.layout.popup_fail,null);
        failText = failView.findViewById(R.id.popup_fail_text);
        Button failCancel = failView.findViewById(R.id.popup_fail_cancel);
        failCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });

        views.add(inputPasswordView);
        views.add(successView);
        views.add(failView);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    public void showBluetoothDeviceAddKeyPopupwindow(final Activity activity, String title, YnBleCapability ynBleCapability, View.OnClickListener addFingerprintListener, View.OnClickListener addPasswordListener, View.OnClickListener addRFICListener, View.OnClickListener cancelListener, PopupWindow.OnDismissListener onDismissListener){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key,null);
        TextView addKeyTips = view.findViewById(R.id.popup_bluetoothdevice_add_key_tips);
        LinearLayout addFingerprint = view.findViewById(R.id.popup_bluetoothdevice_add_key_fingerprint);
        LinearLayout addPassword = view.findViewById(R.id.popup_bluetoothdevice_add_key_password);
        LinearLayout addRFIC = view.findViewById(R.id.popup_bluetoothdevice_add_key_iccard);
        Button cancelBtn = view.findViewById(R.id.popup_bluetoothdevice_add_key_cancel);

        if(title != null){
            addKeyTips.setText(title);
        }
        if(ynBleCapability != null){
            if(ynBleCapability.isFingerprintCapability()){
                addFingerprint.setOnClickListener(addFingerprintListener);
            }else{
                addFingerprint.setVisibility(View.GONE);
            }
            if(ynBleCapability.isPasswordCapability()){
                addPassword.setOnClickListener(addPasswordListener);
            }else{
                addPassword.setVisibility(View.GONE);
            }
            if(ynBleCapability.isRficCapability()){
                addRFIC.setOnClickListener(addRFICListener);
            }else{
                addRFIC.setVisibility(View.GONE);
            }
        }
        cancelBtn.setOnClickListener(cancelListener);

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
//        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
//        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if(onDismissListener != null){
            popupWindow.setOnDismissListener(onDismissListener);
        }else{
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    YoyonUtils.darkenBackground(1f,activity);
                }
            });
        }

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showBluetoothDeviceModifyKeyNamePopupwindow(final Activity activity, String originalName, TextWatcher nameTextWatcher, View.OnClickListener saveButtonListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
            }
        });
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                YoyonUtils.darkenBackground(1f,activity);
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        //这句话，让pop自适应输入状态
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();
        View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);
        TextView title = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_title);
        title.setText(activity.getResources().getString(R.string.key_name));
        final EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.length()+dest.toString().length() > 10){
                    Toast.makeText(activity,R.string.key_name_oversize,Toast.LENGTH_SHORT).show();
                    return "";
                }
                return source;
            }
        };
        keyName.setFilters(filters);
        keyName.setHint(activity.getResources().getString(R.string.key_name_oversize));
        if(originalName != null){
            keyName.setText(originalName);
            keyName.setSelection(originalName.length());
        }
        keyName.addTextChangedListener(nameTextWatcher);

        CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
        TextView keyIsManagerText = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManagerText);
        keyIsManager.setVisibility(View.GONE);
        keyIsManagerText.setVisibility(View.GONE);

        Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
        keyInfoNextStep.setText(activity.getResources().getString(R.string.save));
        keyInfoNextStep.setOnClickListener(saveButtonListener);
        views.add(keyInfo);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showBluetoothDeviceModifyLockNamePopupwindow(final Activity activity, String originalName, TextWatcher nameTextWatcher, View.OnClickListener saveButtonListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken() , 0);
            }
        });
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                YoyonUtils.darkenBackground(1f,activity);
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        //这句话，让pop自适应输入状态
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();
        View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);

        TextView title = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_title);
        title.setText(activity.getResources().getString(R.string.device_name));
        final EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
        InputFilter[] filters = new InputFilter[2];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.length()+dest.toString().length() > 12){
                    Toast.makeText(activity,R.string.lock_name_oversize,Toast.LENGTH_SHORT).show();
                    return "";
                }
                return source;
            }
        };
        filters[1] = new InputFilter.LengthFilter(12);
        keyName.setFilters(filters);
        keyName.setHint(activity.getResources().getString(R.string.lock_name_oversize));
        if(originalName != null){
            keyName.setText(originalName);
            keyName.setSelection(originalName.length());
        }
        keyName.addTextChangedListener(nameTextWatcher);

        CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
        TextView keyIsManagerText = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManagerText);
        keyIsManager.setVisibility(View.GONE);
        keyIsManagerText.setVisibility(View.GONE);



        Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
        keyInfoNextStep.setText(activity.getResources().getString(R.string.save));
        keyInfoNextStep.setOnClickListener(saveButtonListener);
        views.add(keyInfo);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showBluetoothDeviceModifyPropertyPopupwindow(final Activity activity, boolean originalProperty, CompoundButton.OnCheckedChangeListener isManagerListener, View.OnClickListener saveButtonListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                YoyonUtils.darkenBackground(1f,activity);
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        //这句话，让pop自适应输入状态
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();
        View keyInfo = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_key_info,null);

        TextView title = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_title);
        EditText keyName = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_name);
        title.setVisibility(View.GONE);
        keyName.setVisibility(View.GONE);

        CheckBox keyIsManager = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_isManager);
        keyIsManager.setChecked(originalProperty);
        keyIsManager.setOnCheckedChangeListener(isManagerListener);

        Button keyInfoNextStep = keyInfo.findViewById(R.id.popup_bluetoothdevice_add_key_info_nextStep);
        keyInfoNextStep.setText("保存");
        keyInfoNextStep.setOnClickListener(saveButtonListener);
        views.add(keyInfo);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showBluetoothDeviceModifyPasswordPopupwindow(final Activity activity, TextWatcher passwordTextWatcher, View.OnClickListener passwordNextStepListener){
        View contentView = LayoutInflater.from(activity).inflate(R.layout.pop_bottom,null);
        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(contentView, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                YoyonUtils.darkenBackground(1f,activity);
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        //这句话，让pop自适应输入状态
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        viewPager = contentView.findViewById(R.id.bottomPopupWindow_viewPager);

        List<View> views = new ArrayList<>();

        //输入密码页面
        View inputPasswordView = LayoutInflater.from(activity).inflate(R.layout.popup_bluetoothdevice_add_password,null);
        final EditText newPassword = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_new);
        final EditText confirmPassword = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_confirm);
        final TextView passwordTips = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_tips);
        final Button passwordNextStep = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_nextStep);
        CheckBox newPasswordVisible = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_new_visible);
        CheckBox confirmPasswordVisible = inputPasswordView.findViewById(R.id.popup_bluetoothdevice_add_password_confirm_visible);
        newPasswordVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //从密码不可见模式变为密码可见模式
                    newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    //从密码可见模式变为密码不可见模式
                    newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                newPassword.setSelection(newPassword.getText().length());
            }
        });
        confirmPasswordVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //从密码不可见模式变为密码可见模式
                    confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    //从密码可见模式变为密码不可见模式
                    confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                confirmPassword.setSelection(confirmPassword.getText().length());
            }
        });
        passwordNextStep.setOnClickListener(passwordNextStepListener);
        passwordNextStep.setAlpha(0.2f);
        passwordNextStep.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(newPassword.getText().length() < 6 || confirmPassword.getText().length() < 6){
//                    passwordTips.setText("密码长度不能少于6位");
                    passwordTips.setText("");
                    passwordNextStep.setAlpha(0.2f);
                    passwordNextStep.setEnabled(false);
                }else{
                    if(!confirmPassword.getText().toString().equalsIgnoreCase(newPassword.getText().toString())){
                        passwordTips.setText(R.string.confirm_password_difference);
                        passwordNextStep.setAlpha(0.2f);
                        passwordNextStep.setEnabled(false);
                    }else{
                        passwordTips.setText("");
                        passwordNextStep.setAlpha(1f);
                        passwordNextStep.setEnabled(true);
                    }
                }
            }
        };
        newPassword.addTextChangedListener(textWatcher);
        confirmPassword.addTextChangedListener(textWatcher);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if(source.length()+dest.toString().length() > 12){
                    Toast.makeText(activity,R.string.password_oversize,Toast.LENGTH_SHORT).show();
                    return "";
                }
                return source;
            }
        };
        newPassword.setFilters(filters);
        confirmPassword.setFilters(filters);
        confirmPassword.addTextChangedListener(passwordTextWatcher);


        //成功页面
        View successView = LayoutInflater.from(activity).inflate(R.layout.popup_success,null);
        successText = successView.findViewById(R.id.popup_success_text);
        Button successDone = successView.findViewById(R.id.popup_success_done);
        successDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });
        //失败页面
        View failView = LayoutInflater.from(activity).inflate(R.layout.popup_fail,null);
        failText = failView.findViewById(R.id.popup_fail_text);
        Button failCancel = failView.findViewById(R.id.popup_fail_cancel);
        failCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissPopupWindow();
            }
        });

        views.add(inputPasswordView);
        views.add(successView);
        views.add(failView);

        PopupWindowViewPagerAdapter adapter = new PopupWindowViewPagerAdapter(views);
        viewPager.setAdapter(adapter);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showWarningPopupWindow(final Activity activity, String title, String body, View.OnClickListener confirmListener){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_warn,null);
        TextView tv_title = view.findViewById(R.id.popup_warn_title);
        TextView tv_body = view.findViewById(R.id.popup_warn_body);
        Button btn_confirm = view.findViewById(R.id.popup_warn_confirm);
        Button btn_cancel = view.findViewById(R.id.popup_warn_cancel);

        tv_title.setText(title);
        tv_body.setText(body);
        btn_confirm.setOnClickListener(confirmListener);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {YoyonUtils.darkenBackground(1f,activity);
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showWarningPopupWindow(final Activity activity, String title, String body, String confirmText, View.OnClickListener confirmListener){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_warn,null);
        TextView tv_title = view.findViewById(R.id.popup_warn_title);
        TextView tv_body = view.findViewById(R.id.popup_warn_body);
        Button btn_confirm = view.findViewById(R.id.popup_warn_confirm);
        Button btn_cancel = view.findViewById(R.id.popup_warn_cancel);

        tv_title.setText(title);
        tv_body.setText(body);
        btn_confirm.setText(confirmText);
        btn_confirm.setOnClickListener(confirmListener);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {YoyonUtils.darkenBackground(1f,activity);
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void showOneButtonWarningPopupwindow(final Activity activity, String title, String body, View.OnClickListener confirmListener){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_warn,null);
        TextView tv_title = view.findViewById(R.id.popup_warn_title);
        TextView tv_body = view.findViewById(R.id.popup_warn_body);
        Button btn_confirm = view.findViewById(R.id.popup_warn_confirm);
        Button btn_cancel = view.findViewById(R.id.popup_warn_cancel);

        tv_title.setText(title);
        tv_body.setText(body);
        btn_confirm.setOnClickListener(confirmListener);
        btn_confirm.setVisibility(View.GONE);
        btn_cancel.setText(activity.getResources().getString(R.string.ok));
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {YoyonUtils.darkenBackground(1f,activity);
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    public void showFailPopupWindow(final Activity activity, String failText){
        View view = LayoutInflater.from(activity).inflate(R.layout.popup_fail,null);
        TextView tv_failText = view.findViewById(R.id.popup_fail_text);
        Button btn_cancel = view.findViewById(R.id.popup_fail_cancel);

        tv_failText.setText(failText);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        View parent = activity.getWindow().getDecorView();
        popupWindow = new PopupWindow(view, parent.getWidth(), (parent.getHeight()/7)*4, true);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {YoyonUtils.darkenBackground(1f,activity);
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        YoyonUtils.darkenBackground(0.5f,activity);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
    public void dismissPopupWindow(){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
    }

    public void setViewPagerToTargetItem(final Activity activity, final int targetItem){
        if(viewPager != null){
            PopupWindowViewPagerAdapter adapter = (PopupWindowViewPagerAdapter) viewPager.getAdapter();
            if(adapter != null && adapter.getCount() > targetItem){
                (activity).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(targetItem);
                    }
                });
            }
        }
    }

    public void setSuccessText(String s){
        if(successText != null){
            successText.setText(s);
        }
    }

    public void setFailText(String s){
        if(failText != null){
            failText.setText(s);
        }
    }

    @SuppressLint("HandlerLeak")
    public class PopupWindowHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(fingerprintAnimation != null){
                        Bundle bundle = msg.getData();
                        float from = bundle.getFloat("From");
                        float to = bundle.getFloat("To");
                        ValueAnimator animator = ValueAnimator.ofFloat(from,to);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                float progress = (float) valueAnimator.getAnimatedValue();
                                fingerprintAnimation.setProgress(progress);
                            }
                        });
                        animator.start();
                    }
                    break;
                case 2:

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
