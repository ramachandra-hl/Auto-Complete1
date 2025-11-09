package autoqa.controller;

import org.springframework.web.bind.annotation.*;
import services.RoasterService;
import utils.PropertiesReader;
import services.ControllerConfigService;

import java.util.*;

@CrossOrigin(origins = "*")
@RequestMapping("/roster/helper")
@RestController
public class TestHelperController extends ControllerConfigService {

    private PropertiesReader propertiesReader;
    private RoasterService roasterService;


    @PostMapping("/getShowroomsList")
    public Map<String, Object> getShowroomsList(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        System.out.println(request);
        customerType = request.getOrDefault("customerType", "");
        environment = request.getOrDefault("environment", "");
        // String city = request.getOrDefault("city", "");

        // Validation
        if (customerType.isEmpty() || environment.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Missing required fields: customerType, environment");
            response.put("showrooms", Collections.emptyList());
            return response;
        }

        try {
            initializeTestData();
            roasterService = new RoasterService();
            roasterService.initialize();

            List<Map<String, String>> showroomList = roasterService.getShowroomList(customerType);

            response.put("status", "success");
            response.put("customerType", customerType);
            response.put("environment", environment);
            response.put("showrooms", showroomList);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("showrooms", Collections.emptyList());
        }

        return response;
    }
}
