package com.edifecs.qa.utils

import com.sun.xml.internal.bind.v2.TODO

/**
 * Created with IntelliJ IDEA.
 * User: InaG
 * Date: 7/1/13
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
class SimpleTests extends CopyFiles{
    def currentTimeSQLFormat
    def currentEncTimeSQLFormat
    def currentAcksTimeSQLFormat
    def currentAdjEncTimeSQLFormat

    public void inboundClaim(String fileType, String path1, String path2){
        copyXMLs(fileType, path1, path2)
        currentTimeSQLFormat = createCurrentTimeSQLFormat()
        currentTransmissionSID = getTransmissionSID(currentTimeSQLFormat)
        //check Claim is loaded in TM
        checkTransmissionSID(currentTransmissionSID)
        checkClaimID(currentTransmissionSID)
    }

    public void claimUpdates(String fileType, String path1, String path2){
        copyXMLs(fileType, path1, path2)
        // TODO: add check for nodes/properties creation in ECF and check for properties updates in ECF
    }

    public void claimTrigger(String fileType, String path1, String path2){
        copyXMLs(fileType, path1, path2)
        currentEncTimeSQLFormat = createCurrentTimeSQLFormat()
        encTransmissionSID = getTransmissionSID(currentEncTimeSQLFormat)
        //check Encounter is loaded in TM
        checkTransmissionSID(encTransmissionSID)
        // TODO: add check for nodes/properties creation in ECF and check for properties updates in ECF
    }

    public void claimAcks(String fileType, String path){
        copyAcks(fileType, path)
        currentAcksTimeSQLFormat = createCurrentTimeSQLFormat()
        ackTransmissionSID = getTransmissionSID(currentAcksTimeSQLFormat)
        //check Ack is loaded in TM
        checkTransmissionSID(ackTransmissionSID)
        // TODO: add check for nodes/properties creation in ECF and check for properties updates in ECF
    }

    public void claimAdjustment(String fileType, String path1, String path2){
        copyXMLs(fileType, path1, path2)
        // TODO: add check for nodes/properties creation in ECF and check for properties updates in ECF
    }

    public void paymentAdjustment(String fileType, String path1, String path2){
        copyXMLs(fileType, path1, path2)
        currentAdjEncTimeSQLFormat = createCurrentTimeSQLFormat()
        encAdjTransmissionSID = getTransmissionSID(currentAdjEncTimeSQLFormat)
        //check Encounter is loaded in TM
        checkTransmissionSID(encAdjTransmissionSID)
        // TODO: add check for nodes/properties creation in ECF and check for properties updates in ECF
    }

}
