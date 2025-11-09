package utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class UpdateJsons {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void updatePayloadFromMap(Map<String, String> map, String filename) {
        JSONObject jsonObject = readJsonFile(filename);
        jsonObject.putAll(map);
        writeJsonFile(filename, jsonObject);
    }

    public  JSONObject readJsonFile(String filepath) {
        try (FileReader reader = new FileReader(filepath)) {
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + filepath, e);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error reading or parsing file: " + filepath, e);
        }
    }

    public void writeJsonFile(String filepath, JSONObject jsonObject) {
        try (FileOutputStream outputStream = new FileOutputStream(filepath)) {
            byte[] strToBytes = jsonObject.toJSONString().getBytes();
            outputStream.write(strToBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error writing JSON to file: " + filepath, e);
        }
    }
    public String updatePayloadFromMap(String filePath, Map<String, Object> updatedData) {
        JsonNode rootNode;
        try {
            File jsonFile = resolveResourceFile(filePath);
            rootNode = objectMapper.readTree(jsonFile);

            if (rootNode.isObject()) {
                updateNode((ObjectNode) rootNode, updatedData);
            } else if (rootNode.isArray()) {
                for (JsonNode item :  rootNode) {
                    if (item.isObject()) {
                        updateNode((ObjectNode) item, updatedData);
                    }
                }
            } else {
                throw new IllegalArgumentException("Unsupported root node type in JSON");
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateNode(ObjectNode node, Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                if (node.has(key) && node.get(key).isObject()) {
                    updateNode((ObjectNode) node.get(key), (Map<String, Object>) value);
                } else {
                    node.set(key, objectMapper.valueToTree(value));
                }
            } else if (value instanceof Iterable) {
                ArrayNode arrayNode = objectMapper.createArrayNode();
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof Map) {
                        ObjectNode childNode = objectMapper.createObjectNode();
                        updateNode(childNode, (Map<String, Object>) item);
                        arrayNode.add(childNode);
                    } else {
                        arrayNode.addPOJO(item);
                    }
                }
                node.set(key, arrayNode);
            } else {
                node.putPOJO(key, value);
            }
        }
    }

    private File resolveResourceFile(String filePath) throws IOException {
        // 1️⃣ Try local file (useful during development)
        Path localPath = Paths.get("src/main/resources", filePath);
        if (Files.exists(localPath)) {
            return localPath.toFile();
        }

        // 2️⃣ Try classpath resource (inside JAR)
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(filePath);
        if (resourceStream != null) {
            // Copy to a temp file so we can modify
            File tempFile = File.createTempFile("payload-", ".json");
            Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }

        throw new FileNotFoundException("❌ Resource not found: " + filePath);
    }


}
