package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.LockModelListViewAdapter;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2017/10/4.
 */

public class ChooseLockModelActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lock_model);
        YoyonUtils.setTransparentStatusBar(this);
        initComponent();
    }

    private void initComponent(){
        LinearLayout returnArrow= findViewById(R.id.product_left);
        returnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView top_title = findViewById(R.id.top_title);
        top_title.setText(R.string.product_list);
        ListView listView_lockModel = findViewById(R.id.chooseLockModelActivity_listView);
        List<String> lockModels = new ArrayList<>();
        lockModels.add(getResources().getString(R.string.bluetooth_smart_lock));
        LockModelListViewAdapter lockModelListViewAdapter = new LockModelListViewAdapter(this,lockModels);
        listView_lockModel.setAdapter(lockModelListViewAdapter);
        listView_lockModel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(ChooseLockModelActivity.this,PairingLockTipsActivity.class));
            }
        });
    }
}
