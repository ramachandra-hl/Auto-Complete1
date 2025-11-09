package configurator;

import io.restassured.response.Response;
import services.*;
import utils.PropertiesReader;
import utils.Utilities;

import java.util.Map;

import static configurator.ApiService.buildAuthorizationHeader;

public class TestConfig extends BaseClass {

    public static String projectID;
    public static String floorID;
    public static String user_id;
    public static String dp_id;
    public static String customerId;
    public static Map<String, String> token;

    private Response response;
    public RoomService roomService;
    public ModuleService moduleService;
    public SynapseService synapseService;
    public ScBackendService scBackendService;
    public PropertiesReader propertiesReader;
    public Utilities utilities;

    public String isCarcass;
    public String isShutter;
    public String isInternals;
    public String isAccessories;

    public TestConfig() {
        // ✅ Step 1: Ensure test data is loaded before using it
        ApiService.refreshTestData();

        // ✅ Step 2: Initialize helper classes after test data is ready
        propertiesReader = new PropertiesReader(testData);
        roomService = new RoomService();
        moduleService = new ModuleService();
        synapseService = new SynapseService();
        scBackendService = new ScBackendService();
        utilities = new Utilities();

        // ✅ Step 3: Initialize token after data & URLs are available
        token = buildAuthorizationHeader(new UrlService().getSpaceCraftToken());

        // ✅ Step 4: Initialize flags safely
        isCarcass = getOrDefault(propertiesReader.getIsCarcass(), "FALSE");
        isShutter = getOrDefault(propertiesReader.getIsShutter(), "FALSE");
        isInternals = getOrDefault(propertiesReader.getIsInternals(), "FALSE");
        isAccessories = getOrDefault(propertiesReader.getIsAccessories(), "FALSE");

        // ✅ Step 5: Initialize project details
        initializeProjectDetails();
    }

    private void initializeProjectDetails() {
        projectID = propertiesReader.getProjectID();

        if (projectID == null || projectID.isEmpty()) {
            log.error("❌ projectID is missing in testData. Check configuration or CSV input.");
            return;
        }

        response = roomService.getProjectDetails(projectID, token);

        if (response != null && response.getStatusCode() == 200) {
            floorID = response.jsonPath().getString("selectedFloorId");
            orderProfileType = safeUpper(response.jsonPath().getString("customer.orderProfile"));
            projectCreationDate = response.jsonPath().getString("createdTimeStampMillis");
            priceVersion = response.jsonPath().getString("priceVersion");
            hdsPriceVersion = response.jsonPath().getString("hdsPriceVersion");
            cityCode = response.jsonPath().getString("cityId");
            user_id = response.jsonPath().getString("customer.userId");
            dp_id = response.jsonPath().getString("dp.email");
            customerId = response.jsonPath().getString("customer.customerId");
            catalogCity = response.jsonPath().getString("property.catalogCity");

            System.out.println("🔍 [Current Project Info] 🔽" +
                    "\n   🏷️ orderProfileType ➡️ " + orderProfileType +
                    "\n   💰 priceVersion ➡️ " + priceVersion +
                    "\n   💰 hdsPriceVersion ➡️ " + hdsPriceVersion +
                    "\n   🌆 cityCode ➡️ " + cityCode);
        } else {
            log.error("Failed to fetch project details for projectID: {}", projectID);
        }
    }

    private String getOrDefault(String value, String def) {
        return (value != null && !value.isEmpty()) ? value : def;
    }

    private String safeUpper(String value) {
        return (value != null) ? value.toUpperCase() : "";
    }
}
