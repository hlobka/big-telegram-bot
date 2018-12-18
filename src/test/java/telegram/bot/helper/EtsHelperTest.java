package telegram.bot.helper;

import com.pengrad.telegrambot.model.User;
import helper.file.SharedObject;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class EtsHelperTest {
    private String testRootUrl = "/tmp/test/";

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(testRootUrl));
        SharedObject.remove(this, "test");
    }

    @Test
    public void testClearFromDuplicates() {
        EtsHelper etsHelper = getEtsHelper();
        HashMap<User, Boolean> users = etsHelper.getUsers();
        users.put(getUser(1, "test1"), true);
        users.put(getUser(1, "test2"), false);
        Assertions.assertThat(users).hasSize(2);
        etsHelper.clearFromDuplicates(users);
        Assertions.assertThat(users).hasSize(1);
    }

    @Test
    public void testGetUsers() {
        EtsHelper etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();
    }

    @Test
    public void testRemoveUser() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1));
        etsHelper.userOnVacation(getUser(2));
        etsHelper.userHasIssue(getUser(3));

        assertThat(etsHelper.getUsers()).hasSize(3);

        etsHelper.removeUser(getUser(1));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);
    }

    @Test
    public void testRemoveUserShouldBeRemoveFromVacations() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1));
        etsHelper.userOnVacation(getUser(2));
        etsHelper.userHasIssue(getUser(3));

        assertThat(etsHelper.getUsers()).hasSize(3);
        assertThat(etsHelper.isUserOnVacation(getUser(2))).isTrue();

        etsHelper.removeUser(getUser(2));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);
        assertThat(etsHelper.isUserOnVacation(getUser(2))).isFalse();
    }

    @Test
    public void testRemoveUserShouldBeRemoveFromIssuesUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1));
        etsHelper.userOnVacation(getUser(2));
        etsHelper.userHasIssue(getUser(3));

        assertThat(etsHelper.getUsers()).hasSize(3);
        assertThat(etsHelper.isUserHasIssue(getUser(3))).isTrue();

        etsHelper.removeUser(getUser(3));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);
        assertThat(etsHelper.isUserHasIssue(getUser(3))).isFalse();
    }

    @Test
    public void testResolveAllUsualUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        etsHelper.resolveUser(getUser(1));
        etsHelper.userOnVacation(getUser(2));
        etsHelper.userHasIssue(getUser(3));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(1))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(1))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(2))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(2))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(3))).isTrue();
        assertThat(etsHelper.isUserOnVacation(getUser(3))).isFalse();

        etsHelper.unResolveAllUsualUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(1))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(1))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(2))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(2))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(3))).isTrue();
        assertThat(etsHelper.isUserOnVacation(getUser(3))).isFalse();
    }

    @Test
    public void testResolveAllUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        etsHelper.resolveUser(getUser(1));
        etsHelper.resolveUser(getUser(2));
        etsHelper.resolveUser(getUser(3));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(2))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3))).isTrue();

        etsHelper.unResolveAllUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(3))).isFalse();

        etsHelper.resolveAllUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(2))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3))).isTrue();
    }

    @Test
    public void testResolveUser() {

        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        User user = getUser();
        etsHelper.resolveUser(user);

        assertThat(etsHelper.getUsers()).isNotEmpty();
    }

    @Test
    public void testResolveUserShouldBeBackFromIssues() {

        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        User user = getUser();
        etsHelper.userHasIssue(user);
        assertThat(etsHelper.isUserHasIssue(user)).isTrue();

        etsHelper.resolveUser(user);

        assertThat(etsHelper.isUserHasIssue(user)).isFalse();
    }

    @Test
    public void testResolveUserShouldBeBackFromVacation() {

        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        User user = getUser();
        etsHelper.userOnVacation(user);
        assertThat(etsHelper.isUserOnVacation(user)).isTrue();

        etsHelper.resolveUser(user);

        assertThat(etsHelper.isUserOnVacation(user)).isFalse();
    }

    @Test
    public void testSaveUsers() {
        HashMap<User, Boolean> users;
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        users = etsHelper.getUsers();
        assertThat(users).isEmpty();

        users.put(getUser(), true);
        etsHelper = getEtsHelper();
        users = etsHelper.getUsers();
        assertThat(users).isEmpty();

        User userMock = getUser();


        users.put(userMock, true);
        etsHelper.saveUsers(users);
        etsHelper = getEtsHelper();
        users = etsHelper.getUsers();
        assertThat(users).isNotEmpty();
    }

    @Test
    public void testGetUsersFromVacation() {
        EtsHelper etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsersFromVacation()).isEmpty();
    }

    @Test
    public void testSaveUsersWhichInVacation() {
        EtsHelper etsHelper;
        ArrayList<User> usersFromVacation;
        etsHelper = getEtsHelper();
        usersFromVacation = etsHelper.getUsersFromVacation();
        assertThat(usersFromVacation).isEmpty();
        User userMock = getUser();

        usersFromVacation.add(userMock);
        etsHelper.saveUsersWhichInVacation(usersFromVacation);

        etsHelper = getEtsHelper();
        usersFromVacation = etsHelper.getUsersFromVacation();
        assertThat(usersFromVacation).isNotEmpty();

    }

    @Test
    public void testUserOnVacation() {
        ArrayList<User> usersFromVacation;
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        usersFromVacation = etsHelper.getUsersFromVacation();
        assertThat(usersFromVacation).isEmpty();
        User userMock = getUser();

        etsHelper.userOnVacation(userMock);

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsersFromVacation()).isNotEmpty().contains(userMock);
    }

    @Test
    public void testUserOnVacationShouldBeWithoutIssues() {
        EtsHelper etsHelper;
        ArrayList<User> usersFromVacation;
        etsHelper = getEtsHelper();
        usersFromVacation = etsHelper.getUsersFromVacation();
        assertThat(usersFromVacation).isEmpty();
        User userMock = getUser();

        etsHelper.userHasIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        etsHelper.userOnVacation(userMock);

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsersFromVacation()).isNotEmpty().contains(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
    }

    @Test
    public void testUserHasIssue() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();


        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        etsHelper.userHasIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
    }

    @Test
    public void testUserHasIssueShouldBeLeftFromVacation() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();

        etsHelper.userOnVacation(userMock);

        assertThat(etsHelper.isUserOnVacation(userMock)).isTrue();
        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        etsHelper.userHasIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserOnVacation(userMock)).isFalse();
    }

    @Test
    public void testUserOnVacationShouldBeResolve() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();

        etsHelper.unResolveUser(userMock);

        assertThat(etsHelper.isUserResolve(userMock)).isFalse();
        etsHelper.userOnVacation(userMock);

        assertThat(etsHelper.isUserOnVacation(userMock)).isTrue();
        assertThat(etsHelper.isUserResolve(userMock)).isTrue();
    }

    @Test
    public void testIsUserResolve() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();

        assertThat(etsHelper.isUserResolve(userMock)).isFalse();
    }

    @Test
    public void testUserHasIssueShouldBeUnResolve() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();

        etsHelper.resolveUser(userMock);

        assertThat(etsHelper.isUserResolve(userMock)).isTrue();
        etsHelper.userHasIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserResolve(userMock)).isFalse();
    }


    private EtsHelper getEtsHelper() {
        String etsUsers = testRootUrl + "test1.ser";
        String etsUsersInVacation = testRootUrl + "test2.ser";
        String etsUsersWithIssues = testRootUrl + "test3.ser";
        return new EtsHelper(etsUsers, etsUsersInVacation, etsUsersWithIssues);
    }

    private User getUser() {
        return getUser(1);
    }

    private User getUser(Integer id) {
        return getUser(id, "testName");
    }

    private User getUser(Integer id, String firstName) {
        User user = new User();
        setField(user, "id", id);
        setField(user, "first_name", firstName);
        return user;
    }

    public static boolean setField(Object targetObject, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = targetObject.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }
        Class superClass = targetObject.getClass().getSuperclass();
        while (field == null && superClass != null) {
            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }
        }
        if (field == null) {
            return false;
        }
        field.setAccessible(true);
        try {
            field.set(targetObject, fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

}