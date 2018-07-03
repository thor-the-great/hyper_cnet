package contentnet.drivers;

import contentnet.GlobalProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static contentnet.GlobalProperties.*;
import static java.nio.file.Files.walk;

public class DriverProgressStat {

    public static void main(String[] args) {
        DriverProgressStat driver = new DriverProgressStat();
        driver.doWork();
    }

    void doWork() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(getSetting("stat.current_progress.file")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //processed
        writer.println("---Categories processed---");
        String processedFolderName = GlobalProperties.getSetting("result.processed.folder");
        AtomicInteger count = new AtomicInteger();
        try (Stream<Path> paths = walk(Paths.get(processedFolderName), 1)) {
            PrintWriter finalWriter = writer;
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                String fileNameString = filePath.getFileName().toString();
                String categoryName = fileNameString.substring(fileNameString.indexOf("_") + 1, fileNameString.indexOf("."));
                finalWriter.println(categoryName);
                count.getAndIncrement();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.println("------- count = " + count + " -------");
        writer.println("---------------------------");
        //raw graphs are ready
        writer.println("-----Raw graphs-----");
        String folderName = GlobalProperties.getSetting("result.raw_graph.folder");
        AtomicInteger count2 = new AtomicInteger();
        try (Stream<Path> paths = walk(Paths.get(folderName), 1)) {
            PrintWriter finalWriter = writer;
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                String fileNameString = filePath.getFileName().toString();
                String categoryName = fileNameString.substring(fileNameString.indexOf("_") + 1, fileNameString.indexOf("."));
                finalWriter.println(categoryName);
                count2.getAndIncrement();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.println("------- count = " + count2 + " -------");
        writer.println("---------------------------");
        //raw graphs are ready
        writer.println("-----Headwords ready-----");
        folderName = GlobalProperties.getSetting("result.hw_collected.folder");
        try (Stream<Path> paths = walk(Paths.get(folderName), 1)) {
            PrintWriter finalWriter = writer;
            paths.filter(Files::isRegularFile)
                    .filter(filePath ->filePath.getFileName().toString().endsWith(".txt") && filePath.getFileName().toString().startsWith("hwByCategory_"))
                    .forEach(filePath -> {
                String fileNameString = filePath.getFileName().toString();
                String categoryName = fileNameString.split("_")[1];
                finalWriter.println(categoryName);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.println("---------------------------");

        writer.close();
    }
}
