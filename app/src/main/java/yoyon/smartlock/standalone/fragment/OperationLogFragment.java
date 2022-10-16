package yoyon.smartlock.standalone.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.List;

import yoyon.smartlock.standalone.R;
import yoyon.smartlock.standalone.adapter.LockOperationLogListViewAdapter;
import yoyon.smartlock.standalone.db.YoyonOperationLogDao;
import yoyon.smartlock.standalone.model.YoyonOperationLog;

/**
 * Created by QinBin on 2017/9/27.
 */

public class OperationLogFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_operation_log,container,false);
        initComponent(view);
        return view;
    }

    private void initComponent(View view){
        LinearLayout product_left= view.findViewById(R.id.product_left);
        product_left.setVisibility(View.GONE);
        TextView top_title = view.findViewById(R.id.top_title);
        top_title.setText(R.string.message);
        initOperationLogListView(view);
    }

    private ListView operationLogListView;
    private LockOperationLogListViewAdapter lockOperationLogListViewAdapter;
    private List<YoyonOperationLog> yoyonOperationLogs;
    private RefreshLayout refreshLayout_operationLog;
    private LinearLayout noLogTipsLayout;

    private void initOperationLogListView(View view){
        operationLogListView = view.findViewById(R.id.operationLogFragment_listView);
        refreshLayout_operationLog = view.findViewById(R.id.operationLogFragment_reFreshLayout);
        refreshLayout_operationLog.setRefreshHeader(new ClassicsHeader(getActivity()));//设置Header
        refreshLayout_operationLog.setRefreshFooter(new ClassicsFooter(getActivity()));//设置Footer
        refreshLayout_operationLog.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshLayout_operationLog.finishRefresh(true);
            }
        });
        refreshLayout_operationLog.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                refreshLayout_operationLog.finishLoadmore(true);
            }
        });
        yoyonOperationLogs = YoyonOperationLogDao.getInstance(getActivity()).getAllOperationLog();
        lockOperationLogListViewAdapter = new LockOperationLogListViewAdapter(getActivity(), yoyonOperationLogs);
        operationLogListView.setAdapter(lockOperationLogListViewAdapter);

        noLogTipsLayout = view.findViewById(R.id.operationLogFragment_emptyLayout);
        if(yoyonOperationLogs.size() > 0){
            noLogTipsLayout.setVisibility(View.GONE);
        }else{
            noLogTipsLayout.setVisibility(View.VISIBLE);
        }
    }
    public void updateListView(){
        if (yoyonOperationLogs == null) {
            return;
        }
        yoyonOperationLogs.clear();
        yoyonOperationLogs.addAll(YoyonOperationLogDao.getInstance(getActivity()).getAllOperationLog());
        lockOperationLogListViewAdapter.notifyDataSetChanged();
        if(yoyonOperationLogs.size() > 0){
            noLogTipsLayout.setVisibility(View.GONE);
        }else{
            noLogTipsLayout.setVisibility(View.VISIBLE);
        }
    }
}
