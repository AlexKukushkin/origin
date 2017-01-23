package com.edifecs.qa

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

//TODO:
//TODO:     TO INCREASE GET_TRANSMISSION_SID TO USE SUBMITTED FILE NAME IN SEARCHES,
//TODO:     USING ONLY TRANCMISIONRECEIPTDATETIME DOES NOT GET PROPER TRANSMISSIONSID if other transmissions are generated
//TODO:

/**
 * Created by IntelliJ IDEA.
 * User: LilianP
 * Date: 9/29/11
 * Time: 10:31 AM
 */
public class RegularEncounterIntegrationTest extends UtilsQuery {
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
    def originalTransmissionSID
    def transmissionFileName
    def encTransmissionSID
    def randomProviderClaimNumber
    def randomNumber
    def randomNumber1
    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewSubmitedClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        currentEncTransmissionSID = "ECHCF_TC1_NewSub.xml"
        def TC1a = new File(testDataPath + "RegularEncounters/ECHCF_TC1_NewSub.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/ECHCF_TC1_NewSub.xml doesn't exist!")
        def text = TC1a.getText()
        randomProviderClaimNumber = new Random().nextLong().abs()
        randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber
        println "TrackingID  = " + randomProviderClaimNumber
        def binding = ["UniqueID": randomNumber, "ProviderClaimNumber": randomProviderClaimNumber, "TargetTrackingID": randomProviderClaimNumber]


        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/ECHCF_TC1_NewSub.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/ECHCF_TC1_NewSub.xml.properties doesn't exist!")

        def text2 = TC1b.getText()
        binding = ["TargetTrackingID": randomProviderClaimNumber]
        engine = new GStringTemplateEngine()


        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_NewSub.xml.properties", engine.createTemplate(text2).make(binding))
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_NewSub.xml", template)

//        def today = new Date()
//
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println "Claim Submitted Current Time = " + currentTimeSQLFormat;
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
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
    void checkClaimID() {
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkGroupIdentifiers() {
//        sleep(30000) // works with sleep. not added by me
        def groupCMSEncounterGroup = getGroupWaiting("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNotNull(groupCMSEncounterGroup, "There should be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
// todo check req
// Assert.assertNull(groupCMSEncounterGroup,"There should be group for Claim with GroupIdentifier = "+trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInsClaim")
    void checkExtraGroupIdentifiers() {

        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"

        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        //  Assert.assertNotNull(exGroup,"There is no extra group for GroupIdentifier = "+extraGroup)
        //  Assert.assertNull(exGroup,"There is no extra group for GroupIdentifier = "+extraGroup)
        Assert.assertNull(exGroup, "There should be no extra group for GroupIdentifier = " + extraGroup)

    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCustomFields() {
        // wait till it sets in UI
       // sleep 30000;

//        def cf2 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Source') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf22; activeSql.eachRow(cf2) { cf22 = "$it.FieldValue" }
//        Assert.assertNull(cf22, "There is no Custom field Source=Channel for GroupIdentifier = " + cf2)
        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field Source=Channel for GroupIdentifier = " + cf2)

//        def cf3 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Disposition') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf33; activeSql.eachRow(cf3) { cf33 = "$it.FieldValue" }
//        Assert.assertNull(cf33, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)
        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field Source=Channel for GroupIdentifier = " + cf3)

//        def cf4 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='ClaimID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf44; activeSql.eachRow(cf4) { cf44 = "$it.FieldValue" }
//        Assert.assertNotNull(cf44, "There should not be value Custom field ClaimID with GroupIdentifier = " + cf4)
        def cf4 = getCustomField("ClaimID", currentTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field Source=Channel for GroupIdentifier = " + cf3)

//        def cf5 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='EncounterID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf55; activeSql.eachRow(cf5) { cf55 = "$it.FieldValue" }
//        Assert.assertNull(cf55, "There should not be value Custom field EncounterID with GroupIdentifier = " + cf5)
        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There is no Custom field Source=Channel for GroupIdentifier = " + cf5)

//        def cf6 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='CMSContractID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf66; activeSql.eachRow(cf6) { cf66 = "$it.FieldValue" }
//        Assert.assertEquals(cf66, "H1036", "There should not be value Custom field EncounterID with GroupIdentifier = " + cf6)
        def cf6 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf6, "H1036", "There should not be value Custom field EncounterID with GroupIdentifier = " + cf6)

//        def cf7 = "select * from dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
//        def cf77; activeSql.eachRow(cf7) { cf77 = "$it.FieldValue" }
//        Assert.assertNotNull(cf77, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf7)
        def cf7 = getCustomFieldWaiting("LastInternalActivityDate", currentTransmissionSID, "302", "CFDateTimeValue")
        Assert.assertNotNull(cf7, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf7)

    }

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInsClaim")
    void checkOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")

        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        //   Assert.assertNotNull(claim,"Cannot find Ops Repository claim attachment for trackingID = "+trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        //Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim Data attachment for trackingID = "+trackingIdentifier);
        //Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("EC-CF")),"Cannot find Ops Repository claim data for trackingID = "+trackingIdentifier);
        initialClaimDataXML = new StringReader(new String(claimData))

        // commented because after NewSubmitted is processed , NO attachment should be present in ops repository
        /*
        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
        Assert.assertNotNull(claimAtt,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("NATIVE")),"Cannot find Ops Repository attachment for trackingID = "+trackingIdentifier);
        initialClaimAttEDI = new StringReader(new String(claimAtt))
        */
    }

    /* Check Event for the claim */

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewSubmitedClaim")
    void checkClaimSubmittedDirectlyEvent() {
//        sleep 60000;
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim Submitted Directly", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "There is no Events for Claim for transmission SID= " + currentTransmissionSID)
        }
    }

    /*    Process ECHCF_TC1_EncRep.xml+ properties file in : []:\Edifecs\EUO\CLM\RIM\working\Inbound\InternalClaimUpdates     */

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "ECHCF_EncRep")
    void copyECHCF_EncounterReport_ToInternalClaimUpdates() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/ECHCF_TC1_EncRep.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/ECHCF_TC1_EncRep.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": randomProviderClaimNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/ECHCF_TC1_EncRep.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/ECHCF_TC1_EncRep.xml.properties doesn't exist!")

        text = TC1b.getText()
        binding = ["TargetTrackingID": randomProviderClaimNumber]
        def template2 = engine.createTemplate(text).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_EncRep.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_EncRep.xml", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat + "  second ";
    }

    @Test(dependsOnMethods = "copyECHCF_EncounterReport_ToInternalClaimUpdates", groups = "ECHCF_EncRep")
    void copyECHCF_EncounterReport2_ToInternalClaimUpdates() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/ECHCF_TC1_EncRep2.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/ECHCF_TC1_EncRep2.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": randomProviderClaimNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/ECHCF_TC1_EncRep2.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/ECHCF_TC1_EncRep2.xml.properties doesn't exist!")

        text = TC1b.getText()
        binding = ["TargetTrackingID": randomProviderClaimNumber]
        def template2 = engine.createTemplate(text).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_EncRep2.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_TC1_EncRep2.xml", template)

    }

    /* Get Ops Repository Attachment*/
//    @Test (dependsOnMethods = "copyECHCF_EncounterReport_ToInternalClaimUpdates", groups = "ECHCF_EncRep")
//    void checkUpdatedClaimData(){
//        sleep 30000; // works ok with sleep
//        def claim = repository.getItem("Claim",trackingIdentifier,new ContentContext().forAttachment("EC-CF"))
////        Assert.assertNotNull(claim,"Cannot find Ops Repository claim attachment for trackingID = "+trackingIdentifier);
//
//        def updatedClaimData = repository.getData("Claim",trackingIdentifier, "EC-CF")
//
//       // Assert.assertNotNull(updatedClaimData,"Cannot find Ops Repository Claim Data attachment for trackingID = "+trackingIdentifier);
//    //    Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("EC-CF")),"Cannot find Ops Repository claim attachment for trackingID = "+trackingIdentifier);
//
//        def updatedClaimDataXMLinitialClaimDataXML = new String(updatedClaimData)
//        if (initialClaimDataXML==updatedClaimDataXMLinitialClaimDataXML) Assert.fail("Claim for trackingID"+trackingIdentifier+" was not updated in OpsRepository \n BUG:94475");
//
////        creatNewFile("./src/test/resources/updatedClaimDataXML.xml",updatedClaimDataXMLinitialClaimDataXML);
//    }

    /* Check Event for the claim */

    @Test(dependsOnMethods = "copyECHCF_EncounterReport2_ToInternalClaimUpdates", groups = "ECHCF_EncRep")
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

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "checkEncounterEvent", groups = "NewInsClaim")
    void getEncounterClaimTransmissionSID() {
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkAckReconID() {
        ackReconIntID = getCorrelationIDWaiting("343", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconIntID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconFGID = getCorrelationIDWaiting("331", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconFGID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)

        ackReconTrID = getCorrelationIDWaiting("397", "AckReconID", encTransmissionSID)
        Assert.assertNotNull(ackReconTrID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterGroupIdentifiers() {
//        sleep 30000
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

        //def groupTrEncGroup= getGroup("AckGroup","837Txn", ackReconTrID,"397", encTransmissionSID )
        //Assert.assertNotNull(groupTrEncGroup,"There is no groupCMSEncounterGroup for GroupIdentifier = "+groupIntEncGroup)

    }

    //todo: is this to be commented??
//    @Test (dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
//    void checkEncounterOpsData(){
//    waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
//        def claim = repository.getItem("Claim",trackingIdentifier,new ContentContext().forAttachment("EC-CF"))
//        Assert.assertNotNull(claim,"Cannot find Ops Repository claim attachment for trackingID = "+trackingIdentifier);
//
//        def claimData = repository.getData("Claim",trackingIdentifier, "EC-CF")
//        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim Data attachment for trackingID = "+trackingIdentifier);
//        Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("EC-CF")),"Cannot find Ops Repository claim data for trackingID = "+trackingIdentifier);
//        updatedClaimDataXML = new StringReader(new String(claimData))
//        //Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")
//
//
//        //was NATIVE changed to EC-CF , like in DB
//        //def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
//        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "EC-CF")
//        //its not claimData, its claimAtt for: Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
//        Assert.assertNotNull(claimAtt,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
//        Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("NATIVE")),"Cannot find Ops Repository attachment for trackingID = "+trackingIdentifier);
//        updatedClaimAttEDI = new StringReader(new String(claimAtt))
//        //Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")
//        //todo modify method of comparing two xmls
//    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
//        sleep 30000


        def cf2 = getCustomField("Source", encTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", encTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", encTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should be Custom field for ClaimID with GroupIdentifier = " + cf4)


        def cf5 = getCustomFieldWaiting("EncounterID", encTransmissionSID, "302")
        Assert.assertNotNull(cf5, "There should be Custom field for ClaimID with GroupIdentifier =" + cf5)


        def cf6 = getCustomField("CMSICN", encTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", encTransmissionSID, "302")
        Assert.assertEquals(cf7, "H1036", "There should be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
//        Assert.assertEquals(cf8,"Chart Review No ICN","There should be Custom field for SubmissionType with GroupIdentifier ="+cf8)
        Assert.assertEquals(cf8, "Initial", "There should be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomFieldDT("LastInternalActivityDate", encTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)

    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstTA1")
    void copyTA1ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_TA1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_TA1.dat doesn't exist!")
        def text = TC1a.getText()

        def intContNumSelect = "Select InterchangeControlNumber from Interchange where TransmissionSID = " + encTransmissionSID
        def intContNum
        activeSql.eachRow(intContNumSelect) {
            intContNum = it.InterchangeControlNumber
        }

        def controlNumber = createControlNumber(intContNum)
        def binding = ["IntContNum": controlNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\837_TC3_TA1.dat", template)
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

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInst999")
    void copy999ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_999.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_999.dat doesn't exist!")
        def text = TC1a.getText()

        def grContNumSelect = "Select GroupControlNumber from FunctionalGroup where TransmissionSID = " + encTransmissionSID
        def grContNum
        activeSql.eachRow(grContNumSelect) {
            grContNum = it.GroupControlNumber
        }

        // def controlNumber = createControlNumber(grContNum)
        def binding = ["GroupContNum": grContNum]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\837_TC3_999.dat", template)
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

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999GroupIdentifiers() {
//        sleep(3000)
        def group999EncounterGroup = getGroupSimpleWaiting("AckGroup", "999", "397", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999Identifiers() {
        def group999EncounterGroup = getCorrelationIDWaiting("397", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst277")
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837_TC3_277CA_m.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC3_277CA_m.dat doesn't exist!")
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
//        sleep(3000)
        def group999EncounterGroup = getGroupSimpleWaiting("AckGroup", "STN", "489", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277 group for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277Identifiers() {
        // AckReconID
        def group999EncounterGroup = getCorrelationIDWaiting("397", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 277CA  TrackingID indentifier for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277AckReconIDIdentifier() {

        // should be empty?
        def group999EncounterGroup = getCorrelationID("489", "AckReconID", currentTransmissionSID)
        //  def group999EncounterGroup=getCorrelationID("397", "TrackingID", currentTransmissionSID)
        Assert.assertNull(group999EncounterGroup, "There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }
}