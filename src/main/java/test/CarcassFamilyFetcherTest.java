package test;

import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import models.ModuleInputData;
import org.testng.annotations.Test;
import utils.DataProviderUtil;
import utils.Utilities;

import java.io.IOException;
import java.util.*;

public class CarcassFamilyFetcherTest extends RoomModuleHandler {
    List<Map<String, List<String>>> carcassDataList = new ArrayList<>();

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void fetchCarcassFamilyForMultipleRoomTypes(Map<String, String> roomDetails) throws IOException {
        ModuleInputData moduleInputData = new ModuleInputData(roomDetails);
        Utilities utilities = new Utilities();
        String filePath = "reports/CarcassReports/" + projectID + ".csv";

        String roomType = formatRoomType(moduleInputData.getRoomType());

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

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(),
                moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

        Response getZoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId,
                subCategoryId, dimension, moduleInputData.getModuleId());

        List<Map<String, String>> carcassFamilyData = (List<Map<String, String>>)
                getZoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.graphqlCarcasses").get(0);

        List<String> headers = new ArrayList<>(List.of("moduleName", "moduleId", "categoryName", "subCategoryName"));
        List<String> data = new ArrayList<>(List.of(
                moduleInputData.getModuleName(),
                moduleInputData.getModuleId(),
                categoryInfo.get("categoryName"),
                categoryInfo.get("subCategoryName")
        ));

        for (int i = 0; i < carcassFamilyData.size(); i++) {
            headers.add("carcassFamily" + (i + 1));
            data.add(carcassFamilyData.get(i).get("carcassName"));
        }

        Map<String, List<String>> csvDataMap = new HashMap<>();
        csvDataMap.put("headers", headers);
        csvDataMap.put("data", data);
        carcassDataList.add(csvDataMap);

        utilities.appendTestResultsWithHeaders(headers.toArray(new String[0]), data, filePath);
    }

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void fetchCarcassWithColours(Map<String, String> roomDetails) {
        ModuleInputData moduleInputData = new ModuleInputData(roomDetails);
        Utilities utilities = new Utilities();
        String filePath = "reports/CarcassReports/carcassWithColour" + projectID + ".csv";

        String roomType = formatRoomType(moduleInputData.getRoomType());

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

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(),
                moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

        Response getZoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId,
                subCategoryId, dimension, moduleInputData.getModuleId());

        List<Map<String, Object>> graphqlModules = getZoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules");
        List<Map<String, Object>> carcassFamilyData = (List<Map<String, Object>>) graphqlModules.get(0).get("graphqlCarcasses");

        List<String> headers = new ArrayList<>(List.of("moduleName", "moduleId", "categoryName", "subCategoryName"));
        List<String> data = new ArrayList<>(List.of(
                moduleInputData.getModuleName(),
                moduleInputData.getModuleId(),
                categoryInfo.get("categoryName"),
                categoryInfo.get("subCategoryName")
        ));

        for (Map<String, Object> carcass : carcassFamilyData) {
            String carcassName = String.valueOf(carcass.get("carcassName"));
            String carcassCode = String.valueOf(carcass.get("carcassCode"));
            headers.add(carcassName);

            Response colorRes = synapseService.getMaterialColor("cabinet", carcassCode);
            List<Map<String, Object>> colorList = colorRes.jsonPath().getList("data.getColour");

            List<String> colorNames = new ArrayList<>();
            for (Map<String, Object> color : colorList) {
                colorNames.add(String.valueOf(color.get("colourName")));
            }
            data.add("\"" + String.join(", ", colorNames) + "\"");
        }

        utilities.appendTestResultsWithHeaders(headers.toArray(new String[0]), data, filePath);
    }

    private String formatRoomType(String rawType) {
        return (rawType != null && !rawType.isBlank())
                ? rawType.substring(0, 1).toUpperCase() + rawType.substring(1).toLowerCase()
                : null;
    }
}
