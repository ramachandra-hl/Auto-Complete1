package autoqa.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

import static utils.Utilities.updateProperties;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/roster")
public class LeadController {

    @Value("${server.port:8080}")
    private String serverPort;

    @GetMapping("config")
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
        config.put("apiBaseUrl", proto + "://" + hostHeader);
        return config;
    }

    @PostMapping("/LeadCreation")
    public Map<String, Object> runLeadCreationTest(@RequestBody Map<String, String> request) {
        System.out.println("Received Lead Creation Request: " + request);

        // Update test config properties
        updateProperties("userConfigurations.properties", new HashMap<>(Map.of(
                "customerName", request.get("customerName"),
                "environment", request.get("environment"),
                "customerType", request.get("customerType"),
                "totalLeadsToCreate", request.get("noOfLeads"),
                "userSelectedCityProperty", request.get("userSelectedCityProperty"),
                "mobileNoStarting2digitPrefix", request.get("mobileNoStarting2digitPrefix"),
                "showroomId", request.getOrDefault("showroomId", null)
        )));

        Map<String, Object> response = new HashMap<>();
        List<String> allOutputs = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        try {
            // --- Setup TestNG suite
            XmlSuite suite = new XmlSuite();
            suite.setName("LeadSuite");

            XmlTest test = new XmlTest(suite);
            test.setName("LeadTest");

            XmlClass testClass = new XmlClass("test.LeadCreationTest");
            testClass.getIncludedMethods().add(new XmlInclude("testCreateBulkLeads"));
            test.setXmlClasses(Collections.singletonList(testClass));

            TestNG testng = new TestNG();
            testng.setXmlSuites(Collections.singletonList(suite));
            System.out.println("testng.setXmlSuites(Collections.singletonList(suite)); ");
            // --- Custom listener to capture success/failure
            TestListenerAdapter tla = new TestListenerAdapter() {

                @Override
                public void onTestSuccess(ITestResult tr) {
                    response.put("status", "PASS");
                    response.put("testName", tr.getName());
                    ITestContext context = tr.getTestContext();

                    if (context != null) {
                        Map<String, Object> contextData = new LinkedHashMap<>();
                        for (String key : context.getAttributeNames()) {
                            Object value = context.getAttribute(key);
                            if (value instanceof List<?>) {
                                contextData.put(key, value);
                                allOutputs.addAll((List<String>) value);
                            } else {
                                contextData.put(key, value);
                            }
                        }
                        response.put("output", contextData);
                    }
                }

                @Override
                public void onTestFailure(ITestResult tr) {
                    response.put("status", "FAIL");
                    response.put("testName", tr.getName());
                    response.put("error", tr.getThrowable() != null ? tr.getThrowable().getMessage() : "Unknown error");

                    ITestContext context = tr.getTestContext();
                    if (context != null) {
                        for (String key : context.getAttributeNames()) {
                            Object value = context.getAttribute(key);
                            if (value instanceof List<?>) {
                                failures.addAll((List<String>) value);
                                allOutputs.addAll((List<String>) value);
                            }
                        }
                    }
                }

                @Override
                public void onConfigurationFailure(ITestResult tr) {
                    System.out.println("❌ Configuration Failure in: " + tr.getName());
                    if (tr.getThrowable() != null) {
                        tr.getThrowable().printStackTrace(System.out);
                    }
                }


                @Override
                public void onTestSkipped(ITestResult tr) {
                    response.put("status", "SKIPPED");
                    response.put("testName", tr.getName());
                    failures.add("⚠️ Test skipped: " + (tr.getThrowable() != null ? tr.getThrowable().getMessage() : "No reason"));
                }
            };

            testng.addListener(tla);
            testng.run();

            response.put("outputs", allOutputs);
            response.put("failures", failures);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }

        return response;
    }
}
