package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

public class FetchPricePayloadDetailsReportTest {
    @Test
    public void generatePricePayloadDetailsReport() throws Exception {
        String inputFilePath = "CSVinputs/pricePayload.json";
        String outputFilePath = "reports/PriceReports/FetchPricePayloadDetailsReport.csv";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(inputFilePath));
        JsonNode modules = root.get("modules");
        FileWriter writer = new FileWriter(outputFilePath);
        writer.append("s.n,roomId,moduleId,moduleNumber,moduleName,unitEntryId,uuid\n");
        int sn = 1;
        for (JsonNode module : modules) {
            String roomId = module.has("roomId") ? module.get("roomId").asText("") : "";
            String moduleId = module.has("moduleId") ? module.get("moduleId").asText("") : "";
            String moduleNumber = module.has("modulenumber") ? module.get("modulenumber").asText("") : "";
            String moduleName = module.has("moduleName") ? module.get("moduleName").asText("") : "";
            String unitEntryId = module.has("unitEntryId") ? module.get("unitEntryId").asText("") : "";
            String uuid = module.has("uuid") ? module.get("uuid").asText("") : "";
            writer.append(sn + "," + roomId + "," + moduleId + "," + moduleNumber + "," + moduleName.replace(",", " ") + "," + unitEntryId + "," + uuid + "\n");
            sn++;
        }
        writer.flush();
        writer.close();
        System.out.println("Report generated at: " + outputFilePath);
    }
}

