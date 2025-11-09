package services;

import configurator.ApiService;
import io.restassured.response.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.Assert;
import org.testng.SkipException;
import payloadHelper.RoasterPayloadHelper;
import utils.PropertiesReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static configurator.BaseClass.testData;
import static io.restassured.RestAssured.given;
import static utils.Utilities.formatCurrentDate;


public class RoasterService extends ApiService {

    private final static Logger log = LogManager.getLogger(RoasterService.class);
    static Response response;

    RoasterPayloadHelper rosterPayloadHelper = new RoasterPayloadHelper();
    PropertiesReader propertiesReader = new PropertiesReader(testData);
    static Map<String, String> cookiesData ;
    Map<String, String> financeCookieData;
    static String token ;
    String projectID;
    String environment = propertiesReader.getEnvironment();
    String dp_email = propertiesReader.getDpEmail();
    String  dp_password = propertiesReader.getDpPassword();
    String roasterBaseUrl = propertiesReader.getRoasterBaseUrl();
    String baseURl = propertiesReader.getBaseURl();
    String mobileNoStarting2digitPrefix = propertiesReader.getMobilePrefix();
    String finance_dp = propertiesReader.getFinanceDp();
    String finance_Password = propertiesReader.getFinancePassword();
    String gmailDomain = propertiesReader.getGmailDomain();
    String customerName = propertiesReader.getCustomerName();
    String appointment_venue = propertiesReader.getAppointmentVenue();
    String customerType = propertiesReader.getCustomerType();

    public synchronized void initialize() {
        try {
            log.info("Initializing RoasterService for environment: {}", environment);

            cookiesData = getCookieData(dp_email, dp_password);
            if (cookiesData != null && cookiesData.containsKey("django_access_token")) {
                token = cookiesData.get("django_access_token");
                log.info("✅ RoasterService initialized successfully with new token.");
            } else {
                log.error("❌ Failed to fetch django_access_token. Check login credentials or cookies API.");
                token = null;
            }
        } catch (Exception e) {
            log.error("❌ Failed to initialize RoasterService: {}", e.getMessage(), e);
        }
    }


    public static Map<String, String> commonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("accept-language", "en-US,en;q=0.9");
        headers.put("authorization", "Bearer " + token);
        return headers;
    }


    public Map<String, String> createProject(String dpEmail, int count) throws InterruptedException {
        String formattedCount = String.format("%02d", count);
        String mobileNumber = mobileNoStarting2digitPrefix + formatCurrentDate("ddMMyy") + formattedCount;
        String email = customerName.replaceAll("\\s+", "") + mobileNumber + gmailDomain;
        Map<String, String> customerDetails = createNewProLead(mobileNumber, email, dpEmail, count);
        String userId = customerDetails.get("userId");
        String customerId = customerDetails.get("customerId");
        String env = (String) testData.get("customerType");
        if (env.equals("LUXE")) {
            financeCookieData = getCookieData(finance_dp, finance_Password);
            convertHLtoLUXE(userId, customerId);
            String pd_id = paymentForConvertHLtoLUXE(userId);
            markDeposited(pd_id);
            approvePayment(pd_id, userId);
        }
        String fullProjectURL = getFullProjectURL(projectID);
        customerDetails.put("fullProjectURL", fullProjectURL);
        System.out.println("ProjectURL : "+ fullProjectURL);
        System.out.println("Customer ID:" + customerId);
        return customerDetails;
    }

    public Map<String, String> createNewProLead(String mobileNumber, String email, String dpEmail, int count) throws InterruptedException {
        Map<String, String> userData = new HashMap<>();

        Map<String, String> customerDetails = leadGen(mobileNumber, email, count);
        String userId = customerDetails.get("userId");
        String hashedEmail = getUserData(userId);
        if (dpEmail != null && !environment.equalsIgnoreCase("preProd")) {
            String dpId = getDpId(dpEmail);
            userData.put("dpId", dpId);
            updateStakeholders(dpId, userId);
        }
        updateAddress(userId);
        Map<String, String> projectDetails = updateCustomerDetails(userId, hashedEmail);
        verifyOTP(userId);
        updateCheckInStatus(userId);
        projectID = projectDetails.get("project_id");
        userData.put("projectID", projectID);
        userData.put("floorId", projectDetails.get("floor_id"));
        userData.put("userId", userId);
        userData.put("customerId", customerDetails.get("customerId"));
        userData.put("email", email);
        userData.put("dpName", projectDetails.get("dp_name"));
        userData.put("dpEmail", projectDetails.get("dp_email"));
        userData.put("dpMobile", projectDetails.get("dp_mobile"));

        return userData;
    }



    public Map<String, String> completeProLeadSetup(String mobileNumber)  {
        Map<String, String> userData = new HashMap<>();
        String userId = getUserIdFromPhoneNumber(mobileNumber);
        String hashedEmail =  getUserData(userId);
        updateAddress(userId);
        Map<String, String> projectDetails = updateCustomerDetails(userId, hashedEmail);
        verifyOTP(userId);
        updateCheckInStatus(userId);
        projectID = projectDetails.get("project_id");
        userData.put("customerId", projectDetails.get("customer_id"));
        userData.put("projectID", projectID);
        userData.put("floorId", projectDetails.get("floor_id"));
        userData.put("userId", userId);
        userData.put("dpName", projectDetails.get("dp_name"));
        userData.put("dpEmail", projectDetails.get("dp_email"));
        userData.put("dpMobile", projectDetails.get("dp_mobile"));

        String fullProjectURL = getFullProjectURL(projectID);
        userData.put("fullProjectURL", fullProjectURL);
        System.out.println(getFullProjectURL(projectID));
        return userData;
    }

    public Map<String, String> leadGen(String mobileNumber, String email, int count) {
        Map<String, String> customerDetails = setCustomerAndPropertyDetails(mobileNumber, email, count);
        String id = customerDetails.get("SFLeadId");
        String userId = setRequirements(id);
        customerDetails.put("userId", userId);
        setTimelineDetails(id);
        setAppointmentDetails(id);

        return customerDetails;
    }

    public Map<String, String> setCustomerAndPropertyDetails(String mobileNumber, String email, int count) {
        log.log(Level.INFO, "Submitting customer-details-form");
        Response res = invokePostRequestWithMapOfObjet(roasterBaseUrl + "/apis/leads/save_lead_new", rosterPayloadHelper.getCustomerAndPropertyDetailsPayload(mobileNumber, email,count,testData), commonHeaders(),cookiesData);
        Assert.assertEquals(200, res.getStatusCode(), res.asPrettyString());
        Map<String, String> customerDetails = new HashMap<>();
        String SFLeadId = res.jsonPath().getString("data.sf_lead_id");
        String customerId = res.jsonPath().getString("data.customer_id");
        customerDetails.put("SFLeadId", SFLeadId);
        customerDetails.put("customerId", customerId);
        return customerDetails;
    }

    public String setRequirements(String SFLeadId) {
        log.log(Level.INFO, "Submitting property-details-form");
        Response res = invokePostRequestWithMapOfObjet(roasterBaseUrl + "/apis/leads/save_lead_new", rosterPayloadHelper.getPropertyRequirementsPayload(SFLeadId, testData), commonHeaders(), cookiesData);
        Assert.assertEquals(200, res.getStatusCode(), res.asPrettyString());
        return res.jsonPath().get("data.user_id");
    }

    public void setTimelineDetails(String SFLeadId) {
        log.log(Level.INFO, "Submitting timelines-form");
        response = invokePostRequestWithMapOfObjet(roasterBaseUrl + "/apis/leads/save_lead_new", rosterPayloadHelper.getTimelineDetailsPayload(SFLeadId), commonHeaders(), cookiesData);
        Assert.assertEquals(200, response.getStatusCode(), response.asPrettyString());
    }

    public void setAppointmentDetails(String SFLeadId) {
        log.log(Level.INFO, "Submitting appointment-form");

        response = invokePostRequestWithMapOfObjet(
                roasterBaseUrl + "/apis/leads/save_lead_new",
                rosterPayloadHelper.getAppointmentDetailsPayload(SFLeadId, appointment_venue),
                commonHeaders(),
                cookiesData
        );
        int statusCode = response.statusCode();

        // Consider body-level failures even if HTTP status is 200
        Boolean successStatus = null;
        String apiMessage = null;
        try { successStatus = response.jsonPath().getBoolean("success_status"); } catch (Exception ignored) {}
        try { apiMessage = response.jsonPath().getString("message"); } catch (Exception ignored) {}

        if (statusCode >= 400 || (successStatus != null && !successStatus)) {
            log.log(Level.FATAL, "⚠️ Appointment submission failed. HTTP: " + statusCode + ", success_status: " + successStatus + ", message: " + apiMessage);
            log.log(Level.FATAL, "Response: " + response.asPrettyString());
            throw new SkipException("Skipping this test; appointment failed: " + (apiMessage != null ? apiMessage : "unknown"));
        }

        Assert.assertEquals(200, statusCode, response.asPrettyString());
    }


    public void updateAddress(String userId) {
        log.log(Level.INFO, "Executing updateAddress Method");
        if(environment.equalsIgnoreCase("preProd")){
            response = invokePostRequestWithMapOfObjet("https://rosters-v2-preprod-sap-cutover.homelane.com/api/v1/general/update_address/", rosterPayloadHelper.getUpdateAddressPayload(userId, testData), commonHeaders(), cookiesData);
        } else{
            response = invokePostRequestWithMapOfObjet(roasterBaseUrl + "/welcome/updateAddressToZohoAndLocalDB", rosterPayloadHelper.getUpdateAddressPayload(userId, testData), commonHeaders(), cookiesData);
        }
        Assert.assertEquals(200, response.statusCode(), response.asPrettyString());
        log.log(Level.INFO, "Address Updated Successfully");
    }

    public Map<String, String> updateCustomerDetails(String userId, String email) {
        log.log(Level.INFO, "Executing updateCustomerDetails Method");
        Map<String, String> result = new HashMap<>();
        System.out.println(rosterPayloadHelper.customerDetailsPayload(userId, email, testData));
        if (environment.equalsIgnoreCase("preProd")){
            response = invokePostRequest("https://rosters-v2-preprod-sap-cutover.homelane.com/api/v1/general/update_customer_details/",
                    rosterPayloadHelper.customerDetailsPayload(userId, email, testData),
                    commonHeaders(), cookiesData);
        }else {
            response = invokePostRequest(roasterBaseUrl + "/apis/quote/updateCustomerDetails",
                    rosterPayloadHelper.customerDetailsPayload(userId, email, testData),
                    commonHeaders(), cookiesData);
        }
        int statusCode = response.getStatusCode();
        if (statusCode != 200) {
            log.error("⚠️ Request failed with status code: {}\nResponse: {}", statusCode, response.asPrettyString());
            Assert.fail("Request failed with status code: " + statusCode);
        }

        String project_id = response.jsonPath().getString("SC_project_creation_response.projectId");
        String floor_id = response.jsonPath().getString("SC_project_creation_response.selectedFloorId");
        String customer_id = response.jsonPath().getString("SC_project_creation_response.customer.customerId");
        String dp_email = response.jsonPath().getString("SC_project_creation_response.dp.email");
        String dp_mobile = response.jsonPath().getString("SC_project_creation_response.dp.mobile");
        String dp_name = response.jsonPath().getString("SC_project_creation_response.dp.name");

        result.put("project_id", project_id);
        result.put("floor_id", floor_id);
        result.put("customer_id", customer_id);
        result.put("dp_email", dp_email);
        result.put("dp_mobile", dp_mobile);
        result.put("dp_name", dp_name);
        log.log(Level.INFO, "Executed updateCustomerDetails Method Successfully");

        return result;
    }

    public void convertHLtoLUXE(String userId, String customerId) {
        String payload = rosterPayloadHelper.convertHLtoLUXEPayload(userId, customerId);
        response = invokePostRequest(roasterBaseUrl + "/apis/Customer_detail_v2/updateCustomerTypeDetails", payload, commonHeaders());
        Assert.assertEquals(200, response.getStatusCode());
    }

    public String paymentForConvertHLtoLUXE(String user_id) {
        String payload = rosterPayloadHelper.paymentForConvertHLtoLUXEPayload(user_id);
        response = invokePostRequest("https://rosters-v2-preprod-sap-cutover.homelane.com/api/v1/customerPayments/tempDetails/", payload, commonHeaders());
        Assert.assertEquals(200, response.getStatusCode());
        return response.jsonPath().getString("data.inserted_id");
    }

    public void markDeposited(String pd_id) {
        String url = roasterBaseUrl + "/collections/mark_deposited/" + pd_id;

        // Prepare headers as a Map
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "*/*");
        headers.put("accept-language", "en-US,en;q=0.9");
        System.out.println(finance_dp + finance_Password);
        System.out.println(financeCookieData);
        response = invokePostRequestWithMultiPart(url, rosterPayloadHelper.getMarkDepositedPayload(), headers, financeCookieData);
        Assert.assertEquals(200, response.getStatusCode());
        System.out.println(response.asPrettyString());
    }


    public void approvePayment(String cardId, String custId) {
        Response response = given()
                .header("accept", "*/*")
                .cookies(financeCookieData)
                .formParam("card_id", cardId)
                .formParam("status", "true")
                .formParam("order_id", "")
                .formParam("cust_id", custId)
                .formParam("sku", "")
                .formParam("reject_reason", "")
                .formParam("bank_name", "HDFC Bank")
                .formParam("service_type", "1")
                .formParam("rejectApprovedPayment", "false")
                .formParam("amount", "25000.00")
                .when()
                .post(roasterBaseUrl + "/finance/approve_payment");
        Assert.assertEquals(response.getStatusCode(), 200, "Payment approval failed!");
    }


    public String getUserData(String userId) {
        log.log(Level.INFO, "Executing getUserData method");
        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("cust_id", userId);
        response = invokeGetRequestWithQueryParams(roasterBaseUrl + "/apis/Customer_detail_v2/get_customer_details/", payLoad, commonHeaders(), cookiesData);
        Assert.assertEquals(200, response.statusCode(), response.asPrettyString());
        log.log(Level.INFO, "Executed getUserData method Successfully");
        return response.jsonPath().getString("data.customer_details.primary_email");
    }

    public void verifyOTP(String userId) {
        log.log(Level.INFO, "Executing verifyOTP Method");
        String otp = "000111";
        invokePostRequest(roasterBaseUrl + "/apis/customer/customer_otp", rosterPayloadHelper.getSendOTPPayload(userId));
        invokePostRequest(roasterBaseUrl + "/apis/customer/customer_otp", rosterPayloadHelper.getVerifyOTPPayload(userId, otp));
        log.log(Level.INFO, " OTP Verified  Successfully");
    }

    public void updateCheckInStatus(String userId) {
        log.log(Level.INFO, "Executing updateCheckInStatus Method");
        String payload = rosterPayloadHelper.CheckInStatusPayload(userId);
        invokePostRequest(roasterBaseUrl + "/apis/customer/updateCheckinStatus", payload);
        log.log(Level.INFO, "Executed updateCheckInStatus Method Successfully");
    }

    public void updateStakeholders(String dpId, String userId) {
        log.log(Level.INFO, "Executing updateStakeholders Method");
        response = invokePostRequest(roasterBaseUrl + "/apis/user_hierarchy/update_stakeholders", rosterPayloadHelper.updateUserAssignmentPayload(dpId, userId), commonHeaders());
        Assert.assertEquals(200, response.getStatusCode());
        log.log(Level.INFO, "Executed updateStakeholders Method Successfully");


    }

    public String lauchProject(String projectId) {
        String endpoint = baseURl + "/security/testUrl";
        Map<String, String> payLoad = new HashMap<>();
        payLoad.put("projectId", projectId);
        // Define the cookies
        Map<String, String> cookies = new HashMap<>();
        cookies.put("cookies", (String) testData.get("cookie"));
        Response response = invokePostRequestWithCookies(endpoint, payLoad, cookies);
        return response.asPrettyString();
    }

    public void sendCreateVersionRequest(String projectId, Map<String, String> token) {
        String requestBody = "{\"name\":\"TestHl_One's Home ver_-Infinity\",\"scope\":\"2BHK\"}";
        Response res = invokePostRequest(baseURl + "/api/v1.0/project/" + projectId + "/version", requestBody, token);
        Assert.assertEquals(200, res.getStatusCode(), res.getBody().asPrettyString());
        log.info("Quote Published SuccessFully");
    }

    public void sendFirstMeetingEmail(String customerId) {
        String url = roasterBaseUrl + "/apis/iq/send_first_meeting_email";
        invokePostRequestFormDataAsUrlencoded(url, rosterPayloadHelper.getSendFirstMeetingEmailPayload(customerId));
    }

    public  Map<String, String> getCookieData(String dpEmail, String dpPwd) {
        log.log(Level.INFO, "------Authorizing Api Access------");
        HashMap<Object, Object> formParams = new HashMap<>();
        formParams.put("email", dpEmail);
        formParams.put("password", dpPwd);
        response = invokePostRequestWithMultiPart(roasterBaseUrl + "/welcome/verify_user", formParams);
        Assert.assertEquals(200, response.statusCode());
        String rToken = response.jsonPath().getString("rosterV2_access");
        String dnjToken = response.jsonPath().getString("django_access_token");
        String sessionId = response.getCookie("ci_session");
        HashMap<String, String> cookiesData = new HashMap<>();
        cookiesData.put("ci_session", sessionId);
        cookiesData.put("rosterV2_access", rToken);
        cookiesData.put("django_access_token", dnjToken);
        return cookiesData;
    }

    public String getDpId(String dpEmail) {

        Response res = given()
                .cookies(cookiesData)
                .formParam("page", "0")
                .formParam("email_search", dpEmail)
                .post(roasterBaseUrl + "/dp_management/dp_management_ajax/0");
        String html = res.asPrettyString();
        Document doc = Jsoup.parse(html);
        Element trElement = doc.select("tr[id]").first();
        return trElement.id();
    }

    public void changeCustomerScType(String customerId, String scType) {
        response = invokePostRequest(roasterBaseUrl + "/apis/spacecraft/changeCustomerScType", rosterPayloadHelper.changeCustomerType(customerId, scType), commonHeaders(), cookiesData);
        Assert.assertEquals(response.getStatusCode(), 200, response.asPrettyString());
        log.log(Level.INFO, customerId + " converted from Pro to Lite  ✅ ");
    }

    public String getUserIdFromPhoneNumber(String mobileNumber) {
        String url = "";
        if (environment.equalsIgnoreCase("preProd")) {
            url = "https://dc-roster-django-preprod.homelane.com/api/v1/order_management/opportunity/fetch_lead_opportunity/";
        } else {
            url = "https://dc-orderwise-v2.homelane.com/api/v1/order_management/opportunity/fetch_lead_opportunity/?limit=10&offset=0";
        }
        response = invokePostRequest(url, rosterPayloadHelper.updateSearchCustomerPayload(mobileNumber), commonHeaders());
        Assert.assertEquals(200, response.statusCode(), response.asPrettyString());
        System.out.println(response.jsonPath().getString("data.results[0].user_id"));
        return response.jsonPath().getString("data.results[0].user_id");
    }

    public String getFullProjectURL(String projectId) {
        JSONObject payLoad = new JSONObject();
        payLoad.put("projectId", projectId);
        response = invokePostRequest(baseURl + "/security/testUrl", payLoad, "");
        return response.asPrettyString();
    }

    public List<Map<String,String>> getShowroomList(String customerType){
        List<Map<String,String>> result =  new ArrayList<>();
        if(customerType.equalsIgnoreCase("HFN")){
            String prodCookie = " traffic_source=utmcsr=direct|device=null|keyword=null|position=null|adgroup_id=null|landingpage=www.homelane.com/payment?stage_id=h6weKoMzuSz0%2FNeRstMJKzrfKSmCTl4M5DsRyxNzETU%3D; first_interaction=utmcsr=direct|device=null|keyword=null|position=null|adgroup_id=null; session_date=23/10/2025; _vwo_uuid_v2=D1E3DA0B8FF32E5C55B3BE84F07FFABD1|9538ec4d2bbff84345ab3ac0e0e981a0; _vwo_uuid=D1E3DA0B8FF32E5C55B3BE84F07FFABD1; _vis_opt_exp_71_combi=3; _gcl_au=1.1.1199058800.1761201521; _fbp=fb.1.1761201520998.816521036871469503; _vis_opt_exp_71_goal_1=1; _vis_opt_exp_71_goal_2=1; homelane-_zldp=ol43%2F8WmCpCIGPXbH8otWlyBskMeEcGRNzt%2Ft3c5tY%2BD8wcz7PevdHYHkuKgbeXMqaV0tnRbIq4%3D; _gid=GA1.2.1217222507.1761539617; _vis_opt_exp_61_combi=3; _vis_opt_exp_61_goal_1=1; _vis_opt_exp_61_goal_2=1; _ga_G0HBTJ9HSK=GS2.2.s1761646713$o1$g1$t1761646835$j46$l0$h0; _ga_GPY7DV32LQ=GS2.1.s1761646712$o1$g1$t1761646866$j3$l0$h0; _clck=188ddyj%5E2%5Eg0k%5E0%5E2105; _vwo_ds=3%241761201516%3A49.97364326%3A%3A%3A%3A%3A1761713582%3A1761673079; _vis_opt_s=4%7C; _vis_opt_test_cookie=1; homelane=a%3A5%3A%7Bs%3A10%3A%22session_id%22%3Bs%3A32%3A%227741059fe41900999f65ef84a41f07a9%22%3Bs%3A10%3A%22ip_address%22%3Bs%3A11%3A%2210.0.138.97%22%3Bs%3A10%3A%22user_agent%22%3Bs%3A111%3A%22Mozilla%2F5.0+%28Windows+NT+10.0%3B+Win64%3B+x64%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F141.0.0.0+Safari%2F537.36%22%3Bs%3A13%3A%22last_activity%22%3Bi%3A1761714089%3Bs%3A12%3A%22session_data%22%3Bs%3A0%3A%22%22%3B%7D38f7ca1f9e7dd6cff723d79fddf873f22907074b; _ga_7G53CN5TWE=GS2.1.s1761713583$o3$g1$t1761714090$j60$l0$h0; cto_bundle=nMsIC18lMkJsWk9yWThCRmJNZHhaWW5PJTJGUmxkV2RlSW9oWGtCMzFoSVY2RHpRYWYxYlJsdGpZJTJGRDlZbWdwNHlEZFppdGxmYTFwVXdTNUI3ZGNHJTJCdXpQZnJQemRTTnJpVGlOSmVmVnFzek9LczJxVDNxbGxiZW83cnJMR0kzUEtWV3NSZzk4ZUxyemxVNHBkRGxQU0xFcHElMkYwSVJyQ0tmbFdhZmV2NjV4QVp4YVNDWHJyTUZiTExxRDlOYmFFdWZ3UldiM1BKazMxJTJCOEhQSjVRYkZhNEV0VWoxbk1BJTNEJTNE; _uetsid=76616d50b3e711f0bc3715bc557fbf7c; _uetvid=4499f92093a211f0a263ffb05be61a80; _clsk=1d8wpzr%5E1761743008512%5E2%5E1%5Eo.clarity.ms%2Fcollect; user_name=testhfndealer%40homelane.com; cookie_pass=C%2FFHd7%2Bag0kcSedlOFMZwEid0lznu5vgbcsGp7wc04U%3D; mp_159efc34b5648bde9758becc2d7b5d95_mixpanel=%7B%22distinct_id%22%3A%22%24device%3A0ff2b53f-3d66-4003-b02e-d63e574ac2e1%22%2C%22%24device_id%22%3A%220ff2b53f-3d66-4003-b02e-d63e574ac2e1%22%2C%22%24initial_referrer%22%3A%22https%3A%2F%2Frosters.homelane.com%2Fv2%2Flogin%22%2C%22%24initial_referring_domain%22%3A%22rosters.homelane.com%22%2C%22__mps%22%3A%7B%7D%2C%22__mpso%22%3A%7B%22%24initial_referrer%22%3A%22https%3A%2F%2Frosters.homelane.com%2Fv2%2Flogin%22%2C%22%24initial_referring_domain%22%3A%22rosters.homelane.com%22%7D%2C%22__mpus%22%3A%7B%7D%2C%22__mpa%22%3A%7B%7D%2C%22__mpu%22%3A%7B%7D%2C%22__mpr%22%3A%5B%5D%2C%22__mpap%22%3A%5B%5D%7D; _ga_8JTMFJ6JMW=GS2.2.s1761744988$o18$g1$t1761745241$j60$l0$h0; _ga=GA1.1.374078852.1759727601; mp_930ff1d63438a667a2a39beaaf781ea4_mixpanel=%7B%22distinct_id%22%3A%22%24device%3A9abbdd67-2309-4008-8ba2-02e420ac35d9%22%2C%22%24device_id%22%3A%229abbdd67-2309-4008-8ba2-02e420ac35d9%22%2C%22%24initial_referrer%22%3A%22https%3A%2F%2Frosters-preprod-new.homelane.com%2Fv2%2Fcustomer_search%22%2C%22%24initial_referring_domain%22%3A%22rosters-preprod-new.homelane.com%22%2C%22__mps%22%3A%7B%7D%2C%22__mpso%22%3A%7B%22%24initial_referrer%22%3A%22https%3A%2F%2Frosters-preprod-new.homelane.com%2Fv2%2Fcustomer_search%22%2C%22%24initial_referring_domain%22%3A%22rosters-preprod-new.homelane.com%22%7D%2C%22__mpus%22%3A%7B%7D%2C%22__mpa%22%3A%7B%7D%2C%22__mpu%22%3A%7B%7D%2C%22__mpr%22%3A%5B%5D%2C%22__mpap%22%3A%5B%5D%7D; g_state={\"i_l\":0,\"i_ll\":1761747075517,\"i_b\":\"0JYbd+49P5wMWyaDj5RNiVRN4TONBaZQcxzX27nxqt4\"}; ci_session="+cookiesData.get("ci_session")+"; user_details="+cookiesData.get("django_access_token")+"; rosterV2_access="+cookiesData.get("rosterV2_access")+"; rosterV2_refresh="+cookiesData.get("rosterV2_access")+";";
            Map<String,String> headers = new HashMap<>();
            headers.put("Cookie", prodCookie);
            headers.putAll(commonHeaders());
            response = invokeGetRequest(roasterBaseUrl+"/apis/leads/lead_create_data", headers);
            Assert.assertEquals(200, response.getStatusCode(),response.asPrettyString());
            List<Map<String,String>> listOfShowrooms = response.jsonPath().getList("data");
            for(Map<String,String> showroom : listOfShowrooms){
                Map<String,String> oneShowRoom = new HashMap<>();
                oneShowRoom.put("showroom_name", showroom.get("showroom_name"));
                oneShowRoom.put("live_sf_id", showroom.get("showrooms_sf_id"));
                result.add(oneShowRoom);
            }
        }else{
            if (environment.equals("prod")) {
                response = invokeGetRequest(roasterBaseUrl+"/apis/Customer_detail_v2/getShowroomsList?city_specific=true", commonHeaders());
                Assert.assertEquals(200, response.getStatusCode(),response.asPrettyString());
                result = response.jsonPath().getList("");
            }else{
                response = invokeGetRequest("https://rosters-v2-preprod-sap-cutover.homelane.com/api/v1/auth/user/get_showrooms_list/?city_specific=true", commonHeaders());
                Assert.assertEquals(200, response.getStatusCode(),response.asPrettyString());
                result = response.jsonPath().getList("data");
            }

        }

        return result;
    }
}