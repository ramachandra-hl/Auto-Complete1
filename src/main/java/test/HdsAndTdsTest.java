package test;

import configurator.TestConfig;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import services.HdsAndAdsService;
import utils.DataProviderUtil;
import utils.Utilities;

import java.util.Map;

public class HdsAndTdsTest extends TestConfig {

    private static final Logger logger = LoggerFactory.getLogger(HdsAndTdsTest.class);

    @Test(dataProvider = "HdsAndTdsDataProvider", dataProviderClass = DataProviderUtil.class)
    public void addService(Map<String, String> roomDetailsMap) {
        logger.info("Room Details Map: {}", roomDetailsMap);

        Response response = roomService.getProjectDetails(projectID, token);
        existingRoomRegistry = Utilities.extractRoomTypeIdMap(response);
        logger.info("Existing Room Registry: {}", existingRoomRegistry);

        String roomId = existingRoomRegistry.get(roomDetailsMap.get("roomType"));
        if (roomId == null) {
            logger.error("Room type not found in registry: {}", roomDetailsMap.get("roomType"));
            throw new RuntimeException("Room type not found in registry: " + roomDetailsMap.get("roomType"));
        }

        HdsAndAdsService hdsAndAdsService = new HdsAndAdsService();

        String hdsType = roomDetailsMap.get("hdsType");
        switch (hdsType.toLowerCase()) {
            case "service":
                logger.info("Applying HDS service for roomType: {}", roomDetailsMap.get("roomType"));
                hdsAndAdsService.applyHdsService(
                        projectID,
                        floorID,
                        roomId,
                        roomDetailsMap.get("subCategoryId"),
                        roomDetailsMap.get("roomType"),
                        hdsType,
                        roomDetailsMap.get("skuId"),
                        token
                );
                break;

            case "loosefurniture":
                logger.info("Applying Loose Furniture for roomType: {}", roomDetailsMap.get("roomType"));
                hdsAndAdsService.applyLooseFurniture(
                        projectID,
                        floorID,
                        roomId,
                        roomDetailsMap.get("subCategoryId"),
                        roomDetailsMap.get("roomType"),
                        "LOOSE_FURNITURE",
                        roomDetailsMap.get("skuId"),
                        token
                );
                break;

            case "appliance":
                logger.info("Applying Appliance for roomType: {}", roomDetailsMap.get("roomType"));
                hdsAndAdsService.applyLooseFurniture(
                        projectID,
                        floorID,
                        roomId,
                        roomDetailsMap.get("subCategoryId"),
                        roomDetailsMap.get("roomType"),
                        "APPLIANCE",
                        roomDetailsMap.get("skuId"),
                        token
                );
                break;

            default:
                logger.error("Unsupported HDSType: {}", hdsType);
                throw new IllegalArgumentException("Unsupported HDSType: " + hdsType);
        }
    }
}
