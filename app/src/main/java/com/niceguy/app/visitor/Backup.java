package com.niceguy.app.visitor;


import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class Backup extends Fragment implements View.OnClickListener{

    private ListView listView = null;

    public static String BACKUP_DIR = Environment.getExternalStorageDirectory()+"/sicheng/backup/";
    private Button startBackup = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.backup, container, false);

        initViews(view);
        listBackupedDBFiles();
        initEvents();

        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);
                final String filename = (String) map.get("file_name");
                AlertDialog detailDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("提示").setMessage("请选择相应的操作：" +
                                "\r\n删除备份文件会导致被删除的历史备份无法恢复，" +
                                "\r\n恢复该备份到系统会导致该备份会覆盖当前系统的数据")
                        .setPositiveButton("删除此备份文件", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteBackupedDBFile(filename);
                                Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("恢复该备份到系统", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restoreDBFile(filename);
                            }
                        }).create();
                detailDialog.show();

            }
        });


        return view;
    }

    private void initViews(View view){
        startBackup = (Button) view.findViewById(R.id.start_backup);
        listView = (ListView) view.findViewById(R.id.backup_listview);

    }

    private void initEvents(){
        startBackup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.start_backup:
                backupDBFile();
                Toast.makeText(getActivity(),"备份成功",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void backupDBFile(){

        String dbFilePath = getActivity().getDatabasePath("visitor.db").getAbsolutePath();
        File file = new File(dbFilePath);
        File dir = new File(BACKUP_DIR);
        Log.v("YYX",dbFilePath);
        Log.v("YYX",BACKUP_DIR);
        try {
            if(!dir.exists()){
                dir.mkdirs();
            }
            if(dir.exists() && file.exists()){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String backupname = sdf.format(new Date())+".db";
                FileOutputStream fos = new FileOutputStream(BACKUP_DIR+backupname);
                FileUtils.copyFile(file, fos);
                listBackupedDBFiles();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteBackupedDBFile(String filename){
        File file = new File(BACKUP_DIR+filename);
        if(file.exists()){
            file.delete();
            listBackupedDBFiles();
        }
    }

    public void restoreDBFile(String filename){

        String dbFilePath = getActivity().getDatabasePath("visitor.db").getPath();
        File file = new File(BACKUP_DIR+filename);
        File dbFile = new File(dbFilePath);
        try {
            if(dbFile.exists() && file.exists()){
                FileOutputStream fos = new FileOutputStream(dbFile);
                long rs = FileUtils.copyFile(file, fos);
                Log.v("YYX",rs+"");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listBackupedDBFiles(){
        Log.v("YYX","in listBackupedDBFiles");
        ArrayList<HashMap<String,Object>> objs = new ArrayList<HashMap<String, Object>>();
        File backup_dir = new File(BACKUP_DIR);
        if(backup_dir.exists()){
            File[] files=backup_dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified()-f2.lastModified();
                    if(diff<0){
                        return 1;
                    }else if(diff==0){
                        return 0;
                    }else{
                        return -1;
                    }
                }
            });
            int i = 1;
            for (File f : files) {
                if(f.isFile()){
                    HashMap hashMap = new HashMap();
                    hashMap.put("file_id",i+"");
                    hashMap.put("file_name",f.getName());
                    objs.add(hashMap);
                }
                i++;
            }
        }

        if(objs != null && objs.size()>0 ){
            SimpleAdapter simple = new SimpleAdapter(getActivity(),objs,
                    R.layout.backup_file_item,new String[]{"file_id","file_name"},new int[]{R.id.file_id,R.id.file_name});
            listView.setAdapter(simple);
        }
    }


}
