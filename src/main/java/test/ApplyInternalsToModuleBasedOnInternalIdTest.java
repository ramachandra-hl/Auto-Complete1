package test;

import configurator.RoomModuleHandler;
import models.ModuleInputData;
import org.testng.annotations.Test;
import utils.DataProviderUtil;

import java.util.HashMap;
import java.util.Map;


public class ApplyInternalsToModuleBasedOnInternalIdTest extends RoomModuleHandler {

    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    public void applyInternalsToModuleBasedOnInternalId(Map<String, String> roomDetailsMap) {

        // Extract module data from the response
    Map<String,String> moduleData = synapseService.getModuleByInternalId(roomDetailsMap.get("internalId"));
        System.out.println("Module Data: " + moduleData);

        roomDetailsMap.put("moduleId",moduleData.get("moduleID"));
        roomDetailsMap.put("moduleName",moduleData.get("moduleName"));
        roomDetailsMap.put("width",moduleData.get("moduleWidth"));
        roomDetailsMap.put("height",moduleData.get("moduleHeight"));
        roomDetailsMap.put("depth",moduleData.get("moduleDepth"));
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
