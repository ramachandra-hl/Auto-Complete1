package configurator;

import com.aventstack.extentreports.ExtentReports;
import java.util.Properties;

public class GlobalVariables {
	public static Properties configProperties = new Properties();
	public static String currentExtentReportDir;
	public static String baseDir;

	public static ExtentReports extentReports;
	public static String deviceType = "Desktop";
	public static String executedBy;
	public static String url;
	public static String browser;
}
