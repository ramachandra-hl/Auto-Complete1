package test;

import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import models.ModuleInputData;
import org.testng.annotations.Test;
import utils.DataProviderUtil;

import java.util.*;

public class ShutterFamilyFetcherTest extends RoomModuleHandler {

    String ReportFilePath ;
    int baseSize;

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void getShutterMaterials(Map<String, String> roomDetailsMap) {
        ReportFilePath =  "reports/ShutterReports/shutterFamily -  " +projectID + ".csv";
        processShutters(roomDetailsMap, "shutterFamily");
    }

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void getAluShutters(Map<String, String> roomDetailsMap) {
        ReportFilePath =  "reports/ShutterReports/aluShuttersWithFamily -  " +projectID + ".csv";
        processShutters(roomDetailsMap, "aluShutter");
    }

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void getAluShuttersWithMaterials(Map<String, String> roomDetailsMap) {
        ReportFilePath =  "reports/ShutterReports/aluShutters -  " +projectID + ".csv";
        processShutters(roomDetailsMap, "aluShutterWithMaterials");
    }

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void getShutterCore(Map<String, String> roomDetailsMap) {
        ReportFilePath = "reports/ShutterReports/shutterCore - " +projectID + ".csv";
        processShutters(roomDetailsMap, "shutterCore");
    }


    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void getDcShutterCore(Map<String, String> roomDetailsMap) {
        ReportFilePath = "reports/ShutterReports/DCShutterCore - " +projectID + ".csv";
        processShutters(roomDetailsMap, "DCShutterCore");
    }

    private void processShutters(Map<String, String> roomDetailsMap, String type) {
        ModuleInputData moduleInputData = new ModuleInputData(roomDetailsMap);
        Map<String, String> categoryInfo = synapseService.getCategoryDetails(moduleInputData.getModuleId());
        Map<String, String> zoneInfo = getZoneInfo(moduleInputData, categoryInfo);

        handleRoomAndModule(zoneInfo.get("roomType"), zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

        ArrayList<String> csvHeaders = new ArrayList<>(Arrays.asList("moduleName", "moduleId", "categoryName", "subCategoryName"));
        ArrayList<String> csvData = new ArrayList<>(Arrays.asList(moduleInputData.getModuleName(), moduleInputData.getModuleId(), categoryInfo.get("categoryName"), categoryInfo.get("subCategoryName")));
        baseSize = csvData.size();
        Response zoneDataRes = synapseService.getZoneData(zoneInfo.get("roomType"), currentZoneId, zoneInfo.get("categoryId"), zoneInfo.get("subCategoryId"), dimension, moduleInputData.getModuleId());
        Response wardrobeMaterialResponse = synapseService.getWardrobeMaterialData(moduleInputData.getModuleId(), zoneInfo.get("categoryId"), zoneInfo.get("subCategoryId"));

        if (type.equalsIgnoreCase("aluShutterWithMaterials")){
            List<Map<String, String>> glassMaterials =  wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.glassMaterials");
            List<Map<String, String>> multiPartShutterMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.multiPartShutterMaterials");
            getAluDoorsMaterials(moduleInputData.getModuleId(),zoneInfo.get("categoryId"),zoneInfo.get("subCategoryId"),glassMaterials,multiPartShutterMaterials,csvData,csvHeaders);

        } else if (type.equalsIgnoreCase("aluShutter")) {
            getAluDoors(moduleInputData.getModuleId(),zoneInfo.get("categoryId"),zoneInfo.get("subCategoryId"),csvData,csvHeaders);
        } else if(type.equalsIgnoreCase("DCShutterCore")){
            List<Map<String, String>> graphqlShutters = zoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules[0].graphqlShutters");
            getDCShutters(moduleInputData.getModuleId(),zoneInfo.get("categoryId"),zoneInfo.get("subCategoryId"),graphqlShutters,csvData,csvHeaders);
        }
        else {
            List<Map<String, String>> shuttersWithAccessories = (List<Map<String, String>>) zoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.shuttersWithAccessories").get(0);
            List<Map<String, String>> graphqlExposedPanels = (List<Map<String, String>>) zoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.graphqlExposedPanels").get(0);
            List<Map<String, String>> group25Materials = zoneDataRes.jsonPath().getList("data.getZoneData.group25Materials");
            List<Map<String, String>> visibleSideMaterials =  wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.visibleSideMaterials");
            List<Map<String, String>> woodenMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.woodenMaterials");
            graphqlExposedPanels = getDistinctByProperty(graphqlExposedPanels, "panelGroup");

            processShutterItems(shuttersWithAccessories, group25Materials, woodenMaterials, csvData, csvHeaders, type);
            processShutterItems(graphqlExposedPanels, group25Materials, visibleSideMaterials, csvData, csvHeaders, type);
        }
        utilities.appendTestResultsWithHeaders(new String[]{""}, new ArrayList<>(List.of("")), ReportFilePath);
    }

    private Map<String, String> getZoneInfo(ModuleInputData moduleInputData, Map<String, String> categoryInfo) {
        String roomType = Optional.ofNullable(moduleInputData.getRoomType())
                .map(rt -> rt.substring(0, 1).toUpperCase() + rt.substring(1).toLowerCase())
                .orElse(categoryInfo.get("roomType"));

        Map<String, String> zoneInfo = new HashMap<>();
        zoneInfo.put("roomType", roomType);
        zoneInfo.put("categoryId", categoryInfo.get("categoryId"));
        zoneInfo.put("subCategoryId", categoryInfo.get("subCategoryId"));
        zoneInfo.put("categoryName", categoryInfo.get("categoryName"));
        zoneInfo.put("subCategoryName", categoryInfo.get("subCategoryName"));
        zoneInfo.put("moduleName", moduleInputData.getModuleName());
        return zoneInfo;
    }

    private void processShutterItems(List<Map<String, String>> items, List<Map<String, String>> primaryMaterials,
                                     List<Map<String, String>> secondaryMaterials, ArrayList<String> shutterFamilyNames,
                                     ArrayList<String> csvHeaders, String type) {
        for (Map<String, String> item : items) {
            csvHeaders.add("component");
            if (item.get("name") == null){
                shutterFamilyNames.add(item.get("panelGroup"));
            }else {
                shutterFamilyNames.add(item.get("name"));
            }

            if ("shutterFamily".equals(type)) {
                addShutterMaterials(item, primaryMaterials, secondaryMaterials, shutterFamilyNames, csvHeaders);
            } else {
                addShutterCore(item, primaryMaterials, secondaryMaterials, shutterFamilyNames, csvHeaders, type);
            }
        }
    }

    private void addShutterMaterials(Map<String, String> item, List<Map<String, String>> primaryMaterials,
                                     List<Map<String, String>> secondaryMaterials, ArrayList<String> shutterFamilyNames,
                                     ArrayList<String> csvHeaders) {
        List<Map<String, String>> materialList = getMaterialList(item, primaryMaterials, secondaryMaterials);
        Map<String, List<String>> csvDataMap = new HashMap<>();

        for (Map<String, String> material : materialList) {
            csvDataMap.computeIfAbsent(material.get("shutterCoreName"), k -> new ArrayList<>())
                    .add(material.get("shutterFinishName") + " on " + material.get("shutterCoreName"));
        }

        for (Map.Entry<String, List<String>> data : csvDataMap.entrySet()) {
            csvHeaders.add(data.getKey());
            shutterFamilyNames.add("\"" + String.join(", ", data.getValue()) + "\"");
        }

        writeToCsv(csvHeaders, shutterFamilyNames);
    }

    private void addShutterCore(Map<String, String> item, List<Map<String, String>> primaryMaterials,
                                List<Map<String, String>> secondaryMaterials, ArrayList<String> shutterFamilyNames,
                                ArrayList<String> csvHeaders, String header) {
        List<Map<String, String>> materialList = getMaterialList(item, primaryMaterials, secondaryMaterials);
        Set<String> coreNames = new HashSet<>();

        for (Map<String, String> material : materialList) {
            coreNames.add(material.get("shutterCoreName"));
        }

        int count = 1;
        for (String coreName : coreNames) {
            shutterFamilyNames.add(coreName);
            csvHeaders.add(header + count);
            count++;
        }

        writeToCsv(csvHeaders, shutterFamilyNames);
    }

    private List<Map<String, String>> getMaterialList(Map<String, String> item, List<Map<String, String>> primaryMaterials,
                                                      List<Map<String, String>> secondaryMaterials) {
        String checkPoint = item.getOrDefault("shutterType", null);
        if (checkPoint == null) {
            return item.get("panelGroup").equalsIgnoreCase("Colour Group 1-25") ? primaryMaterials : secondaryMaterials;
        } else {
            return item.get("shutterType").equalsIgnoreCase("INCT-25mm-v1") ? primaryMaterials : secondaryMaterials;
        }
    }

    private void writeToCsv(ArrayList<String> csvHeaders, ArrayList<String> shutterFamilyNames) {
        utilities.appendTestResultsWithHeaders(csvHeaders.toArray(new String[0]), shutterFamilyNames, ReportFilePath);
        if (shutterFamilyNames.size() > baseSize) {
            shutterFamilyNames.subList(baseSize, shutterFamilyNames.size()).clear();
            csvHeaders.subList(baseSize, csvHeaders.size()).clear();
        }
    }

    public static List<Map<String, String>> getDistinctByProperty(List<Map<String, String>> items, String key) {
        Set<String> seenValues = new HashSet<>();
        List<Map<String, String>> distinctList = new ArrayList<>();

        for (Map<String, String> item : items) {
            String value = item.get(key);
            if (value != null && seenValues.add(value)) {
                distinctList.add(item);
            }
        }

        return distinctList;
    }


    public void getAluDoors(String moduleId, String categoryId, String subCategoryId,
                            ArrayList<String> csvData, ArrayList<String> csvHeaders) {
        Map<String, Object> wardrobeShutters = getWardrobeShutters();
        if (wardrobeShutters == null) {
            addNoShuttersToCsv(csvHeaders, csvData);
            return;
        }

        for (Map.Entry<String, Object> shutterEntry : wardrobeShutters.entrySet()) {
            String side = shutterEntry.getKey();
            csvHeaders.add("side");
            csvData.add(side);

            String dimension = getDisplayDimension(shutterEntry.getValue());
            Response res = synapseService.multiPartShutterData(moduleId, categoryId, subCategoryId, dimension, side);
            List<Map<String, String>> shutters = res.jsonPath().getList("data.getMultiPartShutter");

            Map<String, List<String>> groupedShutters = groupShuttersByPrefix(shutters);

            for (Map.Entry<String, List<String>> entry : groupedShutters.entrySet()) {
                csvHeaders.add(entry.getKey());
                csvData.add("\"" + String.join(", ", entry.getValue()) + "\"");
            }

            writeToCsv(csvHeaders, csvData);
        }
    }

    public void getAluDoorsMaterials(String moduleId, String categoryId, String subCategoryId,
                                     List<Map<String, String>> glassMaterials, List<Map<String, String>> multiPartMaterials,
                                     ArrayList<String> csvData, ArrayList<String> csvHeaders) {
        Map<String, Object> wardrobeShutters = getWardrobeShutters();
        if (wardrobeShutters == null) {
            addNoShuttersToCsv(csvHeaders, csvData);
            return;
        }

        for (Map.Entry<String, Object> shutterEntry : wardrobeShutters.entrySet()) {
            String side = shutterEntry.getKey();
            csvHeaders.add("side");
            csvData.add(side);

            Map<String, String> sideData = (Map<String, String>) shutterEntry.getValue();
            String dimension = sideData.get("displayDimension");
            String frameCode = sideData.get("frameCode");

            Response res = synapseService.multiPartShutterData(moduleId, categoryId, subCategoryId, dimension, side);
            List<Map<String, String>> shutters = res.jsonPath().getList("data.getMultiPartShutter");

            for (Map<String, String> shutter : shutters) {
                String shutterName = shutter.get("name");
                String shutterId = String.valueOf(shutter.get("shutterId"));

                csvHeaders.add("shutterName");
                csvData.add(shutterName);

                Map<String, String> shutterIds = Map.of(side, shutterId);
                Map<String, Object> aluRes = moduleService.aluminiumShutters(
                        projectID, floorID, currentRoomId, currentZoneId, miqModuleObjectId, shutterIds, frameCode, frameCode, null,token
                );

                List<Map<String, Object>> panels = (List<Map<String, Object>>) aluRes.get("panels");
                List<String> panelNames = extractPanelNames((List<Map<String, Object>>) panels.get(0));
                csvHeaders.add("panels");
                csvData.add(String.join("\"" +",", panelNames)+ "\"");

                List<String> finishNames = extractFinishNamesByType(glassMaterials);
                finishNames.addAll(extractFinishNamesByType(multiPartMaterials));

                csvHeaders.add("ShutterMaterials");
                csvData.add("\"" + String.join(", ", finishNames) + "\"");

                writeToCsv(csvHeaders, csvData);
            }
        }
    }

    public void getDCShutters(String moduleId, String categoryId, String subCategoryId, List<Map<String, String>> graphqlShutters,
                                     ArrayList<String> csvData, ArrayList<String> csvHeaders) {

        Map<String, List<String>> csvDataMap = new HashMap<>();

        for (Map<String, String> material : graphqlShutters) {
            csvDataMap.computeIfAbsent(material.get("shutterFinishCategory") + " on " +material.get("shutterDesignType"), k -> new ArrayList<>())
                    .add( material.get("shutterFinishName"));
        }

        for (Map.Entry<String, List<String>> data : csvDataMap.entrySet()) {
            csvHeaders.add(data.getKey());
            csvData.add("\"" + String.join(", ", data.getValue()) + "\"");
        }
                writeToCsv(csvHeaders, csvData);
    }


    private Map<String, Object> getWardrobeShutters() {
        Response response = scBackendService.getUnitEntryData(projectID, floorID, currentRoomId, currentZoneId, token);

        return response.jsonPath().getMap("miqModules[0].wardrobeShutters");
    }

    private String getDisplayDimension(Object shutterValue) {
        Map<String, String> sideData = (Map<String, String>) shutterValue;
        return sideData.get("displayDimension");
    }

    private void addNoShuttersToCsv(ArrayList<String> csvHeaders, ArrayList<String> csvData) {
        csvHeaders.add("side");
        csvData.add("no alu shutters");
    }

    private Map<String, List<String>> groupShuttersByPrefix(List<Map<String, String>> shutters) {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (Map<String, String> shutter : shutters) {
            String name = shutter.get("name");
            int idx = name.indexOf("Door");
            if (idx != -1) {
                String prefix = name.substring(0, idx+4).trim();
                grouped.computeIfAbsent(prefix, k -> new ArrayList<>()).add(name);
            }
        }
        return grouped;
    }

    private List<String> extractPanelNames(List<Map<String, Object>> panels) {
        List<String> names = new ArrayList<>();
        for (Map<String, Object> panel : panels) {
            String panelName = panel.get("name").toString();
            if (panelName.contains("Section")) {
                names.add(panelName);
            }
        }
        return names;
    }

    public List<String> extractFinishNamesByType(List<Map<String, String>> inputList) {
        List<String> result = new ArrayList<>();
        for (Map<String, String> item : inputList) {
            String name = item.get("type");
            if (name == null) {
                name = item.get("shutterFinishName");
            }
            result.add(name);
        }
        return result;
    }

}


