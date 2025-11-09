package payloadHelper;

import utils.UpdateJsons;

import static utils.PropertiesReader.*;

import java.util.*;

public class SynapsePayloadHelper {
    UpdateJsons updateJsons = new UpdateJsons();
    Map<String, Object> updateData = new HashMap<>();
    Map<String, Object> variables = new HashMap<>();
    String filePath;

    public String getMaterialColorPayload(String materialType, String materialId) {
        variables.put("materialType", materialType);
        variables.put("cityCode", cityCode);
        variables.put("materialId", materialId);
        variables.put("finishId", "");
        variables.put("orderProfileType", orderProfileType);
        variables.put("priceVersion", priceVersion);
        variables.put("paymentStage", "0");
        variables.put("projectCreationDate", projectCreationDate);
        updateData.put("variables", variables);
        if (orderProfileType.equalsIgnoreCase("DC")) {
            filePath = "payloads/graphql/colourData.json";
        } else
            filePath = "payloads/graphql/hl/colourData.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getInternalsQueryPayload(String moduleId, String dimension, String applicableSection, String categoryId, String subcategoryId) {
        variables.put("moduleId", moduleId);
        variables.put("applicableSection", applicableSection);
        variables.put("dimension", dimension);
        variables.put("categoryId", categoryId);
        variables.put("subcategoryId", subcategoryId);
        variables.put("priceVersion", priceVersion);
        updateData.put("variables", variables);
        filePath = "payloads/graphql/internalsQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getAllShuttersPayload(String categoryId, String subCategoryId, String moduleId) {
        variables.put("cityCode", cityCode);
        variables.put("categoryId", categoryId);
        variables.put("subcategoryId", subCategoryId);
        variables.put("moduleId", moduleId);
        variables.put("finishId", "");
        variables.put("xpDoowupInHL", true);
        variables.put("orderProfileType", orderProfileType);
        variables.put("priceVersion", priceVersion);
        variables.put("paymentStage", 0);
        variables.put("projectCreationDate", "1743005981089");
        variables.put("elementalType", "");
        variables.put("orderBookingDate", null);
        updateData.put("variables", variables);
        filePath = "payloads/graphql/wardrobeMaterials.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }


    public String getZoneDataPayload(String roomType, String currentZoneId, String categoryId, String subCategoryId, String dimension, String moduleIdFromInput) {

        ArrayList<Map<String, Object>> moduleIdsPayload = new ArrayList<>();
        moduleIdsPayload.add(Map.of("moduleId", moduleIdFromInput, "dimension", dimension, "selectedShutterDesign", ""));
        variables.put("cityCode", cityCode);
        variables.put("roomType", roomType);
        variables.put("zoneId", currentZoneId);
        variables.put("categoryId", categoryId);
        variables.put("subCategoryId", subCategoryId);
        variables.put("moduleIds", moduleIdsPayload);
        variables.put("priceVersion", priceVersion);
        variables.put("completeData", true);
        variables.put("profileType", "REGULAR");
        variables.put("xpDoowupInHL", true);
        variables.put("orderProfileType", orderProfileType);
        variables.put("paymentStage", 0);
        variables.put("projectCreationDate", projectCreationDate);
        variables.put("isElemental", false);
        variables.put("elementalType", "");
        variables.put("orderBookingDate", null);
        updateData.put("variables", variables);
        if (orderProfileType.equalsIgnoreCase("DC")) {
            filePath = "payloads/graphql/zoneDataQuery.json";
        } else
            filePath = "payloads/graphql/hl/zoneDataQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String MultiPartShutterPayload(String moduleId, String dimension, String categoryId, String subCategoryId, String side) {
        variables.put("moduleId", moduleId);
        variables.put("applicableSection", side);
        variables.put("cityCode", cityCode);
        variables.put("categoryId", categoryId);
        variables.put("subCategoryId", subCategoryId);
        variables.put("dimension", dimension);
        updateData.put("variables", variables);
        filePath = "payloads/graphql/multipartShutter.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String woodenSkirtingApplicablePayload(String categoryId, String subCategoryId, String zoneId, String moduleWidth, String skirtingModuleId, String moduleId) {

        updateData.put("categoryId", categoryId);
        updateData.put("subCategoryId", subCategoryId);
        updateData.put("zoneCode", zoneId);
        updateData.put("modulesWithWidth", new ArrayList<>().add(Map.of("moduleId", moduleId, "width", moduleWidth)));
        updateData.put("skirtingModuleId", skirtingModuleId);
        filePath = "payloads/graphql/woodenSkirtingApplicable.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);

    }

    public String getZoneCatalogPayload(String roomType, String elementalType) {
        variables.put("roomType", roomType);
        variables.put("profile", orderProfileType);
        variables.put("elementalType", elementalType);
        variables.put("cityCode", cityCode);
        updateData.put("variables", variables);
        filePath = "payloads/graphql/multipartShutter.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getWardrobeMaterialPayload(String moduleId, String categoryId, String subCategoryId) {
        variables.put("moduleId", moduleId);
        variables.put("categoryId", categoryId);
        variables.put("subCategoryId", subCategoryId);
        variables.put("xpDoowupInHL", true);
        variables.put("priceVersion", priceVersion);

        variables.put("cityCode", cityCode);
        variables.put("finishId", "");
        variables.put("orderProfileType", orderProfileType);

        variables.put("paymentStage", "0");
        variables.put("projectCreationDate", projectCreationDate);
        variables.put("isElemental", false);
        variables.put("elementalType", "");
        variables.put("orderBookingDate", null);
        updateData.put("variables", variables);
        if (orderProfileType.equalsIgnoreCase("DC")) {
            filePath = "payloads/graphql/wardrobeMaterials.json";
        } else{
            filePath = "payloads/graphql/hl/wardrobeMaterials.json";
        }
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }
    public String getWardrobeShutterDesignsPayload(String moduleId, String dimension) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("moduleId", moduleId);
        variables.put("dimension", dimension);
        variables.put("cityId", cityCode);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("variables", variables);

        String filePath = "payloads/graphql/getwardrobeShutterDesign.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }
    public String getHandlesPayload(String categoryId, String subCategoryId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("categoryId", categoryId);
        variables.put("subCategoryId", subCategoryId);
        variables.put("internal", false);
        variables.put("orderProfileType", orderProfileType);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("variables", variables);

        String filePath = "payloads/graphql/getHandleQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getHandlePositionPayload(String moduleId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("modules", Collections.singletonList(moduleId));

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("variables", variables);

        String filePath = "payloads/graphql/getHandlePositionQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getServicesPayload(String roomType) {
        // Prepare variables map
        Map<String, Object> variables = new HashMap<>();
        variables.put("roomType", roomType);
        variables.put("priceVersion", hdsPriceVersion);
        variables.put("catalogProfileType", orderProfileType);
        variables.put("cancelledSuborder", false);

        // Prepare the map to update the payload
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("variables", variables);

        // Path to the JSON file containing the GraphQL query
        String filePath = "payloads/graphql/getServicesQuery.json";

        // Use your helper to update the payload JSON with variables
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getSubCategoryPayload(String categoryId, String subCategoryId, String priceVersion) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("categoryId", categoryId);
        variables.put("subCategoryId", subCategoryId);
        variables.put("priceVersion", priceVersion);
        variables.put("source", "SCPRO");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("variables", variables);
        String filePath = "payloads/graphql/getSubCategoryQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String getHdsElementsPayload(int length, String hdsId, int breadth, Integer height, int quantity) {
        // Prepare hdsElements object
        Map<String, Object> hdsElement = new HashMap<>();
        hdsElement.put("length", length);
        hdsElement.put("hdsId", hdsId);
        hdsElement.put("breadth", breadth);
        hdsElement.put("height", height);
        hdsElement.put("quantity", quantity);

        // Wrap into list
        List<Map<String, Object>> hdsElements = new ArrayList<>();
        hdsElements.add(hdsElement);

        Map<String, Object> payload = new HashMap<>();
        payload.put("hdsElements", hdsElements);
        String filePath = "payloads/graphql/hdsElementsTemplate.json";

        return updateJsons.updatePayloadFromMap(filePath, payload);
    }

    public String getFurniturePayload(String roomType, String productType) {
        // Prepare variables object
        Map<String, Object> variables = new HashMap<>();
        variables.put("roomType", roomType);
        variables.put("productType", productType);
        variables.put("catalogProfileType", orderProfileType);
        variables.put("city", catalogCity);
        variables.put("cancelledSuborder", false);


        // Prepare payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("variables", variables);

        String filePath = "payloads/graphql/getFurnitureQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, payload);
    }

    public String getProductPayload(String subCategoryId, String city, String productType) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("productId", null);
        variables.put("subCategoryId", subCategoryId);
        variables.put("city", city);
        variables.put("selectedFields", new ArrayList<>());
        variables.put("productType", productType);
        variables.put("catalogProfileType", orderProfileType);
        variables.put("cancelledSuborder", false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("variables", variables);

        String filePath = "payloads/graphql/getProductQuery.json";
        return updateJsons.updatePayloadFromMap(filePath, payload);
    }




}
