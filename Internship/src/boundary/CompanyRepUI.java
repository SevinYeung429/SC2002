package boundary;

import control.IInternshipService;
import control.IUserService;
import entities.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyRepUI implements ICompanyRepUI {

    private final IUserService userService;
    private final IInternshipService internshipService;
    private final Scanner sc;

    private InternshipStatus lastStatusFilter = null;
    private InternshipLevel lastLevelFilter = null;
    private String lastMajorFilter = null;
    private LocalDate lastClosingDateFilter = null;

    public CompanyRepUI(IUserService userService, IInternshipService internshipService, Scanner sc) {
        this.userService = userService;
        this.internshipService = internshipService;
        this.sc = sc;
    }

    @Override
    public void repMenu(CompanyRepresentative rep) {
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

    public void registerCompanyRep() {
        try {
            System.out.println("-- Company Representative Registration --");
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

            CompanyRepresentative rep = userService.registerCompanyRep(email, name, company, dept, position);
            System.out.println(rep == null ? "Registration failed. Account may already exist."
                    : "Account created. Awaiting Career Center approval.");

        } catch (Exception e) {
            System.out.println("Error: invalid input during registration. " + e.getMessage());
        }
    }

    public void changePassword(CompanyRepresentative rep) {
        try {
            System.out.println("-- Change Password --");
            System.out.print("Enter current password: ");
            String oldPass = sc.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPass = sc.nextLine().trim();
            System.out.println(rep.changePassword(oldPass, newPass)
                    ? "Password changed successfully."
                    : "Password change failed. Current password incorrect.");
        } catch (Exception e) {
            System.out.println("Error while changing password: " + e.getMessage());
        }
    }

    private void createInternshipPosting(CompanyRepresentative rep) {
        try {
            long active = rep.getInternshipsPosted().stream()
                    .filter(it -> it.getStatus() != InternshipStatus.FILLED
                            && it.getStatus() != InternshipStatus.REJECTED)
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

            System.out.print("Slots (1–10): ");
            int slots = Integer.parseInt(sc.nextLine());

            Internship internship =
                    internshipService.createInternship(rep, title, desc, level, major, open, close, slots);

            System.out.println(internship != null
                    ? "Posting created (pending Career Center approval)."
                    : "Failed to create posting.");

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
                System.out.println((i + 1) + ". " + it.getTitle()
                        + " | Status: " + it.getStatus()
                        + " | Visible: " + (it.isVisible() ? "ON" : "OFF")
                        + " | Applicants: " + it.getApplications().size());
            }

            System.out.print("Choose posting (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch > 0 && ch <= list.size()) manageSinglePosting(list.get(ch - 1));

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
                        internshipService.toggleVisibility(internship);
                        System.out.println("Visibility is now " + (internship.isVisible() ? "ON" : "OFF"));
                    }
                    case "4" -> editPosting(internship);
                    case "5" -> {
                        if (deletePosting(internship)) back = true;
                    }
                    case "6" -> back = true;
                    default -> System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
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
                User u = userService.getUserById(sid);

                if (u instanceof Student s) {
                    System.out.println(count++ + ". " + s.getId() + ", " + s.getName() + ", "
                            + s.getMajor() + ", " + s.getYear() + ", " + s.getEmail()
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
            List<String> pending = internship.getApplications().entrySet().stream()
                    .filter(e -> e.getValue() == ApplicationStatus.APPLIED)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (pending.isEmpty()) {
                System.out.println("No pending applications.");
                return;
            }

            for (int i = 0; i < pending.size(); i++) {
                String sid = pending.get(i);
                User u = userService.getUserById(sid);
                String name = (u instanceof Student s) ? s.getName() : sid;
                System.out.println((i + 1) + ". " + name + " (" + sid + ")");
            }

            System.out.print("Choose applicant (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch <= 0 || ch > pending.size()) return;

            String studentId = pending.get(ch - 1);
            Student student = (Student) userService.getUserById(studentId);

            System.out.print("Approve this applicant? (y/n): ");
            boolean approve = sc.nextLine().trim().toLowerCase().startsWith("y");

            boolean result = internshipService.reviewApplication(internship, student, approve);

            System.out.println(result
                    ? (approve ? "Offer sent to " + student.getName() + "." : "Application rejected.")
                    : "Operation failed (slots full or invalid state).");

        } catch (Exception e) {
            System.out.println("Error while reviewing applicant: " + e.getMessage());
        }
    }

    private void editPosting(Internship internship) {
        if (internship.getStatus() != InternshipStatus.PENDING) {
            System.out.println("Error: Cannot edit an internship that is already " + internship.getStatus() + ".");
            return;
        }

        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            System.out.println("-- Editing: " + internship.getTitle() + " (Status: PENDING) --");
            System.out.println("Press Enter to keep the current value.");

            System.out.print("Title [" + internship.getTitle() + "]: ");
            String t = sc.nextLine().trim();
            if (!t.isEmpty()) internship.setTitle(t);

            System.out.print("Description [" + internship.getDescription() + "]: ");
            String d = sc.nextLine().trim();
            if (!d.isEmpty()) internship.setDescription(d);

            System.out.print("Preferred Major [" + internship.getPreferredMajor() + "]: ");
            String m = sc.nextLine().trim();
            if (!m.isEmpty()) internship.setPrefer_major(m);

            System.out.print("Level (Basic/Intermediate/Advanced) [" + internship.getLevel() + "]: ");
            String lv = sc.nextLine().trim().toUpperCase();
            if (!lv.isEmpty()) internship.setLevel(InternshipLevel.valueOf(lv));

            System.out.print("Opening Date (YYYY-MM-DD) [" + internship.getOpeningDate() + "]: ");
            String od = sc.nextLine().trim();
            if (!od.isEmpty()) internship.setOpeningDate(LocalDate.parse(od, df));

            System.out.print("Closing Date (YYYY-MM-DD) [" + internship.getClosingDate() + "]: ");
            String cd = sc.nextLine().trim();
            if (!cd.isEmpty()) internship.setClosingDate(LocalDate.parse(cd, df));

            System.out.print("Slots (1–10) [" + internship.getSlots() + "]: ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) internship.setSlots(Integer.parseInt(s));

            internshipService.updateInternship(internship);
            System.out.println("Internship updated successfully.");

        } catch (Exception e) {
            System.out.println("Error while editing internship: " + e.getMessage());
        }
    }

    private boolean deletePosting(Internship internship) {
        if (!internship.getApplications().isEmpty()) {
            System.out.println("Error: Cannot delete posting. It has applicants.");
            return false;
        }

        try {
            System.out.print("Are you sure you want to delete this posting? (y/n): ");
            if (sc.nextLine().trim().toLowerCase().startsWith("y")) {
                boolean ok = internshipService.deleteInternship(internship);
                System.out.println(ok ? "Internship deleted." : "Failed to delete.");
                return ok;
            }
            return false;

        } catch (Exception e) {
            System.out.println("Error while deleting internship: " + e.getMessage());
            return false;
        }
    }

    private void viewFilteredInternships() {
        try {
            System.out.println("-- Internship Filters --");
            System.out.println("Press Enter to keep previous values.");

            System.out.print("Status (Pending/Approved/Rejected/Filled): ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                try {
                    lastStatusFilter = InternshipStatus.valueOf(s.toUpperCase());
                } catch (Exception ignored) {
                    lastStatusFilter = null;
                }
            }

            System.out.print("Preferred Major: ");
            String m = sc.nextLine().trim();
            if (!m.isEmpty()) lastMajorFilter = m;

            System.out.print("Level (Basic/Intermediate/Advanced): ");
            String lv = sc.nextLine().trim();
            if (!lv.isEmpty()) {
                try {
                    lastLevelFilter = InternshipLevel.valueOf(lv.toUpperCase());
                } catch (Exception ignored) {
                    lastLevelFilter = null;
                }
            }

            System.out.print("Closing Date before (YYYY-MM-DD): ");
            String dateStr = sc.nextLine().trim();
            if (!dateStr.isEmpty()) {
                try {
                    lastClosingDateFilter = LocalDate.parse(dateStr,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception ignored) {
                    lastClosingDateFilter = null;
                }
            }

            List<Internship> list =
                    new ArrayList<>(internshipService.getAllInternships());

            list.sort(Comparator.comparing(Internship::getTitle,
                    String.CASE_INSENSITIVE_ORDER));

            list.removeIf(it ->
                    (lastStatusFilter != null && it.getStatus() != lastStatusFilter) ||
                            (lastMajorFilter != null &&
                                    !it.getPreferredMajor().equalsIgnoreCase(lastMajorFilter)) ||
                            (lastLevelFilter != null && it.getLevel() != lastLevelFilter) ||
                            (lastClosingDateFilter != null &&
                                    it.getClosingDate().isAfter(lastClosingDateFilter))
            );

            if (list.isEmpty()) {
                System.out.println("No internships match your filters.");
                return;
            }

            for (Internship it : list) {
                System.out.println(it.getTitle() + " | " + it.getCompanyName()
                        + " | Level: " + it.getLevel()
                        + " | Major: " + it.getPreferredMajor()
                        + " | Status: " + it.getStatus()
                        + " | Closes: " + it.getClosingDate());
            }

        } catch (Exception e) {
            System.out.println("Error while filtering internships: " + e.getMessage());
        }
    }
}
