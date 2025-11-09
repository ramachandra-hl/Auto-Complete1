package services;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import payloadHelper.HdsAndAdsPayloadHelper;
import utils.PropertiesReader;

import java.util.*;

import static configurator.ApiService.*;
import static configurator.BaseClass.*;


public class HdsAndAdsService {
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    String baseURl = propertiesReader.getBaseURl();
    SynapseService synapseService = new SynapseService();
    HdsAndAdsPayloadHelper hdsAndAdsPayloadHelper = new HdsAndAdsPayloadHelper();

    public void applyHdsService(String projectId, String floorId, String roomId, String hdsSubCategoryId, String roomType, String HDSType, String skuId, Map<String, String> token) {
        Map<String, String> serviceDetails = synapseService.getServiceDetails(roomType, hdsSubCategoryId);
        logAndReport("Service Details: {}", serviceDetails);

        Map<String, Object> hdsPayload = synapseService.getSKUDetails(serviceDetails.get("categoryId"), serviceDetails.get("subCategoryId"), hdsPriceVersion, skuId);
        logAndReport("HDS Payload: {}", hdsPayload);

        Map<String, Object> quoteData = new HashMap<>();
        quoteData.put("hdsId", hdsPayload.get("id"));
        quoteData.put("name", hdsPayload.get("name"));
        quoteData.put("categoryId", serviceDetails.get("categoryId"));
        quoteData.put("category", serviceDetails.get("categoryName"));
        quoteData.put("subCategoryId", serviceDetails.get("subCategoryId"));
        quoteData.put("subCategory", serviceDetails.get("subCategoryName"));

        boolean hasLength = hdsPayload.get("haveLength") != null && (boolean) hdsPayload.get("haveLength");
        boolean hasBreadth = hdsPayload.get("haveBreadth") != null && (boolean) hdsPayload.get("haveBreadth");
        boolean hasHeight = hdsPayload.get("haveHeight") != null && (boolean) hdsPayload.get("haveHeight");
        boolean hasQuantity = hdsPayload.get("haveQuantity") != null && (boolean) hdsPayload.get("haveQuantity");
        boolean hasTx = hdsPayload.get("haveTx") != null && (boolean) hdsPayload.get("haveTx");

        quoteData.put("length", hasLength ? 1000 : null);
        quoteData.put("breadth", hasBreadth ? 1000 : null);
        quoteData.put("height", hasHeight ? 1000 : null);
        quoteData.put("quantity", hasQuantity ? 10 : 1);

        quoteData.put("hasLength", hasLength);
        quoteData.put("hasBreadth", hasBreadth);
        quoteData.put("hasHeight", hasHeight);
        quoteData.put("hasQuantity", hasQuantity);
        quoteData.put("haveTx", hasTx);

        String finalPayload = hdsAndAdsPayloadHelper.servicePayload(quoteData, projectId, floorId, roomId);
        logAndReport("Final Service Payload => {}", finalPayload);

        String url = baseURl + "/api/v1.0/project/" + projectId + "/categoryId/" + serviceDetails.get("categoryId") + "/service";
        logAndReport("POST URL: {}", url);

        Response response = invokePostRequest(url, finalPayload, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("Service Applied Successfully: {}", response.asPrettyString());
    }

    public void applyLooseFurniture(String projectId, String floorId, String roomId, String hdsSubCategoryId, String roomType, String HDSType, String skuId, Map<String, String> token) {
        Map<String, String> furnitureDetails = synapseService.getFurnitureDetails(roomType, HDSType, hdsSubCategoryId);

        if (furnitureDetails != null && furnitureDetails.get("categoryName").equalsIgnoreCase("wall Cladding")) {
            furnitureDetails = synapseService.getWallCladdingCategory(hdsSubCategoryId);
            logAndReport("Wall Cladding Furniture Details: {}", furnitureDetails);
        }

        Map<String, Object> hdsData = synapseService.getProductDetails(furnitureDetails.get("subCategoryId"), "", skuId);
        logAndReport("HDS Furniture Data: {}", hdsData);

        Map<String, Object> quoteData = new HashMap<>();
        quoteData.put("productId", hdsData.get("productId"));
        quoteData.put("productName", hdsData.get("name"));
        quoteData.put("name", hdsData.get("name"));
        quoteData.put("categoryId", furnitureDetails.get("categoryId"));
        quoteData.put("category", furnitureDetails.get("categoryName"));
        quoteData.put("subCategoryId", furnitureDetails.get("subCategoryId"));
        quoteData.put("subCategory", furnitureDetails.get("subCategoryName"));

        Map<String, Object> skuData = (Map<String, Object>) hdsData.get("matchedSku");
        skuData.put("hdsType", HDSType);
        quoteData.put("hlSkuId", skuData.get("hlSkuId"));

        ArrayList<Map<String, Object>> fieldData = (ArrayList<Map<String, Object>>) skuData.get("fieldData");
        for (Map<String, Object> field : fieldData) {
            if ("Brand".equals(field.get("fieldName"))) quoteData.put("brand", field.get("fieldValue"));
            if ("Description".equals(field.get("fieldName"))) quoteData.put("Description", field.get("fieldValue"));
        }

        Map<String, Object> colourData = new HashMap<>();
        if ("wall Cladding".equalsIgnoreCase(furnitureDetails.get("categoryName"))) {
            colourData = synapseService.getWallCladdingPanelMaterialId(furnitureDetails.get("categoryId"), skuData.get("hlSkuId").toString());
            quoteData.put("imageUrl", colourData.get("imageUrl"));
            skuData.put("backPanelMaterialId", colourData.get("materialId"));
        } else {
            quoteData.put("imageUrl", ((ArrayList) skuData.get("imageList")).get(0));
        }

        String finalPayload = hdsAndAdsPayloadHelper.furnitureHdsPayload(quoteData, projectId, floorId, roomId, skuData);
        logAndReport("Final Furniture Payload => {}", finalPayload);

        String url = baseURl + "/api/v1.0/project/" + projectId + "/categoryId/" + HDSType + "/service";

        Response response = invokePostRequest(url, finalPayload, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("Furniture Service Applied Successfully");
    }
}
