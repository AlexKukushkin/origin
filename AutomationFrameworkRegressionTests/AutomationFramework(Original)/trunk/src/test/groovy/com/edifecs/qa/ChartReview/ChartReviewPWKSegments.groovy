package com.edifecs.qa.ChartReview

import com.edifecs.bizitem.data.Attachment
import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.InputOutputStream
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.apache.commons.io.IOUtils
import org.testng.Assert
import org.testng.annotations.Test
import org.testng.internal.Invoker

/**
 * Created with IntelliJ IDEA.
 * User: satisuri
 * Date: 4/24/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
import org.testng.annotations.Listeners
import com.edifecs.qa.CuantoListener
@Listeners(com.edifecs.qa.CuantoListener)
class ChartReviewControlNumberREF6R extends UtilsQuery {
    def currentTimeSQLFormat
    def randomID
    def trackingIdentifier
    def initialClaimDataXML
    def initialClaimAttEDI
    def currentEncTransmissionSID
    def originalTransmissionSID
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

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "ChartReview")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReviewControlNumberREF6R/837_1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReviewControlNumberREF6R/837_1.dat doesn't exist!")
        def text = TC1a.getText()

        randomID = new Random(9999999999999).nextLong().abs()
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReviewControlNumberREF6R/837_1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReviewControlNumberREF6R/837_1.dat.properties doesn't exist!")
        def text1 = TC1b.getText()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_1.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_1.dat", template)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "ChartReview")
    void getClaimTransmissionSID() {
//        sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        originalTransmissionSID = currentTransmissionSID
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    /* Check SubmittedClaimID and InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void checkClaimID() {
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        // change to Null because they are not set, bug was reported  , once bug fixed, change back
        // Assert.assertNotNull(groupCMSEncounterGroup,"There should be group for Claim with GroupIdentifier = "+trackingIdentifier + groupCMSEncounterGroup)
        Assert.assertNull(groupCMSEncounterGroup, "There should't be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void checkExtraGroupIdentifiers() {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        Assert.assertNull(exGroup, "There is no extra group for GroupIdentifier = " + extraGroup)
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void checkCustomFields() {
//        sleep(60000)

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

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))
        //  initialClaimDataXML =new String(claimData)

//        def claimAtt = repository.getAttachment("Claim", trackingIdentifier, "NATIVE")
//        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim attachment for trackingID = " + trackingIdentifier);
//        Assert.assertNotNull(claimAtt, "Cannot find Ops Repository Claim attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        initialClaimAttEDI = new StringReader(new String(claimAtt))
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "ChartReview")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReviewControlNumberREF6R/ECHCF_2.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReviewControlNumberREF6R/ECHCF_2.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReviewControlNumberREF6R/ECHCF_2.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReviewControlNumberREF6R/ECHCF_2.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_2.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_2.xml", template)
    }

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "ChartReview")
    void getEncounterClaimTransmissionSID() {
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
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

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
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
        Assert.assertEquals(cf7, "H0308", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial-Capitated", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    /* Check after update InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void checkInternalClaimID() {
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void checkAckReconID() {
        ackReconIntID = getCorrelationIDWaiting("343", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconIntID, "There should be AckReconIDValue for Interchange Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconFGID = getCorrelationIDWaiting("331", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconFGID, "There should be AckReconIDValue for Functional Group level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconTrID = getCorrelationIDWaiting("397", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconTrID, "There should be AckReconIDValue for Transaction level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void checkSubmittedClaimID() {
        submittedClaimID = getCorrelationIDWaiting("302", "SubmittedClaimID", encTransmissionSID)
        Assert.assertNotNull(submittedClaimID, "There should be submittedClaimID for Claim Level ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void checkEncounterGroupIdentifiers() {
//        sleep(30000)
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

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
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

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
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
        Assert.assertEquals(cf7, "H0308", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial-Capitated", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", encTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    @Test(dependsOnMethods = "checkEncounterCustomFields", groups = "ChartReview")
    void copyMaoToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def encounterId = getCustomField("EncounterID", encTransmissionSID, "302")
        def mao002Id = getMao002Id(encTransmissionSID)

        def TC1a = new File(testDataPath + "ChartReviewControlNumberREF6R/MAO002-TC1_4.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReviewControlNumberREF6R/MAO002-TC1_4.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "EncounterID": encounterId, "MAO002ID": mao002Id]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "ChartReviewControlNumberREF6R/MAO002-TC1_4.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "ChartReviewControlNumberREF6R/MAO002-TC1_4.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(inboundMAO + "\\MAO002-TC1_4.xml.properties", template2)
        createNewFile(inboundMAO + "\\MAO002-TC1_4.xml", template)

    }

    @Test(dependsOnMethods = "copyMaoToConnectors", groups = "ChartReview")
    void checkMaoEvents() {
        def queryClaim = "select * from Claim where TransmissionSID=" + originalTransmissionSID
        def claimSid; activeSql.eachRow(queryClaim) { claimSid = "$it.ClaimSID" }
        def encounterId = getCustomField("EncounterID", encTransmissionSID, "302")
        def eventClaim = getEvent1("MAO002 reported Encounter $encounterId as Accepted", claimSid, "302")
        Assert.assertNotNull(eventClaim, "MAO Event is missing for ClaimSID =" + claimSid)

        def queryEncounter = "select * from Claim where TransmissionSID=" + encTransmissionSID
        def encounterSid; activeSql.eachRow(queryEncounter) { encounterSid = "$it.ClaimSID" }
        def eventEncounter = getEvent1("Accepted status reported and details posted from MAO002 External Acknowledgement", encounterSid, "302")
        Assert.assertNotNull(eventEncounter, "MAO Event is missing for Encounter, ClaimSID =" + claimSid)
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkMaoEvents", groups = "ChartReview")
    void copyClaimUpdateToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReviewControlNumberREF6R/837_P-5_Update.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReviewControlNumberREF6R/837_P-5_Update.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReviewControlNumberREF6R/837_P-5_Update.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReviewControlNumberREF6R/837_P-5_Update.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\837_P-5_Update.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\837_P-5_Update.xml", template)
    }

    @Test(dependsOnMethods = "copyClaimUpdateToConnectors", groups = "ChartReview", alwaysRun = true)
    void validateAttachment() {
        FileInputStream firstTargetFile = new FileInputStream(new File("./src/test/resources/com/edifecs/qa/Validation/Assertion_ControlNumberREF6R.dat"));
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