package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class VisitReasonList extends Fragment implements View.OnClickListener{

    private ListView listView = null;
    private static final String TABLE = "visit_reason";
    private Activity activity = null;
    private TextView curpage,page,total;
    private Button add,first,next,pre,last;
    private AlertDialog detailDialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.visit_reason_list, container, false);


        DBHelper helper = new DBHelper(getContext());

        initViews(view);

        Cursor cur = helper.fetchAll(TABLE, 0, 10);
        long count = helper.getCount(TABLE);

        if(cur != null){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.reason_item, cur,
                    new String[]{"_id", "reason"}, new int[]{R.id.reason_id, R.id.reason});
            //实现列表的显示
            listView.setAdapter(adapter);
        }

        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(activity,"test",Toast.LENGTH_SHORT).show();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.reason_detail, null);
                showDetailDialog(detailView);
            }
        });

        initEvents();
        return view;
    }

    private void initViews(View view){
        activity = getActivity();
        listView = (ListView) view.findViewById(R.id.reason_listview);

        curpage = (TextView) view.findViewById(R.id.reason_curpage);
        total = (TextView) view.findViewById(R.id.reason_total);
        page = (TextView) view.findViewById(R.id.reason_page);

        first = (Button) view.findViewById(R.id.reason_first_page);
        last = (Button) view.findViewById(R.id.reason_last_page);
        next = (Button) view.findViewById(R.id.reason_next_page);
        pre = (Button) view.findViewById(R.id.reason_pre_page);
        add = (Button) view.findViewById(R.id.reason_add);

    }

    private void initEvents(){
        last.setOnClickListener(this);
        first.setOnClickListener(this);
        next.setOnClickListener(this);
        pre.setOnClickListener(this);
        add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reason_first_page:

                break;
            case R.id.reason_last_page:
                break;
            case R.id.reason_pre_page:
                break;
            case R.id.reason_next_page:
                break;
            case R.id.reason_add:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.reason_detail, null);
                showDetailDialog(detailView);
                break;
        }
    }

    public void updateList(){

    }

    private void showDetailDialog(View view) {
        detailDialog = new AlertDialog.Builder(getActivity())
                .setTitle("详情").setView(view)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "保存成功", Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "删除", Toast.LENGTH_LONG).show();
                        detailDialog.dismiss();
                    }
                }).create();
        detailDialog.show();
    }
}
