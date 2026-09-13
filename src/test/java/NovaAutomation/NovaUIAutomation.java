package NovaAutomation;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class NovaUIAutomation {



    private WebDriver driver;
    private WebDriverWait wait;
    
    // App URL
    private final String APP_URL = "https://novatools.org/";

    // UI Element Locators
    private final By signup = By.xpath("//span[text()='Sign up free!']");
    private final By login = By.xpath("//span[text()='Login']");
    private final By emailField = By.id("choose-email");
    private final By passwordField = By.id("choose-password");
    private final By verifyPass = By.id("verify-password");
    private final By clickBox = By.xpath("//input[@type='checkbox']");
    private final By loginButton = By.xpath("//button[@type='submit']");
    private final By checkDashboard = By.xpath("//div[div/text()='Workspace settings']");
    
    private final By AddSpace = By.xpath("//span[contains(.,'+')]");
    private final By enterSpaceName = By.id("spaces-add-name");
    private final By clickOnSpace = By.xpath("//div[text()='QA Assignment Project']");
    private final By clickProjectButton = By.xpath("//div[@data-testid='add-project-button']");
    private final By clickOnBlankProject = By.xpath("//*[local-name()='svg' and contains(@class, 'lucide lucide-plus')]");
    private final By projectNameInput = By.id("pj-name");
    private final By addGoals = By.xpath("//textarea[@placeholder='Goals...']");
    private final By clickOnCreateProject = By.xpath("//span[normalize-space()='Create project']");
    private final By activeProjectTile = By.xpath("//input[@aria-invalid='false']");
    
    private final By addTaskButton = By.xpath("//div[normalize-space()='+ Add task']");
    private final By createdTaskCard = By.xpath("//input[@placeholder='Write a title for this task']");

    private final By clickOnThreeDots = By.xpath("//div[contains(@class, 'MuiAvatar-root')]/following-sibling::div/following-sibling::*[local-name()='svg']");
    private final By clickOnLogout = By.xpath("//li[normalize-space()='Logout']");

    @BeforeClass
    public void setup() {
        // setup and configure Chrome Binary
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

    }

    // 1. Verify User Login Functionality
    @Test(priority = 1)
    public void testUserLogin() throws IOException {
        driver.get(APP_URL);
        
        // Wait and perform user login actions
        wait.until(ExpectedConditions.elementToBeClickable(login)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("abiduddina69@gmail.com");
        driver.findElement(passwordField).sendKeys("Password123!");
        // driver.findElement(verifyPass).sendKeys("Password123!");
        // driver.findElement(clickBox).click();
        driver.findElement(loginButton).click();
        
        // Assert successful login by checking the page dashboard
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(checkDashboard));
        Assert.assertTrue(header.isDisplayed(), "Dashboard verification failed: 'Workspace settings' element not displayed.");
        
        //Capture screenshot of success
        captureScreenshot("Passed_TestUserLogin");
    }

    // 2. Verify Project Creation Workflow
    @Test(priority = 2, dependsOnMethods = {"testUserLogin"})
    public void testCreateProject() throws IOException {

        // Wait and click space and project button
        wait.until(ExpectedConditions.elementToBeClickable(AddSpace)).click();
        WebElement spaceField = wait.until(ExpectedConditions.visibilityOfElementLocated(enterSpaceName));
        spaceField.sendKeys("QA Assignment Project", Keys.ENTER);
        wait.until(ExpectedConditions.elementToBeClickable(clickOnSpace)).click();
        wait.until(ExpectedConditions.elementToBeClickable(clickProjectButton)).click();//click project button.
        
        // Input text fields for creating project metadata
        wait.until(ExpectedConditions.visibilityOfElementLocated(clickOnBlankProject)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(projectNameInput)).sendKeys("QA Automation");
        wait.until(ExpectedConditions.visibilityOfElementLocated(addGoals)).sendKeys("Verify Core Functionalities");
        wait.until(ExpectedConditions.elementToBeClickable(clickOnCreateProject)).click();
        
        
        // Assert the newly created project is displayed on the board UI
        WebElement projectCard = wait.until(ExpectedConditions.visibilityOfElementLocated(activeProjectTile));
        Assert.assertTrue(projectCard.isDisplayed(), "Project tracking layout missing newly created project card.");
    
        //Capture evidence of success
        captureScreenshot("Passed_TestCreateProject");
    }

    // 3. Verify Task Creation Inside The Project Workflow
    @Test(priority = 3, dependsOnMethods = {"testCreateProject"})
    public void testCreateTaskWithinProject() throws IOException {

        // Click on the created project card to view item management board
        driver.findElement(activeProjectTile).click();
        
        // Click on add task button and Write Nova Project inside the project structure
        WebElement addTask = wait.until(ExpectedConditions.elementToBeClickable(addTaskButton));
        addTask.sendKeys(Keys.ENTER, "Nova Project", Keys.ENTER);
        
        // Assert task card displays on the view dashboard properly
        WebElement taskCard = wait.until(ExpectedConditions.visibilityOfElementLocated(createdTaskCard));
        Assert.assertNotNull(taskCard, "Task card creation fail.");
    
        //Capture evidence of success
        captureScreenshot("Passed_TestCreateTaskInsideProject");
    }

    // 4. Verify User Logout Functionality
    @Test(priority = 4, dependsOnMethods = {"testCreateTaskWithinProject"})
    public void testUserLogout() throws IOException {
        // Click on the three dots menu and select logout
        wait.until(ExpectedConditions.elementToBeClickable(clickOnThreeDots)).click();
        wait.until(ExpectedConditions.elementToBeClickable(clickOnLogout)).click();
        
        // Assert successful logout by checking the login button is displayed again
        WebElement loginButtonElement = wait.until(ExpectedConditions.visibilityOfElementLocated(login));
        Assert.assertTrue(loginButtonElement.isDisplayed(), "Logout verification failed");

        //Capture evidence of success
        captureScreenshot("Passed_TestUserLogout");
    }

    //TestNG Listener Method: Automatically executes immediately after each individual test method.
    //Captures a failure screenshot instantly if an assertion drops or an element timeout triggers.
    @AfterMethod
    public void checkTestStatus(ITestResult result) throws IOException {
        if(result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot("Failed_" + result.getName());
            System.out.println("Automation Failure Captured: " + result.getName());
        }
    }

    //Utility method helper to extract, format, and save raw driver context screenshots.
    private void captureScreenshot(String fileNamePrefix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        File evidenceDirectory = new File("C:\\Users\\abida\\OneDrive\\Desktop\\Abiduddin Ahmed - QA Assignment NOVA\\Test Evidence\\UI Automation");
        if (!evidenceDirectory.exists() && !evidenceDirectory.mkdirs()) {
            throw new IOException("Unable to create screenshot directory: " + evidenceDirectory.getAbsolutePath());
        }

        File destFile = new File(evidenceDirectory,
                "UI_AutomationClick." + fileNamePrefix + "_" + timeStamp + ".png");

        FileUtils.copyFile(srcFile, destFile);
        System.out.println("Screenshot saved: " + destFile.getAbsolutePath());
    }

    @AfterClass
    public void tearDown() {
        // Gracefully kill and close browser context instances
        if (driver != null) {
            driver.quit();
        }
    }
}


