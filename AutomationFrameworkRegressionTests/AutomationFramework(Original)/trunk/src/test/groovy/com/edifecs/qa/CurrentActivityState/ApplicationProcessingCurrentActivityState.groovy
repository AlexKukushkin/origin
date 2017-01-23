package com.edifecs.qa.CurrentActivityState

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: AnaL
 * Date: 16.01.2012
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
class ApplicationProcessingCurrentActivityState extends UtilsQuery {
    def currentTimeSQLFormat
    def randomID
    def trackingIdentifier
    def initialClaimDataXML
    def initialClaimAttEDI
    def currentEncTransmissionSID
    def ackReconTrID
    def ackReconIntID
    def ackReconFGID
    def ackReconClID
    def updatedClaimDataXML
    def updatedClaimAttEDI
    def currentEncTimeSQLFormat
    def currentTransmissionSID
    def encTransmissionSID
    def randomNumber
    def groupIdentifier
    def randomNr
    def randomNr2
    def updatedClaimDataXML2
    def randomProviderClaimNumber

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()

        def TC1a = new File(testDataPath + "CurrentActivityState/AP/837_TC1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CurrentActivityState/AP/837_TC1.dat doesn't exist!")
        def text = TC1a.getText()
        randomProviderClaimNumber = new Random().nextLong().abs()
        println "TrackingID  = " + randomProviderClaimNumber
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomProviderClaimNumber]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "CurrentActivityState/AP/837_TC1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "CurrentActivityState/AP/837_TC1.dat.properties doesn't exist!")

        def text1 = TC1b.getText()
        engine = new GStringTemplateEngine()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_TC1.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_TC1.dat", template)

//        def today = new Date()
//
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println "Claim Submitted Current Time = " + currentTimeSQLFormat;
//        sleep 60000
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewInsClaim")
    void checkClaimID() {
//        sleep 30000

        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCIDFields() {
        def cid = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(cid, "There is no CID field SubmittedClaimID = " + cid)
    }

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")

    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        def claimItem = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claimItem, "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))


    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCurrentActivityState1() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Application Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "/CurrentActivityState/AP/835_TC1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "/CurrentActivityState/AP/835_TC1.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CurrentActivityState/AP/835_TC1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CurrentActivityState/AP/85_TC1.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\835_TC1.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_TC1.dat", template)

//        def today = new Date()
//
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep 60000
    }


    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void checkCurrentActivityState2() {
//        sleep 30000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    //part 2

    // @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    @Test(dependsOnMethods = "checkCurrentActivityState2", groups = "NewInsClaim")
    void copyClaimsToConnectors2() {

        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "CurrentActivityState/AP/837_TC1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CurrentActivityState/AP/837_TC1.dat doesn't exist!")
        def text = TC1a.getText()
        randomProviderClaimNumber = new Random().nextLong().abs()
        println "TrackingID  = " + randomProviderClaimNumber
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomProviderClaimNumber]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "CurrentActivityState/AP/837_TC1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "CurrentActivityState/AP/837_TC1.dat.properties doesn't exist!")

        def text1 = TC1b.getText()
        engine = new GStringTemplateEngine()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_TC1.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_TC1.dat", template)

//        def today = new Date()
//
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println "Claim Submitted Current Time = " + currentTimeSQLFormat;
//        sleep 60000
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors2", groups = "NewInsClaim")
    void getClaimTransmissionSID2() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID2", groups = "NewInsClaim")
    void checkClaimID2() {
//        sleep 30000

        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "checkClaimID2", groups = "NewInsClaim")
    void checkCIDFields2() {
        def cid = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(cid, "There is no CID field SubmittedClaimID = " + cid)
    }

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "checkClaimID2", groups = "NewInsClaim")
    void checkCurrentActivityState3() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Application Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "checkClaimID2", groups = "NewInstPayment")
    void copyPaymentToConnectors2() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
//        sleep 10000
        def TC1a = new File(testDataPath + "/CurrentActivityState/AP/835_TC2.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "/CurrentActivityState/AP/835_TC2.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CurrentActivityState/AP/835_TC2.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CurrentActivityState/AP/85_TC2.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\835_TC2.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_TC2.dat", template)

//        def today = new Date()
//
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep 60000
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkEventAudit3() {
//        sleep(60000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->

//                def event1 = getEvent1("Claim Processor Claim ID assigned", item.values().toString().replace("[", "").replace("]", ""), "302")
                def event1 = getEvent1("Claim Processor Claim ID assigned", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.toString()}"
                Assert.assertNotNull(event1, "Audit Event is missing for TransmissionSID =" + currentTransmissionSID)
        }

    }

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInsClaim")
    void checkCurrentActivityState4() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-7000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }


}
