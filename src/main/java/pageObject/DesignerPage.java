package pageObject;

import configurator.UIBaseClass;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

public class DesignerPage extends UIBaseClass {
    public WebDriver driver;
    WebDriverWait wait;
    Actions actions;

    public DesignerPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(300));
        actions = new Actions(driver);
    }

    @FindBy(className = "UpdateProduct__pickColorAction--1kl4t")
    WebElement clickHereLocator;

    @FindBy(id = "session-viewer-canvas")
    WebElement canvasLocator;

    @FindBy(xpath = "//i[@class='fa fa-pencil']")
    WebElement editLocatorLocator;

    @FindBy(xpath = "//div[@class='UpdateProduct__addModuleButtonWrapper--1AGEq']//i[@class='fa fa-caret-down']")
    WebElement addModuleBtnLocator;
    @FindBy(className = "UpdateProduct__option--3qmqm")
    WebElement moduleDropDownOptionLocator;

    @FindBy(xpath = "//button[normalize-space()='Add To Space']")
    WebElement addToSpaceLocator;
    @FindBy(className = "UpdateProduct__applyBtn--3VKYz")
    WebElement saveAndApplyButtonLocator;

    @FindBy(className = "UpdateProduct__moduleWrapper--3TfMD")
    List<WebElement> modulesListLocator;

    @FindBy(id = "camera-view")
    WebElement cameraViewLocator;
    @FindBy(xpath = "//div[@title='Placement Status']")
    WebElement placementStatusLocator;

    @FindBy(xpath = "//div[@title='Actions' and @class='Topbar__actions--S3ZPz']")
     WebElement actionsLocator;

    @FindBy(className = "UpdateProduct__updateMaterialsSelect--1ZmoB")
    WebElement materialTypeDropDownLocator;

    @FindBy(className = "UpdateProduct__moduleUnit--3Roya")
    List<WebElement> moduleUnitsListLocator;


    By moduleNameByLocator = By.className("UpdateProduct__moduleName--ShgWs");
    By checkBoxByLocator = By.className("UpdateProduct__checkbox--3nHoz");
    By checkBoxLabelByLocator = By.className("UpdateProduct__checkmark--2FwaE");
    By generate2DDrawingImageByLocator = By.xpath("//a[contains(text(),'Generate 2D Drawing Images')]");

    By moduleDescriptionByLocator = By.cssSelector(".UpdateProduct__moduleDesc--2OEtV");

    public void clickExportButton() {
        String exportButtonSelector = "div.right > div.export"; // CSS Selector for the export button
        WebElement exportButton = driver.findElement(By.cssSelector(exportButtonSelector));
        exportButton.click();
    }

    // Function to click the "Skip and Continue" button
    public void clickSkipAndContinueButton() {
        String skipAndContinueXPath = "//div[@class='close-btn' and text()='Skip And Continue']"; // XPath for the button
        WebElement skipAndContinueButton = driver.findElement(By.xpath(skipAndContinueXPath));
        skipAndContinueButton.click();
    }

    public boolean checkPlacementStatusAvailability() {
        return placementStatusLocator.isDisplayed();
    }

    public void clickOnActions() throws InterruptedException {
        Thread.sleep(5000);
        WebElement actionOption = wait.until(ExpectedConditions.elementToBeClickable(actionsLocator));
        actionOption.click();
    }
    public void clickGenerate2DImage() throws InterruptedException {
        WebElement generate2DImageOption = wait.until(ExpectedConditions.elementToBeClickable(generate2DDrawingImageByLocator));
        generate2DImageOption.click();
    }

    public void check2DGenerationMessage() throws InterruptedException {
        try {
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), '2D Drawing Generated Successfully!')]")));
            if (successMessage != null) {
                System.out.println("2D Drawing Generated Successfully!");
                return;
            }
        } catch (TimeoutException e) {
            System.out.println("2d generation not Successfull.");
            System.out.println("checking for error");
        }
        Thread.sleep(5000);
        try {
            WebElement errorMessageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".Topbar__bodyModal--38C_0.modal-body")));
            String errorMessage = errorMessageElement.getText();
            System.out.println(errorMessage);
            Assert.assertTrue(errorMessage.contains("Your 2D Drawing generation has failed due to a technical glitch or poor internet connectivity!"), "Error message text does not match the expected text");
        } catch (TimeoutException e) {
            Assert.fail("Neither success nor error message was found");
        }
    }


    public void clickOnCameraView() {
        cameraViewLocator.click();
    }

    public void clickOnAddModuleDropDown() {
        wait.until(ExpectedConditions.visibilityOf(addModuleBtnLocator)).click();
    }

    public void selectModuleFromDropDown() {
        wait.until(ExpectedConditions.elementToBeClickable(moduleDropDownOptionLocator)).click();
    }

    public void clickOnAddToSpace() {
        wait.until(ExpectedConditions.visibilityOf(addToSpaceLocator)).click();

    }

    public void clickOnSaveAndApplyButton() {
        wait.until(ExpectedConditions.visibilityOf(saveAndApplyButtonLocator)).click();
    }

    public void clickOnAnyItemPresentInsideRoomInCanvas() {
        actions
                .moveToElement(canvasLocator)
                .click()
                .perform();
    }

    public void clickOnEdit() {
        editLocatorLocator.click();
    }

    public void selectAnyProductTabByChoice(String choice) {
        try {
            String xpathExpression = String.format("//div[contains(@class, 'UpdateProduct__updateProductTab--2l3aM') and text()='%s']", choice);
            WebElement tab = driver.findElement(By.xpath(xpathExpression));
            tab.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("An error occurred while trying to click the Product tab: " + choice);
        }
    }

    public void clickMaterialTypeByChoice(String materialchoice) {
        try {
            String xpathExpression = String.format("//div[contains(@class, 'UpdateProduct__updateMaterialOption--3UYuH') and text()='%s']", materialchoice);
            WebElement materialOption = driver.findElement(By.xpath(xpathExpression));
            materialOption.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("An error occurred while trying to click the material option: " + materialchoice);
        }
    }

    public void selectModuleCheckbox(String moduleName) {
        if (moduleName != null && !moduleName.isEmpty()) {
            for (WebElement module : modulesListLocator) {
                WebElement nameElement = module.findElement(moduleNameByLocator);
                if (nameElement.getText().equals(moduleName)) {
                    WebElement checkbox = module.findElement(checkBoxByLocator);
                    if (!checkbox.isSelected()) {
                        WebElement checkboxLabel = module.findElement(checkBoxLabelByLocator);
                        checkboxLabel.click();
                    }
                    return;
                }
            }
        } else {
            if (!modulesListLocator.isEmpty()) {
                WebElement firstModule = modulesListLocator.get(0);
                WebElement checkbox = firstModule.findElement(checkBoxByLocator);
                if (!checkbox.isSelected()) {
                    WebElement checkboxLabel = firstModule.findElement(checkBoxLabelByLocator);
                    checkboxLabel.click();
                }
            }
        }
    }

    public void selectMaterialTypeOptionByVisibleText(String visibleText) {
        Select dropdown = new Select(materialTypeDropDownLocator);
        List<WebElement> options = dropdown.getOptions();
        if (visibleText == null || visibleText.isEmpty()) {
            if (!options.isEmpty()) {
                dropdown.selectByVisibleText(options.get(2).getText()); //index 1 is select option.
            }
        } else {
            dropdown.selectByVisibleText(visibleText);
        }
    }

    public void clickHereButtonForColor() {
        wait.until(ExpectedConditions.elementToBeClickable(clickHereLocator)).click();
    }

    public void clickOnColorOptionByChoice(String colorName) {
        WebElement colorOption = driver.findElement(By.xpath("//div[@title='" + colorName + "']"));
        colorOption.click();
    }

    public void selectCameraView(String cameraView) {
        cameraView = cameraView.toLowerCase().trim();
        String xpath = "";
        switch (cameraView) {
            case "wall a":
                xpath = "//span[text()='wall A']";
                break;
            case "wall b":
                xpath = "//span[text()='wall B']";
                break;
            case "wall c":
                xpath = "//span[text()='wall C']";
                break;
            case "wall d":
                xpath = "//span[text()='wall D']";
                break;
            case "ceil":
                xpath = "//span[text()='ceil']";
                break;
            case "floor":
                xpath = "//span[text()='floor']";
                break;
            case "free":
                xpath = "//span[text()='free']";
                break;
            default:
                System.out.println("Invalid choice: " + cameraView);
                return;
        }
        WebElement option = driver.findElement(By.xpath(xpath));
        option.click();
    }

    public void selectModule(String choice) {
        if (choice != null && !choice.isEmpty()) {
            for (WebElement moduleUnit : moduleUnitsListLocator) {
                WebElement moduleDesc = moduleUnit.findElement(moduleDescriptionByLocator);
                String moduleTitle = moduleDesc.getAttribute("title");
                String moduleText = moduleDesc.getText();
//                System.out.println(moduleTitle + "  " + moduleText);

                if (choice.equals(moduleTitle) || choice.equals(moduleText)) {
                    actions.moveToElement(moduleUnit).click().perform();
                    return;
                }
            }
        } else {
            if (!moduleUnitsListLocator.isEmpty()) {
                moduleUnitsListLocator.get(0).click();
            } else {
                System.out.println("No modules available.");
            }
        }
    }


}
