package entities;

import boundary.StudentUI;
import control.InternshipManager;
import control.UserManager;

import java.util.Scanner;

public class Student extends User {
    private int year;
    private String major;
    private String email;
    private int acceptedInternshipId;

    public Student(String id, String name, String major, int year, String email) {
        super(id, name, "password");
        this.year = year;
        this.major = major;
        this.email = email;
        this.acceptedInternshipId = -1;
    }

    public int getYear() {
        return year;
    }

    public String getMajor() {
        return major;
    }

    public String getEmail() {
        return email;
    }

    public int getAcceptedInternshipId() {
        return acceptedInternshipId;
    }

    public void setAcceptedInternshipId(int internshipId) {
        this.acceptedInternshipId = internshipId;
    }

    @Override
    public String toString() {
        return "Student [ID=" + getId() + ", Name=" + getName() +
                ", Year=" + year + ", Major=" + major +
                ", Email=" + email + ", AcceptedInternshipID=" + acceptedInternshipId + "]";
    }
    @Override
    public void showMenu(UserManager um, InternshipManager im, Scanner sc) {
        new StudentUI(um, im, sc).studentMenu(this);
    }

}
