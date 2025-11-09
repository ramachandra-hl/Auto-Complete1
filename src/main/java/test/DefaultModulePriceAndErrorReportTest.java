package test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import models.ModuleInputData;

import org.testng.annotations.Test;
import utils.DataProviderUtil;
import utils.PropertiesReader;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


import static utils.Utilities.formatCurrentDate;

public class DefaultModulePriceAndErrorReportTest extends RoomModuleHandler {
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    String isInFillDataRequired = propertiesReader.getIsInFillDataRequired();
    String reportFilePath = "";
    int noOfModule = 1;

    @Test(enabled = false, dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    void processMultipleRoomTypes(Map<String, String> roomDetailsMap) throws Exception {

        ModuleInputData moduleInputData = new ModuleInputData(roomDetailsMap);
        String roomTypeFromInput = moduleInputData.getRoomType();
        String roomType = (roomTypeFromInput != null)
                ? roomTypeFromInput.substring(0, 1).toUpperCase() + roomTypeFromInput.substring(1).toLowerCase()
                : null;

        roomConfigurationParams.put("roomType", roomType);
        roomConfigurationParams.put("name", moduleInputData.getRoomName());

        Map<String, String> categoryInfo = synapseService.getCategoryDetails(moduleInputData.getModuleId());
        String categoryId = categoryInfo.get("categoryId");
        String subCategoryId = categoryInfo.get("subCategoryId");

        if (roomType == null) {
            roomType = categoryInfo.get("roomType");
        }

        Map<String, String> zoneInfo = new HashMap<>();
        zoneInfo.put("categoryId", categoryId);
        zoneInfo.put("subCategoryId", subCategoryId);
        zoneInfo.put("categoryName", categoryInfo.get("categoryName"));
        zoneInfo.put("subCategoryName", categoryInfo.get("subCategoryName"));
        zoneInfo.put("moduleName", moduleInputData.getModuleName());

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());
        String modulePrice = null;
        Thread.sleep(5000);
        Response getItemsSummary = scBackendService.getItems(projectID, token);

        List<Map<String, Object>> projectSummary = getItemsSummary.jsonPath().getList("projectSummary.rooms");

        for (Map<String, Object> room : projectSummary) {
            if (room.get("roomId").toString().equalsIgnoreCase(currentRoomId)) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) ((Map<String, Object>) room.get("fittedFurniture")).get("data");

                for (Map<String, Object> furnitureItem : data) {
                    if (furnitureItem.get("name").toString().equalsIgnoreCase(zoneInfo.get("subCategoryName"))) {
                        List<Map<String, Object>> subCategoriesData = (List<Map<String, Object>>) furnitureItem.get("subCategories");

                        for (Map<String, Object> subCat : subCategoriesData) {
                            List<Map<String, Object>> zones = (List<Map<String, Object>>) subCat.get("zones");

                            for (Map<String, Object> zone : zones) {
                                if (zone.get("objectId").toString().equalsIgnoreCase(currentZoneId)) {
                                    System.out.println("📍 Current Zone ID: " + currentZoneId);
                                    List<Map<String, Object>> modules = (List<Map<String, Object>>) zone.get("modules");
                                    if (modules != null && !modules.isEmpty()) {
                                        Object addedModulePriceInCurrentZone = modules.get(0).get("price");
                                        modulePrice = addedModulePriceInCurrentZone.toString();
                                        System.out.println("addedModulePriceInCurrentZone -> " + addedModulePriceInCurrentZone);
                                    } else {
                                        System.out.println("No modules found in this zone.");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Response getPricingError = scBackendService.getPricingError(projectID, token);
        List<Map<String, String>> ErrorDetails = extractErrorDetails(getPricingError.getBody().asString(), currentRoomId, currentZoneId, moduleInputData.getModuleId());

        String[] resultData = {Integer.toString(noOfModule++), zoneInfo.get("categoryName"), zoneInfo.get("subCategoryName"), moduleInputData.getModuleId(), moduleInputData.getModuleName(), modulePrice, extractValuesByKeyAsString(ErrorDetails, "errorType"), extractValuesByKeyAsString(ErrorDetails, "errorMessage")};
        utilities.appendTestResults(resultData, reportFilePath);

    }

    @Test(priority = 0)
    public void computeLatestAndUpdateToLatestProjectPriceTest() throws InterruptedException {
        Thread.sleep(30000);
        Response computeResponse = scBackendService.computeLatestProjectPrice(projectID, token);
        System.out.println("computeLatestProjectPrice response: " + computeResponse.asPrettyString());
        Response updateResponse = scBackendService.updateToLatestProjectPrice(projectID, token);
        System.out.println("updateToLatestProjectPrice response: " + updateResponse.asPrettyString());
    }

    @Test(priority = 1)
    public void extractAllModulePriceFromProject() throws Exception {
        Response getItemsSummary = scBackendService.getItems(projectID, token);
        String jsonResponse = getItemsSummary.getBody().asString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(jsonResponse, new TypeReference<>() {
        });
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) ((Map<String, Object>) data.get("projectSummary")).get("rooms");
        if (rooms == null) return;


        // Step 1: Scan for max accessory count
        int maxAccessoryCount = 0;
        int multipartShutterCount = 0;
        for (Map<String, Object> room : rooms) {
            Map<String, Object> fittedFurniture = (Map<String, Object>) room.get("fittedFurniture");
            if (fittedFurniture == null) continue;
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) fittedFurniture.get("data");
            if (dataList == null) continue;
            for (Map<String, Object> subCategory : dataList) {
                List<Map<String, Object>> subCategories = (List<Map<String, Object>>) subCategory.get("subCategories");
                if (subCategories == null) continue;
                for (Map<String, Object> subCat : subCategories) {
                    List<Map<String, Object>> zones = (List<Map<String, Object>>) subCat.get("zones");
                    if (zones == null) continue;
                    for (Map<String, Object> zone : zones) {
                        List<Map<String, Object>> modules = (List<Map<String, Object>>) zone.get("modules");
                        if (modules == null || modules.isEmpty()) continue;
                        Map<String, Object> module = modules.get(0);
                        List<Map<String, Object>> accessories = (List<Map<String, Object>>) module.get("accessories");
                        if (accessories != null && accessories.size() > maxAccessoryCount) {
                            maxAccessoryCount = accessories.size();
                        }
                    }
                }
            }
        }

        // Step 2: Build header
        List<String> reportHeaders = new ArrayList<>(Arrays.asList(
                "s.no", "moduleId", "moduleName", "ModuleDesc", "Price", "Dimension", "Cabinet", "CabinetColor","HandleName","HandleDesign",
                "ShutterCoreName", "ShutterFinishName", "ShutterFinishCategory", "ShutterColor", "ShutterDesignType", "ShutterSubDesignName"
        ));
        if ("true".equalsIgnoreCase(isInFillDataRequired)) {
            reportHeaders.addAll(Arrays.asList("InfillMaterialName", "InfillColor"));
        }
        if ("true".equalsIgnoreCase(isInternals)) {
            reportHeaders.addAll(Arrays.asList("internalName", "section"));
        }

        for (int i = 1; i <= maxAccessoryCount; i++) {
            reportHeaders.add("Accessory" + i);
            reportHeaders.add("Price" + i);
        }

        // Step 3: Collect and write rows
        for (Map<String, Object> room : rooms) {
            Map<String, Object> fittedFurniture = (Map<String, Object>) room.get("fittedFurniture");
            if (fittedFurniture == null) continue;
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) fittedFurniture.get("data");
            if (dataList == null) continue;
            for (Map<String, Object> subCategory : dataList) {
                List<Map<String, Object>> subCategories = (List<Map<String, Object>>) subCategory.get("subCategories");
                if (subCategories == null) continue;
                for (Map<String, Object> subCat : subCategories) {
                    List<Map<String, Object>> zones = (List<Map<String, Object>>) subCat.get("zones");
                    if (zones == null) continue;
                    for (Map<String, Object> zone : zones) {
                        String zoneNameWithSequence = (String) zone.get("name");
                        System.out.println("Processing zone: " + zoneNameWithSequence);
                        String[] parts = zoneNameWithSequence.split("_");
                        String zoneNumber = parts.length > 0 ? parts[0] : "Sn";
                        String serialNo = parts.length > 1 ? parts[1] : "";
                        String moduleId = parts.length > 2 ? parts[2] : "";
                        String zoneName = parts.length > 3 ? parts[3] : "";

                        List<Map<String, Object>> modules = (List<Map<String, Object>>) zone.get("modules");
                        if (modules == null || modules.isEmpty()) continue;

                        Map<String, Object> module = modules.get(0);
                        String moduleDesc = (String) module.get("name");
                        String price = module.get("price") != null ? module.get("price").toString() : "";
                        String dimension = module.get("dimension") != null ? module.get("dimension").toString() : "";
                        String cabinet = module.get("cabinet") != null ? module.get("cabinet").toString() : "";
                        String cabinetColor = module.get("cabinetColor") != null ? module.get("cabinetColor").toString() : "";
                        Map<String, Object> details = (Map<String, Object>) module.get("details");
                        List<Map<String, Object>> handles = (List<Map<String, Object>>) details.get("handles");
                        List<Map<String, Object>> multipartShutters = (List<Map<String, Object>>) module.get("multipartShutters");
                        List<Map<String, Object>> shutters = (List<Map<String, Object>>) module.get("shutters");
                        List<Map<String, Object>> groupPanels =(List<Map<String, Object>>) module.get("groupPanels");
                        List<Map<String, Object>> accessories = (List<Map<String, Object>>) module.get("accessories");
                        String handleName = "", handleDesign = "";
                        if (handles != null && !handles.isEmpty()) {
                            Map<String, Object> handle = handles.get(0);
                             handleName = handle.get("handleName") != null ? handle.get("handleName").toString() : "";
                            handleDesign = handle.get("handleDesign") != null ? handle.get("handleDesign").toString() : "";
                        }

                        if (multipartShutters != null && !multipartShutters.isEmpty()) {
                            multipartShutterCount = multipartShutters.size();
                        }
                        for (int i = 1; i <= multipartShutterCount; i++) {
                            reportHeaders.add("multipartShutter" + i);
                        }

                        String coreName = "", finishName = "", finishCategory = "", color = "", shutterDesignType = "", shutterSubDesignName = "";
                        String infillMaterialName = "", infillColor = "", infillType = "", internalName="", section="";
                        List<String> multipartShutterData = new ArrayList<>();
                        if ((shutters != null && !shutters.isEmpty()) || (groupPanels != null && !groupPanels.isEmpty())) {
                            Map<String, Object> shutter = (shutters != null && !shutters.isEmpty()) ? shutters.get(0) : null;
                            Map<String, Object> groupPanel = (groupPanels != null && !groupPanels.isEmpty()) ? groupPanels.get(0) : null;

                            coreName = shutter != null && shutter.get("coreName") != null
                                    ? shutter.get("coreName").toString()
                                    : groupPanel != null && groupPanel.get("coreName") != null
                                    ? groupPanel.get("coreName").toString()
                                    : "";

                            finishName = shutter != null && shutter.get("finishName") != null
                                    ? shutter.get("finishName").toString()
                                    : groupPanel != null && groupPanel.get("finishName") != null
                                    ? groupPanel.get("finishName").toString()
                                    : "";

                            finishCategory = shutter != null && shutter.get("finishCategory") != null
                                    ? shutter.get("finishCategory").toString()
                                    : groupPanel != null && groupPanel.get("finishCategory") != null
                                    ? groupPanel.get("finishCategory").toString()
                                    : "";

                            color = shutter != null && shutter.get("color") != null
                                    ? shutter.get("color").toString()
                                    : groupPanel != null && groupPanel.get("color") != null
                                    ? groupPanel.get("color").toString()
                                    : "";

                            shutterDesignType = shutter != null && shutter.get("shutterDesignType") != null
                                    ? shutter.get("shutterDesignType").toString()
                                    : groupPanel != null && groupPanel.get("shutterDesignType") != null
                                    ? groupPanel.get("shutterDesignType").toString()
                                    : "";

                            shutterSubDesignName = shutter != null && shutter.get("shutterSubDesignName") != null
                                    ? shutter.get("shutterSubDesignName").toString()
                                    : groupPanel != null && groupPanel.get("shutterSubDesignName") != null
                                    ? groupPanel.get("shutterSubDesignName").toString()
                                    : "";
                            if (isInFillDataRequired.equalsIgnoreCase("true")){
                                Map<String, Object> infillData = (shutters != null && !shutters.isEmpty()) ? shutters.get(1) : null;
                                if (infillData != null) {
                                    infillType = infillData.get("name") != null ? infillData.get("name").toString() : "";
                                    if (infillType.equalsIgnoreCase("Infill Glass")) {
                                        infillMaterialName = infillData.get("glassName") != null ? infillData.get("glassName").toString() : "";
                                        infillColor = infillData.get("glassColorName") != null ? infillData.get("glassColorName").toString() : "";
                                    } else {
                                        infillMaterialName = infillData.get("finishName") != null ? infillData.get("finishName").toString() : "";
                                        infillColor = infillData.get("color") != null ? infillData.get("color").toString() : "";
                                    }
                                }
                            }

                        }else {
                            if (multipartShutters != null && !multipartShutters.isEmpty()) {

                                for (Map<String, Object> multipartShutter : multipartShutters) {
                                    multipartShutterData.add(multipartShutter.get("name") != null ? multipartShutter.get("name").toString() : "");
                                    List<Map<String, Object>> sections = (List<Map<String, Object>>) ((Map<String, Object>) multipartShutter.get("details")).get("sections");
                                    color = sections.get(0).get("colourName") != null ? sections.get(0).get("colourName").toString() : "";
                                    coreName = sections.get(0).get("coreName") != null ? sections.get(0).get("coreName").toString() : "";
                                    finishName = sections.get(0).get("finishName") != null ? sections.get(0).get("finishName").toString() : "";
                                    if (color == null || color.isEmpty()) {
                                        color = sections.get(0).get("glassName") != null ? sections.get(0).get("glassName").toString() : "";
                                    }
                                }
                                int extra = multipartShutterCount - multipartShutters.size();
                                for (int i = 0; i < extra; i++) {
                                    multipartShutterData.add("");
                                    multipartShutterData.add("");
                                }
                            }else {
                                for (int i = 0; i < multipartShutterCount; i++) {
                                    multipartShutterData.add("");
                                    multipartShutterData.add("");
                                }
                            }
                        }



                        if (isInternals.equalsIgnoreCase("true")) {
                            List<Map<String, Object>> internals = (List<Map<String,Object>>) module.get("wardrobeInternals");
                            if (internals != null && !internals.isEmpty()) {
                                    Map<String, Object> internal = internals.get(0);
                                    internalName = internal.get("name") != null ? internal.get("name").toString() : "";
                                    section= internal.get("section") != null ? internal.get("section").toString(): "";
                            }
                        }

                        String rootFolder = "PriceReports/";
                        setupCSVReport(rootFolder, reportHeaders.toArray(new String[0]));


                        List<String> row = new ArrayList<>(Arrays.asList(
                                serialNo, moduleId, zoneName, moduleDesc, price, dimension, cabinet, cabinetColor,handleName,handleDesign,
                                coreName, finishName, finishCategory, color, shutterDesignType, shutterSubDesignName
                        ));

                        if ("true".equalsIgnoreCase(isInFillDataRequired)) {
                            row.addAll(Arrays.asList(infillMaterialName, infillColor));
                        }
                        if ("true".equalsIgnoreCase(isInternals)) {
                            row.addAll(Arrays.asList(internalName, section));
                        }
                        if (!multipartShutterData.isEmpty()) {
                            row.addAll(multipartShutterData);
                        } else {
                            for (int i = 0; i < multipartShutterCount; i++) {
                                row.add("");
                            }
                        }

                        if (accessories != null) {
                            for (Map<String, Object> accessory : accessories) {
                                row.add(accessory.get("name") != null ? accessory.get("name").toString() : "");
                                row.add(accessory.get("price") != null ? accessory.get("price").toString() : "");
                            }
                            int extra = maxAccessoryCount - accessories.size();
                            for (int i = 0; i < extra; i++) {
                                row.add(""); // Accessory name
                                row.add(""); // Accessory price
                            }
                        } else {
                            for (int i = 0; i < maxAccessoryCount; i++) {
                                row.add("");
                                row.add("");
                            }
                        }

                        utilities.appendTestResults(row.toArray(new String[0]), reportFilePath);
                    }
                }
            }
        }

        System.out.println("Zone module details report generated: " + reportFilePath);
    }

    @Test(priority = 2)
    public void extractAllPricingErrorsFromProject() throws Exception {
        Response getPricingError = scBackendService.getPricingError(projectID, token);
        System.out.println("Response Status Code: " + getPricingError.getStatusCode());
        String jsonResponse = getPricingError.getBody().asString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(jsonResponse, new TypeReference<>() {
        });
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("rooms");
        if (rooms == null) return;
        String[] reportHeaders = {"s.no","moduleId", "moduleName", "errorMessage"};
        String rootFolder = "ErrorReports/";
        setupCSVReport(rootFolder, reportHeaders);

        for (Map<String, Object> room : rooms) {
            Map<String, Object> errors = (Map<String, Object>) room.get("errors");
            if (errors == null) continue;
            Map<String, Object> pricing = (Map<String, Object>) errors.get("pricing");
            if (pricing == null) continue;
            for (Map.Entry<String, Object> entry : pricing.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof List)) continue;
                List<Map<String, Object>> errorList = (List<Map<String, Object>>) value;
                for (Map<String, Object> errorItem : errorList) {
                    String zoneNameWithSequence = (String) errorItem.get("zoneName");
                    System.out.println("Processing zone: " + zoneNameWithSequence);
                    String[] parts = zoneNameWithSequence.split("_");
                    String zoneNumber = parts.length > 0 ? parts[0] : "Sn";
                    String serialNo = parts.length > 1 ? parts[1] : "";
                    String moduleId = parts.length > 2 ? parts[2] : "";
                    String zoneName = parts.length > 3 ? parts[3] : "";

                    String errorMessage = (String) errorItem.get("errorMessage");
                    if (errorMessage != null) {
                        errorMessage = "\"" + errorMessage.replace("\"", "\"\"") + "\"";
                    }
                    String[] row = {serialNo,moduleId, zoneName, errorMessage};
                    utilities.appendTestResults(row, reportFilePath);
                }
            }
        }
        System.out.println("Pricing error report generated: " + reportFilePath);
    }


    public static List<Map<String, String>> extractErrorDetails(String jsonResponse, String targetRoomId, String targetZoneId, String targetModuleId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {
        });
        List<Map<String, String>> results = new ArrayList<>();

        List<Map<String, Object>> rooms = (List<Map<String, Object>>) data.get("rooms");
        if (rooms == null) return results;


        for (Map<String, Object> room : rooms) {
            if (!targetRoomId.equals(room.get("id"))) continue;

            Map<String, Object> errors = (Map<String, Object>) room.get("errors");
            if (errors == null) continue;

            Map<String, Object> pricing = (Map<String, Object>) errors.get("pricing");
            if (pricing == null) continue;

            for (Map.Entry<String, Object> entrySet : pricing.entrySet()) {
                Object value = entrySet.getValue();
                if (!(value instanceof List)) continue;

                List<Map<String, Object>> entries = (List<Map<String, Object>>) value;
                for (Map<String, Object> errorItem : entries) {
                    if (!targetModuleId.equals(String.valueOf(errorItem.get("moduleId"))) && !targetZoneId.equalsIgnoreCase("zoneObjectId"))
                        continue;

                    Map<String, String> result = new HashMap<>();
                    result.put("category", entrySet.getKey()); // e.g., woodwork, hds, etc.
                    result.put("zoneName", String.valueOf(errorItem.get("zoneName")));
                    result.put("moduleName", String.valueOf(errorItem.get("moduleName")));
                    result.put("errorMessage", String.valueOf(errorItem.get("errorMessage")));
                    result.put("errorType", String.valueOf(errorItem.get("errorType")));
                    results.add(result);
                }
            }
        }

        return results;
    }

    public static String extractValuesByKeyAsString(List<Map<String, String>> list, String key) {
        return list.stream()
                .filter(map -> map.containsKey(key))
                .map(map -> map.get(key))
                .collect(Collectors.joining(", "));
    }

    public void setupCSVReport(String reportType, String[] headers) {
        try {
            String environment = Optional.ofNullable(propertiesReader.getEnvironment()).orElse("preProd");
            String projectId = Optional.ofNullable(propertiesReader.getProjectID()).orElse("unknown");
            String customerType = Optional.ofNullable(propertiesReader.getCustomerType()).orElse("NA");

            // ✅ Use utility to generate the proper report file path
            Path reportPath = utils.ReportPathUtils.getTimestampedReportFile(
                    reportType, environment, projectId + "_" + customerType, ".csv"
            );

            // ✅ Save for later appending
            reportFilePath = reportPath.toString();

            // ✅ Initialize CSV with headers
            utilities.createCSVReport(headers, reportFilePath);

            System.out.println("✅ CSV report initialized: " + reportFilePath);

        } catch (Exception e) {
            System.err.println("❌ Failed to setup CSV report: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
