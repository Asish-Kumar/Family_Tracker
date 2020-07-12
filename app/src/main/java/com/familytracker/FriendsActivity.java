package com.familytracker;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.familytracker.adapters.FriendsAdapter;
import com.familytracker.pojos.FriendCardPojo;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private static final String TAG = "FriendsActivity";
    private RecyclerView recyclerView;
    private List<FriendCardPojo> friendCardPojoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        major();

    }

    private void major() {

        init();

    }

    private void init() {

        Log.d(TAG, "Invoked init");

        initializeFriendsCardPojoList();

        //  Create new friendsAdapter
        FriendsAdapter friendsAdapter = new FriendsAdapter(friendCardPojoList);

        //  Initialize RecyclerView
        recyclerView = findViewById(R.id.idFriendsRecyclerView_AF);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(friendsAdapter);

        Log.d(TAG, "Leaving init");

    }

    private void initializeFriendsCardPojoList() {

        friendCardPojoList = new ArrayList<>();

        //  Create Dummy Data
        friendCardPojoList.add(new FriendCardPojo("username1", "display1"));
        friendCardPojoList.add(new FriendCardPojo("username2", "display2"));
        friendCardPojoList.add(new FriendCardPojo("username3", "display3"));
        friendCardPojoList.add(new FriendCardPojo("username4", "display4"));
        friendCardPojoList.add(new FriendCardPojo("username5", "display5"));
        friendCardPojoList.add(new FriendCardPojo("username6", "display6"));
        friendCardPojoList.add(new FriendCardPojo("username7", "display7"));
        friendCardPojoList.add(new FriendCardPojo("username8", "display8"));
        friendCardPojoList.add(new FriendCardPojo("username9", "display9"));
        friendCardPojoList.add(new FriendCardPojo("username10", "display10"));
        friendCardPojoList.add(new FriendCardPojo("username11", "display11"));
        friendCardPojoList.add(new FriendCardPojo("username12", "display12"));
        friendCardPojoList.add(new FriendCardPojo("username13", "display13"));

    }


}