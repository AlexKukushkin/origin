package com.edifecs.qa.Acknowledgement

import com.edifecs.qa.utils.UtilsQuery
import org.testng.annotations.Test
import org.testng.annotations.Listeners
import com.edifecs.qa.CuantoListener

/**
 * Created by IntelliJ IDEA.
 * User: AnaL
 * Date: 09.02.2012
 * Time: 12:11
 * To change this template use File | Settings | File Templates.
 */

@Listeners(com.edifecs.qa.CuantoListener)
class Acknowledgement999ClaimTest extends UtilsQuery {
    def currentTimeSQLFormat
//    def randomID
    def trackingIdentifier
//    def initialClaimDataXML
//    def initialClaimAttEDI
//    def currentEncTransmissionSID
    def ackReconTrID
    def ackReconIntID
    def ackReconFGID
//    def ackReconClID
//    def updatedClaimDataXML
//    def updatedClaimAttEDI
    def currentEncTimeSQLFormat
    def currentTransmissionSID
    def encTransmissionSID
    def randomNumber
//    def groupIdentifier
//    def randomNr
//    def randomNr2
//    def updatedClaimDataXML2

    /**
     *  Get TransmissionSID verify TM DB Schema exists
     */
    @Test(groups = "NewClaim")
    void getClaimTransmissionSID() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        randomNumber = createControlNumber(new Random().nextInt(999999999))

        def binding = ["UniqueID": randomNumber]
        copyFileAndPropertiesToConnector("837_Prof_TC1.dat", "Acknowledgement/999Test", inboundFileConnector, binding)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
    }

    /**
     *  Check SubmittedClaimID and InternalClaimID  are proper in DB
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewClaim")
    void checkClaimID() {
        trackingIdentifier = checkCorrelationIDNotNull("302", "SubmittedClaimID", currentTransmissionSID)
        checkCustomFieldNull("InternalClaimID", currentTransmissionSID, "302")
    }

    /**
     *  Check Group Identifiers
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewClaim")
    void checkGroupIdentifiers() {
        checkGroupNull("CMSEncounterGroup", "Claim", "302", currentTransmissionSID, trackingIdentifier)
    }

    /**
     * Check extra Group Identifiers
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewClaim")
    void checkExtraGroupIdentifiers() {
        checkExtraGroupIds(currentTransmissionSID)
    }

    /**
     * Check Custom Fields
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewClaim")
    void checkCustomFields() {
        checkCustomFieldNull("Source", currentTransmissionSID, "302")
        checkCustomFieldNull("Disposition", currentTransmissionSID, "302")
        checkCustomFieldNull("ClaimID", currentTransmissionSID, "302")
        checkCustomFieldNull("EncounterID", currentTransmissionSID, "302")
        checkCustomFieldNull("CMSICN", currentTransmissionSID, "302")
        checkCustomFieldNull("CMSContractID", currentTransmissionSID, "302")
        checkCustomFieldNull("SubmissionType", currentTransmissionSID, "302")
        checkCustomFieldNull("LastInternalActivityDate", currentTransmissionSID, "302")
    }

    /**
     * Get Ops Repository data and attachment
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "NewClaim")
    void checkOpsData() {
        checkClaimOperRepoData(trackingIdentifier)
    }

    /**
     * Copy files to connectors.Verify that ETL processed them in 5 seconds
     */
    @Test(dependsOnMethods = "getClaimTransmissionSID", groups = "Encounter")
    void getEncounterClaimTransmissionSID() {
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()

        def binding = ["TargetTrackingID": trackingIdentifier]
        copyFileAndPropertiesToConnector("835_TC1.dat", "Acknowledgement/999Test", paymentAsClaimUpdate, binding)
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
    }

    /**
     * Test Event AUDIT for Claim
     */
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkEncounterEvent() {
        checkEventNotNull("Claim successfully met conditions for encounter generation", "302", currentTransmissionSID)
    }

    /**
     * Check Custom Fields
     */
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkUpdatedCustomFields() {
        checkCustomFieldNotNull("Source", currentTransmissionSID, "302")
        checkCustomFieldEquals("In Progress", "Disposition", currentTransmissionSID, "302")
        checkCustomFieldNotNull("ClaimID", currentTransmissionSID, "302")
        checkCustomFieldNull("EncounterID", currentTransmissionSID, "302")
        checkCustomFieldNull("CMSICN", currentTransmissionSID, "302")
        checkCustomFieldEquals("R5826", "CMSContractID", currentTransmissionSID, "302")
        checkCustomFieldEquals("Initial", "SubmissionType", currentTransmissionSID, "302")
        checkCustomFieldNull("LastInternalActivityDate", currentTransmissionSID, "302")
    }

    /**
     * Check after update InternalClaimID are proper in DB
     */
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkInternalClaimID() {
        checkCorrelationIDNotNull("302", "InternalClaimID", currentTransmissionSID)
    }

    /**
     * Check encounter AckReconID are proper in DB
     */
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Adjusted")
    void checkAckReconID() {
        ackReconIntID = checkCorrelationIDNotNull("343", "AckReconID", encTransmissionSID)
        ackReconFGID = checkCorrelationIDNotNull("331", "AckReconID", encTransmissionSID)
        ackReconTrID = checkCorrelationIDNotNull("397", "AckReconID", encTransmissionSID)
    }

    /**
     * Check encounter AckReconID are proper in DB
     */
    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkSubmittedClaimID() {
        checkCorrelationIDNotNull("302", "SubmittedClaimID", encTransmissionSID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkEncounterGroupIdentifiers() {
        checkGroupNotNull("AckGroup", "Interchange", "343", encTransmissionSID, ackReconIntID)
        checkGroupNotNull("AckGroup", "FuncGroup", "331", encTransmissionSID, ackReconFGID)
        checkGroupNotNull("AckGroup", "837Txn", "397", encTransmissionSID, ackReconTrID)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkEncounterOpsData() {
        checkEncounterOperRepoData(trackingIdentifier)
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Encounter")
    void checkEncounterCustomFields() {
        checkCustomFieldNull("Source", encTransmissionSID, "302")
        checkCustomFieldNull("Disposition", encTransmissionSID, "302")
        checkCustomFieldNotNull("ClaimID", encTransmissionSID, "302")
        checkCustomFieldNotNull("EncounterID", encTransmissionSID, "302")
        checkCustomFieldNull("CMSICN", encTransmissionSID, "302")
        checkCustomFieldEquals("R5826", "CMSContractID", encTransmissionSID, "302")
        checkCustomFieldEquals("Initial", "SubmissionType", encTransmissionSID, "302")
        checkCustomFieldNull("LastInternalActivityDate", encTransmissionSID, "302")
    }

    @Test(dependsOnMethods = "getEncounterClaimTransmissionSID", groups = "Ack999")
    void get999TransmissionSID() {
        currentTimeSQLFormat = createCurrentTimeSQLFormat()

        def groupControlNumber = getGroupControlNumber(encTransmissionSID)
        def binding = ["GroupContNum": groupControlNumber, "TargetTrackingID": trackingIdentifier, "UniqueID": randomNumber]
        copyFileToConnector("999_1Rej_TC1.dat", "Acknowledgement/999Test", receiveInboundAcks, binding)
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "Ack999")
    void check999GroupIdentifiers() {
        checkGroupNotNull("AckGroup", "999", "397", currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "Ack999")
    void check999Identifiers() {
        checkCorrelationIDNotNull("397", "TrackingID", currentTransmissionSID)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "Ack999")
    void checkCurrentState999() {
        checkCurrentState(encTransmissionSID)
    }

    @Test(dependsOnMethods = "get999TransmissionSID", groups = "Ack999")
    void check1Event999() {
        checkEventNotNull("Negative 999 Acknowledgement received from partner indicating functional group is rejected", "331", encTransmissionSID)
    }


    @Test(dependsOnMethods = "check1Event999", groups = "Ack999")
    void check2Event999() {
        checkEventNotNull("Negative 999 Acknowledgement received from partner indicating transaction is rejected for compliance issues", "397", encTransmissionSID)
    }

    @Test(dependsOnMethods = "check2Event999", groups = "Ack999")
    void check2Exception999() {
        checkClaimException(encTransmissionSID)
    }

    @Test(dependsOnMethods = "check1Event999", groups = "Ack999")
    void checkCurrentActivityState5() {
        checkCurrentActivityStateWaiting("Transmission", "TransmissionSID", encTransmissionSID, "-1000")

        def claimID = getClaimIDString(encTransmissionSID);
        checkCurrentActivityStateWaiting("Claim", "ClaimSID", claimID, "-13000")

        def interchangeID = getInterchangeSIDString(encTransmissionSID)
        checkCurrentActivityStateWaiting("Interchange", "InterchangeSID", interchangeID, "-1000")

        def transactionID = getTransactionIDString(encTransmissionSID)
        checkCurrentActivityStateWaiting("TransactionHeader", "TransactionHeaderSID", transactionID, "-13000")

        def funcGroupID = getFunctionalGroupIDString(encTransmissionSID)
        checkCurrentActivityStateWaiting("FunctionalGroup", "FunctionalGroupSID", funcGroupID, "-12000")
    }
}