package test;

import configurator.ApiService.*;
import configurator.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;

public class DeleteAllRoomsInFloorTest extends TestConfig {

    @Test(priority = 0)
    public void resetFloorPlan() {
        ReporterInfo("Resetting floor plan for projectID: " + projectID);
            Response response = roomService.resetFloorPlan(projectID, token);
            if (response.getStatusCode() == 200) {
                ReporterPass("Floor plan reset successfully.");
            } else {
                ReporterFail("Failed to reset floor plan. Status: " + response.getStatusCode());
            }
    }

    @Test(priority = 1)
    public void deleteRooms() {
        ReporterInfo("Deleting existing rooms and walls for projectID: " + projectID + ", floorID: " + floorID);
      Boolean isAllRoomsDeleted=   roomService.deleteAllRoomsAndWalls(projectID, floorID, token);

        if (isAllRoomsDeleted) {
            ReporterPass("Deleted existing rooms and walls successfully.");
        } else {
            ReporterFail("Failed to delete existing rooms and walls.");
        }
    }
}
