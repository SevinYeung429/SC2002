package entities;
import control.InternshipManager;
import control.UserManager;

import java.io.Serializable;
import java.util.Scanner;

public abstract class User implements Serializable {
    private String id;
    private String name;
    protected String password;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (!this.password.equals(oldPassword)) {
            return false;
        }
        this.password = newPassword;
        return true;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public String toString() {
        return "User [ID=" + id + ", Name=" + name + "]";
    }

    public abstract void showMenu(UserManager um, InternshipManager im, Scanner sc);
}
