package com.edifecs.qa.DeniedEncounters

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: ValentinaT
 * Date: 1/25/12
 * Time: 6:10 PM
 * To change this template use File | Settings | File Templates.
 */
class FullDenied extends UtilsQuery {
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
    def select1
    def relItAck

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "DeniedEncounters", alwaysRun = true)
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeniedEncounters/837_Full.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeniedEncounters/837_Full.dat doesn't exist!")
        def text = TC1a.getText()

        randomID = new Random(9999999999999).nextLong().abs()
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "DeniedEncounters/837_Full.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "DeniedEncounters/837_Full.dat.properties doesn't exist!")
        def text1 = TC1b.getText()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_Full.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_Full.dat", template)

//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;

//        sleep(sleepTime)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "DeniedEncounters", alwaysRun = true)
    void getClaimTransmissionSID() {
//        sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Claim have errors!" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkClaimID() {
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNull(groupCMSEncounterGroup, "There should't be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkExtraGroupIdentifiers() {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        Assert.assertNull(exGroup, "There is no extra group for GroupIdentifier = " + extraGroup)
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
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

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))
//        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
//        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
//        Assert.assertNotNull(claimAtt,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        initialClaimAttEDI = new StringReader(new String(claimAtt))
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeniedEncounters/835_Full.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeniedEncounters/835_Full.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "DeniedEncounters/835_Full.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "DeniedEncounters/835_Full.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\835_Full.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_Full.dat", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep(sleepTime)
    }

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "DeniedEncounters", alwaysRun = true)
    void getEncounterClaimTransmissionSID() {
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

// comment because not set, bug was reported
//    /* Test Event AUDIT for Claim*/
//    @Test (dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters",alwaysRun=true)
//    void checkEventAudit(){
//        sleep(60000)
//        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
//        listClaimSID.each{
//            item -> def event = getEvent1("Claim was modified by Unknown ", item.values().toString().replace("[","").replace("]",""), "302")
//            print "${item.values()}"
//
//        }
//    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkEncounterEvent() {
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)

        }
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkUpdatedCustomFields() {
//        sleep(30000)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomFieldWaiting("Disposition", currentTransmissionSID, "302")
        Assert.assertEquals(cf3, "In Progress", "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1234567", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", currentTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    /* Check after update InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkInternalClaimID() {
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkAckReconID() {
        ackReconIntID = getCorrelationIDWaiting("343", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconIntID, "There should be AckReconIDValue for Interchange Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconFGID = getCorrelationIDWaiting("331", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconFGID, "There should be AckReconIDValue for Functional Group level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconTrID = getCorrelationIDWaiting("397", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconTrID, "There should be AckReconIDValue for Transaction level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkSubmittedClaimID() {
        submittedClaimID = getCorrelationIDWaiting("302", "SubmittedClaimID", encTransmissionSID)
        Assert.assertNotNull(submittedClaimID, "There should be submittedClaimID for Claim Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkEncounterGroupIdentifiers() {
//        sleep(30000)
        def groupIntEncGroup = getGroupWaiting("AckGroup", "Interchange", ackReconIntID, "343", encTransmissionSID)
        Assert.assertNotNull(groupIntEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupFGEncGroup = getGroupWaiting("AckGroup", "FuncGroup", ackReconFGID, "331", encTransmissionSID)
        Assert.assertNotNull(groupFGEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        // select
        select1 = "select GroupIdentifierSID from EcActive.dbo.GroupIdentifier where TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=397"
        activeSql.eachRow(select1) { relItAck = "$it.GroupIdentifierSID" }
        Assert.assertNotNull(relItAck, "There is no groupCMSEncounterGroup for GroupIdentifier =" + select1)

    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEncounters", alwaysRun = true)
    void checkEncounterOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim attachment should not be equals")

//        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
//        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        updatedClaimAttEDI = new StringReader(new String(claimAtt))
//        Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "DeniedEnconters", alwaysRun = true)
    void checkEncounterCustomFields() {
//        sleep(sleepTime)

        def cf2 = getCustomField("Source", encTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", encTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", encTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should  be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomFieldWaiting("EncounterID", encTransmissionSID, "302")
        Assert.assertNotNull(cf5, "There should be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", encTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", encTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1234567", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", encTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }
}
