package control;

import entities.*;

import java.util.List;

public interface IUserService {
    CompanyRepresentative registerCompanyRep(String email, String name, String company, String dept, String position);
    User getUserById(String id);
    List<CompanyRepresentative> getPendingCompanyReps();
    void removeUser(String id);
    User authenticateUser(String id, String password);
}
