package yoyon.smartlock.standalone.fragment;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.activity.PrivacyPolicyActivity;
import yoyon.smartlock.standalone.activity.SettingActivity;
import yoyon.smartlock.standalone.activity.UserAgreementActivity;
import yoyon.smartlock.standalone.utils.VersionUtils;

/**
 * Created by QinBin on 2017/9/27.
 */

public class MineFragment extends Fragment implements View.OnClickListener{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine,container,false);
        initComponent(view);
        return view;
    }
    private void initComponent(View view){
        LinearLayout product_left= view.findViewById(R.id.product_left);
        product_left.setVisibility(View.GONE);
        TextView top_title = view.findViewById(R.id.top_title);
        top_title.setText(R.string.me);
        RelativeLayout rl_setting = view.findViewById(R.id.mineFragment_setting);
        rl_setting.setOnClickListener(this);
        RelativeLayout rl_checkUpdate = view.findViewById(R.id.mineFragment_checkUpdate);
        rl_checkUpdate.setOnClickListener(this);
        RelativeLayout rl_privatePolicy = view.findViewById(R.id.mineFragment_privacyPolicy);
        rl_privatePolicy.setOnClickListener(this);
        RelativeLayout rl_userAgreement = view.findViewById(R.id.mineFragment_userAgreement);
        rl_userAgreement.setOnClickListener(this);
        TextView versionCode = view.findViewById(R.id.mineFragment_versionCode);
        versionCode.setText(VersionUtils.getVersionName(getActivity()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mineFragment_setting:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.mineFragment_checkUpdate:
                NetworkInfo netInfo = ((ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if(netInfo == null){
                    Toast.makeText(getActivity(),getResources().getString(R.string.no_network),Toast.LENGTH_SHORT).show();
                }else{
                    VersionUtils.checkUpdate(getActivity(),true);
                }
                break;
            case R.id.mineFragment_privacyPolicy:
                startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
                break;
            case R.id.mineFragment_userAgreement:
                startActivity(new Intent(getActivity(), UserAgreementActivity.class));
                break;
            default:
                break;
        }
    }
}
