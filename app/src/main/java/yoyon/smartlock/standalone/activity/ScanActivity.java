package yoyon.smartlock.standalone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.IOException;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.utils.YoyonUtils;

/**
 * Created by pyh on 2017/10/27.
 */

public class ScanActivity extends Activity implements DecoratedBarcodeView.TorchListener,View.OnClickListener{
    public static final int SCAN_GALLERY_SUCCESS = 4;
    public static final int SCAN_CAMERA_SUCCESS = -1;

    private CaptureManager captureManager;
    private DecoratedBarcodeView mDBV;
    private TextView flash;
    private boolean isLightOn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan);
        YoyonUtils.setTransparentStatusBar(this);
        initComponent();
        mDBV = findViewById(R.id.scan_dbv);
        //重要代码，初始化捕获
        captureManager = new CaptureManager(this,mDBV);
        captureManager.initializeFromIntent(getIntent(),savedInstanceState);
        captureManager.decode();

        super.onCreate(savedInstanceState);
    }


    private void initComponent(){
        LinearLayout cancelLayout = findViewById(R.id.scan_cancelLayout);
        LinearLayout galleryLayout = findViewById(R.id.scan_galleryLayout);

        cancelLayout.setOnClickListener(this);
        galleryLayout.setOnClickListener(this);
        flash = findViewById(R.id.scan_flash);

        flash.setOnClickListener(this);

        if(!hasFlash()){
            flash.setVisibility(View.GONE);
        }else{
            isLightOn = false;
            flash.setText(R.string.turn_on_flash);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }

    @Override
    public void onTorchOn() {

    }

    @Override
    public void onTorchOff() {

    } // 实现相关接口

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.scan_cancelLayout:
                finish();
                break;
            case R.id.scan_galleryLayout:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
                break;
            case R.id.scan_flash:
                if(isLightOn){
                    mDBV.setTorchOff();
                    isLightOn = false;
                    flash.setText(R.string.turn_on_flash);
                }else{
                    mDBV.setTorchOn();
                    isLightOn = true;
                    flash.setText(R.string.turn_off_flash);
                }
                break;
            default:
                break;
        }
    }
    // 判断是否有闪光灯功能
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && null != data){
            Result result = null;
            Uri sourceUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), sourceUri);
                bitmap = getSmallerBitmap(bitmap);
                // 获取bitmap的宽高，像素矩阵
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

                // 最新的库中，RGBLuminanceSource 的构造器参数不只是bitmap了
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
                Reader reader = new MultiFormatReader();

                // 尝试解析此bitmap，！！注意！！ 这个部分一定写到外层的try之中，因为只有在bitmap获取到之后才能解析。写外部可能会有异步的问题。（开始解析时bitmap为空）
                try {
                    result = reader.decode(binaryBitmap);
                    if(result == null){
                        Toast.makeText(ScanActivity.this,"请确认图片中含有二维码",Toast.LENGTH_SHORT).show();
                    }else{
                        switch (requestCode){
                            case 1: {
                                Intent intent = new Intent();
                                intent.putExtra("qrCode", result.getText());
                                this.setResult(SCAN_GALLERY_SUCCESS, intent);
                                finish();
                            }
                            break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(ScanActivity.this,"请确认图片中含有二维码",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Bitmap getSmallerBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() / 160000;
        if (size <= 1) return bitmap; // 如果小于
        else {
            Matrix matrix = new Matrix();
            matrix.postScale((float) (1 / Math.sqrt(size)), (float) (1 / Math.sqrt(size)));
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizeBitmap;
        }
    }
}
