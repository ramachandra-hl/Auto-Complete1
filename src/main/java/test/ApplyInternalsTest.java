package test;

import configurator.RoomModuleHandler;
import models.ModuleInputData;
import org.testng.annotations.Test;
import utils.DataProviderUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static test.FetchPricesForModulesTest.generatePanelPriceSumReport;

public class ApplyInternalsTest extends RoomModuleHandler {

    /**
     * Test 1: Apply all internals for a given hardcoded module ID ("13732").
     */
    @Test(enabled = true)
    public void applyAllInternalsByModuleId() {
        List<Map<String, String>> internalsList = synapseService.getInternalSetList("13806");

        for (Map<String, String> internal : internalsList) {
            String internalId = internal.get("internalId");
            String internalName = internal.get("internalName");

            // Extract module data from the response
            Map<String,String> moduleData = synapseService.getModuleByInternalId(internalId);
            System.out.println("Module Data: " + moduleData);
            Map<String, String> roomDetailsMap = new HashMap<>();
            roomDetailsMap.put("moduleId",moduleData.get("moduleID"));
            roomDetailsMap.put("moduleName",moduleData.get("moduleName"));
            roomDetailsMap.put("width",moduleData.get("moduleWidth"));
            roomDetailsMap.put("height",moduleData.get("moduleHeight"));
            roomDetailsMap.put("depth",moduleData.get("moduleDepth"));
            roomDetailsMap.put("internalId", internalId);
            roomDetailsMap.put("internalName", internalName);
            ModuleInputData moduleInputData = new ModuleInputData(roomDetailsMap);


            String roomTypeFromInput = moduleInputData.getRoomType();
            String roomType = (roomTypeFromInput != null)
                    ? roomTypeFromInput.substring(0, 1).toUpperCase() + roomTypeFromInput.substring(1).toLowerCase()
                    : null;

            roomConfigurationParams.put("roomType", roomType);
            roomConfigurationParams.put("name", moduleInputData.getRoomName());
            System.out.println(moduleInputData.getModuleId());
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
            String zoneName = moduleInputData.getSerialNo()+"_"+moduleInputData.getModuleId()+"_"+moduleInputData.getModuleName();
            zoneInfo.put("moduleName",zoneName);
            handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

            moduleService.processWardrobeComponents(projectID, floorID, currentRoomId, currentZoneId,
                    moduleInputData.getModuleId(), categoryId, subCategoryId, miqModuleObjectId,
                    moduleInputData.getInternalId(), moduleInputData.getInternalWidth(),
                    moduleInputData.getInternalDepth(), moduleInputData.getInternalHeight(),
                    moduleInputData.getAccessoryCode(), roomType, isInternals, token
            );
        }
    }

    @Test(enabled = true)
    public void testGeneratePanelPriceSumReport() throws Exception {
        generatePanelPriceSumReport(projectID, token);
    }

    /**
     * Test 2: Apply internals to module based on internalId (Data-driven test).
     */
    @Test( enabled = false, dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void applyInternalsToModuleBasedOnInternalId(Map<String, String> roomDetailsMap) {
        Map<String, String> moduleData = synapseService.getModuleByInternalId(roomDetailsMap.get("internalId"));
        System.out.println("Module Data: " + moduleData);

        roomDetailsMap.put("moduleId", moduleData.get("moduleID"));
        roomDetailsMap.put("moduleName", moduleData.get("moduleName"));
        roomDetailsMap.put("width", moduleData.get("moduleWidth"));
        roomDetailsMap.put("height", moduleData.get("moduleHeight"));
        roomDetailsMap.put("depth", moduleData.get("moduleDepth"));

        applyInternalsToModule(roomDetailsMap);
    }

    /**
     * Shared logic to apply internals using a module data map.
     */
    private void applyInternalsToModule(Map<String, String> Data) {
      Map<String, String> moduleData = new HashMap<>();
        moduleData.put("moduleId", Data.get("moduleId"));
        moduleData.put("moduleName", Data.get("moduleName"));
        moduleData.put("width", Data.get("moduleWidth"));
        moduleData.put("height", Data.get("moduleHeight"));
        moduleData.put("depth", Data.get("moduleDepth"));
        ModuleInputData moduleInputData = new ModuleInputData(moduleData);

        String roomTypeFromInput = moduleInputData.getRoomType();
        String roomType = (roomTypeFromInput != null)
                ? roomTypeFromInput.substring(0, 1).toUpperCase() + roomTypeFromInput.substring(1).toLowerCase()
                : null;

        roomConfigurationParams.put("roomType", roomType);
        roomConfigurationParams.put("name", moduleInputData.getRoomName());

        System.out.println("Processing Module ID: " + moduleInputData.getModuleId());

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

        String zoneName = moduleInputData.getSerialNo() + "_" + moduleInputData.getModuleId() + "_" + moduleInputData.getModuleName();
        zoneInfo.put("moduleName", zoneName);

        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(),
                moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

        moduleService.processWardrobeComponents(projectID, floorID, currentRoomId, currentZoneId,
                moduleInputData.getModuleId(), categoryId, subCategoryId, miqModuleObjectId,
                moduleInputData.getInternalId(), moduleInputData.getInternalWidth(),
                moduleInputData.getInternalDepth(), moduleInputData.getInternalHeight(),
                moduleInputData.getAccessoryCode(), roomType, isInternals, token
        );
    }
}
