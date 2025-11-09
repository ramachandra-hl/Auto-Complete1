package services;

import io.restassured.response.Response;
import org.testng.Assert;
import payloadHelper.DesignerFeedBackPayloadHelper;
import utils.PropertiesReader;

import static configurator.ApiService.*;
import static configurator.BaseClass.logAndReport;
import static configurator.BaseClass.testData;

public class DesignerFeedBackService {
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    String roasterBaseUrl = propertiesReader.getRoasterBaseUrl();
    DesignerFeedBackPayloadHelper designerFeedBackPayloadHelper = new DesignerFeedBackPayloadHelper();

    public void updateFeedBackSet1(String user_id, String dp_id) {
        logAndReport("Updating Feedback Set 1 for user_id: {}, dp_id: {}", user_id, dp_id);
        String payload = designerFeedBackPayloadHelper.getFeedbackPayloadSet1(user_id, dp_id);
        Response response = invokePostRequest(roasterBaseUrl + "/apis/feedback/set_feedback", payload);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
    }

    public void updateFeedBackSet2(String user_id, String dp_id) {
        logAndReport("Updating Feedback Set 2 for user_id: {}, dp_id: {}", user_id, dp_id);
        String payload = designerFeedBackPayloadHelper.getFeedbackPayloadSet2(user_id, dp_id);
        Response response = invokePostRequest(roasterBaseUrl + "/apis/feedback/set_feedback", payload);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
    }

    public void updateFeedBackSet3(String user_id, String dp_id) {
        logAndReport("Updating Feedback Set 3 for user_id: {}, dp_id: {}", user_id, dp_id);
        String payload = designerFeedBackPayloadHelper.getFeedbackPayloadSet3(user_id, dp_id);
        Response response = invokePostRequest(roasterBaseUrl + "/apis/feedback/set_feedback", payload);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
    }

    public void updateFeedbackStatus(String user_id, String dp_id) {
        logAndReport("Checking Feedback Status for user_id: {}, dp_id: {}", user_id, dp_id);
        String payload = designerFeedBackPayloadHelper.getFeedbackStatusPayload(user_id, dp_id);
        Response response = invokePostRequest(roasterBaseUrl + "/apis/feedback/is_scvm_feedback_done", payload);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
    }

    public void updateDesignerFeedBack(String user_id,String dp_id){

        updateFeedBackSet1(user_id,dp_id);
        updateFeedBackSet2(user_id,dp_id);
        updateFeedBackSet3(user_id,dp_id);
        updateFeedbackStatus(user_id,dp_id);
    }
}
