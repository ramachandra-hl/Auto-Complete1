package test;

import models.ModuleInputData;
import configurator.RoomModuleHandler;
import org.testng.annotations.Test;
import utils.DataProviderUtil;

import java.util.*;


public class RoomConfigurationWithModuleAdditionTest extends RoomModuleHandler {
    @Test(dataProvider = "MiscModuleDataProvider", dataProviderClass = DataProviderUtil.class)
    void configureRoomWithModulesAndMaterials(Map<String, String> roomDetailsMap) {
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
        zoneInfo.put("categoryId", moduleInputData.getCategoryId()!=null? moduleInputData.getCategoryId() : categoryId);
        zoneInfo.put("subCategoryId",moduleInputData.getSubCategoryId()!=null? moduleInputData.getSubCategoryId(): subCategoryId);
        zoneInfo.put("categoryName", moduleInputData.getCategoryName()!= null?moduleInputData.getCategoryName(): categoryInfo.get("categoryName"));
        zoneInfo.put("subCategoryName",moduleInputData.getSubCategoryName()!=null? moduleInputData.getSubCategoryName(): categoryInfo.get("subCategoryName"));
        String zoneName = moduleInputData.getSerialNo()+"_"+moduleInputData.getModuleId()+"_"+moduleInputData.getModuleName();
        zoneInfo.put("moduleName",zoneName);
        handleRoomAndModule(roomType, zoneInfo, moduleInputData.getModuleId(), moduleInputData.getWidth(), moduleInputData.getDepth(), moduleInputData.getHeight());

        moduleService.applyShutterForModule(projectID, floorID, currentRoomId, currentZoneId,
                categoryId, subCategoryId, moduleInputData.getModuleId(), miqModuleObjectId,
                miqModulesShutterWithAccessory, miqModulesGroupsPanels, moduleInputData.getShutterDetailsList(), roomType,
                dimension, moduleInputData.getShutterCategory(), isShutter, token
        );

        moduleService.applyCarcassForModule(projectID, floorID, currentRoomId, currentZoneId, roomType, categoryId, subCategoryId, moduleInputData.getModuleId(),
                moduleInputData.getCarcassCode(),
                miqModuleObjectId, moduleInputData.getCarcassColour(), dimension, isCarcass, token
        );
        moduleService.addAccessoriesToKitchenModule(projectID, floorID, currentRoomId, currentZoneId,
                categoryId, subCategoryId, miqModuleObjectId, shutterWithAccessoryObjectIds,
                moduleInputData.getModuleId(), dimension, moduleInputData.getAccessoryCode(), roomType, isAccessories, token
        );

        moduleService.processWardrobeComponents(projectID, floorID, currentRoomId, currentZoneId,
                moduleInputData.getModuleId(), categoryId, subCategoryId, miqModuleObjectId,
                moduleInputData.getInternalId(), moduleInputData.getInternalWidth(),
                moduleInputData.getInternalDepth(), moduleInputData.getInternalHeight(),
                moduleInputData.getAccessoryCode(), roomType, isInternals, token
        );

        moduleService.addHandlesToModule(projectID, floorID, currentRoomId, currentZoneId,
                categoryId, subCategoryId, moduleInputData.getHandleId(), token
        );

    }
}
