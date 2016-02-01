package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;

import java.sql.SQLException;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class DepartmentList extends Fragment implements View.OnClickListener{

    private ListView listView = null;
    private static final String TABLE = "department";
    private static final int ACTION_ADD = 1;
    private static final int ACTION_UPDATE = 2;
    private Activity activity = null;
    private TextView curpage,page,total,page1;
    private Button add,first,next,pre,last;
    private AlertDialog detailDialog;
    private DBHelper helper = null;
    private SQLiteDatabase db = null;
    private int pagesize = 7,total_page = 0,curpage_num=1;
    private long count = 0;
    private String old_dept_name;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.department_list, container, false);

        connectDB();

        initViews(view);

        updateList(curpage_num);

        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor item = (Cursor) parent.getItemAtPosition(position);
                String code = item.getString(item.getColumnIndex("code_num"));
                String name = item.getString(item.getColumnIndex("dept_name"));
                int _id = item.getInt(item.getColumnIndex("_id"));

                old_dept_name = name;

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.dept_detail, null);

                TextView deptId = (TextView) detailView.findViewById(R.id.dept_detail_id);
                deptId.setText(String.valueOf(_id));
                EditText deptName = (EditText) detailView.findViewById(R.id.dept_detail_name);
                deptName.setText(name);
                EditText deptCode = (EditText) detailView.findViewById(R.id.dept_detail_code);
                deptCode.setText(code);

                showDetailDialog(detailView, ACTION_UPDATE);
            }
        });

        initEvents();
        return view;
    }

    private void initViews(View view){
        activity = getActivity();
        listView = (ListView) view.findViewById(R.id.dept_listview);

        curpage = (TextView) view.findViewById(R.id.dept_curpage);
        total = (TextView) view.findViewById(R.id.dept_total);
        page = (TextView) view.findViewById(R.id.dept_page);
        page1 = (TextView) view.findViewById(R.id.page1);

        first = (Button) view.findViewById(R.id.dept_first_page);
        last = (Button) view.findViewById(R.id.dept_last_page);
        next = (Button) view.findViewById(R.id.dept_next_page);
        pre = (Button) view.findViewById(R.id.dept_pre_page);
        add = (Button) view.findViewById(R.id.dept_add);

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
            case R.id.dept_first_page:
                updateList(1);
                break;
            case R.id.dept_last_page:
                if(curpage_num != total_page && total_page>1){
                    updateList(total_page);
                }
                break;
            case R.id.dept_pre_page:
                if(curpage_num > 1){
                    updateList(curpage_num - 1);
                }
                break;
            case R.id.dept_next_page:

                if( curpage_num != total_page){
                    updateList(curpage_num+1);
                }
                break;
            case R.id.dept_add:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.dept_detail, null);
                detailView.findViewById(R.id.dept_id_block).setVisibility(View.INVISIBLE);
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
        int offset = 0;
        if(page_num>0){
            offset = (page_num-1)*pagesize;
        }else{
            offset = page_num*pagesize;
        }
        connectDB();
        Cursor cur = helper.fetchAll(TABLE, offset, pagesize);
        if(cur != null){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.dept_item, cur,
                    new String[]{"_id", "dept_name","code_num"}, new int[]{R.id.dept_id, R.id.dept_name,R.id.dept_code});
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
                            EditText t = (EditText) view.findViewById(R.id.dept_detail_name);
                            String name = t.getText().toString();
                            EditText t1 = (EditText) view.findViewById(R.id.dept_detail_code);
                            String code = t1.getText().toString();

                            ContentValues cv = new ContentValues();
                            if ("".equals(name.trim())) {
                                Toast.makeText(getActivity(), "请填写部门名称", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }
                            connectDB();
                            try {
                                Cursor c = helper.fetchDepartmentByName(name);
                                if (c.getCount() > 0) {
                                    Toast.makeText(getActivity(), "部门名称已经存在", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                c.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            cv.put("dept_name", name);
                            cv.put("code_num", code);
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
                    .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            TextView v = (TextView) view.findViewById(R.id.dept_detail_id);

                            EditText t = (EditText) view.findViewById(R.id.dept_detail_name);
                            String name = t.getText().toString();
                            EditText t1 = (EditText) view.findViewById(R.id.dept_detail_code);
                            String code = t1.getText().toString();

                            ContentValues cv = new ContentValues();
                            if ("".equals(name.trim())) {
                                Toast.makeText(getActivity(), "请填写部门名称", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }

                            connectDB();
                            try {
                                if (!name.equals(old_dept_name)) {
                                    Cursor c = helper.fetchDepartmentByName(name);
                                    if (c.getCount() > 0) {
                                        Toast.makeText(getActivity(), "部门名称已经存在", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    c.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            cv.put("dept_name", name);
                            cv.put("code_num", code);

                            helper.update(TABLE, cv, Long.parseLong(v.getText().toString()));
                            releaseDB();
                            updateList(1);
                            Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("删除", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            TextView v = (TextView) view.findViewById(R.id.dept_detail_id);
                            connectDB();
                            helper.delete(TABLE,Long.parseLong(v.getText().toString()));
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

        if(count > 0 && count <pagesize){
            total_page = 1;
        }else{
            double d = (Double.parseDouble(String.valueOf(count)) / Double.parseDouble(String.valueOf(pagesize)));
            total_page = (int) Math.ceil(d);
        }
        releaseDB();
        return total_page;
    }

    private void connectDB(){
        if(helper==null){
            helper = DBHelper.getInstance(getActivity());
        }
    }
    private void releaseDB(){
        /*if(helper!=null){
            helper.close();
        }*/
    }
}
