package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.testng.Assert;
import payloadHelper.ModuleServicePayloadHelper;
import utils.PropertiesReader;
import utils.UpdateJsons;
import java.util.*;

import static configurator.ApiService.*;
import static configurator.BaseClass.orderProfileType;
import static utils.Utilities.*;

public class ModuleService {
    private static final Logger logger = LogManager.getLogger(ModuleService.class);
    private static final String MATERIAL_TYPE_SHUTTER = "shutter";
    private static final String MATERIAL_TYPE_GLASS = "glass";
    private static final String MATERIAL_TYPE_MIRROR = "mirror";

    PropertiesReader propertiesReader = new PropertiesReader();
    String isHandle = propertiesReader.getIsHandleRequired();
    String baseURl = propertiesReader.getBaseURl();

    UpdateJsons updateJsons = new UpdateJsons();
    ModuleServicePayloadHelper moduleServicePayloadHelper = new ModuleServicePayloadHelper();
    Response response;
    SynapseService synapseService = new SynapseService();

    public Response addCustomZone(String projectId, String floorId, String roomId, JSONObject data, Map<String, String> token) {

        response = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId + "/room/" + roomId + "/customzone", data, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logger.info("Custom zone is successfully added");
        return response;
    }

    public Response addModule(String projectId, String floorId, String roomId, String zoneID, String moduleID, String width, String depth, String height, Map<String, String> token) {
        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("width", width);
        moduleData.put("depth", depth);
        moduleData.put("height", height);
        String modulePayloadPath = "payloads/addModule.json";
       String payload =  updateJsons.updatePayloadFromMap(modulePayloadPath,moduleData);

        response = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId + "/room/" + roomId + "/zone/" + zoneID + "/module/" + moduleID, payload, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logger.info("Module with dimensions is successfully added");
        return response;
    }

    public void updateDimension(String projectId, String floorId, String roomId, String unitEntryId, String width, String length, String height, Map<String, String> token) {
        String payload = moduleServicePayloadHelper.dimensionPayload(width, length, height);
        response = invokePutRequest(baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId + "/rooms/" + roomId + "/unitEntries/" + unitEntryId + "/dimension", payload, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
    }


    public Response deleteCustomZone(String projectId, String floorId, String roomId, String zoneId, Map<String, String> token) {

        response = invokePutRequest(baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId + "/rooms/" + roomId + "/unitEntries/" + zoneId, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logger.info("zone is successfully deleted");
        return response;
    }

    public Response updateModules(String projectID, String floorID, String currentRoomId, String currentZoneId, List<Map<String, Object>> payload, Map<String, String> token) {
        response = invokePutRequest(baseURl + "/api/v1.0/project/" + projectID + "/floors/" + floorID + "/rooms/" + currentRoomId + "/unitEntries/" + currentZoneId + "/modules", payload, token);
        Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
        logger.info("module updated successfully");
        return response;
    }

    public void applyCarcassForModule(String projectId, String floorId, String roomId, String zoneId, String roomType, String categoryId, String subCategoryId, String moduleId, String carcassCode, String moduleObjectId, String carcassColour, String dimension, String isCarcass, Map<String, String> token) {
        if (isCarcass.equalsIgnoreCase("TRUE") && carcassCode != null) {
            System.out.println("Applying colour for carcass: " + carcassColour);
            Response carcassResponse = applyCarcass(projectId, floorId, roomId, zoneId, roomType, categoryId, subCategoryId, moduleId, dimension, carcassCode, moduleObjectId, carcassColour, token);
            if (carcassResponse != null && carcassResponse.getStatusCode() == 200) {
                logger.info("Carcass applied successfully.");
            } else {
                logger.error("Failed to apply carcass. Status code: {}", carcassResponse.getStatusCode());
            }
        }
    }


    public Response applyCarcass(String projectId, String floorId, String roomId, String zoneId, String roomType, String categoryId, String subCategoryId, String moduleId, String dimension, String carcassCode, String moduleObjectId, String carcassColourId, Map<String, String> token) {
        Response getZoneDataRes = synapseService.getZoneData(roomType, zoneId, categoryId, subCategoryId, dimension, moduleId);
        List<Map<String, String>> carcassData = (List<Map<String, String>>) getZoneDataRes.jsonPath().getList("data.getZoneData.graphqlModules.graphqlCarcasses").get(0);
        String carcassName = "";
        for (Map<String, String> carcass : carcassData) {
            if (carcass.get("carcassCode").equalsIgnoreCase(carcassCode)) {
                carcassName = carcass.get("carcassName");
                break;
            }
        }
if (carcassName == null && carcassName.isEmpty()) {
            logger.error("Carcass name not found for code: {}", carcassCode);
            return null; // or throw an exception
}
        Response MaterialColorCarcasResponse = synapseService.getMaterialColor("cabinet", carcassCode);
        List<Map<String, Object>> carcasColourDetailsList = MaterialColorCarcasResponse.jsonPath().getList("data.getColour");


        Map<String, String> carcasMap = new HashMap<>();
        String carcassColourName = "";
        String carcassColourImage = "";
        String carcassMaterialId = "";
        String carcassColourFinishGroup = "";

        for (Map<String, Object> map : carcasColourDetailsList) {
            if (map.get("colourId").toString().equals(carcassColourId)) {
                carcassColourName = map.get("colourName").toString();
                carcassColourImage = map.get("colourImage").toString();
                carcassMaterialId = map.get("assetMaterialId").toString();
                if (orderProfileType.equalsIgnoreCase("DC"))
                carcassColourFinishGroup =map.get("finishGroup").toString();
                break;
            }
        }

        carcasMap.put("carcassCode", carcassCode);
        carcasMap.put("carcassName", carcassName);
        carcasMap.put("carcassColourCode", carcassColourId);
        carcasMap.put("carcassColourImage", carcassColourImage);
        carcasMap.put("carcassColourName", carcassColourName);
        carcasMap.put("carcassMaterialId", carcassMaterialId);
        carcasMap.put("finishGroup",carcassColourFinishGroup);
        carcasMap.put("moduleObjectId", moduleObjectId);

        Response applyCarcasMaterialResponse = applyCarcassMaterial(projectId, floorId, roomId, zoneId, carcasMap, token);
        Assert.assertEquals(applyCarcasMaterialResponse.getStatusCode(), 200);
        return applyCarcasMaterialResponse;
    }

    public Response applyCarcassMaterial(String projectId, String floorId, String roomId, String uId, Map<String, String> payloadData, Map<String, String> token) {
        System.out.println(moduleServicePayloadHelper.carcassMaterialPayload(payloadData));
        return response = invokePutRequest(baseURl + "/api/v1.0/projects/" + projectId + "/floors/" + floorId + "/rooms/" + roomId + "/unitEntries/" + uId + "/material/carcass", moduleServicePayloadHelper.carcassMaterialPayload(payloadData), token);
    }


    public void applyShutterForModule(
            String projectID, String floorID, String currentRoomId, String currentZoneId,
            String categoryId, String subCategoryId, String moduleIdFromInput, String miqModuleObjectId,
            List<Map<String, String>> miqModulesShutterWithAccessory, List<Map<String, String>> miqModulesGroupsPanels,
            Map<String, Object> shutterDetailsListFromInput, String roomType, String dimension,
            String shutterCategoryFromInput, String isShutter, Map<String, String> token
    ) {
        if (!"TRUE".equalsIgnoreCase(isShutter)) return;

        if ("DC".equalsIgnoreCase(orderProfileType)) {
            System.out.println(categoryId);
            if (!categoryId.equalsIgnoreCase("10016")) {
                applyShutterForNonWardrobeInDC(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId, miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, token);
            } else {
                applyShutterMaterialsForDC(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId, miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, token);
            }
            return;
        }
        Response shutterResponse = handleBySingleComponent(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId, miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, shutterCategoryFromInput, token);
        logShutterResponse(shutterResponse, miqModuleObjectId);

    }


    private Response handleBySingleComponent(
            String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId, String subCategoryId,
            String moduleIdFromInput, String miqModuleObjectId, List<Map<String, String>> miqModulesShutterWithAccessory,
            List<Map<String, String>> miqModulesGroupsPanels, Map<String, Object> shutterDetailsListFromInput,
            String roomType, String dimension, String shutterCategoryFromInput, Map<String, String> token

    ) {
        if ("WOODEN".equalsIgnoreCase(shutterCategoryFromInput)) {
            return applyShutter(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId, miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, token);
        } else if ("ALUMINIUM".equalsIgnoreCase(shutterCategoryFromInput)) {
            return multiPartShutter(projectID, floorID, currentRoomId, currentZoneId, moduleIdFromInput, categoryId, subCategoryId, miqModuleObjectId, shutterDetailsListFromInput, token);
        } else {
            logger.error("Unknown shutter category: {}", shutterCategoryFromInput);
            return null;
        }
    }

    public void applyShutterForNonWardrobeInDC(String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId,
                                           String subCategoryId, String moduleIdFromInput, String miqModuleObjectId,
                                           List<Map<String, String>> miqModulesShutterWithAccessory,
                                           List<Map<String, String>> miqModulesGroupsPanels,
                                           Map<String, Object> shutterDetailsListFromInput,
                                           String roomType, String dimension,
                                           Map<String, String> token) {

        String finishCategoryName = String.valueOf(shutterDetailsListFromInput.getOrDefault("finishCategoryName", ""));
        String shutterDesignsName = String.valueOf(shutterDetailsListFromInput.getOrDefault("shutterDesignsName", ""));
        String finishCategory = null;
        String shutterSubTypeId = null;
        String shutterDesignsType = null;

        Response zoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId, subCategoryId, dimension, moduleIdFromInput);
        Map<String, Object> zoneData = zoneDataRes.jsonPath().getMap("data.getZoneData");
        List<Map<String, Object>> graphqlModules = (List<Map<String, Object>>) zoneData.get("graphqlModules");
        if (graphqlModules != null && !graphqlModules.isEmpty()) {
            Map<String, Object> module = graphqlModules.get(0);
            List<Map<String, Object>> finishCategories = (List<Map<String, Object>>) module.get("finishCategories");
            if (finishCategories != null) {
                for (Map<String, Object> fc : finishCategories) {
                    if (finishCategoryName.equalsIgnoreCase(String.valueOf(fc.get("finishName")))) {
                        logger.info("Found Finish Category: {}", fc.get("finishName"));
                        finishCategory = String.valueOf(fc.get("finishName"));
                        List<Map<String, Object>> shutterDesigns = (List<Map<String, Object>>) fc.get("shutterDesigns");
                        if (shutterDesigns != null) {
                            for (Map<String, Object> sd : shutterDesigns) {
                                if (shutterDesignsName.equalsIgnoreCase(String.valueOf(sd.get("name")))) {
                                    logger.info("Found Shutter Design: {}", sd.get("name"));
                                    shutterDesignsType = String.valueOf(sd.get("type"));
                                    shutterSubTypeId = String.valueOf(sd.get("shutterSubTypeId"));
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (finishCategory == null) finishCategory = finishCategoryName;
        if (shutterSubTypeId == null) shutterSubTypeId = "";

        Response shutterTypeResponse = new ScBackendService().updateShutterType(projectID, floorID, currentRoomId, currentZoneId, miqModuleObjectId, shutterDesignsName, finishCategory, shutterSubTypeId, token);
        List<Map<String, String>> shuttersWithAccessory = shutterTypeResponse.jsonPath().getList("miqModules[0].shuttersWithAccessory");
        if (shuttersWithAccessory != null) {
            miqModulesShutterWithAccessory = shuttersWithAccessory;
        }
        applyShutter(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId,
                miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, token);
    }



    public void applyShutterMaterialsForDC(
            String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId, String subCategoryId,
            String moduleIdFromInput, String miqModuleObjectId, List<Map<String, String>> miqModulesShutterWithAccessory,
            List<Map<String, String>> miqModulesGroupsPanels, Map<String, Object> shutterDetailsListFromInput,
            String roomType, String dimension, Map<String, String> token) {

        String finishCategory = (String) shutterDetailsListFromInput.get("finishCategoryName");
        String finishType = (String) shutterDetailsListFromInput.get("finishType");
        String finishSubType = (String) shutterDetailsListFromInput.get("finishSubType");
        String designName = (String) shutterDetailsListFromInput.get("designName");
        String shutterCategoryFromInput = (String) shutterDetailsListFromInput.get("shutterCategory");

        Response shutterDesignResponse = synapseService.getWardrobeShutterDesigns(moduleIdFromInput, dimension);
        List<Map<String, Object>> shutterDesigns = shutterDesignResponse.jsonPath().getList("data.getWardrobeShutterDesigns");

        boolean found = false;
        for (Map<String, Object> design : shutterDesigns) {

            if (equalsIgnoreCaseOrNull(finishCategory, design.get("finishCategory")) &&
                    equalsIgnoreCaseOrNull(designName, design.get("designName")) &&
                    equalsIgnoreCaseOrNull(finishType, design.get("finishType")) &&
                    equalsIgnoreCaseOrNull(finishSubType, design.get("finishSubType"))
            ) {
                String wardrobeShutterType = String.valueOf(design.get("wardrobeShutterType"));
                Map<String, String> shutterDesignData = new HashMap<>();
                shutterDesignData.put("finishCategory", String.valueOf(design.get("finishCategory")));
                shutterDesignData.put("finishType", String.valueOf(design.get("finishType")));
                shutterDesignData.put("finishSubType", String.valueOf(design.get("finishSubType")));
                shutterDesignData.put("designName", String.valueOf(design.get("designName")));
                shutterDesignData.put("shutterImage", String.valueOf(design.get("shutterImage")));
                shutterDesignData.put("wardrobeShutterType", wardrobeShutterType);
                shutterDetailsListFromInput.put("shutterDesignData", shutterDesignData);

                List<Map<String, Object>> shutterData = (List<Map<String, Object>>) design.get("shutterData");
                Map<String, Integer> sectionIdMap = new HashMap<>();
                for (Map<String, Object> item : shutterData) {
                    sectionIdMap.put(String.valueOf(item.get("applicableSection")), (Integer) item.get("id"));
                }
                String shutterIds = sectionIdMap.toString().replaceAll(" ", "");
                shutterDetailsListFromInput.put("shutterCode", shutterIds);
                Response response = null;
                try {
                    if ("WOODEN".equalsIgnoreCase(wardrobeShutterType) || "WOODEN_DESIGN".equalsIgnoreCase(wardrobeShutterType)) {
                        response = applyShutter(projectID, floorID, currentRoomId, currentZoneId, categoryId, subCategoryId, moduleIdFromInput, miqModuleObjectId,
                                miqModulesShutterWithAccessory, miqModulesGroupsPanels, shutterDetailsListFromInput, roomType, dimension, token);
                    } else if ("ALUMINIUM".equalsIgnoreCase(wardrobeShutterType)) {

                        response = multiPartShutter(projectID, floorID, currentRoomId, currentZoneId, moduleIdFromInput, categoryId, subCategoryId, miqModuleObjectId, shutterDetailsListFromInput, token);
                    }
                    if (response != null && response.getStatusCode() == 200) {
                        logger.info("Shutter applied successfully.");
                    } else {
                        logger.error("Failed to apply shutter. Status code: {}", response != null ? response.getStatusCode() : "null");
                    }
                } catch (Exception e) {
                    logger.error("Error applying shutter: {}", e.getMessage());
                }
                found = true;
                break;
            }
        }

        if (!found) {
            logger.error("No matching shutter design found for given parameters: finishCategory={}, finishType={}, finishSubType={}, designName={}",
                    finishCategory, finishType, finishSubType, designName);
        }
    }


    public Response applyShutter(String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId,
                                 String subCategoryId, String moduleIdFromInput, String miqModuleObjectId,
                                 List<Map<String, String>> miqModulesShutterWithAccessory,
                                 List<Map<String, String>> miqModulesGroupsPanels,
                                 Map<String, Object> shutterDetailsListFromInput,
                                 String roomType, String dimension,
                                 Map<String, String> token) {


        String shutterCoreCode = String.valueOf(shutterDetailsListFromInput.getOrDefault("shutterCoreCode", "")).trim();
        String shutterFinishCode = String.valueOf(shutterDetailsListFromInput.getOrDefault("shutterFinishCode", "")).trim();
        String shutterColourCode = String.valueOf(shutterDetailsListFromInput.getOrDefault("shutterColourCode", "")).trim();
        String infillMaterialId = String.valueOf(shutterDetailsListFromInput.getOrDefault("infillMaterialId", "")).trim();
        String infillMaterialColourId = String.valueOf(shutterDetailsListFromInput.getOrDefault("infillMaterialColourId", "")).trim();

        if ( infillMaterialId != "null"){
          response =  applyInfillMaterialForShutter(projectID,floorID,currentRoomId,currentZoneId,categoryId,subCategoryId,moduleIdFromInput,miqModuleObjectId,miqModulesShutterWithAccessory,miqModulesGroupsPanels,shutterDetailsListFromInput,roomType,dimension,token);
            return response;
        }

        if (!Objects.equals(orderProfileType, "DC")) {
            if (shutterCoreCode.isEmpty() || shutterFinishCode.isEmpty() || shutterColourCode.isEmpty()) {
                return null;
            }
        }

        Response zoneDataRes = synapseService.getZoneData(roomType, currentZoneId, categoryId, subCategoryId, dimension, moduleIdFromInput);

        Map<String, List<Map<String, String>>> applicableMaterialForAllComponent = new HashMap<>();
        Map<String, List<Map<String, String>>> applicableMaterialForAllGroupPanels = new HashMap<>();
        List<Map<String, String>> group25Materials = zoneDataRes.jsonPath().getList("data.getZoneData.group25Materials");

        Response wardrobeMaterialResponse = synapseService.getWardrobeMaterialData(moduleIdFromInput, categoryId, subCategoryId);
        List<Map<String, String>> visibleSideMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.visibleSideMaterials");
        List<Map<String, String>> woodenMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.woodenMaterials");
        List<Map<String, String>> frameMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.frameMaterials");

        List<Map<String, String>> glassMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.glassMaterials");
        List<Map<String, String>> infillShutterMaterial = new ArrayList<>();
        Object raw = wardrobeMaterialResponse.jsonPath().get("data.getWardrobeMaterial.infillShutterMaterial");
        if (raw instanceof List) {
            infillShutterMaterial = (List<Map<String, String>>) raw;
        }

        for (Map<String, String> shutterData : miqModulesShutterWithAccessory) {
            String shutterObjectId = shutterData.get("objectId");
            String shutterName = shutterData.get("shutterName");
            String shutterNameWithObjectId = shutterName + "_" + shutterObjectId;
            if (shutterName != null) {
                List<Map<String, String>> materialList;
                if (orderProfileType.equalsIgnoreCase("DC")) {
                    String materialType = resolveMaterialType(shutterData);
                    materialList =
                            "wooden".equals(materialType) ? woodenMaterials :
                                    "frame".equals(materialType) ? frameMaterials :
                                            "infill".equals(materialType) ? infillShutterMaterial :
                                                    "glass".equals(materialType) ? glassMaterials :
                                                            Collections.emptyList();
                } else {
                    materialList = shutterName.contains("25") ? group25Materials : woodenMaterials;
                }
                applicableMaterialForAllComponent.put(shutterNameWithObjectId, materialList);
            }
        }

        for (Map<String, String> groupPanel : miqModulesGroupsPanels) {
            String groupPanelId = groupPanel.get("groupPanelId");
            String panelName = groupPanel.get("name");
            String groupPanelNameWithId = panelName + "_" + groupPanelId;
            System.out.println("Group Panel Name -> " + panelName + " And Its Object ID :" + groupPanelId);
            if (panelName != null) {
                List<Map<String, String>> materialList = panelName.contains("25") ? group25Materials : visibleSideMaterials;
                applicableMaterialForAllGroupPanels.put(groupPanelNameWithId, materialList);
            }
        }
        Map<String, Map<String, String>> materialDetailsForComponents =
                buildMaterialDetailsForComponents(applicableMaterialForAllComponent, "accessoryComponent", shutterCoreCode, shutterFinishCode, infillMaterialId, shutterColourCode, infillMaterialColourId);

        Map<String, Map<String, String>> materialDetailsForGroupPanels =
                buildMaterialDetailsForComponents(applicableMaterialForAllGroupPanels, "groupPanelsComponent", shutterCoreCode, shutterFinishCode, infillMaterialId, shutterColourCode, infillMaterialColourId);
        logger.info("Components material map size: {}", materialDetailsForComponents.size());
        logger.info("Group Panels material map size: {}", materialDetailsForGroupPanels.size());
        Response response = null;


        if (!materialDetailsForComponents.isEmpty()) {
            Response shutterRes = applyShutterMaterial(projectID, floorID, currentRoomId, currentZoneId,
                    materialDetailsForComponents, miqModuleObjectId, token);
            Assert.assertEquals(shutterRes.getStatusCode(), 200, "Shutter material application failed");
            logger.info("Shutter material applied successfully for components.");

            response = shutterRes;
        }

        if (!materialDetailsForGroupPanels.isEmpty()) {
            Response groupPanelRes = applyGroupPanel(projectID, floorID, currentRoomId, currentZoneId,
                    materialDetailsForGroupPanels, miqModuleObjectId, token);
            Assert.assertEquals(groupPanelRes.getStatusCode(), 200, "Group panel material application failed");
            logger.info("Group panel material applied successfully for components.");
            response = groupPanelRes;
        }

        return response;
    }


    public Response applyInfillMaterialForShutter(
            String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId,
            String subCategoryId, String moduleIdFromInput, String miqModuleObjectId,
            List<Map<String, String>> miqModulesShutterWithAccessory,
            List<Map<String, String>> miqModulesGroupsPanels,
            Map<String, Object> shutterDetailsListFromInput,
            String roomType, String dimension,
            Map<String, String> token) {

        String shutterCoreCode = String.valueOf(shutterDetailsListFromInput.getOrDefault("shutterCoreCode", "")).trim();
        String infillMaterialId = String.valueOf(shutterDetailsListFromInput.getOrDefault("infillMaterialId", "")).trim();
        String infillMaterialColourId = String.valueOf(shutterDetailsListFromInput.getOrDefault("infillMaterialColourId", "")).trim();

        boolean isInfillMaterial = false;
        String materialType = "";
        Map<String,Object> shutterPayload= new HashMap<>();
        Map<String, Object> mapData = new HashMap<>();
        ArrayList <Map<String, Object>> modules = new ArrayList<>();
        Map<String,Object> insideModules = new HashMap<>();
        ArrayList <Map<String, Object>> shutterMaterials = new ArrayList<>();



        String coreCode = "", finishCode = "", finishName = "", coreName = "", shutterDesignType = "", shutterFinishCategory = "";
        String colourCode = "", colourImage = "", colourName = "", materialId = "", glassMaterialId="", glassColorCode="", glassColorName="", glassMaterialName="", glassColourImage="";

        Response wardrobeMaterialResponse = synapseService.getWardrobeMaterialData(moduleIdFromInput, categoryId, subCategoryId);
        List<Map<String, String>> frameMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.frameMaterials");
        List<Map<String, String>> infillShutterMaterials = wardrobeMaterialResponse.jsonPath().get("data.getWardrobeMaterial.infillShutterMaterial[0].infillMaterials");
        List<Map<String, Object>> glassMaterials = wardrobeMaterialResponse.jsonPath().getList("data.getWardrobeMaterial.glassMaterials");

        // First, check infill shutter materials
        for (Map<String, String> infillShutterMaterial : infillShutterMaterials) {
            if (shutterCoreCode.equalsIgnoreCase(infillShutterMaterial.get("shutterCoreCode")) &&
                    infillMaterialId.equalsIgnoreCase(infillShutterMaterial.get("shutterFinishCode"))) {

                isInfillMaterial = true;
                materialType = infillShutterMaterial.get("__typename");
                coreName = infillShutterMaterial.get("shutterCoreName");
                coreCode = infillShutterMaterial.get("shutterCoreCode");
                finishName = infillShutterMaterial.get("shutterFinishName");
                finishCode = infillShutterMaterial.get("shutterFinishCode");
                shutterDesignType = infillShutterMaterial.get("shutterDesignType");
                shutterFinishCategory = infillShutterMaterial.get("shutterFinishCategory");
                break;
            }
        }

        if (!isInfillMaterial) {
            for (Map<String, Object> glassMaterial : glassMaterials) {
                if (infillMaterialId.equalsIgnoreCase(glassMaterial.get("type").toString())) {
                    isInfillMaterial = true;
                    glassMaterialName = String.valueOf(glassMaterial.get("type"));
                    boolean isMirror = (Boolean) glassMaterial.get("isMirror");
                    materialType = isMirror ? "mirror" : "glass";
                    break;
                }
            }
        }

        // If not found, fallback to frame materials
        if (!isInfillMaterial) {
            for (Map<String, String> frameMaterial : frameMaterials) {
                if (shutterCoreCode.equalsIgnoreCase(frameMaterial.get("shutterCoreCode")) &&
                        infillMaterialId.equalsIgnoreCase(frameMaterial.get("shutterFinishCode"))) {

                    isInfillMaterial = false;
                    materialType = frameMaterial.get("__typename");
                    coreName = frameMaterial.get("shutterCoreName");
                    coreCode = frameMaterial.get("shutterCoreCode");
                    finishName = frameMaterial.get("shutterFinishName");
                    finishCode = frameMaterial.get("shutterFinishCode");
                    shutterDesignType = frameMaterial.get("shutterDesignType");
                    shutterFinishCategory = frameMaterial.get("shutterFinishCategory");
                    break;
                }
            }
        }

        // Fetch color info
        Response colorRes = synapseService.getMaterialColor(materialType, materialType.equalsIgnoreCase("mirror")?"10066":finishCode.equalsIgnoreCase("")?infillMaterialId: finishCode);
        List<Map<String, Object>> colorList = colorRes.jsonPath().getList("data.getColour");


        for (Map<String, Object> colourData : colorList) {
            String colourId = String.valueOf(colourData.get("colourId"));
            if (infillMaterialColourId.equals(colourId)) {
                logger.info("Found matching infill color for colourId: {}", colourId);
                if (materialType.equalsIgnoreCase("shutter")) {
                    colourCode = colourId;
                    colourName = String.valueOf(colourData.get("colourName"));
                    colourImage = String.valueOf(colourData.get("colourImage"));
                    materialId = String.valueOf(colourData.get("assetMaterialId"));
                }else{
                    glassColorCode = colourId;
                    glassColorName = String.valueOf(colourData.get("colourName"));
                    glassMaterialId = String.valueOf(colourData.get("assetMaterialId"));
                    glassColourImage = String.valueOf(colourData.get("colourImage"));
                }
            }
        }

        insideModules.put("moduleObjectId", miqModuleObjectId);
        mapData.put("coreCode", coreCode);
        mapData.put("coreName", coreName);
        mapData.put("finishCode", finishCode);
        mapData.put("finishName", finishName);
        mapData.put("colourCode", colourCode);
        mapData.put("colourName", colourName);
        mapData.put("colourImage", colourImage);
        mapData.put("materialId", materialId);
        mapData.put("finishCategory", shutterFinishCategory);
        mapData.put("designType", shutterDesignType);
        mapData.put("finishGroup", null);
        mapData.put("glassColorCode", glassColorCode);
        mapData.put("glassColorName", glassColorName);
        mapData.put("glassColourImage", glassColourImage);
        mapData.put("glassMaterialId", glassMaterialId);
        mapData.put("glassMaterialName", glassMaterialName);


        // Now apply to shutter modules
        for (Map<String, String> shutter : miqModulesShutterWithAccessory) {
            String shutterType = shutter.get("shutterType");
            boolean isAPS = "APS".equalsIgnoreCase(shutterType) ? true :"WF".equalsIgnoreCase(shutterType);

            if ((isInfillMaterial && !isAPS) || (!isInfillMaterial && isAPS)) {
                String objectId = shutter.get("objectId");
                String designCode =String.valueOf(shutter.get("designCode"));
                String designType = shutter.get("designType");
                String subDesignId =String.valueOf( shutter.get("subDesignId"));
                String subDesignName = shutter.get("subDesignName");
                logger.info("Applying infill to objectId {} with materialId {}", objectId, materialId);


                // Add shutter-specific values to the map
                mapData.put("shutterObjectId", objectId);
                mapData.put("designCode", designCode);
                mapData.put("designName", designCode);
                mapData.put("subDesignId", subDesignId);
                mapData.put("subDesignName", subDesignName);
                mapData.put("designType", designType);
                // Build payload
                Map<String, Object> payload = buildShutterMaterialPayload(mapData);
                shutterMaterials.add(payload);
            }
        }
        insideModules.put("shutterMaterials",shutterMaterials);
        modules.add(insideModules);
        shutterPayload.put("modules",modules);
        System.out.println(shutterPayload);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonPayload = objectMapper.writeValueAsString(shutterPayload);
            response = invokePutRequest(baseURl + "/api/v1.0/projects/" + projectID + "/floors/" + floorID + "/rooms/" + currentRoomId + "/unitEntries/" + currentZoneId + "/material/shutterFascia", jsonPayload, token);
            Assert.assertEquals(response.getStatusCode(),200,response.asPrettyString());
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> buildShutterMaterialPayload(Map<String, Object> data) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("shutterObjectId", data.getOrDefault("shutterObjectId", null));
        payload.put("coreCode", data.getOrDefault("coreCode", null));
        payload.put("coreName", data.getOrDefault("coreName", null));
        payload.put("finishCode", data.getOrDefault("finishCode", null));
        payload.put("finishName", data.getOrDefault("finishName", null));
        payload.put("colourCode", data.getOrDefault("colourCode", null));
        payload.put("colourName", data.getOrDefault("colourName", null));
        payload.put("colourImage", data.getOrDefault("colourImage", null));
        payload.put("materialId", data.getOrDefault("materialId", null));

        payload.put("glassAnnotation", null);
        payload.put("glassColorCode", data.getOrDefault("glassColorCode", null));
        payload.put("glassColorName", data.getOrDefault("glassColorName", null));
        payload.put("glassColourImage", data.getOrDefault("glassColourImage", null));
        payload.put("glassMaterialId", data.getOrDefault("glassMaterialId", null));
        payload.put("glassMaterialName", data.getOrDefault("glassMaterialName", null));
        payload.put("mirrorData", null);

        payload.put("designCode", data.getOrDefault("designCode", null));
        payload.put("designName", data.getOrDefault("designName", null));
        payload.put("subDesignId", data.getOrDefault("subDesignId", null));
        payload.put("subDesignName", data.getOrDefault("subDesignName", null));
        payload.put("ignoreSubDesignErrorCheck", false);
        payload.put("designType", data.getOrDefault("designType", null));
        payload.put("finishType", data.getOrDefault("finishType", ""));
        payload.put("finishCategory", data.getOrDefault("finishCategory", null));
        payload.put("finishGroup", data.getOrDefault("finishGroup", null));

        return payload;
    }




    public static String resolveMaterialType(Map<String, String> shutter) {
        boolean isInfill = Boolean.parseBoolean(String.valueOf(shutter.get("isInfill")));
        boolean hasGlass = Boolean.parseBoolean(String.valueOf(shutter.get("hasGlass")));
        boolean isFrame = Boolean.parseBoolean(String.valueOf(shutter.get("isFrame")));
        boolean hasAluminium = Boolean.parseBoolean(String.valueOf(shutter.get("hasAluminium")));

        if (isInfill) {
            return hasGlass ? "glass" : "infill";
        }
        if (isFrame && hasAluminium) {
            return "frame";
        }
        return "wooden";
    }


    public Map<String, Map<String, String>> buildMaterialDetailsForComponents(
            Map<String, List<Map<String, String>>> applicableMaterialForAllComponent,
            String shutterComponent,
            String shutterCoreCode,
            String shutterFinishCode,
            String infillMaterialId,
            String shutterColourCode,
            String infillMaterialColourId
    ) {
        boolean isInfillPassedFromInput = infillMaterialId != null
                && !infillMaterialId.trim().isEmpty()
                && !"null".equalsIgnoreCase(infillMaterialId);

        Map<String, String> componentIdMap = Map.of(
                "accessoryComponent", "shutterObjectId",
                "grouppanelscomponent", "groupPanelId"
        );
        String finalComponentId = componentIdMap.getOrDefault(shutterComponent.toLowerCase(), null);

        Map<String, Map<String, String>> materialDetailsForComponents = new HashMap<>();

        for (Map.Entry<String, List<Map<String, String>>> entry : applicableMaterialForAllComponent.entrySet()) {
            String shutterNameWithObjectId = entry.getKey();
            logger.info("Processing component: {}", shutterNameWithObjectId);
            String shutterName = shutterNameWithObjectId.split("_")[0];
            String componentObjectId = shutterNameWithObjectId.split("_")[1];
            boolean isInfillComponent = shutterName.contains("Infill");
            List<Map<String, String>> materials = entry.getValue();
            logger.info("Is Infill Component: {}", isInfillComponent);
            boolean currentComponent = false;
            String coreName = "", finishName = "", designType = "", finishType = "", finishCategory = "", materialType = "";

            if (!isInfillPassedFromInput && isInfillComponent) {
                logger.info("Skipping component as it's Infill but no infillMaterialId was passed.");
                continue;
            }

            for (Map<String, String> mat : materials) {
                if (!isInfillComponent) {
                    if (shutterCoreCode.equals(mat.get("shutterCoreCode")) &&
                            shutterFinishCode.equals(mat.get("shutterFinishCode"))) {
                        logger.info("Found matching shutter material for component: {}", shutterNameWithObjectId);
                        coreName = mat.getOrDefault("shutterCoreName", "");
                        finishName = mat.getOrDefault("shutterFinishName", "");
                        designType = mat.getOrDefault("shutterDesignType", "");
                        finishType = mat.getOrDefault("shutterFinishType", "");
                        finishCategory = mat.getOrDefault("shutterFinishCategory", "");
                        currentComponent = true;
                    }

                } else {
                    if (infillMaterialId.equalsIgnoreCase(mat.get("type"))) {
                        if (infillMaterialId.equalsIgnoreCase("Mirror")) {
                            materialType = MATERIAL_TYPE_MIRROR;
                            infillMaterialId = mat.getOrDefault("materialId", infillMaterialId);
                        } else {
                            materialType = MATERIAL_TYPE_GLASS;
                        }

                        currentComponent = true;
                        break;
                    }

                }
            }

            if (currentComponent && !isInfillComponent) {
                logger.info("Building shutter details for component: {}", shutterNameWithObjectId);
                Map<String, String> shutterDetails = buildShutterDetails(coreName, finishName, designType,
                        finishType, finishCategory, shutterCoreCode, shutterFinishCode,
                        finalComponentId, componentObjectId, shutterColourCode);
                materialDetailsForComponents.put(componentObjectId, shutterDetails);
            } else {
                if (isInfillPassedFromInput) {
                    logger.info("Building infill details for component: {}", shutterNameWithObjectId);
                    Map<String, String> glassDetails = buildInfillDetails(infillMaterialId, materialType,
                            infillMaterialColourId, finalComponentId, componentObjectId);
                    materialDetailsForComponents.put(componentObjectId, glassDetails);
                }
            }
        }

        return materialDetailsForComponents;
    }

    private Map<String, String> buildShutterDetails(String coreName, String finishName, String designType,
                                                    String finishType, String finishCategory,
                                                    String shutterCoreCode, String shutterFinishCode,
                                                    String finalComponentId, String componentName,
                                                    String shutterColourCode) {
        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put("coreName", coreName);
        detailsMap.put("finishName", finishName);
        detailsMap.put("designType", designType);
        detailsMap.put("finishType", finishType);
        detailsMap.put("finishCategory", finishCategory);
        detailsMap.put("coreCode", shutterCoreCode);
        if (finalComponentId != null) {
            detailsMap.put(finalComponentId, componentName);
        }
        detailsMap.put("finishCode", shutterFinishCode);
        fillShutterColorDetails(detailsMap, MATERIAL_TYPE_SHUTTER, shutterFinishCode, shutterColourCode);
        return detailsMap;
    }

    private Map<String, String> buildInfillDetails(String infillMaterialId, String materialType,
                                                   String infillMaterialColourId,
                                                   String finalComponentId, String componentName) {
        Map<String, String> detailsMapForColour = new HashMap<>();
        fillInfillMaterialDetails(detailsMapForColour, infillMaterialId, materialType, infillMaterialColourId);
        if (finalComponentId != null) {
            detailsMapForColour.put(finalComponentId, componentName);
        }
        detailsMapForColour.put("glassMaterialName", infillMaterialId);
        return detailsMapForColour;
    }


    private void fillInfillMaterialDetails(Map<String, String> detailsMapForColour, String infillMaterialId, String materialType, String infillMaterialColourId) {
        logger.info("start finding infill material colour for infillMaterialColourId: {}", infillMaterialColourId);
        Response glassColorRes = synapseService.getMaterialColor(materialType, infillMaterialId);
        List<Map<String, Object>> glassColorList = glassColorRes.jsonPath().getList("data.getColour");
        for (Map<String, Object> colourData : glassColorList) {
            String colourId = String.valueOf(colourData.get("colourId"));
            if (infillMaterialColourId.equals(colourId)) {
                logger.info("Found matching infill color for colourId: {}", colourId);
                detailsMapForColour.put("glassColorCode", String.valueOf(colourData.get("colourId")));
                detailsMapForColour.put("glassColorName", String.valueOf(colourData.get("colourName")));
                detailsMapForColour.put("glassColorImage", String.valueOf(colourData.get("colourImage")));
                detailsMapForColour.put("glassMaterialId", String.valueOf(colourData.get("assetMaterialId")));
                break;
            }
        }
    }


    public void fillShutterColorDetails(Map<String, String> detailsMap, String materialType, String finishCode, String colourCode) {

        Response colorRes = synapseService.getMaterialColor(materialType, finishCode);
        List<Map<String, Object>> colorList = colorRes.jsonPath().getList("data.getColour");
        if (colorList != null) {
            for (Map<String, Object> color : colorList) {
                if (colourCode.equals(String.valueOf(color.get("colourId")))) {
                    detailsMap.put("colourName", (String) color.get("colourName"));
                    detailsMap.put("colourImage", (String) color.get("colourImage"));
                    detailsMap.put("materialId", (String) color.get("assetMaterialId"));
                    detailsMap.put("finishGroup", (String) color.get("finishGroup"));
                    detailsMap.put("colourCode", colourCode);
                    logger.info("Found matching color details for code: {} and Colour Name :{} ", colourCode, color.get("colourName"));
                    break;
                }
            }
        }
    }


    public Response applyGroupPanel(String projectID, String floorID, String currentRoomId, String currentZoneId, Map<String, Map<String, String>> materialDetailsForGroupPanels, String miqModuleObjectId, Map<String, String> token) {
        String payloadPath = "payloads/material/groupPanel.json";
        String payload = moduleServicePayloadHelper.generateShutterMaterialPayload(materialDetailsForGroupPanels, miqModuleObjectId, payloadPath);

        response = invokePutRequest(baseURl + "/api/v1.0/projects/" + projectID + "/floors/" + floorID + "/rooms/" + currentRoomId + "/unitEntries/" + currentZoneId + "/material/groupPanel", payload, token);
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public Response applyShutterMaterial(String projectID, String floorID, String currentRoomId, String currentZoneId, Map<String, Map<String, String>> materialDetailsForComponents, String miqModuleObjectId, Map<String, String> token) {
        String payloadPath = "payloads/material/shutterFascia.json";
        String payload = moduleServicePayloadHelper.generateShutterMaterialPayload(materialDetailsForComponents, miqModuleObjectId, payloadPath);
        response = invokePutRequest(baseURl + "/api/v1.0/projects/" + projectID + "/floors/" + floorID + "/rooms/" + currentRoomId + "/unitEntries/" + currentZoneId + "/material/shutterFascia", payload, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        return response;
    }


    public Response multiPartShutter(String projectID, String floorID, String currentRoomId, String currentZoneId, String moduleIdFromInput, String categoryId, String subCategoryId,
                                     String miqModuleObjectId, Map<String, Object> shutterDetailsListFromInput, Map<String, String> token) {

        Response res;
        Map<String, Object> shuttersData;
        Map<String, String> shutterIds = new HashMap<>();
        Map<String, Object> designData = new HashMap<>();
        Map<String, Object> panelData = new HashMap<>();
        Map<String, Object> panelDataMap = new HashMap<>();


        String shutterCode = getString(shutterDetailsListFromInput, "shutterCode");
        String frameCode = getString(shutterDetailsListFromInput, "frameCode");
        String frameFinishCode = getString(shutterDetailsListFromInput, "frameFinishCode");
        String shutterCategory = getString(shutterDetailsListFromInput, "ShutterCategory");
        String frameColorCode = getString(shutterDetailsListFromInput, "frameColourCode");
        String side = getString(shutterDetailsListFromInput, "shutterSide");
        designData = (Map<String, Object>) shutterDetailsListFromInput.get("shutterDesignData");
        String panelIds = getString(shutterDetailsListFromInput, "panelIds");
        String panelCoreCode = getString(shutterDetailsListFromInput, "panelCoreCode");
        String panelFinishCode = getString(shutterDetailsListFromInput, "panelFinishCode");
        String panelColorCode = getString(shutterDetailsListFromInput, "panelColourCode");


        if (shutterCode == null) throw new IllegalArgumentException("shutterCode is required");
        shutterCode = shutterCode.replaceAll("[{} ]", "");
        String[] pairs = shutterCode.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                shutterIds.put(kv[0], kv[1]);
            }
        }

        // Get Shutter Details
        shuttersData = aluminiumShutters(projectID, floorID, currentRoomId, currentZoneId, miqModuleObjectId, shutterIds, frameCode, frameColorCode, designData, token);
        if (!orderProfileType.equalsIgnoreCase("DC")) {
            if (frameFinishCode == null || frameColorCode == null) {
                throw new IllegalStateException("frameCode or frameColorCode is null. Cannot proceed with multiPartShutter.");
            }
        }
        if (orderProfileType.equalsIgnoreCase("DC")) {
            Map<String, String> colorDetails = new HashMap<>();
            panelData.put("colourCode", panelColorCode);
            panelData.put("coreCode", panelCoreCode);
            panelData.put("finishCode", panelFinishCode);
            Response panelcolorResponse = synapseService.getMaterialColor("shutter", panelFinishCode);
            List<Map<String, Object>> panelColours = panelcolorResponse.jsonPath().getList("data.getColour");
            for (Map<String, Object> colourData : panelColours) {
                if (panelColorCode.equals(String.valueOf(colourData.get("colourId")))) {
                    panelData.put("colourName", String.valueOf(colourData.get("colourName")));
                    panelData.put("colourImage", String.valueOf(colourData.get("colourImage")));
                    panelData.put("materialId", String.valueOf(colourData.get("assetMaterialId")));
                    break;
                }
            }
            Response wardrobeMaterialDataResponse = synapseService.getWardrobeMaterialData(moduleIdFromInput, categoryId, subCategoryId);
            List<Map<String, String>> multipartShutterMaterials = wardrobeMaterialDataResponse.jsonPath().getList("data.getWardrobeMaterial.multiPartShutterMaterials");
            for (Map<String, String> material : multipartShutterMaterials) {
                if (panelCoreCode != null && panelFinishCode != null
                        && panelCoreCode.equals(material.get("shutterCoreCode"))
                        && panelFinishCode.equals(material.get("shutterFinishCode"))) {
                    panelData.put("coreName", material.get("shutterCoreName"));
                    panelData.put("finishName", material.get("shutterFinishName"));
                    break;
                }
            }
        }
        String shutterObjectIdLeft = null;
        String shutterObjectIdRight = null;
        String frameNameLeft = null;
        String frameNameRight = null;
        Map<String, Object> shutters = (Map<String, Object>) shuttersData.get("shutters");
        // Combine left/right panel logic
        for (String sideKey : new String[]{"LEFT", "RIGHT"}) {
            if (shutters != null && shutters.containsKey(sideKey)) {
                Map<String, Object> shutterMap = (Map<String, Object>) shutters.get(sideKey);
                String shutterObjectId = cleanString(shutterMap.get("shutterId").toString());
                String frameName = cleanString(shutterMap.get("name").toString());
                List<Map<String, Object>> panelsList = (List<Map<String, Object>>) shutterMap.get("panels");
                if (panelsList == null) continue;
                for (Map<String, Object> panel : panelsList) {
                    if (panel.containsKey("panelID")) {
                        List<Map<String, Object>> panelDataList = new ArrayList<>();
                        panelData.put("panelId",panel.get("panelID"));
                        panelDataList.add(panelData);
                        panelDataMap.put(sideKey.toLowerCase() + "Panels", panelDataList);
                        if (sideKey.equalsIgnoreCase("LEFT")) {
                            shutterObjectIdLeft = shutterObjectId;
                            frameNameLeft = frameName;
                        } else {
                            shutterObjectIdRight = shutterObjectId;
                            frameNameRight = frameName;
                        }
                    }
                }
            }
        }
        // Extract additional frame details
        Map<String, String> colorDetails = (Map<String, String>) shuttersData.get("colorDetails");
        String frameColorName = colorDetails.get("frameColorName");
        String frameColourImage = colorDetails.get("frameColourImage");
        String frameMaterialId = colorDetails.get("frameMaterialId");
        // Generate the JSON payload using the helper method
        String jsonPayload = moduleServicePayloadHelper.getMultiPartShutterPayload(
                miqModuleObjectId, shutterCategory, frameFinishCode, frameColorName, frameColorCode,
                frameColourImage, frameMaterialId, shutterObjectIdLeft, frameNameLeft,
                shutterObjectIdRight, frameNameRight, panelDataMap
        );

        System.out.println("rama chandra");
        System.out.println( "multipart shutter Payload : "+ jsonPayload);
        String url = baseURl + "/api/v1.0/projects/" + projectID + "/floors/" + floorID +
                "/rooms/" + currentRoomId + "/unitEntries/" + currentZoneId + "/material/multiPartShutter";
        res = invokePutRequest(url, jsonPayload, token);
        Assert.assertEquals(res.getStatusCode(), 200, res.asPrettyString());
        return res;
    }

    public Map<String, Object> aluminiumShutters(String projectId, String floorId, String roomId,
                                                 String unitEntryId, String moduleObjectId, Map<String, String> shutterIds,
                                                 String frameCode, String frameColorCode, Map<String, Object> designData, Map<String, String> token) {

        String url = baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId +
                "/rooms/" + roomId + "/unitEntries/" + unitEntryId + "/aluminiumshutters";

        Map<String, Object> multiPartShutterData = new HashMap<>();
        Map<String, Object> payloadData = new HashMap<>();

        // Build shutterIdsMap
        Map<String, Object> shutterIdsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : shutterIds.entrySet()) {
            shutterIdsMap.put(entry.getKey(), Integer.parseInt(entry.getValue()));
        }
        Map<String, String> colorDetails = new HashMap<>();
        if (!orderProfileType.equalsIgnoreCase("DC")) {
            // Get color material data
            Response colorResponse = synapseService.getMaterialColor("shutter", frameCode);
            List<Map<String, Object>> colours = colorResponse.jsonPath().getList("data.getColour");

            String frameColorName = "";
            String frameColourImage = "";
            String frameMaterialId = "";

            for (Map<String, Object> colourData : colours) {
                if (frameColorCode.equals(colourData.get("colourId").toString())) {
                    frameColorName = colourData.get("colourName").toString();
                    frameColourImage = colourData.get("colourImage").toString();
                    frameMaterialId = colourData.get("assetMaterialId").toString();
                    break;
                }
            }

            // Store color details for return

            colorDetails.put("frameColorName", frameColorName);
            colorDetails.put("frameColourImage", frameColourImage);
            colorDetails.put("frameMaterialId", frameMaterialId);

            multiPartShutterData.put("frameCode", frameCode);
            multiPartShutterData.put("colorDetails", colorDetails);

            // Build payloadData map
            payloadData.put("frameCode", frameCode);
            payloadData.put("frameColorCode", frameColorCode);
            payloadData.put("frameColorName", frameColorName);
            payloadData.put("frameColourImage", frameColourImage);
            payloadData.put("frameMaterialId", frameMaterialId);
        }
        payloadData.put("moduleObjectId", moduleObjectId);
        payloadData.put("designData", designData != null ? designData : "{}");

        // Use the map-based aluminiumShutterPayload method
        String payload = moduleServicePayloadHelper.aluminiumShutterPayload(payloadData, shutterIdsMap);
        Response res = invokePostRequest(url, payload, token);
        System.out.println(res.asPrettyString());
        Assert.assertEquals(res.getStatusCode(), 200, res.asPrettyString());
        // Parse response and collect shutter info
        Map<String, Map<String, Object>> shutterDataMap = new HashMap<>();
        for (String side : shutterIds.keySet()) {
            String shutterPath = "wardrobeShutters." + side;

            Map<String, Object> shutterDetails = new HashMap<>();
            shutterDetails.put("shutterId", res.jsonPath().getString(shutterPath + ".objectId"));
            if (orderProfileType.equalsIgnoreCase("DC")) {
                shutterDetails.put("name", res.jsonPath().getString(shutterPath + ".frameName"));
                colorDetails.put("frameColorName",  cleanString(res.jsonPath().getString(shutterPath + ".frameColorName")));
                colorDetails.put("frameColourImage",  res.jsonPath().getString(shutterPath + ".frameColourImage"));
                colorDetails.put("frameMaterialId",  cleanString(res.jsonPath().getString(shutterPath + ".frameMaterialId")));

                multiPartShutterData.put("frameCode", res.jsonPath().getString(shutterPath + ".frameCode"));
                multiPartShutterData.put("frameColorCode", res.jsonPath().getString(shutterPath + ".frameColorCode"));
                multiPartShutterData.put("colorDetails", colorDetails);
                multiPartShutterData.put("shutterCategory", res.jsonPath().getString("shutterCategory"));
            } else {
                shutterDetails.put("name", res.jsonPath().getString(shutterPath + ".name"));
            }

            shutterDetails.put("panels", res.jsonPath().getList(shutterPath + ".panels").get(0));
            shutterDataMap.put(side, shutterDetails);
        }

        logger.info("Extracted shutter data: {}", shutterDataMap);
        multiPartShutterData.put("shutters", shutterDataMap);

        return multiPartShutterData;
    }


    public Response replaceInternalComponent(String pID, String fId, String rId, String moduleId, String categoryId, String subCategoryId, String moduleObjectId, String unitEntryId, String internalId, String dimension, String section, Map<String, String> token) {
        String payload = moduleServicePayloadHelper.replaceInternalPayload(moduleId, categoryId, subCategoryId, moduleObjectId, internalId, dimension, section);
        System.out.println(payload);
        response = invokePutRequest(baseURl + "/api/v1.0/project/" + pID + "/floors/" + fId + "/rooms/" + rId + "/unitEntries/" + unitEntryId + "/replaceModules", payload, token);
        Assert.assertEquals(response.statusCode(), 200);
        logger.info("Internal component successfully added.");
        return response;
    }

    public Response addInternalAccessories(String projectId, String floorId, String roomId, String categoryId, String subCategoryId, String width, String depth, String height, String moduleObjectId, String internalId, String wardrobeInternalId, String unitEntryId, String internalCode, Map<String, String> token) {
        Response accessoriesRes = synapseService.getAccDetails(internalId, categoryId, subCategoryId, width, depth, height);
        List<Map<String, Object>> accessoriesData = accessoriesRes.jsonPath().getList("data");


        boolean flag = false;
        String internalName = "";
        for (Map<String, Object> item : accessoriesData) {
            String code = (String) item.get("code");
            if (code.equals(internalCode)) {
                flag = true;
                internalName = (String) item.get("name");
                break; // No need to continue if found
            }
        }

        if (flag) {
            String payload = "{\n" +
                    "    \"code\": \"" + internalCode + "\",\n" +
                    "    \"name\": \"" + internalName + "\",\n" +
                    "    \"distanceFromTop\": 241,\n" +
                    "    \"xoffset\": 18.5,\n" +
                    "    \"yoffset\": 1823,\n" +
                    "    \"zoffset\": 28\n" +
                    "}";

            response = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floors/" + floorId + "/rooms/" + roomId + "/unitEntry/" + unitEntryId + "/module/" + moduleObjectId + "/wardrobeInternal/" + wardrobeInternalId + "/wardrobeInternalAccessory", payload, token);

            Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
            logger.info("Internal accessories successfully added.");
            return response;
        } else {
            logger.warn("Internal accessory code not applicable: {}", internalCode);
            return null;
        }
    }


    public String getWardrobeInternalId(Response response, String internalId) {
        String wardrobeInternalId = "";
        List<Map<String, Object>> miqModules = response.jsonPath().getList("miqModules");
        for (Map<String, Object> miqModule : miqModules) {
            List<Map<String, String>> wardrobeInternals = (List<Map<String, String>>) miqModule.get("wardrobeInternals");
            for (Map<String, String> wardrobeInternal : wardrobeInternals) {
                String internals = String.valueOf(wardrobeInternal.get("internalId"));
                if (internals.equals(internalId)) {
                    wardrobeInternalId = wardrobeInternal.get("objectId");
                }
            }
        }
        return wardrobeInternalId;
    }


    public Response addAccessories(String projectId, String floorId, String roomId, String unitEntryId,
                                   String roomType, String categoryId, String subCategoryId,
                                   String moduleObjectId, List<String> shutterObjectIds,
                                   String moduleId, String dimension, String code,
                                   Map<String, String> token) {

        String url = baseURl + "/api/v1.0/projects/" + projectId + "/floors/" + floorId +
                "/rooms/" + roomId + "/unitEntries/" + unitEntryId + "/accessories";

        Map<String, String> accessoryData = getAccessories(roomType, unitEntryId, categoryId, subCategoryId, moduleId, dimension, code);

        // Get the payload from the helper class
        String requestBody = moduleServicePayloadHelper.getAddAccessoriesPayload(moduleObjectId, shutterObjectIds, accessoryData);
        Response response = invokePutRequest(url, requestBody, token);
        Assert.assertEquals(response.getStatusCode(), 200);

        return response;
    }

    public Map<String, String> getAccessories(String roomType, String zoneId, String categoryId, String subCategoryId, String moduleId, String dimension, String accesoryCode) {
        Response response = synapseService.getZoneData(roomType, zoneId, categoryId, subCategoryId, moduleId, dimension);
        List<Map<String, Object>> cabinetAccessories = response.jsonPath().getList("data.getZoneData.graphqlModules[0].cabinetAccessories");
        List<Map<String, Object>> shutterAccessories = response.jsonPath().getList("data.getZoneData.graphqlModules[0].shuttersWithAccessories");
        Map<String, String> accessoriesData = new HashMap<>();

        boolean checkFlag = false;
        if (cabinetAccessories != null) {
            for (Map<String, Object> cabinetAccessory : cabinetAccessories) {
                if (cabinetAccessory.get("code").toString().equals(accesoryCode)) {
                    checkFlag = true;
                    accessoriesData.put("cabinateCode", cabinetAccessory.get("code").toString());
                    accessoriesData.put("cabinateIsEnabled", cabinetAccessory.get("isEnabled").toString());
                    accessoriesData.put("cabinateName", cabinetAccessory.get("name").toString());
                    accessoriesData.put("cabinateImgUrl", cabinetAccessory.get("imageUrl").toString());
                    accessoriesData.put("cabinateHtcMotion", cabinetAccessory.get("htcMotion").toString());
                    accessoriesData.put("cabinateHtcMake", cabinetAccessory.get("htcMake").toString());
                }
            }
        }

        if (!checkFlag) {
            if (shutterAccessories != null) {
                for (Map<String, Object> shutterAccessory : shutterAccessories) {
                    List<Map<String, Object>> accessories = (List<Map<String, Object>>) shutterAccessory.get("accessories");
                    for (Map<String, Object> accessory : accessories) {
                        if (accessory.get("code").toString().equals(accesoryCode)) {
                            accessoriesData.put("shutterCode", accessory.get("code").toString());
                            accessoriesData.put("shutterIsEnabled", accessory.get("isEnabled").toString());
                            accessoriesData.put("shutterName", accessory.get("name").toString());
                            accessoriesData.put("shutterImgUrl", accessory.get("imageUrl").toString());
                            accessoriesData.put("shutterHtcMotion", accessory.get("htcMotion").toString());
                            accessoriesData.put("shutterHtcMake", accessory.get("htcMake").toString());
                        }
                    }

                }
            }

        }
        return accessoriesData;
    }


    public Response addInternalComponent(String projectId, String floorId, String roomId,
                                         String categoryId, String subCategoryId,
                                         String unitEntryId, String moduleObjectId,
                                         String moduleId, String internalId,
                                         String dimension, String section,
                                         Map<String, String> token) {

        Response internalDetails = synapseService.getInternalsQuery(moduleId, dimension, section, categoryId, subCategoryId);
        String url = baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId +
                "/room/" + roomId + "/unitEntry/" + unitEntryId + "/addInternalComponent";

        List<Integer> internalIds = internalDetails.jsonPath().getList("data.getInternals.internalId");
        String[] dimensionParts = dimension.split(" X ");
        String height = dimensionParts[dimensionParts.length - 1];

        boolean flag = internalIds.contains(Integer.parseInt(internalId));

        if (flag) {
            String jsonPayload = moduleServicePayloadHelper.getAddInternalComponentPayload(moduleObjectId, dimension, internalId, section, height);

            Response response = invokePostRequest(url, jsonPayload, token);
            Assert.assertEquals(response.getStatusCode(), 200);
            return response;
        }

        return null;
    }

    public void addHandlesToModule(String projectID, String floorID, String currentRoomId, String currentZoneId, String categoryId, String subCategoryId, String handleId, Map<String, String> token) {
        if (isHandle.equalsIgnoreCase("TRUE") && handleId != null) {
            Response handleResponse = synapseService.getHandles(categoryId, subCategoryId);
            List<Map<String, Object>> handlesList = handleResponse.jsonPath().get("data.getHandles");
            Map<String, Object> matchingHandle = getHandleById(handleId, handlesList);
            if (matchingHandle != null) {
                Response unitEntryResponse = new ScBackendService().getUnitEntryData(projectID, floorID, currentRoomId, currentZoneId, token);
                List<Map<String, Object>> unitEntryPayLoad = unitEntryResponse.jsonPath().getList("miqModules");
                for (Map<String, Object> unitEntry : unitEntryPayLoad) {
                    List<Map<String, Object>> shuttersWithAccessory = (List<Map<String, Object>>) unitEntry.get("shuttersWithAccessory");
                    for (Map<String, Object> shutter : shuttersWithAccessory) {
                        Map<String, Object> handle = (Map<String, Object>) shutter.get("handle");

                        if (handle == null) {
                            handle = new HashMap<>();
                        }
                        handle.put("id", matchingHandle.get("spacecraftHandleId"));
                        handle.put("name", matchingHandle.get("name"));
                        handle.put("image", matchingHandle.get("imageUrl"));
                        handle.put("handleType", matchingHandle.get("handleType"));
                        handle.put("finish", matchingHandle.get("finish"));
                        handle.put("pitch", matchingHandle.get("pitch"));
                        handle.put("shutterFamilyIds", matchingHandle.get("shutterFamilyIds"));
                        shutter.put("handle", handle);
                    }
                }

                response = updateModules(projectID, floorID, currentRoomId, currentZoneId, unitEntryPayLoad, token);
                if (response.getStatusCode() == 200) {
                    logger.info("Handle added successfully to the module.");
                } else {
                    logger.error("Failed to add handle to the module. Status code: {}", response.getStatusCode());
                }
            } else {
                logger.error("No matching shutter found for position ID: {}", handleId);
            }
        }
    }

    public Map<String, Object> getShutterIdAndPositionByPositionId(String positionId, List<Map<String, Object>> shutterDataList) {
        for (Map<String, Object> shutterData : shutterDataList) {
            Object shutterId = shutterData.get("shutterId");
            List<Map<String, Object>> handlePositions = (List<Map<String, Object>>) shutterData.get("handlePositions");

            if (handlePositions != null) {
                for (Map<String, Object> position : handlePositions) {
                    Object posIdObj = position.get("positionId");
                    if (posIdObj != null && positionId.equalsIgnoreCase(String.valueOf(posIdObj))) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("shutterId", shutterId);
                        result.put("position", position);
                        return result;
                    }
                }
            }
        }
        return null; // or throw exception if preferred
    }

    public Map<String, Object> getHandleById(String spacecraftHandleId, List<Map<String, Object>> handlesList) {
        for (Map<String, Object> handle : handlesList) {
            Object idObj = handle.get("spacecraftHandleId");
            if (idObj != null && spacecraftHandleId.equalsIgnoreCase(String.valueOf(idObj))) {
                return handle;
            }
        }
        return null; // Return null or throw an exception if not found
    }


    public void addAccessoriesToKitchenModule(String projectId, String floorId, String roomId, String zoneId, String categoryId, String subCategoryId, String moduleObjectId, List<String> shutterObjectId, String moduleId,
                                              String dimension, String accessoryCode, String roomType, String isAccessories, Map<String, String> token) {
        if (roomType.equalsIgnoreCase("Kitchen") && isAccessories.equalsIgnoreCase("TRUE") && accessoryCode != null) {
            Response response = addAccessories(projectId, floorId, roomId, zoneId, "Kitchen", categoryId, subCategoryId,
                    moduleObjectId, shutterObjectId, moduleId, dimension, accessoryCode, token);
            if (response.getStatusCode() == 200) {
                logger.info("Accessories added Successfully");
            } else {
                logger.error("Failed to Add Accessories to the Module {}", response.getStatusCode());
            }
        }
    }


    public void processWardrobeComponents(String projectId, String floorId, String roomId, String zoneId, String moduleId, String categoryId, String subCategoryId, String moduleObjectId, String internalId,
                                          String internalWidth, String internalDepth, String internalHeight, String accessoryCode, String roomType, String isInternals, Map<String, String> token) {
        if (isInternals.equalsIgnoreCase("TRUE")) {
            Response internalResponse;

            if (internalId != null) {
                logger.info("Handling wardrobe specific operations for module ID: {}", moduleId);
                Map<String, String> internalDetails = synapseService.getInternalDetails(internalId);
                String section = internalDetails.get("applicableSections");
                String sectionDimension = internalDetails.get("sectionDimension");

                if (subCategoryId.equals("10101") || subCategoryId.equals("10038") || subCategoryId.equals("10100") || subCategoryId.equals("10077")) {
                    internalResponse = addInternalComponent(projectId, floorId, roomId, categoryId, subCategoryId, zoneId,
                            moduleObjectId, moduleId, internalId, sectionDimension, section, token);
                } else {
                    internalResponse = replaceInternalComponent(projectId, floorId, roomId, moduleId, categoryId, categoryId,
                            moduleObjectId, zoneId, internalId, section, sectionDimension, token);
                }

                if (internalResponse.getStatusCode() == 200) {
                    logger.info("Internal component added successfully.");
                    String wardrobeInternalId = getWardrobeInternalId(internalResponse, internalId);

                    if (accessoryCode != null) {
                        Response internAccessResponse = addInternalAccessories(projectId, floorId, roomId, categoryId, subCategoryId,
                                internalWidth, internalDepth, internalHeight, moduleObjectId, internalId, wardrobeInternalId, zoneId, accessoryCode, token);
                        if (internAccessResponse.getStatusCode() == 200) {
                            logger.info("Internal accessories added successfully.");
                        } else {
                            logger.error("Failed to add internal accessories. Status code: {}", internAccessResponse.getStatusCode());
                        }
                    }
                } else {
                    logger.error("Failed to add internal component. Status code: {}", internalResponse.getStatusCode());
                }
            }
        }
    }

    public Response updateCounterTopSkirting(String projectId, String floorId, String roomId, String categoryId, String subCategoryId, String zoneId, String moduleId, String skirtingType, String skirtingHeight, String skirtingWidth, String skirtingModuleId, Map<String, String> token) {
        Response updateSkirtingResponse = null;
        Response checkResponse = synapseService.checkWoodenSkirtingApplicable(categoryId, subCategoryId, zoneId, skirtingWidth, skirtingModuleId, moduleId);
        if (checkResponse.asPrettyString().equalsIgnoreCase("true")) {
            String payload = moduleServicePayloadHelper.counterTopSkirtingPayload(skirtingType, skirtingHeight, skirtingWidth, skirtingModuleId);
            updateSkirtingResponse = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/floor/" + floorId + "/room/" + roomId + "/unitentry/" + zoneId + "/countertopskirting", payload, token);
            Assert.assertEquals(updateSkirtingResponse.getStatusCode(), 200, updateSkirtingResponse.asPrettyString());
            System.out.println("skirting Updated");
        }
        return updateSkirtingResponse;
    }

    private void logShutterResponse(Response response, String componentId) {
        if (response != null && response.getStatusCode() == 200) {
            logger.info("Shutter applied successfully for Module: {}", componentId);
        } else if (response != null) {
            logger.error("Failed to apply shutter for module {}. Status code: {}", componentId, response.getStatusCode());
        } else {
            logger.error("Shutter response is null for module: {}", componentId);
        }
    }

}
