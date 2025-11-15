package boundary;

import control.InternshipManager;
import control.UserManager;
import entities.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class StudentUI {
    private final UserManager userManager;
    private final InternshipManager internshipManager;
    private final Scanner sc;

    private InternshipStatus lastStatusFilter = null;
    private InternshipLevel lastLevelFilter = null;
    private String lastMajorFilter = null;
    private LocalDate lastClosingDateFilter = null;

    public StudentUI(UserManager um, InternshipManager im, Scanner sc) {
        this.userManager = um;
        this.internshipManager = im;
        this.sc = sc;
    }

    public void studentMenu(Student student) {
        boolean logout = false;
        while (!logout) {
            try {
                System.out.println("\n-- Student Menu --");
                System.out.println("1. View Internship Opportunities (with Filters)");
                System.out.println("2. View My Applications");
                System.out.println("3. Apply for Internship");
                System.out.println("4. Withdraw Application");
                System.out.println("5. Accept Offer");
                System.out.println("6. Change Password");
                System.out.println("7. Logout");
                System.out.print("Choice: ");
                switch (sc.nextLine().trim()) {
                    case "1" -> viewFilteredInternships(student);
                    case "2" -> listMyApplications(student);
                    case "3" -> applyToInternship(student);
                    case "4" -> withdrawApplication(student);
                    case "5" -> acceptOffer(student);
                    case "6" -> changePassword(student);
                    case "7" -> logout = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void viewFilteredInternships(Student s) {
        try {
            System.out.println("-- Internship Filters --");
            System.out.println("Press Enter to keep previous filters");

            System.out.print("Status (Pending/Approved/Rejected/Filled): ");
            String st = sc.nextLine().trim();
            if (!st.isEmpty()) {
                try { lastStatusFilter = InternshipStatus.valueOf(st.toUpperCase()); }
                catch (Exception e) { lastStatusFilter = null; }
            }

            System.out.print("Preferred Major: ");
            String mj = sc.nextLine().trim();
            if (!mj.isEmpty()) lastMajorFilter = mj;

            System.out.print("Internship Level (Basic/Intermediate/Advanced): ");
            String lv = sc.nextLine().trim();
            if (!lv.isEmpty()) {
                try { lastLevelFilter = InternshipLevel.valueOf(lv.toUpperCase()); }
                catch (Exception e) { lastLevelFilter = null; }
            }

            System.out.print("Closing Date before (YYYY-MM-DD): ");
            String dateStr = sc.nextLine().trim();
            if (!dateStr.isEmpty()) {
                try { lastClosingDateFilter = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                catch (DateTimeParseException e) { lastClosingDateFilter = null; }
            }

            List<Internship> list = new ArrayList<>(internshipManager.getAllInternships());
            list.removeIf(it ->
                    it.getStatus() != InternshipStatus.APPROVED ||
                            !it.isVisible() ||
                            !it.getPreferredMajor().equalsIgnoreCase(s.getMajor()) ||
                            (s.getYear() <= 2 && it.getLevel() != InternshipLevel.BASIC) ||
                            (lastStatusFilter != null && it.getStatus() != lastStatusFilter) ||
                            (lastMajorFilter != null && !it.getPreferredMajor().equalsIgnoreCase(lastMajorFilter)) ||
                            (lastLevelFilter != null && it.getLevel() != lastLevelFilter) ||
                            (lastClosingDateFilter != null && it.getClosingDate().isAfter(lastClosingDateFilter))
            );

            list.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));

            if (list.isEmpty()) {
                System.out.println("No internships match the filters.");
                return;
            }

            for (Internship it : list) {
                System.out.println(it.getTitle() + " | " + it.getCompanyName() +
                        " | Level: " + it.getLevel() +
                        " | Major: " + it.getPreferredMajor() +
                        " | Closes: " + it.getClosingDate());
            }
        } catch (Exception e) {
            System.out.println("Error while filtering internships: " + e.getMessage());
        }
    }

    private void listMyApplications(Student s) {
        try {
            System.out.println("-- My Applications --");
            boolean found = false;
            for (Internship it : internshipManager.getAllInternships()) {
                ApplicationStatus st = it.getApplications().get(s.getId());
                if (st != null) {
                    found = true;
                    System.out.println(it.getTitle() + " | " + it.getCompanyName() + " | Status: " + st);
                }
            }
            if (!found) System.out.println("No applications found.");
        } catch (Exception e) {
            System.out.println("Error while listing applications: " + e.getMessage());
        }
    }

    private void applyToInternship(Student s) {
        try {
            List<Internship> openList = new ArrayList<>();
            for (Internship it : internshipManager.getAllInternships()) {
                if (it.isOpenForApplication(s)) openList.add(it);
            }
            openList.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));
            if (openList.isEmpty()) {
                System.out.println("No internships available for application.");
                return;
            }
            for (int i = 0; i < openList.size(); i++) {
                System.out.println((i + 1) + ". " + openList.get(i).getTitle());
            }
            System.out.print("Choose internship to apply: ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch < 1 || ch > openList.size()) return;
            Internship selected = openList.get(ch - 1);
            if (internshipManager.applyForInternship(s, selected))
                System.out.println("Application submitted.");
            else
                System.out.println("Application failed (limit reached or already applied).");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error while applying: " + e.getMessage());
        }
    }

    private void withdrawApplication(Student s) {
        try {
            List<Internship> withdrawable = new ArrayList<>();
            for (Internship it : internshipManager.getAllInternships()) {
                ApplicationStatus st = it.getApplications().get(s.getId());
                if (st == ApplicationStatus.APPLIED || st == ApplicationStatus.OFFERED || st == ApplicationStatus.CONFIRMED)
                    withdrawable.add(it);
            }
            if (withdrawable.isEmpty()) {
                System.out.println("No applications available to withdraw.");
                return;
            }
            for (int i = 0; i < withdrawable.size(); i++)
                System.out.println((i + 1) + ". " + withdrawable.get(i).getTitle());
            System.out.print("Choose internship to withdraw: ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch < 1 || ch > withdrawable.size()) return;
            Internship selected = withdrawable.get(ch - 1);
            WithdrawalRequest req = internshipManager.requestWithdrawal(s, selected);
            if (req == null) System.out.println("Withdrawal request failed or already pending.");
            else System.out.println("Withdrawal request submitted. Awaiting staff approval.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error while withdrawing: " + e.getMessage());
        }
    }

    private void acceptOffer(Student s) {
        try {
            List<Internship> offerList = new ArrayList<>();
            for (Internship it : internshipManager.getAllInternships()) {
                ApplicationStatus st = it.getApplications().get(s.getId());
                if (st == ApplicationStatus.OFFERED) offerList.add(it);
            }
            if (offerList.isEmpty()) {
                System.out.println("No offers available to accept.");
                return;
            }
            System.out.println("-- Offers Available --");
            for (int i = 0; i < offerList.size(); i++) {
                Internship it = offerList.get(i);
                System.out.println((i + 1) + ". " + it.getTitle() +
                        " | Company: " + it.getCompanyName() +
                        " | Level: " + it.getLevel());
            }
            System.out.print("Choose offer to accept (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch < 1 || ch > offerList.size()) {
                System.out.println("Cancelled.");
                return;
            }
            Internship chosen = offerList.get(ch - 1);
            if (internshipManager.acceptOffer(s, chosen)) {
                System.out.println("Offer accepted successfully for " + chosen.getTitle());
            } else {
                System.out.println("Failed to accept offer.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        } catch (Exception e) {
            System.out.println("Error while accepting offer: " + e.getMessage());
        }
    }
    private void changePassword(User u) {
        try {
            System.out.print("Current password: ");
            String oldP = sc.nextLine();
            System.out.print("New password: ");
            String newP = sc.nextLine();
            System.out.println(u.changePassword(oldP, newP) ? "Password updated." : "Failed to change password.");
        } catch (Exception e) {
            System.out.println("Error while changing password: " + e.getMessage());
        }
    }
}
