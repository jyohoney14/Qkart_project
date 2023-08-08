package QKART_TESTNG;

import QKART_TESTNG.pages.Checkout;
import QKART_TESTNG.pages.Home;
import QKART_TESTNG.pages.Login;
import QKART_TESTNG.pages.Register;
import QKART_TESTNG.pages.SearchResult;

import static org.testng.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.annotations.Test;

public class QKART_Tests {

    static RemoteWebDriver driver;
    public static String lastGeneratedUserName;

     @BeforeSuite(alwaysRun=true)
    public static void createDriver() throws MalformedURLException {
        // Launch Browser using Zalenium
        final DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(BrowserType.CHROME);
        driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
        System.out.println("createDriver()");
    }

    /*
     * Testcase01: Verify a new user can successfully register
     */
         @Test(description = "verify registration happens correctly" , priority = 1, groups={"Sanity_test"})
         @Parameters({"TestCase01_un" , "TestCase01_pwd"})
         public void TestCase01(@Optional("testUser") String username, @Optional("abc@123") String password) throws InterruptedException {
        Boolean status;
        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
         status = registration.registerUser(username, password, true);
        assertTrue(status, "Failed to register new user");
        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;
        // Visit the login page and login with the previuosly registered user
        Login login = new Login(driver);
        login.navigateToLoginPage();
         status = login.PerformLogin(lastGeneratedUserName, password);
        // assertTrue(status, "Failed to login with registered user");
        // Visit the home page and log out the logged in user
        Home home = new Home(driver);
        status = home.PerformLogout();
        //  takeScreenshot(driver, "EndTestCase", "TestCase1");
    }

    @Test(description = "verify re-registering an already existed user fails" , priority = 2, groups={"Sanity_test"})
    @Parameters({"TestCase01_un" , "TestCase01_pwd"})
    public void TestCase02(@Optional("testUser") String username, @Optional("abc@123") String password) throws InterruptedException {
        Boolean status;
        //  takeScreenshot(driver, "StartTestCase", "TestCase1");
        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
         status = registration.registerUser(username, password, true);
        assertTrue(status, "TestCase2: Failed to verify user Registration");
        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;
      // Visit the Registration page and try to register using the previously
        // registered user's credentials
        registration.navigateToRegisterPage();
        status = registration.registerUser(lastGeneratedUserName, password, false);
        assertFalse(status, "TestCase2: Failed to verify user Registration");
        //  takeScreenshot(driver, "EndTestCase", "TestCase2");
    }

    @Test(description = "verify the functionality of search box" , priority = 3, groups={"Sanity_test"})
    @Parameters({"TestCase03_product name to search for"})
    public void TestCase03(String Product) throws InterruptedException {
        Boolean status;
        // Visit the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();
        // Search for the "yonex" product
        status = homePage.searchForProduct(Product);
        assertTrue(status, "TestCase3: Failed to verify user Registration");
        // Fetch the search results
        List<WebElement> searchResults = homePage.getSearchResults();
        assertTrue(searchResults.size()!=0, "Test Case Failure. There were no results for the given search string");
        // Verify the search results are available
            for (WebElement webElement : searchResults) {
            // Create a SearchResult object from the parent element
            SearchResult resultelement = new SearchResult(webElement);
            // Verify that all results contain the searched text
            String elementText = resultelement.getTitleofResult();
            assertTrue(elementText.toUpperCase().contains(Product), "Test Case Failure. Test Results contains un-expected values");
        }
        // Search for product
        status = homePage.searchForProduct("Gesundheit");
        assertFalse(status, "TestCase3:Test Case Failure, Invalid keyword returned results");
        // Verify no search results are found
        searchResults = homePage.getSearchResults();
       assertTrue(searchResults.size() == 0&&homePage.isNoResultFound(),"Test Case Failure. Expected: no results , actual: Results were available");
         }

    @Test(description = "verify that existence of size chart for certain items and validate contents of size chart" , priority = 4, groups={"Regression_test"})
    @Parameters({"TestCase04"})
       public void TestCase04(@Optional("Roadster") String product) throws InterruptedException {
        boolean status = false;
        // Visit home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();
        // Search for product and get card content element of search results
        status = homePage.searchForProduct(product);
        List<WebElement> searchResults = homePage.getSearchResults();
        // Create expected values
        List<String> expectedTableHeaders = Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
        List<List<String>> expectedTableBody = Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                Arrays.asList("7", "7", "41", "10.2"), Arrays.asList("8", "8", "42", "10.6"),
                Arrays.asList("9", "9", "43", "11"), Arrays.asList("10", "10", "44", "11.5"),
                Arrays.asList("11", "11", "45", "12.2"), Arrays.asList("12", "12", "46", "12.6"));
        // Verify size chart presence and content matching for each search result
            for (WebElement webElement : searchResults) {
            SearchResult result = new SearchResult(webElement);
         // Verify if the size chart exists for the search result
            assertTrue(result.verifySizeChartExists(), "size chart is not present");
            status = result.verifyExistenceofSizeDropdown(driver);
            assertTrue(status, "sizechart drop down is not present");
              // Open the size chart
                status= result.openSizechart();
                Thread.sleep(2000);
               Assert.assertTrue(status,"unable to open sizechart");
               status=result.validateSizeChartContents(expectedTableHeaders, expectedTableBody, driver);
               Thread.sleep(2000);
               Assert.assertTrue(status,"unable to validate sizechart contents");

                    // Close the size chart modal
                   status = result.closeSizeChart(driver);
    }
    }

    @Test(description = "verify that a new user can add multiple products into the cart and checkout" , priority = 5, groups={"Sanity_test"})
    @Parameters({"TestCase05_produ" , "TestCase05_produ2" ,"TestCase05_addr"})
    public void TestCase05(String product, String product2, String address) throws InterruptedException {
        Boolean status;
        // Go to the Register page
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        // Register a new user
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Test Case Failure. Happy Flow Test Failed");
        // Save the username of the newly registered user
        lastGeneratedUserName = registration.lastGeneratedUsername;
        // Go to the login page
        Login login = new Login(driver);
        login.navigateToLoginPage();
        // Login with the newly registered user's credentials
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "Failed to register new user");
        // Go to the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();
        // Find required products by searching and add them to the user's cart
        status = homePage.searchForProduct(product);
        homePage.addProductToCart("YONEX Smash Badminton Racquet");
        status = homePage.searchForProduct(product2);
        homePage.addProductToCart("Tan Leatherette Weekender Duffle");
        // Click on the checkout button
        homePage.clickCheckout();  
        // Thread.sleep(2000);
        // List<WebElement> checkOutProduct = driver.findElements(By.xpath("//div[@class='MuiBox-root css-1gjj37g']/div[1]"));  
        // ArrayList<String> list = new ArrayList<>();
        // list.add("YONEX Smash Badminton Racquet");
        // list.add("Tan Leatherette Weekender Duffle");
        // for(int i=0;i<checkOutProduct.size();i++)
        // {
        //     WebElement ele = checkOutProduct.get(i);
        //     Assert.assertEquals(ele.getText(), list.get(i), "Product not present");
        // }
        // Add a new address on the Checkout page and select it
        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(address);
        checkoutPage.selectAddress(address);
        // Place the order
        checkoutPage.placeOrder();
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
        // Check if placing order redirected to the Thansk page
        status = driver.getCurrentUrl().endsWith("/thanks");
        Assert.assertTrue(status, "unable to redirected to success page");
        // Go to the home page
        homePage.navigateToHome();
        // Log out the user
        homePage.PerformLogout();
         }

         @Test(description = "verify that the contents of the cart can be edited" , priority = 6, groups={"Regression_test"})
         @Parameters({"TestCase06_produ" , "TestCase06_produ2"})
         public void TestCase06(String product, String product2) throws InterruptedException {
            Boolean status;
            Home homePage = new Home(driver);
            Register registration = new Register(driver);
            Login login = new Login(driver);
    
            registration.navigateToRegisterPage();
            status = registration.registerUser("testUser", "abc@123", true);
            assertTrue(status,"User Perform Register Failed");
            lastGeneratedUserName = registration.lastGeneratedUsername;
    
            login.navigateToLoginPage();
            status = login.PerformLogin(lastGeneratedUserName, "abc@123");
           assertTrue(status,"User Perform Login Failed");
    
            homePage.navigateToHome();
            status = homePage.searchForProduct(product);
            homePage.addProductToCart("Xtend Smart Watch");
    
            status = homePage.searchForProduct(product2);
            homePage.addProductToCart("Yarine Floor Lamp");
    
            // update watch quantity to 2
            homePage.changeProductQuantityinCart("Xtend Smart Watch", 2);
    
            // update table lamp quantity to 0
            homePage.changeProductQuantityinCart("Yarine Floor Lamp", 0);
    
            // update watch quantity again to 1
            homePage.changeProductQuantityinCart("Xtend Smart Watch", 1);
    
            homePage.clickCheckout();
            Checkout checkoutPage = new Checkout(driver);
            checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
            checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");
    
            checkoutPage.placeOrder();
    
            try {
                WebDriverWait wait = new WebDriverWait(driver, 30);
                wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
            } catch (TimeoutException e) {
                System.out.println("Error while placing order in: " + e.getMessage());
               
            }
    
            status = driver.getCurrentUrl().endsWith("/thanks");
            assertTrue(status, "Test Case 6: Verify that cart can be edited");
            homePage.navigateToHome();
            homePage.PerformLogout();
     }
     
     @Test(description = "verify that the contents made to the cart are saved againest user's login details" , priority = 7, groups={"Regression_test"})
     @Parameters({"TestCase07_produ" , "TestCase07_produ2"})
     public void TestCase07(String product, String product2) throws InterruptedException {
        Boolean status = false;
        List<String> expectedResult = Arrays.asList("Stylecon 9 Seater RHS Sofa Set ",
                "Xtend Smart Watch");

       // logStatus("Start TestCase", "Test Case 7: Verify that cart contents are persisted after logout", "DONE");

        Register registration = new Register(driver);
        Login login = new Login(driver);
        Home homePage = new Home(driver);

        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status,"User Perform Login Failed");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     logStatus("End TestCase", "Test Case 7:  Verify that cart contents are persited after logout: ",
        //             status ? "PASS" : "FAIL");
        //     return false;
        // }
        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status,"User Perform Login Failed,Test Case 7:  Verify that cart contents are persited after logout:");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     logStatus("End TestCase", "Test Case 7:  Verify that cart contents are persited after logout: ",
        //             status ? "PASS" : "FAIL");
        //     return false;
        // }

        homePage.navigateToHome();
        status = homePage.searchForProduct(product);
        homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set ");

        status = homePage.searchForProduct(product2);
        homePage.addProductToCart("Xtend Smart Watch");

        homePage.PerformLogout();

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");

        status = homePage.verifyCartContents(expectedResult);
        assertTrue(status,"Test Case 7: Verify that cart contents are persisted after logout:");
        // logStatus("End TestCase", "Test Case 7: Verify that cart contents are persisted after logout: ",
        //         status ? "PASS" : "FAIL");

        // homePage.PerformLogout();
        // return status;
    }

    @Test(description = "verify that insufficient balance error is thrown when wallet balance is not enough" , priority = 8, groups={"Sanity_test"})
    @Parameters({"TestCase08_prod","TestCase08_quant"})
    public void TestCase08(String product, String quantity) throws InterruptedException {
        Boolean status;
        // logStatus("Start TestCase",
        //         "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough",
        //         "DONE");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status,"User Perform Registration Failed");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Registration Failed", status ? "PASS" : "FAIL");
        //     logStatus("End TestCase",
        //             "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough: ",
        //             status ? "PASS" : "FAIL");
        //     return false;
        // }
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status,"User Perform Login Failed,Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough:");

        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     logStatus("End TestCase",
        //             "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough: ",
        //             status ? "PASS" : "FAIL");
        //     return false;
        // }

        Home homePage = new Home(driver);
        homePage.navigateToHome();
        status = homePage.searchForProduct(product);
        homePage.addProductToCart("Stylecon 9 Seater RHS Sofa Set ");

        homePage.changeProductQuantityinCart("Stylecon 9 Seater RHS Sofa Set ", Integer.parseInt(quantity));

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();
        Thread.sleep(3000);

        status = checkoutPage.verifyInsufficientBalanceMessage();
        assertTrue(status,"End TestCase,Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough:");
        // logStatus("End TestCase",
        //         "Test Case 8: Verify that insufficient balance error is thrown when the wallet balance is not enough: ",
        //         status ? "PASS" : "FAIL");

        // return status;
    }

    @Test(dependsOnMethods ="TestCase10",description = "verify that a product added to the cart is available when a new tab is added" , priority =10, groups={"Regression_test"})
    public void TestCase09() throws InterruptedException {
        Boolean status = false;

        // logStatus("Start TestCase",
        //         "Test Case 9: Verify that product added to cart is available when a new tab is opened",
        //         "DONE");
        // takeScreenshot(driver, "StartTestCase", "TestCase09");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status,"Test Case Failure. Verify that product added to cart is available when a new tab is opened");
        // if (!status) {
        //     logStatus("TestCase 9",
        //             "Test Case Failure. Verify that product added to cart is available when a new tab is opened",
        //             "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase09");
        // }
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status,"User Perform Login Failed");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase9");
        //     logStatus("End TestCase",
        //             "Test Case 9:   Verify that product added to cart is available when a new tab is opened",
        //             status ? "PASS" : "FAIL");
        // }

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct("YONEX");
        homePage.addProductToCart("YONEX Smash Badminton Racquet");

        String currentURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

        driver.get(currentURL);
        Thread.sleep(2000);

        List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
        status = homePage.verifyCartContents(expectedResult);

        driver.close();

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
        assertTrue(status,"Test Case 9: Verify that product added to cart is available when a new tab is opened");
        // logStatus("End TestCase",
        // "Test Case 9: Verify that product added to cart is available when a new tab is opened",
        // status ? "PASS" : "FAIL");
        // takeScreenshot(driver, "EndTestCase", "TestCase09");
        //return status;
    }
    
    @Test(description = "verify that the privacy policy and aboutus links are working fine" , priority = 9, groups={"Regression_test"})
    public void TestCase10() throws InterruptedException {
        Boolean status = false;

        // logStatus("Start TestCase",
        //         "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
        //         "DONE");
        // takeScreenshot(driver, "StartTestCase", "TestCase10");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status,"Test Case Failure.Verify that the Privacy Policy, About Us are displayed correctly");
        // if (!status) {
        //     logStatus("TestCase 10",
        //             "Test Case Failure.  Verify that the Privacy Policy, About Us are displayed correctly ",
        //             "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase10");
        // }
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status,"User Perform Login Failed --> Test Case 10:    Verify that the Privacy Policy, About Us are displayed correctly");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase10");
        //     logStatus("End TestCase",
        //             "Test Case 10:    Verify that the Privacy Policy, About Us are displayed correctly ",
        //             status ? "PASS" : "FAIL");
        // }

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        String basePageURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        status = driver.getCurrentUrl().equals(basePageURL);
        assertTrue(status, "Verifying parent page url didn't change on privacy policy link click failed --> Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly");
        // if (!status) {
        //     logStatus("Step Failure", "Verifying parent page url didn't change on privacy policy link click failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase10");
        //     logStatus("End TestCase",
        //             "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
        //             status ? "PASS" : "FAIL");
        // }

        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
        WebElement PrivacyPolicyHeading = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        status = PrivacyPolicyHeading.getText().equals("Privacy Policy");
        assertTrue(status, "Verifying new tab opened has Privacy Policy page heading failed --> Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly");
        // if (!status) {
        //     logStatus("Step Failure", "Verifying new tab opened has Privacy Policy page heading failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase10");
        //     logStatus("End TestCase",
        //             "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
        //             status ? "PASS" : "FAIL");
        // }

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
        driver.findElement(By.linkText("Terms of Service")).click();

        handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
        WebElement TOSHeading = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/h2"));
        status = TOSHeading.getText().equals("Terms of Service");
        assertTrue(status, "Verifying new tab opened has Terms Of Service page heading failed --> Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly");
        // if (!status) {
        //     logStatus("Step Failure", "Verifying new tab opened has Terms Of Service page heading failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase10");
        //     logStatus("End TestCase",
        //             "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
        //             status ? "PASS" : "FAIL");
        // }

        driver.close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        // logStatus("End TestCase",
        // "Test Case 10: Verify that the Privacy Policy, About Us are displayed correctly ",
        // "PASS");
        // takeScreenshot(driver, "EndTestCase", "TestCase10");

        // return status;
    }
    
    @Test(description = "verify that cotactus dailog box working fine" , priority = 11, groups={"Regression_test"})
    @Parameters({"TestCase11_userName","TestCase11_Email" ,"TestCase11_QueryContent"})
    public void TestCase11(String username, String Email ,String query) throws InterruptedException {
        // logStatus("Start TestCase",
        //         "Test Case 11: Verify that contact us option is working correctly ",
        //         "DONE");
        // takeScreenshot(driver, "StartTestCase", "TestCase11");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        driver.findElement(By.xpath("//*[text()='Contact us']")).click();

        WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
        name.sendKeys(username);
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys(Email);
        WebElement message = driver.findElement(By.xpath("//input[@placeholder='Message']"));
        message.sendKeys(query);

        WebElement contactUs = driver.findElement(
                By.xpath("/html/body/div[2]/div[3]/div/section/div/div/div/form/div/div/div[4]/div/button"));

        contactUs.click();

        WebDriverWait wait = new WebDriverWait(driver, 30);
       assertTrue(wait.until(ExpectedConditions.invisibilityOf(contactUs)));
        // logStatus("End TestCase",
        //         "Test Case 11: Verify that contact us option is working correctly ",
        //         "PASS");

        // takeScreenshot(driver, "EndTestCase", "TestCase11");

        // return true;
    }

    @Test(description = "ensure that the advertisement links in qkart page are clickable" , priority = 12, groups={"Sanity_test"})
    @Parameters({"TestCase12_product" , "TestCase12_Address"})
    public void TestCase12(String product, String address) throws InterruptedException {
        Boolean status = false;
        // logStatus("Start TestCase",
        //         "Test Case 12: Ensure that the links on the QKART advertisement are clickable",
        //         "DONE");
        // takeScreenshot(driver, "StartTestCase", "TestCase12");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        assertTrue(status, "Test Case Failure. Ensure that the links on the QKART advertisement are clickable");
        // if (!status) {
        //     logStatus("TestCase 12",
        //             "Test Case Failure. Ensure that the links on the QKART advertisement are clickable",
        //             "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase12");
        // }
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        assertTrue(status, "User Perform Login Failed --> Test Case 12:  Ensure that the links on the QKART advertisement are clickable");
        // if (!status) {
        //     logStatus("Step Failure", "User Perform Login Failed", status ? "PASS" : "FAIL");
        //     takeScreenshot(driver, "Failure", "TestCase 12");
        //     logStatus("End TestCase",
        //             "Test Case 12:  Ensure that the links on the QKART advertisement are clickable",
        //             status ? "PASS" : "FAIL");
        // }

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct(product);
        homePage.addProductToCart(product);
        homePage.changeProductQuantityinCart("YONEX Smash Badminton Racquet", 1);
        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(address);
        checkoutPage.selectAddress(address);
        checkoutPage.placeOrder();
        Thread.sleep(3000);

        String currentURL = driver.getCurrentUrl();

        List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

        status = Advertisements.size() == 3;
        //logStatus("Step ", "Verify that 3 Advertisements are available", status ? "PASS" : "FAIL");

        WebElement Advertisement1 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[1]"));
        driver.switchTo().frame(Advertisement1);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        status = !driver.getCurrentUrl().equals(currentURL);
       // logStatus("Step ", "Verify that Advertisement 1 is clickable ", status ? "PASS" : "FAIL");

        driver.get(currentURL);
        Thread.sleep(3000);

        WebElement Advertisement2 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div[2]/div/iframe[2]"));
        driver.switchTo().frame(Advertisement2);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        status = !driver.getCurrentUrl().equals(currentURL);
        assertTrue(status, "Ensure that the links on the QKART advertisement are not clickable");
        // logStatus("Step ", "Verify that Advertisement 2 is clickable ", status ? "PASS" : "FAIL");

        // logStatus("End TestCase",
        //         "Test Case 12:  Ensure that the links on the QKART advertisement are clickable",
        //         status ? "PASS" : "FAIL");
        // return status;
    }
    @AfterSuite
    public static void quitDriver() {
        System.out.println("quit()");
        driver.quit();
    }


    // public static void logStatus(String type, String message, String status) {

    //     System.out.println(String.format("%s |  %s  |  %s | %s", String.valueOf(java.time.LocalDateTime.now()), type,
    //             message, status));
    // }

    public static void takeScreenshot(String screenshotType, String description) {
        try {
            File theDir = new File("/screenshots");
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            String timestamp = String.valueOf(java.time.LocalDateTime.now());
            String fileName = String.format("screenshot_%s_%s_%s.png", timestamp, screenshotType, description);
            TakesScreenshot scrShot = ((TakesScreenshot) driver);
            File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
            File DestFile = new File("screenshots/" + fileName);
            FileUtils.copyFile(SrcFile, DestFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

