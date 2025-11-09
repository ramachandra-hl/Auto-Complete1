package autoqa.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.testng.TestNG;
import utils.Utilities;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/scPro")
public class TestController {

    @Value("${app.environment:local}")
    private String environment;

    @Value("${xml.folder:src/main/resources/xmlFiles}")
    private String xmlFolder;

    private static final String USER_PROPERTIES_FILE = "userConfigurations.properties";
    private static final String DEFAULT_REPORT_BASE = "reports";
    private static final String CLOUD_REPORT_BASE = "/mnt/data/AutoQA-Complete/input/reports";

    // ==========================================================
    // ✅ Ensure base reports folder exists
    // ==========================================================
    @PostConstruct
    public void initReportsFolder() {
        try {
            Path reportsBase = getBaseReportsDir();
            if (!Files.exists(reportsBase)) {
                Files.createDirectories(reportsBase);
                System.out.println("📁 Created base reports directory: " + reportsBase.toAbsolutePath());
            } else {
                System.out.println("✅ Using existing reports directory: " + reportsBase.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create reports directory: " + e.getMessage());
        }
    }

    // ==========================================================
    // ✅ Dynamic Base Directory (Local / Railway)
    // ==========================================================
    private Path getBaseReportsDir() {
        String baseDir = System.getenv("CONFIG_BASE_DIR");
        if (baseDir != null && !baseDir.isEmpty()) {
            return Paths.get(baseDir, "reports");
        }
        return Paths.get(DEFAULT_REPORT_BASE);
    }

    // ==========================================================
    // ✅ API Config Endpoint
    // ==========================================================
    @GetMapping("/config")
    public Map<String, String> getConfig(HttpServletRequest request) {
        Map<String, String> config = new HashMap<>();
        String proto = Optional.ofNullable(request.getHeader("X-Forwarded-Proto")).orElse(request.getScheme());
        String hostHeader = Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
                .orElse(Optional.ofNullable(request.getHeader("Host")).orElse(""));
        if (hostHeader.isEmpty()) {
            int port = request.getServerPort();
            String portPart = (port == 80 || port == 443) ? "" : ":" + port;
            hostHeader = request.getServerName() + portPart;
        }
        config.put("apiBaseUrl", proto + "://" + hostHeader + "/scPro");
        return config;
    }

    // ==========================================================
    // ✅ Get all test XMLs
    // ==========================================================
    @GetMapping("/test/suites")
    public ResponseEntity<List<String>> getAllXmlFiles() {
        try {
            List<String> xmlFiles = Utilities.listXmlFilesFromClasspath("xmlFiles");
            if (xmlFiles.isEmpty()) {
                return ResponseEntity.ok(List.of("No XML files found in classpath"));
            }
            return ResponseEntity.ok(xmlFiles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Error reading XML files: " + e.getMessage()));
        }
    }


    // ==========================================================
    // ✅ Run TestNG Suite & Return Reports
    // ==========================================================
    @PostMapping("/run")
    public ResponseEntity<?> runTestSuite(
            @RequestParam String xmlFile,
            @RequestBody(required = false) List<Map<String, Object>> testData,
            @RequestParam(required = false) Map<String, String> queryParams
    ) {
        try {
            File xmlFilePath = resolveXmlFile(xmlFile);
            if (xmlFilePath == null || !xmlFilePath.exists()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "XML file NOT found: " + xmlFile
                ));
            }

            // ✅ Inject test data into system property
            if (testData != null && !testData.isEmpty()) {
                String jsonString = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(testData);
                System.setProperty("testDataJson", jsonString);
            } else {
                System.clearProperty("testDataJson");
            }

            // ✅ Merge user configs
            Map<String, String> mergedProps = buildEffectiveProps(queryParams);
            Utilities.updateProperties(USER_PROPERTIES_FILE, mergedProps);
            String envUsed = mergedProps.getOrDefault("environment", environment);

            // ✅ Run TestNG Suite
            System.out.println("🧪 Running suite: " + xmlFilePath.getAbsolutePath());
            TestNG testng = new TestNG();
            testng.setTestSuites(Collections.singletonList(xmlFilePath.getAbsolutePath()));
            testng.run();

            // ✅ Identify CSV folders based on XML
            List<String> generatedCsvFolders = new ArrayList<>();
            if (xmlFile.equalsIgnoreCase("ModulesWithPriceAndErrorReport.xml")) {
                generatedCsvFolders = Arrays.asList("ErrorReports", "PriceReports");
            } else if (xmlFile.equalsIgnoreCase("ApplyInternalsToModuleAndGenerateReports.xml")) {
                generatedCsvFolders = Collections.singletonList("AllPriceBreakReport");
            }

            // ✅ Fetch latest reports
            Map<String, Path> csvMap = findLatestCsvsForFolders(generatedCsvFolders, envUsed);
            Path latestHtmlReport = getLatestExtentReport();

            // ✅ Build response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "success");
            response.put("message", "Test completed successfully");
            response.put("xmlFile", xmlFile);
            response.put("environmentUsed", envUsed);

            if (latestHtmlReport != null)
                response.put("htmlReportUrl", "/scPro/reports/view/latest");

            // ✅ Generate descriptive CSV report names
            if (!csvMap.isEmpty()) {
                List<Map<String, String>> csvLinks = new ArrayList<>();
                csvMap.forEach((folder, path) -> {
                    String readableName;
                    if (folder.equalsIgnoreCase("ErrorReports")) {
                        readableName = "Error Report - " + path.getFileName();
                    } else if (folder.equalsIgnoreCase("PriceReports")) {
                        readableName = "Price Report - " + path.getFileName();
                    } else if (folder.equalsIgnoreCase("AllPriceBreakReport")) {
                        readableName = "All Price Break Report - " + path.getFileName();
                    } else {
                        readableName = folder + " - " + path.getFileName();
                    }

                    csvLinks.add(Map.of(
                            "name", readableName,
                            "url", "/scPro/reports/csv/view/" + folder + "/" + envUsed
                    ));
                });
                response.put("csvReports", csvLinks);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }


    // ==========================================================
    // ✅ Serve Latest ExtentReport (inline view)
    // ==========================================================
    @GetMapping("/reports/view/latest")
    public ResponseEntity<Resource> viewLatestExtentReport() {
        try {
            Path latestReport = getLatestExtentReport();
            if (latestReport == null || !Files.exists(latestReport))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            Resource resource = new FileSystemResource(latestReport.toFile());
            System.out.println("📂 Serving Extent HTML Report: " + latestReport);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + latestReport.getFileName())
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================================
    // ✅ Serve Latest CSV Report (download)
    // ==========================================================
    @GetMapping("/reports/csv/view/{folderType}/{env}")
    public ResponseEntity<Resource> viewLatestCsvReport(
            @PathVariable String folderType,
            @PathVariable String env
    ) {
        try {
            Path baseDir = getBaseReportsDir().resolve(folderType).resolve(env);
            String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            Path dateFolder = baseDir.resolve(today);

            if (!Files.exists(dateFolder)) return ResponseEntity.notFound().build();

            Optional<Path> latestCsv = Files.list(dateFolder)
                    .filter(f -> f.toString().endsWith(".csv"))
                    .max(Comparator.comparingLong(f -> f.toFile().lastModified()));

            if (latestCsv.isEmpty()) return ResponseEntity.notFound().build();

            Path csvPath = latestCsv.get();
            Resource resource = new FileSystemResource(csvPath.toFile());
            System.out.println("📄 Serving CSV report: " + csvPath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvPath.getFileName() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================================
    // ✅ Locate Latest Extent Report (Matches ExtentManager)
    // ==========================================================
    private Path getLatestExtentReport() {
        try {
            Path reportsBase = getBaseReportsDir().resolve("ExtentReports");

            if (!Files.exists(reportsBase)) {
                System.out.println("⚠️ ExtentReports folder not found: " + reportsBase);
                return null;
            }

            Optional<Path> latestDateFolder = Files.list(reportsBase)
                    .filter(Files::isDirectory)
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            if (latestDateFolder.isEmpty()) return null;

            Optional<Path> latestHtml = Files.list(latestDateFolder.get())
                    .filter(p -> p.toString().endsWith(".html"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            if (latestHtml.isEmpty()) return null;

            System.out.println("✅ Latest ExtentReport found: " + latestHtml.get());
            return latestHtml.get();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==========================================================
    // ✅ Find latest CSVs for folders
    // ==========================================================
    private Map<String, Path> findLatestCsvsForFolders(List<String> folderTypes, String env) {
        Map<String, Path> latestCsvs = new LinkedHashMap<>();
        for (String folderType : folderTypes) {
            try {
                Path reportsDir = getBaseReportsDir().resolve(folderType).resolve(env);
                String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                Path dateFolder = reportsDir.resolve(today);

                if (!Files.exists(dateFolder)) continue;

                Optional<Path> latestCsv = Files.list(dateFolder)
                        .filter(f -> f.toString().endsWith(".csv"))
                        .max(Comparator.comparingLong(f -> f.toFile().lastModified()));

                latestCsv.ifPresent(path -> latestCsvs.put(folderType, path));

            } catch (Exception e) {
                System.err.println("❌ Error reading folder: " + folderType);
                e.printStackTrace();
            }
        }
        return latestCsvs;
    }

    // ==========================================================
    // ✅ Merge default + custom properties
    // ==========================================================
    private Map<String, String> buildEffectiveProps(Map<String, String> queryParams) {
        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put("isCarcassRequired", "false");
        defaults.put("isShutterRequired", "false");
        defaults.put("isAccessoriesRequired", "false");
        defaults.put("isHandleRequired", "false");
        defaults.put("isInternalRequired", "false");
        defaults.put("isQPModules", "false");
        defaults.put("environment", "local");
        defaults.put("projectID", "");
        defaults.put("modulesInRoom", "10");

        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (key.startsWith("is")) {
                    String val = (value == null) ? "false" : value.trim().toLowerCase();
                    value = Set.of("true", "1", "yes", "on").contains(val) ? "true" : "false";
                }
                defaults.put(key, value);
            });
        }
        return defaults;
    }

    // ==========================================================
    // ✅ Resolve XML Suite File (Classpath / FileSystem)
    // ==========================================================
    private File resolveXmlFile(String xmlFile) throws IOException {
     return  Utilities.getXmlResource(xmlFile);
    }

}
