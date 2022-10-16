package yoyon.smartlock.standalone.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yoyon.ynblelib.clj.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

import yoyon.smartlock.standalone.R;

/**
 * Created by pyh on 2018/3/29.
 */

public class ScannedLockListViewAdapter extends BaseAdapter{
    private Context context;
    private List<BleDevice> bleDeviceList;
    public ScannedLockListViewAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(BleDevice bleDevice) {
        List<String> bleDeviceMacList = new ArrayList<>();
        for (int i = 0; i < bleDeviceList.size(); i++) {
            bleDeviceMacList.add(bleDeviceList.get(i).getMac());
        }
        if(!bleDeviceMacList.contains(bleDevice.getMac())){
            bleDeviceList.add(bleDevice);
        }
    }

    public void clearDevice() {
        bleDeviceList.clear();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder = null;
        if(view == null){
            holder = new Holder();
            view = View.inflate(context, R.layout.item_scanned_lock_list, null);
            holder.tv_name = view.findViewById(R.id.item_scanned_lock_name);
            holder.tv_mac = view.findViewById(R.id.item_scanned_lock_mac);
            view.setTag(holder);
        }else{
            holder = (Holder) view.getTag();
        }
        final BleDevice bleDevice = getItem(i);
        holder.tv_name.setText(bleDevice.getName());
        holder.tv_mac.setText(bleDevice.getMac());
        return view;
    }
    class Holder{
        TextView tv_name;
        TextView tv_mac;
    }
}
