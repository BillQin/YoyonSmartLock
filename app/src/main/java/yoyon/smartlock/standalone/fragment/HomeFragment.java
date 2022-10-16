package yoyon.smartlock.standalone.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.activity.ChooseLockModelActivity;
import yoyon.smartlock.standalone.activity.LockDetailsActivity;
import yoyon.smartlock.standalone.activity.OncePasswordActivity;
import yoyon.smartlock.standalone.adapter.BondedLockListViewAdapter;
import yoyon.smartlock.standalone.db.YoyonLockDao;
import yoyon.smartlock.standalone.model.YoyonLock;
import yoyon.smartlock.standalone.utils.SoundPlayUtils;

/**
 * Created by QinBin on 2017/9/27.
 */

public class HomeFragment extends Fragment implements View.OnClickListener{
    private ImageView imageView_addDevice;
    private MZBannerView mMZBanner;
    private LinearLayout layout_addDeviceNow;
    private BondedLockListViewAdapter adapter;
    private List<YoyonLock> yoyonLocks;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        initComponent(view);
        return view;
    }

    private void initComponent(View view){
        ImageView oncePassword = view.findViewById(R.id.homeFragment_oncePassword);
        oncePassword.setOnClickListener(this);
        Button btn_addDeviceNow = view.findViewById(R.id.homeFragment_addDeviceNow_btn);
        btn_addDeviceNow.setOnClickListener(this);

        LinearLayout layout_addDevice = view.findViewById(R.id.homeFragment_addDeviceLayout);
        layout_addDevice.setOnClickListener(this);

        layout_addDeviceNow = view.findViewById(R.id.homeFragment_addDeviceNow);

        ListView homeFragment_listView = view.findViewById(R.id.homeFragment_listView);
        yoyonLocks = YoyonLockDao.getInstance(getActivity().getApplicationContext()).getAllLock();
        if(yoyonLocks.size() <= 0){
            layout_addDeviceNow.setVisibility(View.VISIBLE);
        }else{
            layout_addDeviceNow.setVisibility(View.GONE);
        }
        adapter = new BondedLockListViewAdapter(getActivity(),yoyonLocks);
        homeFragment_listView.setAdapter(adapter);
        homeFragment_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), LockDetailsActivity.class);
                intent.putExtra("yoyonLock",(YoyonLock)parent.getItemAtPosition(position));
                startActivity(intent);
            }
        });

        mMZBanner = view.findViewById(R.id.banner);
        initBanner();
    }
    public void updateListView(){
        if(getActivity() != null){
            yoyonLocks.clear();
            yoyonLocks.addAll(YoyonLockDao.getInstance(getActivity().getApplicationContext()).getAllLock());
            if(yoyonLocks.size() <= 0){
                layout_addDeviceNow.setVisibility(View.VISIBLE);
            }else{
                layout_addDeviceNow.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homeFragment_addDeviceLayout:
            case R.id.homeFragment_addDeviceNow_btn:
                startActivity(new Intent(getActivity(),ChooseLockModelActivity.class));
                break;
            case R.id.homeFragment_oncePassword:
                startActivity(new Intent(getActivity(),OncePasswordActivity.class));
                break;
            default:
                break;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mMZBanner.start();//开始轮播
    }
    private void initBanner(){
        List<Integer> imageResList = new ArrayList<>();
        imageResList.add(R.drawable.banner1);
        imageResList.add(R.drawable.banner2);
        imageResList.add(R.drawable.banner3);
        mMZBanner.setPages(imageResList, new MZHolderCreator<BannerViewHolder>() {
            @Override
            public BannerViewHolder createViewHolder() {
                return new BannerViewHolder();
            }
        });
        mMZBanner.start();
    }
    public static class BannerViewHolder implements MZViewHolder<Integer> {
        private ImageView mImageView;
        @Override
        public View createView(Context context) {
            // 返回页面布局
            mImageView = new ImageView(context);
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            mImageView.setPadding(10,0,10,0);
            return mImageView;
        }

        @Override
        public void onBind(Context context, int i, Integer resId) {
            RequestOptions requestOptions = RequestOptions.bitmapTransform(new RoundedCorners(20)).placeholder(R.drawable.image_onloading);
            Glide.with(context).load(resId).apply(requestOptions).into(mImageView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMZBanner.pause();//暂停轮播
    }
}
