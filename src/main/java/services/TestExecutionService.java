package services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.testng.TestNG;
import utils.Utilities;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class TestExecutionService {

    private final Executor executor;
    private static final Map<String, String> STATUS_MAP = new ConcurrentHashMap<>();

    public TestExecutionService(@Qualifier("testExecutor") Executor executor) {
        this.executor = executor;
    }

    public String runTestAsync(String xmlFile, List<Map<String, Object>> testData, Map<String, String> queryParams) {
        String runId = UUID.randomUUID().toString();
        STATUS_MAP.put(runId, "running");

        executor.execute(() -> {
            try {
                System.out.println("🚀 Starting async test run: " + runId);
                runTestInternal(xmlFile, testData, queryParams);
                STATUS_MAP.put(runId, "completed");
                System.out.println("✅ Completed run: " + runId);
            } catch (Exception e) {
                e.printStackTrace();
                STATUS_MAP.put(runId, "failed: " + e.getMessage());
            }
        });

        return runId;
    }

    private void runTestInternal(String xmlFile, List<Map<String, Object>> testData, Map<String, String> queryParams) throws Exception {
        // ✅ Load YAML data if needed
        Utilities.addToTestDataFromYml("configuration/HL_config.yml", new HashMap<>());
        Utilities.addToTestDataFromYml("configuration/HL_standardConfigurations.yml", new HashMap<>());

        // ✅ Update user config properties
        Utilities.updateProperties("input/userConfigurations.properties", queryParams);

        // ✅ Resolve XML file from classpath
        File xmlPath = Utilities.getXmlResource(xmlFile);
        if (xmlPath == null || !xmlPath.exists()) {
            throw new RuntimeException("XML file not found: " + xmlFile);
        }

        // ✅ Run TestNG suite
        TestNG testng = new TestNG();
        testng.setTestSuites(Collections.singletonList(xmlPath.getAbsolutePath()));
        testng.run();
    }

    public String getStatus(String runId) {
        return STATUS_MAP.getOrDefault(runId, "unknown");
    }
}
