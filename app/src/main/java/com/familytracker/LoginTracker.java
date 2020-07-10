package com.familytracker;

import java.io.Serializable;

public interface LoginTracker extends Serializable {
    public void loginComplete(LoginActivity loginActivity);
}
