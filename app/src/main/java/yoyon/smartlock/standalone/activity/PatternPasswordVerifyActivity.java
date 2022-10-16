package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ihsg.patternlocker.OnPatternChangeListener;
import com.github.ihsg.patternlocker.PatternIndicatorView;
import com.github.ihsg.patternlocker.PatternLockerView;

import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.utils.PatternHelper;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2018/8/10.
 */

public class PatternPasswordVerifyActivity extends Activity implements View.OnClickListener {
    private PatternLockerView patternLockerView;
    private PatternIndicatorView patternIndicatorView;
    private TextView textMsg;
    private PatternHelper patternHelper;
    private boolean isGotoSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YoyonUtils.setTransparentStatusBar(this);
        setContentView(R.layout.activity_pattern_password_setting);
        getDataFromLastActivity();
        initComponent();
    }

    private void getDataFromLastActivity() {
        isGotoSetting = getIntent().getBooleanExtra("IsGotoSetting", false);
    }

    private void initComponent() {
        LinearLayout returnArrow = findViewById(R.id.product_left);
        returnArrow.setOnClickListener(this);
        if (!isGotoSetting) {
            returnArrow.setVisibility(View.GONE);
        }
        TextView top_title = findViewById(R.id.top_title);
        top_title.setText(R.string.gesture_password_validation);

        this.patternIndicatorView = findViewById(R.id.pattern_indicator_view);
        this.patternLockerView = findViewById(R.id.pattern_lock_view);
        this.textMsg = findViewById(R.id.text_msg);

        this.patternLockerView.setOnPatternChangedListener(new OnPatternChangeListener() {
            @Override
            public void onStart(PatternLockerView view) {
            }

            @Override
            public void onChange(PatternLockerView view, List<Integer> hitList) {
            }

            @Override
            public void onComplete(PatternLockerView view, List<Integer> hitList) {
                boolean isError = !isPatternOk(hitList);
                view.updateStatus(isError);
                patternIndicatorView.updateState(hitList, isError);
                updateMsg();
            }

            @Override
            public void onClear(PatternLockerView view) {
                finishIfNeeded();
            }
        });

        this.textMsg.setText(R.string.draw_unlock_pattern);
        this.patternHelper = new PatternHelper(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.product_left:
                if (!isGotoSetting) {
                    Intent intent = new Intent();
                    intent.setAction("VerifyPatternPasswordGiveUp");
                    sendBroadcast(intent);
                }
                finish();
                break;
            default:
                break;
        }
    }

    private boolean isPatternOk(List<Integer> hitList) {
        this.patternHelper.validateForChecking(hitList);
        return this.patternHelper.isOk();
    }

    private void updateMsg() {
        this.textMsg.setText(this.patternHelper.getMessage());
        this.textMsg.setTextColor(this.patternHelper.isOk() ?
                getResources().getColor(R.color.colorPrimary) :
                getResources().getColor(R.color.colorAccent));
    }

    private void finishIfNeeded() {
        if (this.patternHelper.isFinish()) {
            if (this.patternHelper.isOk()) {
                if (isGotoSetting) {
                    Intent intent = new Intent(PatternPasswordVerifyActivity.this, PatternPasswordSettingActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }
    }
}
