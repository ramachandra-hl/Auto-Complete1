package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class RuntimeDataUtil {

    public static List<Map<String, String>> getRuntimeDataIfAvailable() {
        try {
            String jsonData = System.getProperty("testDataJson"); // data passed from UI/API
            if (jsonData != null && !jsonData.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(jsonData, new TypeReference<List<Map<String, String>>>() {});
            }
        } catch (Exception e) {
            System.out.println("Runtime test data not valid JSON: " + e.getMessage());
        }
        return null;
    }
}
