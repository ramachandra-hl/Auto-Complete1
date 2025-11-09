package test;

import models.ModuleInputData;
import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.DataProviderUtil;
import utils.PropertiesReader;
import utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import static utils.Utilities.formatCurrentDate;

public class ApplyShutterBasedOnFamilyTest extends RoomModuleHandler {
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    String filePath = "";
    String[] headers = {"S.no", "ModuleName", "ModuleId", "ShutterCoreCode", "shutterCoreName", "ShutterFinishCategory", "shutterDesignType", "ShutterFinishCode", "shutterFinishName", "ColorId", "ColorName", "Status"};

    @BeforeClass
    public void setupCSVReport() {
        String subFolder = "reports/ShutterReports/" + propertiesReader.getEnvironment() + "/" + formatCurrentDate("📅dd-MM-yyyy↘️");
        String fileName = projectID + "_" + formatCurrentDate(" ⏰ hh.mm.a ") + customerType + ".csv";
        String filePath = subFolder + "/" + fileName;
        utilities.createCSVReport(headers, filePath);
    }

    /*

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    void applyShutterForMultipleRoomTypes(Map<String, String> roomDetailsMap) {

        ModuleInputData moduleInputData = new ModuleInputData(roomDetailsMap);
        Utilities utilities = new Utilities();

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

        Map<String, String> zoneInfo = new HashMap<>();
        zoneInfo.put("categoryId", categoryId);
        zoneInfo.put("subCategoryId", subCategoryId);
        zoneInfo.put("categoryName", categoryInfo.get("categoryName"));
        zoneInfo.put("subCategoryName", categoryInfo.get("subCategoryName"));
        zoneInfo.put("moduleName", moduleInputData.getModuleName());

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());
        Response getZoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId, subCategoryId, dimension, moduleInputData.getModuleId());
        List<Map<String, String>> shutterGaphqls = (List<Map<String, String>>) getZoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.graphqlShutters").getFirst();
        List<String> coreNames = new ArrayList<>();
        int count = 0;

        for (Map<String, String> shutter : shutterGaphqls) {
            String shutterCoreName = shutter.get("shutterCoreName");
            if (!coreNames.contains(shutterCoreName)) {
                coreNames.add(shutterCoreName);
            }
            System.out.println("🎨 Shutter Finish Code: " + shutter.get("shutterFinishCode"));
            Map<String, Object> shutterMap = new HashMap<>();


            Response materialColorShutterResponse = synapseService.getMaterialColor("shutter", shutter.get("shutterFinishCode"));
            List<Map<String, Object>> shutterColourDetailsList = materialColorShutterResponse.jsonPath().getList("data.getColour");
            System.out.println("🌈 Total number of colours in this finish:➡️ " + shutterColourDetailsList.size());
            for (Map<String, Object> color : shutterColourDetailsList) {
                ++count;
                shutterMap.put("shutterColourName", color.get("colourName"));
                shutterMap.put("shutterColourImage", color.get("colourImage"));
                shutterMap.put("shutterMaterialId", color.get("assetMaterialId"));
                shutterMap.put("shutterColourCode", color.get("colourId"));
                System.out.println("🎨 Applying Shutter Colour: " + color.get("colourName") + " | Using Colour Code 🟦 " + color.get("colourId"));
                shutterMap.put("shutterCoreCode", shutter.get("shutterCoreCode"));
                shutterMap.put("shutterCoreName", shutterCoreName);
                shutterMap.put("shutterFinishCode", shutter.get("shutterFinishCode"));
                shutterMap.put("shutterFinishName", shutter.get("shutterFinishName"));
                shutterMap.put("shutterCategory", shutter.get("shutterCategory"));
                shutterMap.put("moduleObjectId", miqModuleObjectId);

                Response applyShutterMaterialResponse = moduleService.applyShutterMaterial(projectID, floorID, currentRoomId, currentZoneId, shutterMap, miqModulesShutterWithAccessory, token);

                Assert.assertEquals(applyShutterMaterialResponse.getStatusCode(), 200, "Failed to apply shutter material for module: " + moduleInputData.getModuleName());
                String[] resultData = {String.valueOf(count), moduleInputData.getModuleName(), moduleInputData.getModuleId(), shutter.get("shutterCoreCode"), shutterCoreName, shutter.get("shutterFinishCategory"), shutter.get("shutterDesignType"), shutter.get("shutterFinishCode"), shutter.get("shutterFinishName"), (String) color.get("colourId"), (String) color.get("colourName"), String.valueOf(applyShutterMaterialResponse.getStatusCode())};
                utilities.appendTestResults(resultData, filePath);
//                    handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());
            }
        }
    }

     */
}
