package control;

import data.UserDataHandler;
import entities.CompanyRepresentative;
import entities.User;

import java.util.*;

public class UserManager {
    private Map<String, User> users;
    private final UserDataHandler dataHandler;

    public UserManager(UserDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        this.users = new HashMap<>();
    }

    public void loadAllUsers() {
        this.users = dataHandler.loadUsersFromCSV();
    }

    public User login(String id, String password) {
        User user = users.get(id);
        if (user == null || !user.checkPassword(password)) return null;
        if (user instanceof CompanyRepresentative rep && !rep.isApproved()) return null;
        return user;
    }

    public CompanyRepresentative registerCompanyRep(String email, String name, String company, String dept, String position) {
        if (users.containsKey(email)) return null;
        CompanyRepresentative rep = new CompanyRepresentative(email, name, company, dept, position, email);
        users.put(email, rep);
        return rep;
    }

    public List<CompanyRepresentative> getPendingCompanyReps() {
        List<CompanyRepresentative> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof CompanyRepresentative rep && !rep.isApproved()) list.add(rep);
        }
        return list;
    }

    public User getUserById(String id) {
        return users.get(id);
    }

    public void removeUser(String id) {
        users.remove(id);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }
}
