package configurator;

import io.restassured.response.Response;
import org.json.simple.JSONObject;
import utils.PropertiesReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Utilities.updatePositionInModule;

public class RoomModuleHandler extends TestConfig {
    public static final String OBJECT_ID = "objectId";
    public static final String MIQ_MODULES_OBJECT_ID = "miqModules[0].objectId";
    public static final String ASSETS_OBJECT_ID = "assets.objectId";
    public static final String SHUTTERS_ACCESSORY = "miqModules[0].shuttersWithAccessory";
    public static final String SHUTTERS_ACCESSORY_ID = "miqModules[0].shuttersWithAccessory.objectId";
    public static final String SHUTTERS_EXPOSED_PANELS = "miqModules[0].groupsPanels";

    public Map<String, String> roomRegistry = new HashMap<>();
    public Map<String, JSONObject> customZonesDataRegistry = new HashMap<>();
    public Map<String, Integer> roomTypeCounts = new HashMap<>();
    public Map<String, String> roomConfigurationParams = new HashMap<>();

    public String currentRoomId = null;
    public String dimension = null;
    public JSONObject currentCustomZoneData;
    public String currentZoneId = null;

    public String moduleObjectId = null;
    public String miqModuleObjectId = null;
    public String assetObjectId = null;
    public List<String> shutterWithAccessoryObjectIds = null;
    public List<Map<String,String>> miqModulesShutterWithAccessory = null;
    public List<Map<String, String>> miqModulesGroupsPanels = null;
    int modulesInRoom = (new PropertiesReader(testData)).getModulesInRoom();

    public void handleRoomAndModule(String roomType, Map<String, String> zoneDetails, String moduleId,
                                    String moduleWidth, String moduleDepth, String moduleHeight) {
        String profileType = synapseService.getModuleProfileById(moduleId);
//        if (Objects.equals(orderProfileType, "DC") &&
//                (Objects.equals(profileType, "Gola") || Objects.equals(profileType, "GP"))) {
//
//            ReporterInfo(moduleId + " is a Gola/GP Module under DC profile");
//            throw new SkipException("Skipping test due to Gola/GP Module in DC profile.");
//        }

        String roomName = profileType != null ? roomType + profileType : roomType;

        updateRoomTypeCount(roomName);
        if (shouldCreateNewRoom(roomName)) {
            createNewRoom(roomType,roomName, profileType, zoneDetails);
        } else {
            assignExistingRoom(roomType,roomName, profileType, zoneDetails);
        }

        // Add custom zone
        ReporterInfo("Adding custom zone for room ID: " + currentRoomId);
        Response customZoneResponse = moduleService.addCustomZone(projectID, floorID, currentRoomId, currentCustomZoneData, token);
        if (customZoneResponse.getStatusCode() == 200) {
            currentZoneId = customZoneResponse.jsonPath().getString("objectId");
            ReporterPass("Custom zone added successfully with ID: " + currentZoneId);
            customZonesDataRegistry.put(roomName, currentCustomZoneData);
        } else {
            ReporterFail("Failed to add custom zone. Status code: " + customZoneResponse.getStatusCode());
        }

        Map<String, String> updatedValues = validateAndUpdateModuleDimensions(moduleId, moduleWidth, moduleDepth, moduleHeight);
        moduleWidth = updatedValues.get("width");
        moduleDepth = updatedValues.get("depth");
        moduleHeight = updatedValues.get("height");
        addingModule(moduleId, moduleWidth, moduleDepth, moduleHeight);
    }

    private void updateRoomTypeCount(String roomName) {
        roomTypeCounts.merge(roomName, 1, Integer::sum);
    }
    private String currentRoom(String roomName) {
        int count = roomTypeCounts.get(roomName);

        if (modulesInRoom == 1) {
            if (count == 1) {
                return roomName;
            }
            return roomName + (count - 1);
        }

        int index = (count - 1) / modulesInRoom;
        return index == 0 ? roomName : roomName + index;
    }


    private boolean shouldCreateNewRoom(String roomName) {
        int count = roomTypeCounts.get(roomName);
        return count % modulesInRoom == 1 && count > modulesInRoom || modulesInRoom == 1;
    }

    private void createNewRoom(String roomType,String roomName, String profileType, Map<String, String> zoneDetails) {
        roomType = checkProfileType(profileType) != null? checkProfileType(profileType) : roomType;
        Map<String, Object> roomCreationResponse = roomService.handleMultiRoomCreation(roomType,roomName, roomTypeCounts.get(roomName),
                projectID, floorID, zoneDetails,
                roomRegistry, token);
        currentCustomZoneData = (JSONObject) roomCreationResponse.get("customZone");
        roomRegistry = (Map<String, String>) roomCreationResponse.get("listOfRooms");
        currentRoomId = roomCreationResponse.get("roomId").toString();
        roomService.convertGolaOrGP(profileType, projectID, floorID, currentRoomId, token);
    }

    private void assignExistingRoom(String roomType,String roomName, String profileType, Map<String, String> zoneDetails) {
        roomType = checkProfileType(profileType) != null? checkProfileType(profileType) : roomType;
       String currentRoomName = currentRoom(roomName);

        if (roomRegistry.containsKey(currentRoomName)) {
            currentRoomId = roomRegistry.get(currentRoomName);
            currentCustomZoneData = updatePositionInModule(roomType, customZonesDataRegistry.get(roomName), zoneDetails);
        } else {
            System.out.println("Creating room as  : "+roomType);
            Map<String, Object> singleRoomResponse = roomService.handleRoomCreation(roomType,roomName, projectID, floorID, zoneDetails, token);
            currentCustomZoneData = (JSONObject) singleRoomResponse.get("customZone");
            roomRegistry = (Map<String, String>) singleRoomResponse.get("listOfRooms");
            currentRoomId = singleRoomResponse.get("roomId").toString();
        }
        roomService.convertGolaOrGP(profileType, projectID, floorID, currentRoomId, token);
    }

    private Map<String, String> validateAndUpdateModuleDimensions(String moduleId, String moduleWidth, String moduleDepth, String moduleHeight) {
        Map<String, String> updatedDimensions = new HashMap<>();

        if (moduleWidth == null || moduleDepth == null || moduleHeight == null) {
            Map<String, String> moduleDimensions = synapseService.getModuleDimension(moduleId);
            moduleWidth = moduleDimensions.get("width");
            moduleHeight = moduleDimensions.get("height");
            moduleDepth = moduleDimensions.get("depth");
        }
        dimension = moduleWidth + " X " + moduleDepth + " X " + moduleHeight;
        System.out.println(dimension);
        updatedDimensions.put("width", moduleWidth);
        updatedDimensions.put("depth", moduleDepth);
        updatedDimensions.put("height", moduleHeight);

        return updatedDimensions;
    }

    private void addingModule(String moduleId, String moduleWidth, String moduleDepth, String moduleHeight) {

        Response response = moduleService.addModule(projectID, floorID, currentRoomId, currentZoneId, moduleId,
                moduleWidth, moduleDepth, moduleHeight, token);

        if (response.getStatusCode() == 200) {
            ReporterPass("Module added successfully.");
            extractModuleDetails(response, moduleWidth, moduleDepth, moduleHeight);
        } else {
            ReporterFail("Failed to add module.  Status code: " + response.getStatusCode());
        }
    }

    private void extractModuleDetails(Response response, String moduleWidth, String moduleDepth, String moduleHeight) {
        moduleObjectId = response.jsonPath().getString(OBJECT_ID);
        miqModuleObjectId = response.jsonPath().getString(MIQ_MODULES_OBJECT_ID);
        assetObjectId = response.jsonPath().getString(ASSETS_OBJECT_ID);
        shutterWithAccessoryObjectIds = response.jsonPath().get(SHUTTERS_ACCESSORY_ID);
        miqModulesShutterWithAccessory = response.jsonPath().get(SHUTTERS_ACCESSORY);
        miqModulesGroupsPanels = response.jsonPath().get(SHUTTERS_EXPOSED_PANELS);
        moduleService.updateDimension(projectID, floorID, currentRoomId, moduleObjectId, moduleWidth, moduleDepth, moduleHeight, token);
    }
    private String checkProfileType(String profileType){

        if (profileType != null && profileType.equalsIgnoreCase("gola")){
          return profileType;
        }else if (profileType != null && profileType.equalsIgnoreCase("gp")){
            return profileType;
        }
        return null;
    }

}
