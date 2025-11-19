package control;

import entities.*;
import java.time.LocalDate;
import java.util.List;

public interface IInternshipService {
    List<Internship> getAllInternships();

    void reviewInternshipPosting(Internship internship, boolean approve);
    List<WithdrawalRequest> getPendingWithdrawalRequests();
    void processWithdrawalRequest(WithdrawalRequest req, boolean approve);
    WithdrawalRequest requestWithdrawal(Student student, Internship internship);
    boolean acceptOffer(Student student, Internship internship);
    boolean applyForInternship(Student student, Internship internship);

    Internship createInternship(
            CompanyRepresentative rep,
            String title,
            String description,
            InternshipLevel level,
            String prefer_major,
            LocalDate openingDate,
            LocalDate closingDate,
            int slots
    );

    void toggleVisibility(Internship internship);

    boolean updateInternship(Internship internship);

    boolean deleteInternship(Internship internship);

    boolean reviewApplication(Internship internship, Student student, boolean approve);
}
