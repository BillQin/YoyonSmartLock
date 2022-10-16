package yoyon.smartlock.standalone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoyon.ynblelib.yoyon.YnBleKeyType;

import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.db.YoyonKeyDao;
import yoyon.smartlock.standalone.model.YoyonOperationLog;

/**
 * Created by user on 17/10/11.
 */

public class LockOperationLogListViewAdapter extends BaseAdapter{
    private Context context;
    private List<YoyonOperationLog> yoyonOperationLogs;
    public LockOperationLogListViewAdapter(Context context, List<YoyonOperationLog> yoyonOperationLogs){
        this.context = context;
        this.yoyonOperationLogs = yoyonOperationLogs;
    }
    @Override
    public int getCount() {
        return yoyonOperationLogs.size();
    }

    @Override
    public Object getItem(int position) {
        return yoyonOperationLogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if(convertView==null){
            holder=new Holder();
            convertView= LayoutInflater.from(context).inflate(R.layout.item_lock_operation_log,parent,false);
            holder.icon = convertView.findViewById(R.id.item_lock_operation_log_icon);
            holder.date = convertView.findViewById(R.id.item_lock_operation_log_date);
            holder.title = convertView.findViewById(R.id.item_lock_operation_log_title);
            holder.content = convertView.findViewById(R.id.item_lock_operation_log_content);
            convertView.setTag(holder);
        }else {
            holder= (Holder)convertView.getTag();
        }
        switch (yoyonOperationLogs.get(position).getType()){
            case 2:
            case 8:
            case 15:
                switch (yoyonOperationLogs.get(position).getAttachKeyType()){
                    case YnBleKeyType.FINGERPRINT:
                        holder.icon.setBackgroundResource(R.drawable.item_icon_fingerprint);
                        break;
                    case YnBleKeyType.PASSWORD:
                    case YnBleKeyType.TEMP_PASSWORD:
                        holder.icon.setBackgroundResource(R.drawable.item_icon_password);
                        break;
                    case YnBleKeyType.RFIC:
                        holder.icon.setBackgroundResource(R.drawable.item_icon_iccard);
                        break;
                    case YnBleKeyType.BLUETOOTH:
                        holder.icon.setBackgroundResource(R.drawable.item_icon_bluetooth);
                        break;
                    case YnBleKeyType.KEY:
                        holder.icon.setBackgroundResource(R.drawable.item_icon_key);
                        break;
                    default:
                        break;
                }
                break;
            case 13:
            case 14:
                holder.icon.setBackgroundResource(R.drawable.item_icon_warning);
                break;
            default:
                break;
        }
        String keyName = YoyonKeyDao.getInstance(context).getTheKeyName(yoyonOperationLogs.get(position).getAttachLockMac(),yoyonOperationLogs.get(position).getAttachKeyType());

        String title = "";
        StringBuilder content = new StringBuilder();
        switch (yoyonOperationLogs.get(position).getType()){
            case 2://开锁
                if(keyName != null && !keyName.equalsIgnoreCase("")){
                    content.append(keyName+"-");
                }
                if (yoyonOperationLogs.get(position).getAttachKeyType() != YnBleKeyType.TEMP_PASSWORD) {
                    content.append(context.getResources().getString(R.string.id)+"「"+yoyonOperationLogs.get(position).getAttachKeyIndex()+"」");
                }
                switch (yoyonOperationLogs.get(position).getAttachKeyType()){
                    case YnBleKeyType.FINGERPRINT:
                        title = context.getResources().getString(R.string.fingerprint_unlock);
                        break;
                    case YnBleKeyType.PASSWORD:
                        title = context.getResources().getString(R.string.password_unlock);
                        break;
                    case YnBleKeyType.RFIC:
                        title = context.getResources().getString(R.string.proximity_card_unlock);
                        break;
                    case YnBleKeyType.KEY:
                        title = context.getResources().getString(R.string.key_unlock);
                        break;
                    case YnBleKeyType.REMOTE:
                        title = context.getResources().getString(R.string.remote_unlock);
                        break;
                    case YnBleKeyType.BLUETOOTH:
                        title = context.getResources().getString(R.string.bluetooth_unlock);
                        break;
                    case YnBleKeyType.FACE:
                        title = context.getResources().getString(R.string.face_unlock);
                        break;
                    case YnBleKeyType.TEMP_PASSWORD:
                        title = context.getResources().getString(R.string.temp_password_unlock);
                        break;
                    case YnBleKeyType.UNDEFINE:
                    default:
                        title = context.getResources().getString(R.string.unlock);
                        break;
                }
                break;
            case 13://冻结
                switch (yoyonOperationLogs.get(position).getAttachKeyType()){
                    case YnBleKeyType.FINGERPRINT:
                        title = context.getResources().getString(R.string.fingerprint_freeze);
                        break;
                    case YnBleKeyType.PASSWORD:
                        title = context.getResources().getString(R.string.password_freeze);
                        break;
                    case YnBleKeyType.RFIC:
                        title = context.getResources().getString(R.string.proximity_card_freeze);
                        break;
                    case YnBleKeyType.KEY:
                        title = context.getResources().getString(R.string.key_freeze);
                        break;
                    case YnBleKeyType.REMOTE:
                        title = context.getResources().getString(R.string.remote_freeze);
                        break;
                    case YnBleKeyType.BLUETOOTH:
                        title = context.getResources().getString(R.string.bluetooth_freeze);
                        break;
                    case YnBleKeyType.FACE:
                        title = context.getResources().getString(R.string.face_freeze);
                        break;
                    case YnBleKeyType.UNDEFINE:
                    default:
                        title = context.getResources().getString(R.string.freeze);
                        break;
                }
                break;
            case 8://添加钥匙
                if(keyName != null && !keyName.equalsIgnoreCase("")){
                    content.append(keyName+"-");
                }
                content.append(context.getResources().getString(R.string.id)+"「"+yoyonOperationLogs.get(position).getAttachKeyIndex()+"」");
                switch (yoyonOperationLogs.get(position).getAttachKeyType()){
                    case YnBleKeyType.FINGERPRINT:
                        title = context.getResources().getString(R.string.add_fingerprint);
                        break;
                    case YnBleKeyType.PASSWORD:
                        title = context.getResources().getString(R.string.add_password);
                        break;
                    case YnBleKeyType.RFIC:
                        title = context.getResources().getString(R.string.add_proximity_card);
                        break;
                    case YnBleKeyType.KEY:
                        title = context.getResources().getString(R.string.add_key);
                        break;
                    case YnBleKeyType.REMOTE:
                        title = context.getResources().getString(R.string.add_remote);
                        break;
                    case YnBleKeyType.BLUETOOTH:
                        title = context.getResources().getString(R.string.add_bluetooth);
                        break;
                    case YnBleKeyType.FACE:
                        title = context.getResources().getString(R.string.add_face);
                        break;
                    case YnBleKeyType.UNDEFINE:
                    default:
                        title = context.getResources().getString(R.string.add);
                        break;
                }
                break;
            case 15://删除钥匙
                if(keyName != null && !keyName.equalsIgnoreCase("")){
                    content.append(keyName+"-");
                }
                content.append(context.getResources().getString(R.string.id)+"「"+yoyonOperationLogs.get(position).getAttachKeyIndex()+"」");
                switch (yoyonOperationLogs.get(position).getAttachKeyType()){
                    case YnBleKeyType.FINGERPRINT:
                        title = context.getResources().getString(R.string.delete_fingerprint);
                        break;
                    case YnBleKeyType.PASSWORD:
                        title = context.getResources().getString(R.string.delete_password);
                        break;
                    case YnBleKeyType.RFIC:
                        title = context.getResources().getString(R.string.delete_proximity_card);
                        break;
                    case YnBleKeyType.KEY:
                        title = context.getResources().getString(R.string.delete_key);
                        break;
                    case YnBleKeyType.REMOTE:
                        title = context.getResources().getString(R.string.delete_remote);
                        break;
                    case YnBleKeyType.BLUETOOTH:
                        title = context.getResources().getString(R.string.delete_bluetooth);
                        break;
                    case YnBleKeyType.FACE:
                        title = context.getResources().getString(R.string.delete_face);
                        break;
                    case YnBleKeyType.UNDEFINE:
                    default:
                        title = context.getResources().getString(R.string.delete);
                        break;
                }
                break;
            case 14://防撬报警
                if(yoyonOperationLogs.get(position).getAttachKeyType() == YnBleKeyType.KEY){
                    title = context.getResources().getString(R.string.alarm_key_detect);
                }else{
                    title = context.getResources().getString(R.string.alarm_anti_thief);
                }
                break;
            default:
                break;
        }

        holder.date.setText(yoyonOperationLogs.get(position).getDate());
        holder.title.setText(title);
        holder.content.setText(content.toString());
        return convertView;
    }
    class Holder{
        ImageView icon;
        TextView date;
        TextView title;
        TextView content;
    }
}
