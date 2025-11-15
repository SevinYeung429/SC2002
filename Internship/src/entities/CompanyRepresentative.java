package entities;
/**
 * CompanyRepresentative user class, extends User.
 * Contains additional attributes like company name, department, position,
 * email, and approval status for account activation.
 */
import java.util.ArrayList;
import java.util.List;

public class CompanyRepresentative extends User {
    private String companyName;
    private String department;
    private String position;
    private String email;
    private boolean isApproved;
    private List<Internship> internshipsPosted;

    public CompanyRepresentative(String id, String name, String companyName,
                                 String department, String position, String email) {
        super(id, name, "password");
        this.companyName = companyName;
        this.department = department;
        this.position = position;
        this.email = email;
        this.isApproved = false;
        this.internshipsPosted = new ArrayList<>();
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        this.isApproved = approved;
    }

    public List<Internship> getInternshipsPosted() {
        return internshipsPosted;
    }

    public void addInternshipPosted(Internship internship) {
        internshipsPosted.add(internship);
    }


    @Override
    public String toString() {
        return "CompanyRepresentative [ID=" + getId() + ", Name=" + getName() +
                ", Company=" + companyName + ", Department=" + department +
                ", Position=" + position + ", Email=" + email +
                ", Approved=" + isApproved + "]";
    }
}

