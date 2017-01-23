package com.edifecs.qa.RegularEncounters

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: AnaL
 * Date: 02.02.2012
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
class RegularEncounter3ServiceLinesEDITest extends UtilsQuery {
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
    def randomProviderClaimNumber
    def randomNumber
    def grIdentifier

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/1b/837_PNEW_3SV.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/1b/837_PNEW_3SV.dat doesn't exist!")
        def text = TC1a.getText()
        randomProviderClaimNumber = new Random().nextLong().abs()
        println "TrackingID  = " + randomProviderClaimNumber
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomProviderClaimNumber]

        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/1b/837_PNEW_3SV.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/1b/837_PNEW_3SV.dat.properties doesn't exist!")

        def text1 = TC1b.getText()
        engine = new GStringTemplateEngine()
        def template1 = engine.createTemplate(text1).make(binding)

        createNewFile(inboundFileConnector + "\\837_PNEW_3SV.dat.properties", template1)
        createNewFile(inboundFileConnector + "\\837_PNEW_3SV.dat", template)

//        def today = new Date()
//
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println "Claim Submitted Current Time = " + currentTimeSQLFormat;
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
        def SubmittedClaimID = "select * from ExternalCorrelationAssociation where InstanceType=302 and TransmissionSID=" + currentTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='SubmittedClaimID')"
        activeSql.eachRow(SubmittedClaimID) { trackingIdentifier = "$it.ExternalCorrelationIdValue" }
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def InternalClaimIDValue = "select * from ExternalCorrelationAssociation where InstanceType=302 and TransmissionSID=" + currentTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='InternalClaimID')"
        def internalID; activeSql.eachRow(InternalClaimIDValue) { internalID = "$it.ExternalCorrelationIdValue" }
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkGroupIdentifiers() {
        // sleep 30000
        def groupCMSEncounterGroup = "select * from GroupIdentifier where GroupIdentifierDefSID in" +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='CMSEncounterGroup')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='Claim')" +
                "and GroupIdentifier='" + trackingIdentifier + "' and InstanceTypeID=302"
        def gr; activeSql.eachRow(groupCMSEncounterGroup) { gr = "$it.GroupIdentifierSID" }
        //  Assert.assertNotNull(gr,"There is no groupCMSEncounterGroup for GroupIdentifier = "+groupCMSEncounterGroup)
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
        def cf2 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Source') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf22; activeSql.eachRow(cf2) { cf22 = "$it.FieldValue" }
        Assert.assertNull(cf22, "There is no Custom field Source=Channel for GroupIdentifier = " + cf2)

        def cf3 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Disposition') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf33; activeSql.eachRow(cf3) { cf33 = "$it.FieldValue" }
        Assert.assertNull(cf33, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)

        def cf4 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='ClaimID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf44; activeSql.eachRow(cf4) { cf44 = "$it.FieldValue" }
        Assert.assertNull(cf44, "There should not be value Custom field ClaimID with GroupIdentifier = " + cf4)

        def cf5 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='EncounterID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf55; activeSql.eachRow(cf5) { cf55 = "$it.FieldValue" }
        Assert.assertNull(cf55, "There should not be value Custom field EncounterID with GroupIdentifier = " + cf5)
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

//        def claimAtt = repository.getAttachment("Claim", trackingIdentifier, "NATIVE")
//        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        initialClaimAttEDI = new StringReader(new String(claimAtt))
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/1b/835_PNEW_3SV_TC1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/1b/835_PNEW_3SV_TC1.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/1b/835_PNEW_3SV_TC1.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/1b/835_PNEW_3SV_TC1.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\835_PNEW_3SV_TC1.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_PNEW_3SV_TC1.dat", template)

//        def today = new Date()
//
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEventAudit1() {
        // sleep 90000;
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Encounter Trigger Cancelled", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    /* Test Event encounter for Claim*/

    @Test(dependsOnMethods = "checkGroupIdentifiers", groups = "NewInstPayment")
    void copyPaymentToConnectors2() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/1b/835_PNEW_3SV_TC2.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/1b/835_PNEW_3SV_TC2.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/RegularEncounters/1b/835_PNEW_3SV_TC2.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/RegularEncounters/1b/835_PNEW_3SV_TC2.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(paymentAsClaimUpdate + "\\835_PNEW_3SV_TC2.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_PNEW_3SV_TC2.dat", template)

//        def today = new Date()
//
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
    }



    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkEncounterEvent() {
        // sleep 30000

        def select = "select ClaimSID from Claim where TransmissionSID=" + currentTransmissionSID
        def result; activeSql.eachRow(select) { result = "$it.ClaimSID" }
//        def updateAudit = "select * from EdifecsBaseEvent where Message='Claim successfully met conditions for encounter generation.' and PrimaryInstanceSID='" + result + "' and PrimaryInstanceTypeID=302"
//        def upAudit; activeSql.eachRow(updateAudit) { upAudit = "$it.EdifecsBaseEventSID" }
//
//        def updateAudit = "select * from EdifecsBaseEvent where Message='Claim successfully met conditions for encounter generation.' and PrimaryInstanceSID='" + result + "' and PrimaryInstanceTypeID=302"
        def eventAudit = getEvent1("Claim successfully met conditions for encounter generation.", result, "302")
        Assert.assertNotNull(eventAudit, "Update Event with Message='Claim successfully met conditions for encounter generation ' is missing for TransmissionSID =" + currentTransmissionSID)
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkUpdatedCustomFields() {
        sleep 30000
        def cf1 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='CMSContractID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf11; activeSql.eachRow(cf1) { cf11 = "$it.FieldValue" }
        Assert.assertEquals(cf11, "R5826", "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf1)

        //The custom field SubmissionType is updated for the encounter transmission. Updated sql to pass encTransmissionSID
        def cf2 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='SubmissionType') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf22; activeSql.eachRow(cf2) { cf22 = "$it.FieldValue" }
        Assert.assertEquals(cf22, "Initial", "There should be Custom field SubmissionType=Initial for GroupIdentifier = " + cf2)

        def cf3 = "select * from dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf33; activeSql.eachRow(cf3) { cf33 = "$it.FieldValue" }
        Assert.assertNotNull(cf33, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf3)

        def cf4 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='CMSICN') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf44; activeSql.eachRow(cf4) { cf44 = "$it.FieldValue" }
        Assert.assertNull(cf44, "CMSICN should be null for GroupIdentifier = " + cf4)

        def cf5 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='EncounterID') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf55; activeSql.eachRow(cf5) { cf55 = "$it.FieldValue" }
        Assert.assertNull(cf55, "There should not be value Custom field EncounterID with GroupIdentifier = " + cf5)

    }

    /* Check after update InternalClaimID  are proper in DB */

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInstPayment")
    void checkInternalClaimID() {
        def InternalClaimIDValue = "select * from ExternalCorrelationAssociation where InstanceType=302 and TransmissionSID=" + currentTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='InternalClaimID')"
        def internalID; activeSql.eachRow(InternalClaimIDValue) { internalID = "$it.ExternalCorrelationIdValue" }
        Assert.assertNotNull(internalID, "There should be InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyPaymentToConnectors2", groups = "NewInsClaim")
    void getEncounterClaimTransmissionSID() {
        // sleep 30000
        encTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID, "Encounter is not generated!!! Report Blocker bug.")
    }

    /* Check encounter AckReconID are proper in DB */

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkAckReconID() {
        try {
            def AckReconIDIntValue = "select * from ExternalCorrelationAssociation where InstanceType=343 and TransmissionSID=" + encTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='AckReconID')"
            ackReconIntID; activeSql.eachRow(AckReconIDIntValue) { ackReconIntID = "$it.ExternalCorrelationIdValue" }
        } catch (Exception e) {
            Assert.assertNotNull(ackReconIntID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID + e)
        }
        try {
            def AckReconIDFGValue = "select * from ExternalCorrelationAssociation where InstanceType=331 and TransmissionSID=" + encTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='AckReconID')"
            ackReconFGID; activeSql.eachRow(AckReconIDFGValue) { ackReconFGID = "$it.ExternalCorrelationIdValue" }
        } catch (Exception e) {
            Assert.assertNotNull(ackReconFGID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID + e)
        }
        try {
            def AckReconIDTrValue = "select * from ExternalCorrelationAssociation where InstanceType=331 and TransmissionSID=" + encTransmissionSID + " and ExternalCorrelationIdSID in (select ExternalCorrelationIdSID from ExternalCorrelationId where Name='AckReconID')"
            ackReconTrID; activeSql.eachRow(AckReconIDTrValue) { ackReconTrID = "$it.ExternalCorrelationIdValue" }
        } catch (Exception e) {
            Assert.assertNotNull(ackReconTrID, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID + e)
        }
        //groupIdentifier
        try {
            def groupIdentifierValue = "select * from GroupIdentifier where InstanceTypeID=397 and TransmissionSID=" + encTransmissionSID
            grIdentifier; activeSql.eachRow(groupIdentifierValue) { grIdentifier = "$it.GroupIdentifier" }
        } catch (Exception e) {
            Assert.assertNotNull(grIdentifier, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID + e)
        }
    }

    /* Check Encounter Group Identifiers*/


    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterGroupIdentifiers() {
//        sleep 30000
        def groupIntEncGroup = getGroupWaiting("AckGroup", "Interchange", ackReconIntID, "343", encTransmissionSID)
        Assert.assertNotNull(groupIntEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupFGEncGroup = getGroupWaiting("AckGroup", "FuncGroup", ackReconFGID, "331", encTransmissionSID)
        Assert.assertNotNull(groupFGEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupTrEncGroup = getGroupWaiting("AckGroup", "837Txn", grIdentifier, "397", encTransmissionSID)
        Assert.assertNotNull(groupTrEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)
    }

    /* Get Ops Repository data and attachment*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF")), "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        //    Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")

        /*
        satish - Disabling this check as there is more than one record of type NATIVE being created in the OPS Repository.
        The ops repository library has to be updated to handle this, commenting this check until the library is updated.

        def claimAtt = repository.getAttachment("Claim", trackingIdentifier, "NATIVE")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim attachment for trackingID = " + trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
        updatedClaimAttEDI = new StringReader(new String(claimAtt))
        //   Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")
        */
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
        sleep 30000

        def cf2 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Source') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf22; activeSql.eachRow(cf2) { cf22 = "$it.FieldValue" }
        Assert.assertNull(cf22, "There is no Custom field Source=Gateway for GroupIdentifier = " + cf2)

        def cf3 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='Disposition') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf33; activeSql.eachRow(cf3) { cf33 = "$it.FieldValue" }
        Assert.assertNull(cf33, "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)

        def cf4 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='ClaimID') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf44; activeSql.eachRow(cf4) { cf44 = "$it.FieldValue" }
        Assert.assertNotNull(cf44, "There should be value Custom field ClaimID with GroupIdentifier = " + cf4)

        def cf5 = "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='EncounterID') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf55; activeSql.eachRow(cf5) { cf55 = "$it.FieldValue" }
        Assert.assertNotNull(cf55, "There should be value Custom field EncounterID with GroupIdentifier = " + cf5)
    }

    /* Copy TA1 to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstTa1")
    void copyTA1ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837P_TC1_TA1.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC1_TA1.dat doesn't exist!")
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

        createNewFile(receiveInboundAcks + "\\837P_TC1_TA1.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    /* Verify TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyTA1ToConnectors", groups = "NewInsClaim")
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

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInst999")
    void copy999ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "RegularEncounters/837P_TC1_999.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC1_999.dat doesn't exist!")
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

        createNewFile(receiveInboundAcks + "\\837P_TC1_999.dat", template)
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
//        sleep(10000)
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
        def TC1a = new File(testDataPath + "RegularEncounters/837P_TC1_277CA.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "RegularEncounters/837_TC1_277CA.dat doesn't exist!")
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

        createNewFile(receiveInboundAcks + "\\837P_TC1_277CA.dat", template)
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
        // sleep(3000)
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
        Assert.assertNull(group999EncounterGroup, "There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = " + currentTransmissionSID)
        // According to last Requirements AckReconID should be empty
    }

}
