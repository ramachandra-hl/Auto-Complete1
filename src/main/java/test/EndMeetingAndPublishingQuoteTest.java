package test;

import configurator.TestConfig;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import services.DesignerFeedBackService;
import services.RoasterService;

import java.util.List;
import java.util.Map;

public class EndMeetingAndPublishingQuoteTest extends TestConfig {
    DesignerFeedBackService designerFeedBackService = new DesignerFeedBackService();
    RoasterService roasterService = new RoasterService();

    @Test
    public void meetingEndAndPublishingQuote() throws InterruptedException {
        Thread.sleep(5000);
        designerFeedBackService.updateDesignerFeedBack(user_id, dp_id);
        scBackendService.endMeeting(projectID, token);
        roasterService.sendFirstMeetingEmail(customerId);
        Response response = scBackendService.getPricingError(projectID, token);
        List<Map<String, Object>> rooms =  response.jsonPath().getList("rooms");
        if (rooms == null){
        roasterService.sendCreateVersionRequest(projectID, token);
        } else {
       log.error("Pricing errors found in rooms: " + rooms);
       log.error("Cannot proceed to create version due to pricing errors. please resolve the pricing Error");
        }
    }
}
