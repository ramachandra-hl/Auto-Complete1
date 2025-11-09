package test;

import models.ModuleInputData;
import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import utils.DataProviderUtil;
import java.util.HashMap;
import java.util.Map;


public class ModuleFillerAdderTest extends RoomModuleHandler {
    @Test(dataProvider = "MiscModuleDataProvider",dataProviderClass = DataProviderUtil.class)
    void fillerAddition(Map<String, String> roomDetails)  {
        ModuleInputData moduleInputData = new ModuleInputData(roomDetails);
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

        addFiller(moduleInputData.getFillerId());
    }

    public void addFiller(String fillerId) {
        Map<String, String> fillerDimensions = synapseService.getModuleDimension(fillerId);
        String fillerWidth = fillerDimensions.get("width");
        String fillerHeight = fillerDimensions.get("height");
        String fillerDepth = fillerDimensions.get("depth");

        Response fillerCreationResponse = moduleService.addModule(projectID, floorID, currentRoomId, currentZoneId, fillerId,
                fillerWidth, fillerDepth, fillerHeight, token);
        if (fillerCreationResponse.getStatusCode() == 200) {
            ReporterPass("Filler added successfully.");
        } else {
            ReporterFail("Failed to add filler. Status code: " + fillerCreationResponse.getStatusCode());
        }
    }


}
