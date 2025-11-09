package payloadHelper;

import utils.UpdateJsons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HdsAndAdsPayloadHelper {
    UpdateJsons updateJsons = new UpdateJsons();
    Map<String, Object> updateData = new HashMap<>();
    Map<String, Object> variables = new HashMap<>();
    String filePath;

    public String servicePayload(
            Map<String, Object> quoteData,
            String projectId,
            String floorId,
            String roomId
    ) {
        // Build hdsServiceData
        Map<String, Object> hdsServiceData = new HashMap<>();
        hdsServiceData.put("objectId", null);
        hdsServiceData.put("projectId", projectId);
        hdsServiceData.put("floorId", floorId);
        hdsServiceData.put("roomId", roomId);
        hdsServiceData.put("unitEntryId", "");
        hdsServiceData.put("wallId", "");
        hdsServiceData.put("visualEntity", new HashMap<>());
        hdsServiceData.put("hasScVisuals", false);
        hdsServiceData.put("visibility", false);
        hdsServiceData.put("serviceType", "BESPOKE");

        Map<String, Object> payload = new HashMap<>();
        String quoteKey = quoteData.getOrDefault("categoryId", "").toString().contains("COUNTER_TOP")
                ? "countertopQuoteData"
                : "remainingServicesQuoteData";

        payload.put(quoteKey, quoteData);
        payload.put("hdsServiceData", hdsServiceData);
        filePath = "payloads/HDS&ADS/Service.json";
        return updateJsons.updatePayloadFromMap(filePath, payload);
    }

    public String furnitureHdsPayload(
            Map<String, Object> furnitureData,
            String projectId,
            String floorId,
            String roomId,
            Map<String, Object> skuData
    ) {
        // Build hdsServiceData

        Map<String, Object> hdsServiceData = new HashMap<>();
        hdsServiceData.put("projectId", projectId);
        hdsServiceData.put("floorId", floorId);
        hdsServiceData.put("roomId", roomId);
        hdsServiceData.put("unitEntryId", null);

        Map<String, Object> pointEntity = new HashMap<>();
        Map<String, Object> visualEntity = new HashMap<>();
        if (!(furnitureData.get("category").toString()).equalsIgnoreCase("wall Cladding")){
        pointEntity.put("stroke", "");
        pointEntity.put("angle", "");
        pointEntity.put("type", "");
        Map<String, Object> loc = new HashMap<>();
        loc.put("x", 0);
        loc.put("y", 0);
        pointEntity.put("loc", loc);
        pointEntity.put("shape", "");
        pointEntity.put("height", "");
        pointEntity.put("radius", "");
        pointEntity.put("heightFromFloor", "");
        pointEntity.put("url", "");
        pointEntity.put("wallDir", "");
        pointEntity.put("group", "");
        pointEntity.put("description", "");
        pointEntity.put("pointType", "PROPOSED");
        pointEntity.put("connectedSocketId", null);
    }else {
    visualEntity.put("description", skuData.get("productName"));
    visualEntity.put("lineEntityCaption", "");
    visualEntity.put("pipeType", "");
    visualEntity.put("runningLength", "");
    visualEntity.put("pointEntities", new ArrayList<>());
    // empty list
            List<Map<String, Object>> framePanelData = new ArrayList<>();

            visualEntity.put("framePanelData", framePanelData);

    // framePanelData (list of maps)
    Map<String, Object> framePanel = new HashMap<>();
    framePanel.put("objectId", "");
    framePanel.put("panelDimension", "120x11x2440");
    List<Map<String,Object>> fieldData = (List<Map<String, Object>>) skuData.get("fieldData");
    for (Map<String, Object> field : fieldData){
        if (field.get("fieldName").equals("Description")) {
            framePanel.put("framePanelName", field.get("fieldValue"));
        }
        if (field.get("fieldName").equals("Panels Per Box")){
            visualEntity.put("perBoxPanels", field.get("fieldValue"));
        }
    }
    framePanel.put("panelAlignment", "left to right");
    framePanel.put("panelQtyForFramePanel", skuData.get("quantity"));
    framePanel.put("orientation_x", 0);
    framePanel.put("orientation_y", 1.5707963267948966);
    framePanel.put("orientation_z", 0);
    framePanel.put("position_x", 0);
    framePanel.put("position_y", 0);
    framePanel.put("position_z", 0);

    Map<String, Object> scale = new HashMap<>();
    scale.put("x", 0);
    scale.put("y", 0);
    scale.put("z", 0);
    framePanel.put("scale", scale);

    framePanel.put("width", 1000);
    framePanel.put("height", 1000);
    framePanel.put("depth", "11");
    framePanel.put("materialId", skuData.get("backPanelMaterialId"));
    framePanelData.add(framePanel);
}

        hdsServiceData.put("pointEntity", pointEntity);
        hdsServiceData.put("visualEntity", visualEntity);
        hdsServiceData.put("visibility", true);
        hdsServiceData.put("serviceType", "QUANTITY");
        hdsServiceData.put("hdsType", skuData.get("hdsType"));

        // Build payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("furnitureHdsQuoteData", furnitureData);
        payload.put("hdsServiceData", hdsServiceData);

        String filePath = "payloads/HDS&ADS/looseFurniture.json";
        return updateJsons.updatePayloadFromMap(filePath, payload);
    }

}
