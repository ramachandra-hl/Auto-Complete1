package test;

import configurator.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import utils.PropertiesReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetCategoryAndSubCategoryBasedOnCityTest extends TestConfig {

    private static final List<Map<String, String>> ROOM_TYPES = new ArrayList<>();

    static {
        ROOM_TYPES.add(createRoomType("LIVINGROOM", ""));
        ROOM_TYPES.add(createRoomType("LIVINGROOM", "DECKO"));
        ROOM_TYPES.add(createRoomType("KITCHEN", ""));
        ROOM_TYPES.add(createRoomType("KITCHEN", "DECKO"));
        ROOM_TYPES.add(createRoomType("BEDROOM", ""));
        ROOM_TYPES.add(createRoomType("BEDROOM", "DECKO"));
        ROOM_TYPES.add(createRoomType("BATHROOM", ""));
        ROOM_TYPES.add(createRoomType("BATHROOM", "DECKO"));
    }

    private static Map<String, String> createRoomType(String type, String elementalType) {
        Map<String, String> roomType = new HashMap<>();
        roomType.put("type", type);
        roomType.put("elementalType", elementalType);
        return roomType;
    }

    @Test
    public void getCategories() {
        String filePath = "reports/CategoriesReports/" + PropertiesReader.cityCode + ".csv";

        for (Map<String, String> roomType : ROOM_TYPES) {
            Response response = synapseService.getZoneCatalog(roomType.get("type"), roomType.get("elementalType"));
            List<Map<String, Object>> zoneCatalogs = response.jsonPath().getList("data.getZoneCatalog");

            for (Map<String, Object> zoneCatalog : zoneCatalogs) {
                List<String> headers = new ArrayList<>();
                List<String> rowData = new ArrayList<>();
                headers.add("userSelectedCityProperty");
                headers.add("roomName");
                headers.add("categoryName");

                int subCategoryIndex = 1;
                boolean isFirstEntry = true;

                for (Map<String, Object> subCategory : (List<Map<String, Object>>) zoneCatalog.get("subCategoryList")) {
                    if (isFirstEntry) {
                        rowData.add(PropertiesReader.cityCode);
                        rowData.add(roomType.get("elementalType") + roomType.get("type"));
                        rowData.add((String) zoneCatalog.get("categoryName"));
                        isFirstEntry = false;
                    } else {
                        rowData.add("");
                        rowData.add("");
                        rowData.add("");
                    }

                    headers.add("subCategory" + subCategoryIndex++);
                    rowData.add((String) subCategory.get("subCategoryName"));

                    if (roomType.get("type").equalsIgnoreCase("KITCHEN")) {
                        List<String> kitchenHeaders = new ArrayList<>();
                        List<String> kitchenRowData = new ArrayList<>();
                        kitchenHeaders.add("userSelectedCityProperty");
                        kitchenHeaders.add("roomName");
                        kitchenHeaders.add("categoryName");

                        kitchenRowData.add(PropertiesReader.cityCode);
                        kitchenRowData.add(roomType.get("elementalType") + roomType.get("type"));
                        kitchenRowData.add((String) zoneCatalog.get("categoryName"));
                        kitchenRowData.add((String) subCategory.get("subCategoryName"));

                        int zoneTypeIndex = 1;
                        for (Map<String, Object> zoneType : (List<Map<String, Object>>) subCategory.get("zoneTypes")) {
                            kitchenHeaders.add("zoneType" + zoneTypeIndex++);
                            kitchenRowData.add((String) zoneType.get("zoneTypeName"));
                        }

                        utilities.appendTestResultsWithHeaders(
                                kitchenHeaders.toArray(new String[0]),
                                kitchenRowData,
                                filePath
                        );
                    }
                }

                if (!roomType.get("type").equalsIgnoreCase("KITCHEN")) {
                    utilities.appendTestResultsWithHeaders(
                            headers.toArray(new String[0]),
                            rowData,
                            filePath
                    );
                }
            }
        }
    }
}
