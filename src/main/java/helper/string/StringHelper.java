package helper.string;

import helper.file.FileHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static String getRegString(String text, String regex) {
        return getRegString(text, regex, 1);
    }

    public static boolean hasRegString(String text, String regex, int group) {
        try {
            return !getRegString(text, regex, group).isEmpty();
        } catch (IndexOutOfBoundsException e){
            return false;
        }
    }

    public static List<String> getAllRegString(String text, String regex, int group) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String values = matcher.group(group);
            result.add(values);
        }
        return result;
    }

    public static String getRegString(String text, String regex, int group) {
        int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        return getRegString(text, regex, group, flags);
    }

    public static String getRegString(String text, String regex, int group, int flags) {
        Pattern pattern = Pattern.compile(/*"(?U)" + */regex, flags);
        Matcher matcher = pattern.matcher(text);
        String values = "";
        if (matcher.find()) {
            values = matcher.group(group);
        }
        return values;
    }

    public static String getFileAsString(String fileUrl) throws IOException {
        try {
            File fileDir = new File(FileHelper.getFilePath(fileUrl));
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(fileDir), "UTF8"));
            String str;
            StringBuilder result = new StringBuilder();
            String newLine = "\n";
            while ((str = in.readLine()) != null) {
                result.append(str).append(newLine);
            }
            in.close();
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getFileAsString2(String fileUrl) throws IOException {
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

    public static String getAsSimpleCrypted(String value) {
        String result = "";
        for (char b : value.toCharArray()) {
            result += (char)((int)b + 1);
        }
        return result;
    }

    public static String getAsSimpleDeCrypted(String value) {
        String result = "";
        for (char b : value.toCharArray()) {
            result += (char)((int)b - 1);
        }
        return result;
    }

    public static String getIssueIdFromSvnRevisionComment(String comment) {
        return getRegString(comment, "(\\w+-\\d+).*", 1);
    }

    public static List<String> getIssueIdsFromSvnRevisionComment(String comment) {
        List<String> result = new ArrayList<>();
        while (hasRegString(comment, "(\\w+-\\d+).*", 1)){
            String issueId = getIssueIdFromSvnRevisionComment(comment);
            comment = comment.replace(issueId, "");
            result.add(issueId);
        }
        return result;
    }

    public static String getBar(List<Double> list, List<String> valueCharacterSymbols, int barLength) {
        assert list.size() == valueCharacterSymbols.size();
        Double sum = list.stream().mapToDouble(Double::doubleValue).sum();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Double value = list.get(i);
            String valueSymbol = valueCharacterSymbols.get(i);
            int openedIssuesAmount = Math.round((float) Math.round((value / sum) * barLength));
            result.append(repeat(valueSymbol, openedIssuesAmount));
        }
        return result.toString();
    }

    public static String repeat(String s, int n) {
        return String.join("", Collections.nCopies(n, s));
    }
}
