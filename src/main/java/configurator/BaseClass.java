package configurator;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;
import reporter.ExtentFactory;
import reporter.ExtentManager;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static utils.PropertiesReader.*;
import static utils.Utilities.*;

public class BaseClass  {

    public static final Logger log = LogManager.getLogger(BaseClass.class);
    public static ExtentTest extentTest;
    public static HashMap<String, Object> testData = new HashMap<>();

    public static String customerType;
    static String userSelectedCityProperty;
    public static String cityCode;
    public static String orderProfileType;
    public static String priceVersion;
    public static String hdsPriceVersion;
    public static String projectCreationDate;
    public static String isUserConfigPassed;
    public static String userConfigPath;
    public static Map<String,String> existingRoomRegistry=new HashMap<>();
    public static String catalogCity;

    public static void initializeTestData() {
        testData.clear();
        // Load user configuration properties
        addToTestDataFromProperties("userConfigurations.properties", testData);
        System.out.println(testData);
        // Read customerType and city
        customerType = (String) testData.get("customerType");
        userSelectedCityProperty = String.valueOf(testData.get("userSelectedCityProperty"));
        if (customerType == null || userSelectedCityProperty == null) {
            log.error("❌ customerType or userSelectedCityProperty is missing in userConfigurations.properties");
            return;
        }

        // Load standard, customer-specific, and city configurations
        loadStandardConfig(customerType, testData);
        loadCustomerConfig(customerType, testData);
        loadCityConfig(userSelectedCityProperty, testData);

        log.info("✅ Test data initialized. CustomerType: {}, CityCode: {}", customerType, userSelectedCityProperty);
    }

//    private static void loadUserConfigurationsTakenFromCLI() {
//        try {
//            userConfigPath = System.getProperty("userConfigPath");
//            isUserConfigPassed = System.getProperty("useCustomConfig");
//            if ("true".equalsIgnoreCase(isUserConfigPassed) && userConfigPath != null && !userConfigPath.trim().isEmpty()) {
//                log.info("Loading user config from CSV: {}", userConfigPath);
//
//                // Convert to .properties and store temporarily
//                String tempPropertiesFile = "Configurations/tempUserConfigurations.properties";
//                csvToProperties(userConfigPath, tempPropertiesFile);
//
//                // Load into testData
//                addToTestDataFromProperties(tempPropertiesFile, testData);
//            } else {
//                log.warn("No configFile path provided.");
//
//                String defaultFile = "userConfigurations.properties";
//                addToTestDataFromProperties(defaultFile, testData);
//            }
//        } catch (Exception e) {
//            log.error("Error loading user configurations: ", e);
//        }
//    }
//
//
//    private static void loadCustomerConfig(String customerType) {
//        String configFile = switch (customerType) {
//            case "HL", "LUXE" -> "Configurations/HL_config.yml";
//            case "HFN" -> "Configurations/HFN_config.yml";
//            default -> null;
//        };
//        if (configFile != null) {
//            addToTestDataFromYml(configFile, testData);
//        } else {
//            log.warn("Unknown customer type: {}", customerType);
//        }
//    }
//
//    private static void loadCityConfig(String citycode) {
//        String cityFile = switch (citycode) {
//            case "1" -> "Configurations/CitiesData/homelane/1-Bengaluru.properties";
//            case "2" -> "Configurations/CitiesData/homelane/2-chennai.properties";
//            case "3" -> "Configurations/CitiesData/homelane/3-Mumbai.properties";
//            case "4" -> "Configurations/CitiesData/homelane/4-Kolkata.properties";
//            case "5" -> "Configurations/CitiesData/homelane/5-Kochi.properties";
//            case "6" -> "Configurations/CitiesData/homelane/6-visakhapatnam.properties";
//            case "7" -> "Configurations/CitiesData/homelane/7-Delhi.properties";
//            case "8" -> "Configurations/CitiesData/homelane/8-Hydrabad.properties";
//            case "9" -> "Configurations/CitiesData/homelane/9-Gurgaon.properties";
//            case "10" -> "Configurations/CitiesData/homelane/10-Pune.properties";
//            case "11" -> "Configurations/CitiesData/homelane/11-thane.properties";
//            case "12" -> "Configurations/CitiesData/homelane/12-Lucknow.properties";
//            case "13" -> "Configurations/CitiesData/homelane/13-Mangalore.properties";
//            case "14" -> "Configurations/CitiesData/homelane/14-Mysore.properties";
//            case "15" -> "Configurations/CitiesData/homelane/15-Patna.properties";
//            case "16" -> "Configurations/CitiesData/franchise/16-Thirupathi.properties";
//            case "17" -> "Configurations/CitiesData/franchise/17-Guwahati.properties";
//            case "18" -> "Configurations/CitiesData/franchise/18-Vijayawada.properties";
//            case "19" -> "Configurations/CitiesData/franchise/19-Nizamabad.properties";
//            case "20" -> "Configurations/CitiesData/franchise/20-Shivamogga.properties";
//            case "21" -> "Configurations/CitiesData/franchise/21-Siliguri.properties";
//            case "22" -> "Configurations/CitiesData/franchise/22-Trivendrum.properties";
//            case "23" -> "Configurations/CitiesData/franchise/23-warangal.properties";
//            case "24" -> "Configurations/CitiesData/franchise/24-Karimnagar.properties";
//            case "25" -> "Configurations/CitiesData/franchise/25-Jamshedpur.properties";
//            case "26" -> "Configurations/CitiesData/homelane/26-Noida.properties";
//            case "27" -> "Configurations/CitiesData/homelane/27-Coimbatore.properties";
//            case "28" -> "Configurations/CitiesData/homelane/28-bhubaneswar.properties";
//            case "29" -> "Configurations/CitiesData/homelane/29-salem.properties";
//            case "30" -> "Configurations/CitiesData/homelane/30-Nagpur.properties";
//            case "31" -> "Configurations/CitiesData/homelane/31-Surat.properties";
//            case "32" -> "Configurations/CitiesData/homelane/32-Ranchi.properties";
//            case "33" -> "Configurations/CitiesData/homelane/33-Ghaziabad.properties";
//            case "34" -> "Configurations/CitiesData/homelane/34-Nashik.properties";
//            case "35" -> "Configurations/CitiesData/homelane/35-Madurai.Properties";
//            case "36" -> "Configurations/CitiesData/homelane/36-Tiruchirappalli .properties";
//            case "37" -> "Configurations/CitiesData/homelane/37-jaipur.properties";
//            case "38" -> "Configurations/CitiesData/homelane/38-ahmedabad.properties";
//            default -> null;
//        };
//        if (cityFile != null) {
//            addToTestDataFromProperties(cityFile, testData);
//        } else {
//            log.warn("Unknown city code: {}", citycode);
//        }
//    }
//
//    public static void loadCityConfigByName(String cityName) {
//        String cityFile = switch (cityName.toLowerCase()) {
//            case "bengaluru" -> "Configurations/CitiesData/homelane/1-Bengaluru.properties";
//            case "chennai" -> "Configurations/CitiesData/homelane/2-chennai.properties";
//            case "mumbai" -> "Configurations/CitiesData/homelane/3-Mumbai.properties";
//            case "kolkata" -> "Configurations/CitiesData/homelane/4-Kolkata.properties";
//            case "kochi" -> "Configurations/CitiesData/homelane/5-Kochi.properties";
//            case "visakhapatnam" -> "Configurations/CitiesData/homelane/6-visakhapatnam.properties";
//            case "delhi" -> "Configurations/CitiesData/homelane/7-Delhi.properties";
//            case "hydrabad" -> "Configurations/CitiesData/homelane/8-Hydrabad.properties";
//            case "gurgaon" -> "Configurations/CitiesData/homelane/9-Gurgaon.properties";
//            case "pune" -> "Configurations/CitiesData/homelane/10-Pune.properties";
//            case "thane" -> "Configurations/CitiesData/homelane/11-thane.properties";
//            case "lucknow" -> "Configurations/CitiesData/homelane/12-Lucknow.properties";
//            case "mangalore" -> "Configurations/CitiesData/homelane/13-Mangalore.properties";
//            case "mysore" -> "Configurations/CitiesData/homelane/14-Mysore.properties";
//            case "patna" -> "Configurations/CitiesData/homelane/15-Patna.properties";
//            case "thirupathi" -> "Configurations/CitiesData/franchise/16-Thirupathi.properties";
//            case "guwahati" -> "Configurations/CitiesData/franchise/17-Guwahati.properties";
//            case "vijayawada" -> "Configurations/CitiesData/franchise/18-Vijayawada.properties";
//            case "nizamabad" -> "Configurations/CitiesData/franchise/19-Nizamabad.properties";
//            case "shivamogga" -> "Configurations/CitiesData/franchise/20-Shivamogga.properties";
//            case "siliguri" -> "Configurations/CitiesData/franchise/21-Siliguri.properties";
//            case "trivendrum" -> "Configurations/CitiesData/franchise/22-Trivendrum.properties";
//            case "warangal" -> "Configurations/CitiesData/franchise/23-warangal.properties";
//            case "karimnagar" -> "Configurations/CitiesData/franchise/24-Karimnagar.properties";
//            case "jamshedpur" -> "Configurations/CitiesData/franchise/25-Jamshedpur.properties";
//            case "noida" -> "Configurations/CitiesData/homelane/26-Noida.properties";
//            case "coimbatore" -> "Configurations/CitiesData/homelane/27-Coimbatore.properties";
//            case "bhubaneswar" -> "Configurations/CitiesData/homelane/28-bhubaneswar.properties";
//            case "salem" -> "Configurations/CitiesData/homelane/29-salem.properties";
//            case "nagpur" -> "Configurations/CitiesData/homelane/30-Nagpur.properties";
//            case "surat" -> "Configurations/CitiesData/homelane/31-Surat.properties";
//            case "ranchi" -> "Configurations/CitiesData/homelane/32-Ranchi.properties";
//            case "ghaziabad" -> "Configurations/CitiesData/homelane/33-Ghaziabad.properties";
//            case "nashik" -> "Configurations/CitiesData/homelane/34-Nashik.properties";
//            case "madurai" -> "Configurations/CitiesData/homelane/35-Madurai.Properties";
//            case "tiruchirappalli" -> "Configurations/CitiesData/homelane/36-Tiruchirappalli .properties";
//            case "jaipur" -> "Configurations/CitiesData/homelane/37-jaipur.properties";
//            case "ahmedabad" -> "Configurations/CitiesData/homelane/38-ahmedabad.properties";
//            default -> null;
//        };
//
//        if (cityFile != null) {
//            addToTestDataFromProperties(cityFile, testData);
//        } else {
//            log.warn("Unknown city name: {}", cityName);
//        }
//    }

    public void ReporterCommon(String message, Status status) {
        if (message == null) {
            message = "No message provided";
        }
        log.info("Reporting: {}", message);
        ExtentFactory.getInstance().getExtentTest().log(status, message);
    }

    public void ReporterPass(String str) {
        ReporterCommon(str, Status.PASS);
    }

    public void ReporterInfo(String str) {
        if (str == null) {
            str = "No message provided";
        }
        ReporterCommon(str, Status.PASS);
    }

    public void ReporterFail(String str) {
        ReporterCommon(str, Status.FAIL);
    }

    public static void logAndReport(Status status, String message) {
        String callingMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
        String logMessage = "[" + callingMethod + "] " + message;

        switch (status) {
            case PASS:
                log.info(logMessage);
                break;
            case FAIL:
                log.error(logMessage);
                break;
            case SKIP:
                log.warn(logMessage);
                break;
            default:
                log.debug(logMessage);
                break;
        }
        if (ExtentFactory.getInstance().getExtentTest() != null) {
            ExtentFactory.getInstance().getExtentTest().log(status, message);
        }
    }

    public static void logAndReport(String message) {
        logAndReport(Status.INFO, message);
    }


    public static void logAndReport(String message, Object... args) {
        if (message == null) {
            message = "No message provided";
        }
        String formattedMessage;
        try {
            formattedMessage = String.format(message.replace("{}", "%s"), args);
        } catch (Exception e) {
            formattedMessage = message + " " + Arrays.toString(args);
        }

        // Include calling method name for better traceability
        String callingMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
        String logMessage = "[" + callingMethod + "] " + formattedMessage;

        log.info(logMessage);

        if (ExtentFactory.getInstance().getExtentTest() != null) {
            ExtentFactory.getInstance().getExtentTest().log(Status.INFO, formattedMessage);
        }
    }


    @BeforeSuite
    public void beforeSuite() {
        initializeGlobalVariables();
        GlobalVariables.extentReports = ExtentManager.setupExtentReport();
    }

    @BeforeMethod
    public void beforeMethod(ITestContext context, Method method) {
        String methodName = method.getName();
        log.info("{} Test Method started", methodName);
        extentTest = GlobalVariables.extentReports.createTest(methodName);
        ExtentFactory.getInstance().setExtentTest(extentTest);
    }

    @AfterMethod
    public void afterMethod(ITestResult result, Method method) {
        int status = result.getStatus();
        String testName = method.getName();
        if (status == ITestResult.SUCCESS) {
            ExtentFactory.getInstance().getExtentTest().pass("Test Method " + testName + " Passed");
        } else if (status == ITestResult.FAILURE) {
            ExtentFactory.getInstance().getExtentTest().fail("Test Method " + testName + " Failed");
            ExtentFactory.getInstance().getExtentTest().log(Status.FAIL, result.getThrowable());
            //  System.exit(1);
        } else if (status == ITestResult.SKIP) {
            ExtentFactory.getInstance().getExtentTest().skip("Test Method " + testName + " Skipped");
        }
    }

    @AfterSuite
    public void afterSuite() {
//        System.out.println(
//                "\n" +
//                        "🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇\n" +
//                        "       ✅ TESTS COMPLETED ✅\n" +
//                        "     🎉 SUITE FINISHED 🎉\n" +
//                        "📅 Time  ➡️  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a")) + "\n" +
//                        "🌐 URL   ➡️  " + ScProUrl + "/v/floorplanner/" + projectID + "\n" +
//                        "🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇🎇\n"
//        );
        GlobalVariables.extentReports.setSystemInfo("Device Type", GlobalVariables.deviceType);
        GlobalVariables.extentReports.setSystemInfo("Executed By", GlobalVariables.executedBy);
        GlobalVariables.extentReports.setSystemInfo("Environment", "PRODUCTION");

        if (!GlobalVariables.deviceType.equalsIgnoreCase("desktop")) {
            GlobalVariables.extentReports.setSystemInfo("Device Name",
                    GlobalVariables.configProperties.getProperty("deviceName"));
        }
        GlobalVariables.extentReports.flush();
    }

    private void initializeGlobalVariables() {
        GlobalVariables.configProperties = new Properties();
        GlobalVariables.baseDir = System.getProperty("user.dir");
        System.out.println(System.getProperty("user.dir"));
        GlobalVariables.currentExtentReportDir = GlobalVariables.configProperties
                .getProperty("currentExtentReportDir");

    }

    public  void captureOutput(ITestContext context, String key, String message) {
        List<String> output = (List<String>) context.getAttribute(key);
        if (output == null) {
            output = new ArrayList<>();
        }
        output.add(message);
        context.setAttribute(key, output);
    }

    public void captureFailure(ITestContext context, String key, String message) {
        List<String> output = (List<String>) context.getAttribute(key);
        if (output == null) output = new ArrayList<>();
        output.add(message);
        context.setAttribute(key, output);
    }

}
