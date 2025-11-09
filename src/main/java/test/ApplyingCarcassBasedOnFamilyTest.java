package test;

import models.ModuleInputData;
import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.DataProviderUtil;
import utils.Utilities;
import java.util.*;

public class ApplyingCarcassBasedOnFamilyTest extends RoomModuleHandler {

    @Test(dataProvider = "MiscModuleDataProvider",dataProviderClass = DataProviderUtil.class)
    void applyCarcassMaterialToModules(Map<String, String> roomDetailsMap)  {
        ModuleInputData moduleInputData = new ModuleInputData(roomDetailsMap);
        Utilities utilities = new Utilities();

        String[] headers = {"S.no", "ModuleName", "ModuleId",  "CarcassId","CarcassName", "ColourName", "Status"};
        String filePath = "reports/CarcassReports/" + projectID +".csv";
        utilities.createCSVReport(headers, filePath);
        String roomTypeFromInput = moduleInputData.getRoomType();
        String roomType = (roomTypeFromInput != null)
                ? roomTypeFromInput.substring(0, 1).toUpperCase() + roomTypeFromInput.substring(1).toLowerCase() : null;

        roomConfigurationParams.put("roomType", roomType);
        roomConfigurationParams.put("name", moduleInputData.getRoomName());

        Map<String, String> categoryInfo = synapseService.getCategoryDetails(moduleInputData.getModuleId());
        String categoryId = categoryInfo.get("categoryId");
        String subCategoryId = categoryInfo.get("subCategoryId");

        if (roomType == null) {
            roomType = categoryInfo.get("roomType");
        }

        // Create Zone Info Map
        Map<String, String> zoneInfo = new HashMap<>();
        zoneInfo.put("categoryId", categoryId);
        zoneInfo.put("subCategoryId", subCategoryId);
        zoneInfo.put("categoryName", categoryInfo.get("categoryName"));
        zoneInfo.put("subCategoryName", categoryInfo.get("subCategoryName"));
        zoneInfo.put("moduleName", moduleInputData.getModuleName());

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());
        Response getZoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId, subCategoryId, dimension, moduleInputData.getModuleId());
        List<Map<String, String>> carcassData = (List<Map<String, String>>) getZoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.graphqlCarcasses").get(0);

        int count = 0;
        for (Map<String, String> carcass : carcassData) {
            if (carcass.get("carcassCode").equals(moduleInputData.getCarcassCode())) {
                String carcassCode = carcass.get("carcassCode");
                String carcassName = carcass.get("carcassName");
                System.out.println("🧱 Carcass Code: " + carcassCode + " | 🏷️ Name: " + carcassName);
                Response materialColorResponse = synapseService.getMaterialColor("cabinet", carcassCode);
                List<Map<String, Object>> carcassColourDetails = materialColorResponse.jsonPath().getList("data.getColour");

                for (Map<String, Object> colorMap : carcassColourDetails) {
                    ++count;
                    String carcassColourId = colorMap.get("colourId").toString();
                    String carcassColourName = colorMap.get("colourName").toString();
                    String carcassColourImage = colorMap.get("colourImage").toString();
                    String carcassMaterialId = colorMap.get("assetMaterialId").toString();
                    System.out.println(" Carcass Colour🆔: " + carcassColourId + " 🎨 Name: " + carcassColourName);
                    Map<String, String> carcassDataMap = createCarcassDataMap(carcassCode, carcassName, carcassColourId, carcassColourImage, carcassColourName, carcassMaterialId);

                    Response applyCarcassMaterialResponse = moduleService.applyCarcassMaterial(projectID, floorID, currentRoomId, currentZoneId, carcassDataMap, token);
                    Assert.assertEquals(applyCarcassMaterialResponse.getStatusCode(), 200, "Failed to apply carcass material.");
                    String[] resultData = {String.valueOf(count), moduleInputData.getModuleName(), moduleInputData.getModuleId(), carcassCode, carcassName, carcassColourName, String.valueOf(applyCarcassMaterialResponse.getStatusCode())};
                    utilities.appendTestResults(resultData, filePath);
//                  handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());
                }
            }
        }
    }

    private Map<String, String> createCarcassDataMap(String carcassCode, String carcassName, String carcassColourId, String carcassColourImage, String carcassColourName, String carcassMaterialId) {
        Map<String, String> carcassDataMap = new HashMap<>();
        carcassDataMap.put("carcassCode", carcassCode);
        carcassDataMap.put("carcassName", carcassName);
        carcassDataMap.put("carcassColourCode", carcassColourId);
        carcassDataMap.put("carcassColourImage", carcassColourImage);
        carcassDataMap.put("carcassColourName", carcassColourName);
        carcassDataMap.put("carcassMaterialId", carcassMaterialId);
        carcassDataMap.put("moduleObjectId", miqModuleObjectId);
        return carcassDataMap;
    }
}
