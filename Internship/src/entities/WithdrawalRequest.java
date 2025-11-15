package entities;

public class WithdrawalRequest {
    private Student student;
    private Internship internship;
    private boolean afterConfirmation;
    private boolean approved;

    public WithdrawalRequest(Student student, Internship internship, boolean afterConfirmation) {
        this.student = student;
        this.internship = internship;
        this.afterConfirmation = afterConfirmation;
        this.approved = false;
    }

    public Student getStudent() { return student; }
    public Internship getInternship() { return internship; }
    public boolean wasAfterConfirmation() { return afterConfirmation; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}
