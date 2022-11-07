package yoyon.smartlock.standalone.activity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import yoyon.smartlock.standalone.R;

/**
 * Created by QinBin on 2018/5/8.
 */

public class PairingLockTipsActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing_lock_tips);
        initComponent();
        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 0);
    }
    private void initComponent(){
        LinearLayout cancelLayout = findViewById(R.id.pairingLockTipsActivity_cancelLayout);
        Button nextStep = findViewById(R.id.pairingLockTipsActivity_nextStep);

        cancelLayout.setOnClickListener(this);
        nextStep.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.pairingLockTipsActivity_cancelLayout:
                finish();
                break;
            case R.id.pairingLockTipsActivity_nextStep:
                startActivity(new Intent(PairingLockTipsActivity.this,PairingLockActivity.class));
                finish();
                break;
            default:
                break;
        }
    }
}
