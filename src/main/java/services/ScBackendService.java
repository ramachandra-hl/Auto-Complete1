package services;

import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import payloadHelper.ModuleServicePayloadHelper;
import utils.PropertiesReader;

import java.util.Map;

import static configurator.ApiService.*;
import static configurator.BaseClass.*;

public class ScBackendService {
PropertiesReader propertiesReader = new PropertiesReader();
String baseURl = propertiesReader.getBaseURl();

    public void endMeeting(String projectId, Map<String,String> token){
        Response response =  invokePostRequest(baseURl+"/api/v1.0/project/" + projectId +"/meeting",token);
        Assert.assertEquals(200, response.getStatusCode(), response.asPrettyString());
        logAndReport("Meeting Ended Successfully");
    }

    public void getPrice(String projectId){
        Response response = invokeGetRequest(baseURl +"/price/"+projectId);
        Assert.assertEquals(200,response.getStatusCode(),response.asPrettyString());
        System.out.println(response.asPrettyString());
    }

    public  void convertRoomSetting(String roomSettingType, String pID, String fId, String rId,Map<String,String> token) {
        if (roomSettingType!= null&&roomSettingType.equalsIgnoreCase("Gola")) {
            Response  response = invokePutRequestWithFile(baseURl + "/api/v1.0/project/" + pID + "/floors/" + fId + "/rooms/" + rId
                    + "/settings/golaAndLoft","payloads/Gola.json",token);
            Assert.assertEquals(response.statusCode(), 200, response.asPrettyString());
            log.info("Converting Room setting to Gola");
        } else if (roomSettingType!= null && roomSettingType.equalsIgnoreCase("gp")) {
          Response  response = invokePutRequestWithFile(baseURl + "/api/v1.0/project/" + pID + "/floors/" + fId + "/rooms/" + rId
                    + "/settings/golaAndLoft","payloads/G_Profile.json",token);
            log.info("Converting Room setting to GP");
            Assert.assertEquals(response.statusCode(), 200);

        }
    }

    public Response getUnitEntryData(String projectId, String floorId, String roomId, String zoneId, Map<String,String> token){
        Response res =  invokeGetRequest(baseURl+"/api/v1.0/project/"+projectId+"/floors/"+floorId+"/rooms/"+roomId+"/unitEntries/"+zoneId, token);
        Assert.assertEquals(res.getStatusCode(),200,res.asPrettyString());
        return res;

    }

    public Response getRoomSummary(String projectId, Map<String,String>token){
        return invokeGetRequest("https://sc-backend-production.homelane.com/sse/message/showPrice/162c2c19-5e50-41dc-b5b6-e553eb1cecf1",token);
    }

    public Response getItems(String prjectId, Map<String,String> token){
        return invokeGetRequest(baseURl+"/api/v1.0/project/report/getItems/"+prjectId,token);
    }

    public Response getPricingError(String projectId, Map<String,String>token){
        return invokeGetRequest(baseURl+"/api/v1.0/project/getPricingError/"+projectId, token);
    }

    public Response getDetailPriceData(String projectId, Map<String, String> token) {
        String url = baseURl + "/d2m/validation/detailed-price/" + projectId;
        Response response = invokeGetRequest(url, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("Fetched getDetailPriceData Response for project:{} Successfully." , projectId);
        return response;
    }

    public Response updateShutterType(String projectID, String floorID, String currentRoomId, String currentZoneId, String moduleObjectId, String shutterDesignsName, String finishCategory, String shutterSubTypeId, Map<String, String> token) {
        String url = baseURl + "/api/v1.0/project/" + projectID + "/floor/" + floorID + "/room/" + currentRoomId + "/zone/" + currentZoneId + "/updateShutterType";

        String payload =  new ModuleServicePayloadHelper().updateShutterTypePayload(moduleObjectId, finishCategory, shutterDesignsName, shutterSubTypeId);

        Response response = invokePutRequest(url, payload, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("Shutter type updated successfully for module object ID: {}", moduleObjectId);
        return response;
    }

    public Response computeLatestProjectPrice(String projectId, Map<String, String> token) {
        String url = baseURl+"/price/computeLatest/project/" + projectId;
        Response response = invokePostRequest(url, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("computeLatestProjectPrice called successfully");
        return response;
    }

    public Response updateToLatestProjectPrice(String projectId, Map<String, String> token) {
        String url = baseURl+"/price/updateToLatest/project/" + projectId;
        Response response = invokePostRequest(url, token);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        logAndReport("updateToLatestProjectPrice called successfully");
        return response;
    }
}
