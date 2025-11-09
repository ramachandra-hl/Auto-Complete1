package autoqa.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.testng.TestNG;
import utils.Utilities;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

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
    private static final Map<String, Map<String, Object>> RUN_STATUS = new ConcurrentHashMap<>();

    private final Executor executor;

    // ==========================================================
    // ✅ ThreadPool for async execution
    // ==========================================================
    public TestController() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(10);
        exec.setThreadNamePrefix("AsyncRun-");
        exec.initialize();
        this.executor = exec;
    }

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
    // ✅ List available XML test suites
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
    // ✅ Run Test Suite (Async for Railway)
    // ==========================================================
    @PostMapping("/run")
    public ResponseEntity<?> runTestSuiteAsync(
            @RequestParam String xmlFile,
            @RequestBody(required = false) List<Map<String, Object>> testData,
            @RequestParam(required = false) Map<String, String> queryParams
    ) {
        try {
            String runId = UUID.randomUUID().toString();
            RUN_STATUS.put(runId, Map.of(
                    "status", "running",
                    "message", "Test execution started",
                    "startedAt", new Date().toString()
            ));

            executor.execute(() -> {
                try {
                    Map<String, Object> result = executeTest(xmlFile, testData, queryParams);
                    result.put("status", "completed");
                    result.put("finishedAt", new Date().toString());
                    RUN_STATUS.put(runId, result);
                } catch (Exception e) {
                    e.printStackTrace();
                    RUN_STATUS.put(runId, Map.of(
                            "status", "failed",
                            "message", e.getMessage(),
                            "finishedAt", new Date().toString()
                    ));
                }
            });

            return ResponseEntity.ok(Map.of(
                    "status", "started",
                    "runId", runId,
                    "message", "Test started asynchronously",
                    "checkStatusAt", "/scPro/status/" + runId
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ==========================================================
    // ✅ Check Status of Async Run
    // ==========================================================
    @GetMapping("/status/{runId}")
    public ResponseEntity<?> getRunStatus(@PathVariable String runId) {
        Map<String, Object> status = RUN_STATUS.get(runId);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "error",
                    "message", "Run ID not found: " + runId
            ));
        }
        return ResponseEntity.ok(status);
    }

    // ==========================================================
    // ✅ Actual synchronous logic for Test Execution
    // ==========================================================
    private Map<String, Object> executeTest(
            String xmlFile,
            List<Map<String, Object>> testData,
            Map<String, String> queryParams
    ) throws Exception {

        File xmlFilePath = resolveXmlFile(xmlFile);
        if (xmlFilePath == null || !xmlFilePath.exists()) {
            throw new FileNotFoundException("XML file NOT found: " + xmlFile);
        }

        // Inject test data
        if (testData != null && !testData.isEmpty()) {
            String jsonString = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(testData);
            System.setProperty("testDataJson", jsonString);
        } else {
            System.clearProperty("testDataJson");
        }

        // Merge configs
        Map<String, String> mergedProps = buildEffectiveProps(queryParams);
        Utilities.updateProperties(USER_PROPERTIES_FILE, mergedProps);
        String envUsed = mergedProps.getOrDefault("environment", environment);

        // Run TestNG Suite
        System.out.println("🧪 Running suite: " + xmlFilePath.getAbsolutePath());
        TestNG testng = new TestNG();
        testng.setTestSuites(Collections.singletonList(xmlFilePath.getAbsolutePath()));
        testng.run();

        // Identify CSV folders
        List<String> generatedCsvFolders = new ArrayList<>();
        if (xmlFile.equalsIgnoreCase("ModulesWithPriceAndErrorReport.xml")) {
            generatedCsvFolders = Arrays.asList("ErrorReports", "PriceReports");
        } else if (xmlFile.equalsIgnoreCase("ApplyInternalsToModuleAndGenerateReports.xml")) {
            generatedCsvFolders = Collections.singletonList("AllPriceBreakReport");
        }

        // Fetch reports
        Map<String, Path> csvMap = findLatestCsvsForFolders(generatedCsvFolders, envUsed);
        Path latestHtmlReport = getLatestExtentReport();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Test completed successfully");
        response.put("xmlFile", xmlFile);
        response.put("environmentUsed", envUsed);

        if (latestHtmlReport != null)
            response.put("htmlReportUrl", "/scPro/reports/view/latest");

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

        return response;
    }

    // ==========================================================
    // ✅ Serve Latest Extent Report
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
    // ✅ Serve Latest CSV Report
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
    // ✅ Get Latest Extent Report
    // ==========================================================
    private Path getLatestExtentReport() {
        try {
            Path reportsBase = getBaseReportsDir().resolve("ExtentReports");

            if (!Files.exists(reportsBase)) return null;

            Optional<Path> latestDateFolder = Files.list(reportsBase)
                    .filter(Files::isDirectory)
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            if (latestDateFolder.isEmpty()) return null;

            Optional<Path> latestHtml = Files.list(latestDateFolder.get())
                    .filter(p -> p.toString().endsWith(".html"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));

            return latestHtml.orElse(null);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==========================================================
    // ✅ Find Latest CSV Reports
    // ==========================================================
    private Map<String, Path> findLatestCsvsForFolders(List<String> folderTypes, String env) {
        Map<String, Path> latestCsvs = new LinkedHashMap<>();
        for (String folderType : folderTypes) {
            try {
                Path reportsDir = Paths.get("reports", folderType, env);
                String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                Path dateFolder = reportsDir.resolve(today);
                if (!Files.exists(dateFolder)) {
                    System.out.println("⚠️ Folder missing: " + dateFolder);
                    continue;
                }
                Optional<Path> latestCsv = Files.list(dateFolder) .filter(f -> f.toString().endsWith(".csv")) .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
                latestCsv.ifPresent(path -> { latestCsvs.put(folderType, path); System.out.println("✅ Found latest CSV for " + folderType + ": " + path); });
            } catch (Exception e) {
                System.err.println("❌ Error reading folder: " + folderType);
                e.printStackTrace(); } } return latestCsvs;
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
        defaults.put("customerType", "HL");

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
    // ✅ Resolve XML File (Classpath or Temp)
    // ==========================================================
    private File resolveXmlFile(String xmlFile) throws IOException {
        return Utilities.getXmlResource(xmlFile);
    }
}
