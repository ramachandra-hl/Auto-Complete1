package utils;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


import static io.restassured.RestAssured.given;

public class Utilities {
    private static final Logger log = LogManager.getLogger(Utilities.class);
    private static final Random random = new Random();
    private static final String DEFAULTS_FILE = "src/main/resources/Configuration/defaultConfigurations.properties";

    public static String getRandomNumber() {
        int number = random.nextInt(9999999);
        return "387" + String.format("%06d", number);
    }

    private static final String LOCAL_CONFIG_FOLDER = System.getProperty("user.home") + "/Documents/AutoQA-Complete"; // ✅ include /input
    private static final String CLOUD_CONFIG_FOLDER = System.getProperty("user.home") + "/AutoQA-Complete/input"; // ✅ include /input

    /**
     * Dynamically determines config folder depending on environment.
     */
    private static Path getConfigDir() {

        Path localPath = Paths.get(LOCAL_CONFIG_FOLDER);
        if (Files.exists(localPath)) {
            System.out.println("💻 Using local config directory: " + localPath.toAbsolutePath());
            return localPath;
        }

        Path cloudPath = Paths.get(CLOUD_CONFIG_FOLDER);
        try {
            if (!Files.exists(cloudPath)) {
                Files.createDirectories(cloudPath);
                System.out.println("☁️ Created fallback cloud config directory: " + cloudPath.toAbsolutePath());
            } else {
                System.out.println("☁️ Using existing fallback cloud config directory: " + cloudPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create cloud config directory: " + e.getMessage());
        }
        return cloudPath;
    }

    public static String formatCurrentDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    public static String getValidPhoneNumber() {
        int maxAttempts = 100;

        for (int i = 0; i < maxAttempts; i++) {
            String num = getRandomNumber();
            if (validatePhoneNumber(num)) {
                return num;
            }
        }
        throw new RuntimeException("Unable to generate a valid phone number after " + maxAttempts + " attempts.");
    }


    public static boolean validatePhoneNumber(String phNum) {
        RequestSpecification req = given();
        String payload = "{\"phone\":\"" + phNum + "\",\"country\":\"IN\"}";
        req.header("referer", "https://website-preprod.homelane.com/");
        req.header("content-type", "text/plain;charset=UTF-8");
        req.body(payload);
        req.contentType("text/plain");
        Response res = req.post("https://website-preprod-node.homelane.com/api/v1/validatePhone").then().extract().response();
        return (boolean) res.jsonPath().getMap("data").get("valid");
    }

    public static String getTomorrowDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        date = c.getTime();
        return formatter.format(date);
    }

    public static String getDateTwoMonthsInTheFuture() {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return twoMonthsLater.format(formatter);
    }

    public static String generateUniqueKeyAndID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static String getTodayDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    public static JSONObject updateRoomDetails(String roomName, JSONArray walls, String roomType) {
        JSONParser jsonParser = new JSONParser();
        String uniqueKey = generateUniqueKeyAndID();
        String filePath = getResourceFile(getRoomPathBasedOnRoomType(roomType));
        JSONObject updatedRoomData = null;

        try (FileReader readerRoom = new FileReader(filePath)) {
            JSONObject roomData = (JSONObject) jsonParser.parse(readerRoom);
            JSONArray boundaries = (JSONArray) roomData.get("boundaryWalls");
            roomData.put("key", uniqueKey);
            roomData.put("id", uniqueKey);
            roomData.put("name", roomName);

            int minSize = Math.min(walls.size(), boundaries.size());
            for (int i = 0; i < minSize; i++) {
                JSONObject wall = (JSONObject) walls.get(i);
                JSONObject keyItem = (JSONObject) boundaries.get(i);
                String wallKey = (String) wall.get("key");
                keyItem.put("key", wallKey);
            }

            roomData.put("boundaryWalls", boundaries);
            updatedRoomData = roomData;

        } catch (FileNotFoundException e) {
            log.error("Room file not found: {}", filePath, e);
        } catch (IOException e) {
            log.error("Error reading room file: {}", filePath, e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: ", e);
        }

        return updatedRoomData;
    }

    public static JSONObject updatePositionInModule(int position, Map<String, String> zoneData) {
        JSONParser jsonParser = new JSONParser();
        String filePath = "payloads/customZone.json";
        JSONObject customZoneData = null;
        try {
            FileReader readerRoom = new FileReader(filePath);
            customZoneData = (JSONObject) jsonParser.parse(readerRoom);
            JSONObject boundaries = (JSONObject) customZoneData.get("position");
            long currentX = (long) boundaries.get("x");
            boundaries.put("x", currentX + position);
            customZoneData.putAll(zoneData);
        } catch (Exception e) {
            log.error("e: ", e);
        }
        return customZoneData;
    }

    public static JSONObject updatePositionInModule(JSONObject customzone, Map<String, String> zoneData) {
        JSONObject customZoneData = null;
        try {
            customZoneData = customzone;
            customZoneData.putAll(zoneData);
        } catch (Exception e) {
            log.error("e: ", e);
        }
        return customZoneData;
    }

    public static JSONObject updatePositionInCustomZone(String filePath, Map<String, String> zoneData) {
        JSONParser jsonParser = new JSONParser();
        JSONObject customZoneData = null;

        try (FileReader readerRoom = new FileReader(filePath)) {
            customZoneData = (JSONObject) jsonParser.parse(readerRoom);
            System.out.println(customZoneData);
            for (Map.Entry<String, String> entry : zoneData.entrySet()) {
                String key = entry.getKey().trim();
                String value = entry.getValue().trim();

                if (key.equalsIgnoreCase("moduleName")) {
                    customZoneData.put("zoneName", "1_" + value);
                } else {
                    customZoneData.put(key, value);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("CustomZoneFile not found: {}", filePath, e);
        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: ", e);
        }

        return customZoneData;
    }



    public static JSONObject updatePositionInCustomZone(JSONObject customZoneData, Map<String, String> zoneData) {
        for (Map.Entry<String, String> entry : zoneData.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("moduleName")) {
                customZoneData.put("zoneName", "1_" + entry.getValue());
            } else {
                customZoneData.put(entry.getKey(), entry.getValue());
            }
        }
        return customZoneData;
    }

    public static JSONObject updatePositionInModule(String roomCategory, JSONObject customZoneData, Map<String, String> additionalZoneData) {
        JSONObject currentCustomZoneData = null;
        try {
            currentCustomZoneData = customZoneData;
            JSONObject positionBounds = (JSONObject) currentCustomZoneData.get("position");
            String previousZoneName = (String) currentCustomZoneData.get("zoneName");
            int prevoiusZoneCount = Integer.parseInt(String.valueOf(previousZoneName.charAt(0)));
            Number currentPositionX = (Number) positionBounds.get("x");
            Number currentPositionZ = (Number) positionBounds.get("z");
            double[] targetPositionValues = getTargetValues(roomCategory);
            double maxBoundaryX = targetPositionValues[0];
            double minBoundaryX = targetPositionValues[2];
            double newPositionX = currentPositionX.longValue() + 2000;
            if (newPositionX > (maxBoundaryX - 500)) {
                long newPositionZ = currentPositionZ.longValue() + 2000;
                newPositionX = (minBoundaryX + 1500);
                positionBounds.put("z", newPositionZ);
            }

            positionBounds.put("x", newPositionX);
            String moduleName = additionalZoneData.get("moduleName");
            for (Map.Entry<String, String> zoneDataEntry : additionalZoneData.entrySet()) {
                if (zoneDataEntry.getKey().equalsIgnoreCase("moduleName")) {
                    currentCustomZoneData.put("zoneName", prevoiusZoneCount + 1 + "_" + moduleName);
                } else {
                    currentCustomZoneData.put(zoneDataEntry.getKey(), zoneDataEntry.getValue());
                }
            }
        } catch (Exception e) {
            log.error("e: ", e);
        }
        return currentCustomZoneData;
    }


    public static double[] getTargetValues(String roomCategory) {
        String wallFilePath = getResourceFile(getWallPathBasedOnRoomType(roomCategory));

        JSONParser jsonParser = new JSONParser();
        try {
            FileReader wallFileReader = new FileReader(wallFilePath);
            JSONArray wallDataArray = (JSONArray) jsonParser.parse(wallFileReader);
            JSONObject firstWallObject = (JSONObject) wallDataArray.get(0);
            double startPointY = ((Number) ((JSONObject) firstWallObject.get("startPoint")).get("y")).doubleValue();
            double endPointX = ((Number) ((JSONObject) firstWallObject.get("endPoint")).get("x")).doubleValue();
            double startPointX = ((Number) ((JSONObject) firstWallObject.get("startPoint")).get("x")).doubleValue();
            return new double[]{endPointX, startPointY, startPointX};
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Wall data file not found: " + wallFilePath, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading wall data file: " + wallFilePath, e);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing wall data file: " + wallFilePath, e);
        }
    }


    public static String generateRandomString(int length) {
        if (length <= 0) return "";

        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomAscii;
            if (ThreadLocalRandom.current().nextBoolean()) {
                randomAscii = ThreadLocalRandom.current().nextInt(65, 91);
            } else {
                randomAscii = ThreadLocalRandom.current().nextInt(97, 123);
            }
            stringBuilder.append((char) randomAscii);
        }

        return stringBuilder.toString();
    }


    public static String convertDateToStringFormat() {
        Date today = new Date();

        SimpleDateFormat dayFormat = new SimpleDateFormat("d");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yy");

        int day = Integer.parseInt(dayFormat.format(today));
        int month = Integer.parseInt(monthFormat.format(today));
        int year = Integer.parseInt(yearFormat.format(today));

        // Mapping for months
        String[] months = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        // Mapping for day numbers
        String[] days = {
                "", "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
                "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth",
                "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth", "twentieth",
                "twentyfirst", "twentysecond", "twentythird", "twentyfourth", "twentyfifth",
                "twentysixth", "twentyseventh", "twentyeighth", "twentyninth", "thirtieth",
                "thirtyfirst"
        };

        // Convert year to words
        Map<Character, String> numberWords = new HashMap<>();
        numberWords.put('0', "zero");
        numberWords.put('1', "one");
        numberWords.put('2', "two");
        numberWords.put('3', "three");
        numberWords.put('4', "four");
        numberWords.put('5', "five");
        numberWords.put('6', "six");
        numberWords.put('7', "seven");
        numberWords.put('8', "eight");
        numberWords.put('9', "nine");

        StringBuilder yearInWords = new StringBuilder();
        for (char digit : String.valueOf(year).toCharArray()) {
            yearInWords.append(numberWords.get(digit));
        }
        return days[day] + months[month] + yearInWords;
    }

    public static String convert(int number) {
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};

        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        if (number == 0) {
            return "Zero";
        }
        String result = "";

        if (number >= 1000) {
            result += ones[number / 1000] + "Thousand";
            number %= 1000;
        }
        if (number >= 100) {
            result += ones[number / 100] + "Hundred";
            number %= 100;
        }
        if (number >= 20) {
            result += tens[number / 10];
            number %= 10;
        }
        if (number > 0) {
            result += ones[number];
        }
        return result.trim();
    }

    public static JSONArray updateWallKey(String roomType) {
        JSONParser jsonParser = new JSONParser();
        String filePath = getResourceFile(getWallPathBasedOnRoomType(roomType));
        try {
            JSONArray newWallData = new JSONArray();
            FileReader readerWall = new FileReader(filePath);
            JSONArray wallData = (JSONArray) jsonParser.parse(readerWall);

            wallData.forEach(value -> {
                JSONObject eachWall = (JSONObject) value;
                String uniqueKey = Utilities.generateUniqueKeyAndID();
                eachWall.put("key", uniqueKey);
                newWallData.add(eachWall);
            });
            return newWallData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String customerNameFormatter(String name, int count, String environment, String brand) {
        String prefix = environment.equalsIgnoreCase("prod") ? "Test" : "";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return String.format("%s%s",
                prefix,
                name.trim().replaceAll("\\s+", "")
        );
    }


    public static JSONArray updateWallDataInYAxis(int width, String roomType) {
        JSONParser jsonParser = new JSONParser();
        String filePath = getResourceFile(getWallPathBasedOnRoomType(roomType));
        try {
            JSONArray newWallData = new JSONArray();
            FileReader readerWall = new FileReader(filePath);
            JSONArray wallData = (JSONArray) jsonParser.parse(readerWall);

            wallData.forEach(value -> {
                JSONObject eachWall = (JSONObject) value;
                String uniqueKey = Utilities.generateUniqueKeyAndID();
                eachWall.put("key", uniqueKey);

                JSONObject endPoint = (JSONObject) eachWall.get("endPoint");
                JSONObject startPoint = (JSONObject) eachWall.get("startPoint");
                double y = Double.parseDouble(endPoint.get("y").toString());
                double ys = Double.parseDouble(startPoint.get("y").toString());

                endPoint.put("y", y + width);
                startPoint.put("y", ys + width);
                newWallData.add(eachWall);
            });

            return newWallData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject updateZoneZValueBasedOnZoneType(JSONArray wallPayload, String roomType) {
        String filePath = getResourceFile(getCustomZoneFilePathBasedOnZoneType(roomType));
        JSONParser jsonParser = new JSONParser();
        JSONObject updatedCustomZone = new JSONObject();

        try {
            FileReader readerCustomZone = new FileReader(filePath);
            JSONObject zoneData = (JSONObject) jsonParser.parse(readerCustomZone);

            JSONObject positionBounds = (JSONObject) zoneData.get("position");
            Number currentPositionZ = (Number) positionBounds.get("z");
            positionBounds.put("z", currentPositionZ.doubleValue());

            updatedCustomZone.putAll(zoneData);
            updatedCustomZone.put("position", positionBounds);

        } catch (Exception e) {
            log.info("Error while updating zone Z value: {}", String.valueOf(e));
        }

        return updatedCustomZone;
    }

    public static String getWallPathBasedOnRoomType(String roomType) {
        String filePath;
        filePath = switch (roomType.toLowerCase()) {
            case "livingroom" -> "payloads/walls/livingRoomWall.json";
            case "kitchen" -> "payloads/walls/kitchenWall.json";
            case "bedroom" -> "payloads/walls/bedRoomWall.json";
            case "bathroom" -> "payloads/walls/bathRoomWall.json";
            case "deckokitchen" -> "payloads/walls/DeckoKitchenWall.json";
            case "deckolivingroom" -> "payloads/walls/DeckoLivingRoomWall.json";
            case "deckobedroom" -> "payloads/walls/DeckoBedRoomWall.json";
            case "deckobathroom" -> "payloads/walls/DeckoBathRoomWall.json";
            case "gola" -> "payloads/walls/GolaKitchenWall.json";
            case "gp"   -> "payloads/walls/GPKitchenWall.json";
            default -> throw new IllegalArgumentException("Invalid room type: " + roomType);
        };

        return filePath;
    }

    public static String getRoomPathBasedOnRoomType(String roomType) {

        return switch (roomType.toLowerCase()) {
            case "livingroom" -> "payloads/rooms/livingRoom.json";
            case "kitchen" -> "payloads/rooms/kitchenRoom.json";
            case "bedroom" -> "payloads/rooms/bedRoom.json";
            case "bathroom" -> "payloads/rooms/bathRoom.json";
            case "deckokitchen" -> "payloads/rooms/DeckoKitchenRoom.json";
            case "deckolivingroom" -> "payloads/rooms/DeckoLivingRoom.json";
            case "deckobedroom" -> "payloads/rooms/DeckoBedRoom.json";
            case "deckobathroom" -> "payloads/rooms/DeckoBathRoom.json";
            case "gola" -> "payloads/rooms/GolaKitchenRoom.json";
            case "gp"   -> "payloads/rooms/GPKitchenRoom.json";
            default -> throw new IllegalArgumentException("Invalid room type: " + roomType);
        };
    }

    public static String getCustomZoneFilePathBasedOnZoneType(String roomType) {

        return switch (roomType.toLowerCase()) {
            case "livingroom" -> "payloads/customZones/livingCustomZone.json";
            case "kitchen" -> "payloads/customZones/kitchenCustomZone.json";
            case "bedroom" -> "payloads/customZones/bedRoomCustomZone.json";
            case "bathroom" -> "payloads/customZones/bathRoomCustomZone.json";
            case "deckokitchen" -> "payloads/customZones/DeckoKitchenCustomZone.json";
            case "deckolivingroom" -> "payloads/customZones/DeckoLivingCustomZone.json";
            case "deckobedroom" -> "payloads/customZones/DeckoBedRoomCustomZone.json";
            case "deckobathroom" -> "payloads/customZones/DeckoBathRoomCustomZone.json";
            case "gola" -> "payloads/customZones/GolaKitchenCustomZone.json";
            case "gp"   -> "payloads/customZones/GPKitchenCustomZone.json";
            default -> throw new IllegalArgumentException("Invalid zone type: " + roomType);
        };
    }


    public static void csvToProperties(String csvFilePath, String propertiesFile) throws IOException {
        Properties properties = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
             FileWriter writer = new FileWriter(propertiesFile)) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split(",", 2);

                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    properties.setProperty(key, value);
                } else {
                    log.error("Invalid line in CSV: {}", line);
                }
            }
            properties.store(writer, "Generated from CSV");
            log.info("Properties file created successfully: {}", propertiesFile);
        }
    }

    public void createCSVReport(String[] headers, String filePath) {
        File file = new File(filePath);

        file.getParentFile().mkdirs();
        if (!Files.exists(Paths.get(filePath))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(String.join(",", headers));
                writer.newLine();
                log.info("Test report created with headers.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, String> extractRoomTypeIdMap(Response response) {
        Map<String, String> roomTypeIdMap = new HashMap<>();

        // Get floors list from JSON path
        List<Map<String, Object>> floors = response.jsonPath().getList("floors");

        if (floors == null || floors.isEmpty()) {
            return roomTypeIdMap;
        }

        for (Map<String, Object> floor : floors) {
            // Get rooms list for each floor
            List<Map<String, Object>> rooms = (List<Map<String, Object>>) floor.get("rooms");
            if (rooms == null) continue;

            for (Map<String, Object> room : rooms) {
                String roomType = room.get("roomType").toString();
                String roomId = room.get("id").toString();
                roomTypeIdMap.put(roomType, roomId);
            }
        }

        return roomTypeIdMap;
    }


    public void appendTestResults(String[] testResults, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {

            writer.write(String.join(",", testResults));
            writer.newLine();

            log.info("Test results added to report for  {}", testResults[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendTestResultsWithHeaders(String[] headers, List<String> values, String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean fileExists = Files.exists(path);

            List<String> lines = fileExists ? Files.readAllLines(path) : new ArrayList<>();
            List<String> existingHeaders = lines.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(lines.get(0).split(",")));

            if (!fileExists) {
                Files.createFile(path);
                Files.write(path, Collections.singleton(String.join(",", headers)), StandardOpenOption.CREATE);
                existingHeaders = new ArrayList<>(Arrays.asList(headers));
                lines.add(String.join(",", headers));
            }

            List<String> newHeaders = new ArrayList<>();
            for (String header : headers) {
                if (!existingHeaders.contains(header)) {
                    newHeaders.add(header);
                }
            }
            if (!newHeaders.isEmpty()) {
                existingHeaders.addAll(newHeaders);
                String updatedHeaderLine = String.join(",", existingHeaders);
                if (lines.isEmpty()) {
                    lines.add(updatedHeaderLine);
                } else {
                    lines.set(0, updatedHeaderLine);
                }
                Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
            }

            Map<String, String> valueMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                valueMap.put(headers[i], values.size() > i ? values.get(i) : "");
            }

            StringBuilder row = new StringBuilder();
            for (String header : existingHeaders) {
                row.append(valueMap.getOrDefault(header, "")).append(",");
            }
            row.setLength(row.length() - 1);
            Files.write(path, Collections.singleton(row.toString()), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void convertCSVToExcel(String csvFile, String excelFile) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int rowNum = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < values.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(values[i]);
                }
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }

    public static List<Map<String, String>> getCommonShutterMaterial(Response zoneResponse) {
        List<Map<String, String>> shuttersWithAccessories = (List<Map<String, String>>) zoneResponse.jsonPath().getList("data.getZoneData.graphqlModules.shuttersWithAccessories").get(0);
        List<Map<String, String>> graphqlShutters = zoneResponse.jsonPath().getList("data.getZoneData.graphqlModules.graphqlShutters");

        for (Map<String, String> shuttersWithAccessory : shuttersWithAccessories) {
            if (shuttersWithAccessory.get("shutterType").equalsIgnoreCase("INCT-25mm-v1")) {
                return Collections.emptyList();
            }
        }
        return graphqlShutters;
    }


    public void renamingLeadReportFile(String filePath, String finalFileName) {
        File oldFile = new File(filePath);
        String parentDir = oldFile.getParent();

        File newFile = new File(parentDir, finalFileName);
        boolean renamed = oldFile.renameTo(newFile);
        if (renamed) {
            log.info("📝 Renamed report to: {}", newFile.getAbsolutePath());
        } else {
            log.warn("⚠️ Failed to rename report file.");
        }
    }


    public static boolean isBeforeToday(String filedDate) {
        try {
            if (filedDate == null || filedDate.isBlank()) {
                filedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }

            filedDate = filedDate.trim().replaceAll("[^0-9\\-]", "");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);

            LocalDate fileDateParsed = LocalDate.parse(filedDate, formatter);
            LocalDate today = LocalDate.now();

            // Only before today (your logic seems incorrect earlier)
            return fileDateParsed.isBefore(today);

        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: [" + filedDate + "] → " + e.getMessage());
            System.out.println("time check failed");
            return false;
        }
    }

    public static List<String> parseStringList(String str) {
        str = str.replaceAll("[\\[\\]\\s]", "");
        if (str.isEmpty()) return new ArrayList<>();
        return Arrays.asList(str.split(","));
    }

    public static boolean equalsIgnoreCaseAndSpace(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        String normalizedStr1 = str1.trim().replaceAll("\\s+", "");
        String normalizedStr2 = str2.trim().replaceAll("\\s+", "");
        return normalizedStr1.equalsIgnoreCase(normalizedStr2);
    }

    // Streamline extraction with helper
    public static String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    public static String cleanString(String input) {
        if (input != null) {
            // Remove leading and trailing quotes
            if (input.startsWith("\"") && input.endsWith("\"")) {
                input = input.substring(1, input.length() - 1);
            }
            // Remove leading and trailing brackets
            if (input.startsWith("[") && input.endsWith("]")) {
                input = input.substring(1, input.length() - 1);
            }
        }
        return input;
    }
    public static void safePut(Map<String, Object> map, String key, List<String> list, int idx) {
        if (list != null && idx < list.size())
            map.put(key, list.get(idx));
    }
    public static List<String> safeParseStringList(Object obj) {
        if (obj == null) return new ArrayList<>();
        return Utilities.parseStringList(obj.toString());
    }
    public static boolean equalsIgnoreCaseOrNull(String input, Object designValue) {
        if (input != null) {
            input = input.trim();
            if (input.isEmpty() || input.equalsIgnoreCase("null")) {
                input = null;
            }
        }
        String designStr = null;
        if (designValue != null) {
            designStr = String.valueOf(designValue).trim();
            if (designStr.isEmpty() || designStr.equalsIgnoreCase("null")) {
                designStr = null;
            }
        }
        if (input == null && designStr == null) return true;
        if (input == null || designStr == null) return false;

        return input.equalsIgnoreCase(designStr);
    }


    public static InputStream getResourceAsStream(String path) {
        // Try classpath resource first (for Spring Boot / JAR)
        InputStream resource = Utilities.class.getClassLoader().getResourceAsStream(path);
        if (resource != null) {
            return resource;
        }

        // Fallback for local IntelliJ run
        try {
            if (Files.exists(Paths.get(path))) {
                return new FileInputStream(path);
            }
        } catch (Exception e) {
            System.out.println("⚠️ Unable to load resource: " + path);
        }

        return null;
    }

    public static File getResourceAsFile(String path) throws IOException {
        InputStream is = getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + path);
        }

        File tempFile = File.createTempFile("config-", ".tmp");
        tempFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tempFile)) {
            is.transferTo(out);
        }
        return tempFile;
    }

    public synchronized static Map<String, Object> addToTestDataFromProperties(String fileName, Map<String, Object> testData) {
        Properties prop = new Properties();
        Path filePath = null;

        // 1️⃣ Try local folder
        Path localFile = Paths.get( fileName);
        if (Files.exists(localFile)) {
            filePath = localFile;
            log.info("📂 Using local properties: {}", filePath.toAbsolutePath());
        } else {
            // 2️⃣ Try cloud folder
            Path cloudFile = Paths.get(CLOUD_CONFIG_FOLDER, fileName);
            if (Files.exists(cloudFile)) {
                filePath = cloudFile;
                log.info("☁️ Using cloud properties: {}", filePath.toAbsolutePath());
            }
        }

        try (InputStream inputStream = filePath != null
                ? Files.newInputStream(filePath)
                : Utilities.class.getResourceAsStream("/" + fileName)) {

            if (inputStream == null) {
                log.error("❌ Properties file not found in local, cloud, or classpath: {}", fileName);
                return testData; // ✅ return input map even if file not found
            }

            prop.load(inputStream);
            for (String name : prop.stringPropertyNames()) {
                testData.put(name, prop.getProperty(name));
            }

            log.info("✅ Properties loaded successfully from {}",
                    filePath != null ? filePath.toAbsolutePath() : "classpath:/" + fileName);

        } catch (IOException e) {
            log.error("❌ Failed to read properties {}: {}", fileName, e.getMessage(), e);
        }

        return testData; // ✅ return the updated map
    }

    public static Map<String, Object> addToTestDataFromYml(String fileName, Map<String, Object> testData) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = openClasspathStream(fileName)) { // try-with-resources
            if (inputStream == null) {
                log.warn("⚠️ YAML file {} not found in classpath", fileName);
                return testData;
            }

            Map<String, Object> yamlData = yaml.load(inputStream);
            if (yamlData != null) {
                testData.putAll(yamlData);
                log.info("✅ Loaded YAML test data from {}", fileName);
            } else {
                log.warn("⚠️ YAML file {} was empty or invalid", fileName);
            }
        } catch (Exception e) {
            log.error("❌ Failed to read YAML {}: {}", fileName, e.getMessage(), e);
        }
        return testData;
    }

    public static Map<String, Object> loadStandardConfig(String customerType, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String configFile = switch (customerType) {
            case "HL", "LUXE", "HFN" -> "configuration/HL_standardConfigurations.yml";
            case "DC" -> "configuration/DC_standardConfigurations.yml";
            default -> null;
        };
        if (configFile != null) {
            updatedTestData =   addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
        return updatedTestData;
    }

    public static String getResourceFile(String resourcePath) {
        try {
            // 1️⃣ Check local file (for IntelliJ or Maven run)
            Path localPath = Paths.get("src/main/resources/", resourcePath);
            if (Files.exists(localPath)) {
                return localPath.toAbsolutePath().toString();
            }

            // 2️⃣ Check inside JAR (classpath resource)
            InputStream resourceStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(resourcePath);

            if (resourceStream != null) {
                File tempFile = File.createTempFile("payload-", ".json");
                Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return tempFile.getAbsolutePath();
            }

            // 3️⃣ File not found anywhere
            System.err.println("⚠️ Resource not found: " + resourcePath);
            return null;

        } catch (IOException e) {
            System.err.println("⚠️ Failed to resolve resource: " + resourcePath + " | Reason: " + e.getMessage());
            return null;
        }
    }

    public static Map<String, Object> loadCustomerConfig(String customerType, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String configFile = switch (customerType) {
            case "HL", "LUXE" -> "configuration/HL_config.yml";
            case "HFN" -> "configuration/HFN_config.yml";
            case "DC" -> "configuration/DC_config.yml";
            default -> null;
        };
        if (configFile != null) {
            updatedTestData = addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
        return updatedTestData;
    }

    public static Map<String, Object> loadCityConfig(String citycode, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String cityFile = switch (citycode) {
            case "1" -> "configuration/CitiesData/homelane/1-Bengaluru.properties";
            case "2" -> "configuration/CitiesData/homelane/2-Chennai.properties";
            case "3" -> "configuration/CitiesData/homelane/3-Mumbai.properties";
            case "4" -> "configuration/CitiesData/homelane/4-Kolkata.properties";
            case "5" -> "configuration/CitiesData/homelane/5-Kochi.properties";
            case "6" -> "configuration/CitiesData/homelane/6-Visakhapatnam.properties";
            case "7" -> "configuration/CitiesData/homelane/7-Delhi.properties";
            case "8" -> "configuration/CitiesData/homelane/8-Hyderabad.properties";
            case "9" -> "configuration/CitiesData/homelane/9-Gurgaon.properties";
            case "10" -> "configuration/CitiesData/homelane/10-Pune.properties";
            case "11" -> "configuration/CitiesData/homelane/11-Thane.properties";
            case "12" -> "configuration/CitiesData/homelane/12-Lucknow.properties";
            case "13" -> "configuration/CitiesData/homelane/13-Mangalore.properties";
            case "14" -> "configuration/CitiesData/homelane/14-Mysore.properties";
            case "15" -> "configuration/CitiesData/homelane/15-Patna.properties";
            case "16" -> "configuration/CitiesData/franchise/16-Thirupathi.properties";
            case "17" -> "configuration/CitiesData/franchise/17-Guwahati.properties";
            case "18" -> "configuration/CitiesData/franchise/18-Vijayawada.properties";
            case "19" -> "configuration/CitiesData/franchise/19-Nizamabad.properties";
            case "20" -> "configuration/CitiesData/franchise/20-Shivamogga.properties";
            case "21" -> "configuration/CitiesData/franchise/21-Siliguri.properties";
            case "22" -> "configuration/CitiesData/franchise/22-Trivendrum.properties";
            case "23" -> "configuration/CitiesData/franchise/23-Warangal.properties";
            case "24" -> "configuration/CitiesData/franchise/24-Karimnagar.properties";
            case "25" -> "configuration/CitiesData/franchise/25-Jamshedpur.properties";
            case "26" -> "configuration/CitiesData/homelane/26-Noida.properties";
            case "27" -> "configuration/CitiesData/homelane/27-Coimbatore.properties";
            case "28" -> "configuration/CitiesData/homelane/28-Bhubaneswar.properties";
            case "29" -> "configuration/CitiesData/homelane/29-Salem.properties";
            case "30" -> "configuration/CitiesData/homelane/30-Nagpur.properties";
            case "31" -> "configuration/CitiesData/homelane/31-Surat.properties";
            case "32" -> "configuration/CitiesData/homelane/32-Ranchi.properties";
            case "33" -> "configuration/CitiesData/homelane/33-Ghaziabad.properties";
            case "34" -> "configuration/CitiesData/homelane/34-Nashik.properties";
            case "35" -> "configuration/CitiesData/homelane/35-Madurai.properties";
            case "36" -> "configuration/CitiesData/homelane/36-Tiruchirappalli.properties";
            case "37" -> "configuration/CitiesData/homelane/37-Jaipur.properties";
            case "38" -> "configuration/CitiesData/homelane/38-Ahmedabad.properties";
            default -> null;
        };
        if (cityFile != null) {
            updatedTestData = addToTestDataFromProperties(cityFile, testData);
        } else {
            log.warn("Unknown city code: {}", citycode);
        }
        return updatedTestData;
    }


    private static InputStream openClasspathStream(String fileName) {
        String cpName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
        InputStream is = Utilities.class.getClassLoader().getResourceAsStream(cpName);
        if (is == null) {
            log.error("❌ Resource not found on classpath: {}", cpName);
        } else {
            System.out.println("📄 Loading from classpath: " + cpName);
        }
        return is;
    }

    /**
     * Updates (or creates once) a .properties file with given key-value pairs.
     * ✅ Never recreates file — only updates or adds keys.
     */
    public synchronized static boolean updateProperties(String fileName, Map<String, String> propertiesToUpdate) {
        Properties props = new Properties();
        Path configDir = getConfigDir();
        Path filePath = configDir.resolve(fileName);

        try {
            // ✅ Ensure directory exists
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // ✅ Load existing properties if file exists
            if (Files.exists(filePath)) {
                try (InputStream in = Files.newInputStream(filePath)) {
                    props.load(in);
                    System.out.println("📄 Loaded existing properties from: " + filePath.toAbsolutePath());
                }
            } else {
                // Create empty file only once
                Files.createFile(filePath);
                System.out.println("🆕 Created new properties file: " + filePath.toAbsolutePath());
                propertiesToUpdate.put("lastProcessedLeadIndex", "1");
                propertiesToUpdate.put("gmailDomain", "@yopmail.com");
                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                propertiesToUpdate.put("leadScriptRunDate", formattedDate);
            }

            // Print current values before update
            if (!props.isEmpty()) {
                System.out.println("🔹 Current properties before update:");
                props.forEach((k, v) -> System.out.println(k + " = " + v));
            } else {
                System.out.println("⚪ No existing properties, starting fresh.");
            }

            // Apply updates (overwrites same keys, keeps old ones)
            propertiesToUpdate.forEach(props::setProperty);

            // Print values after update
            System.out.println("🔹 Properties after update:");
            props.forEach((k, v) -> System.out.println(k + " = " + v));


            // ✅ Apply updates (overwrites same keys, keeps old ones)
            propertiesToUpdate.forEach(props::setProperty);

            // ✅ Save without deleting existing data
            try (OutputStream out = Files.newOutputStream(filePath)) {
                props.store(out, "Updated by ConfigUtils");
            }

            System.out.println("✅ Properties updated successfully: " + filePath.toAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("❌ Error updating properties file: " + e.getMessage());
            return false;
        }
    }




}
