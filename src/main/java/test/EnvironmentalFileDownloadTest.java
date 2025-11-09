package test;


import configurator.TestConfig;
import org.testng.annotations.Test;
import services.EnvironmentalService;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentalFileDownloadTest extends TestConfig {

    @Test
    public void downloadEnvironmentalFiles() {

        List<String> validNames = new ArrayList<>();
        validNames.add("cutlist");
        validNames.add("pricepayload");
        validNames.add("boq");
        validNames.add("detailedprice");
        validNames.add("basicprice");
        validNames.add("packagelist");

        EnvironmentalService environmentalService = new EnvironmentalService();

        try {
            ReporterInfo("Downloading files for types: " + validNames);
            environmentalService.downloadByType(validNames, projectID, token);
            ReporterPass("Files downloaded successfully");

        } catch (AssertionError e) {
            ReporterFail("Test failed due to assertion error: " + e.getMessage());

            throw new AssertionError("Test failed due to unexpected error", e);
        }
    }
}
