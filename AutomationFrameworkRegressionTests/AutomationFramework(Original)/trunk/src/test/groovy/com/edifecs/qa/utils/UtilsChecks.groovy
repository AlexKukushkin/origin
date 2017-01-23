package com.edifecs.qa.utils

import org.testng.Assert

/**
 * Created with IntelliJ IDEA.
 * User: InaG
 * Date: 7/1/13
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilsChecks extends UtilsQuery{

    public void checkTransmissionSID(String transmissionSID){
        //currentTransmissionSID = getTransmissionSID(valueFormat)
        try {
            Assert.assertNotNull(transmissionSID)
        } catch (Exception e) {
            println("Transmission SID shouldn't be NULL" + e)
        }
        println "Transmission ID = " + transmissionSID
    }

    public void checkClaimID(transmissionSID) {
        def trackingIdentifier = getCorrelationIDWaiting("302", "SubmittedClaimID", transmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is no ExternalCorrelationIdValue for TransmissionSID = " + transmissionSID)
        println "SubmittedClaimID = " + trackingIdentifier

        def internalID = getCorrelationIDWaiting("302", "InternalClaimID", transmissionSID)
        Assert.assertNotNull(trackingIdentifier, "There is extra InternalClaimIDValue ExternalCorrelationIdValue for TransmissionSID = " + currentTransmissionSID)
        println "InternalClaimID = " + trackingIdentifier
    }

    public String getControlNumber(String encounterTransmissionSID, String controlNumberType){
        //dbConnect();
        initAll();
        def controlNumber
        def sql
        if(controlNumberType.equals("InterchangeControlNumber")){
            sql = "Select InterchangeControlNumber from Interchange where TransmissionSID = " + encounterTransmissionSID
            activeSql.eachRow(sql) {
                controlNumber = it.InterchangeControlNumber
            }
        }
        else if (controlNumberType.equals("GroupControlNumber")){
            sql = "Select GroupControlNumber from FunctionalGroup where TransmissionSID = " + encounterTransmissionSID
            activeSql.eachRow(sql) {
                controlNumber = it.GroupControlNumber
            }
        }
        return controlNumber
    }

    public String getIdentifierByEncounter(String identifierType, String encounterTransmissionSID){
        //dbConnect()
        initAll()
        def sql
        def identifier
        switch(identifierType){
            case "OriginatorTranIdentifier":
            sql =   "Select OriginatorTranIdentifier from ClaimHeader where TransmissionSID = " + encounterTransmissionSID
            activeSql.eachRow(sql) {
                identifier = it.OriginatorTranIdentifier
            }
                break
            case "ProviderClaimNumber":
            sql = "Select ProviderClaimNumber from Claim where TransmissionSID = " + encounterTransmissionSID
            activeSql.eachRow(sql) {
                identifier = it.ProviderClaimNumber
            }
                break
        }
        return identifier
    }

    public checkXMLElementsUseVersions(String fileType, String elementPath, String expectedValue, String versionName){
        def node = null
        switch (fileType){
            case "Claim":
                node = new XmlSlurper().parseText(claimXML.str).declareNamespace(ns0:'http://xml.edifecs.com/schema/ECHCF',ns1:'http://xml.edifecs.com/schema/UCF',
                        ns2:'http://xml.edifecs.com/schema/UCFD',ns3:'http://xml.edifecs.com/schema/HCFD')
                break
            case "Encounter":
                node = new XmlSlurper().parseText(encounterXML.str).declareNamespace(echcf:'http://xml.edifecs.com/schema/ECHCF',ucf:'http://xml.edifecs.com/schema/UCF',
                        ucfd:'http://xml.edifecs.com/schema/UCFD',hcfd:'http://xml.edifecs.com/schema/HCFD')
                break
        }
        //node.'ns0:ClaimStatus'.'ns3:CurrentState'.@value.text()
        def path = elementPath.split('/')
        if (path.size()== 3)
            Assert.assertEquals(node."${path[0]}"."${path[1]}"."${path[2]}".find{it.@name.text().contains("${versionName}")}.@value.text(),expectedValue)
        else if (path.size() == 4)
            Assert.assertEquals(node."${path[0]}"."${path[1]}"."${path[2]}"."${path[3]}".find{it.@name.text().contains("${versionName}")}.@value.text(),expectedValue)
        else if (path.size() == 5)
            Assert.assertEquals(node."${path[0]}"."${path[1]}"."${path[2]}"."${path[3]}"."${path[4]}".find{it.@name.text().contains("${versionName}")}.@value.text(),expectedValue)
        else
            Assert.fail("Method does not support path with more than 5 nested levels")

    }
}
