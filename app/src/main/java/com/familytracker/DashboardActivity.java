package com.familytracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private ImageView newGroupIV;
    private LinearLayout linearLayout;
    private LayoutInflater groupCardInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        init();

        newGroupIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        for(int i = 1; i<10;i++) {
            View v = groupCardInflater.inflate(R.layout.group_card, null);
            TextView groupNameTV = v.findViewById(R.id.idGroupNameTV_GC);
            TextView memberCountTV = v.findViewById(R.id.idMemberCountTV_GC);
            groupNameTV.setText("Group Name " + i);
            memberCountTV.setText("Member Count: "+i);

            linearLayout.addView(v);
        }
    }

    private void init(){
        newGroupIV = findViewById(R.id.idNewGroupNavBtn_DA);
        linearLayout = findViewById(R.id.idMainLinearLayout_AD);
        groupCardInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

}