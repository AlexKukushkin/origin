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
 * Time: 11:17
 * To change this template use File | Settings | File Templates.
 */
class InstDatCurrentActivityState extends UtilsQuery {
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
    def randomNr3

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {

        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC2_I.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC2_I.dat doesn't exist!")
        def text = TC1a.getText()
        randomNr3 = new Random().nextInt(999999999)
        randomID = new Random(9999999999999).nextLong().abs()
        println randomID

        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)


        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)
        createNewFile(inboundFileConnector + "\\837_TC2_I.dat", template)

//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
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
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC2_I835.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC2_I835.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/837_TC2_I835.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/837_TC2_I835.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(paymentAsClaimUpdate + "\\837_TC2_I835.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\837_TC2_I835.dat", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep 60000
    }

    /* Test Event AUDIT for Claim*/

//    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
//    void checkEventAudit2() {
//        sleep(60000)
//        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
//        listClaimSID.each {
//            item ->
//
//            def event1 = getEvent1("Claim was modified by UnknownUser ", item.values().toString().replace("[", "").replace("]", ""), "302")
//            print "${item.values()}"
//            Assert.assertNotNull(event1, "Audit Event is missing for TransmissionSID =" + currentTransmissionSID)
//        }
//
//    }

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void checkCurrentActivityState2() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-4000", "CurrentActivityState for Claim is not Channel Processing")

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

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEncounterEvent() {
//        sleep(60000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    /* Check after update InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkInternalClaimID() {
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void getEncounterClaimTransmissionSID() {
//        sleep(60000)
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState3() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "checkCurrentActivityState3", groups = "NewInstTA1")
    void copyTA1ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/Inst/TA1_TC2.dat")
        def TC = TC1a.exists()
        Assert.assertTrue(TC, testDataPath + "Acknowledgement/Inst/TA1_TC2.dat doesn't exist!")
        def text = TC1a.getText()

        def intContNumSelect = "Select InterchangeControlNumber from Interchange where TransmissionSID = " + encTransmissionSID
        def intContNum
        activeSql.eachRow(intContNumSelect) {
            intContNum = it.InterchangeControlNumber
        }

        def controlNumber = createControlNumber(intContNum)
        def binding = ["IntContNum": controlNumber, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\TA1_TC2.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    @Test(dependsOnMethods = "copyTA1ToConnectors", groups = "NewInstTA1")
    void getTA1TransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState4() {
//        sleep 60000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-11000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "checkCurrentActivityState4", groups = "NewInst999")
    void copy999ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/Inst/999_TC1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/Inst/999_TC1.dat doesn't exist!")
        def text = TC1a.getText()

        def grContNumSelect = "Select GroupControlNumber from FunctionalGroup where TransmissionSID = " + encTransmissionSID
        def grContNum
        activeSql.eachRow(grContNumSelect) {
            grContNum = it.GroupControlNumber
        }

        def binding = ["GroupContNum": grContNum, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\999_TC1.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    @Test(dependsOnMethods = "copy999ToConnectors", groups = "NewInst999")
    void get999TransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState5() {
//        sleep 60000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-11000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-11000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-11000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "checkCurrentActivityState5", groups = "NewInst277", alwaysRun = true)
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/Inst/277.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/Inst/277.dat doesn't exist!")
        def text = TC1a.getText()

        def originatorTranIdentifier = "Select OriginatorTranIdentifier from ClaimHeader where TransmissionSID = " + encTransmissionSID
        def origTranIdent
        activeSql.eachRow(originatorTranIdentifier) {
            origTranIdent = it.originatorTranIdentifier
        }

        def binding = ["OriginatorTranIdentifier": origTranIdent, "TargetTrackingID": trackingIdentifier, "ID": randomNr, "UniqueID": randomNumber, "UniqueID2": randomNr3]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)


        createNewFile(receiveInboundAcks + "\\277.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    @Test(dependsOnMethods = "copy277ToConnectors", groups = "NewInst277")
    void get277TransmissionSID() {
//        sleep 30000
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState6() {
//        sleep 60000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-11000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-11000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-11000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-11000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

}
