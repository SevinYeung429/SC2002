package data;

import control.UserManager;
import entities.CareerCenterStaff;
import entities.CompanyRepresentative;
import entities.Student;
import entities.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserDataHandler {

    public Map<String, User> loadUsersFromCSV() {
        Map<String, User> users = new HashMap<>();

        readStudents(users);
        readStaff(users);
        readCompanyReps(users);

        if (users.isEmpty()) {
            System.out.println("⚠ No CSV found, creating demo data.");
            CareerCenterStaff staff = new CareerCenterStaff("staff1", "Default Staff", "Career Coach", "Career Center", "staff1@career.com");
            Student student = new Student("U1234567A", "Default Student", "Computer Science", 3, "student@ntu.edu.sg");
            CompanyRepresentative rep = new CompanyRepresentative("rep001", "Default Rep", "ABC Corp", "HR", "Manager", "rep@company.com");
            rep.setApproved(true);
            users.put(staff.getId(), staff);
            users.put(student.getId(), student);
            users.put(rep.getId(), rep);
        }
        return users;
    }

    private void readStudents(Map<String, User> users) {
        try (BufferedReader br = new BufferedReader(new FileReader("sample_student_list.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5)
                    users.put(p[0].trim(), new Student(p[0].trim(), p[1].trim(), p[2].trim(), Integer.parseInt(p[3].trim()), p[4].trim()));
            }
        } catch (IOException ignored) {}
    }

    private void readStaff(Map<String, User> users) {
        try (BufferedReader br = new BufferedReader(new FileReader("sample_staff_list.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 5)
                    users.put(p[0].trim(), new CareerCenterStaff(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim()));
            }
        } catch (IOException ignored) {}
    }

    private void readCompanyReps(Map<String, User> users) {
        try (BufferedReader br = new BufferedReader(new FileReader("sample_company_representative_list.csv"))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 6) {
                    CompanyRepresentative rep = new CompanyRepresentative(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim(), p[5].trim());
                    if (p.length > 6 && p[6].trim().equalsIgnoreCase("approved")) rep.setApproved(true);
                    users.put(p[0].trim(), rep);
                }
            }
        } catch (IOException ignored) {}
    }

    public void saveUsersToCSV(UserManager manager) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("users_out.csv"))) {
            pw.println("ID,Name,Email,Type,Extra1,Extra2,Approved");
            for (User u : manager.getAllUsers()) {
                if (u instanceof Student s)
                    pw.println(s.getId() + "," + s.getName() + "," + s.getEmail() + ",Student," + s.getMajor() + ",Year " + s.getYear());
                else if (u instanceof CareerCenterStaff st)
                    pw.println(st.getId() + "," + st.getName() + "," + st.getEmail() + ",Staff," + st.getRole() + "," + st.getDepartment());
                else if (u instanceof CompanyRepresentative rep)
                    pw.println(rep.getId() + "," + rep.getName() + "," + rep.getEmail() + ",CompanyRep," +
                            rep.getCompanyName() + "," + rep.getDepartment() + "," + (rep.isApproved() ? "approved" : "pending"));
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }
}
