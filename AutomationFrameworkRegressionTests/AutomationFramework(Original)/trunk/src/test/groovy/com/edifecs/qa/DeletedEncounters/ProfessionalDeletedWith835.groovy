package com.edifecs.qa.DeletedEncounters

import com.edifecs.ops.data.ContentContext
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: valentinat
 * Date: 2/2/12
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
class ProfessionalDeletedWith835 extends UtilsQuery {
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
    def encTransmissionSID
    def randomNumber
    def sleepTime = 60000
    def groupIdentifier

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeletedEncounters/837_P_Deletion.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeletedEncounters/837_P_Deletion.dat doesn't exist!")
        def text = TC1a.getText()
        randomID = new Random().nextLong().abs()
        println randomID
        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

        println randomNumber

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/DeletedEncounters/837_P_Deletion.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/DeletedEncounters/837_P_Deletion.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundFileConnector + "\\837_P_Deletion.dat.properties", template2)
        createNewFile(inboundFileConnector + "\\837_P_Deletion.dat", template)

//        def today = new Date()
//
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
        //sleep(sleepTime)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
        //sleep(30000)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        originalTransmissionSID = currentTransmissionSID
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

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
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
        //sleep(30000)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There should be no value for Custom field Source  for GroupIdentifier = " + cf2)

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

//        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
//        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE")), "Cannot find Ops Repository attachment for trackingID = " + trackingIdentifier);
//        initialClaimAttEDI = new StringReader(new String(claimAtt))
    }

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/
//    @Test (dependsOnMethods = "checkGroupIdentifiers", groups = "NewInstPayment")
    @Test(dependsOnMethods = "checkClaimID", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeletedEncounters/835_P_Deletion.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeletedEncounters/835_P_Deletion.dat doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "DeletedEncounters/835_P_Deletion.dat.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/DeletedEncounters/835_P_Deletion.dat.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(paymentAsClaimUpdate + "\\835_P_Deletion.dat.properties", template2)
        createNewFile(paymentAsClaimUpdate + "\\835_P_Deletion.dat", template)

//        def today = new Date()
//        currentEncTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentEncTimeSQLFormat;
        //sleep 30000
    }

//
//        /* Test Event AUDIT for Claim*/
//        @Test (dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
//        void checkEventAudit2(){
//            sleep 30000
//            List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
//            listClaimSID.each{
//                item -> def event = getEvent1("Claim was modified by UnknownUser : ", item.values().toString().replace("[","").replace("]",""), "302")
//                print "${item.values()}"
//                Assert.assertNotNull(event,"Audit Event is missing for TransmissionSID ="+currentTransmissionSID)
//            }
//        }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkEncounterEvent() {
        //sleep 30000
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    /* Check Custom Fields*/

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkUpdatedCustomFields() {
        //sleep 60000

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field Source=Gateway for GroupIdentifier = " + cf2)

        def cf3 = getCustomFieldWaiting("Disposition", currentTransmissionSID, "302")
        Assert.assertEquals(cf3, "In Progress", "There is no Custom field Disposition=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf5)


        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)
//
        def cf9 = "select * from dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID=302"
        def cf99; activeSql.eachRow(cf9) { cf99 = "$it.FieldValue" }
        Assert.assertNotNull(cf99, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf99)

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
        //sleep 30000
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        Assert.assertNotNull(encTransmissionSID)
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
        //sleep 30000
        def groupIntEncGroup = getGroupWaiting("AckGroup", "Interchange", ackReconIntID, "343", encTransmissionSID)
        Assert.assertNotNull(groupIntEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupFGEncGroup = getGroupWaiting("AckGroup", "FuncGroup", ackReconFGID, "331", encTransmissionSID)
        Assert.assertNotNull(groupFGEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)

        def groupTrEncGroup = getGroupWaiting("AckGroup", "837Txn", groupIdentifier, "397", encTransmissionSID)
        Assert.assertNotNull(groupTrEncGroup, "There is no groupCMSEncounterGroup for GroupIdentifier = " + groupIntEncGroup)
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
        //  Assert.assertNotEquals(updatedClaimDataXML, initialClaimDataXML, "OPS Claim data should not be equals")
        //In groovy with assert can not be compared two xmls

        /*
        def claimAtt = repository.getAttachment("Claim",trackingIdentifier, "NATIVE")
        Assert.assertNotNull(claimData,"Cannot find Ops Repository Claim attachment for trackingID = "+trackingIdentifier);
        Assert.assertNotNull(repository.getItem("Claim",trackingIdentifier, new ContentContext().forAttachment("NATIVE")),"Cannot find Ops Repository attachment for trackingID = "+trackingIdentifier);
        updatedClaimAttEDI = new StringReader(new String(claimAtt))
        Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equals")
               */
        //In groovy with assert can not be compared two xmls
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInstPayment")
    void checkEncounterCustomFields() {
        //sleep(30000)

        def cf2 = getCustomField("Source", encTransmissionSID, "302")
        Assert.assertNull(cf2, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", encTransmissionSID, "302")
        Assert.assertNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)
//        Assert.assertEquals(cf3,"In Progress","There is no Custom field CurrentState=In Progress for GroupIdentifier = "+cf3)

        def cf4 = getCustomFieldWaiting("ClaimID", encTransmissionSID, "302")
        Assert.assertNotNull(cf4, "The Custom field for ClaimID with GroupIdentifier = " + cf4 + " should not be null")

        def cf5 = getCustomFieldWaiting("EncounterID", encTransmissionSID, "302")
        Assert.assertNotNull(cf5, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", encTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", encTransmissionSID, "302")
        Assert.assertEquals(cf7, "R5826", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomFieldWaiting("SubmissionType", encTransmissionSID, "302")
        Assert.assertEquals(cf8, "Initial", "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = "select * from dbo.CFDateTimeValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='LastInternalActivityDate') and TransmissionSID='" + encTransmissionSID + "' and InstanceTypeID=302"
        def cf99; activeSql.eachRow(cf9) { cf99 = "$it.FieldValue" }
        Assert.assertNull(cf99, "Custom field LastInternalActivityDate should not be null for GroupIdentifier = " + cf99)

    }
    // according bug 108369 277 Rejected should be processed for this scenario and exception from
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "NewInst277")
    void copy277ToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeletedEncounters/277CA_Deletion_Acc.dat")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeletedEncounters277CA_Deletion_Acc.dat doesn't exist!")
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

        createNewFile(receiveInboundAcks + "\\277CA_Deletion.dat", template)
//        def today = new Date()
//        currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', today)
//        println currentTimeSQLFormat;
        //sleep(sleepTime)
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
        //Assert.assertNotNull(group999EncounterGroup,"There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = "+currentTransmissionSID)
        Assert.assertNull(group999EncounterGroup, "There is no 277CA AckReconID identifier  for Encounter for GroupIdentifier = " + currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check1Event277() {
        //sleep(30000)
        List<GroovyRowResult> listTransactionSID = getClaimID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("Accepted status reported and details posted from 277 External Acknowledgement", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check2Event277() {
        //sleep(30000)
        List<GroovyRowResult> listTransactionSID = getTransactionID(encTransmissionSID)
        listTransactionSID.each {
            item ->
                def event = getEvent1("277 Acknowledgement received from partner indicating 837 transaction is accepted", item.values().toString().replace("[", "").replace("]", ""), "397")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + encTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
    void check3Event277() {
        //sleep(30000)
        List<GroovyRowResult> listClaimSID = getClaimID(originalTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("277 reported Encounter", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + originalTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "get277TransmissionSID", groups = "NewInst277")
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

        def TC1b = new File(testDataPath + "DeletedEncounters/MAO002_TC1.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "DeletedEncounters/MAO002_TC1.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)
        createNewFile(inboundMAO + "\\MAO002_TC1.xml.properties", template2)
        createNewFile(inboundMAO + "\\MAO002_TC1.xml", template)

    }

    @Test(dependsOnMethods = "copyMaoToConnectors", groups = "NewInst277")
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


    @Test(dependsOnMethods = "copyMaoToConnectors", groups = "NewInstPayment")
    void copyDeletion() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "DeletedEncounters/P-4Deletion.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "DeletedEncounters/P-4Deletion.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/DeletedEncounters/P-4Deletion.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/DeletedEncounters/P-4Deletion.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\P-4Deletion.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\P-4Deletion.xml", template)

    }

    @Test(dependsOnMethods = "copyDeletion", groups = "NewInst277")
    void checkClaimEvent1() {
        List<GroovyRowResult> listClaimSID = getClaimID(originalTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Partner deletion request was received", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + originalTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "copyDeletion", groups = "NewInstPayment")
    void getDeletionTransmissionSID() {
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        originalTransmissionSID = currentTransmissionSID
        try {
            Assert.assertNotNull(currentTransmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
    }

}





  

