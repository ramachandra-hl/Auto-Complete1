package test;

import com.fasterxml.jackson.databind.*;
import configurator.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import services.ScBackendService;
import utils.ReportPathUtils; // ✅ import the new utility

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchPricesForModulesTest extends TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(FetchPricesForModulesTest.class);

    @Test
    public void testGeneratePanelPriceSumReport() throws Exception {
        generatePanelPriceSumReport(projectID, token);
    }

    public static void generatePanelPriceSumReport(String projectId, Map<String, String> token) throws Exception {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId must not be null or empty");
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token must not be null or empty");
        }

        logger.info("🔍 Generating comprehensive pricing report for Project ID: {}", projectId);
        Response response = new ScBackendService().getDetailPriceData(projectId, token);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> items = mapper.readValue(response.getBody().asInputStream(), List.class);
        if (items == null || items.isEmpty()) {
            logger.warn("⚠️ No items found for project {}", projectId);
            return;
        }

        // --- Grouping logic (unchanged) ---
        List<List<Map<String, Object>>> groupedItems = new ArrayList<>();
        List<Map<String, Object>> currentGroup = new ArrayList<>();
        Integer lastSubId = null;

        for (Map<String, Object> item : items) {
            if (item.isEmpty()) {
                flushCurrentGroup(groupedItems, currentGroup);
                lastSubId = null;
                continue;
            }

            Integer subId = extractSubId(item.get("subID"));

            if (currentGroup.isEmpty()) {
                currentGroup.add(item);
            } else {
                if (shouldAddToCurrentGroup(lastSubId, subId)) {
                    currentGroup.add(item);
                } else {
                    flushCurrentGroup(groupedItems, currentGroup);
                    currentGroup.add(item);
                }
            }
            lastSubId = subId;
        }

        if (!currentGroup.isEmpty()) {
            groupedItems.add(new ArrayList<>(currentGroup));
        }

        // --- Determine dynamic columns ---
        int maxInternals = 0;
        for (List<Map<String, Object>> group : groupedItems) {
            Set<String> internalIds = new LinkedHashSet<>();
            for (Map<String, Object> item : group) {
                String internalId = (String) item.get("Internal_ID");
                Integer subId = extractSubId(item.get("subID"));
                if (internalId != null && !internalId.isEmpty() && subId == null) {
                    internalIds.add(internalId);
                }
            }
            if (internalIds.size() > maxInternals) maxInternals = internalIds.size();
        }

        // ✅ Use utility to create report file path
        String environment = Optional.ofNullable(System.getProperty("environment")).orElse("preProd");
        Path reportFile = ReportPathUtils.getTimestampedReportFile(
                "AllPriceBreakReport", environment, projectId, ".csv"
        );

        logger.info("📁 Report will be written to: {}", reportFile.toAbsolutePath());

        // --- Write CSV Report ---
        try (FileWriter writer = new FileWriter(reportFile.toFile())) {
            logger.info("✏️ Writing header to CSV");

            StringBuilder header = new StringBuilder("Module_Id,Module_Name,TotalPrice,CabinetPrice,TotalInternalPrice,HardwarePrice");
            for (int i = 1; i <= maxInternals; i++) {
                header.append(",Internal_ID").append(i).append(",Internal_Name").append(i).append(",Internal Price").append(i);
            }
            header.append("\n");
            writer.write(header.toString());
            for (List<Map<String, Object>> group : groupedItems) {
                if (group.isEmpty()) continue;
                logger.info("Aggregating all internals for the current group");
                String moduleId = String.valueOf(group.get(0).get("Module_ID"));
                String moduleName = String.valueOf(group.get(0).get("Module_Name"));
                double cabinetPrice = 0.0;
                double totalInternalPrice = 0.0;
                double hardwarePrice = 0.0;
                Map<String, Double> internalIdToPrice = new LinkedHashMap<>();
                Map<String, String> internalIdToName = new LinkedHashMap<>();
                logger.info("Calculation within current  group Started)");
                for (Map<String, Object> row : group) {
                    String internalId = (String) row.get("Internal_ID");
                    String internalName = (String) row.get("Internal_Name");
                    Object subIdObj = row.get("subID");
                    Integer subId = null;
                    if (subIdObj != null && !String.valueOf(subIdObj).isEmpty()) {
                        try {
                            subId = Integer.valueOf(subIdObj.toString());
                        } catch (Exception e) {
                            subId = null;
                        }
                    }
                    Double panelPrice = 0.0;
                    if (row.get("panelPrice") != null) {
                        try {
                            panelPrice = Double.valueOf(row.get("panelPrice").toString());
                        } catch (Exception e) {
                            panelPrice = 0.0;
                        }
                    }
                    Double totalPrice = 0.0;
                    if (row.get("totalPrice") != null) {
                        try {
                            totalPrice = Double.valueOf(row.get("totalPrice").toString());
                        } catch (Exception e) {
                            totalPrice = 0.0;
                        }
                    }
                    if ((internalId == null || internalId.isEmpty()) && subId == null) {
                        cabinetPrice += panelPrice;
                    }
                    if (internalId != null && !internalId.isEmpty() && subId == null) {
                        internalIdToPrice.put(internalId, internalIdToPrice.getOrDefault(internalId, 0.0) + panelPrice);
                        if (internalName != null) internalIdToName.put(internalId, internalName);
                    }
                    if (subId != null) {
                        hardwarePrice += totalPrice;
                    }
                }
                for (Double price : internalIdToPrice.values()) {
                    totalInternalPrice += price;
                }
                double totalPrice = cabinetPrice + totalInternalPrice + hardwarePrice;

                StringBuilder row = new StringBuilder();
                row.append(moduleId).append(",").append(moduleName).append(",");
                row.append(totalPrice).append(",").append(cabinetPrice).append(",").append(totalInternalPrice).append(",").append(hardwarePrice);
                int internalIdx = 1;
                for (Map.Entry<String, Double> entry : internalIdToPrice.entrySet()) {
                    String id = entry.getKey();
                    String name = internalIdToName.getOrDefault(id, "");
                    Double price = entry.getValue();
                    row.append(",").append(id).append(",").append(name).append(",").append(price);
                    internalIdx++;
                }
                for (; internalIdx <= maxInternals; internalIdx++) {
                    row.append(",,," );
                }
                row.append("\n");
                writer.write(row.toString());
            }

            logger.info("✅ Pricing report generated at: {}", reportFile.toAbsolutePath());

        } catch (IOException e) {
            logger.error("❌ Failed to write pricing report: {}", e.getMessage(), e);
        }
    }

    private static void flushCurrentGroup(List<List<Map<String, Object>>> groupedItems, List<Map<String, Object>> currentGroup) {
        if (!currentGroup.isEmpty()) {
            if (currentGroup.size() != 1) groupedItems.add(new ArrayList<>(currentGroup)); // skip skirting
            currentGroup.clear();
        }
    }

    private static Integer extractSubId(Object subIdObj) {
        if (subIdObj == null || String.valueOf(subIdObj).isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(subIdObj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean shouldAddToCurrentGroup(Integer lastSubId, Integer currentSubId) {
        return lastSubId == null || (currentSubId != null && currentSubId > lastSubId);
    }
}