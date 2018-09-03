package helper.file;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class SharedObjectTest {

    private String testRootUrl = "/tmp/test/";
    private String testFileUrl = testRootUrl + "testFile.ser";

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(new File(testRootUrl));
        SharedObject.remove(this, "test");
    }

    @Test
    public void testLoad() {
        Assertions
            .assertThat(SharedObject.load(testFileUrl, Integer.class))
            .isNull();
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void testLoadIncorrectValue() {
        SharedObject.save(testFileUrl, "Test");
        Assertions
            .assertThat(SharedObject.load(testFileUrl, Integer.class))
            .isInstanceOf(String.class);
    }

    @Test
    public void testSaveAndLoad() {
        SharedObject.save(testFileUrl, 1);
        Assertions
            .assertThat(SharedObject.load(testFileUrl, Integer.class))
            .isEqualTo(1);
    }

    @Test
    public void testSaveAndLoadByInstanceName() {
        SharedObject.save(this, "test", 1);

        Assertions
            .assertThat(SharedObject.load(this, "test", Integer.class))
            .isEqualTo(1);
    }

    @Test
    public void testSaveAndLoadByInstanceNameWithDefaultValue() {
        SharedObject.save(this, "test", 1);

        Assertions
            .assertThat(SharedObject.load(this, "test", Integer.class, 2))
            .isEqualTo(1);

        Assertions
            .assertThat(SharedObject.load(this, "test2", Integer.class, 2))
            .isEqualTo(2);
    }

    @Test
    public void testSaveAndLoadByInstanceNameWithNotNullDefaultValue() {
        Assertions
            .assertThat(SharedObject.load(this, "test", 2))
            .isEqualTo(2);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testSaveAndLoadByInstanceNameWithNotDefaultValue() {
        Integer defaultValue = null;
        SharedObject.load(this, "test", defaultValue);
    }

    @Test
    public void testLoadList() {
        Assertions
            .assertThat(SharedObject.loadList(testFileUrl))
            .isEqualTo(new ArrayList<>());
    }

    @Test
    public void testSaveAndLoadList() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        SharedObject.save(testFileUrl, strings);
        Assertions
            .assertThat(SharedObject.loadList(testFileUrl))
            .isEqualTo(strings);
    }

    @Test
    public void testLoadListWithDefaultValue() {
        Assertions
            .assertThat(SharedObject.loadList(testFileUrl, new ArrayList<>()))
            .isEqualTo(new ArrayList<>());
    }

    @Test
    public void testSaveAndLoadListWithDefaultValue() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("1");
        SharedObject.save(testFileUrl, strings);
        Assertions
            .assertThat(SharedObject.loadList(testFileUrl, new ArrayList<>()))
            .isEqualTo(strings);
    }

    @Test
    public void testLoadMap() {
        Assertions
            .assertThat(SharedObject.loadMap(testFileUrl))
            .isEqualTo(new HashMap<String, String>());
    }

    @Test
    public void testSaveAndLoadMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test", "test");
        SharedObject.save(testFileUrl, map);
        Assertions
            .assertThat(SharedObject.loadMap(testFileUrl))
            .isEqualTo(map);
    }

    @Test
    public void testSaveAndLoadMapWithDefaultValue() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test", "test");
        SharedObject.save(testFileUrl, map);
        Assertions
            .assertThat(SharedObject.loadMap(testFileUrl, new HashMap<String, String>()))
            .isEqualTo(map);
    }

    @Test
    public void testLoadMapWithDefaultValue() {
        Assertions
            .assertThat(SharedObject.loadMap(testFileUrl, new HashMap<String, String>()))
            .isEqualTo(new HashMap<String, String>());
    }

    @Test
    public void testLoadMapWithDefaultValueIsNotEqualToLocalMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test", "test");
        SharedObject.save(testFileUrl, map);
        map.remove("test");
        Assertions
            .assertThat(SharedObject.loadMap(testFileUrl, new HashMap<String, String>()))
            .isNotEqualTo(map);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testRemoveNegativeTest() {
        testRemove("/testFile.ser");
    }

    @Test
    public void testRemoveLocalToProjectFileInRoot() {
        testRemove("./testFile.ser");
    }

    @Test
    public void testRemoveLocalToProjectFileInRoot2() {
        testRemove("testFile.ser");
    }

    @Test
    public void testRemoveLocalToProjectFile() {
        testRemove(testFileUrl);
    }

    @Test
    public void testRemoveLocalToSystemFile() {
        testRemove("/tmp/testFile.ser");
    }

    private void testRemove(String url) {
        SharedObject.save(url, new ArrayList<>());
        Assertions
            .assertThat(SharedObject.load(url, ArrayList.class))
            .isNotNull();

        SharedObject.remove(url);

        Assertions
            .assertThat(SharedObject.load(url, ArrayList.class))
            .isNull();
    }
}