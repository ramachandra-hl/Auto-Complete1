package runner;

import org.testng.TestNG;

import java.util.*;

public class TestLauncher {
    public static void main(String[] args) {

        TestNG testng = new TestNG();
        testng.setTestSuites(List.of("xmlFiles/RoomSetupWithModuleAddition.xml"));
        testng.run();

    }
}
