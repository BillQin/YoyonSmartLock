package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.utils.PatternHelper;
import yoyon.smartlock.standalone.utils.SharedPreferencesUtil;
import yoyon.smartlock.standalone.utils.YoyonUtils;


/**
 * Created by QinBin on 2018/6/26.
 */

public class SettingActivity extends Activity implements View.OnClickListener{
    private RelativeLayout setPatternPassword;
    private Switch isVerifyPatternPassword;
    private PatternHelper patternHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YoyonUtils.setTransparentStatusBar(this);
        setContentView(R.layout.activity_setting);
        initComponent();
    }

    private void initComponent(){
        LinearLayout returnArrow= findViewById(R.id.product_left);
        returnArrow.setOnClickListener(this);
        TextView top_title = findViewById(R.id.top_title);
        top_title.setText(R.string.setting);

        setPatternPassword = findViewById(R.id.settingActivity_setPatternPassword);
        setPatternPassword.setOnClickListener(this);

        isVerifyPatternPassword = findViewById(R.id.settingActivity_isVerifyPatternPassword);

        if(!SharedPreferencesUtil.getInstance().isEnablePatternPassword()){
            setPatternPassword.setClickable(false);
            setPatternPassword.setAlpha(0.2f);
        }
        patternHelper = new PatternHelper(this);
        isVerifyPatternPassword.setChecked(SharedPreferencesUtil.getInstance().isEnablePatternPassword() && patternHelper.getFromStorage() != null);
        isVerifyPatternPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferencesUtil.getInstance().setEnablePatternPassword(b);
                if(b){
                    setPatternPassword.setClickable(true);
                    setPatternPassword.setAlpha(1f);

                    if(patternHelper.getFromStorage() == null){
                        Intent intent = new Intent(SettingActivity.this,PatternPasswordSettingActivity.class);
                        startActivityForResult(intent,1);
                    }
                }else{
                    setPatternPassword.setClickable(false);
                    setPatternPassword.setAlpha(0.2f);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            switch (resultCode){
                case 1000:
                    break;
                case 1001:
                    isVerifyPatternPassword.setChecked(false);
                    break;
                default:
                    isVerifyPatternPassword.setChecked(false);
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.product_left:
                finish();
                break;
            case R.id.settingActivity_setPatternPassword:
                Intent intent = new Intent(SettingActivity.this,PatternPasswordVerifyActivity.class);
                intent.putExtra("IsGotoSetting",true);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
