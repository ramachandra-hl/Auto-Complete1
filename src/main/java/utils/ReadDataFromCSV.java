package utils;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReadDataFromCSV {

    private static final Logger log = LoggerFactory.getLogger(ReadDataFromCSV.class);
    private String path;

    public ReadDataFromCSV(String path) {
        this.path = path;
    }

    public List<Map<String, String>> readCSVAsListOfMaps() throws IOException {
        List<Map<String, String>> dataList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] header = reader.readNext(); // Read header row
            String[] row;

            while ((row = reader.readNext()) != null) {
                Map<String, String> rowMap = new HashMap<>();
                for (int i = 0; i < header.length; i++) {
                    rowMap.put(header[i].trim(), row[i]);
                }
                dataList.add(rowMap);
            }
        } catch (IOException | CsvValidationException e) {
            throw new IOException("Error reading CSV file: " + e.getMessage(), e);
        }
        return dataList;
    }
}
