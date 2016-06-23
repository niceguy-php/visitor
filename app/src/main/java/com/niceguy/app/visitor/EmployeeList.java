package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.niceguy.app.utils.DBHelper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class EmployeeList extends Fragment implements View.OnClickListener{

    private ListView listView = null;
    private static final String TABLE = "user";
    private static final String TABLE_USER_DEPARTMENT = "user_department";
    public static final String USER_IMPORT_DIR= Environment.getExternalStorageDirectory().getPath()+"/sicheng/user_import/";
    private static final int ACTION_ADD = 1;
    private static final int ACTION_UPDATE = 2;
    public static final int USER_TYPE_EMPLOYEE = 1;//员工
    public static final int USER_TYPE_DUTY = 2;//值班用户
    private static final int SEX_MALE = 1;//男
    private static final int SEX_FEMALE = 2;//女

    private Activity activity = null;
    private TextView curpage,page,total,page1;
    private Button add,first,next,pre,last,import_insert;
    private AlertDialog detailDialog,importDialog;
    private DBHelper helper = null;
    private int pagesize = 7,total_page = 0,curpage_num=1;
    private long count = 0;
    private String user_dept = null;
    private String user_old_dept = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_list, container, false);

        connectDB();
        File import_dir = new File(DutyUserList.USER_IMPORT_DIR);
        if(!import_dir.exists()){
            import_dir.mkdirs();
        }

        initViews(view);

        updateList(curpage_num);

        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor item = (Cursor) parent.getItemAtPosition(position);
                String code = item.getString(item.getColumnIndex("code_num"));
                String name = item.getString(item.getColumnIndex("username"));
                String dept_name = item.getString(item.getColumnIndex("dept_name"));
                String user_position = item.getString(item.getColumnIndex("position"));
                String phone = item.getString(item.getColumnIndex("phone"));
                String sex = item.getString(item.getColumnIndex("sex"));
                int _id = item.getInt(item.getColumnIndex("_id"));

                user_old_dept = dept_name;
                user_dept = user_old_dept;

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.user_detail, null);

                TextView userId = (TextView) detailView.findViewById(R.id.user_detail_id);
                userId.setText(String.valueOf(_id));

                EditText userName = (EditText) detailView.findViewById(R.id.user_detail_name);
                userName.setText(name);

                EditText userCode = (EditText) detailView.findViewById(R.id.user_detail_code);
                userCode.setText(code);

                EditText userPosition = (EditText) detailView.findViewById(R.id.user_detail_position);
                userPosition.setText(user_position);

                EditText userPhone = (EditText) detailView.findViewById(R.id.user_detail_phone);
                userPhone.setText(phone);

                RadioButton userSexFemale = (RadioButton) detailView.findViewById(R.id.user_detail_sex_female);
                RadioButton userSexMale = (RadioButton) detailView.findViewById(R.id.user_detail_sex_male);

                Spinner deptlist = (Spinner) detailView.findViewById(R.id.user_detail_dept);

                connectDB();
                final String[] deptNames = helper.getDeptNames();
                int selectedIndex = 0;
                for(int i=0;i<deptNames.length;i++){
                    if(deptNames[i].equals(dept_name)){
                        selectedIndex = i;
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, deptNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deptlist.setAdapter(adapter);
                deptlist.setSelection(selectedIndex, true);

                if(deptlist.getCount()==0){
                    Toast.makeText(getActivity(), "请先新建部门，在添加部门成员", Toast.LENGTH_SHORT).show();
                    return;
                }

                deptlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        user_dept = deptNames[position];
                        Toast.makeText(getActivity(), user_dept + "---", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        user_dept = user_old_dept;
                    }
                });

                if("男".equals(sex)){
                    userSexMale.setChecked(true);
                    userSexFemale.setChecked(false);
                }else{
                    userSexFemale.setChecked(true);
                    userSexMale.setChecked(false);
                }

                showDetailDialog(detailView,ACTION_UPDATE);
                releaseDB();
            }
        });

        initEvents();
        return view;
    }

    private void initViews(View view){
        activity = getActivity();
        listView = (ListView) view.findViewById(R.id.user_listview);

        curpage = (TextView) view.findViewById(R.id.user_curpage);
        total = (TextView) view.findViewById(R.id.user_total);
        page = (TextView) view.findViewById(R.id.user_page);
        page1 = (TextView) view.findViewById(R.id.page1);

        first = (Button) view.findViewById(R.id.user_first_page);
        last = (Button) view.findViewById(R.id.user_last_page);
        next = (Button) view.findViewById(R.id.user_next_page);
        pre = (Button) view.findViewById(R.id.user_pre_page);
        add = (Button) view.findViewById(R.id.user_add);
        import_insert = (Button) view.findViewById(R.id.user_import);


    }

    private void initEvents(){
        last.setOnClickListener(this);
        first.setOnClickListener(this);
        next.setOnClickListener(this);
        pre.setOnClickListener(this);
        add.setOnClickListener(this);
        import_insert.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        connectDB();
        count = helper.getCount(TABLE,"user_type=1");
        releaseDB();
        total_page = getTotalPage();
        switch (v.getId()){
            case R.id.user_first_page:
                updateList(1);
                break;
            case R.id.user_last_page:
                if(curpage_num != total_page && total_page>1){
                    updateList(total_page);
                }
                break;
            case R.id.user_pre_page:
                if(curpage_num > 1){
                    updateList(curpage_num - 1);
                }
                break;
            case R.id.user_next_page:

                if( curpage_num != total_page){
                    updateList(curpage_num+1);
                }
                break;
            case R.id.user_add:
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View detailView = inflater.inflate(R.layout.user_detail, null);
                detailView.findViewById(R.id.user_id_block).setVisibility(View.INVISIBLE);

                Spinner deptlist = (Spinner) detailView.findViewById(R.id.user_detail_dept);

                connectDB();
                final String[] deptNames = helper.getDeptNames();
                releaseDB();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, deptNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deptlist.setAdapter(adapter);
                deptlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        user_dept = deptNames[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        Toast.makeText(getActivity(), "请选择员工所属部门", Toast.LENGTH_SHORT).show();
                    }
                });
                if(deptlist.getCount()==0){
                    Toast.makeText(getActivity(), "请先新建部门，在添加部门成员", Toast.LENGTH_SHORT).show();
                    return;
                }
                showDetailDialog(detailView, ACTION_ADD);
                break;
            case R.id.user_import:
                LayoutInflater inflater1 = getActivity().getLayoutInflater();
                View importView = inflater1.inflate(R.layout.import_view, null);
                String[] import_file_list = null;
                File  f = new File(USER_IMPORT_DIR);
                import_file_list = f.list();
                if (import_file_list !=null && import_file_list.length >0){
                    Spinner importSpinner = (Spinner) importView.findViewById(R.id.import_file);
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, import_file_list);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    importSpinner.setAdapter(adapter1);
                    importView.findViewById(R.id.import_file_create_time).setVisibility(View.INVISIBLE);
                    showImportDialog(importView);
                }else{
                    Toast.makeText(getActivity(), "未发现需要导入的文件", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void updateList(int page_num){

        connectDB();
        curpage_num = page_num;
        count = helper.getCount(TABLE,"user_type=1");
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
        Cursor cur = helper.fetchAllUser(USER_TYPE_EMPLOYEE, offset, pagesize);
        if(cur != null){
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, R.layout.user_item, cur,
                    new String[]{"_id", "username","sex","code_num","dept_name","position","phone"}, new int[]{R.id.user_id, R.id.user_name,R.id.user_sex,R.id.user_code,R.id.user_dept,R.id.user_position,R.id.user_phone});
            //实现列表的显示
            listView.setAdapter(adapter);
        }

        releaseDB();
    }

    private void showImportDialog(final View view){
        importDialog = new AlertDialog.Builder(getActivity())
                .setTitle("导入部门成员").setView(view)
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("开始导入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Spinner import_spinner = (Spinner) view.findViewById(R.id.import_file);

                        String file_name = import_spinner.getSelectedItem().toString().trim();

                        File import_file = new File(USER_IMPORT_DIR + '/' + file_name);
                        if (import_file.exists()) {

                            ArrayList<String> lines = new ArrayList<String>();
                            BufferedReader reader;
                            try {
                                //lines = (ArrayList<String>) FileUtils.readLines(import_file, "gb2312");
                                FileInputStream fis = new FileInputStream(import_file);
                                BufferedInputStream in = new BufferedInputStream(fis);
                                in.mark(4);
                                byte[] first3bytes = new byte[3];
                                in.read(first3bytes);//找到文档的前三个字节并自动判断文档类型。
                                in.reset();
                                if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {
                                    reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                                } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                                    reader = new BufferedReader(new InputStreamReader(in, "unicode"));
                                } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                                    reader = new BufferedReader(new InputStreamReader(in, "utf-16be"));
                                } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                                    reader = new BufferedReader(new InputStreamReader(in, "utf-16le"));
                                } else {
                                    reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                                }

                                //InputStreamReader isr = new InputStreamReader(new FileInputStream(import_file), "gb2312");//UTF-8
                                //BufferedReader br = new BufferedReader(isr);

                                String mimeTypeLine = null;
                                while ((mimeTypeLine = reader.readLine()) != null) {
                                    lines.add(mimeTypeLine);
                                }

                                reader.close();
                                in.close();
                                fis.close();

                                //if (checkImport(lines)) {
                                connectDB();
                                int len = lines.size();
                                for (int i = 0; i < len; i++) {
                                    String info = (String) lines.get(i);
                                    String[] info_arr = info.split(",");
                                    if (info_arr.length > 2) {
                                        ContentValues cv = new ContentValues();

                                        String deptname = info_arr[0];//new String(info_arr[0].getBytes("gb2312"),"utf-8");
                                        Log.v("YYX", deptname);
                                        //校验部门名称是否存在？
                                        int deptId = 0;
                                        try {
                                            Cursor c = helper.fetchDepartmentByName(deptname);
                                            if (c.getCount() == 0) {
                                                Log.v("YYX", deptname + "部门名称不存在");
                                                continue;
                                            } else
                                                deptId = c.getInt(c.getColumnIndex("_id"));
                                            c.close();
                                        } catch (SQLException e) {
                                            //e.printStackTrace();
                                        }
                                        String username = info_arr[1];//new String(info_arr[0].getBytes("gb2312"),"utf-8");
                                        Log.v("YYX", username);

                                        String strsex = "男";
                                        int sex = 1;
                                        if (info_arr.length >= 3) {
                                            strsex = info_arr[2];//new String(info_arr[1].getBytes("gb2312"),"utf-8");
                                            if ("男".equals(strsex.trim()) ) sex = 1;
                                            else sex = 2;

                                        }

                                        String code = "";
                                        if (info_arr.length >= 4) {
                                            code = info_arr[3];//new String(info_arr[1].getBytes("gb2312"),"utf-8");
                                        }
                                        String position = "";
                                        if (info_arr.length >= 5) {
                                            position = info_arr[4];//new String(info_arr[1].getBytes("gb2312"),"utf-8");
                                        }
                                        String phone = "";
                                        if (info_arr.length >= 6) {
                                            phone = info_arr[5];//new String(info_arr[1].getBytes("gb2312"),"utf-8");
                                        }

                                        cv.put("username", username);
                                        cv.put("sex", sex);  //1 男  2女
                                        cv.put("code_num", code);
                                        cv.put("position", position);
                                        cv.put("phone", phone);
                                        cv.put("user_type", 1);//1 是员工， 2 是值班人员

                                        long userId = helper.insert(TABLE, cv);
                                        if (deptId > 0 && userId > 0) {
                                            ContentValues cv1 = new ContentValues();
                                            cv1.put("dept_id", deptId);
                                            cv1.put("user_id", userId);
                                            helper.insert(TABLE_USER_DEPARTMENT, cv1);
                                        }

                                    }
                                    updateList(1);
                                    Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                                }
                                //} else {
                                //    return;
                                //}

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity(), "选择的文件不存在", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }).create();
        importDialog.show();
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
                            connectDB();
                            EditText name = (EditText)view.findViewById(R.id.user_detail_name);
                            EditText code = (EditText)view.findViewById(R.id.user_detail_code);
                            EditText pos = (EditText)view.findViewById(R.id.user_detail_position);
                            EditText phone = (EditText)view.findViewById(R.id.user_detail_phone);
                            RadioButton userSexFemale = (RadioButton) view.findViewById(R.id.user_detail_sex_female);

                            ContentValues cv = new ContentValues();
                            cv.put("username",name.getText().toString());
                            cv.put("phone",phone.getText().toString());
                            cv.put("code_num", code.getText().toString());
                            cv.put("position", pos.getText().toString());

                            if("".equals(cv.get("username").toString())){
                                Toast.makeText(getActivity(), "请填写员工姓名", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }
                            if("".equals(cv.get("phone").toString())){
                                Toast.makeText(getActivity(), "请填写员工电话", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }

                            if(userSexFemale.isChecked()){
                                cv.put("sex",SEX_FEMALE);
                            }else{
                                cv.put("sex",SEX_MALE);
                            }

                            int dept_id = 0;
                            try {
                                Cursor cursor = helper.fetchDepartmentByName(user_dept);
                                cursor.moveToFirst();
                                dept_id = cursor.getInt(cursor.getColumnIndex("_id"));
                                Log.v("YYX", user_dept + "------------------" + dept_id);
                                cursor.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            long user_id = helper.insert(TABLE, cv);

                            if(dept_id == 0 || user_id == 0){
                                Toast.makeText(getActivity(),"请选择员工所属部门",Toast.LENGTH_SHORT).show();
                                detailDialog.show();
                                return;
                            }


                            ContentValues cv1 = new ContentValues();
                            cv1.put("user_id",user_id);
                            cv1.put("dept_id",dept_id);
                            helper.insert(TABLE_USER_DEPARTMENT, cv1);
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

                            TextView id = (TextView) view.findViewById(R.id.user_detail_id);
                            EditText name = (EditText) view.findViewById(R.id.user_detail_name);
                            EditText code = (EditText) view.findViewById(R.id.user_detail_code);
                            EditText pos = (EditText) view.findViewById(R.id.user_detail_position);
                            EditText phone = (EditText) view.findViewById(R.id.user_detail_phone);
                            RadioButton userSexFemale = (RadioButton) view.findViewById(R.id.user_detail_sex_female);


                            Long user_id = Long.parseLong(id.getText().toString());
                            ContentValues cv = new ContentValues();
                            cv.put("username", name.getText().toString());
                            cv.put("phone", phone.getText().toString());
                            cv.put("code_num", code.getText().toString());
                            cv.put("position", pos.getText().toString());
                            if (userSexFemale.isChecked()) {
                                cv.put("sex", SEX_FEMALE);
                            } else {
                                cv.put("sex", SEX_MALE);
                            }


                            if ("".equals(cv.get("username").toString())) {
                                Toast.makeText(getActivity(), "请填写员工姓名", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }
                            if ("".equals(cv.get("phone").toString())) {
                                Toast.makeText(getActivity(), "请填写员工电话", Toast.LENGTH_LONG).show();
                                detailDialog.show();
                                return;
                            }


                            connectDB();
                            boolean rs = helper.update(TABLE, cv, user_id);

                            Log.v("YYX", user_dept + "-------" + user_old_dept);

                            if (rs && user_dept != user_old_dept) {

                                int old_dept_id = 0;
                                try {
                                    Cursor cursor = helper.fetchDepartmentByName(user_old_dept);
                                    cursor.moveToFirst();
                                    old_dept_id = cursor.getInt(cursor.getColumnIndex("_id"));
                                    cursor.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                helper.db.execSQL("DELETE FROM " + TABLE_USER_DEPARTMENT + " WHERE user_id=? AND dept_id=?", new String[]{String.valueOf(user_id), String.valueOf(old_dept_id)});


                                int dept_id = 0;
                                try {
                                    Cursor cursor = helper.fetchDepartmentByName(user_dept);
                                    cursor.moveToFirst();
                                    dept_id = cursor.getInt(cursor.getColumnIndex("_id"));
                                    cursor.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                ContentValues cv1 = new ContentValues();
                                cv1.put("user_id", user_id);
                                cv1.put("dept_id", dept_id);
                                helper.insert(TABLE_USER_DEPARTMENT, cv1);
                            }

                            releaseDB();
                            updateList(1);

                            Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("删除", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            TextView v = (TextView) view.findViewById(R.id.user_detail_id);
                            Long user_id = Long.parseLong(v.getText().toString());
                            connectDB();
                            helper.delete(TABLE, user_id);
                            helper.db.rawQuery("DELETE FROM " + TABLE_USER_DEPARTMENT + " WHERE user_id=?", new String[]{String.valueOf(user_id)});
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
        count = helper.getCount(TABLE,"user_type=1");

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
