package utils;

import configurator.BaseClass;
import java.util.HashMap;
import java.util.Map;

public class PropertiesReader extends BaseClass {

    // Core data maps
    private Map<String, Object> testData;
    private Map<String, Object> environments;
    private Map<String, Object> login_Credential;

    // Environment + Config fields
    private String environment;
    private String testType;
    private String customerType;
    private String customerName;

    private String baseURl;
    private String ScProUrl;
    private String synapseUrl;
    private String hdsSynapseUrl;
    private String roasterBaseUrl;
    private String rosterCookies;

    private String logged_in_user_id;
    private String appointment_venue;
    private String dp_email;
    private String dp_password;
    private String finance_dp;
    private String finance_Password;
    private String sf_url;
    private String sf_login_email;
    private String sf_login_password;
    private String design_User;

    // Requirement toggles
    private String isCarcass;
    private String isShutter;
    private String isInternals;
    private String isHandleRequired;
    private String isAccessories;
    private String isQPModules;

    // Test data fields
    private int leadCount;
    private String customerId;
    private String projectID;
    private String floorID;
    private String token;
    private String browser;
    private String TwoddRequired;
    private int modulesInRoom;
    private String emailSubject;
    private String emailContent;
    private String toEmails;
    private String ccEmails;
    private String senderEmail;
    private String gmailPassword;
    private String mobileNoStarting2digitPrefix;
    private String projectName;
    private String customerGmailStartingPrefix;
    private String gmailDomain;
    private int lastProcessedLeadIndex;
    private String isSingleInputMode;
    private String filedDate;
    private String isInFillDataRequired;

    private boolean initialized = false; // ✅ Lazy load flag

    // ================================
    // 🔹 Constructors
    // ================================
    public PropertiesReader(Map<String, Object> testData) {
        this.testData = testData;
    }

    public PropertiesReader() {
        // ✅ Auto-load from BaseClass if available
        if (BaseClass.testData != null && !BaseClass.testData.isEmpty()) {
            this.testData = BaseClass.testData;
        } else {
            System.err.println("⚠️ Warning: BaseClass.testData is empty — call refreshTestData() before using PropertiesReader.");
            this.testData = new HashMap<>();
        }
    }

    // ================================
    // 🔹 Lazy Loader
    // ================================
    private synchronized void ensureLoaded() {
        if (!initialized) {
            if (testData == null || testData.isEmpty()) {
                throw new IllegalStateException("❌ testData is empty — did you forget to call refreshTestData()?");
            }
            load();
            initialized = true;
        }
    }

    // ================================
    // 🔹 Load All Properties (One Time)
    // ================================
    private void load() {
        // Environment setup
        this.environment = (String) testData.getOrDefault("environment", "");
        this.testType = (String) testData.getOrDefault("testType", "");
        this.customerType = (String) testData.getOrDefault("customerType", "");
        this.customerName = (String) testData.getOrDefault("customerName", "");

        // Test data
        this.leadCount = Integer.parseInt((String) testData.getOrDefault("totalLeadsToCreate", "1"));
        this.customerId = (String) testData.getOrDefault("customerId", "");
        this.projectID = (String) testData.getOrDefault("projectID", "");
        this.floorID = (String) testData.getOrDefault("floorID", "");
        this.token = (String) testData.getOrDefault("token", "");
        this.browser = (String) testData.getOrDefault("browser", "");
        this.TwoddRequired = (String) testData.getOrDefault("2DDRequired", "");
        this.modulesInRoom = Integer.parseInt((String) testData.getOrDefault("modulesInRoom", "0"));
        this.emailSubject = (String) testData.getOrDefault("emailSubject", "");
        this.emailContent = (String) testData.getOrDefault("emailContent", "");
        this.toEmails = (String) testData.getOrDefault("toEmails", "");
        this.ccEmails = (String) testData.getOrDefault("ccEmails", "");
        this.senderEmail = (String) testData.getOrDefault("senderEmail", "");
        this.gmailPassword = (String) testData.getOrDefault("gmail.app.password", "");
        this.mobileNoStarting2digitPrefix = (String) testData.getOrDefault("mobileNoStarting2digitPrefix", "");
        this.projectName = (String) testData.getOrDefault("projectName", "");
        this.customerGmailStartingPrefix = (String) testData.getOrDefault("customerGmailStartingPrefix", "");
        this.gmailDomain = (String) testData.getOrDefault("gmailDomain", "");
        this.lastProcessedLeadIndex = Integer.parseInt((String) testData.getOrDefault("lastProcessedLeadIndex", "0"));
        this.isSingleInputMode = (String) testData.getOrDefault("isSingleInputMode", "");
        this.filedDate = (String) testData.getOrDefault("leadScriptRunDate", "");
        this.isInFillDataRequired = (String) testData.getOrDefault("isInFillDataRequired", "");
        this.isQPModules = (String) testData.getOrDefault("isQPModules", "");

        // Submaps
        this.environments = (Map<String, Object>) testData.get("projectEnvironments");
        this.login_Credential = (Map<String, Object>) testData.get("login_Credential");

        initializeEnvironment(environment);
        initializeRequirements(testType);
    }

    // ================================
    // 🔹 Environment Initializer
    // ================================
    private void initializeEnvironment(String env) {
        Map<String, String> envData = getSubMap(environments, env);
        Map<String, String> loginData = getSubMap(login_Credential, env);

        if (envData == null || loginData == null) {
            throw new IllegalStateException("Environment or login credentials not found for: " + env);
        }

        ScProUrl = envData.getOrDefault("ScProUrl", "");
        baseURl = envData.getOrDefault("BaseURL", "");
        synapseUrl = envData.getOrDefault("SynapseUrl", "");
        hdsSynapseUrl = envData.getOrDefault("HdsSynapseUrl", "");
        roasterBaseUrl = envData.getOrDefault("RoasterBaseUrl", "");
        rosterCookies = envData.getOrDefault("cookies", "");

        finance_dp = loginData.getOrDefault("finance_email", "");
        finance_Password = loginData.getOrDefault("finance_password", "");
        logged_in_user_id = loginData.getOrDefault("logged_in_user_id", "");
        dp_email = loginData.getOrDefault("dp_email", "");
        dp_password = loginData.getOrDefault("dp_password", "");

        if (customerType.equalsIgnoreCase("HL")) {
            appointment_venue = String.valueOf(
                    testData.get("showroomId") != null
                            ? testData.get("showroomId")
                            : testData.getOrDefault(env + "Appointment_venue", "")
            );
        } else if (customerType.equalsIgnoreCase("DC")) {
            sf_url = loginData.getOrDefault("SFurl", "");
            sf_login_email = loginData.getOrDefault("SFUserName", "");
            sf_login_password = loginData.getOrDefault("SFPassword", "");
            design_User = loginData.getOrDefault("design_User", "");
            appointment_venue = loginData.getOrDefault("DCAppointment_venue", "");
        } else {
            appointment_venue = String.valueOf(
                    testData.get("showroomId") != null
                            ? testData.get("showroomId")
                            : testData.getOrDefault(env + "Hfn_showroom", "")
            );
        }
    }

    // ================================
    // 🔹 Requirements Initializer
    // ================================
    private void initializeRequirements(String testType) {
        if (testType.equalsIgnoreCase("sanityTest")) {
            isCarcass = "true";
            isShutter = "true";
            isAccessories = "true";
            isHandleRequired = "true";
            isInternals = "true";
        } else {
            isCarcass = (String) testData.getOrDefault("isCarcassRequired", "");
            isShutter = (String) testData.getOrDefault("isShutterRequired", "");
            isInternals = (String) testData.getOrDefault("isInternalRequired", "");
            isHandleRequired = (String) testData.getOrDefault("isHandleRequired", "");
            isAccessories = (String) testData.getOrDefault("isAccessoriesRequired", "");
        }
    }

    // ================================
    // 🔹 Helper
    // ================================
    private Map<String, String> getSubMap(Map<String, Object> parentMap, String key) {
        if (parentMap == null || key == null) return new HashMap<>();
        for (String mapKey : parentMap.keySet()) {
            if (mapKey.equalsIgnoreCase(key)) {
                Object value = parentMap.get(mapKey);
                if (value instanceof Map) {
                    return (Map<String, String>) value;
                }
            }
        }
        return new HashMap<>();
    }

    // ================================
    // 🔹 Getters (Lazy)
    // ================================
    public String getEnvironment() { ensureLoaded(); return environment; }
    public String getCustomerType() { ensureLoaded(); return customerType; }
    public String getCustomerName() { ensureLoaded(); return customerName; }
    public String getBaseURl() { ensureLoaded(); return baseURl; }
    public String getScProUrl() { ensureLoaded(); return ScProUrl; }
    public String getSynapseUrl() { ensureLoaded(); return synapseUrl; }
    public String getHdsSynapseUrl() { ensureLoaded(); return hdsSynapseUrl; }
    public String getRoasterBaseUrl() { ensureLoaded(); return roasterBaseUrl; }
    public String getRosterCookies() { ensureLoaded(); return rosterCookies; }

    public String getLoggedInUserId() { ensureLoaded(); return logged_in_user_id; }
    public String getAppointmentVenue() { ensureLoaded(); return appointment_venue; }
    public String getDpEmail() { ensureLoaded(); return dp_email; }
    public String getDpPassword() { ensureLoaded(); return dp_password; }
    public String getFinanceDp() { ensureLoaded(); return finance_dp; }
    public String getFinancePassword() { ensureLoaded(); return finance_Password; }
    public String getSfUrl() { ensureLoaded(); return sf_url; }
    public String getSfLoginEmail() { ensureLoaded(); return sf_login_email; }
    public String getSfLoginPassword() { ensureLoaded(); return sf_login_password; }
    public String getDesignUser() { ensureLoaded(); return design_User; }

    public String getIsCarcass() { ensureLoaded(); return isCarcass; }
    public String getIsShutter() { ensureLoaded(); return isShutter; }
    public String getIsInternals() { ensureLoaded(); return isInternals; }
    public String getIsHandleRequired() { ensureLoaded(); return isHandleRequired; }
    public String getIsAccessories() { ensureLoaded(); return isAccessories; }
    public String getIsQPModules() { ensureLoaded(); return isQPModules; }

    public int getLeadCount() { ensureLoaded(); return leadCount; }
    public String getCustomerId() { ensureLoaded(); return customerId; }
    public String getProjectID() { ensureLoaded(); return projectID; }
    public String getFloorID() { ensureLoaded(); return floorID; }
    public String getToken() { ensureLoaded(); return token; }
    public String getBrowser() { ensureLoaded(); return browser; }
    public String getTwoddRequired() { ensureLoaded(); return TwoddRequired; }
    public int getModulesInRoom() { ensureLoaded(); return modulesInRoom; }
    public String getEmailSubject() { ensureLoaded(); return emailSubject; }
    public String getEmailContent() { ensureLoaded(); return emailContent; }
    public String getToEmails() { ensureLoaded(); return toEmails; }
    public String getCcEmails() { ensureLoaded(); return ccEmails; }
    public String getSenderEmail() { ensureLoaded(); return senderEmail; }
    public String getGmailPassword() { ensureLoaded(); return gmailPassword; }
    public String getMobilePrefix() { ensureLoaded(); return mobileNoStarting2digitPrefix; }
    public String getProjectName() { ensureLoaded(); return projectName; }
    public String getCustomerGmailStartingPrefix() { ensureLoaded(); return customerGmailStartingPrefix; }
    public String getGmailDomain() { ensureLoaded(); return gmailDomain; }
    public int getLastProcessedLeadIndex() { ensureLoaded(); return lastProcessedLeadIndex; }
    public String getIsSingleInputMode() { ensureLoaded(); return isSingleInputMode; }
    public String getFiledDate() { ensureLoaded(); return filedDate; }
    public String getIsInFillDataRequired() { ensureLoaded(); return isInFillDataRequired; }
}
