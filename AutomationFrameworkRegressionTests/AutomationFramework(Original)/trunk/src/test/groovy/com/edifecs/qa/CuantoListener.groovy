package com.edifecs.qa;

/**
 * Created with IntelliJ IDEA.
 * User: satisuri
 * Date: 9/23/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
import org.apache.log4j.Logger;
import com.edifecs.qa.CuantoService;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class CuantoListener implements ITestListener {

    private final Logger LOG = Logger.getLogger(CuantoListener.class);

    private CuantoService cuantoService;

    public CuantoListener(){
        cuantoService = new CuantoService();
    }


    public void onTestStart(ITestResult iTestResult) {
        //Invoked each time before a test will be invoked.

    }


    public void onTestSuccess(ITestResult iTestResult) {
        //Invoked each time a test succeeds.
        cuantoService.presist(iTestResult);

    }


    public void onTestFailure(ITestResult iTestResult) {
        //Invoked each time a test fails.
        cuantoService.presist(iTestResult);
    }


    public void onTestSkipped(ITestResult iTestResult) {
        //Invoked each time a test is skipped.
        cuantoService.presist(iTestResult);
    }


    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        //Invoked each time a method fails but has been annotated with successPercentage and this failure still keeps it within the success percentage requested.
    }


    public void onStart(ITestContext iTestContext) {
        cuantoService.initialize(iTestContext);
    }


    public void onFinish(ITestContext iTestContext) {
        //Invoked after all the tests have run and all their Configuration methods have been called.
    }
}
