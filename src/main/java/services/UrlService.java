package services;

import com.aventstack.extentreports.Status;
import configurator.BaseClass;
import io.restassured.response.Response;
import utils.PropertiesReader;

import java.util.HashMap;
import java.util.Map;

import static configurator.ApiService.invokePostRequestWithCookies;
import static utils.PropertiesReader.*;


public class UrlService {
    PropertiesReader propertiesReader = new PropertiesReader(BaseClass.testData);
    String baseURl = propertiesReader.getBaseURl();
    String projectID = propertiesReader.getProjectID();
    public String getUrlSpaceCraft() {
        String url = baseURl + "/security/testUrl";

        Map<String, String> cookiesMap = new HashMap<>();
        Map<String, String> payload = new HashMap<>();
        payload.put("projectId", projectID);

        Response response = invokePostRequestWithCookies(url, payload, cookiesMap);
        if (response.statusCode() != 200) {
            logAndReport(String.valueOf(Status.FAIL),"Failed to initialize: {}", response.asString());
            throw new RuntimeException("Failed to initialize: " + response.asString());
        }

        return response.asPrettyString();
    }

    public String getSpaceCraftToken() {
        String url = getUrlSpaceCraft();
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
}
