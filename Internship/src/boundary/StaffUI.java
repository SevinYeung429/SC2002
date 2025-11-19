package boundary;

import control.IInternshipService;
import control.IUserService;
import entities.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class StaffUI implements IStaffUI {

    private final IUserService userService;
    private final IInternshipService internshipService;
    private final Scanner sc;

    private InternshipStatus lastStatusFilter = null;
    private InternshipLevel lastLevelFilter = null;
    private String lastMajorFilter = null;

    public StaffUI(IUserService userService, IInternshipService internshipService, Scanner sc) {
        this.userService = userService;
        this.internshipService = internshipService;
        this.sc = sc;
    }

    @Override
    public void staffMenu(CareerCenterStaff staff) {
        boolean logout = false;
        while (!logout) {
            try {
                System.out.println("\n-- Career Center Staff Menu --");
                System.out.println("1. Approve/Reject Company Representative Accounts");
                System.out.println("2. Approve/Reject Internship Postings");
                System.out.println("3. Approve/Reject Student Withdrawal Requests");
                System.out.println("4. Generate Reports");
                System.out.println("5. Change Password");
                System.out.println("6. Logout");
                System.out.print("Choice: ");
                switch (sc.nextLine().trim()) {
                    case "1" -> approveCompanyAccounts();
                    case "2" -> approveInternships();
                    case "3" -> reviewWithdrawals();
                    case "4" -> generateReports();
                    case "5" -> changePassword(staff);
                    case "6" -> logout = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void changePassword(CareerCenterStaff staff) {
        try {
            System.out.println("-- Change Password --");
            System.out.print("Enter current password: ");
            String oldPass = sc.nextLine().trim();
            System.out.print("Enter new password: ");
            String newPass = sc.nextLine().trim();
            boolean success = staff.changePassword(oldPass, newPass);
            System.out.println(success ? "Password changed successfully." : "Password change failed. Current password incorrect.");
        } catch (Exception e) {
            System.out.println("Error while changing password: " + e.getMessage());
        }
    }

    private void approveCompanyAccounts() {
        try {
            var pending = userService.getPendingCompanyReps();
            if (pending.isEmpty()) {
                System.out.println("No pending company representative accounts.");
                return;
            }
            for (int i = 0; i < pending.size(); i++) {
                CompanyRepresentative rep = pending.get(i);
                System.out.println((i + 1) + ". " + rep.getName() + " (" + rep.getCompanyName() + ")");
            }
            System.out.print("Select account (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch <= 0 || ch > pending.size()) return;
            CompanyRepresentative rep = pending.get(ch - 1);
            System.out.print("Approve this account? (y/n): ");
            if (sc.nextLine().trim().toLowerCase().startsWith("y")) {
                rep.setApproved(true);
                System.out.println("Account approved. The representative may now log in.");
            } else {
                userService.removeUser(rep.getId());
                System.out.println("Account rejected and removed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error while approving company accounts: " + e.getMessage());
        }
    }

    private void approveInternships() {
        try {
            var pending = internshipService.getAllInternships().stream()
                    .filter(i -> i.getStatus() == InternshipStatus.PENDING)
                    .toList();
            if (pending.isEmpty()) {
                System.out.println("No pending internship postings.");
                return;
            }
            for (int i = 0; i < pending.size(); i++) {
                Internship it = pending.get(i);
                System.out.println((i + 1) + ". " + it.getTitle() + " (" + it.getCompanyName() + ","+it.getDescription()+ ")");
            }
            System.out.print("Select posting (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch <= 0 || ch > pending.size()) return;
            Internship internship = pending.get(ch - 1);
            System.out.print("Approve this internship? (y/n): ");
            boolean approve = sc.nextLine().trim().toLowerCase().startsWith("y");
            internshipService.reviewInternshipPosting(internship, approve);
            System.out.println(approve ? "Posting approved and now visible to students." : "Posting rejected.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error while approving internships: " + e.getMessage());
        }
    }

    private void reviewWithdrawals() {
        try {
            var reqs = internshipService.getPendingWithdrawalRequests();
            if (reqs.isEmpty()) {
                System.out.println("No withdrawal requests pending.");
                return;
            }
            for (int i = 0; i < reqs.size(); i++) {
                WithdrawalRequest r = reqs.get(i);
                System.out.println((i + 1) + ". Student: " + r.getStudent().getName() +
                        " | Internship: " + r.getInternship().getTitle() +
                        (r.wasAfterConfirmation() ? " (After Confirmation)" : ""));
            }
            System.out.print("Select request (0 to cancel): ");
            int ch = Integer.parseInt(sc.nextLine());
            if (ch <= 0 || ch > reqs.size()) return;
            WithdrawalRequest req = reqs.get(ch - 1);
            System.out.print("Approve this withdrawal? (y/n): ");
            boolean approve = sc.nextLine().trim().toLowerCase().startsWith("y");
            internshipService.processWithdrawalRequest(req, approve);
            System.out.println(approve ? "Withdrawal approved." : "Withdrawal rejected.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        } catch (Exception e) {
            System.out.println("Error while reviewing withdrawals: " + e.getMessage());
        }
    }

    private void generateReports() {
        try {
            System.out.println("-- Internship Report Filters --");
            System.out.println("Press Enter to keep previous filter.");

            System.out.print("Status (Pending/Approved/Rejected/Filled): ");
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) {
                try { lastStatusFilter = InternshipStatus.valueOf(s.toUpperCase()); }
                catch (Exception e) { lastStatusFilter = null; }
            }

            System.out.print("Preferred Major: ");
            String m = sc.nextLine().trim();
            if (!m.isEmpty()) lastMajorFilter = m;

            System.out.print("Level (Basic/Intermediate/Advanced): ");
            String l = sc.nextLine().trim();
            if (!l.isEmpty()) {
                try { lastLevelFilter = InternshipLevel.valueOf(l.toUpperCase()); }
                catch (Exception e) { lastLevelFilter = null; }
            }

            List<Internship> list = new ArrayList<>(internshipService.getAllInternships());
            list.sort(Comparator.comparing(Internship::getTitle, String.CASE_INSENSITIVE_ORDER));
            list.removeIf(it ->
                    (lastStatusFilter != null && it.getStatus() != lastStatusFilter) ||
                            (lastMajorFilter != null && !it.getPreferredMajor().equalsIgnoreCase(lastMajorFilter)) ||
                            (lastLevelFilter != null && it.getLevel() != lastLevelFilter)
            );

            if (list.isEmpty()) {
                System.out.println("No internship postings match the filters.");
                return;
            }

            System.out.println("=== Internship Report ===");
            for (Internship it : list) {
                int totalApps = it.getApplications().size();
                int confirmed = it.countApplicationsByStatus(ApplicationStatus.CONFIRMED);
                System.out.println(it.getTitle() + " | " + it.getCompanyName() +
                        " | Description: " + it.getDescription() +
                        " | Status: " + it.getStatus() +
                        " | Level: " + it.getLevel() +
                        " | Major: " + it.getPreferredMajor() +
                        " | Applicants: " + totalApps +
                        " | Confirmed: " + confirmed);
            }
        } catch (Exception e) {
            System.out.println("Error while generating reports: " + e.getMessage());
        }
    }
}

