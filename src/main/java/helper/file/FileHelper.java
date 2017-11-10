package helper.file;

public class FileHelper {
    public static String getFilePath(String fileUrl) {
        return FileHelper.class.getClassLoader().getResource(fileUrl).getPath();
    }
}
