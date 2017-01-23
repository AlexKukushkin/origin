package com.edifecs.qa;

/**
 * Created with IntelliJ IDEA.
 * User: satisuri
 * Date: 9/23/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
import cuanto.api.*;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CuantoService {

    private final Logger LOG = Logger.getLogger(CuantoService.class);

    final String url = "http://enclmcd01:8880/cuanto/";
    final String projectKey = "clm7011";
    final String applicationEnvironment = "CLM"

    public static CuantoConnector cuantoConnector;
    public static TestRun testRun;

    private TestOutcome testOutcome;


    public void initialize(ITestContext iTestContext){
        if (cuantoConnector == null){
            try{
                cuantoConnector = CuantoConnector.newInstance(url, projectKey);
                testRun = new TestRun(new Date());
                testRun.addTestProperty("Environment",applicationEnvironment);
                testRun.addTestProperty("Test Suite", iTestContext.getSuite().getName());
                cuantoConnector.addTestRun(testRun);

            }catch (Exception e){
                String message = String.format("Cuanto server with the url [%s] & project key [%s] is not available.",url,projectKey);
                LOG.info(message);

            }
        }
    }


    private String getStatusString(int status){
        switch(status){
            case ITestResult.FAILURE:
                return "FAIL";
            case ITestResult.SKIP:
                return "SKIP";
            case ITestResult.SUCCESS:
                return "PASS";
            default:
                return "?";
        }

    }

    public void presist(ITestResult iTestResult){
        try{
            testOutcome = TestOutcome.newInstance(iTestResult.getTestClass().getName(), iTestResult.getName()
                    , TestResult.valueOf(getStatusString(iTestResult.getStatus())));

            if (iTestResult.getThrowable()!= null){
                testOutcome.setTestOutput(iTestResult.getThrowable().getMessage());
            }

            testOutcome.setDuration(iTestResult.getEndMillis() - iTestResult.getStartMillis());

            for (int i=0; i< iTestResult.getMethod().getGroups().length;i++){
                testOutcome.addTag(iTestResult.getMethod().getGroups()[i]);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String startDate = dateFormat.format(new Date (iTestResult.getStartMillis()));
            String endDate = dateFormat.format(new Date (iTestResult.getEndMillis()));
            testOutcome.setStartedAt(dateFormat.parse(startDate));
            testOutcome.setFinishedAt(dateFormat.parse(endDate));

            String testDescription = iTestResult.getMethod().getDescription();

            if (testDescription!=null){
                if (testDescription.startsWith("DE")){
                    testOutcome.setBug(new Bug(iTestResult.getMethod().getDescription(),""));
                    testOutcome.setAnalysisState(AnalysisState.Bug);
                }

            }
            cuantoConnector.addTestOutcome(testOutcome,testRun);

        }catch (Exception e){
            // do nothing for now.
            LOG.debug("Not able to add test outcome:"+e);
        }

    }
}
