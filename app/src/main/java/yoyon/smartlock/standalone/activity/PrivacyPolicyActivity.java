package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by QinBin on 2018/6/26.
 */

public class PrivacyPolicyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YoyonUtils.setTransparentStatusBar(this);
        setContentView(R.layout.activity_privacy_policy);
        initComponent();
    }

    private void initComponent() {
        LinearLayout returnArrow= findViewById(R.id.product_left);
        returnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView top_title = findViewById(R.id.top_title);
        top_title.setText("隐私政策");

        WebView webView = findViewById(R.id.privacy_policy_webview);
        webView.loadUrl("file:///android_asset/web/privacy_policy.html");
    }
}
