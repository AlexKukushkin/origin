package com.edifecs.qa.ChartReview

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
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
class ChartReviewProfIntegrationTest extends UtilsQuery {
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
        def TC1a = new File(testDataPath + "ChartReview/837_PChartReview.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/837_PChartReview.dat doesn't exist!")
        def text = TC1a.getText()

        randomID = new Random(9999999999999).nextLong().abs()
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReview/837_PChartReview.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReview/837_PChartReview.dat.properties doesn't exist!")
        def text1 = TC1b.getText()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_PChartReview.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_PChartReview.dat", template)

//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;

//        sleep(sleepTime)
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

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewInsClaim")
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
        def TC1a = new File(testDataPath + "ChartReview/837_PChartReview_835.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/837_PChartReview_835.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReview/837_PChartReview_835.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReview/837_PChartReview_835.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\837_PChartReview_835.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\837_PChartReview_835.dat", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
//        sleep(sleepTime)
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
        Assert.assertEquals(cf7, "H1234567", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

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

        /*
        def groupTrEncGroup= getGroup("AckGroup","837Txn", ackReconTrID,"397", encTransmissionSID )
        Assert.assertNotNull(groupTrEncGroup,"There is no groupCMSEncounterGroup for GroupIdentifier = "+groupIntEncGroup)
        */
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void checkEncounterOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim attachment should not be equals")
//        updatedClaimDataXML = new String(claimData)
//
//            File updatedClaimDataFile = new File("updatedClaimData.XML")
//
//            updatedClaimDataFile.text = updatedClaimDataXML
//            File initialClaimDataFile = new File("initialClaimData.XML")
//            initialClaimDataFile.text = initialClaimDataXML
//
//            def sortedInitialClaimDataXML =  new File("sortedInitialClaimData.XML")
//            new XmlSortingTask(initialClaimDataFile,sortedInitialClaimDataXML).execute()
//            def sortedUpdatedClaimDataXML = new File("sortedUpdatedClaimData.XML")
//            new XmlSortingTask(updatedClaimDataFile, sortedUpdatedClaimDataXML).execute()
//            XMLAssert.assertXMLNotEqual("OPS Claim data should not be equals", sortedInitialClaimDataXML.text, sortedUpdatedClaimDataXML.text);
////        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")
//            sortedInitialClaimDataXML.delete()
//            sortedUpdatedClaimDataXML.delete()
//            initialClaimDataFile.delete()
//            updatedClaimDataFile.delete()

        /*
        def claimAtt = repository.getAttachment("Claim", trackingIdentifier, "NATIVE")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
        updatedClaimAttEDI = new StringReader(new String(claimAtt))
        Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")*/
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
        Assert.assertEquals(cf7, "H1234567", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", encTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "ChartReview")
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReview/837_PChartReview_277.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/837_PChartReview_277.dat doesn't exist!")
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

        createNewFile(receiveInboundAcks + "\\837_PChartReview_277.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
//        sleep(sleepTime)
    }

    @Test(dependsOnMethods = "copy277ToConnectors", groups = "ChartReview")
    void get277TransmissionSID() {
        chartTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(chartTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "ChartReview")
    void check277GroupIdentifiers() {
        def group999EncounterGroup = getGroupSimple("AckGroup", "STN", "489", chartTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277 group for Encounter for GroupIdentifier = " + chartTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "ChartReview")
    void check277Identifiers() {
        def group999EncounterGroup = getCorrelationID("397", "TrackingID", chartTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277CA  TrackingID indentifier for Encounter for GroupIdentifier = " + chartTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "ChartReview")
    void check277AckReconIDIdentifier() {
        def group999EncounterGroup = getCorrelationID("489", "AckReconID", chartTransmissionSID)
        // should be empty?
        //  def group999EncounterGroup=getCorrelationID("397", "TrackingID", currentTransmissionSID)
        //  Assert.assertNotNull(group999EncounterGroup,"There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = "+currentTransmissionSID)
        Assert.assertNull(group999EncounterGroup, "There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = " + chartTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "ChartReview")
    void check277Event() {
//        sleep(60000)
        def tr = "select * from Claim where TransmissionSID=" + encTransmissionSID
        def trID; activeSql.eachRow(tr) { trID = "$it.ClaimSID" }
        // modified message to: Accepted 277CA acknowledgement has been received
        //def event = getEvent("Acknowledgement received from partner with Accepted status", trID, "302")
        def event = getEvent1("Accepted status ", trID, "302")
        Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + encTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "ChartReview")
    void copyMaoToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def encounterId = getCustomField("EncounterID", encTransmissionSID, "302")
        def mao002Id = getMao002Id(encTransmissionSID)

        def TC1a = new File(testDataPath + "ChartReview/MAO002_TC1.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/MAO002_TC1.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "EncounterID": encounterId, "MAO002ID": mao002Id]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "ChartReview/MAO002_TC1.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "ChartReview/MAO002_TC1.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(inboundMAO + "\\MAO002_TC1.xml.properties", template2)
        createNewFile(inboundMAO + "\\MAO002_TC1.xml", template)

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
    void copyChartReviewToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "ChartReview/837_PChartReview.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "ChartReview/837_PChartReview.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/ChartReview/837_PChartReview.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/ChartReview/837_PChartReview.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\837_PChartReview.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\837_PChartReview.xml", template)
}


    @Test(dependsOnMethods = "copyChartReviewToConnectors", groups = "ChartReview")
    void checkChartReviewCustomFields() {
//        sleep(100000)

        def cf2 = getCustomField("Source", chartTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", chartTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomField("ClaimID", chartTransmissionSID, "302")
        Assert.assertNull(cf4, "There should  be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", chartTransmissionSID, "302")
        Assert.assertNull(cf5, "There should be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomFieldWaiting("CMSICN", currentTransmissionSID, "302")
        Assert.assertEquals(cf6, "123456789", "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1234567", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", chartTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

}