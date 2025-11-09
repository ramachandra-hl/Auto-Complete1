package pageObject;

import configurator.UIBaseClass;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.PropertiesReader;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SFHomePage extends UIBaseClass {
    WebDriver driver;
    WebDriverWait wait;

    PropertiesReader propertiesReader = new PropertiesReader(testData);
    public SFHomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(300));
    }

    @FindBy(xpath = "(//div[@class='slds-context-bar__label-action slds-p-left_none'])[3]")
    WebElement LeadsTabPreProd;

    @FindBy(xpath = "(//div[@class='slds-context-bar__label-action slds-p-left_none'])[2]")
    WebElement LeadsTabProd;



    @FindBy(xpath = "//div[contains(@class,'menuItemsWrapper')]//slot//one-app-nav-bar-menu-item[contains(@class,'slds-dropdown__item')]//a[@role='menuitem' and contains(@href,'recordtypeselect.jsp') and contains(., 'New Lead')]")
    WebElement LeadsTabDropdown;
    @FindBy(xpath = "//span[text()='DC Sales Manager']/ancestor::label//input[@type='radio']")
    WebElement DCSalesManagerRadioButton;
    @FindBy(xpath = "(//button[contains(@class,'slds-button slds-button_neutral slds-button slds-button_brand uiButton')])[1]")
    WebElement nextButton;
    @FindBy(xpath = "//div[@data-target-selection-name='sfdc:RecordField.Lead.Meeting_Type__c']//lightning-base-combobox[@class='slds-combobox_container']//div[@class='slds-combobox_container']")
    WebElement meetingTypeDropdown;

    @FindBy(xpath = "//div[@data-target-selection-name='sfdc:RecordField.Lead.Meeting_Venue__c']//button")
    WebElement meetingVenueDropdownButton;

    @FindBy(xpath = "//div[contains(@data-target-selection-name,'sfdc:RecordField.Lead.Approx_Budget__c')]//lightning-base-combobox[@class='slds-combobox_container']")
    WebElement approxBudgetDropdown;

    @FindBy(xpath = "//input[@name='Client_s_Budget__c']")
    WebElement clientBudgetInput;

    @FindBy(xpath = "//div[@data-target-selection-name='sfdc:RecordField.Lead.Willingness_For_Meeting__c']//div[@class='slds-form-element__control slds-input-has-icon slds-input-has-icon_right']")
    WebElement meetingDateInput;

    @FindBy(xpath = "//div[@data-target-selection-name='sfdc:RecordField.Lead.Willingness_For_Meeting__c']//lightning-base-combobox[@class='slds-combobox_container']")
    WebElement meetingTimeInput;

    @FindBy(xpath = "//div[@data-target-selection-name='sfdc:RecordField.Lead.Design_User__c']//span[@class='test-id__field-value slds-size_1-of-1']//div[@class='slds-form-element__control']")
    WebElement designUserDropdown;

    @FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='city']")
    WebElement cityInput;

    @FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='lastName']")
    WebElement lastNameInput;

    @FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='Email']")
    WebElement emailInput;

    @FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='MobilePhone']")
    WebElement mobilePhoneInput;

    @FindBy(xpath = "//button[@aria-label='View all dependencies for Channel']")
    WebElement viewAllDependenciesButton;

    @FindBy(xpath = "//button[normalize-space()='Apply']")
    WebElement applyButton;

    @FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//a[@role='tab' and contains(text(),'Details') and contains(@class,'slds-tabs_default__link')]")
    WebElement detailsTab;





    public void clickLeadsTab() {
        pageLoaded();
        if (propertiesReader.getEnvironment().equalsIgnoreCase("prod")) {
            wait.until(ExpectedConditions.elementToBeClickable(LeadsTabProd));
            LeadsTabProd.click();
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(LeadsTabPreProd));
            LeadsTabPreProd.click();
        }
        System.out.println("Leads tab is clicked.");
    }

    public void clickLeadsTabDropdown() {
        pageLoaded();
        pageLoaded(LeadsTabDropdown);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", LeadsTabDropdown);
        System.out.println("Leads tab dropdown is clicked.");
    }

    public void clickDCSalesManagerRadioButton() {
        pageLoaded(DCSalesManagerRadioButton);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", DCSalesManagerRadioButton);
        System.out.println("DCSalesManagerRadioButton is clicked.");
    }

    public void clickNextButton() {
        pageLoaded(nextButton);
        nextButton.click();
        System.out.println("Next button is clicked.");
    }

    public void clickMeetingTypeDropdown() {
        pageLoaded(meetingTypeDropdown);
        meetingTypeDropdown.click();
        System.out.println("Meeting Type dropdown is clicked.");
    }

    public void clickMeetingVenueDropdown() {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", meetingVenueDropdownButton);
        js.executeScript("arguments[0].click();", meetingVenueDropdownButton);
        System.out.println("Meeting Venue dropdown is clicked.");
        try {
            Thread.sleep(2000); // Adding sleep to wait for any potential loading or processing
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clickApproxBudgetDropdown() {
        pageLoaded(approxBudgetDropdown);
        approxBudgetDropdown.click();
        System.out.println("Approx Budget dropdown is clicked.");
    }

    public void selectDropdownOptionByText(String optionText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement option = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[normalize-space(text())='" + optionText + "']"))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", option);
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }


    public void enterClientBudget(String budget) {
        pageLoaded(designUserDropdown);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", designUserDropdown);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clientBudgetInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", clientBudgetInput, budget);
        System.out.println("Client Budget is entered: " + budget);
    }


    public void enterMeetingDate() {
        pageLoaded(meetingDateInput);
        LocalDate currentDate = LocalDate.now();
        String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); // or "dd/MM/yyyy"

        // Format the time 30 minutes ahead
        LocalTime futureTime = LocalTime.now().plusMinutes(5);
        String formattedTime = futureTime.format(DateTimeFormatter.ofPattern("hh:mm a")); // e.g., "03:30 PM"
        meetingDateInput.click();  // Activate the field
        meetingDateInput.sendKeys(formattedDate);
        System.out.println("Meeting date is entered: " + formattedDate);
        meetingTimeInput.click(); // Focus the field
        meetingTimeInput.sendKeys(Keys.chord(Keys.CONTROL, "a")); // Select all
        meetingTimeInput.sendKeys(Keys.DELETE);                   // Delete selected
        meetingTimeInput.sendKeys(formattedTime);
        System.out.println("Meeting time is entered: " + formattedTime);
    }

    public void clickDesignUserDropdown(String designUser) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Scroll into view and click the dropdown
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", designUserDropdown);
        wait.until(ExpectedConditions.elementToBeClickable(designUserDropdown)).click();


        // Input the search term
        WebElement inputBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@data-target-selection-name='sfdc:RecordField.Lead.Design_User__c']//input[contains(@class,'slds-combobox__input')]")
        ));
        inputBox.clear();
        inputBox.sendKeys(designUser);
        System.out.println("Typed Design User: " + designUser);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> options = driver.findElements(By.xpath("//lightning-base-combobox-item"));

        if (options.isEmpty()) {
            System.out.println("⚠️ No matching options available in dropdown for: " + designUser);
            return;
        }

        boolean found = false;
        for (WebElement option : options) {
            String text = option.getText().trim();
            if (text.equalsIgnoreCase(designUser)) {
                wait.until(ExpectedConditions.elementToBeClickable(option)).click();
                System.out.println("✅ Selected option: " + text);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("❌ Matching option not found. Available options:");
            for (WebElement option : options) {
                System.out.println(" - " + option.getText().trim());
            }
        }
    }

    public void enterCity(String city) {
        pageLoaded(cityInput);
        cityInput.sendKeys(city);
        System.out.println("City is entered: " + city);
    }

    public void enterLastName(String lastName) {
        pageLoaded(lastNameInput);
        lastNameInput.sendKeys(lastName);
        System.out.println("Last Name is entered: " + lastName);
    }

    public void enterEmail(String email) {
        pageLoaded(emailInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", emailInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", emailInput);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", emailInput, email);
        System.out.println("Client Budget is entered: " + email);
    }
    public void enterMobilePhone(String mobilePhone) {
        pageLoaded(mobilePhoneInput);
        mobilePhoneInput.click();
        mobilePhoneInput.sendKeys(mobilePhone);
        System.out.println("Mobile Phone is entered: " + mobilePhone);
    }

    public void clickViewAllDependenciesButton() {
        pageLoaded(viewAllDependenciesButton);

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", viewAllDependenciesButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewAllDependenciesButton);
        System.out.println("View All Dependencies button is clicked.");
    }

    public void clickChannelDropdown() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@name='Channel__c' and @role='combobox']")));
        dropdownButton.click();
        WebElement optionOnline = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@title='Online']")));
        optionOnline.click();
    }

    public void clickSource() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@name='Source__c' and @role='combobox']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
        WebElement optionOnline = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@title='Paid']")));
        optionOnline.click();
    }

    public void clickCampaignSource() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@name='DC_Campaign_Source__c' and @role='combobox']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
        WebElement optionOnline = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@title='Google AdWords']")));
        optionOnline.click();
    }

    public void clickLeadSource() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@name='DC_Lead_Source__c' and @role='combobox']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
        WebElement optionOnline = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[@title='Google Search Ads']")));
        optionOnline.click();
    }

    public void clickApplyButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement applyButtonElement = wait.until(ExpectedConditions.elementToBeClickable(applyButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyButtonElement);
        System.out.println("Apply button is clicked.");
    }

    public void clickSaveButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        List<WebElement> allSaveButtons = driver.findElements(By.xpath("//button[@name='SaveEdit' or text()='Save' or contains(@class,'slds-button_brand')]"));
        for (WebElement button : allSaveButtons) {
            System.out.println("Button text: " + button.getText());
            if (button.getText().equals("Save") && button.getAttribute("name").equals("SaveEdit")) {
                System.out.println("Matching Save button found: " + button.getText());
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                break;
            }
        }
        System.out.println("Save button clicked.");
    }


    public String clickRoasterLink() {
        pageLoaded();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        // Correct XPath concatenation with quotes around roasterBaseUrl
        WebElement roasterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href, '" + propertiesReader.getRoasterBaseUrl() + "')]")));

        String roasterLinkText = roasterLink.getText();
        System.out.println("Roaster link : " + roasterLinkText);
        String[] parts = roasterLinkText.split("/");
        String userId = parts[parts.length - 1];

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", roasterLink);
        System.out.println("Roaster link is clicked.");
        pageLoaded();
        return userId;
    }


    public  void switchToWindowByTitleOrUrl(String partialMatch) {
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
            String currentTitle = driver.getTitle();
            String currentUrl = driver.getCurrentUrl();

            if (currentTitle.contains(partialMatch) || currentUrl.contains(partialMatch)) {
                System.out.println("Switched to window: " + currentTitle);
            }
        }
        System.out.println("No window found with keyword: " + partialMatch);
    }

    public void clickOnDetailsTab() {
        pageLoaded(detailsTab);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", detailsTab);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", detailsTab);
        System.out.println("Details tab is clicked.");
    }

    private void pageLoaded() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void pageLoaded(WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Replace this with any element that confirms the page is loaded
        wait.until(ExpectedConditions.visibilityOf(element));
    }

}
