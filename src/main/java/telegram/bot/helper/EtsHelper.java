package telegram.bot.helper;

import com.pengrad.telegrambot.model.User;
import helper.file.SharedObject;

import java.util.*;
import java.util.stream.Collectors;

public class EtsHelper {

    private final String etsUsers;
    private final String etsUsersInVacation;
    private final String etsUsersWithIssues;
    private final String etsUsersWithApprovedIssues;

    public EtsHelper(String etsUsers, String etsUsersInVacation, String etsUsersWithIssues, String etsUsersWithApprovedIssues) {
        this.etsUsers = etsUsers;
        this.etsUsersInVacation = etsUsersInVacation;
        this.etsUsersWithIssues = etsUsersWithIssues;
        this.etsUsersWithApprovedIssues = etsUsersWithApprovedIssues;
    }

    public void clearFromDuplicates(HashMap<User, Boolean> users) {
        List<User> userList = new ArrayList<>();
        Set<Map.Entry<User, Boolean>> entries = users.entrySet();
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User user = userBooleanEntry.getKey();
            if (isUserPresent(user, entries) && !isUserPresent(user, userList)) {
                userList.add(user);
            }
        }
        for (User user : userList) {
            users.remove(user);
        }
    }

    public void removeUser(User userToRemove) {
        List<User> userList = new ArrayList<>();
        HashMap<User, Boolean> users = getUsers();
        Set<Map.Entry<User, Boolean>> entries = users.entrySet();
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User user = userBooleanEntry.getKey();
            if (user.id().equals(userToRemove.id())) {
                userList.add(user);
            }
        }
        for (User user : userList) {
            users.remove(user);
        }
        removeUserFromApprovedIssuesList(userToRemove);
        removeUserFromIssuesList(userToRemove);
        removeUserFromVacation(userToRemove);
        saveUsers(users);
    }

    private static boolean isUserPresent(User user, List<User> users) {
        for (User user1 : users) {
            if (user1.id().equals(user.id())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUserPresent(User user, Set<Map.Entry<User, Boolean>> entries) {
        int amount = 0;
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User entryKey = userBooleanEntry.getKey();
            if (!entryKey.equals(user) && Objects.equals(entryKey.id(), user.id())) {
                amount++;
            }
        }
        return amount > 0;
    }

    public HashMap<User, Boolean> getUsers() {
        return SharedObject.loadMap(etsUsers, new HashMap<>());
    }

    public void saveUsers(HashMap<User, Boolean> users) {
        clearFromDuplicates(users);
        SharedObject.save(etsUsers, users);
    }

    public ArrayList<User> getUsersFromVacation() {
        return SharedObject.loadList(etsUsersInVacation, new ArrayList<>());
    }

    public void saveUsersWhichInVacation(ArrayList<User> users) {
        SharedObject.save(etsUsersInVacation, users);
    }

    private void removeUserFromVacation(User user) {
        ArrayList<User> users = getUsersFromVacation()
            .stream().filter(user1 -> !user1.id().equals(user.id()))
            .collect(Collectors.toCollection(ArrayList::new));
        saveUsersWhichInVacation(users);
    }

    private void saveUsersWhichHaveIssues(ArrayList<User> users) {
        SharedObject.save(etsUsersWithIssues, users);
    }

    private void saveUsersWhichHaveApprovedIssues(ArrayList<User> users) {
        SharedObject.save(etsUsersWithApprovedIssues, users);
    }

    public ArrayList<User> getUsersWhichHaveIssues() {
        return SharedObject.loadList(etsUsersWithIssues, new ArrayList<>());
    }

    public ArrayList<User> getUsersWhichHaveApprovedIssues() {
        return SharedObject.loadList(etsUsersWithApprovedIssues, new ArrayList<>());
    }


    public void userOnVacation(User user) {
        ArrayList<User> users = getUsersFromVacation();
        users.add(user);
        removeUserFromIssuesList(user);
        removeUserFromApprovedIssuesList(user);
        resolveUser(user);
        saveUsersWhichInVacation(users);
    }

    private void removeUserFromIssuesList(User user) {
        ArrayList<User> users = getUsersWhichHaveIssues()
            .stream().filter(user1 -> !user1.id().equals(user.id()))
            .collect(Collectors.toCollection(ArrayList::new));
        saveUsersWhichHaveIssues(users);
    }

    private void removeUserFromApprovedIssuesList(User user) {
        ArrayList<User> users = getUsersWhichHaveApprovedIssues()
            .stream().filter(user1 -> !user1.id().equals(user.id()))
            .collect(Collectors.toCollection(ArrayList::new));
        saveUsersWhichHaveApprovedIssues(users);
    }

    public void approveUserIssue(User user) {
        ArrayList<User> users = getUsersWhichHaveApprovedIssues();
        users.add(user);
        removeUserFromVacation(user);
        unResolveUser(user);
        userHasIssue(user);
        saveUsersWhichHaveApprovedIssues(users);
    }

    public void userHasIssue(User user) {
        ArrayList<User> users = getUsersWhichHaveIssues();
        users.add(user);
        removeUserFromVacation(user);
        unResolveUser(user);
        saveUsersWhichHaveIssues(users);
    }

    public boolean isUserHasIssue(User user) {
        ArrayList<User> users = getUsersWhichHaveIssues();
        for (User user1 : users) {
            if (user1.id().equals(user.id())) {
                return true;
            }
        }

        return false;
    }

    public boolean isUserHasApprovedIssue(User user) {
        ArrayList<User> users = getUsersWhichHaveApprovedIssues();
        for (User user1 : users) {
            if (user1.id().equals(user.id())) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserOnVacation(User user) {
        ArrayList<User> users = getUsersFromVacation();
        for (User user1 : users) {
            if (user1.id().equals(user.id())) {
                return true;
            }
        }

        return false;
    }

    public boolean isUserResolve(User user) {
        HashMap<User, Boolean> users = getUsers();
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            if (entry.getKey().id().equals(user.id())) {
                return entry.getValue();
            }
        }
        return false;
    }

    public void unResolveAllUsers() {
        resolveAllUsers(false);
    }

    public void resolveAllUsers() {
        resolveAllUsers(true);
    }

    public void resolveAllUsers(Boolean status) {
        HashMap<User, Boolean> users = getUsers();
        for (Map.Entry<User, Boolean> entry : users.entrySet()) {
            resolveUser(entry.getKey(), status);
        }
    }

    public void unResolveAllUsualUsers() {
        HashMap<User, Boolean> users = getUsers();
        for (User user : users.keySet()) {
            if (!isUserOnVacation(user)) {
                unResolveUser(user);
            }
        }
    }

    public void unResolveUser(User user) {
        resolveUser(user, false);
    }

    public void resolveUser(User user) {
        resolveUser(user, true);
    }

    public void resolveUser(User user, boolean status) {
        HashMap<User, Boolean> users = getUsers();
        users.put(user, status);
        saveUsers(users);
        removeUserFromIssuesList(user);
        removeUserFromApprovedIssuesList(user);
        removeUserFromVacation(user);
    }
}
