package reporter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;
import configurator.GlobalVariables;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentManager {
    private static ExtentReports extentReports;

    /**
     * Sets up Extent Report (Railway + Local compatible)
     *
     * @return ExtentReports instance
     */
    public static ExtentReports setupExtentReport() {
        if (extentReports != null) return extentReports;

        try {
            // ✅ Step 1: Detect environment base directory (local vs Railway)
            String baseDirEnv = System.getenv("CONFIG_BASE_DIR");
            String baseDir;

            if (baseDirEnv != null && !baseDirEnv.isEmpty()) {
                // Railway persistent volume (e.g. /mnt/data/AutoQA-Complete/input)
                baseDir = baseDirEnv;
                System.out.println("☁️ Using Railway base directory: " + baseDir);
            } else {
                // Local fallback
                baseDir = GlobalVariables.baseDir != null ? GlobalVariables.baseDir : System.getProperty("user.dir");
                System.out.println("💻 Using local base directory: " + baseDir);
            }

            // ✅ Step 2: Keep your existing file naming logic
            SimpleDateFormat todayDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
            Date date = new Date();
            String today = todayDateFormat.format(date);
            String todayTime = simpleDateFormat.format(date);
            GlobalVariables.currentExtentReportDir = today;

            // ✅ Step 3: Folder and report file path — unchanged
            String folderPath = "/reports/ExtentReports/" + today;
            String reportFilePath = folderPath + "/Report_" + todayTime + ".html";

            // ✅ Step 4: Create folder under correct base path (local or Railway)
            Path reportDirPath = Paths.get(baseDir + folderPath);
            if (!Files.exists(reportDirPath)) {
                Files.createDirectories(reportDirPath);
                System.out.println("📁 Created ExtentReports directory: " + reportDirPath.toAbsolutePath());
            }

            // ✅ Step 5: Initialize Extent Reporter with same filename
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(baseDir + reportFilePath);
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // ✅ Step 6: Apply existing configurations
            sparkReporter.config().setProtocol(Protocol.HTTPS);
            sparkReporter.config().setReportName("Test Report");
            sparkReporter.config().setDocumentTitle("Test Suite Execution");
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setTimelineEnabled(true);
            sparkReporter.config().setEncoding("utf-8");

            // ✅ Step 7: System information
            extentReports.setSystemInfo("OS Name", System.getProperty("os.name"));
            extentReports.setSystemInfo("Host", getHostName());
            extentReports.setSystemInfo("Executed By", "Sagar Dhal");
            extentReports.setSystemInfo("Browser", GlobalVariables.configProperties.getProperty("browserName", "N/A"));

            // ✅ Step 8: Success log
            System.out.println("✅ Extent report initialized at: " + baseDir + reportFilePath);

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Extent Report: " + e.getMessage());
            e.printStackTrace();
        }

        return extentReports;
    }

    private static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }
}
