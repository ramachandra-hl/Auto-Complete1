package services;

import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.testng.Assert;
import utils.PropertiesReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static configurator.ApiService.invokeGetRequestWithHeaders;
import static configurator.ApiService.invokePostRequest;


public class EnvironmentalService {

    private static final Logger logger = LogManager.getLogger(EnvironmentalService.class);
    static String folderName;
    static  PropertiesReader propertiesReader =  new PropertiesReader();
   String baseURl = propertiesReader.getBaseURl();
    public EnvironmentalService() {
        deleteCreatedFolder();
        createFolder();
    }

    Response response;

    public Response downloadCutlist(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/d2m/validation/cutlist/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("cutlist", "csv", fileContent);
        logger.info("Cutlist successfully downloaded");
        return response;
    }

    public Response downloadPricePayLoad(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/scripts/d2m/payload/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("price_load", "json", fileContent);
        logger.info("Price Payload successfully downloaded");
        return response;
    }

    public Response downloadBOQ(String projectId, Map<String, String> token) {
        response = invokePostRequest(baseURl + "/api/v1.0/customer/customerId/order/orderId/quote/" + projectId + "/d2m/bom/sap", token);
        Assert.assertEquals(200, response.statusCode());
        Map<String, Object> data = response.jsonPath().getMap("data");

        // Process and save files for each part of the BOM
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                byte[] byteArray = convertJsonObjectToBytes(new JSONObject((Map) value));
                saveFile(key, "csv", byteArray);
            }
        }
        logger.info("BOQ successfully downloaded");
        return response;
    }

    public Response downloadDetailedPrice(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/d2m/validation/detailed-price/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("detailed_price", "csv", fileContent);
        logger.info("Detailed Price successfully downloaded");
        return response;
    }

    public Response downloadBasicPrice(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/d2m/validation/price/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("Basic_price", "csv", fileContent);
        logger.info("Basic Price successfully downloaded");
        return response;
    }

    public Response downloadPackageList(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/d2m/validation/packing-list/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("package_list", "csv", fileContent);
        logger.info("Package List successfully downloaded");
        return response;
    }

    public Response downloadEdgeBandReport(String projectId, Map<String, String> token) {
        response = invokeGetRequestWithHeaders(baseURl + "/d2m/validation/edge-band/" + projectId, token);
        Assert.assertEquals(200, response.statusCode());
        byte[] fileContent = response.asByteArray();
        saveFile("edge_band", "csv", fileContent);
        logger.info("Edge Band Report successfully downloaded");
        return response;
    }

    public Response downloadByType(String type, String projectId, Map<String, String> token) {
        return switch (type.toLowerCase()) {
            case "cutlist" -> downloadCutlist(projectId, token);
            case "pricepayload" -> downloadPricePayLoad(projectId, token);
            case "boq" -> downloadBOQ(projectId, token);
            case "detailedprice" -> downloadDetailedPrice(projectId, token);
            case "basicprice" -> downloadBasicPrice(projectId, token);
            case "packagelist" -> downloadPackageList(projectId, token);
            case "edgebandreport" -> downloadEdgeBandReport(projectId, token);
            default -> {
                logger.error("Invalid download type: {}", type);
                throw new IllegalArgumentException("Invalid download type: " + type);
            }
        };
    }

    public void downloadByType(List<String> types, String projectId, Map<String, String> token) {
        List<Response> responses = new ArrayList<>();
        for (String type : types) {
            try {
                Response response = downloadByType(type, projectId, token);
                responses.add(response);
                logger.info("Downloaded type: {}", type);
            } catch (IllegalArgumentException e) {
                logger.error("Error: Invalid download type - {}", type);
            }
        }
    }

    public void saveFile(String fileName, String extension, byte[] fileContent) {
        File downloadFile = new File(folderName + "/" + fileName + "." + extension);
        try {
            FileUtils.writeByteArrayToFile(downloadFile, fileContent);
            logger.info("{} file saved at: {}", extension.toUpperCase(), downloadFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error saving {} file: {}", extension, e.getMessage());
        }
    }

    public static byte[] convertJsonObjectToBytes(JSONObject jsonObject) {
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void createFolder() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date date = new Date();
        String todayTime = simpleDateFormat.format(date);
        folderName = "environmentalFiles/" + propertiesReader.getProjectID() + todayTime;
        File folder = new File(folderName);

        // Create the folder if it doesn't exist
        if (!folder.exists()) {
            if (folder.mkdir()) {
                logger.info("Folder '{}' created successfully.", folderName);
            } else {
                logger.error("Failed to create folder '{}'.", folderName);
            }
        } else {
            logger.warn("Folder '{}' already exists.", folderName);
        }
    }

    public static void deleteCreatedFolder() {
        File folder = new File("environmentalFiles");

        // Check if the folder exists
        if (folder.exists() && folder.isDirectory()) {
            try {
                deleteFolderRecursively(folder.toPath());
                logger.info("Folder '{}' deleted successfully.", folderName);
            } catch (IOException e) {
                logger.error("Failed to delete folder '{}'. Error: {}", folderName, e.getMessage());
            }
        } else {
            logger.warn("Folder '{}' does not exist or is not a directory.", folderName);
        }
    }

    private static void deleteFolderRecursively(Path folderPath) throws IOException {
        Files.walk(folderPath)
                .sorted(Comparator.reverseOrder()) // Reverse order to delete files first
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.error("Error deleting path: {} - {}", path, e.getMessage());
                    }
                });
    }

}
