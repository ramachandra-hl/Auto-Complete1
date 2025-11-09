package services;

    import static configurator.BaseClass.testData;
    import static utils.Utilities.*;

    public class ControllerConfigService {

        public static String customerType;
        public static String environment;
        public static void initializeTestData() {
            testData.clear();
            testData.put("environment", environment);
            loadStandardConfig(customerType, testData);
            loadCustomerConfig(customerType, testData);
        }

    }
