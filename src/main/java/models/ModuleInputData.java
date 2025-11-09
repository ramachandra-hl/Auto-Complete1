package models;

import java.util.HashMap;
import java.util.Map;

public class ModuleInputData {
    private final String serialNo;
    private final String roomType;
    private final String roomName;
    private final String moduleId;
    private final String moduleName;
    private final String width;
    private final String depth;
    private final String height;
    private final String carcassCode;
    private final String carcassColour;
    private final String internalWidth;
    private final String internalDepth;
    private final String internalHeight;
    private final String internalId;
    private final String section;
    private final String accessoryCode;
    private final String skirtingType;
    private final String skirtingModuleId;

    private final Map<String, Object> shutterDetailsList;
    private final String fillerId;
    private final String designerName;
    private final String designerEmail;
    private final String designerId;
    private final String officeCity;
    private final String customerId;
    private final String handleId;
    private final String handlePositionId;

    private final  String subCategoryId;
    private final  String categoryId;
    private final  String HDSType;
    private final  String skuId;
    private final String categoryName;
    private final String subCategoryName;

    // Utility method to safely retrieve values
    private static String getValue(Map<String, String> map, String key) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                String value = entry.getValue();
                return (value != null && !value.isEmpty()) ? value : null;
            }
        }
        return null;
    }


    public ModuleInputData(Map<String, String> roomDetails) {
        this.serialNo= getValue(roomDetails, "s.no");
        this.roomType = getValue(roomDetails, "roomType");
        this.roomName = getValue(roomDetails, "roomName");
        this.moduleId = getValue(roomDetails, "moduleId");
        this.moduleName = getValue(roomDetails, "moduleName");
        this.width = getValue(roomDetails, "width");
        this.depth = getValue(roomDetails, "depth");
        this.height = getValue(roomDetails, "height");
        this.carcassCode = getValue(roomDetails, "carcassCode");
        this.carcassColour = getValue(roomDetails, "carcassColourCode");

        this.internalWidth = getValue(roomDetails, "internalWidth");
        this.internalDepth = getValue(roomDetails, "internalDepth");
        this.internalHeight = getValue(roomDetails, "internalHeight");
        this.internalId = getValue(roomDetails, "internalId");
        this.section = getValue(roomDetails, "section");

        this.accessoryCode = getValue(roomDetails, "accessoryCode");
        this.skirtingType = getValue(roomDetails, "skirtingType");
        this.skirtingModuleId = getValue(roomDetails, "skirtingModuleId");
        this.fillerId = getValue(roomDetails, "fillerId");
        this.designerName =getValue(roomDetails,"designerName");
        this.designerEmail = getValue(roomDetails, "designerEmail");
        this.designerId = getValue(roomDetails, "designerId");
        this.officeCity=getValue(roomDetails,"officeCity");
        this.customerId = getValue(roomDetails,"customerId");
        this.handleId = getValue(roomDetails, "handleId");
        this.handlePositionId = getValue(roomDetails, "handlePositionId");

        this.shutterDetailsList =new HashMap<>();

        shutterDetailsList.put("shutterCategory", getValue(roomDetails, "shutterCategory"));
        shutterDetailsList.put("shutterCoreCode", getValue(roomDetails, "shutterCoreCode"));
        shutterDetailsList.put("shutterFinishCode", getValue(roomDetails, "shutterFinishCode"));
        shutterDetailsList.put("shutterColourCode", getValue(roomDetails, "shutterColourCode"));

        shutterDetailsList.put("shutterSide", getValue(roomDetails, "shutterSide"));
        shutterDetailsList.put("components", getValue(roomDetails, "components"));
        shutterDetailsList.put("shutterCode", getValue(roomDetails, "shutterCode" ));
        shutterDetailsList.put("frameCode", getValue(roomDetails, "frameCode" ));
        shutterDetailsList.put("frameFinishCode", getValue(roomDetails, "frameFinishCode" ));
        shutterDetailsList.put("frameColourCode", getValue(roomDetails, "frameColourCode" ));


        shutterDetailsList.put("finishCategoryName",getValue(roomDetails,"finishCategoryName"));
        shutterDetailsList.put("shutterDesignsName", getValue(roomDetails,"shutterDesignsName"));
        shutterDetailsList.put("infillMaterialId", getValue(roomDetails, "infillMaterialId"));
        shutterDetailsList.put("infillMaterialColourId", getValue(roomDetails, "infillMaterialColourId"));

        shutterDetailsList.put("finishType", getValue(roomDetails,"finishType"));
        shutterDetailsList.put("finishSubType", getValue(roomDetails, "finishSubType"));
        shutterDetailsList.put("designName", getValue(roomDetails, "designName"));


        shutterDetailsList.put("panelIds", getValue(roomDetails, "panelIds"));
        shutterDetailsList.put("panelCoreCode", getValue(roomDetails, "panelCoreCode"));
        shutterDetailsList.put("panelFinishCode", getValue(roomDetails, "panelFinishCode"));
        shutterDetailsList.put("panelColourCode", getValue(roomDetails, "panelColourCode"));

        this.subCategoryId = getValue(roomDetails, "subCategoryId");
        this.categoryId = getValue(roomDetails, "categoryId");
        this.HDSType = getValue(roomDetails, "hdsType");
        this.skuId = getValue(roomDetails, "skuId");
        this.categoryName = getValue(roomDetails, "categoryName");
        this.subCategoryName = getValue(roomDetails, "subCategoryName");

    }


    // Getters
    public String getSerialNo() {
        return serialNo;
    }
    public String getRoomType() {
        return roomType;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getWidth() {
        return width;
    }

    public String getDepth() {
        return depth;
    }

    public String getHeight() {
        return height;
    }

    public String getCarcassCode() {
        return carcassCode;
    }

    public String getCarcassColour() {
        return carcassColour;
    }


    public String getInternalWidth() {
        return internalWidth;
    }

    public String getInternalDepth() {
        return internalDepth;
    }

    public String getInternalHeight() {
        return internalHeight;
    }

    public String getInternalId() {
        return internalId;
    }

    public String getSection() {
        return section;
    }

    public String getAccessoryCode() {
        return accessoryCode;
    }

    public String getSkirtingType() {
        return skirtingType;
    }

    public String getSkirtingModuleId() {
        return skirtingModuleId;
    }

    public Map<String, Object> getShutterDetailsList() {
        return shutterDetailsList;
    }

    public String getFillerId() {
        return fillerId;
    }

    public String getDesignerName(){
        return designerName;
    }

    public String getDesignerEmail(){
        return designerEmail;
    }

    public String getDesignerId(){
        return designerId;
    }

    public String getOfficeCity(){
        return officeCity;
    }

    public String getCustomerId(){
        return customerId;
    }

    public String getShutterCategory() {
        return  (String) shutterDetailsList.get("shutterCategory");
    }
    public String getHandleId() {
        return handleId;
    }
    public String getHandlePositionId() {
        return handlePositionId;
    }
    public String getSubCategoryId() {
        return subCategoryId;
    }
    public String getCategoryId() {
        return categoryId;
    }
    public String getHDSType() {
        return HDSType;
    }
    public String getSkuId() {
        return skuId;
    }

    public String getCategoryName(){
        return categoryName;
    }

    public String  getSubCategoryName(){
        return subCategoryName;
    }
}

