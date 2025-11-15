package entities;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Internship {
    private int id;
    private String title;
    private String description;
    private InternshipLevel level;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private String prefer_major;
    private InternshipStatus status;
    private boolean visible;
    private CompanyRepresentative postedBy;
    private int slots;
    private Map<String, ApplicationStatus> applications;

    public Internship(int id, String title, String description, InternshipLevel level, String prefer_major,
                      LocalDate openingDate, LocalDate closingDate, int slots, CompanyRepresentative postedBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.slots = slots;
        this.prefer_major = prefer_major;
        this.postedBy = postedBy;
        this.status = InternshipStatus.PENDING;
        this.visible = false;
        this.applications = new HashMap<>();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public InternshipLevel getLevel() { return level; }
    public LocalDate getOpeningDate() { return openingDate; }
    public LocalDate getClosingDate() { return closingDate; }
    public InternshipStatus getStatus() { return status; }
    public void setStatus(InternshipStatus status) { this.status = status; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public CompanyRepresentative getPostedBy() { return postedBy; }
    public int getSlots() { return slots; }
    public void setSlots(int slots) { this.slots = slots; }
    public String getPreferredMajor() { return prefer_major; }
    public void setPrefer_major(String prefer_major) { this.prefer_major = prefer_major; }
    public Map<String, ApplicationStatus> getApplications() { return applications; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLevel(InternshipLevel level) { this.level = level; }
    public void setOpeningDate(LocalDate date) { this.openingDate = date; }
    public void setClosingDate(LocalDate date) { this.closingDate = date; }

    public boolean isOpenForApplication(Student student) {
        if (status != InternshipStatus.APPROVED || !visible) return false;
        LocalDate today = LocalDate.now();
        if (today.isBefore(openingDate) || today.isAfter(closingDate)) return false;
        if (status == InternshipStatus.FILLED) return false;
        if (student.getYear() <= 2 && level != InternshipLevel.BASIC) return false;
        return true;
    }

    public int countApplicationsByStatus(ApplicationStatus s) {
        int count = 0;
        for (ApplicationStatus st : applications.values()) {
            if (st == s) count++;
        }
        return count;
    }

    public String getCompanyName() {
        return postedBy != null ? postedBy.getCompanyName() : "Unknown Company";
    }

    @Override
    public String toString() {
        return "Internship[ID=" + id +
                ", Title=" + title +
                ", Company=" + getCompanyName() +
                ", Level=" + level +
                ", Status=" + status +
                ", Slots=" + slots +
                ", Open Date=" + openingDate +
                ", Close Date=" + closingDate +
                ", Major Pref=" + prefer_major + "]";
    }
}

