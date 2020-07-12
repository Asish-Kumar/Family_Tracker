package com.familytracker.pojos;

public class FriendCardPojo {

    private String friendUserName;
    private String friendDisplayName;

    public FriendCardPojo(String friendUserName, String friendDisplayName) {
        this.friendUserName = friendUserName;
        this.friendDisplayName = friendDisplayName;
    }

    public String getFriendUserName() {
        return friendUserName;
    }

    public String getFriendDisplayName() {
        return friendDisplayName;
    }

}
