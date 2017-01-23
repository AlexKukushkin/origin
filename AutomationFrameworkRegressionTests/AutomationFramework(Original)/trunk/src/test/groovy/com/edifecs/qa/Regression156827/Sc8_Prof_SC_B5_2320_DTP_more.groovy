package com.edifecs.qa.Regression156827

/**
 * Created by c-alexcucu on 9/16/2016.
 */
import com.edifecs.qa.utils.UtilsQuery
import groovy.sql.GroovyRowResult
import groovy.text.GStringTemplateEngine
import org.apache.commons.io.IOUtils
import org.testng.Assert
import org.testng.annotations.Test

class Sc8_Prof_SC_B5_2320_DTP_more extends UtilsQuery {
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
//    def encTransmissionSID
    def randomNumber
    def groupIdentifier
    def randomNr
    def randomNr2

    /* Copy files to connectors. Verify that ETL processed them in 5 seconds*/

    @Test(dependsOnMethods = "checkIfFileConnectorsExist", groups = "NewInsClaim")
    void copyClaimsToConnectors() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Sc_Prof_B5_2320_DTP_date_more_2/837_Claim.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Sc_Prof_B5_2320_DTP_date_more_2/837_Claim.xml doesn't exist!")

        def text = TC1a.getText()
        randomID = new Random().nextLong().abs()

        def randomNumber1 = new Random().nextInt(999999999)
        randomNumber = createControlNumber(randomNumber1)

/*        GlobalVar.n1 = randomNumber;
        GlobalVar.n2 = randomID;*/

        def binding = ["UniqueID": randomNumber, "TargetTrackingID": randomID]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/Sc_Prof_B5_2320_DTP_date_more_2/837_Claim.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/Sc_Prof_B5_2320_DTP_date_more_2/837_Claim.xml.properties doesn't exist!")
        def text2 = TC1b.getText()

        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\837_Claim.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\837_Claim.xml", template)
    }

    /* Get TransmissionSID verify TM DB Schema exists*/

    @Test(dependsOnMethods = "copyClaimsToConnectors", groups = "NewInsClaim")
    void getClaimTransmissionSID() {
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
        trackingIdentifier = getCorrelationID("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)

        def internalID = getCorrelationID("302", "InternalClaimID", currentTransmissionSID)
        Assert.assertNotNull(internalID, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
    }

    /* Check Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "Sc_Prof_B5_2320_DTP_date_more_2", alwaysRun = true)
    void checkGroupIdentifiers() {
        def groupCMSEncounterGroup = getGroup("CMSEncounterGroup", "Claim", trackingIdentifier, "302", currentTransmissionSID)
        Assert.assertNull(groupCMSEncounterGroup, "There should't be group for Claim with GroupIdentifier = " + trackingIdentifier + groupCMSEncounterGroup)
    }

    /* Check extra Group Identifiers*/

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "Sc_Prof_B5_2320_DTP_date_more_2", alwaysRun = true)
    void checkExtraGroupIdentifiers() {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) { exGroup = "$it.GroupIdentifierSID" }
        Assert.assertNull(exGroup, "There is no extra group for GroupIdentifier = " + extraGroup)
    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCurrentActivityState1() {
//        sleep 30000
        sleep 5000
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

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCIDFields() {
        def cid = getCorrelationIDWaiting("302", "SubmittedClaimID", currentTransmissionSID)
        Assert.assertNotNull(cid, "There is no CID field SubmittedClaimID = " + cid)
    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInsClaim")
    void checkCustomFields() {
//        sleep(60000)

        def cf2 = getCustomField("Source", currentTransmissionSID, "302")
        Assert.assertNull(cf2, "There should not be Custom field CurrentState for GroupIdentifier = " + cf2)

        def cf3 = getCustomField("Disposition", currentTransmissionSID, "302")
        Assert.assertNotNull(cf3, "There is no Custom field CurrentState=In Progress for GroupIdentifier = " + cf3)

        def cf4 = getCustomField("ClaimID", currentTransmissionSID, "302")
        Assert.assertNotNull(cf4, "There should not be Custom field for ClaimID with GroupIdentifier = " + cf4)

        def cf5 = getCustomField("EncounterID", currentTransmissionSID, "302")
        Assert.assertNull(cf5, "There should not be Custom field for ClaimID with GroupIdentifier =" + cf5)

        def cf6 = getCustomField("CMSICN", currentTransmissionSID, "302")
        Assert.assertNull(cf6, "There should not be Custom field for EncounterID with GroupIdentifier =" + cf6)

        def cf7 = getCustomFieldWaiting("CMSContractID", currentTransmissionSID, "302")
        Assert.assertEquals(cf7, "R8358", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomField("SubmissionType", currentTransmissionSID, "302")
        Assert.assertNull(cf8, "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)
    }

    @Test(dependsOnMethods = "checkClaimID", groups = "NewInstPayment")
    void checkCurrentState1() {
        def getCurrentState = "select * from dbo.Claim where TransmissionSID='" + currentTransmissionSID + "'"
        def current; activeSql.eachRow(getCurrentState) { current = "$it.CurrentStatusID" }
        Assert.assertEquals(current, "3000", "Claim has CurrentActivityState = " + current)
    }

    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewInstPayment")
    void copyPaymentToConnectors() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        def TC1a = new File(testDataPath + "Sc_Prof_B5_2320_DTP_date_more_2/Payment.xml")
        Assert.assertTrue(TC1a.exists(), testDataPath + "Sc_Prof_B5_2320_DTP_date_more_2/Payment.xml doesn't exist!")
        def text = TC1a.getText()

        def binding = ["TargetTrackingID": trackingIdentifier]
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)

        def TC1b = new File(testDataPath + "/Sc_Prof_B5_2320_DTP_date_more_2/Payment.xml.properties")
        Assert.assertTrue(TC1b.exists(), testDataPath + "/Sc_Prof_B5_2320_DTP_date_more_2/Payment.xml.properties doesn't exist!")
        def text2 = TC1b.getText()
        def template2 = engine.createTemplate(text2).make(binding)

        createNewFile(inboundInternalClaimUpdates + "\\Payment.xml.properties", template2)
        createNewFile(inboundInternalClaimUpdates + "\\Payment.xml", template)
    }

    /* Test Event AUDIT for Claim*/

    @Test(dependsOnMethods = "copyPaymentToConnectors", groups = "NewInstPayment")
    void checkClaimEvent1() {
//        sleep(60000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Claim successfully met conditions for encounter generation", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "checkClaimEvent1", groups = "NewInstPayment")
    void checkClaimEvent2() {
//        sleep(60000)
        List<GroovyRowResult> listClaimSID = getClaimID(currentTransmissionSID)
        listClaimSID.each {
            item ->
                def event = getEvent1("Business rejection during creation of outbound encounter", item.values().toString().replace("[", "").replace("]", ""), "302")
                print "${item.values()}"
                Assert.assertNotNull(event, "Event is missing for TransmissionSID =" + currentTransmissionSID)
        }
    }

    @Test(dependsOnMethods = "checkClaimEvent2", groups = "NewInsClaim")
    void checkCurrentActivityState2() {
//        sleep 60000
        sleep 5000
        def activityStateTransmission = getCurrentActivityState("Transmission", "TransmissionSID", currentTransmissionSID)
        Assert.assertEquals(activityStateTransmission, "1000", "CurrentActivityState for Transmission is not Channel Processing")

        def claimID = getClaimIDString(currentTransmissionSID);
        def activityStateClaim = getCurrentActivityState("Claim", "ClaimSID", claimID)
        Assert.assertEquals(activityStateClaim, "-6000", "CurrentActivityState for Claim is not Application Processing")

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

    @Test(dependsOnMethods = "checkClaimEvent2", groups = "NewInsClaim")
    void checkUpdatedCustomFields() {
//        sleep(60000)

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
        Assert.assertEquals(cf7, "R8358", "There should not be Custom field for CMSContractID with GroupIdentifier =" + cf7)

        def cf8 = getCustomField("SubmissionType", currentTransmissionSID, "302")
        Assert.assertNull(cf8, "There should not be Custom field for SubmissionType with GroupIdentifier =" + cf8)

        def cf9 = getCustomField("LastInternalActivityDate", currentTransmissionSID, "302")
        Assert.assertNull(cf9, "There should not be Custom field for LastInternalActivityDate with GroupIdentifier =" + cf9)

    }


    @Test(dependsOnMethods = "checkUpdatedCustomFields", groups = "Encounter")
    void checkCurrentState2() {
//        sleep 60000
        def getCurrentState = "select * from dbo.Claim where TransmissionSID='" + currentTransmissionSID + "'"
        def current; activeSql.eachRow(getCurrentState) { current = "$it.CurrentStatusID" }
        Assert.assertEquals(current, "3000", "Claim has CurrentActivityState = " + current)
    }

    @Test(dependsOnMethods = "checkClaimEvent2", groups = "Encounter")
    void checkException() {
//        sleep 30000
        def claimID = getClaimID(currentTransmissionSID)
        Assert.assertNotEquals(claimID.size(), 0, "no claims were received for transmission SID = " + currentTransmissionSID)
        print claimID
        def errors = []
        claimID.each {
            def exception = getException(it.ClaimSID, 302)

            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + currentTransmissionSID << "/n"
            } else
            {
                println()
                println("==========================================================")
                println("Business rejection during creation of outbound encounter.")
                println("The exception is expected. The test is passed.")
                println("==========================================================")
                println()
            }
        }
        if (errors) {
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }
    }
}

