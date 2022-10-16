package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.utils.YoyonHash;
import yoyon.smartlock.standalone.utils.YoyonUtils;


/**
 * Created by QinBin on 2018/7/20.
 */

public class OncePasswordActivity extends Activity{
    private YoyonHash yoyonHash = new YoyonHash();
    private HorizontalScrollView horizontalScrollView;
    private EditText editText_adminPassword,editText_random;
    private Button btn_generate;

    private TextView textView_result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_once_password);
        initComponent();
    }
    private void initComponent(){
        horizontalScrollView = findViewById(R.id.scrollView1);
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        LinearLayout cancelLayout = findViewById(R.id.oncePasswordActivity_cancelLayout);
        cancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        editText_adminPassword = findViewById(R.id.oncePasswordActivity_adminPassword);
        editText_random = findViewById(R.id.oncePasswordActivity_random);

        editText_adminPassword.requestFocus();
        editText_adminPassword.addTextChangedListener(mTextWatcher);
        editText_random.addTextChangedListener(mTextWatcher);

        btn_generate = findViewById(R.id.oncePasswordActivity_generate);
        btn_generate.setAlpha(0.2f);
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwdStr = editText_adminPassword.getText().toString();
                String rndStr = editText_random.getText().toString();
                editText_adminPassword.getText().clear();
                editText_random.getText().clear();
                YoyonUtils.hideKeyboard(view);
                byte[] result = yoyonHash.OTP_GENERATE(pwdStr.getBytes(),pwdStr.length(),4,0);
                int initial_key = yoyonHash.bytesToInt(result);
                result = yoyonHash.OTP_GENERATE(rndStr.getBytes(),rndStr.length(),8,initial_key);
                String res = new String(result);
                textView_result.setText(res);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        horizontalScrollView.fullScroll(ScrollView.FOCUS_RIGHT);
                    }
                },500);
            }
        });

        Button btn_copy = findViewById(R.id.oncePasswordActivity_copy);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", textView_result.getText().toString());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                Toast.makeText(OncePasswordActivity.this,"已复制到剪切板",Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout returnLayout = findViewById(R.id.oncePasswordActivity_returnLayout);
        returnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                horizontalScrollView.fullScroll(ScrollView.FOCUS_LEFT);
            }
        });
        textView_result = findViewById(R.id.oncePasswordActivity_result);
    }
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(editText_random.getText().toString().length() >= 4 && (editText_adminPassword.getText().toString().length() >= 6))
            {
                btn_generate.setEnabled(true);
                btn_generate.setAlpha(1f);
            }
            else
            {
                btn_generate.setEnabled(false);
                btn_generate.setAlpha(0.2f);
            }
        }
    };
}
