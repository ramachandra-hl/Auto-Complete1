package test;

import configurator.RoomModuleHandler;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ConvertRoomsProfileTypeTest extends RoomModuleHandler {
    private static final Logger logger = Logger.getLogger(ConvertRoomsProfileTypeTest.class.getName());

    @Test
    public void convertRegularToGolaOrGp() {
        String roomSettingType = "gp";
        Response projectDetails = roomService.getProjectDetails(projectID, token);

        floorID = projectDetails.jsonPath().getString("selectedFloorId");
        List<Map<String, Object>> roomsDetails = projectDetails.jsonPath().getList("floors[0].rooms");
        int count = 0;

        for (Map<String, Object> roomDetails : roomsDetails) {
            String roomType = roomDetails.get("roomType").toString();

            if ("Kitchen".equalsIgnoreCase(roomType)) {
                String roomId = roomDetails.get("id").toString();
                Map<String, Object> roomSettings = (Map<String, Object>) roomDetails.get("roomSettings");
                Map<String, Object> golaProfileData = (Map<String, Object>) roomSettings.get("golaProfileData");
                String profileType = golaProfileData.get("profileType").toString();

                if ("REGULAR".equalsIgnoreCase(profileType)) {
                    logger.info("Converting Room ID " + roomId + " to profile type: " + roomSettingType);
                    scBackendService.convertRoomSetting(roomSettingType, projectID, floorID, roomId, token);
                    count++;
                }
            }
        }
        if (count == 0) {
            logger.info("No Regular Kitchen Rooms found for conversion.");
        } else {
            logger.info(count + " Regular Kitchen Room(s) converted successfully.");
        }
    }
}
