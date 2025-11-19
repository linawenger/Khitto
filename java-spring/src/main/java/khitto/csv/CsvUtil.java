package khitto.csv;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvUtil {

    public static List<String[]> readAll(String path) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(line.split(";", -1));
            }
        } catch (IOException e) {
            // wenn es die Datei noch nicht gibt -> leere Liste
        }
        return rows;
    }

    public static void writeAll(String path, List<String[]> rows) {
        try {
            Path p = Path.of(path);
            // Ordner anlegen, falls nicht da
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }

            try (BufferedWriter bw = Files.newBufferedWriter(p)) {
                for (String[] r : rows) {
                    bw.write(String.join(";", r));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}