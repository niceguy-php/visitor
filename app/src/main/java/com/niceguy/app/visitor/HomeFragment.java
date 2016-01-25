package com.niceguy.app.visitor;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by qiumeilin on 2016/1/9.
 */
public class HomeFragment extends Fragment implements View.OnClickListener{

    private LinearLayout settingFrg ;
    private LinearLayout historyFrg ;
    private LinearLayout leaveFrg ;
    private LinearLayout registerFrg ;
    private LinearLayout exitFrg ;

    private LinearLayout homeTab;
    private LinearLayout visitorHistoryTab;
    private LinearLayout visitorLeaveTab;
    private LinearLayout visitorRegisterTab;
    private LinearLayout existTab;

    private View view;

    @Override
    public void onClick(View v) {
        Activity a = getActivity();
        switch (v.getId()){
            case R.id.leave_fragment_click:
                a.findViewById(R.id.id_tab_visitor_leave).performClick();
                break;
            case R.id.setting_fragment_click:
                Intent intent = new Intent();
                intent.setClass(getActivity(),SettingActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.register_fragment_click:
                a.findViewById(R.id.id_tab_visitor_register).performClick();
                break;
            case R.id.history_fragment_click:
                a.findViewById(R.id.id_tab_visitor_history).performClick();
                break;
            case R.id.exit_fragment_click:
                getActivity().finish();
                break;
        }

    }

    interface HomeListener{

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_tab,container,false);

        initView();
        initEvent();
        return view;
    }

    private void initView(){
        settingFrg = (LinearLayout)view.findViewById(R.id.setting_fragment_click);
        historyFrg = (LinearLayout)view.findViewById(R.id.history_fragment_click);
        leaveFrg = (LinearLayout)view.findViewById(R.id.leave_fragment_click);
        registerFrg = (LinearLayout)view.findViewById(R.id.register_fragment_click);
        exitFrg = (LinearLayout)view.findViewById(R.id.exit_fragment_click);
    }

    private void initEvent(){
        settingFrg.setOnClickListener(this);
        historyFrg.setOnClickListener(this);
        leaveFrg.setOnClickListener(this);
        registerFrg.setOnClickListener(this);
        exitFrg.setOnClickListener(this);
    }


}
