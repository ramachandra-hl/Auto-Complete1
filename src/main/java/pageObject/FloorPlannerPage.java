package pageObject;

import configurator.UIBaseClass;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import services.RoomService;
import services.UrlService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static configurator.ApiService.buildAuthorizationHeader;
import static configurator.TestConfig.projectID;


public class FloorPlannerPage extends UIBaseClass {
    public WebDriver driver;
    WebDriverWait wait;
    Actions actions;
public FloorPlannerPage(){};
    public FloorPlannerPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        actions = new Actions(driver);
    }

    @FindBy(xpath = "//div[contains(text(),'Rooms')]")
    WebElement RoomsLocator;

    @FindBy(xpath = "//span[@class='Rooms__projectName--k_LP6']")
    public List<WebElement> roomListItemsLocator;

    @FindBy(xpath = "//span[@class='Topbar__roomName--5NKvS']")
    WebElement addedCartOptionLocator;

    @FindBy(id = "3DSwitch")
    WebElement switchTo3dLocator;
    @FindBy(xpath = "//div[contains(text(),'Gallery')]")
    WebElement gallaryLocator;

    @FindBy(xpath = "//span[normalize-space()='Manual Screenshots']")
    WebElement manualScreenshotsLocator;

    @FindBy(className = "GalleryView__galleyImage--1c52R")
    List<WebElement> totalScreenshotsListLocator;

    @FindBy(xpath = "//span[@id='capture']")
    WebElement captureLocator;

    @FindBy(xpath = "//span[normalize-space()='Scene']")
    WebElement sceneLocator;

    @FindBy(xpath = "//span[@class='Sidebar__headerIco--1lZyt']")
    WebElement openedRoomCrossBtnLocator;

    @FindBy(xpath = "//div[contains(@class, 'TopBar__tool--xv6vc') and .//*[contains(@id, 'Furniture')]]/*[name()='svg']")
    WebElement furnitureLinkLocator;

    @FindBy(xpath = "//div[contains(@class, 'TopBar__tool--xv6vc') and span[@id='Services']]/*[name()='svg']")
    WebElement servicesLinkLocator;

    @FindBy(xpath = "//div[contains(text(),'Fitted Furniture')]")
    WebElement fittedFurnitureLocator;
    @FindBy(xpath = "//div[contains(text(),'Loose Furniture')]")
    WebElement LooseFurnitureLocator;
    @FindBy(xpath = "//div[@class='Topbar__totalPrice--1W2h6']//*[name()='svg']") WebElement errorImgLocator;
    @FindBy(xpath = "//div[contains(text(),'Beds')]")
    WebElement bedsLocator;
    @FindBy(xpath = "//div[contains(text(),'All Beds')]")
    WebElement allBedsLocator;
    @FindBy(xpath = "//div[contains(text(),'Storage')]")
    WebElement storageLocator;
    @FindBy(xpath = "//div[contains(text(),'Study Unit')]")
    WebElement studyUnitLocator;
    @FindBy(className = "ProductList__cardWrapperProduct--VpD71")
    List<WebElement> studyUnitListsLocator;
    @FindBy(className = "SidebarCatalog__hdsDataWrapper--141da")
    List<WebElement> servicesItemListsLocator;
    By bedProductWrapperClassByLocator = By.className("common__cardWrapperProduct--3M-vq");
    By bedAddToRoomButtonClassByLocator = By.className("common__zoneAddToRoom--hKYqF");
    By bedTitleClassByLocator = By.className("common__cardTitle--2WZxw");
    By AddToQuoteInServiceByLocator = By.cssSelector(".productInfo__ctaButton--ykwdD");
    By addToQuoteByLocator = By.cssSelector(".common__addButton--1ZLrJ");
    By studyItemNameByLocator = By.className("ProductList__cardTitle--hl9QT");
    By studyItemAddToRoomByLocator = By.className("ProductList__zoneAddToRoom--2CrTL");
    By modalAlertByLocator = By.className("modal-dialog");
    By modalCloseByLocator = By.cssSelector(".modal-header .close span[aria-hidden='true']");
    By serviceNameByLocator = By.className("SidebarCatalog__serviceName--1zWkm");
    By serviceAddButtonByLocator = By.className("SidebarCatalog__serviceAddBtn--3P3h2");

    public void clickRoomCrossButton() {
        openedRoomCrossBtnLocator.click();
    }

    public void clickOnFurniture() {
        furnitureLinkLocator.click();
    }

    public void clickOnServices() {
        servicesLinkLocator.click();
    }

    public void clickOnLooseFurniture() {
        LooseFurnitureLocator.click();
    }

    public void clickOnBedThenAllBedTypes() {
        bedsLocator.click();
        allBedsLocator.click();
    }
 public boolean checkErrorImg(){
        return errorImgLocator.isDisplayed();
 }
    public void clickOnFittedFurniture() {
        fittedFurnitureLocator.click();
    }

    public void clickOnStorageItemInFittedFurniture() {
        storageLocator.click();
    }

    public void clickOnStudyUnitInStorage() {
        studyUnitLocator.click();
    }

    public void selectAnyStudyUnit(int noOfItem) {
        int noofItemToSelect = noOfItem;
        for (WebElement studyItem : studyUnitListsLocator) {
            while (noofItemToSelect != 0) {
                actions.moveToElement(studyItem).perform();
                String name = studyItem.findElement(studyItemNameByLocator).getText();
                System.out.println("name of furniture adding in the cart : " + name);
                WebElement addToRoomButton = studyItem.findElement(studyItemAddToRoomByLocator);
                addToRoomButton.click();
                try {
                    WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalAlertByLocator));
                    WebElement closeButton = modal.findElement(modalCloseByLocator);
                    closeButton.click();
                    wait.until(ExpectedConditions.invisibilityOf(modal));
                } catch (Exception e) {
                    System.out.println("No modal appeared for this element.");
                }
                noofItemToSelect--;
            }
        }
    }

    public void selectFirstBedUnitAndAddToCart() {
        List<WebElement> productList = driver.findElements(bedProductWrapperClassByLocator);
        WebElement product = productList.get(0);
        actions.moveToElement(product).perform();
        WebElement titleElement = product.findElement(bedTitleClassByLocator);
        String title = titleElement.getText();
        System.out.println("loose furniture adding name :" + title);
        WebElement addToRoomButton = product.findElement(bedAddToRoomButtonClassByLocator);
        addToRoomButton.click();

        WebElement addToQuoteButton = wait.until(ExpectedConditions.visibilityOfElementLocated(addToQuoteByLocator));
        addToQuoteButton.click();
    }
    public void clickOnRooms() {
        RoomsLocator.click();
    }

    public void selectAnyRoomsIfPresent(String room) {
        if (!roomListItemsLocator.isEmpty()) {
            for (WebElement item : roomListItemsLocator) {
                String text = item.getText();
                if (text.equals(room)) {
                    item.click();
                    break;
                }
            }
        }
    }

    public String getSelectedRoomFromCartLocation() {
        WebElement roomText = wait.until(ExpectedConditions.visibilityOf(addedCartOptionLocator));
        return roomText.getText();
    }

    public void clickTo3d() {
        switchTo3dLocator.click();
    }


    public void openGallary() {
        gallaryLocator.click();
    }

    public void clickToManualScreenshot() {
        manualScreenshotsLocator.click();
    }

    public int getTotalCountOfScreenshots() {
        return totalScreenshotsListLocator.size();
    }

    public void clickToCapture() {
        captureLocator.click();
    }

    public void clickToScene() {
        sceneLocator.click();
    }

    public void clickServicesByText(String serviceName) {
        String serviceNameLocator = String.format("//div[contains(text(),'%s')]", serviceName);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(serviceNameLocator))).click();
    }

    public void selectSubService(String subService) {
        String xpathExpression = String.format("//li[normalize-space()='%s']", subService);
        WebElement subServiceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpression)));
        subServiceElement.click();
    }

    public void selectFirstServiceItem() {
        if (servicesItemListsLocator.isEmpty()) {
            System.out.println("No service items found.");
            return;
        }
        WebElement firstServiceItem = servicesItemListsLocator.get(0);
        WebElement serviceNameElement = firstServiceItem.findElement(serviceNameByLocator);
        String serviceName = serviceNameElement.getText();
        System.out.println("Service item name: " + serviceName);
        WebElement addButton = firstServiceItem.findElement(serviceAddButtonByLocator);
        actions.moveToElement(addButton).click().perform();
        WebElement addToQuoteButton = wait.until(ExpectedConditions.visibilityOfElementLocated(AddToQuoteInServiceByLocator));
        addToQuoteButton.click();
    }

    public List<Map<String, String>>  getRoomDetails(){
        RoomService roomService = new RoomService();
        Map<String, String> token = buildAuthorizationHeader(new UrlService().getSpaceCraftToken());
        Response projectRes =  roomService.getProjectDetails(projectID,token);
        List<Map<String, Object>> rooms = (List<Map<String, Object>>) projectRes.jsonPath().getList("floors.rooms").get(0);
        List<Map<String, String>> roomsData = new ArrayList<>();

        for (Map<String, Object> data : rooms){
            Map<String, String> roomData = new HashMap<>();
            roomData.put("id",data.get("id").toString());
            roomData.put("name",data.get("name").toString());
            roomsData.add(roomData);
        }
        System.out.println(roomsData);
     return roomsData;
    }
}

