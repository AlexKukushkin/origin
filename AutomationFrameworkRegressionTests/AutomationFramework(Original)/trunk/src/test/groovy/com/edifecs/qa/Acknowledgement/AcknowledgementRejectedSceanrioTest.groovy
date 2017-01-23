package com.edifecs.qa.Acknowledgement

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: AnaL
 * Date: 23.11.2011
 * Time: 11:52
 * To change this template use File | Settings | File Templates.
 */
import org.testng.annotations.Listeners
import com.edifecs.qa.CuantoListener
@Listeners(com.edifecs.qa.CuantoListener)
class AcknowledgementRejectedSceanrioTest extends UtilsQuery {

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

//    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    @Test(groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;

//      assert true : "Da ii adevarat"
        randomID = generateNumber(100000000, 2147483647)
        // println randomID
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)
        randomNr3 = new Random().nextInt(999999999)

        randomNr = generateNumber(10000, 99999)

        randomNr2 = generateNumber(1, 9)
        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID, "ID": randomNr, "Nr": randomNr2]

        def TC1a = new File(testDataPath + "Acknowledgement/ECHCF_NewSubmitted.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/ECHCF_NewSubmitted.xml doesn't exist!")
        def text1 = TC1a.getText()

        def TC1b = new File(testDataPath + "Acknowledgement/ECHCF_NewSubmitted.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/Acknowledgement/ECHCF_NewSubmitted.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def engine1 = new GStringTemplateEngine()
        def template1 = engine1.createTemplate(text1).make(binding)

        def engine2 = new GStringTemplateEngine()
        def template2 = engine2.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_NewSubmitted.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_NewSubmitted.xml", template1)

        //sleep 60000

    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
        //sleep(60000)
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
        //sleep(60000)
        trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCIDFields() {
        def cid = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(cid, "There is no CID field SubmittedClaimID = " + cid)
    }

    /* Get Ops Repository data and attachment*/


    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCurrentActivityState1() {
//        sleep(60000)
        sleep(10000)
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

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/ECHCF_EncRep.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/ECHCF_EncRep.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier, "Nr": randomNr2]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "Acknowledgement/ECHCF_EncRep.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/Acknowledgement/ECHCF_EncRep.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_EncRep.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\ECHCF_EncRep.xml", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
        //sleep 60000
    }

    /* Test Event AUDIT for Claim*/


    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEncounterEvent() {
        //sleep(60000)
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
        //sleep(30000)
        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "checkInternalClaimID", groups = "NewInsClaim")
    void checkCurrentActivityState2() {
        sleep(15000)
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-4000", "CurrentActivityState for Claim is not Application Processing")

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

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInsClaim")
    void getEncounterClaimTransmissionSID() {
        //sleep(30000)
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState3() {
//           sleep 30000
        sleep 20000
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
            groupIdentifier; activeSql.eachRow(groupIdentifierValue) { groupIdentifier = "$it.GroupIdentifier" }
        } catch (Exception e) {
            Assert.assertNotNull(groupIdentifier, "There should be AckReconIDValue ExternalCorrelationIdValue for TransmissionSID = " + encTransmissionSID + e)
        }
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterGroupIdentifiers() {
        def groupClaimEncGroup = "select * from GroupIdentifier where GroupIdentifierDefSID in" +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='AckGroup')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='Encounter')" +
                "and InstanceTypeID=302"
        def gr; activeSql.eachRow(groupClaimEncGroup) { gr = "$it.GroupIdentifierSID" }
        Assert.assertNotNull(gr, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupClaimEncGroup)

        def groupIntEncGroup = "select * from GroupIdentifier where GroupIdentifierDefSID in" +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='AckGroup')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='Interchange')" +
                "and GroupIdentifier='" + ackReconIntID + "' and InstanceTypeID=343"
        def grInt; activeSql.eachRow(groupIntEncGroup) { grInt = "$it.GroupIdentifierSID" }
        //TO DO Review because value GroupIdentifier='" + ackReconIntID  is null, but in DB exist values
        Assert.assertNotNull(grInt, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupFGEncGroup = "select * from GroupIdentifier where GroupIdentifierDefSID in" +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='AckGroup')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='FuncGroup')" +
                "and GroupIdentifier='" + ackReconFGID + "' and InstanceTypeID=331"
        def grFG; activeSql.eachRow(groupIntEncGroup) { grFG = "$it.GroupIdentifierSID" }

        Assert.assertNotNull(grFG, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupFGEncGroup)
        def groupTrEncGroup = "select * from GroupIdentifier where GroupIdentifierDefSID in" +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='AckGroup')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='837Txn')" +
                "and GroupIdentifier='" + groupIdentifier + "' and InstanceTypeID=397"
        def grTr; activeSql.eachRow(groupTrEncGroup) { grTr = "$it.GroupIdentifierSID" }
        Assert.assertNotNull(grTr, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupTrEncGroup)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterOpsData() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        def claimItem = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claimItem, "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")
        //In groovy with assert can not be compared two xmls
//
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
        //sleep(60000)

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
        Assert.assertEquals(cf7, "H1036", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        //        Assert.assertEquals(cf8,"Chart Review No ICN",  "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = "select * from dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf99; activeSql.eachRow(cf9) { cf99 = "$it.FieldValue" }
        Assert.assertNotNull(cf99, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf9)


    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstTA1")
    void copyTA1ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/RejectedAcks/TA1_1Ack_R.dat")
        def TC = TC1a.exists()
        Assert.assertTrue(TC, testDataPath + "Acknowledgement/RejectedAcks/TA1_1Ack_R.dat doesn't exist!")
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


        createNewFile(receiveInboundAcks + "\\TA1_1Ack_R.dat", template)
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
    //Current Validation Status = Rejected
    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkCurrentStateTA1() {
        //sleep(30000)
        sleep(10000)
        def getCurrentState = "select * from dbo.Interchange where TransmissionSID='" + encTransmissionSID + "' and IsAcknowledgementRequested = 'True'"
        def current; activeSql.eachRow(getCurrentState) { current = "$it.CurrentStatusID" }
        Assert.assertEquals(current, "5000", "There is no TA1 group for Encounter for GroupIdentifier = " + current)
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkEventTA1() {
        //sleep 30000
        List<GroovyRowResult> listInterchangeSID = getInterchangeSID(encTransmissionSID)
        listInterchangeSID.each {
            item ->
                def event = getEvent1("Negative TA1 Acknowledgement received from partner indicating interchange is rejected for control issue", item.values().toString().replace("[", "").replace("]", ""), "343")
                print "${item.values()}"
                Assert.assertNotNull(event, "Audit Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInstTA1")
    void checkExceptionTA1() {
        def interchangeID = getInterchangeSID(encTransmissionSID)
        print interchangeID
        def errors = []
        interchangeID.each {
            def exception = getException(it.InterchangeSID, 343)
            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + encTransmissionSID << "/n"
            }

        }

        if (errors) {
            //print errors
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }
    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState4() {
        sleep 30000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-13000", "CurrentActivityState for Interchange is not External Exception")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-1000", "CurrentActivityState for Transaction is not Channel Processing")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-1000", "CurrentActivityState for FucntionalGroup is not Channel Processing")

    }

    @Test(dependsOnMethods = "getTA1TransmissionSID", groups = "NewInst999")
    void copy999ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/RejectedAcks/999_5010A_1Rej.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/RejectedAcks/999_5010A_1Rej.dat doesn't exist!")
        def text = TC1a.getText()

        def grContNumSelect = "Select GroupControlNumber from FunctionalGroup where TransmissionSID = " + encTransmissionSID
        def grContNum
        activeSql.eachRow(grContNumSelect) {
            grContNum = it.GroupControlNumber
        }

        def binding = ["GroupContNum": grContNum, "UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\999_5010A_1Rej.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    @Test(dependsOnMethods = "copy999ToConnectors", groups = "NewInst999")
    void get999TransmissionSID() {
        //sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999GroupIdentifiers() {
        //sleep(30000)
        def group999EncounterGroup = getGroupSimpleWaiting("AckGroup", "999", "397", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check999Identifiers() {
        def group999EncounterGroup = getCorrelationIDWaiting("397", "TrackingID", currentTransmissionSID)
        Assert.assertNotNull(group999EncounterGroup, "There is no 999 group for Encounter for GroupIdentifier = " + group999EncounterGroup)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void checkCurrentState999() {
        def getCurrentState = "select * from dbo.FunctionalGroup where TransmissionSID='" + encTransmissionSID + "'"
        def current; activeSql.eachRow(getCurrentState) { current = "$it.CurrentStatusID" }
        Assert.assertEquals(current, "5000", "There is no 999 group for Encounter for GroupIdentifier = " + current)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check1Event999() {
        //sleep(60000)
        List<GroovyRowResult> listFunctionalGroupSID = getFunctionalGroupID(encTransmissionSID)
        listFunctionalGroupSID.each {
            item ->
                def event = getEvent1("Negative 999 Acknowledgement received from partner indicating functional group is rejected for control issues, placed on exception worklist for user action", item.values().toString().replace("[", "").replace("]", ""), "331")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }


    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst277")
    void check1Exception999() {
        def groupID = getTransactionID(encTransmissionSID)
        print groupID
        def errors = []
        groupID.each {
            def exception = getException(it.TransactionHeaderSID, 397)
            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + encTransmissionSID << "/n"
            }

        }


        if (errors) {
            //print errors
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }


    }


    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst999")
    void check2Event999() {
        //sleep(60000)
        List<GroovyRowResult> listTransactionSID = getTransactionID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("Negative 999 Acknowledgement received from partner indicating transaction is rejected for control issues, placed on exception worklist for user action", item.values().toString().replace("[", "").replace("]", ""), "397")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }


    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst277")
    void check2Exception999() {
        def groupID = getFunctionalGroupID(encTransmissionSID)
        print groupID
        def errors = []
        groupID.each {
            def exception = getException(it.FunctionalGroupSID, 331)
            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + encTransmissionSID << "/n"
            }

        }


        if (errors) {
            //print errors
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }


    }



    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInsClaim")
    void checkCurrentActivityState5() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-1000", "CurrentActivityState for Claim is not Channel Processing")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-13000", "CurrentActivityState for Interchange is not External Exception")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-13000", "CurrentActivityState for Transaction is not External Exception")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-13000", "CurrentActivityState for FucntionalGroup is not External Exception")

    }



    @Test(dependsOnMethods = "get999TransmissionSID", groups = "NewInst277")
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Acknowledgement/RejectedAcks/277CA_1Ack_Rej.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Acknowledgement/RejectedAcks/277CA_1Ack_Rej.dat doesn't exist!")
        def text = TC1a.getText()

        def originatorTranIdentifier = "Select OriginatorTranIdentifier from ClaimHeader where TransmissionSID = " + encTransmissionSID
        def origTranIdent
        activeSql.eachRow(originatorTranIdentifier) {
            origTranIdent = it.originatorTranIdentifier
            //sleep 20000
        }


        def binding = ["OriginatorTranIdentifier": origTranIdent, "UniqueID1": randomNumber, "TargetTrackingID": randomID, "ID": randomNr, "UniqueID2": randomNr3]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        createNewFile(receiveInboundAcks + "\\277CA_1Ack_Rej.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
    }

    @Test(dependsOnMethods = "copy277ToConnectors", groups = "NewInst277")
    void get277TransmissionSID() {
        //sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check277GroupIdentifiers() {
        //sleep(1000)
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

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check1Event277() {
        //sleep(60000)
        List<GroovyRowResult> listTransactionSID = getClaimID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("Rejected status reported and details posted from 277 External Acknowledgement", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check2Event277() {
        //sleep(60000)
        List<GroovyRowResult> listTransactionSID = getTransactionID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("Negative 277%", item.values().toString().replace("[", "").replace("]", ""), "397")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check3Event277() {
        //sleep(60000)
        List<GroovyRowResult> listTransactionSID = getTransactionID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("Negative 277 Acknowledgement received from partner indicating 837 transaction is rejected for front end edits", item.values().toString().replace("[", "").replace("]", ""), "397")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    //de decomentat dupa ce se clarifica crearea de exceptii

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void checkException277() {
        //sleep 30000
        def claimID = getClaimID(encTransmissionSID)
        print claimID
        def errors = []
        claimID.each {
            def exception = getException(it.ClaimSID, 302)
            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + encTransmissionSID << "/n"
            }

        }


        if (errors) {
            //print errors
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "Encounter")
    void checkEncounterOpsData2() {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);
        def claimItem = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claimItem, "Cannot find Ops Repository claim data for trackingID = " + trackingIdentifier);
        updatedClaimDataXML2 = new StringReader(new String(claimData))
        Assert.assertNotEquals(updatedClaimDataXML2, updatedClaimDataXML, "OPS Claim data should not be equals")
        //In groovy with assert can not be compared two xmls
//
    }

    @Test(dependsOnMethods = "checkEncounterOpsData2", groups = "NewInsClaim")
    void checkCurrentActivityState6() {
        sleep 10000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", encTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "-1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(encTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-13000", "CurrentActivityState for Claim is not External Exception")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        def activityStateInterchange = getCurrentActivityState("Interchange", "InterchangeSID", interchangeID)
        Assert.assertEquals(activityStateInterchange, "-13000", "CurrentActivityState for Interchange is not External Exception")

        def transactionID = getTransactionIDString(encTransmissionSID)
        def activityStateTransaction = getCurrentActivityState("TransactionHeader", "TransactionHeaderSID", transactionID)
        Assert.assertEquals(activityStateTransaction, "-13000", "CurrentActivityState for Transaction is not External Exception")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        def activityStateFuncGroup = getCurrentActivityState("FunctionalGroup", "FunctionalGroupSID", funcGroupID)
        Assert.assertEquals(activityStateFuncGroup, "-13000", "CurrentActivityState for FucntionalGroup is not External Exception")

    }


}
