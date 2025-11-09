package payloadHelper;
import utils.PropertiesReader;
import utils.UpdateJsons;
import utils.Utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static configurator.BaseClass.testData;
import static utils.Utilities.*;

public class RoasterPayloadHelper {
    UpdateJsons updateJsons = new UpdateJsons();
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    Map<String, Object> updateData = new HashMap<>();
    String filePath;
    String customerType = propertiesReader.getCustomerType();
    String environment = propertiesReader.getEnvironment();
    String appointment_venue = propertiesReader.getAppointmentVenue();
//    public static String projectName=PropertiesReader.projectName != null ? PropertiesReader.projectName : "";


    public Map<String, Object> getCustomerAndPropertyDetailsPayload(String mobileNumber,String email, int count, Map<String, Object> testData) {
        Map<String, Object> body = new HashMap<>();
        body.put("from_name", "customer-details-form");
//        if(projectName!= null && !projectName.isEmpty() && !projectName.equalsIgnoreCase("null")) {
//            body.put("name", projectName);
//        } else {
//            body.put("name", customerGmailStartingPrefix+convertDateToStringFormat() +convert(count));
//        }
        body.put("name", customerNameFormatter(propertiesReader.getCustomerName(), count,environment,customerType));
        body.put("email", email);
        body.put("country_code","91");
        body.put("mobile", mobileNumber);
        body.put("city", testData.get("city"));
        body.put("primary_language", "English");
        body.put("secondary_language", "Hindi");
        body.put("whatsapp_opt_in", "0");
        body.put("channel_partner_id", testData.get("channel_partner_id"));
        body.put("is_showroom_order", "0");
        body.put("lead_type", "Walk-in");
        body.put("latitude", testData.get("latitude"));
        body.put("longitude", testData.get("longitude"));
        body.put("property_city", testData.get("property_city"));
        body.put("country", testData.get("country"));
        body.put("searched_address", testData.get("searched_address"));
        body.put("property_name", testData.get("property_name"));
        body.put("map_url", testData.get("map_url"));
        body.put("lead_id", testData.get("lead_id"));
        body.put("placeId", testData.get("placeId"));
        body.put("locality", testData.get("locality"));
        body.put("state", testData.get("state"));
        body.put("postal_code", testData.get("postal_code"));
        body.put("property_type", "Apartment");
        body.put("property_conf", "1 BHK");
        body.put("possession_month", Utilities.getTomorrowDate());
        body.put("property_status", "New");
        if (customerType.equalsIgnoreCase("HFN")){
            body.put("hfn_showroom",testData.get("hfn_showroom"));
        }else {
            body.put("iron_man_id", testData.get("iron_man_id"));
        }
        body.put("email_id", email);
        return body;
    }

    public Map<String, Object> getPropertyRequirementsPayload(String SFLeadId, Map<String, Object> testData) {
        Map<String, Object> body = new HashMap<>();
        body.put("from_name", "requirements-form");
        body.put("lead_id", SFLeadId);
        body.put("kitchen", "Yes");
        body.put("wardrobes", "0");
        body.put("modular_storage", "No");
        body.put("lose_furniture", "No");
        body.put("hds", "0");
        body.put("budget_range", "1.5L - 4L");
        body.put("postal_code", testData.get("pincode"));
        return body;
    }

    public  Map<String, Object> getTimelineDetailsPayload(String SFLeadId) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("from_name", "timelines-form");
        formData.put("lead_id", SFLeadId);
        formData.put("move_in_date", Utilities.getDateTwoMonthsInTheFuture());
        return formData;
    }

    public Map<String, Object> getAppointmentDetailsPayload(String SFLeadId, String appointmentVenue) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("from_name", "appointments-form");
        if (!customerType.equalsIgnoreCase("HFN")){
            formData.put("appointment_venue", appointmentVenue);
        }
        formData.put("lead_id", SFLeadId);
        formData.put("appointment_type", "Virtual Meeting");
        formData.put("appointment_date_and_time", Utilities.getTomorrowDate());

        return formData;
    }

    public  Map<String, Object> getUpdateAddressPayload(String userId, Map<String, Object> testData) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userID", userId);
        dataMap.put("logged_in_user_id", propertiesReader.getLoggedInUserId());
        dataMap.put("customer_address", testData.get("customer_address"));
        dataMap.put("google_address", testData.get("customer_address"));
        dataMap.put("lattitude", testData.get("latitude"));
        dataMap.put("longitude", testData.get("longitude"));
        dataMap.put("address", testData.get("customer_address"));
        dataMap.put("name", testData.get("project_name"));
        dataMap.put("project_unique_id", Utilities.generateRandomString(15));
        dataMap.put("location_type", "Site Visit");
        dataMap.put("city_id", testData.get("city"));
        dataMap.put("ins_city_id", testData.get("city"));
        dataMap.put("ins_city", testData.get("property_city"));
        dataMap.put("user_ins_city", testData.get("property_city"));
        dataMap.put("pincode2", testData.get("pincode"));
        if (customerType.equalsIgnoreCase("HFN")){
            dataMap.put("insShowroom", testData.get("hfn_showroom"));

        }else {
            dataMap.put("insShowroom", appointment_venue);
        }
        dataMap.put("project_name", testData.get("project_name"));
        dataMap.put("flat_house_no", 11);
        dataMap.put("property_type", "Apartment");
        dataMap.put("installStateId","1");
        dataMap.put("del_plant_id", "40");
        return dataMap;
    }


    public String customerDetailsPayload(String userId, String email, Map<String, Object> testData) {

        updateData.put("user_id", userId);
        updateData.put("email", email);
        updateData.put("dp_email", propertiesReader.getDpEmail());
        updateData.put("city", testData.get("installation_city"));
        updateData.put("address",testData.get("customer_address"));
        filePath = "payloads/lunchScpro/updateCustomerDetails.json";
        return  updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String convertHLtoLUXEPayload(String userId, String customerId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("user_id", userId);
        updateData.put("custType", "Luxe");
        updateData.put("customer_id", customerId);
        updateData.put("isProCust", "1");

        String filePath = "payloads/lunchScpro/convertHLtoLUXE.json"; // Assuming this JSON exists with placeholders
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }


    public String paymentForConvertHLtoLUXEPayload(String userId) {
        updateData.put("user_id", userId);
        updateData.put("type", "Cash");

        Map<String, Object> updateDataMap = new HashMap<>();
        updateDataMap.put("payment_id", "121");
        updateDataMap.put("bank_name", "");
        updateDataMap.put("amount", 25000);
        updateDataMap.put("gateway_txn_id", "");
        updateDataMap.put("receipt_date", "");
        updateDataMap.put("offline_bank_name", "");
        updateDataMap.put("service_type", 1);
        updateDataMap.put("payment_type", "offline");

        updateData.put("update_data", updateDataMap);

        filePath = "payloads/lunchScpro/paymentForConvertHLtoLUXE.json";
        return updateJsons.updatePayloadFromMap(filePath, updateData);
    }


    public String getVerifyOTPPayload(String userId, String otp) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("cust_id", userId);
        otpData.put("action", "verify");
        otpData.put("otp", otp);

        String filePath = "payloads/lunchScpro/verifyOtpPayload.json";
        return updateJsons.updatePayloadFromMap(filePath, otpData);
    }


    public Map<String, Object> getMarkDepositedPayload() {
        Map<String, Object> formData = new HashMap<>();
        formData.put("deposit_date", Utilities.getTodayDate());
        formData.put("ref_number", "123456789");
        formData.put("service_type", "1");
        File file = new File("Configuration/PaymentSlips/HomeLane_Luxe_Ack_Slip_773_isLuxe=1.pdf");
        formData.put("file_upload", file);
        return formData;
    }

    public String getSendOTPPayload(String userId) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("cust_id", userId);
        otpData.put("action", "send");

        String filePath = "payloads/lunchScpro/sendOtpPayload.json";
        return updateJsons.updatePayloadFromMap(filePath, otpData);
    }


    public String CheckInStatusPayload(String userId) {
        Map<String, Object> checkInData = new HashMap<>();
        checkInData.put("time", "2025-03-11T10:49:00.525Z");
        checkInData.put("user_id", userId);
        checkInData.put("type", "auto");
        checkInData.put("meeting_type", "showroom");
        checkInData.put("logged_in_id", propertiesReader.getLoggedInUserId());

        String filePath = "payloads/lunchScpro/updateCheckinStatus.json";
        return updateJsons.updatePayloadFromMap(filePath, checkInData);
    }

    public String updateUserAssignmentPayload(String DPId, String userId) {

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", DPId);
        user1.put("roleName", "designer_1");

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 745564);
        user2.put("roleName", "designer_2");

        List<Map<String, Object>> usersList = new ArrayList<>();
        usersList.add(user1);
        usersList.add(user2);

        Map<String, Object> payload = new HashMap<>();
        payload.put("customerID", userId);
        payload.put("loggedInUserID", 745564);
        payload.put("users", usersList);

        String filePath = "payloads/lunchScpro/updateStakeHolder.json";
        return updateJsons.updatePayloadFromMap(filePath, payload);
    }



    public Map<String, Object> getSendFirstMeetingEmailPayload(String customerId) {
        Map<String, Object> formParams = new HashMap<>();
        formParams.put("customerID", customerId);
        formParams.put("additional_note", "NA");
        formParams.put("template_name", "first-meeting-nps_rebrand");
        return formParams;
    }

    public String changeCustomerType(String customerId,String scType){
        updateData.put("scType", scType);
        updateData.put("customerIDs", customerId);
        updateData.put("scOption", false);
        updateData.put("reason", "Updated by testopsprod@homelane.com");

        filePath = "payloads/lunchScpro/changeCustomerScType.json";
        return  updateJsons.updatePayloadFromMap(filePath, updateData);
    }

    public String updateSearchCustomerPayload(String mobileNumber) {
        updateData.put("customer_name", mobileNumber);
        filePath = "payloads/lunchScpro/searchCustomer.json";
        return  updateJsons.updatePayloadFromMap(filePath, updateData);
    }
}

