package boundary;

import control.InternshipManager;
import control.UserManager;
import data.UserDataHandler;
import entities.CareerCenterStaff;
import entities.CompanyRepresentative;
import entities.Student;
import entities.User;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Current working dir: " + System.getProperty("user.dir"));

        UserDataHandler dataHandler = new UserDataHandler();
        UserManager userManager = new UserManager(dataHandler);
        InternshipManager internshipManager = new InternshipManager();
        Scanner sc = new Scanner(System.in);

        userManager.loadAllUsers();

        System.out.println("=== Internship Management System ===");

        boolean exit = false;
        while (!exit) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Login");
            System.out.println("2. Register as Company Representative");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> login(userManager, internshipManager, sc);
                case "2" -> new CompanyRepUI(userManager, internshipManager, sc).registerCompanyRep();
                case "3" -> exit = true;
                default -> System.out.println("Invalid choice.");
            }
        }

        dataHandler.saveUsersToCSV(userManager);
        System.out.println("System exited. Goodbye!");
    }

    private static void login(UserManager userManager, InternshipManager internshipManager, Scanner sc) {
        System.out.print("Enter User ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String pw = sc.nextLine().trim();

        User user = userManager.login(id, pw);
        if (user == null) {
            System.out.println("Login failed (invalid credentials or not approved).");
            return;
        }

        System.out.println("Welcome, " + user.getName() + "!");
        if (user instanceof Student s)
            new StudentUI(userManager, internshipManager, sc).studentMenu(s);
        else if (user instanceof CompanyRepresentative rep)
            new CompanyRepUI(userManager, internshipManager, sc).companyRepMenu(rep);
        else if (user instanceof CareerCenterStaff st)
            new StaffUI(userManager, internshipManager, sc).staffMenu(st);
    }
}
