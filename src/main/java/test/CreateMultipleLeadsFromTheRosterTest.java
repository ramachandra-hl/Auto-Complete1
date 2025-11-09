package test;

import configurator.BaseClass;
import models.ModuleInputData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import org.testng.asserts.SoftAssert;
import services.RoasterService;
import utils.DataProviderUtil;
import utils.PropertiesReader;
import utils.Utilities;

import java.util.HashMap;
import java.util.Map;

import static configurator.ApiService.refreshTestData;
import static utils.Utilities.*;
import static utils.Utilities.updateProperties;

public class CreateMultipleLeadsFromTheRosterTest extends BaseClass {
    private static final Logger logger = LogManager.getLogger(CreateMultipleLeadsFromTheRosterTest.class);
    RoasterService roasterService ;
    PropertiesReader propertiesReader;
    ModuleInputData inputData;
    Utilities utilities = new Utilities();
    String filepath;
    int currentIndex;
    String environment;
    String roasterBaseUrl;
    int leadCount;
    String customerId;

    @BeforeMethod
    public void checkingValidInput() {
        refreshTestData();
        propertiesReader = new PropertiesReader(testData);
        environment = propertiesReader.getEnvironment();
        roasterBaseUrl = propertiesReader.getRoasterBaseUrl();
         roasterService = new RoasterService();
         leadCount = propertiesReader.getLeadCount();
        if (!leadInputValidation()) {
            throw new RuntimeException("Lead input validation failed. Aborting test execution.");
        }
        filepath = generateLeadCreationCSV(environment, customerType);

    }

    @Test(enabled = false, dataProvider = "LeadDataProvider", dataProviderClass = DataProviderUtil.class)
    public void testDpIdReport(Map<String, String> roomDetailsMap) {
        String dpEmail = roomDetailsMap.getOrDefault("designerEmail", "").trim().toLowerCase();
        Assert.assertFalse(dpEmail.isEmpty(), "designerEmail is missing in input data");
        String dpId = roasterService.getDpId(dpEmail);
        String[] resultData = {dpEmail, dpId};
        utilities.appendTestResults(resultData, filepath);
        logger.info("✅ DpID report entry added: dpEmail={}, dpId={}", dpEmail, dpId);
    }

    @Test(enabled = true)
    public void testCreateBulkLeads() {
        if (propertiesReader.getIsSingleInputMode().equalsIgnoreCase("true")) {
            throw new SkipException("Skipping bulk lead creation because isSingleInputMode is true.");
        }
        runLeadCreation(leadCount, customerType, environment, currentIndex);
    }

    @Test(enabled = false, dataProvider = "LeadDataProvider", dataProviderClass = DataProviderUtil.class)
    public void testCreateSingleLeadWithInput(Map<String, String> roomDetailsMap) {
        int cuIndex = 0;
        inputData = new ModuleInputData(roomDetailsMap);
        Assert.assertNotNull(inputData.getDesignerEmail());
        if (inputData.getSerialNo() != null && !inputData.getSerialNo().isEmpty()) {
            cuIndex = Integer.parseInt(inputData.getSerialNo());
        }else {
            cuIndex= currentIndex;
        }
        System.out.println("Current Index: " + currentIndex);
        runLeadCreation(customerType, environment,
                inputData.getDesignerId(), inputData.getDesignerEmail().trim().toLowerCase(), inputData.getDesignerName(),
                cuIndex);
    }


    @Test(enabled = false, dataProvider = "customerIdProvider", dataProviderClass = DataProviderUtil.class)
    public void proToLite(Map<String, String> roomDetailsMap) {
        String scType = "0";
        inputData = new ModuleInputData(roomDetailsMap);
        customerId = inputData.getCustomerId();
        roasterService.changeCustomerScType(customerId, scType);
    }


    private void runLeadCreation(int leadCount, String customerType, String environment, int currentIndex) {
        String dpId = "745564";
        String dpEmail = "testorgstructuredp1@homelane.com";
        String dpName = "Test DP User";
        runLeadCreationBulk(leadCount, customerType, dpId, dpEmail, dpName, currentIndex);
    }

    private void runLeadCreation(String customerType, String environment,
                                 String dpId, String dpEmail, String dpName,
                                 int currentIndex) {
        runLeadCreationAsInputData(customerType, dpId, dpEmail, dpName, currentIndex);
    }


    private void runLeadCreationBulk(int leadCount, String customerType,
                                     String dpId, String dpEmail, String dpName,
                                     int workingIndex) {


        int successfulLeads = 0;

        logger.info("🚀 Starting to create {} lead(s) from index {} onward.", leadCount, workingIndex);

        while (successfulLeads < leadCount) {
            boolean success = false;

            for (int attempt = 1; attempt <= 3 && !success; attempt++) {
                try {
                    logger.info("Creating  lead at index {} .", workingIndex);
                    Map<String, String> projectDetails = roasterService.createProject(dpEmail, workingIndex);

                    if (projectDetails != null &&
                            projectDetails.containsKey("customerId") &&
                            projectDetails.containsKey("projectID")) {

                        String[] resultData = {
                                String.valueOf(workingIndex),
                                customerType,
                                projectDetails.get("customerId"),
                                projectDetails.get("projectID"),
                                dpName,
                                dpEmail,
                                projectDetails.get("dpId")
                        };

                        utilities.appendTestResults(resultData, filepath);

                        logger.info(
                                "✅ Lead #{} created successfully on attempt {}:\n📦 Details: {}\n🔗 RosterUrl: {}",
                                workingIndex,
                                attempt,
                                projectDetails,
                                roasterBaseUrl + "/v2/customerDetails/" + projectDetails.get("userId")
                        );
                        workingIndex++;
                        successfulLeads++;
                        success = true;
                    } else {
                        logger.warn("⚠️ Lead #{} creation failed on attempt {}: Incomplete project details.", workingIndex, attempt);
                    }
                } catch (Throwable t) {
                    logger.error("🛑 Lead #{} failed on attempt {}: {}", workingIndex, attempt, t.getMessage());
                }

                if (!success && attempt < 3) {
                    logger.info("🔁 Retrying lead #{} (attempt {}/{})", ++workingIndex, attempt + 1, 3);
                }
            }

            if (!success) {
                logger.error("❌ Lead #{} failed after 3 attempts. Skipping to next index.", workingIndex);
                throw new SkipException("Skipping test due to lead creation failure.");
            }
            currentIndex = workingIndex;
        }
    }


    private void runLeadCreationAsInputData(String customerType,
                                            String dpId, String dpEmail, String dpName,
                                            int workingIndex) {

        SoftAssert softAssert = new SoftAssert();
        int successfulLeads = 0;

        logger.info("🚀 Processing data provider entry {} to create {} lead.", workingIndex, 1);
        while (successfulLeads < 1) {
            boolean leadCreated = false;
            String errorMsg = "";

            try {
                logger.info("Creating lead at index {}.", workingIndex);
                Map<String, String> projectDetails = roasterService.createProject(dpEmail, workingIndex);

                if (projectDetails != null &&
                        projectDetails.containsKey("customerId") &&
                        projectDetails.containsKey("projectID")) {

                    String[] resultData = {
                            String.valueOf(workingIndex),
                            projectDetails.get("customerId"),
                            projectDetails.get("projectID"),
                            dpEmail,
                            projectDetails.get("dpId")
                    };

                    utilities.appendTestResults(resultData, filepath);

                    logger.info(
                            "✅ Lead #{} created successfully:\n📦 Details: {}\n🔗 RosterUrl: {}",
                            workingIndex,
                            projectDetails,
                            roasterBaseUrl + "/v2/customerDetails/" + projectDetails.get("userId")
                    );

                    successfulLeads++;
                    leadCreated = true;
                } else {
                    errorMsg = "Incomplete project details";
                    logger.warn("⚠️ Lead #{} creation failed: {}", workingIndex, errorMsg);
                }

            } catch (Throwable t) {
                errorMsg = "Exception: " + t.getMessage();
                logger.error("🛑 Lead #{} creation threw exception: {}", workingIndex, errorMsg);
            }

            if (!leadCreated) {
                String failMessage = String.format(
                        "❌ Lead creation failed at index: %d | Designer Name: %s | Designer Email: %s | DP ID: %s | CustomerType: %s | Reason: %s",
                        workingIndex, dpName, dpEmail, dpId, customerType, errorMsg
                );
                softAssert.fail(failMessage);

                String[] resultData = {
                        String.valueOf(workingIndex),
                        "null",        // customerId
                        "null",        // projectID
                        dpEmail,
                        "null",        // dpId
                };
                utilities.appendTestResults(resultData, filepath);
            }

            workingIndex++;
            currentIndex = workingIndex;

            if ("true".equalsIgnoreCase(propertiesReader.getIsSingleInputMode())) break;
        }

        softAssert.assertAll();
    }

    public String generateLeadCreationCSV(String environment, String customerType) {
        String[] headers = {"S.no", "customer_Id", "projectID", "designerEmail", "designerId"};
        String subFolder = "reports/LeadCreationReports/" + environment + "/" + formatCurrentDate("📅dd-MM-yyyy↘️");
        String fileName = customerType + "-Leads_LastAt_" + propertiesReader.getLastProcessedLeadIndex() + ".csv";
        String filePath = subFolder + "/" + fileName;
        utilities.createCSVReport(headers, filePath);
        return filePath;
    }


    private boolean leadInputValidation() {
        boolean isValid = true;
        int lastProcessedLeadIndex = propertiesReader.getLastProcessedLeadIndex();

        if ( isBeforeToday(propertiesReader.getFiledDate())) {
            lastProcessedLeadIndex = 1;
        }
        if ((leadCount + lastProcessedLeadIndex) >= 100) {
            logger.error("❌ TotalLeadsToCreate should be less than 100. Please provide different mobileStarting prefix and lastProcessedLeadIndex reset to 0 Found: {}", leadCount + lastProcessedLeadIndex);
            isValid = false;
        }

        try {
            int prefix = Integer.parseInt(propertiesReader.getMobilePrefix());
            if (prefix < 59 || prefix > 99) {
                logger.error("❌ Invalid mobile number prefix. It must between 60-99. Found: {}", propertiesReader.getMobilePrefix());
                isValid = false;
            }
        } catch (NumberFormatException e) {
            logger.error("❌ Mobile number prefix is not a valid number: {}", propertiesReader.getMobilePrefix());
            isValid = false;
        }
        currentIndex = lastProcessedLeadIndex + 1;
        return isValid;
    }

    @AfterClass
    public void afterClassTasks() {
        Map<String, String> updateUserProperty = new HashMap<>();

        logger.info("✅ Finished creating {} lead(s). Final attempted index: {}", leadCount, currentIndex - 1);
        updateUserProperty.put("lastProcessedLeadIndex", String.valueOf(--currentIndex));
        updateUserProperty.put("leadScriptRunDate", formatCurrentDate("dd-MM-yyyy"));
        if (userConfigPath != null && !userConfigPath.isEmpty()
                && "True".equalsIgnoreCase(isUserConfigPassed)) {
            updateProperties(userConfigPath, updateUserProperty);
        }
        updateProperties("configuration/userConfigurations.properties", updateUserProperty);

        String finalFileName = leadCount + "_" + customerType + "-Leads_LastAt_" + currentIndex + formatCurrentDate(" ⏰ hh.mm.a") + ".csv";
        utilities.renamingLeadReportFile(filepath, finalFileName);
    }
}
