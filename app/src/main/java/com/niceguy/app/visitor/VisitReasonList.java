package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private static final int ACTION_ADD = 1;
    private static final int ACTION_UPDATE = 2;
    private Activity activity = null;
    private TextView curpage,page,total,page1;
    private Button add,first,next,pre,last;
    private AlertDialog detailDialog;
    private DBHelper helper = null;
    private int pagesize = 10,total_page = 0,curpage_num=1;
    private long count = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.visit_reason_list, container, false);

        connectDB();
        initViews(view);

        updateList(curpage_num);

        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor item = (Cursor) parent.getItemAtPosition(position);
                String reason = item.getString(item.getColumnIndex("reason"));
                int rid = item.getInt(item.getColumnIndex("_id"));

                Toast.makeText(activity,"",Toast.LENGTH_SHORT).show();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.reason_detail, null);

                TextView reasonId = (TextView) detailView.findViewById(R.id.reason_detail_id);
                reasonId.setText(String.valueOf(rid));
                EditText reasonContent = (EditText) detailView.findViewById(R.id.reason_detail_content);
                reasonContent.setText(reason);

                showDetailDialog(detailView,ACTION_UPDATE);
                item.close();
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
        page1 = (TextView) view.findViewById(R.id.page1);

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
        connectDB();
        count = helper.getCount(TABLE);
        releaseDB();
        total_page = getTotalPage();
        switch (v.getId()){
            case R.id.reason_first_page:
                updateList(1);
                break;
            case R.id.reason_last_page:
                if(curpage_num != total_page && total_page>1){
                    updateList(total_page);
                }
                break;
            case R.id.reason_pre_page:
                if(curpage_num > 1){
                    updateList(curpage_num - 1);
                }
                break;
            case R.id.reason_next_page:

                if( curpage_num != total_page){
                    updateList(curpage_num+1);
                }
                break;
            case R.id.reason_add:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.reason_detail, null);
                detailView.findViewById(R.id.reason_id_block).setVisibility(View.INVISIBLE);
                showDetailDialog(detailView,ACTION_ADD);
                break;
        }
    }

    public void updateList(int page_num){

        curpage_num = page_num;
        connectDB();
        count = helper.getCount(TABLE);
        total.setText(String.valueOf(count));
        total_page = getTotalPage();
        page.setText(String.valueOf(total_page));
        page1.setText(String.valueOf(total_page));
        curpage.setText(String.valueOf(curpage_num));
        if(page_num == 1){
            page_num = 0;
        }
        int offset = page_num*pagesize;
        connectDB();
        Cursor cur = helper.fetchAll(TABLE, offset, pagesize);
        if(cur != null){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.reason_item, cur,
                    new String[]{"_id", "reason"}, new int[]{R.id.reason_id, R.id.reason});
            //实现列表的显示
            listView.setAdapter(adapter);
        }
        releaseDB();

    }

    private void showDetailDialog(final View view,int action) {
        String title = "详情";
        if(action == ACTION_ADD){
            title = "添加";
            detailDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(title).setView(view)
                    .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText t = (EditText) view.findViewById(R.id.reason_detail_content);
                            String reason = t.getText().toString();

                            ContentValues cv = new ContentValues();
                            if("".equals(reason.trim())){
                                Toast.makeText(getActivity(), "请填写来访原因", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }
                            cv.put("reason", reason);
                            connectDB();
                            helper.insert(TABLE, cv);
                            releaseDB();
                            updateList(1);

                            Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            detailDialog.dismiss();
                        }
                    }).create();
            detailDialog.show();
        }else{
            detailDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(title).setView(view)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText t = (EditText)view.findViewById(R.id.reason_detail_content);
                            String reason = t.getText().toString();
                            TextView v = (TextView) view.findViewById(R.id.reason_detail_id);

                            ContentValues cv = new ContentValues();
                            cv.put("reason",reason);

                            if("".equals(reason.trim())){
                                Toast.makeText(getActivity(), "请填写来访原因", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }

                            connectDB();
                            helper.update(TABLE, cv, Long.parseLong(v.getText().toString()));
                            releaseDB();
                            updateList(1);
                            Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("删除", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            TextView v = (TextView) view.findViewById(R.id.reason_detail_id);
                            connectDB();
                            helper.delete(TABLE, Long.parseLong(v.getText().toString()));
                            releaseDB();
                            updateList(1);
                            Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();
                            detailDialog.dismiss();
                        }
                    }).create();
            detailDialog.show();
        }

    }

    private int getTotalPage(){
        connectDB();
        count = helper.getCount(TABLE);
        releaseDB();

        if(count > 0 && count <pagesize){
            total_page = 1;
        }else{
            double d = (double) (count / pagesize);
            total_page = (int) Math.ceil(d);
        }

        return total_page;
    }

    private void connectDB(){
        if(helper==null){
            helper = new DBHelper(getContext());
        }
    }
    private void releaseDB(){
        /*if(helper!=null){
            helper.close();
        }*/
    }
}
