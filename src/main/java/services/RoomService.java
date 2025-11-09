package services;

import configurator.BaseClass;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import utils.PropertiesReader;
import utils.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static configurator.ApiService.*;

import static utils.PropertiesReader.*;
import static utils.Utilities.*;

public class RoomService {
    Map<String, String> listOfRooms = new HashMap<>();
    protected final static Logger log = LoggerFactory.getLogger(RoomService.class);
    Response response;
    ScBackendService scBackendService = new ScBackendService();
    PropertiesReader propertiesReader = new PropertiesReader();
    String baseURl = propertiesReader.getBaseURl();

    public Response getProjectDetails(String projectId, Map<String, String> token) {
        System.out.println("🌐 Base URL: " + baseURl);
        logAndReport("🔍 Fetching project details for project ID :️️➡️ {}", projectId);
        response = invokeGetRequest(baseURl + "/api/v1.0/project/" + projectId, token);
        Assert.assertEquals(response.statusCode(), 200, response.body().asPrettyString());
        logAndReport("🏆 Project details retrieved successfully.");
        return response;

    }

    public void createWall(String projectId, String floorId, Map<String, String> token, JSONArray data) {
        logAndReport("Creating wall for project ID: {} and floor ID: {} with provided data.", projectId, floorId);
        response = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId + "/walls", data, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logAndReport("Wall created successfully.");
    }

    public Response createRoom(String projectId, String floorId, Map<String, String> token, JSONObject data) {
        logAndReport("Creating room for project ID: {} and floor ID: {} with provided data.", projectId, floorId);
        response = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId + "/room", data, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logAndReport("Room created successfully.");
        return response;
    }

    public Response deleteRoom(String projectId, String floorId, String roomId, Map<String, String> token) {
        logAndReport("Deleting room with ID: {} from project ID: {} and floor ID: {}", roomId, projectId, floorId);
        response = invokeDeleteRequest(baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId + "/rooms/" + roomId, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logAndReport("Room with ID: {} deleted successfully.", roomId);
        return response;
    }

    public Response deleteWall(String projectId, String floorId, JSONArray payload, Map<String, String> token) {
        logAndReport("Deleting walls for project ID: {} and floor ID: {}", projectId, floorId);
        response = invokeDeleteRequest(baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId + "/walls", payload, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logAndReport("Walls deleted successfully.");
        return response;
    }

    public Boolean deleteAllRoomsAndWalls(String projectId, String floorId, Map<String, String> headers) {
        boolean isAllRoomsWithWallDeleted =false;
        Response projectDetails = getProjectDetails(projectId, headers);
        List<String> roomIds = (List<String>) projectDetails.jsonPath().getList("floors.rooms.id").get(0);
        Set<Object> wallIds = projectDetails.jsonPath().getMap("floors.floorWalls[0]").keySet();
        logAndReport("🗑️ Deleting all rooms and walls for project ID: {} and floor ID: {}", projectId, floorId);

        // Delete rooms if found
        if (roomIds != null && !roomIds.isEmpty()) {
            for (String roomId : roomIds) {
                Response res1 = deleteRoom(projectId, floorId, roomId, headers);
                if (res1.getStatusCode() == 200) {
                    logAndReport("Deleted room with ID: {}", roomId);
                } else {
                    log.warn("Failed to delete room with ID: {}. Status code: {}", roomId, res1.getStatusCode());
                }
            }
        } else {
            log.warn("No rooms found to delete.");
        }

        // Prepare to delete walls
        if (!wallIds.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (Object wallId : wallIds) {
                jsonArray.add(wallId.toString());
            }

            Response res2 = deleteWall(projectId, floorId, jsonArray, headers);
            if (res2.getStatusCode() == 200) {
                logAndReport("Deleted walls successfully.");
            } else {
                log.warn("Failed to delete walls. Status code: {}", res2.getStatusCode());
            }
        } else {
            log.warn("No walls found to delete.");
        }

        // Check if there are remaining rooms or walls
        projectDetails = getProjectDetails(projectId, headers);
        roomIds = (List<String>) projectDetails.jsonPath().getList("floors.rooms.id").get(0);
        wallIds = projectDetails.jsonPath().getMap("floors.floorWalls[0]").keySet();

        // Log remaining rooms
        if (roomIds != null && !roomIds.isEmpty() && !wallIds.isEmpty()) {
            log.warn("Some rooms & walls are still left. Rooms & walls to delete: {} ,{}", roomIds, wallIds);
        } else {
            isAllRoomsWithWallDeleted = true;
            logAndReport("All rooms & walls successfully deleted.");
        }
        return isAllRoomsWithWallDeleted;
    }


    public Map<String, Object> createMultiRooms(int count, String roomType, String roomName) {
        Map<String, Object> updatedPayloads = new HashMap<>();
        int modulesInRoom = propertiesReader.getModulesInRoom();
        int quotient = count / modulesInRoom;
        int remainder = count % modulesInRoom;
        if (remainder == 1 || modulesInRoom == 1) {
            int width = 15000 * quotient;
            JSONArray updatedWallData = updateWallDataInYAxis(width, roomType);
            roomName += quotient;
            JSONObject updateRoomData = updateRoomDetails(roomName, updatedWallData, roomType);
            JSONObject updatedCustomZone = updateZoneZValueBasedOnZoneType(updatedWallData, roomType);
            updatedPayloads.put("wallPayload", updatedWallData);
            updatedPayloads.put("roomPayload", updateRoomData);
            updatedPayloads.put("customZonePayload", updatedCustomZone);
            return updatedPayloads;

        }
        return updatedPayloads;
    }


    public Map<String, Object> handleRoomCreation(String roomType, String roomName, String projectID, String floorID, Map<String, String> zoneData, Map<String, String> token) {
        JSONArray wallPayLoad = updateWallKey(roomType);
        JSONObject roomPayload = updateRoomDetails(roomName, wallPayLoad, roomType);
        String roomId = null;
        roomName = roomPayload.get("name").toString();
        String customzonePath = getResourceFile(getCustomZoneFilePathBasedOnZoneType(roomType));

        Map<String, Object> res = new HashMap<>();

        JSONObject customZoneData = Utilities.updatePositionInCustomZone(customzonePath, zoneData);
        // Creating wall

        createWall(projectID, floorID, token, wallPayLoad);
        // Creating room
        Response roomRes = createRoom(projectID, floorID, token, roomPayload);
        List<Map<String, Object>> rooms = roomRes.jsonPath().getList("rooms");
        boolean roomFound = false;

        for (Map<String, Object> room : rooms) {
            if (room.get("name").toString().equalsIgnoreCase(roomName)) {
                roomId = (String) room.get("id");
                res.put("roomId", roomId);
                roomFound = true;
            }
        }

        if (roomFound) {
            listOfRooms.put(roomName, roomId);
            res.put("customZone", customZoneData);
            res.put("listOfRooms", listOfRooms);
            return res;
        }
        return res;
    }

    public Map<String, Object> handleMultiRoomCreation(String roomType, String roomName, int count, String projectID, String floorID, Map<String, String> zoneData, Map<String, String> listOfRooms, Map<String, String> token) {
        System.out.println("creating roomType : " + roomType);
        Map<String, Object> payloads = createMultiRooms(count, roomType, roomName);
        JSONObject roomPayload = (JSONObject) payloads.get("roomPayload");
        JSONArray wallPayload = (JSONArray) payloads.get("wallPayload");
        JSONObject zonePayload = (JSONObject) payloads.get("customZonePayload");
        roomName = roomPayload.get("name").toString();
        String roomId = null;
        Map<String, Object> res = new HashMap<>();

        JSONObject customZoneData = Utilities.updatePositionInCustomZone(zonePayload, zoneData);

        // Creating wall
        createWall(projectID, floorID, token, wallPayload);
        Response roomRes = createRoom(projectID, floorID, token, roomPayload);
        List<Map<String, Object>> rooms = roomRes.jsonPath().getList("rooms");
        boolean roomFound = false;
        for (Map<String, Object> room : rooms) {
            if (room.get("name").toString().equalsIgnoreCase(roomName)) {
                roomId = (String) room.get("id");
                res.put("roomId", roomId);
                roomFound = true;
            }
        }

        if (roomFound) {
            listOfRooms.put(roomName, roomId);
            res.put("customZone", customZoneData);
            res.put("listOfRooms", listOfRooms);
            return res;
        }
        return res;
    }

    public void convertGolaOrGP(String profileType, String projectId, String floorId, String roomId, Map<String, String> token) {
        scBackendService.convertRoomSetting(profileType, projectId, floorId, roomId, token);
    }

    public Response resetFloorPlan(String projectId, Map<String, String> token) {
        String url = baseURl+"/api/v1.0/project/" + projectId + "/resetFloorPlan";
        Response response = invokeDeleteRequest(url, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("Floor plan reset successfully for projectId: {}", projectId);
        return response;
    }
}