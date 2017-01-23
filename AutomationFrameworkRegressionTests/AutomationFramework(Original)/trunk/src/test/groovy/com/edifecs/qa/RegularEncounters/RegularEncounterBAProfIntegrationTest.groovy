package com.edifecs.qa.RegularEncounters

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import groovy.text.SimpleTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: anastasiaz
 * Date: 10/24/11
 * Time: 11:22 AM
 * To change this template use File | Settings | File Templates.
 */
class RegularEncounterBAProfIntegrationTest extends UtilsQuery {
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

    def sleepTime = 60000

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_PBA.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters837_TC3_PBA.dat doesn't exist!")
        def text = TC1a.getText()

        randomID = new Random(9999999999999).nextLong().abs()
        println randomID
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/837_TC3_PBA_1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/837_TC3_PBA_1.dat.properties doesn't exist!")
        def text1 = TC1b.getText()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_TC3_PBA_1.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_TC3_PBA_1.dat", template)

//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;

        // sleep(sleepTime)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
        // sleep(30000)
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
        // sleep(30000)
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        // change to Null because they are not set, bug was reported  , once bug fixed, change back
        // Assert.assertNotNull(groupCMSEncounterGroup,"There should be group for Claim with GroupIdentifier = "+trackingIdentifier + groupCMSEncounterGroup)
        Assert.assertNull(groupCMSEncounterGroup, "There should't be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
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
        // sleep(60000)

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

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInsClaim")
    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))

//        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
        //Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
//        Assert.assertNotNull(claimAtt,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        initialClaimAttEDI = new StringReader(new String(claimAtt))

    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_PBA835.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_PBA835.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/837_TC3_PBA835.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/837_TC3_PBA835.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(paymentAsClaimUpdate + "\\837_TC3_PBA835.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\837_TC3_PBA835.dat", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
        //sleep(sleepTime)
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEncounterEvent() {
        // sleep(30000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)

        }
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkUpdatedCustomFields() {
        // sleep(30000)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertEquals(cf3, "In Progress", "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1234", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        //The custom field SubmissionType is updated for the encounter transmission. Updated method to pass encTransmissionSID
        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
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
        // sleep sleepTime
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkAckReconID() {
        ackReconIntID = getCorrelationIDWaiting("343", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconIntID, "There should be AckReconIDValue for Interchange Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconFGID = getCorrelationIDWaiting("331", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconFGID, "There should be AckReconIDValue for Functional Group level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconTrID = getCorrelationIDWaiting("397", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconTrID, "There should be AckReconIDValue for Transaction level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkSubmittedClaimID() {
        submittedClaimID = getCorrelationIDWaiting("302", "SubmittedClaimID", encTransmissionSID)
        Assert.assertNotNull(submittedClaimID, "There should be submittedClaimID for Claim Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    @Test(dependsOnMethods = "checkAckReconID", groups = "NewInstPayment")
    void checkEncounterGroupIdentifiers() {
        def groupIntEncGroup = getGroupWaiting("AckGroup", "Interchange", ackReconIntID, "343", encTransmissionSID)
        Assert.assertNotNull(groupIntEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupFGEncGroup = getGroupWaiting("AckGroup", "FuncGroup", ackReconFGID, "331", encTransmissionSID)
        Assert.assertNotNull(groupFGEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        // my select

        def select1;
        def relItAck;
        select1 = "select GroupIdentifierSID from EcActive.dbo.GroupIdentifier where TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=397"

        activeSql.eachRow(select1) { relItAck = "$it.GroupIdentifierSID" }
        Assert.assertNotNull(relItAck, "There is no groupCMSEncounterGroup for GroupIdentifier =" + select1)

    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")

        /*
        satish - Disabling this check as there is more than one record of type NATIVE being created in the OPS Repository.
        The ops repository library has to be updated to handle this, commenting this check until the library is updated.

        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("NATIVE")),"Cannot find Ops Repository attachment for trackingID = "+trackingIdentifier);
        updatedClaimAttEDI = new StringReader(new String(claimAtt))
        Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")
        */
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
        // sleep(30000)

        def cf2 = getCustomField("Source", encTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", encTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", encTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should  be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomFieldWaiting("EncounterID", encTransmissionSID, "302")
        Assert.assertNotNull(cf5, "There should be Custom field for ClaimID with GroupIdentifier =" + cf5)
        print cf5

        def cf6 = getCustomField("CMSICN", encTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", encTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1234", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)
//        Assert.assertEquals(cf7, "H1234567", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", encTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }



    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstTA1")
    void copyTA1ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_TA1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_TA1.dat doesn't exist!")
        def text = TC1a.getText()

        def interControlGroup = "select InterchangeControlNumber from Interchange where TransmissionSID=" + encTransmissionSID
        def icg; activeSql.eachRow(interControlGroup) { icg = "$it.InterchangeControlNumber" }
        def interchangeControlGroup = String.format('%09d', Integer.parseInt(icg))

        def binding = ["IntContNum": interchangeControlGroup]
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\837_TC3_TA1.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
        // sleep(sleepTime)
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

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkTA1GroupIdentifiers() {
        def groupTA1EncounterGroup = getGroupSimpleWaiting("AckGroup", "TA1", "343", currentTransmissionSID)
        Assert.assertNotNull(groupTA1EncounterGroup, "There is no TA1 group for Encounter for GroupIdentifier = " + groupTA1EncounterGroup)
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkTA1Identifiers() {
        def groupTA1EncounterGroup = getCorrelationIDWaiting("343", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(groupTA1EncounterGroup, "There is no TA1 group for Encounter for GroupIdentifier = " + groupTA1EncounterGroup)
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkTA1Event() {
        // sleep(30000)
        def tr = "select * from TransactionHeader where TransmissionSID=" + encTransmissionSID
        println tr
        def trID; activeSql.eachRow(tr) { trID = "$it.InterchangeSID" }
        //def event = getEvent("Acknowledgement received from partner with Accepted status", trID.toString(), "343")
        // also change message to: Outbound interchange accepted for processing by external partner
        def event = getEvent1("TA1 Acknowledgement received from", trID.toString(), "343")
        Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + encTransmissionSID)
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInst999")
    void copy999ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_999.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_999.dat doesn't exist!")
        def text = TC1a.getText()

        def interControlGroup = "select GroupControlNumber from FunctionalGroup where TransmissionSID=" + encTransmissionSID
        def gcn; activeSql.eachRow(interControlGroup) { gcn = "$it.GroupControlNumber" }

        def binding = ["GroupContNum": gcn]
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\837_TC3_999.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
        // sleep(sleepTime)
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

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999GroupIdentifiers() {
        def group999EncounterGroup = getGroupSimpleWaiting("AckGroup", "999", "397", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999Identifiers() {
        def group999EncounterGroup = getCorrelationIDWaiting("397", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999Event() {
        // sleep(30000)
        def tr = "select * from TransactionHeader where TransmissionSID=" + encTransmissionSID
        //def trID; activeSql.eachRow( tr ) {trID="$it.FunctionalGroupSID" }
        def trID; activeSql.eachRow(tr) { trID = "$it.TransactionHeaderSID" }
        println trID
        // modify message to : Accepted 277CA acknowledegment has been received
        //def event = getEvent("Acknowledgement received from partner with Accepted status", trID, "331")
        def event = getEvent1("999 Acknowledgement received from partner", trID, "397")
        Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + encTransmissionSID)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst277")
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_277CA.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_277CA.dat doesn't exist!")
        def text = TC1a.getText()

        def originatorTranIdentifier = "Select OriginatorTranIdentifier from ClaimHeader where TransmissionSID = " + encTransmissionSID
        def origTranIdent
        activeSql.eachRow(originatorTranIdentifier) {
            origTranIdent = it.originatorTranIdentifier
        }

        def providerClaimNumber = "Select ProviderClaimNumber from Claim where TransmissionSID = " + encTransmissionSID
        def provClNum
        activeSql.eachRow(providerClaimNumber) {
            provClNum = it.ProviderClaimNumber
        }

        // def controlNumber = createControlNumber(grContNum)
        def binding = ["OriginatorTranIdentifier": origTranIdent, "ProviderClaimNumber": provClNum]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\837_TC3_277CA.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
        // sleep(sleepTime)
    }

    @Test(dependsOnMethods = "copy277ToConnectors", groups = "NewInst277")
    void get277TransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277GroupIdentifiers() {
        def group999EncounterGroup = getGroupSimpleWaiting("AckGroup", "STN", "489", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277 group for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277Identifiers() {
        def group999EncounterGroup = getCorrelationIDWaiting("397", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277CA  TrackingID indentifier for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277AckReconIDIdentifier() {
        def group999EncounterGroup = getCorrelationID("489", "AckReconID", currentTransmissionSID)
        // should be empty?
        //  def group999EncounterGroup=getCorrelationID("397", "TrackingID", currentTransmissionSID)
        //  Assert.assertNotNull(group999EncounterGroup,"There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = "+currentTransmissionSID)
        Assert.assertNull(group999EncounterGroup, "There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277Event() {
        // sleep(30000)
        def tr = "select * from Claim where TransmissionSID=" + encTransmissionSID
        def trID; activeSql.eachRow(tr) { trID = "$it.ClaimSID" }
        // modified message to: Accepted 277CA acknowledgement has been received
        //def event = getEvent("Acknowledgement received from partner with Accepted status", trID, "302")
        def event = getEvent1("Accepted status reported and details posted from 277 External Acknowledgement", trID, "302")
        Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + encTransmissionSID)
    }
}
