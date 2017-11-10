package helper.string;

import helper.file.FileHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static String getRegString(String text, String regex) {
        return  getRegString(text, regex, 1);
    }

    public static String getRegString(String text, String regex, int group) {
        Pattern pattern = Pattern.compile("(?U)" + regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        String values = "";
        if (matcher.find()) {
            values = matcher.group(group);
        }
        return values;
    }

    public static String getFileAsString(String fileUrl) throws IOException {
        String filePath = FileHelper.getFilePath(fileUrl);
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
