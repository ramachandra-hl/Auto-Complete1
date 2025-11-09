package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class ReportPathUtils {

    // 💻 Local and Cloud Fallbacks (same as your config setup)
    private static final String LOCAL_BASE_FOLDER = System.getProperty("user.home") + "/Documents/AutoQA-Complete";
    private static final String CLOUD_BASE_FOLDER = System.getProperty("user.home") + "/AutoQA-Complete/input";



    public static Path getBaseStoragePath() {
        String envBase = System.getenv("CONFIG_BASE_DIR");

        if (envBase != null && !envBase.isEmpty()) {
            Path envPath = Paths.get(envBase);
            ensureDir(envPath);
            System.out.println("☁️ Using CONFIG_BASE_DIR: " + envPath.toAbsolutePath());
            return envPath;
        }

        Path localPath = Paths.get(LOCAL_BASE_FOLDER);
        if (Files.exists(localPath)) {
            System.out.println("💻 Using local base directory: " + localPath.toAbsolutePath());
            return localPath;
        }

        Path cloudPath = Paths.get(CLOUD_BASE_FOLDER);
        ensureDir(cloudPath);
        System.out.println("☁️ Using fallback cloud directory: " + cloudPath.toAbsolutePath());
        return cloudPath;
    }



    public static Path getReportDir(String reportType, String environment) {
        Path base = getBaseStoragePath();
        String dateFolder = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        Path reportDir = base.resolve(Paths.get("reports", reportType, environment, dateFolder));
        ensureDir(reportDir);
        return reportDir;
    }

    public static Path getTimestampedReportFile(String reportType, String environment, String filePrefix, String extension) {
        Path dir = getReportDir(reportType, environment);
        String timeStamp = new SimpleDateFormat("hh.mm.a").format(new Date());
        String fileName = filePrefix + "_" + timeStamp + extension;
        return dir.resolve(fileName);
    }

    /**
     * Ensure directory exists, creates if missing.
     */
    private static void ensureDir(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("📁 Created directory: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create directory: " + e.getMessage());
        }
    }
}

