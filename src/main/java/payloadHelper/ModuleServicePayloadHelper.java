package payloadHelper;

import io.restassured.response.Response;
import services.SynapseService;
import utils.UpdateJsons;
import utils.Utilities;

import java.util.*;

import static configurator.BaseClass.orderProfileType;

public class ModuleServicePayloadHelper {
    public static UpdateJsons updateJsons = new UpdateJsons();
    SynapseService synapseService = new SynapseService();

    public String carcassMaterialPayload(Map<String, String> payloadData) {
        Map<String, Object> rootMap = new HashMap<>();
        Map<String, Object> moduleMap = new HashMap<>();
        Map<String, Object> carcassMaterialMap = new HashMap<>();
        Map<String, Object> carcassMirrorDataMap = new HashMap<>();
        Map<String, Integer> positionMap = new HashMap<>();
        Map<String, Integer> rotationMap = new HashMap<>();

        positionMap.put("x", 0);
        positionMap.put("y", 0);
        positionMap.put("z", 0);

        rotationMap.put("x", 0);
        rotationMap.put("y", 0);
        rotationMap.put("z", 0);

        carcassMirrorDataMap.put("meshId", null);
        carcassMirrorDataMap.put("position", positionMap);
        carcassMirrorDataMap.put("rotation", rotationMap);

        carcassMaterialMap.put("carcassName", payloadData.get("carcassName"));
        carcassMaterialMap.put("carcassCode", payloadData.get("carcassCode"));
        carcassMaterialMap.put("carcassColourCode", payloadData.get("carcassColourCode"));
        carcassMaterialMap.put("carcassColourImage", payloadData.get("carcassColourImage"));
        carcassMaterialMap.put("carcassColourName", payloadData.get("carcassColourName"));
        carcassMaterialMap.put("carcassMaterialId", payloadData.get("carcassMaterialId"));
        carcassMaterialMap.put("carcassColourFinishGroup", payloadData.get("finishGroup"));
        carcassMaterialMap.put("carcassMirrorData", carcassMirrorDataMap);

        moduleMap.put("moduleObjectId", payloadData.get("moduleObjectId"));
        moduleMap.put("carcassMaterial", carcassMaterialMap);

        // Root JSON Structure
        rootMap.put("modules", new Object[]{moduleMap});
        return updateJsons.updatePayloadFromMap("payloads/material/carcass.json", rootMap);

    }

    public String dimensionPayload(String width, String length, String height) {
        Map<String, Object> rootMap = new HashMap<>();
        Map<String, Object> dimensionMap = new HashMap<>();

        dimensionMap.put("width", width);
        dimensionMap.put("length", length);
        dimensionMap.put("height", height);

        rootMap.put("dimension", dimensionMap);

        return updateJsons.updatePayloadFromMap("payloads/material/dimension.json", rootMap);
    }

    public String addModulePayload(String width, String depth, String height) {
        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("width", width);
        moduleData.put("depth", depth);
        moduleData.put("height", height);

        return updateJsons.updatePayloadFromMap("payloads/addModule.json", moduleData);
    }


    public String counterTopSkirtingPayload(String skirtingType, String skirtingHeight, String skirtingWidth, String skirtingModuleId) {
        Map<String, Object> rootMap = new HashMap<>();

        // leg height
        Map<String, String> legMap = new HashMap<>();
        legMap.put("height", skirtingHeight);

        // skirtingWidths list
        Map<String, Object> skirtingWidthEntry = new HashMap<>();
        skirtingWidthEntry.put("moduleId", skirtingModuleId);
        skirtingWidthEntry.put("dimension", skirtingHeight + " X 18 X " + skirtingWidth);

        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("x", 132);
        positionMap.put("y", 64);
        positionMap.put("z", 132.26000000000022);

        Map<String, Object> orientationMap = new HashMap<>();
        orientationMap.put("x", 0);
        orientationMap.put("y", 0);
        orientationMap.put("z", 1.5707963267948966);

        skirtingWidthEntry.put("position", positionMap);
        skirtingWidthEntry.put("orientation", orientationMap);

        rootMap.put("skirtingType", skirtingType);
        rootMap.put("leg", legMap);
        rootMap.put("skirtingWidths", List.of(skirtingWidthEntry));
        rootMap.put("countertopDimensions", new ArrayList<>());

        return updateJsons.updatePayloadFromMap("payloads/material/counterTopSkirting.json", rootMap);
    }


    public String generateShutterMaterialPayload(
            Map<String, Map<String, String>> payloadData,
            String miqModuleObjectId,
            String jsonFilePath
    ) {
        List<Map<String, Object>> materialsData = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : payloadData.entrySet()) {
            String componentId = entry.getKey();
            Map<String, String> dataMap = entry.getValue();
            Map<String, String> subDesignDetails = getShutterFamilyDetails(dataMap.get("finishCode"));

            Map<String, Object> shutterMaterial = new HashMap<>(dataMap);
            shutterMaterial.put("ignoreSubDesignErrorCheck", false);
            shutterMaterial.put("shutterObjectId", componentId);
            if("HL".equalsIgnoreCase(orderProfileType)) {
                shutterMaterial.put("designCode", subDesignDetails.getOrDefault("designID", null));
                shutterMaterial.put("subDesignId", subDesignDetails.getOrDefault("subDesignId", null));
                shutterMaterial.put("designName", subDesignDetails.getOrDefault("designName", null));
                shutterMaterial.put("subDesignName", subDesignDetails.getOrDefault("subDesignName", null));
            }

            if ("DC".equalsIgnoreCase(orderProfileType)) {
                shutterMaterial.put("designType", dataMap.getOrDefault("designType", null));
                shutterMaterial.put("finishCategory", dataMap.getOrDefault("finishCategory", null));
                shutterMaterial.put("finishCode", dataMap.getOrDefault("finishCode", null));
                shutterMaterial.put("finishGroup", dataMap.getOrDefault("finishGroup", null));
                shutterMaterial.put("finishName", dataMap.getOrDefault("finishName", null));
                shutterMaterial.put("finishType", dataMap.getOrDefault("finishType", null));

            }

            materialsData.add(shutterMaterial);
        }

        Map<String, Object> module = new HashMap<>();
        module.put("moduleObjectId", miqModuleObjectId);

        if (jsonFilePath.contains("shutterFascia")) {
            module.put("shutterCategory", "WOODEN");
            module.put("updateGroupPanels", false);
            module.put("shutterMaterials", materialsData);
        } else if (jsonFilePath.contains("groupPanel")) {
            module.put("groupPanels", materialsData);
        }

        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("modules", Collections.singletonList(module));

        return updateJsons.updatePayloadFromMap(jsonFilePath, finalPayload);
    }


    public Map<String, String> getShutterFamilyDetails(String familyVariantId) {
        Response response = synapseService.getShutterFamily();
        List<Map<String, Object>> shutterFamilyDetails = response.jsonPath().get();

        for (Map<String, Object> shutterFamily : shutterFamilyDetails) {
            String finishCode = shutterFamily.get("familyVariantId").toString();
            if (finishCode.equals(familyVariantId)) {
                Map<String, String> result = new HashMap<>();
                List<Map<String, Object>> subDesignData = getSubDesignList(shutterFamily);
                if (subDesignData != null && !subDesignData.isEmpty()) {
                    result.put("designName", (String) subDesignData.get(0).get("designName"));
                    result.put("designID", String.valueOf(subDesignData.get(0).get("designID")));
                    Map<String, Object> designEntry = subDesignData.get(0);
                    Map<String, Object> onlySubDesign = ((List<Map<String, Object>>) designEntry.get("subDesigns")).get(0);

                    result.put("subDesignId", String.valueOf(onlySubDesign.get("designID")));
                    result.put("subDesignName", String.valueOf(onlySubDesign.get("designName")));

                }
                return result;
            }
        }

        return new HashMap<>();
    }

    public List<Map<String, Object>> getSubDesignList(Map<String, Object> shutterFamily) {
        List<Map<String, Object>> designs = new ArrayList<>();

        if (shutterFamily.containsKey("designs") && shutterFamily.get("designs") != null) {
            designs = (List<Map<String, Object>>) shutterFamily.get("designs");

        } else if (shutterFamily.containsKey("subDesign") && shutterFamily.get("subDesign") != null) {
            Map<String, Object> singleDesign = (Map<String, Object>) shutterFamily.get("subDesign");
            designs = List.of(singleDesign);
        }
        return designs;
    }


    public String replaceInternalPayload(String moduleId, String categoryId, String subCategoryId, String moduleObjectId, String internalId, String dimension, String section) {
        Map<String, Object> rootMap = new HashMap<>();

        rootMap.put("moduleObjectId", moduleObjectId);
        rootMap.put("newModuleId", moduleId);
        rootMap.put("categoryId", categoryId);
        rootMap.put("subCategoryId", subCategoryId);
        rootMap.put("isGola", true);
        rootMap.put("profileType", null);

        Map<String, Object> internalEntry = new HashMap<>();
        internalEntry.put("internalId", internalId);
        internalEntry.put("section", dimension);
        internalEntry.put("dimension", section);

        rootMap.put("internals", List.of(internalEntry));

        return updateJsons.updatePayloadFromMap("payloads/material/replaceModule.json", rootMap);
    }


    public String aluminiumShutterPayload(Map<String, Object> payloadData, Map<String, Object> shutterIdsMap) {
        Map<String, Object> frameDataMap = new HashMap<>();
        Map<String, Object> moduleMap = new HashMap<>();

        // Frame data
        if (payloadData.get("frameCode") != null || payloadData.get("frameColorCode") != null) {
            frameDataMap.put("frameCode", payloadData.get("frameCode"));
            frameDataMap.put("frameColorCode", payloadData.get("frameColorCode"));
            frameDataMap.put("frameColorName", payloadData.get("frameColorName"));
            frameDataMap.put("frameColourImage", payloadData.get("frameColourImage"));
            frameDataMap.put("frameMaterialId", payloadData.get("frameMaterialId"));
            frameDataMap.put("frameName", "Aluminium Profile With Lipping With Handle AlPro");
        }

        // Module map
        moduleMap.put("moduleId", payloadData.get("moduleObjectId"));
        moduleMap.put("isSectionEnabled", new HashMap<>());
        moduleMap.put("shutterIds", shutterIdsMap);
        moduleMap.put("frameData", frameDataMap);
        moduleMap.put("selectedShutterDesign", "");
        moduleMap.put("designData", payloadData.get("designData"));

        List<Object> modules = new ArrayList<>();
        modules.add(moduleMap);
        return updateJsons.updatePayloadFromMap("payloads/material/aluminiumshutters.json", moduleMap);
    }

    public String groupPanelPayload(Map<String, Object> payloadData, List<Map<String, String>> groupPanelsData) {
        Map<String, Object> groupPanelMap = new HashMap<>();
        Map<String, Object> moduleMap = new HashMap<>();
        String targetComponent = (payloadData.get("component") != null) ? payloadData.get("component").toString() : null;

        String matchedGroupPanelId = null;
        for (Map<String, String> groupPanel : groupPanelsData) {
            if (Utilities.equalsIgnoreCaseAndSpace(targetComponent, groupPanel.get("name"))) {
                matchedGroupPanelId = groupPanel.get("groupPanelId");
            }
        }

        if (matchedGroupPanelId == null) {
            throw new RuntimeException("No matching group panel found for component: " + targetComponent);
        }

        groupPanelMap.put("groupPanelId", matchedGroupPanelId);
        groupPanelMap.put("coreCode", payloadData.get("shutterCoreCode"));
        groupPanelMap.put("coreName", payloadData.get("shutterCoreName"));
        groupPanelMap.put("finishCode", payloadData.get("shutterFinishCode"));
        groupPanelMap.put("finishName", payloadData.get("shutterFinishName"));
        groupPanelMap.put("colourCode", payloadData.get("shutterColourCode"));
        groupPanelMap.put("colourName", payloadData.get("shutterColourName"));
        groupPanelMap.put("colourImage", payloadData.get("shutterColourImage"));
        groupPanelMap.put("materialId", payloadData.get("shutterMaterialId"));
        groupPanelMap.put("isGlass", false);
        groupPanelMap.put("glassMaterial", new HashMap<>());

        List<Object> groupPanels = new ArrayList<>();
        groupPanels.add(groupPanelMap);

        // Module details
        moduleMap.put("moduleObjectId", payloadData.get("moduleObjectId"));
        moduleMap.put("groupPanels", groupPanels);

        List<Object> modules = new ArrayList<>();
        modules.add(moduleMap);

        Map<String, Object> rootPayload = new HashMap<>();
        rootPayload.put("modules", modules);

        return updateJsons.updatePayloadFromMap("payloads/material/groupPanel.json", rootPayload);
    }


    public String getMultiPartShutterPayload(String moduleObjectId, String shutterCategory,
                                             String frameCode, String frameColorName, String frameColorCode,
                                             String frameColourImage, String frameMaterialId,
                                             String shutterObjectIdLeft, String frameNameLeft,
                                             String shutterObjectIdRight, String frameNameRight, Map<String, Object> panelDataMap) {

        Map<String, Object> rootMap = new HashMap<>();
        List<Map<String, Object>> modulesList = new ArrayList<>();
        Map<String, Object> moduleMap = new HashMap<>();

        moduleMap.put("moduleObjectId", moduleObjectId);
        moduleMap.put("shutterCategory", shutterCategory);

        Map<String, Object> multipartShuttersMap = new LinkedHashMap<>();

        if (shutterObjectIdLeft != null) {
            Map<String, Object> leftMap = new HashMap<>();
            leftMap.put("shutterObjectId", shutterObjectIdLeft);
            leftMap.put("frameName", frameNameLeft);
            leftMap.put("frameCode", frameCode);
            leftMap.put("frameColorName", frameColorName);
            leftMap.put("frameColorCode", frameColorCode);
            leftMap.put("frameColourImage", frameColourImage);
            leftMap.put("frameMaterialId", frameMaterialId);
            leftMap.put("panels", panelDataMap.get("leftPanels") != null ? panelDataMap.get("leftPanels") : new ArrayList<>());
            multipartShuttersMap.put("LEFT", leftMap);
        }

        if (shutterObjectIdRight != null) {
            Map<String, Object> rightMap = new HashMap<>();
            rightMap.put("shutterObjectId", shutterObjectIdRight);
            rightMap.put("frameName", frameNameRight);
            rightMap.put("frameCode", frameCode);
            rightMap.put("frameColorName", frameColorName);
            rightMap.put("frameColorCode", frameColorCode);
            rightMap.put("frameColourImage", frameColourImage);
            rightMap.put("frameMaterialId", frameMaterialId);
            rightMap.put("panels", panelDataMap.get("rightPanels") != null ? panelDataMap.get("rightPanels") : new ArrayList<>());
            multipartShuttersMap.put("RIGHT", rightMap);
        }

        if (!multipartShuttersMap.isEmpty()) {
            moduleMap.put("multipartShutters", multipartShuttersMap);
        }

        modulesList.add(moduleMap);
        rootMap.put("modules", modulesList);

        return updateJsons.updatePayloadFromMap("payloads/material/multiPartShutter.json", rootMap);
    }

    public String getAddAccessoriesPayload(String moduleObjectId, List<String> shutterObjectIds, Map<String, String> accessoryData) {
        Map<String, Object> payloadMap = new LinkedHashMap<>();

        Map<String, Object> moduleMap = new LinkedHashMap<>();
        moduleMap.put("moduleObjectId", moduleObjectId);

        // Add shutter accessories
        List<Map<String, Object>> shutterAccessories = new ArrayList<>();
        if (shutterObjectIds != null && !shutterObjectIds.isEmpty()) {
            for (int i = 0; i < shutterObjectIds.size(); i++) {
                Map<String, Object> shutterAccessory = new LinkedHashMap<>();
                shutterAccessory.put("shutterObjectId", shutterObjectIds.get(i));

                if (i == 0 && accessoryData.containsKey("shutterCode")) {
                    Map<String, Object> accessoryDetails = new LinkedHashMap<>();
                    accessoryDetails.put("code", accessoryData.get("shutterCode"));
                    accessoryDetails.put("name", accessoryData.get("shutterName"));
                    accessoryDetails.put("quantity", 1);
                    accessoryDetails.put("enabled", Boolean.parseBoolean(accessoryData.getOrDefault("shutterIsEnabled", "false")));
                    accessoryDetails.put("image", accessoryData.get("shutterImgUrl"));
                    accessoryDetails.put("htcMotion", accessoryData.get("shutterHtcMotion"));
                    accessoryDetails.put("htcMake", accessoryData.get("shutterHtcMake"));
                    accessoryDetails.put("softClose", Boolean.parseBoolean(accessoryData.getOrDefault("shutterIsEnabled", "false")));
                    shutterAccessory.put("accessory", accessoryDetails);
                } else {
                    shutterAccessory.put("accessory", new LinkedHashMap<>());
                }

                shutterAccessory.put("addOnAccessories", new ArrayList<>());
                shutterAccessory.put("cutleryAccessories", new ArrayList<>());
                shutterAccessories.add(shutterAccessory);
            }
        }
        moduleMap.put("shutterAccessories", shutterAccessories);

        // Add cabinet accessories if present
        List<Map<String, Object>> cabinetAccessories = new ArrayList<>();
        if (accessoryData.get("cabinateCode") != null) {
            Map<String, Object> cabinetAccessory = new LinkedHashMap<>();
            Map<String, Object> accessoryDetails = new LinkedHashMap<>();
            accessoryDetails.put("code", Integer.parseInt(accessoryData.get("cabinateCode")));
            accessoryDetails.put("name", accessoryData.get("cabinateName"));
            accessoryDetails.put("quantity", 1);
            accessoryDetails.put("enabled", Boolean.parseBoolean(accessoryData.getOrDefault("cabinateIsEnabled", "false")));
            accessoryDetails.put("image", accessoryData.get("cabinateImgUrl"));
            accessoryDetails.put("htcMotion", accessoryData.get("cabinateHtcMotion"));
            accessoryDetails.put("htcMake", accessoryData.get("cabinateHtcMake"));
            accessoryDetails.put("softClose", Boolean.parseBoolean(accessoryData.getOrDefault("cabinateIsEnabled", "false")));

            cabinetAccessory.put("accessory", accessoryDetails);
            cabinetAccessory.put("addOnAccessories", new ArrayList<>());
            cabinetAccessory.put("cutleryAccessories", new ArrayList<>());
            cabinetAccessories.add(cabinetAccessory);
        }
        moduleMap.put("cabinetAccessories", cabinetAccessories);

        // Final structure
        List<Map<String, Object>> modules = new ArrayList<>();
        modules.add(moduleMap);
        payloadMap.put("modules", modules);

        return updateJsons.updatePayloadFromMap("payloads/accessories/addAccessories.json", payloadMap);
    }

    public String getAddInternalComponentPayload(String moduleObjectId, String dimension,
                                                 String internalId, String section, String height) {

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("moduleObjectId", moduleObjectId);

        Map<String, Object> internalMap = new LinkedHashMap<>();
        internalMap.put("dimension", dimension);
        internalMap.put("internalId", internalId);
        internalMap.put("section", section);
        internalMap.put("height", height);
        internalMap.put("internalProperty", "fixed");

        Map<String, Object> positionMap = new HashMap<>();
        positionMap.put("x", 0);
        positionMap.put("y", 334);
        positionMap.put("z", 0);

        Map<String, Object> rotationMap = new HashMap<>();
        rotationMap.put("x", 0);
        rotationMap.put("y", 0);
        rotationMap.put("z", 0);

        internalMap.put("position", positionMap);
        internalMap.put("rotation", rotationMap);

        List<Map<String, Object>> internalsList = new ArrayList<>();
        internalsList.add(internalMap);

        rootMap.put("internals", internalsList);

        return updateJsons.updatePayloadFromMap("payloads/internal/addInternalComponent.json", rootMap);
    }

    /**
     * Builds the updateShutterType payload as per updateShutterType.json structure.
     */
    public String updateShutterTypePayload(String moduleObjectId, String finishCategory, String shutterDesignsName, String shutterSubTypeId) {
        Map<String, Object> item = new HashMap<>();
        item.put("moduleObjectId", moduleObjectId);
        item.put("finishCategory", finishCategory);
        item.put("shutterSubTypeId", shutterSubTypeId);
        item.put("designType", shutterDesignsName);
        return updateJsons.updatePayloadFromMap("payloads/material/updateShutterType.json", item);
    }

}
