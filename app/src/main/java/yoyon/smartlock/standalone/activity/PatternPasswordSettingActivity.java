package yoyon.smartlock.standalone.activity;

import android.app.Activity;
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

public class PatternPasswordSettingActivity extends Activity implements View.OnClickListener{
    private PatternLockerView patternLockerView;
    private PatternIndicatorView patternIndicatorView;
    private TextView textMsg;
    private PatternHelper patternHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YoyonUtils.setTransparentStatusBar(this);
        setContentView(R.layout.activity_pattern_password_setting);
        initComponent();
    }
    private void initComponent(){
        LinearLayout returnArrow= findViewById(R.id.product_left);
        returnArrow.setOnClickListener(this);
        TextView top_title = findViewById(R.id.top_title);
        top_title.setText(R.string.set_gesture_password);

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
                boolean isOk = isPatternOk(hitList);
                view.updateStatus(!isOk);
                patternIndicatorView.updateState(hitList, !isOk);
                updateMsg();
            }

            @Override
            public void onClear(PatternLockerView view) {
                finishIfNeeded();
            }
        });

        this.textMsg.setText(R.string.set_unlock_pattern);
        this.patternHelper = new PatternHelper(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.product_left:
                finish();
                break;
            default:
                break;
        }
    }

    private boolean isPatternOk(List<Integer> hitList) {
        this.patternHelper.validateForSetting(hitList);
        return this.patternHelper.isOk();
    }

    private void updateMsg() {
        this.textMsg.setText(this.patternHelper.getMessage());
        this.textMsg.setTextColor(this.patternHelper.isOk() ?
                getResources().getColor(R.color.colorPrimary) :
                getResources().getColor(android.R.color.holo_red_dark));
    }

    private void finishIfNeeded() {
        if (this.patternHelper.isFinish()) {
            if(this.patternHelper.isOk()){
                setResult(1000);
            }else{
                setResult(1001);
            }
            finish();
        }
    }
}