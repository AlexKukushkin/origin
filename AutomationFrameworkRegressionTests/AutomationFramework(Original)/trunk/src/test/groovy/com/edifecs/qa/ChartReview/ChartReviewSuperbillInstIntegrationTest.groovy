package com.edifecs.qa.ChartReview

import com.edifecs.qa.utils.UtilsQuery
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: ValentinaT
 * Date: 2/21/12
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
import org.testng.annotations.Listeners
import com.edifecs.qa.CuantoListener
@Listeners(com.edifecs.qa.CuantoListener)
class ChartReviewSuperbillInstIntegrationTest extends UtilsQuery {
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
    def submittedClaimID
    def chartTransmissionSID
    def sleepTime = 30000

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "ChartReviewSuperbill", alwaysRun = true)
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        randomID = generateNumber(100000000, 999999999)
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)
        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def TC1a = new File(testDataPath + "ChartReview/837_ISuperBill.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/837_ISuperBill.dat doesn't exist!")
        def text1 = TC1a.getText()
        def TC1b = new File(testDataPath + "ChartReview/837_ISuperBill.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "ChartReview/837_ISuperBill.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def engine1 = new GStringTemplateEngine()
        def template1 = engine1.createTemplate(text1).make(binding)

        def engine2 = new GStringTemplateEngine()
        def template2 = engine2.createTemplate(text2).make(binding)

        createNewFile(inboundFileConnector + "\\837_ISuperBill.dat.properties", template2)
        createNewFile(inboundFileConnector + "\\837_ISuperBill.dat", template1)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
//        sleep 30000
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "ChartReviewSuperbill", alwaysRun = true)
    void getClaimTransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void checkClaimID() {
//        sleep 60000
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /*Check Current Activity State*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void checkCurrentActivityState1() {
//        sleep 30000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertNotNull(activityStateTransmission, "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertNotNull(activityStateClaim, "CurrentActivityState for Claim is not Application Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertNotNull(activityStateInterchange, "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertNotNull(activityStateTransaction, "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertNotNull(activityStateFuncGroup, "CurrentActivityState for FunctionalGroup is not Channel Processing")

    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNull(groupCMSEncounterGroup, "There should't be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void checkExtraGroupIdentifiers() {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        Assert.assertNull(exGroup, "There is no extra group for GroupIdentifier = " + extraGroup)
    }
    /*Check SubmittedClaimID CIDs*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewInsClaim")
    void checkCIDFields() {
        def cid = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(cid, "There is no CID field SubmittedClaimID = " + cid)
    }

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void checkCustomFields() {
//        sleep(30000)


        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There should not be Custom field CurrentState for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomField("ClaimID", currentTransmissionSID, "302")
        Assert.assertNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomField("CMSContractID", currentTransmissionSID, "302")
        Assert.assertNull(cf7, "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomField("SubmissionType", currentTransmissionSID, "302")
        Assert.assertNull(cf8, "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReview/SuperBillTrigger.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/SuperBillTrigger.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "ChartReview/SuperBillTrigger.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "ChartReview/SuperBillTrigger.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\SuperBillTrigger.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\SuperBillTrigger.xml", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep(sleepTime)
    }

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReviewSuperbill", alwaysRun = true)
    void getChartReviewClaimTransmissionSID() {
//        sleep 60000
        chartTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(chartTransmissionSID, "Superbill encounter is not generated!!!")
    }

    void checkCustomFields1() {
//        sleep(60000)
//        def cf8 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='CurrentState') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf88; activeSql.eachRow(cf8) {cf88 = "$it.FieldValue" }
//        Assert.assertNotNull(cf88, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf88)

        def cf1 = getCustomFieldWaiting("CurrentState", currentTransmissionSID, "302")
        Assert.assertNotNull(cf1, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf1)

//        def cf9 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Source') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf99; activeSql.eachRow(cf9) {cf99 = "$it.FieldValue" }
//        Assert.assertNull(cf99, "There is no Custom field Source=Channel for GroupIdentifier = " + cf99)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field Source=Channel for GroupIdentifier = " + cf2)

//        def cf10 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Disposition') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf100; activeSql.eachRow(cf10) {cf100 = "$it.FieldValue" }
//        Assert.assertNull(cf100, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf100)

        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)

//        def cf16 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='ClaimID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf44; activeSql.eachRow(cf16) {cf44 = "$it.FieldValue" }
//        Assert.assertNotNull(cf44, "There should not be value Custom field ClaimID with GroupIdentifier = " + cf44)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There is no Custom field ClaimID for GroupIdentifier = " + cf4)

//        def cf13 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='EncounterID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf55; activeSql.eachRow(cf13) {cf55 = "$it.FieldValue" }
//        Assert.assertNull(cf55, "There should not be value Custom field EncounterID with GroupIdentifier = " + cf55)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There is no Custom field EncounterID for GroupIdentifier = " + cf5)

//        def cf1 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='SubmissionType') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf11; activeSql.eachRow(cf1) {cf55 = "$it.FieldValue" }
//        Assert.assertNull(cf11, "There should not be value Custom field EncounterID with GroupIdentifier = " + cf11)

        def cf6 = getCustomField("SubmissionType", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be value Custom field SubmissionType for GroupIdentifier = " + cf6)
    }
}



