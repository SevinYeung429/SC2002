package entities;

import boundary.StaffUI;
import control.InternshipManager;
import control.UserManager;

import java.util.Scanner;

public class CareerCenterStaff extends User {
    private String role;
    private String department;
    private String email;

    public CareerCenterStaff(String id, String name, String role, String department, String email) {
        super(id, name, "password");
        this.role = role;
        this.department = department;
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "CareerCenterStaff [ID=" + getId() + ", Name=" + getName() +
                ", Role=" + role + ", Department=" + department + ", Email=" + email + "]";
    }
    @Override
    public void showMenu(UserManager um, InternshipManager im, Scanner sc) {
        new StaffUI(um, im, sc).staffMenu(this);
    }

}
