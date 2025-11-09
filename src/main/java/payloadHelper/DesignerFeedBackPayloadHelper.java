package payloadHelper;

import utils.UpdateJsons;
import java.util.HashMap;
import java.util.Map;

public class DesignerFeedBackPayloadHelper {
    UpdateJsons updateJsons = new UpdateJsons();
    Map<String, Object> updateData = new HashMap<>();
    String filePath;
    public  String getFeedbackPayloadSet1(String userId, String dp_id) {
        updateData.put("user_id", userId);
        updateData.put("dp_id", dp_id);
        filePath = "payloads/DesignerFeedBack/feedbackSetOne.json";
     return    updateJsons.updatePayloadFromMap(filePath, updateData);
    }
    public  String getFeedbackPayloadSet2(String userId, String dp_id) {
        updateData.put("user_id", userId);
        updateData.put("dp_id", dp_id);
        filePath = "payloads/DesignerFeedBack/feedbackSetTwo.json";
        return    updateJsons.updatePayloadFromMap(filePath, updateData);
    }
    public  String getFeedbackPayloadSet3(String userId, String dp_id) {
        updateData.put("user_id", userId);
        updateData.put("dp_id", dp_id);
        filePath = "payloads/DesignerFeedBack/feedbackSetThree.json";
        return    updateJsons.updatePayloadFromMap(filePath, updateData);
    }
    public  String getFeedbackStatusPayload(String userId, String dp_id) {
        updateData.put("user_id", userId);
        updateData.put("dp_id", dp_id);
        filePath = "payloads/DesignerFeedBack/feedbackStatus.json";
        return    updateJsons.updatePayloadFromMap(filePath, updateData);
    }

}
