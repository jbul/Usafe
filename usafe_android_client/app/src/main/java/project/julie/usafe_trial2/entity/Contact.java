package project.julie.usafe_trial2.entity;

import project.julie.usafe_trial2.entity.User;

public class Contact extends User {

    private boolean isUser = false;
    private boolean isFriend = false;

    public Contact(String firstName, String lastName, String password, String email, String phoneNo) {
        super(firstName, lastName, password, email, phoneNo);
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
