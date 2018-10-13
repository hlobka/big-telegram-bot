package external;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExternalJob {
    //TBD
    private final String executableFolder;
    private final String executableFile;
    private final String reportPath;
    private final List<String> commandLines;
    public final String groupId;

    public ExternalJob(String executableFolder, String executableFile, List<String> commandLines, String reportPath, String groupId) {
        this.executableFolder = executableFolder;
        this.executableFile = executableFile;
        this.commandLines = commandLines;
        this.reportPath = reportPath;
        this.groupId = groupId;
    }

    public void run(Consumer<String> errorConsumer) throws IOException, InterruptedException {
        for (String commandLine : commandLines) {
            ruCommandLine(commandLine, errorConsumer);
        }
    }

    private void ruCommandLine(String command, Consumer<String> errorConsumer) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        traceLogs(System.out, process.getInputStream(), errorConsumer);
        traceLogs(System.err, process.getErrorStream(), errorConsumer);

        process.waitFor(30, TimeUnit.SECONDS);
    }

    private void traceLogs(PrintStream out, InputStream inputStream, Consumer<String> errorConsumer) throws IOException {
        InputStream is = inputStream;
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            out.println(line);
            if(line.contains("ERROR")){
                errorConsumer.accept(line);
            } else if(out == System.err){
                errorConsumer.accept(line);
            }
        }
    }
}
