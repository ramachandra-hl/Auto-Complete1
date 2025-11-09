package utils;

import org.testng.annotations.DataProvider;
import java.io.IOException;
import java.util.*;

public class DataProviderUtil {
    PropertiesReader propertiesReader = new PropertiesReader();

    public static String isUserCsvPassed = System.getProperty("userCsvPassed", "false");

    private Object[][] convertToObjectArray(List<Map<String, String>> details) {
        return details.stream()
                .map(map -> new Object[]{map})
                .toArray(Object[][]::new);
    }

    // ***** COMMON METHOD: Get runtime data first, else CSV *****
    private Object[][] fetchDataWithRuntimeSupport(String defaultCsvPath) throws IOException {
        List<Map<String, String>> runtimeData = RuntimeDataUtil.getRuntimeDataIfAvailable();

        if (runtimeData != null) {
            System.out.println("✅ Using Runtime Test Data instead of CSV");
            return convertToObjectArray(runtimeData);
        }

        System.out.println("📁 Loading Test Data from File: " + defaultCsvPath);
        ReadDataFromCSV csvReader = new ReadDataFromCSV(defaultCsvPath);
        List<Map<String, String>> details = csvReader.readCSVAsListOfMaps();
        return convertToObjectArray(details);
    }

    @DataProvider(name = "KitchenModuleDataProvider")
    public Object[][] kitchenModuleInputData() throws IOException {
        return fetchDataWithRuntimeSupport("CSVinputs/kitchenInputData.csv");
    }

    @DataProvider(name = "MiscModuleDataProvider")
    public Object[][] miscModuleInputData() throws IOException {
        String filePath = System.getProperty("inputFilePath");

//        if (!Boolean.parseBoolean(isUserCsvPassed)) {
//            filePath = propertiesReader.get.equalsIgnoreCase("sanityTest")
//                    ? "CSVinputs/sanityInputs/skuInput.csv"
//                    : "CSVinputs/testData.csv";
//        }
        return fetchDataWithRuntimeSupport(filePath);
    }

    @DataProvider(name = "HdsAndTdsDataProvider")
    public Object[][] HdsAndTdsInputData() throws IOException {
        String filePath = System.getProperty("inputFilePath");

        if (!Boolean.parseBoolean(isUserCsvPassed)) {
            filePath = propertiesReader.getIsSingleInputMode().equalsIgnoreCase("sanityTest")
                    ? "CSVinputs/sanityInputs/hdsInput.csv"
                    : "CSVinputs/sanityData - HDSInputData.csv";
        }
        return fetchDataWithRuntimeSupport(filePath);
    }

    @DataProvider(name = "LeadDataProvider")
    public Object[][] getLeadData() throws IOException {
        if ("false".equalsIgnoreCase(propertiesReader.getIsSingleInputMode())) {
            return new Object[0][];
        }
        return fetchDataWithRuntimeSupport("CSVinputs/LeadInputData.csv");
    }

    @DataProvider(name = "customerIdProvider")
    public Object[][] getCustomerData() throws IOException {
        return fetchDataWithRuntimeSupport("CSVinputs/customerIdData.csv");
    }
}
