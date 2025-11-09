package services;

import configurator.BaseClass;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import payloadHelper.SynapsePayloadHelper;
import utils.PropertiesReader;

import java.io.*;
import java.util.*;

import static configurator.ApiService.*;
import static utils.PropertiesReader.*;

public class SynapseService {
    private static final Logger log = LoggerFactory.getLogger(SynapseService.class);
    PropertiesReader propertiesReader = new PropertiesReader();
    String synapseUrl = propertiesReader.getSynapseUrl();
    String hdsSynapseUrl = propertiesReader.getHdsSynapseUrl();
    SynapsePayloadHelper synapsePayloadHelper = new SynapsePayloadHelper();


    public Response getMaterialColor(String materialType, String materialId) {
        logAndReport("Fetching material color for materialType: " + materialType + ", materialId: " + materialId);
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getMaterialColorPayload(materialType, materialId));
        if (response.getStatusCode() != 200) {
            logAndReport("Received response for material color: Status code {}", response.getStatusCode());
        }
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public Response getInternalsQuery(String moduleId, String dimension, String applicableSection, String categoryId, String subcategoryId) {
        logAndReport("Fetching internals query for moduleId: " + moduleId + ", dimension: " + dimension + ", applicableSection: " + applicableSection + ", categoryId: " + categoryId + ", subcategoryId: " + subcategoryId);
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getInternalsQueryPayload(moduleId, dimension, applicableSection, categoryId, subcategoryId));
        logAndReport("Received response for internals query: Status code " + response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }


    public Response getAccDetails(String internalId, String categoryId, String subcategoryId, String width, String depth, String height) {
       logAndReport("Fetching accessory details for internalId: {}, categoryId: {}, subcategoryId: {}, dimensions: {}/{}/{}",
                internalId, categoryId, subcategoryId, width, depth, height);

        String url = synapseUrl + "/wardrobe/internal/" + internalId +
                "/category/" + categoryId +
                "/subcategory/" + subcategoryId +
                "/accessories/" + width +
                "/" + depth +
                "/" + height +
                "?orderProfileType=" + orderProfileType;

        Response response = invokeGetRequest(url);
       logAndReport("Received response for accessory details: Status code "+ response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public boolean checkModuleDimension(String moduleId, String width, String depth, String height) {
       logAndReport("Checking module dimensions for moduleId: "+ moduleId);
        Response res = invokeGetRequest(synapseUrl + "/modules/" + moduleId + "/dimensions");
        List<Map<String, Object>> dimensions = res.jsonPath().getList("data");

        for (Map<String, Object> dimension : dimensions) {
            String dimWidth = dimension.get("width").toString();
            String dimDepth = dimension.get("depth").toString();
            String dimHeight = dimension.get("height").toString();

            if (dimWidth.equals(width) && dimDepth.equals(depth) && dimHeight.equals(height)) {
                return true;
            }
        }
        log.warn("No matching dimensions found for moduleId: {}", moduleId);
        return false;
    }

    public Response multiPartShutterData(String moduleId, String categoryId, String subCategoryId, String dimension, String side) {
        Response res = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.MultiPartShutterPayload(moduleId, dimension, categoryId, subCategoryId, side));
        Assert.assertEquals(res.getStatusCode(), 200, res.asPrettyString());
        return res;
    }

    public Response getWardrobeShutterDesigns(String moduleId, String dimension) {
        Response res = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getWardrobeShutterDesignsPayload(moduleId, dimension));
        Assert.assertEquals(res.getStatusCode(), 200, res.asPrettyString());
        return res;
    }

    public Map<String, String> getModuleDimension(String moduleId) {
       logAndReport("Fetching module dimensions From Synapse for moduleId: {}", moduleId);
        Response res = invokeGetRequest(synapseUrl + "/modules/" + moduleId);
        String midWidth;
        int minWidth = Integer.parseInt(res.jsonPath().getString("minWidth"));
        String maxDepth = res.jsonPath().getString("maxDepth");
        String maxHeight = res.jsonPath().getString("maxHeight");
        int maxWidth = Integer.parseInt(res.jsonPath().getString("maxWidth"));
        int nsWidthStep = Integer.parseInt(res.jsonPath().getString("nsWidthStep"));
        if (nsWidthStep != 0) {
            midWidth = String.valueOf((maxWidth + minWidth) / 2);
        } else {
            int reminder = ((maxWidth + minWidth) % 2);
            if (reminder == 0) {
                midWidth = String.valueOf((maxWidth + minWidth) / 2);
            } else {
                midWidth = String.valueOf(((maxWidth + minWidth) / 2) - reminder);
            }
        }

        Map<String, String> dimension = new HashMap<>();
        dimension.put("width", String.valueOf(maxWidth));
        dimension.put("depth", maxDepth);
        dimension.put("height", maxHeight);
       logAndReport("Retrieved dimensions for moduleId {}: {}/{}/{}", moduleId, midWidth, maxDepth, maxHeight);
        return dimension;
    }

    public Response getZoneData(String roomType, String currentZoneId, String categoryId, String subCategoryId, String dimension, String moduleIdFromInput) {
       logAndReport("Fetching zone data for roomType: {}, currentZoneId: {}, categoryId: {}, subCategoryId: {}", roomType, currentZoneId, categoryId, subCategoryId);
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getZoneDataPayload(roomType, currentZoneId, categoryId, subCategoryId, dimension, moduleIdFromInput));
       logAndReport("Received response for zone data: Status code {}", response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public Response getWardrobeMaterialData(String moduleId, String categoryId, String subCategoryId) {

        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getWardrobeMaterialPayload(moduleId, categoryId, subCategoryId));
       logAndReport("Received response for wardrobeMaterial data: Status code {}", response.getStatusCode());
        Assert.assertEquals(response.getStatusCode(), 200);
        return response;
    }

    public Response getShutterFamily() {
        return invokeGetRequest(synapseUrl + "/materials/shutter/families/design");
    }

    public Response checkWoodenSkirtingApplicable(String categoryId, String subCategoryId, String zoneId, String moduleWidth, String skirtingModuleId, String moduleId) {
        String payload = synapsePayloadHelper.woodenSkirtingApplicablePayload(categoryId, subCategoryId, zoneId, moduleWidth, skirtingModuleId, moduleId);
        return invokePostRequest(synapseUrl + "/unitEntry/checkWoodenSkirtingApplicable?orderProfileType=" + orderProfileType, payload);
    }

    public Map<String, String> getCategoryDetails(String moduleId) {
        Response response = invokeGetRequest(synapseUrl + "/category/module/" + moduleId);
        Map<String, String> categoryDetails = new HashMap<>();
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch category details for module: " + moduleId);

        // 🔹 Handle both list and single-object JSON responses safely
        Object json = response.jsonPath().get("");
        List<Map<String, Object>> categories = new ArrayList<>();
        if (json instanceof List) {
            categories = (List<Map<String, Object>>) json;
        } else if (json instanceof Map) {
            categories.add((Map<String, Object>) json);
        }

        if (categories.isEmpty()) {
            throw new RuntimeException("No categories found for moduleId: " + moduleId);
        }

        // 🔹 Cache QP flag once to avoid repeated getter calls
        boolean isQPEnabled = Boolean.parseBoolean(propertiesReader.getIsQPModules());

        Map<String, Object> selectedCategory = null;
        for (Map<String, Object> cat : categories) {
            String catName = String.valueOf(cat.get("categoryName"));
            String subCategoryName = String.valueOf(cat.get("subCategoryName"));

            // 1️⃣ Skip Decko subcategories
            if (subCategoryName != null && subCategoryName.trim().toLowerCase().startsWith("decko")) {
                continue;
            }

            // 2️⃣ Apply QP filter logic once
            if (!isQPEnabled && catName != null && catName.contains("-QP")) {
                continue;
            }
            if (isQPEnabled && (catName == null || !catName.contains("-QP"))) {
                continue;
            }

            // 3️⃣ Skip ELEMENTAL room types
            List<String> roomTypes = (List<String>) cat.get("roomTypes");
            if (roomTypes != null && roomTypes.stream().anyMatch(rt -> rt != null && rt.contains("ELEMENTAL"))) {
                continue;
            }

            // ✅ Found valid category
            selectedCategory = cat;
            break;
        }

        if (selectedCategory == null && !categories.isEmpty()) {
            selectedCategory = categories.get(0);
        }

        // 🔹 Extract details
        categoryDetails.put("categoryId", String.valueOf(selectedCategory.get("categoryId")));
        categoryDetails.put("categoryName", String.valueOf(selectedCategory.get("categoryName")));
        categoryDetails.put("subCategoryId", String.valueOf(selectedCategory.get("subCategoryId")));
        categoryDetails.put("subCategoryName", String.valueOf(selectedCategory.get("subCategoryName")));

        // 🔹 Pick best roomType
        List<String> roomTypes = (List<String>) selectedCategory.get("roomTypes");
        if (roomTypes != null) {
            List<String> priority = Arrays.asList("KITCHEN", "BEDROOM", "BATHROOM", "LIVINGROOM");
            for (String preferred : priority) {
                if (roomTypes.contains(preferred)) {
                    categoryDetails.put("roomType", ROOM_TYPE_MAPPING.get(preferred));
                    break;
                }
            }
        }

        return categoryDetails;
    }


    // Add this method to SynapseService.java
    private void writeCategoryToCSV(String moduleId) {
        String fileName = "module_ids.csv";
        boolean fileExists = new java.io.File(fileName).exists();

        try (FileWriter writer = new FileWriter(fileName, true)) {
            // If file doesn't exist, write header
            if (!fileExists) {
                writer.append("moduleId\n");
            }
            writer.append(moduleId).append("\n");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write moduleId to CSV", e);
        }
    }


    public List<Map<String, String>> getInternalSetList(String moduleId) {
        String url = synapseUrl + "/wardrobe/module/" + moduleId + "/internal";
        Response response = invokeGetRequest(url);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());

        List<Map<String, Object>> internals = response.jsonPath().getList("moduleInternalList");
        List<Map<String, String>> result = new ArrayList<>();

        for (Map<String, Object> internal : internals) {
            Map<String, String> entry = new HashMap<>();
            entry.put("internalId", String.valueOf(internal.get("internalSetID")));
            entry.put("internalName", String.valueOf(internal.get("internalSetName")));
            result.add(entry);
        }
        return result;
    }
    // Room type mapping stored statically for better performance
    private static final Map<String, String> ROOM_TYPE_MAPPING = new HashMap<>();

    static {
        ROOM_TYPE_MAPPING.put("BEDROOM", "Bedroom");
        ROOM_TYPE_MAPPING.put("BEDROOM_ELEMENTAL", "Deckobedroom");
        ROOM_TYPE_MAPPING.put("LIVINGROOM", "Livingroom");
        ROOM_TYPE_MAPPING.put("LIVINGROOM_ELEMENTAL", "Deckolivingroom");
        ROOM_TYPE_MAPPING.put("KITCHEN", "Kitchen");
        ROOM_TYPE_MAPPING.put("KITCHEN_ELEMENTAL", "Deckokitchen");
        ROOM_TYPE_MAPPING.put("BATHROOM", "Bathroom");
        ROOM_TYPE_MAPPING.put("BATHROOM_ELEMENTAL", "Deckobathroom");
    }


    public Map<String, String> getInternalDetails(String internalModuleId) {
        Map<String, String> internalDetails = new HashMap<>();
        Response response = invokeGetRequest(synapseUrl + "/wardrobe/internal/" + internalModuleId);

        Assert.assertEquals(response.getStatusCode(), 200);
        String width = response.jsonPath().getString("width.min");
        String depth = response.jsonPath().getString("depth.min");
        String height = response.jsonPath().getString("height.min");
        String applicableSections = response.jsonPath().getString("applicableSections");

        double widthValue = Double.parseDouble(width);
        double depthValue = Double.parseDouble(depth);
        double heightValue = Double.parseDouble(height);

        String sectionDimension = String.format("%.0f X %.0f X %.0f", widthValue, depthValue, heightValue);

        internalDetails.put("sectionDimension", sectionDimension);
        internalDetails.put("applicableSections", applicableSections);
        return internalDetails;
    }

    public Response getZoneCatalog(String roomType, String elementalType) {
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getZoneCatalogPayload(roomType, elementalType));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        return response;
    }

    public String getModuleProfileById(String moduleId) {
        Response res = invokeGetRequest(synapseUrl + "/modules/" + moduleId);
        String type = null;
        if (res.jsonPath().getBoolean("isGola")) {
            type = "Gola";
        } else if (res.jsonPath().getBoolean("isGProfile")) {
            type = "GP";
        }
        return type;
    }

    public Map<String, String> getModuleByInternalId(String internalId) {
        Response response = invokeGetRequest(synapseUrl + "/wardrobe/internal/" + internalId);
        Map<String, Object> modules = (Map<String, Object>) response.jsonPath().getList("modules").get(0);
        Map<String, String> returnData = new HashMap<>();
        String maxWidth = String.valueOf((int)response.jsonPath().getDouble("width.max"));
        String maxDepth = String.valueOf((int)response.jsonPath().getDouble("depth.max"));
        String maxHeight = String.valueOf((int)response.jsonPath().getDouble("height.max"));

        returnData.put("moduleID", String.valueOf((modules.get("moduleID"))));
        returnData.put("moduleName", (String) modules.get("moduleName"));
        returnData.put("moduleWidth", maxWidth);
        returnData.put("moduleDepth", maxDepth);
        returnData.put("moduleHeight", maxHeight);
        return returnData;
    }

    public Response getHandles(String categoryId, String subCategoryId) {
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getHandlesPayload(categoryId, subCategoryId));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        return response;
    }
    public Response getHandlePosition(String moduleId) {
        Response response = invokePostRequest(synapseUrl + "/graphql", synapsePayloadHelper.getHandlePositionPayload(moduleId));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        return response;
    }
    public  Map<String, String> getServiceDetails(String roomType,String subCategoryId) {
        Response response = invokePostRequest(hdsSynapseUrl+"/graphql", synapsePayloadHelper.getServicesPayload(roomType));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        List<Map<String, Object>> services = response.jsonPath().getList("data.getServices");
        for (Map<String, Object> service : services) {
            String categoryName = (String) service.get("categoryName");
            String categoryId = (String) service.get("categoryId");

            List<Map<String, Object>> subCategories = (List<Map<String, Object>>) service.get("subCategories");
            for (Map<String, Object> subCat : subCategories) {
                String subCatId = (String) subCat.get("subCategoryId");
                String subCatName = (String) subCat.get("subCategoryName");

                if (subCatId.equals(subCategoryId)) {
                    // Create a map with the required details
                    Map<String, String> result = new HashMap<>();
                    result.put("categoryName", categoryName);
                    result.put("categoryId", categoryId);
                    result.put("subCategoryId", subCatId);
                    result.put("subCategoryName", subCatName);
                    return result;
                }
            }
        }
        return null;
    }

    public Map<String, Object> getSKUDetails(String categoryId, String subCategoryId, String priceVersion, String idToMatch) {
        Response response = invokePostRequest(hdsSynapseUrl+"/graphql", synapsePayloadHelper.getSubCategoryPayload(categoryId, subCategoryId, priceVersion));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        List<Map<String, Object>> subCategories = response.jsonPath().getList("data.getSubCategory");

        // Iterate and find the matching ID
        for (Map<String, Object> subCat : subCategories) {
            String currentId = String.valueOf(subCat.get("id")); // Ensure comparison as string
            if (currentId.equals(idToMatch)) {
                return subCat;
            }
        }
        return null;
    }

    public Map<String, String> getFurnitureDetails(String roomType,String HDSType,String subCategoryIdParam) {
        Response response = invokePostRequest(hdsSynapseUrl+"/graphql", synapsePayloadHelper.getFurniturePayload(roomType, HDSType));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());

        List<Map<String, Object>> furnitureList = response.jsonPath().getList("data.getFurniture");

        for (Map<String, Object> category : furnitureList) {
            int categoryId = (Integer) category.get("categoryId");
            String categoryName = (String) category.get("categoryName");

            List<Map<String, Object>> subCategoryList = (List<Map<String, Object>>) category.get("subCategoryList");

            for (Map<String, Object> subCategory : subCategoryList) {
                int subCategoryId = (Integer) subCategory.get("subCategoryId");
                String subCategoryName = (String) subCategory.get("subCategoryName");

                if (subCategoryId == Integer.parseInt(subCategoryIdParam)) {
                    // Found a match → return details
                    Map<String, String> result = new HashMap<>();
                    result.put("categoryId", String.valueOf(categoryId));
                    result.put("categoryName", categoryName);
                    result.put("subCategoryId", String.valueOf(subCategoryId));
                    result.put("subCategoryName", subCategoryName);
                    return result;
                }
            }
        }

        return null;
    }

    public Map<String, Object> getProductDetails(String subCategoryId,String productType, String skuId) {
        Response response = invokePostRequest(hdsSynapseUrl+"/graphql", synapsePayloadHelper.getProductPayload(subCategoryId, catalogCity, productType));
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        // Get list of products
        List<Map<String, Object>> productList = response.jsonPath().getList("data.getProduct");

        for (Map<String, Object> product : productList) {
            String name = (String) product.get("name");
            int baseSkuId = (Integer) product.get("baseSkuId");
            int productIdVal = (Integer) product.get("productId");
            String baseImage = (String) product.get("baseImage");

            List<Map<String, Object>> skuList = (List<Map<String, Object>>) product.get("skuList");

            for (Map<String, Object> sku : skuList) {
                int skuIdVal = (Integer) sku.get("skuId");

                if (String.valueOf(skuIdVal).equals(skuId)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("productId", productIdVal);
                    result.put("name", name);
                    result.put("hlSkuId", baseSkuId);
                    result.put("baseImage", baseImage);
                    result.put("matchedSku", sku);
                    return result;
                }
            }
        }

        return null;
    }

    public Map<String,Object> getWallCladdingPanelMaterialId(String categoryId, String skuId) {
        Response response = invokeGetRequest(hdsSynapseUrl+"/catalogue/wallCladding/category/"+categoryId);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        List<Map<String, Object>> categories = response.jsonPath().getList("wallpaper");

        for (Map<String, Object> category : categories) {
            String catId = String.valueOf(category.get("colorId"));
            if (catId.equals(skuId)) {
                Map<String,Object> result = new HashMap<>();
                result.put("materialId",category.get("id"));
                result.put("imageUrl", (((List<Map<String,Object>>)((Map<String,Object>)category.get("materialData")).get("mapProperties")).get(0)).get("imageUrl"));
                return result;
            }
        }
        return null;

    }

    public Map<String, String> getWallCladdingCategory(String catId){
        Response response = invokeGetRequest(hdsSynapseUrl+"/catalogue/wallCladding/categories");
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        List<Map<String, Object>> categories = response.jsonPath().getList("");
        if(categories.size()>0){
            for (Map<String, Object> category : categories) {
                String subCategoryId = String.valueOf(category.get("valenceId"));
                if (catId.equals(subCategoryId)) {
                    Map<String, String> result = new HashMap<>();
                    result.put("categoryId", category.get("id").toString());
                    result.put("categoryName", "Wall Cladding");
                    result.put("subCategoryId", subCategoryId);
                    result.put("subCategoryName", (String) category.get("categoryName"));
                    return result;
                }
            }
        }
        return null;
    }
}

