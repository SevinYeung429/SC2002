package control;

import entities.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InternshipManager implements IInternshipService {
    private List<Internship> internships;
    private List<WithdrawalRequest> withdrawalRequests;
    private int nextInternshipId;

    public InternshipManager() {
        internships = new ArrayList<>();
        withdrawalRequests = new ArrayList<>();
        nextInternshipId = 1;
    }

   public Internship createInternship(CompanyRepresentative rep, String title, String description,
                                       InternshipLevel level, String prefer_major,
                                       LocalDate openingDate, LocalDate closingDate, int slots) {
        int activeCount = 0;
        for (Internship it : internships) {
            if (it.getPostedBy().equals(rep)
                    && it.getStatus() != InternshipStatus.FILLED
                    && it.getStatus() != InternshipStatus.REJECTED) {
                activeCount++;
            }
        }
        if (activeCount >= 5 || slots < 1 || slots > 10) return null;

        Internship internship = new Internship(nextInternshipId++, title, description, level,
                prefer_major, openingDate, closingDate, slots, rep);
        internships.add(internship);
        rep.addInternshipPosted(internship);
        return internship;
    }

    public boolean applyForInternship(Student student, Internship internship) {
        if (!internship.isOpenForApplication(student)) return false;
        if (internship.getApplications().containsKey(student.getId())) return false;
        if (countActiveApplications(student) >= 3) return false;

        internship.getApplications().put(student.getId(), ApplicationStatus.APPLIED);
        return true;
    }

    public int countActiveApplications(Student student) {
        int count = 0;
        for (Internship it : internships) {
            ApplicationStatus status = it.getApplications().get(student.getId());
            if (status == ApplicationStatus.APPLIED || status == ApplicationStatus.OFFERED) count++;
        }
        return count;
    }


    public WithdrawalRequest requestWithdrawal(Student student, Internship internship) {
        ApplicationStatus currentStatus = internship.getApplications().get(student.getId());
        if (currentStatus == null) return null;

        boolean wasConfirmed = (currentStatus == ApplicationStatus.CONFIRMED);
        WithdrawalRequest req = new WithdrawalRequest(student, internship, wasConfirmed);
        withdrawalRequests.add(req);
        return req;
    }
    public void processWithdrawalRequest(WithdrawalRequest req, boolean approve) {
        Student student = req.getStudent();
        Internship internship = req.getInternship();
        ApplicationStatus currStatus = internship.getApplications().get(student.getId());

        if (!approve || currStatus == null) {
            withdrawalRequests.remove(req);
            return;
        }

        if (req.wasAfterConfirmation()) {
            student.setAcceptedInternshipId(-1);
            internship.getApplications().put(student.getId(), ApplicationStatus.WITHDRAWN);

            if (internship.getStatus() == InternshipStatus.FILLED)
                internship.setStatus(InternshipStatus.APPROVED);
        } else {
            internship.getApplications().put(student.getId(), ApplicationStatus.WITHDRAWN);
        }

        req.setApproved(true);
        withdrawalRequests.remove(req);
    }

    public List<WithdrawalRequest> getPendingWithdrawalRequests() {
        return withdrawalRequests.stream()
                .filter(r -> !r.isApproved())
                .collect(Collectors.toList());
    }

    public void reviewInternshipPosting(Internship internship, boolean approve) {
        if (approve) {
            internship.setStatus(InternshipStatus.APPROVED);
            internship.setVisible(true);
        } else {
            internship.setStatus(InternshipStatus.REJECTED);
            internship.setVisible(false);
        }
    }

    public boolean reviewApplication(Internship internship, Student student, boolean approve) {
        ApplicationStatus status = internship.getApplications().get(student.getId());
        if (status == null || status != ApplicationStatus.APPLIED) return false;

        if (approve) {
            int offeredCount = internship.countApplicationsByStatus(ApplicationStatus.OFFERED);
            int confirmedCount = internship.countApplicationsByStatus(ApplicationStatus.CONFIRMED);
            if (offeredCount + confirmedCount >= internship.getSlots()) return false;

            internship.getApplications().put(student.getId(), ApplicationStatus.OFFERED);
        } else {
            internship.getApplications().put(student.getId(), ApplicationStatus.REJECTED);
        }
        return true;
    }

   public boolean acceptOffer(Student student, Internship internship) {
        ApplicationStatus status = internship.getApplications().get(student.getId());
        if (status == null || status != ApplicationStatus.OFFERED) return false;
        if (student.getAcceptedInternshipId() != -1) return false;

        internship.getApplications().put(student.getId(), ApplicationStatus.CONFIRMED);
        student.setAcceptedInternshipId(internship.getId());

        int confirmedCount = internship.countApplicationsByStatus(ApplicationStatus.CONFIRMED);
        if (confirmedCount >= internship.getSlots()){
            internship.setStatus(InternshipStatus.FILLED);
            autoWithdrawOtherApplications(student, internship);
        }
        return true;
    }

    public void toggleVisibility(Internship internship) {
        internship.setVisible(!internship.isVisible());
    }



    public List<Internship> getAllInternships() {
        return internships;
    }

    public boolean updateInternship(Internship internship) {
        if (internship == null) return false;
        for (int i = 0; i < internships.size(); i++) {
            if (internships.get(i).getId() == internship.getId()) {
                internships.set(i, internship); 
                return true;
            }
        }
        return false;
    }

    public boolean deleteInternship(Internship internship) {
        if (!internships.contains(internship))
            return false;

        internships.remove(internship);

        CompanyRepresentative rep = internship.getPostedBy();
        if (rep != null) {
            rep.getInternshipsPosted().remove(internship);
        }

        return true;
    }

    public void autoWithdrawOtherApplications(Student student, Internship acceptedInternship) {
        String sid = student.getId();
        for (Internship internship : internships) {
            if (internship.getId() == acceptedInternship.getId()) continue;
            if (internship.getApplications().containsKey(sid)) {
                internship.getApplications().put(sid, ApplicationStatus.WITHDRAWN);
            }
        }
    }
}
