package com.familytracker.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.familytracker.R;
import com.familytracker.pojos.FriendCardPojo;

import java.util.List;

public class FriendsAdapter extends Adapter<FriendsAdapter.ViewHolder> {

    private static final String TAG = "FriendsAdapter";
    private List<FriendCardPojo> friendCardPojoList;

    public FriendsAdapter(List<FriendCardPojo> friendCardPojoList) {
        this.friendCardPojoList = friendCardPojoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG, "Invoked onCreateViewHolder");

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.friend_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG, "Invoked onBindViewHolder Position: " + position);

        FriendCardPojo friendCardPojo = friendCardPojoList.get(position);
        holder.friendUserNameTV.setText(friendCardPojo.getFriendUserName());
        holder.friendDisplayNameTV.setText(friendCardPojo.getFriendDisplayName());

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + friendCardPojoList.size());
        return friendCardPojoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView friendUserNameTV;
        TextView friendDisplayNameTV;
        ImageView friendProfileIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "Invoked ViewHolder Constructor");

            this.friendUserNameTV = itemView.findViewById(R.id.idFriendUserNameTV_PC);
            this.friendDisplayNameTV = itemView.findViewById(R.id.idFriendDisplayNameTV_PC);
            this.friendProfileIV = itemView.findViewById(R.id.idFriendProfileIV_PC);

        }
    }

}
