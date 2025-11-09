package pageObject;

import configurator.UIBaseClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SFLoginPage extends UIBaseClass {
    WebDriverWait wait;
    Actions actions;

    public SFLoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(300));
    }

    @FindBy(id = "username")
    WebElement userNameField;

    @FindBy(id = "password")
    WebElement passwordField;

    @FindBy(id = "Login")
    WebElement loginButton;

    public void enterUsername(String username) {
        userNameField.sendKeys(username);
        System.out.println(username + " is entered in the username field.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void enterPassword(String password) {
        passwordField.sendKeys(password);
        System.out.println(password + " is entered in the password field.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clickLoginButton() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        loginButton.click();
    }

}
