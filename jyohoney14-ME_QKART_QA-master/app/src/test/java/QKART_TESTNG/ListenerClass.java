package QKART_TESTNG;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ListenerClass implements ITestListener{
    public void onTestStart(ITestResult result) {
        QKART_Tests.takeScreenshot("StartTestCase", result.getName());
             //System.out.println("New Test Started" +result.getName());
         }
    
        
         public void onTestSuccess(ITestResult result) {
            QKART_Tests.takeScreenshot("EndTestCase", result.getName());
              // System.out.println("onFinish method started");
         }

         public void onTestFailure(ITestResult result) {
            QKART_Tests.takeScreenshot("TestCaseFailed", result.getName());
           // System.out.println("Test Failed : "+ result.getName()+" Taking Screenshot ! ");
            }
}