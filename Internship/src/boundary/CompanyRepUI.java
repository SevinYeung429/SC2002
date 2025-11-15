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

public class CompanyRepUI {
    private final UserManager userManager;
    private final InternshipManager internshipManager;
    private final Scanner sc;

    private InternshipStatus lastStatusFilter = null;
    private InternshipLevel lastLevelFilter = null;
    private String lastMajorFilter = null;
    private LocalDate lastClosingDateFilter = null;

    public CompanyRepUI(UserManager um, InternshipManager im, Scanner sc) {
        this.userManager = um;
        this.internshipManager = im;
        this.sc = sc;
    }

    public void registerCompanyRep() {
        System.out.println("-- Company Representative Registration --");
        try {
            System.out.print("Email (used as ID): ");
            String email = sc.nextLine().trim();
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Company Name: ");
            String company = sc.nextLine().trim();
            System.out.print("Department: ");
            String dept = sc.nextLine().trim();
            System.out.print("Position: ");
            String position = sc.nextLine().trim();

            CompanyRepresentative rep = userManager.registerCompanyRep(email, name, company, dept, position);
            if (rep == null)
                System.out.println("Registration failed. Account may already exist.");
            else
                System.out.println("Account created. Awaiting Career Center approval.");
        } catch (Exception e) {
            System.out.println("Error: invalid input during registration. " + e.getMessage());
        }
    }

    public void changePassword(CompanyRepresentative rep) {
        System.out.println("-- Change Password --");
        try {
            System.out.print("Enter current password: ");
            String oldPass = sc.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPass = sc.nextLine().trim();
            boolean success = rep.changePassword(oldPass, newPass);
            System.out.println(success ? "Password changed successfully." : "Password change failed. Current password incorrect.");
        } catch (Exception e) {
            System.out.println("Error while changing password: " + e.getMessage());
        }
    }

    public void companyRepMenu(CompanyRepresentative rep) {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n-- Company Representative Menu --");
            System.out.println("1. Create Internship Posting");
            System.out.println("2. Manage My Postings");
            System.out.println("3. View All Internships (with Filters)");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            System.out.print("Choice: ");
            try {
                switch (sc.nextLine().trim()) {
                    case "1" -> createInternshipPosting(rep);
                    case "2" -> managePostingsMenu(rep);
                    case "3" -> viewFilteredInternships();
                    case "4" -> changePassword(rep);
                    case "5" -> logout = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void createInternshipPosting(CompanyRepresentative rep) {
        try {
            long active = rep.getInternshipsPosted().stream()
                    .filter(it -> it.getStatus() != InternshipStatus.FILLED && it.getStatus() != InternshipStatus.REJECTED)
                    .count();
            if (active >= 5) {
                System.out.println("You already have 5 active postings.");
                return;
            }
            System.out.println("-- New Internship Posting --");
            System.out.print("Title: ");
            String title = sc.nextLine();
            System.out.print("Description: ");
            String desc = sc.nextLine();
            System.out.print("Preferred Major: ");
            String major = sc.nextLine();
            System.out.print("Level (Basic/Intermediate/Advanced): ");
            InternshipLevel level = InternshipLevel.valueOf(sc.nextLine().trim().toUpperCase());

            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            System.out.print("Opening Date (YYYY-MM-DD): ");
            LocalDate open = LocalDate.parse(sc.nextLine(), df);
            System.out.print("Closing Date (YYYY-MM-DD): ");
            LocalDate close = LocalDate.parse(sc.nextLine(), df);

            System.out.print("Number of Slots (1–10): ");
            int slots = Integer.parseInt(sc.nextLine());

            Internship internship = internshipManager.createInternship(rep, title, desc, level, major, open, close, slots);
            if (internship != null)
                System.out.println("Posting created (pending Career Center approval).");
            else
                System.out.println("Failed to create posting.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: please check level or slot range.");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
        } catch (Exception e) {
            System.out.println("Error while creating internship: " + e.getMessage());
        }
    }

    private void managePostingsMenu(CompanyRepresentative rep) {
        try {
            List<Internship> list = rep.getInternshipsPosted();
            if (list.isEmpty()) {
                System.out.println("No postings yet.");
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                Internship it = list.get(i);
                System.out.println((i + 1) + ". " + it.getTitle() +
                        " | Status: " + it.getStatus() +
                        " | Visible: " + (it.isVisible() ? "ON" : "OFF") +
                        " | Applicants: " + it.getApplications().size());
            }
            System.out.print("Choose posting (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch > 0 && ch <= list.size()) manageSinglePosting(list.get(ch - 1));
        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void manageSinglePosting(Internship internship) {
        boolean back = false;
        while (!back) {
            try {
                System.out.println("\n-- Manage Internship: " + internship.getTitle() + " --");
                System.out.println("1. View Applicants");
                System.out.println("2. Approve/Reject Applicant");
                System.out.println("3. Toggle Visibility (" + (internship.isVisible() ? "ON" : "OFF") + ")");
                System.out.println("4. Edit Posting");
                System.out.println("5. Delete Posting");
                System.out.println("6. Back");
                System.out.print("Choice: ");
                switch (sc.nextLine().trim()) {
                    case "1" -> viewApplicants(internship);
                    case "2" -> reviewApplicant(internship);
                    case "3" -> {
                        internshipManager.toggleVisibility(internship);
                        System.out.println("Visibility is now " + (internship.isVisible() ? "ON" : "OFF"));
                    }
                    case "4" -> editPosting(internship);
                    case "5" -> {
                        if (deletePosting(internship)) {
                            back = true;
                        }
                    }
                    case "6" -> back = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void editPosting(Internship internship) {
        if (internship.getStatus() != InternshipStatus.PENDING) {
            System.out.println("Error: Cannot edit an internship that is already " + internship.getStatus() + ".");
            System.out.println("Please contact the Career Center staff to request changes.");
            return;
        }

        try {
            System.out.println("-- Editing: " + internship.getTitle() + " (Status: PENDING) --");
            System.out.println("Press Enter to keep the current value.");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            System.out.print("Title [" + internship.getTitle() + "]: ");
            String newTitle = sc.nextLine().trim();
            if (!newTitle.isEmpty()) internship.setTitle(newTitle);

            System.out.print("Description [" + internship.getDescription() + "]: ");
            String newDesc = sc.nextLine().trim();
            if (!newDesc.isEmpty()) internship.setDescription(newDesc);

            System.out.print("Preferred Major [" + internship.getPreferredMajor() + "]: ");
            String newMajor = sc.nextLine().trim();
            if (!newMajor.isEmpty()) internship.setPrefer_major(newMajor);

            System.out.print("Level (Basic/Intermediate/Advanced) [" + internship.getLevel() + "]: ");
            String newLevelStr = sc.nextLine().trim().toUpperCase();
            if (!newLevelStr.isEmpty()) {
                try {
                    internship.setLevel(InternshipLevel.valueOf(newLevelStr));
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid level. Level was not changed.");
                }
            }

            System.out.print("Opening Date (YYYY-MM-DD) [" + internship.getOpeningDate() + "]: ");
            String newOpenDate = sc.nextLine().trim();
            if (!newOpenDate.isEmpty()) {
                internship.setOpeningDate(LocalDate.parse(newOpenDate, df));
            }

            System.out.print("Closing Date (YYYY-MM-DD) [" + internship.getClosingDate() + "]: ");
            String newCloseDate = sc.nextLine().trim();
            if (!newCloseDate.isEmpty()) {
                internship.setClosingDate(LocalDate.parse(newCloseDate, df));
            }

            System.out.print("Slots (1–10) [" + internship.getSlots() + "]: ");
            String newSlotsStr = sc.nextLine().trim();
            if (!newSlotsStr.isEmpty()) {
                int newSlots = Integer.parseInt(newSlotsStr);
                if (newSlots >= 1 && newSlots <= 10) {
                    internship.setSlots(newSlots);
                } else {
                    System.out.println("Invalid slot number (1-10). Slots were not changed.");
                }
            }

            internshipManager.updateInternship(internship);
            System.out.println("Internship updated successfully. It is still PENDING approval.");

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for slots.");
        } catch (Exception e) {
            System.out.println("Error while editing internship: " + e.getMessage());
        }
    }

    private boolean deletePosting(Internship internship) {
        if (!internship.getApplications().isEmpty()) {
            System.out.println("Error: Cannot delete posting. This internship already has applicants.");
            System.out.println("Please contact the Career Center staff if you need to remove this posting.");
            return false;
        }

        if (internship.getStatus() == InternshipStatus.APPROVED) {
            System.out.println("Warning: This internship is already approved and visible.");
        }

        try {
            System.out.println("You selected: " + internship.getTitle());
            System.out.print("Are you sure you want to PERMANENTLY delete this posting? (y/n): ");

            if (sc.nextLine().trim().toLowerCase().startsWith("y")) {
                boolean success = internshipManager.deleteInternship(internship);

                if (success) {
                    System.out.println("Internship posting deleted successfully.");
                    return true;
                } else {
                    System.out.println("Failed to delete posting from the database.");
                    return false;
                }
            } else {
                System.out.println("Deletion cancelled.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error while deleting internship: " + e.getMessage());
            return false;
        }
    }


    private void viewApplicants(Internship internship) {
        try {
            System.out.println("-- Applicants for " + internship.getTitle() + " --");
            if (internship.getApplications().isEmpty()) {
                System.out.println("No applicants yet.");
                return;
            }

            int count = 1;
            for (var entry : internship.getApplications().entrySet()) {
                String sid = entry.getKey();
                ApplicationStatus status = entry.getValue();

                User u = userManager.getUserById(sid);

                if (u instanceof Student s) {
                    System.out.println(count++ + ". "
                            + s.getId() + ", "
                            + s.getName() + ", "
                            + s.getMajor() + ", "
                            + s.getYear() + ", "
                            + s.getEmail()
                            + " - " + status);
                } else {
                    System.out.println(count++ + ". " + sid + " - " + status);
                }
            }
        } catch (Exception e) {
            System.out.println("Error while viewing applicants: " + e.getMessage());
        }
    }

    private void reviewApplicant(Internship internship) {
        try {
            List<String> pending = new ArrayList<>();
            for (var e : internship.getApplications().entrySet()) {
                if (e.getValue() == ApplicationStatus.APPLIED) pending.add(e.getKey());
            }
            if (pending.isEmpty()) {
                System.out.println("No pending applications.");
                return;
            }
            for (int i = 0; i < pending.size(); i++) {
                String sid = pending.get(i);
                User u = userManager.getUserById(sid);
                String name = (u instanceof Student s) ? s.getName() : sid;
                System.out.println((i + 1) + ". " + name + " (" + sid + ")");
            }
            System.out.print("Choose applicant (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch <= 0 || ch > pending.size()) return;

            String studentId = pending.get(ch - 1);
            Student student = (Student) userManager.getUserById(studentId);
            System.out.print("Approve this applicant? (y/n): ");
            boolean approve = sc.nextLine().trim().toLowerCase().startsWith("y");
            boolean result = internshipManager.reviewApplication(internship, student, approve);

            if (result)
                System.out.println(approve ? "Offer sent to " + student.getName() + "." : "Application rejected.");
            else
                System.out.println("Operation failed (slots full or invalid state).");
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (Exception e) {
            System.out.println("Error while reviewing applicant: " + e.getMessage());
        }
    }

    private void viewFilteredInternships() {
        try {
            System.out.println("-- Internship Filters --");
            System.out.println("Keep previous filter by pressing Enter");

            System.out.print("Status (Pending/Approved/Rejected/Filled): ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                try { lastStatusFilter = InternshipStatus.valueOf(s.toUpperCase()); }
                catch (Exception e) { lastStatusFilter = null; }
            }

            System.out.print("Preferred Major: ");
            String major = sc.nextLine().trim();
            if (!major.isEmpty()) lastMajorFilter = major;

            System.out.print("Internship Level (Basic/Intermediate/Advanced): ");
            String lvl = sc.nextLine().trim();
            if (!lvl.isEmpty()) {
                try { lastLevelFilter = InternshipLevel.valueOf(lvl.toUpperCase()); }
                catch (Exception e) { lastLevelFilter = null; }
            }

            System.out.print("Closing Date before (YYYY-MM-DD): ");
            String dateStr = sc.nextLine().trim();
            if (!dateStr.isEmpty()) {
                try { lastClosingDateFilter = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
                catch (DateTimeParseException e) { lastClosingDateFilter = null; }
            }

            List<Internship> list = new ArrayList<>(internshipManager.getAllInternships());
            list.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));

            list.removeIf(it ->
                    (lastStatusFilter != null && it.getStatus() != lastStatusFilter) ||
                            (lastMajorFilter != null && !it.getPreferredMajor().equalsIgnoreCase(lastMajorFilter)) ||
                            (lastLevelFilter != null && it.getLevel() != lastLevelFilter) ||
                            (lastClosingDateFilter != null && it.getClosingDate().isAfter(lastClosingDateFilter))
            );

            if (list.isEmpty()) {
                System.out.println("No internships match the filters.");
                return;
            }

            for (Internship it : list) {
                System.out.println(it.getTitle() + " | " + it.getCompanyName() +
                        " | Level: " + it.getLevel() +
                        " | Major: " + it.getPreferredMajor() +
                        " | Status: " + it.getStatus() +
                        " | Closes: " + it.getClosingDate());
            }
        } catch (Exception e) {
            System.out.println("Error while filtering internships: " + e.getMessage());
        }
    }
}