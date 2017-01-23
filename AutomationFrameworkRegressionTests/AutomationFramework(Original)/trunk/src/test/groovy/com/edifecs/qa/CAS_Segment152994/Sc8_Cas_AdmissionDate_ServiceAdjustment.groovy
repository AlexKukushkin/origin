package com.edifecs.qa.CAS_Segment152994

import com.edifecs.bizitem.data.Attachment
import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.InputOutputStream
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.apache.commons.io.IOUtils
import org.testng.Assert
import org.testng.annotations.Test
import java.io.*;
import java.io.File

//

/**
 * Created by IntelliJ IDEA.
 * User: anastasiaz
 * Date: 10/26/11
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */

class Sc8_Cas_AdmissionDate_ServiceAdjustment extends UtilsQuery {
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
    def sleepTime = 60000

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors1() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/1/Claim_1.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/1/Claim_1.xml doesn't exist!")

        def text = TC1a.getText()
        randomID = new Random().nextLong().abs()

        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

/*        GlobalVar.n1 = randomNumber;
        GlobalVar.n2 = randomID;*/

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/1/Claim_1.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/1/Claim_1.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\Claim_1.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\Claim_1.xml", template)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors1", groups = "NewInsClaim")
    void getClaimTransmissionSID1() {
//        sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID1", groups = "NewInsClaim")
    void checkClaimID() {
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        //Assert.assertSame(internalID, currentTransmissionSID)
    }

    //Check Current Activity State 1 of the Claim
    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCurrentActivityState1() {
//        sleep 30000
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "1000", "CurrentActivityState for Claim is not Application Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNull(groupCMSEncounterGroup, "There should be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInsClaim")
    void checkExtraGroupIdentifiers() {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        Assert.assertNull(exGroup, "There is no extra group for GroupIdentifier = " + extraGroup)
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCustomFields() {
//        sleep(30000)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There should be no value for Custom field Source  for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomField("ClaimID", currentTransmissionSID, "302")
        Assert.assertNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for CMSICN with GroupIdentifier =" + cf6)

        def cf7 = getCustomField("CMSContractID", currentTransmissionSID, "302")
        Assert.assertNull(cf7, "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomField("SubmissionType", currentTransmissionSID, "302")
        Assert.assertNull(cf8, "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInsClaim")
    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/1/Payment_1.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/1/Payment_1.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/1/Payment_1.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/1/Payment_1.xml.properties doesn't exist!")
        def text2 = TC1b.getText()
        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\Payment_1.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\Payment_1.xml", template)
    }

    //Check Current Activity State 2
    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void checkCurrentActivityState2() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-6000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEncounterEvent() {
//        sleep(30000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }


    /* Check Custom Fields*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkUpdatedCustomFields() {
//        sleep 30000

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNotNull(cf2, "There should be no Custom field Source for GroupIdentifier = " + cf2)

        def cf3 = getCustomFieldWaiting("Disposition", currentTransmissionSID, "302")
        Assert.assertEquals(cf3, "In Progress", "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "1011", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def select1;
        def lastDate;
        select1 = "select * from EcActive.dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from EcActive.dbo.CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"

        activeSql.eachRow(select1) { lastDate = "$it.FieldValue" }
        Assert.assertNotNull(lastDate, "There should be no LastInternalActivity Date = " + select1)
    }

    /* Check after update InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkInternalClaimID() {
//        sleep(30000)
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    //Begin Part 2
    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void copyClaimsToConnectors2() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/2/Claim_2.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/2/Claim_2.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["UniqueID": trackingIdentifier, "TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/2/Claim_2.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/2/Claim_2.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\Claim_2.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\Claim_2.xml", template)
    }

    //Check Current Activity State 3
    @Test(dependsOnMethods = "copyClaimsToConnectors2", groups = "NewInsClaim")
    void checkCurrentActivityState3() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-6000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
//        sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewInsClaim")
    void checkClaimID2() {
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        //Assert.assertSame(internalID, currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "checkClaimID2", groups = "NewInsClaim")
    void checkGroupIdentifiers2() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNull(groupCMSEncounterGroup, "There should be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }


    @Test(dependsOnMethods = "copyClaimsToConnectors2", groups = "NewInstPayment")
    void copyPaymentToConnectors2() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/2/Payment_2.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "CAS_8_AdmissionDate_ServiceAdjustment/2/Payment_2.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/2/Payment_2.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/CAS_8_AdmissionDate_ServiceAdjustment/2/Payment_2.xml.properties doesn't exist!")
        def text2 = TC1b.getText()
        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\Payment_2.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\Payment_2.xml", template)
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkEncounterEvent2() {
//        sleep(30000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    //Check Custom Fields

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkUpdatedCustomFields2() {
//        sleep 30000

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNotNull(cf2, "There should be no Custom field Source for GroupIdentifier = " + cf2)

        def cf3 = getCustomFieldWaiting("Disposition", currentTransmissionSID, "302")
        Assert.assertEquals(cf3, "In Progress", "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "1011", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        // select

        def select1;
        def lastDate;
        select1 = "select * from EcActive.dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from EcActive.dbo.CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"

        activeSql.eachRow(select1) { lastDate = "$it.FieldValue" }
        Assert.assertNotNull(lastDate, "There should be no LastInternalActivity Date = " + select1)
    }

    //Check after update InternalClaimID  are proper in DB

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkInternalClaimID2() {
//        sleep(30000)
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }
    //End Part 2

    //Verify TransmissionSID verify TM DB Schema exists

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInsClaim")
    void getEncounterClaimTransmissionSID() {
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    //Check Current Activity State 4
    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInsClaim")
    void checkCurrentActivityState4() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-4000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
//        sleep(sleepTime)

        def cf2 = getCustomField("Source", encTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", encTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", encTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomFieldWaiting("EncounterID", encTransmissionSID, "302")
        Assert.assertNotNull(cf5, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", encTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", encTransmissionSID, "302")
        Assert.assertEquals(cf7, "1011", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertNotNull(cf8, "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        // select

        def select1;
        def lastDate;
        select1 = "select * from EcActive.dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from EcActive.dbo.CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"

        activeSql.eachRow(select1) { lastDate = "$it.FieldValue" }
        Assert.assertNull(lastDate, "There should be no LastInternalActivity Date = " + select1)
    }

    //Check Current Activity State 5
    @Test(dependsOnMethods = "checkEncounterCustomFields", groups = "NewInsClaim")
    void checkCurrentActivityState5() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(currentTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "1000", "CurrentActivityState for Interchange is not Channel Processing")

        def transactionID = getTransactionIDString(currentTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(currentTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")
    }

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkEncounterOpsData() {
        System.out.println("TEST");
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")

        //def att = repository.getAt("EDI attachment");
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim attachment should not be equals")

        def claimAttItem = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE"));
        def attachments = claimAttItem.attachments
        def claimAtt;
        attachments as Attachment[] ;
        attachments.each {
            def att = it
            def name = att.getName();
            if (name!= null && name.equals("LastEncounter") == true){
                claimAtt = att.data;
            }
        }

        String claimAttNative = new String(claimAtt, "UTF-8");

        File lstFile = new File(writeToFile, 'OutboundEncounter.dat').withWriter('utf-8') { writer ->
            writer.write(claimAttNative);
        }
    }

    @Test(dependsOnMethods = "checkCurrentActivityState5", groups = "NewInsClaim", alwaysRun = true)
    void validateAttachment() {
        FileInputStream firstTargetFile = new FileInputStream(new File("./src/test/resources/com/edifecs/qa/Validation/Assertion_CASAdmissionDateServiceAdjustment.dat"));
        String assert_file = IOUtils.toString(firstTargetFile, "UTF-8");

        FileInputStream secondTargetFile = new FileInputStream(new File("./src/test/resources/com/edifecs/qa/Validation/OutboundEncounter.dat"));
        String outbound_file = IOUtils.toString(secondTargetFile, "UTF-8");

        println("---------------------------------------------------------------------------------------------------");
        println("--------------------------------------- Validation Result -----------------------------------------");
        println("---------------------------------------------------------------------------------------------------");
        def position = 41;
        InputOutputStream.validateFiles(assert_file, outbound_file, position);
    }
}
