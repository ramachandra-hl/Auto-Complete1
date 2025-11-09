package reporter;


import com.aventstack.extentreports.ExtentTest;

public class ExtentFactory {
	private static ExtentFactory instance = new ExtentFactory();
	ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();

	/***
	 * Get instance of ExtentFactory
	 * 
	 * @return		Instance of ExtentFactory
	 */
	public static ExtentFactory getInstance() {
		return instance;
	}

	/***
	 * Get Extent Test
	 * 
	 * @return	Extent Test
	 */
	public ExtentTest getExtentTest() {
		return extentTest.get();
	}

	/***
	 * Set Extent Test
	 * @param extentTestObj			Set Extent Test
	 */
	public void setExtentTest(ExtentTest extentTestObj) {
		extentTest.set(extentTestObj);
	}

	/***
	 * Removes Extent Test
	 */
	public void removeExtentTest(){
		extentTest.remove();
	}
}