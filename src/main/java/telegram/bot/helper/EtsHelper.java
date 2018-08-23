package telegram.bot.helper;

import com.pengrad.telegrambot.model.User;
import helper.file.SharedObject;

import java.util.*;

import static telegram.bot.data.Common.ETS_USERS;
import static telegram.bot.data.Common.ETS_USERS_IN_VACATION;

public class EtsHelper {

    public static void clearFromDuplicates(HashMap<User, Boolean> users) {
        List<User> userList = new ArrayList<>();
        Set<Map.Entry<User, Boolean>> entries = users.entrySet();
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User user = userBooleanEntry.getKey();
            if (isUserPresent(user, entries)) {
                userList.add(user);
            }
        }
        for (User user : userList) {
            users.remove(user);
        }
    }

    private static boolean isUserPresent(User user, Set<Map.Entry<User, Boolean>> entries) {
        Integer amount = 0;
        for (Map.Entry<User, Boolean> userBooleanEntry : entries) {
            User entryKey = userBooleanEntry.getKey();
            if (!entryKey.equals(user) && Objects.equals(entryKey.id(), user.id())) {
                amount++;
            }
        }
        return amount > 0;
    }

    public static HashMap<User, Boolean> getUsers() {
        return SharedObject.loadMap(ETS_USERS, new HashMap<>());
    }

    public static void saveUsers(HashMap<User, Boolean> users) {
        SharedObject.save(ETS_USERS, users);
    }

    public static ArrayList<User> getUsersFromVacation() {
        return SharedObject.loadList(ETS_USERS_IN_VACATION, new ArrayList<>());
    }

    public static void saveUsersWhichInVacation(ArrayList<User> users) {
        SharedObject.save(ETS_USERS_IN_VACATION, users);
    }
}
