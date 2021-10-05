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
        users.put(getUser(1L, "test1"), true);
        users.put(getUser(1L, "test2"), false);
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
        etsHelper.resolveUser(getUser(1L));
        etsHelper.userOnVacation(getUser(2L));
        etsHelper.userHasIssue(getUser(3L));
        etsHelper.approveUserIssue(getUser(4L));

        assertThat(etsHelper.getUsers()).hasSize(4);

        etsHelper.removeUser(getUser(1L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(3);

        etsHelper.removeUser(getUser(2L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);

        etsHelper.removeUser(getUser(3L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(1);

        etsHelper.removeUser(getUser(4L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(0);
    }

    @Test
    public void testRemoveUserShouldBeRemoveFromVacations() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1L));
        etsHelper.userOnVacation(getUser(2L));
        etsHelper.userHasIssue(getUser(3L));
        etsHelper.approveUserIssue(getUser(4L));

        assertThat(etsHelper.getUsers()).hasSize(4);
        assertThat(etsHelper.isUserOnVacation(getUser(2L))).isTrue();

        etsHelper.removeUser(getUser(2L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(3);
        assertThat(etsHelper.isUserOnVacation(getUser(2L))).isFalse();
    }

    @Test
    public void testRemoveUserShouldBeRemoveFromIssuesUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1L));
        etsHelper.userOnVacation(getUser(2L));
        etsHelper.userHasIssue(getUser(3L));

        assertThat(etsHelper.getUsers()).hasSize(3);
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isTrue();

        etsHelper.removeUser(getUser(3L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isFalse();
    }

    @Test
    public void testRemoveUserShouldBeRemovedFromApprovedIssuesUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        etsHelper.resolveUser(getUser(1L));
        etsHelper.userOnVacation(getUser(2L));
        etsHelper.userHasIssue(getUser(3L));
        etsHelper.approveUserIssue(getUser(3L));

        assertThat(etsHelper.getUsers()).hasSize(3);
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(3L))).isTrue();

        etsHelper.removeUser(getUser(3L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).hasSize(2);
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(3L))).isFalse();
    }

    @Test
    public void testResolveAllUsualUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        etsHelper.resolveUser(getUser(1l));
        etsHelper.userOnVacation(getUser(2l));
        etsHelper.userHasIssue(getUser(3l));
        etsHelper.approveUserIssue(getUser(4l));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1L))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(2L))).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(2L))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isTrue();
        assertThat(etsHelper.isUserOnVacation(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(4L))).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(getUser(4L))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(4L))).isTrue();
        assertThat(etsHelper.isUserOnVacation(getUser(4L))).isFalse();

        etsHelper.unResolveAllUsualUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserHasIssue(getUser(2L))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserHasIssue(getUser(3L))).isFalse();
        assertThat(etsHelper.isUserOnVacation(getUser(3L))).isFalse();
    }

    @Test
    public void testResolveAllUsers() {
        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        etsHelper.resolveUser(getUser(1L));
        etsHelper.resolveUser(getUser(2L));
        etsHelper.resolveUser(getUser(3L));

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3L))).isTrue();

        etsHelper.unResolveAllUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1L))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(2L))).isFalse();
        assertThat(etsHelper.isUserResolve(getUser(3L))).isFalse();

        etsHelper.resolveAllUsers();

        etsHelper = getEtsHelper();
        assertThat(etsHelper.isUserResolve(getUser(1L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(2L))).isTrue();
        assertThat(etsHelper.isUserResolve(getUser(3L))).isTrue();
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
    public void testResolveUserWitsIssue() {

        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        User user = getUser();
        etsHelper.approveUserIssue(user);

        assertThat(etsHelper.isUserHasIssue(user)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(user)).isTrue();

        etsHelper.resolveUser(user);

        assertThat(etsHelper.isUserHasIssue(user)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(user)).isFalse();
    }

    @Test
    public void testResolveUserWitsIssueShouldBeBackFromIssues() {

        EtsHelper etsHelper;

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsers()).isEmpty();

        User user = getUser();
        etsHelper.approveUserIssue(user);
        assertThat(etsHelper.isUserHasIssue(user)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(user)).isTrue();

        etsHelper.resolveUser(user);

        assertThat(etsHelper.isUserHasIssue(user)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(user)).isFalse();
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
        User user2Mock = getUser(2L);

        etsHelper.userHasIssue(userMock);
        etsHelper.approveUserIssue(user2Mock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserHasIssue(user2Mock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(user2Mock)).isTrue();
        etsHelper.userOnVacation(userMock);
        etsHelper.userOnVacation(user2Mock);

        etsHelper = getEtsHelper();
        assertThat(etsHelper.getUsersFromVacation()).isNotEmpty().contains(userMock).contains(user2Mock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserHasIssue(user2Mock)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(user2Mock)).isFalse();
    }

    @Test
    public void testUserHasIssue() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();


        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isFalse();
        etsHelper.userHasIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isFalse();
    }

    @Test
    public void testUserHasApprovedIssue() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();


        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isFalse();
        etsHelper.approveUserIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isTrue();
    }

    @Test
    public void testUserHasIssueShouldBeLeftFromVacation() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();
        User user2Mock = getUser();

        etsHelper.userOnVacation(userMock);
        etsHelper.userOnVacation(user2Mock);

        assertThat(etsHelper.isUserOnVacation(userMock)).isTrue();
        assertThat(etsHelper.isUserHasIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserOnVacation(user2Mock)).isTrue();
        assertThat(etsHelper.isUserHasIssue(user2Mock)).isFalse();
        assertThat(etsHelper.isUserHasApprovedIssue(user2Mock)).isFalse();
        etsHelper.userHasIssue(userMock);
        etsHelper.userHasIssue(user2Mock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isFalse();
        assertThat(etsHelper.isUserOnVacation(userMock)).isFalse();
        assertThat(etsHelper.isUserHasIssue(user2Mock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(user2Mock)).isFalse();
        assertThat(etsHelper.isUserOnVacation(user2Mock)).isFalse();
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

    @Test
    public void testUserHasApprovedIssueShouldBeUnResolve() {
        EtsHelper etsHelper;
        etsHelper = getEtsHelper();
        User userMock = getUser();

        etsHelper.resolveUser(userMock);

        assertThat(etsHelper.isUserResolve(userMock)).isTrue();
        etsHelper.approveUserIssue(userMock);
        assertThat(etsHelper.isUserHasIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserHasApprovedIssue(userMock)).isTrue();
        assertThat(etsHelper.isUserResolve(userMock)).isFalse();
    }


    private EtsHelper getEtsHelper() {
        String etsUsers = testRootUrl + "test1.ser";
        String etsUsersInVacation = testRootUrl + "test2.ser";
        String etsUsersWithIssues = testRootUrl + "test3.ser";
        String etsUsersWithApprovedIssues = testRootUrl + "test4.ser";
        return new EtsHelper(etsUsers, etsUsersInVacation, etsUsersWithIssues, etsUsersWithApprovedIssues);
    }

    private User getUser() {
        return getUser(1L);
    }

    private User getUser(Long id) {
        return getUser(id, "testName");
    }

    private User getUser(Long id, String firstName) {
        User user = new User(id);
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