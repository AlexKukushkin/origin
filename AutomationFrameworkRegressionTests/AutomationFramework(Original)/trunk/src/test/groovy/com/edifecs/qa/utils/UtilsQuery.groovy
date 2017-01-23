package com.edifecs.qa.utils

import com.edifecs.bizitem.exception.ValidationException
import com.edifecs.ops.OperationsProvider
import com.edifecs.ops.OperationsRepository
import com.edifecs.ops.data.ContentContext
import groovy.sql.GroovyRowResult
import org.testng.Assert

import java.sql.SQLException

/**
 * Created by IntelliJ IDEA.
 * User: anastasiaz
 * Date: 10/21/11
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */

public class UtilsQuery extends Utils {

// there is no need to wait for CorrelationId
    public String getCorrelationID(String instType, String idName, String transmissionSID) {
        def correlationID = "select ExternalCorrelationIdValue from ExternalCorrelationAssociation where InstanceType=" +
                instType + " and TransmissionSID=" + transmissionSID + " and ExternalCorrelationIdSID in " +
                "(select ExternalCorrelationIdSID from ExternalCorrelationId where Name='" + idName + "')"
        def id; activeSql.eachRow(correlationID) { id = "$it.ExternalCorrelationIdValue" }
        return id
        //return getCorrelationIDWaiting(instType, idName, transmissionSID, 0)
    }

    public String getCorrelationIDWaiting(String instType, String idName, String transmissionSID, int sleepTime = 180000) {
        String query = "select ExternalCorrelationIdValue from ExternalCorrelationAssociation where InstanceType=" +
                instType + " and TransmissionSID=" + transmissionSID + " and ExternalCorrelationIdSID in " +
                "(select ExternalCorrelationIdSID from ExternalCorrelationId where Name='" + idName + "')"
        String correlationId = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            correlationId = "${it.ExternalCorrelationIdValue}"
        }
        return correlationId
    }

    public String checkCorrelationIDNull(String instType, String idName, String transmissionSID) {
        def internalID = getCorrelationID(instType, idName, transmissionSID)
        String message = "CorrelationID $idName should be null for instanceType " + getInstanceTypeName(instType) +
                " for TransmissionSID = $transmissionSID"
        Assert.assertNull(internalID, message)
        return internalID
    }

    public String checkCorrelationIDNotNull(String instType, String idName, String transmissionSID) {
        def internalID = getCorrelationIDWaiting(instType, idName, transmissionSID)
        String message = "CorrelationID $idName should not be null for instanceType " + getInstanceTypeName(instType) +
                " for TransmissionSID = $transmissionSID"
        Assert.assertNotNull(internalID, message)
        return internalID
    }

    public String getGroupControlNumber(String transmissionSid) {
        def grContNumSelect = "Select GroupControlNumber from FunctionalGroup where TransmissionSID = " + transmissionSid
        String grContNum = null
        activeSql.eachRow(grContNumSelect) {
            grContNum = it.GroupControlNumber
        }
        return grContNum
    }

    @Deprecated
    public String getGroup(String groupDefName, String groupRoleName, String ti, String instType, String transmissionSID) {
        return getGroupWaiting(groupDefName, groupRoleName, ti, instType, transmissionSID, 0);
    }

    public String getGroup_REVISED(String groupDefName, String groupRoleName, String instType, String transmissionSID, String trackingInfo = "") {
        return getGroupWaiting_REVISED(groupDefName, groupRoleName, instType, transmissionSID, trackingInfo, 0);
    }

    @Deprecated
    public String getGroupWaiting(String groupDefName, String groupRoleName, String trackingInfo, String instType, String transmissionSID, int sleepTime = 180000) {
        def query = "select * from GroupIdentifier where GroupIdentifierDefSID in " +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='" + groupDefName + "')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='" +
                groupRoleName + "') and GroupIdentifier='" + trackingInfo + "' and InstanceTypeID=" + instType + " and TransmissionSID=" + transmissionSID
        String gr = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            gr = it.GroupIdentifierSID
        }
        return gr
    }

    public String getGroupWaiting_REVISED(String groupDefName, String groupRoleName, String instanceType,
                                          String transmissionSID, String trackingInfo = "", int sleepTime = 180000) {
        String trackingInfoCriteria = trackingInfo.equals("") ? "" : " and GroupIdentifier = '$trackingInfo' "
        def query = "select * " +
                "from GroupIdentifier " +
                "where GroupIdentifierDefSID in " +
                "   (select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='$groupDefName') " +
                "and GroupIdentifierRoleSID in " +
                "   (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='$groupRoleName') " +
                "and InstanceTypeID=$instanceType " +
                "and TransmissionSID=$transmissionSID " +
                "$trackingInfoCriteria"
        String gr = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            gr = it.GroupIdentifierSID
        }
        return gr
    }

    @Deprecated
    public String getGroupSimple(String groupDefName, String groupRoleName, String instType, String transmissionSID) {
        return getGroupSimpleWaiting(groupDefName, groupRoleName, instType, transmissionSID, 0)
    }

    @Deprecated
    public String getGroupSimpleWaiting(String groupDefName, String groupRoleName, String instType, String transmissionSID, int sleepTime = 180000) {
        String query = "select * from GroupIdentifier where GroupIdentifierDefSID in " +
                "(select GroupIdentifierDefSID From GroupIdentifierDef where GroupIdentifierDefName='" + groupDefName + "')" +
                "and GroupIdentifierRoleSID in (select GroupIdentifierRoleSID from GroupIdentifierRole where GroupIdentifierRoleName='" +
                groupRoleName + "') and InstanceTypeID=" + instType + " and TransmissionSID=" + transmissionSID
        String gr = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            gr = it.GroupIdentifierSID
        }
        return gr
    }

    public String checkGroupNull(String groupDefName, String groupRoleName, String instanceType,
                                 String transmissionSID, String trackingIdentifier = "") {
        def groupIdentifier = getGroup_REVISED(groupDefName, groupRoleName, instanceType, transmissionSID, trackingIdentifier)
        String message = "There should't be group for [" + getInstanceTypeName(instanceType) + "] " +
                "with GroupIdentifier = $trackingIdentifier for transmission SID = $transmissionSID"
        Assert.assertNull(groupIdentifier, message)
        return groupIdentifier
    }

    public String checkGroupNotNull(String groupDefName, String groupRoleName, String instanceType,
                                    String transmissionSID, String trackingIdentifier = "") {
        def groupIdentifier = getGroupWaiting_REVISED(groupDefName, groupRoleName, instanceType, transmissionSID, trackingIdentifier)
        String message = "There should be group for [" + getInstanceTypeName(instanceType) + "] " +
                "with GroupIdentifier = $trackingIdentifier for transmission SID = $transmissionSID"
        Assert.assertNotNull(groupIdentifier, message)

        Assert.assertEquals(a, b);
        return groupIdentifier
    }

    @Deprecated
    public
    static String checkGroupSimpleNull(String groupDefName, String groupRoleName, String instType, String transmissionSID) {
        def groupIdentifier = getGroupSimple(groupDefName, groupRoleName, instType, transmissionSID)
        String message = "There should't be group for [" + getInstanceTypeName(instType) + "] " +
                "for transmission SID = $transmissionSID"
        Assert.assertNull(groupIdentifier, message)
        return groupIdentifier
    }

    @Deprecated
    public
    static String checkGroupSimpleNotNull(String groupDefName, String groupRoleName, String instType, String transmissionSID) {
        def groupIdentifier = getGroupSimpleWaiting(groupDefName, groupRoleName, instType, transmissionSID)
        String message = "There should't be group for [" + getInstanceTypeName(instType) + "] " +
                "for transmission SID = $transmissionSID"
        Assert.assertNotNull(groupIdentifier, message)
        return groupIdentifier
    }

    @Override
    boolean equals(Object obj) {
        return super.equals(obj)
    }

    public String getCustomField(String cfName, String transmissionSID, String instType) {
        return getCustomFieldWaiting(cfName, transmissionSID, instType, "CFStringValue", 0)
    }

    public String getCustomFieldDT(String cfName, String transmissionSID, String instType) {
        return getCustomFieldDTWaiting(cfName, transmissionSID, instType, "CFDateTimeValue", 0)
    }

    public String getCustomFieldWaiting(String cfName, String transmissionSID, String instType, String cfTable = "CFStringValue", int waitTime = 180000) {
        String query = "select FieldValue from EcActive.dbo.$cfTable where CustomFieldSID in " +
                "(select CustomFieldSID from EcActive.dbo.CustomField where Name='" + cfName + "') and " +
                "TransmissionSID='" + transmissionSID + "' and InstanceTypeID=" + instType
        String cfValue = null
        getIdsListWaitingQuery(query, activeSql, waitTime).each {
            cfValue = it.FieldValue
        }
        return cfValue
    }

    //alex kukushkin

    //
    public String getCustomFieldDTWaiting(String cfName, String transmissionSID, String instType, String cfTable = "CFDateTimeValue", int waitTime = 180000) {
        String query = "select FieldValue from EcActive.dbo.$cfTable where CustomFieldSID in " +
                "(select CustomFieldSID from EcActive.dbo.CustomField where Name='" + cfName + "') and " +
                "TransmissionSID='" + transmissionSID + "' and InstanceTypeID=" + instType
        String cfValue = null
        getIdsListWaitingQuery(query, activeSql, waitTime).each {
            cfValue = it.FieldValue
        }
        return cfValue
    }

    public void checkCustomFieldNull(String cfName, String transmissionSID, String instType, String cfTable = "CFStringValue") {
        def cf = getCustomField(cfName, transmissionSID, instType)
        String message = "Custom field $cfName should be null for instanceType $instType for " +
                "TransmissionSID = $transmissionSID"
        Assert.assertNull(cf, message)
    }

    public void checkCustomFieldNotNull(String cfName, String transmissionSID, String instType, String cfTable = "CFStringValue") {
        def cf = getCustomFieldWaiting(cfName, transmissionSID, instType, cfTable)
        String message = "Custom field $cfName is null for instanceType $instType for " +
                "TransmissionSID = $transmissionSID"
        Assert.assertNotNull(cf, message)
    }

    public void checkCustomFieldEquals(String expectedValue, String cfName, String transmissionSID, String instType, String cfTable = "CFStringValue") {
        def cf = getCustomFieldWaiting(cfName, transmissionSID, instType, cfTable)
        String message = "Custom field $cfName is [$cf] but is expected to be [$expectedValue] for" +
                "instanceType $instType for TransmissionSID = $transmissionSID"
        Assert.assertEquals(cf, expectedValue, message)
    }

    public String getCIDFields(String cidName, String transmissionSID, String instType) {
        //def cf= "select * from dbo.CFStringValue where CustomFieldSID in (select CustomFieldSID from CustomField where Name='"+cfName+"') and TransmissionSID='"+currentTransmissionSID+"' and InstanceTypeID="+instType
        def cid = "select * from dbo.ExternalCorrelationId where ExternalCorrelationAssociationIdSID in (select Name from ExternalCorrelationId where Name='" + cidName + "')and TransmissionSID='" + transmissionSID + "' and InstanceTypeID=" + instType
        def cidValue; activeSql.eachRow(cid) { cidValue = "$it.FieldValue" }
        return cidValue;
    }

    public List<GroovyRowResult> getClaimID(String transmissionSID) throws SQLException {
        String query = "select ClaimSID from Claim where TransmissionSID=" + transmissionSID
        def claimID = []
        getIdsListWaitingQuery(query).each {
//            claimID << it.ClaimSID
            claimID << it
        }
        return claimID
    }

    public List<GroovyRowResult> getInterchangeSID(String transmissionSID) throws SQLException {
        def interchangeID = []
        activeSql.eachRow("select InterchangeSID from TransactionHeader where TransmissionSID=" + transmissionSID) {
            interchangeID << it.toRowResult()
        }
        return interchangeID
    }

    public List<GroovyRowResult> getTransactionID(String transmissionSID) throws SQLException {
        def interchangeID = []
        activeSql.eachRow("select TransactionHeaderSID from TransactionHeader where TransmissionSID=" + transmissionSID) {
            interchangeID << it.toRowResult()
        }
        return interchangeID
    }

    public List<GroovyRowResult> getFunctionalGroupID(String transmissionSID) throws SQLException {
        def groupID = []
        activeSql.eachRow("select FunctionalGroupSID from FunctionalGroup where TransmissionSID=" + transmissionSID) {
            groupID << it.toRowResult()
        }
        return groupID
    }

    public String getClaimIDString(String transmissionSID) throws SQLException {
        def claimSIDs = "select ClaimSID from Claim where TransmissionSID=" + transmissionSID
        def claimID; activeSql.eachRow(claimSIDs) { claimID = "$it.ClaimSID" }
        return claimID
    }

    public String getInterchangeSIDString(String transmissionSID) throws SQLException {
        def interchangeSIDs = "select InterchangeSID from Interchange where TransmissionSID=" + transmissionSID
        def interchangeID; activeSql.eachRow(interchangeSIDs) { interchangeID = "$it.InterchangeSID" }
        return interchangeID
    }

    public String getTransactionIDString(String transmissionSID) throws SQLException {
        def transactionSIDs = "select TransactionHeaderSID from TransactionHeader where TransmissionSID=" + transmissionSID
        def transactionID; activeSql.eachRow(transactionSIDs) { transactionID = "$it.TransactionHeaderSID" }
        return transactionID
    }

    public String getFunctionalGroupIDString(String transmissionSID) throws SQLException {
        def funcGrSIDs = "select FunctionalGroupSID from FunctionalGroup where TransmissionSID=" + transmissionSID
        def funcGrID; activeSql.eachRow(funcGrSIDs) { funcGrID = "$it.FunctionalGroupSID" }
        return funcGrID
    }

    public String getEvent(String eventMessage, String claimSID, String instType) {
        def event = "select * from EdifecsBaseEvent where Message='" + eventMessage + "' and PrimaryInstanceSID='" + claimSID + "' and PrimaryInstanceTypeID=" + instType
        def ev; activeSql.eachRow(event) { ev = "$it.EdifecsBaseEventSID" }
        return ev
    }


    public String getAttachment(String claimSID, String instType) {
        def attach = "select * from ECuAttachment where ParentID='" + claimSID + "' and ParentTypeID=" + instType
        def att; enablementSql.eachRow(attach) { att = "$it.PhysicalName" }
        return att
    }

    public String getAttachmentWaiting(String claimSID, String instType) {

        def query = "select PhysicalName from ECuAttachment where ParentID='" + claimSID + "' and ParentTypeID=" + instType
        return getStringWaitingQuery(query, enablementSql)
    }

    //there is no need to wait for currentActivityState
    public String getCurrentActivityState(String tableName, String identifiedSID, String valueSID) {
        def value = "select top 1 CurrentActivityStateID from " + tableName + " where " + identifiedSID + " = " + valueSID
        def val; activeSql.eachRow(value) { val = "$it.CurrentActivityStateID" }
        return val
//        String query = "select CurrentActivityStateID from " + tableName + " where " + identifiedSID + " = "+ valueSID
//        return getIdsListWaitingQuery(query)
    }

    //todo: implement functionality for checkCurrentActivityState error message to show activityState description, not code
    //todo: make method's definition more intuitive
    public String checkCurrentActivityStateWaiting(String tableName, String identifiedSID, String valueSID,
                                                   String expectedValue, int sleepTime = 60000) {
        String currentActivityState = ""
        def int loading = 0;
        int waiting = 1000
        while (loading < sleepTime) {
            currentActivityState = getCurrentActivityState(tableName, identifiedSID, valueSID)
            if (expectedValue.equals(currentActivityState))
                break

            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }

        Assert.assertEquals(currentActivityState, expectedValue, "for $tableName with $identifiedSID=$valueSID,\n" +
                "CurrentActivityState is not $expectedValue after waiting $sleepTime ms");
        return currentActivityState
    }

    public void checkCurrentState(String transmissionSid) {
        def getCurrentState = "select * from dbo.FunctionalGroup where TransmissionSID='" + transmissionSid + "'"
        def current; activeSql.eachRow(getCurrentState) { current = "$it.CurrentStatusID" }
        Assert.assertEquals(current, "5000", "There is no 999 group for Encounter for GroupIdentifier = " + current)
    }

    public String getEvent1(String eventMessage, String claimSID, String instType, int sleepTime = 180000) {
        String query = "select EdifecsBaseEventSID from EdifecsBaseEvent where Message LIKE  '" + eventMessage + "%' and PrimaryInstanceSID='" + claimSID + "' and PrimaryInstanceTypeID=" + instType
        String eventSid = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            eventSid = it.EdifecsBaseEventSID
        }
        return eventSid
    }

    public void checkEventNotNull(String eventMessage, String instanceType, String transmissionSid) {
        String instanceTypeName = getInstanceTypeName(instanceType);
        List<GroovyRowResult> listSids = null
//        if(instanceTypeName.equals("Claim"))
//            listSids = getClaimID(transmissionSid)
//        if(instanceTypeName.equals("Functional Group"))
//            listSids = getFunctionalGroupID(transmissionSid)
        switch (instanceTypeName) {
            case "Claim":
                listSids = getClaimID(transmissionSid)
                break;
            case "Functional Group":
                listSids = getFunctionalGroupID(transmissionSid)
                break;
            case "Transaction":
                listSids = getTransactionID(transmissionSid)
                break;
        }
        if (listSids.size() == 0) {
            println "instance type not processed or there are no sids : $instanceTypeName"
        }
        listSids.each {
            item ->
                def event = getEvent1(eventMessage, item.values().toString().replace("[", "").replace("]", ""), instanceType)
                print "${item.values()}"
                Assert.assertNotNull(event, "Update  Event is missing for TransmissionSID =" + transmissionSid)
        }
    }

    public String getException(def clmSID, def assType) {
        /*def exception = "select * from EcuIssue where IssueID = (select IssueID from EcuIssueAssociation where ((AssociationItemID=" + clmSID + ") AND (AssociationItemTypeID = " + assType + "))) "
        def ex;
        def rows = enablementSql.rows(exception)
        if (rows.size() == 1) {
            ex = rows[0].IssueID
//      rows.each {
//        ex = it.IssueID
//      }
        } else if (rows.size() > 1) {
            Assert.fail("There can't  be more than one IssueIds")
        }
        ex*/

        def query = "select IssueId from EcuIssue where IssueID = (select IssueID from EcuIssueAssociation where ((AssociationItemID=" + clmSID + ") AND (AssociationItemTypeID = " + assType + "))) "
        def exceptions = []
        getIdsListWaitingQuery(query, enablementSql).each {
            exceptions << it.IssueId
        }
        Assert.assertEquals(exceptions.size(), 1, "there are no exceptions or there are more than one exceptions!")
        return exceptions[0].toString()
    }

    public String getErrors(String errorMessage, String claimSID, String instType, int sleepTime = 180000) {
        String query = "select ErrorMessageSID from ErrorMessage where BizErrorMessage LIKE  '" + errorMessage + "%' and TargetInstanceSID='" + claimSID + "' and TargetInstanceType=" + instType
        String errorSid = null
        getIdsListWaitingQuery(query, activeSql, sleepTime).each {
            errorSid = it.ErrorMessageSID
        }
        return errorSid
    }

//    public String getTransmissionSID(String currentTimeSQLFormat, int sleepTime = 180000) {
    public String getTransmissionSID(String currentTimeSQLFormat, int sleepTime = 180000) {
        sleep 500
        def query = "select top 1 TransmissionSID, PublicProcessingStatus  " +
                "from Transmission where TransmissionReceiptDateTime >='" +
                currentTimeSQLFormat + "' order by TransmissionReceiptDateTime desc";
        def publicProcessingStatus = "";
        def newTransmissionSID = null

        def int loading = 0;
        int waiting = 1000
        while (loading < sleepTime) {
            def row = activeSql.firstRow(query)
            if (row != null) {
                newTransmissionSID = "${row.TransmissionSID}"
                publicProcessingStatus = "${row.PublicProcessingStatus}"
                if (newTransmissionSID != null && publicProcessingStatus == "9000") break;
            }
            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }

        Assert.assertNotNull(newTransmissionSID, "File was not loaded in TM DB after 3 minutes, Public Processing status is = " + publicProcessingStatus);
        return newTransmissionSID
    }

    public String getMao002Id(String transmissionSid, int waitTime = 180000) {
        def query = "select ExternalCorrelationIdValue from ExternalCorrelationAssociation " +
                "where ExternalCorrelationIdSID = 100005 and TransmissionSID = " + transmissionSid;
        int loading = 0;
        int waiting = 1000
        while (loading < waitTime) {
            def id = activeSql.firstRow(query)
            if (id.ExternalCorrelationIdValue != null)
                return "${id.ExternalCorrelationIdValue}";
            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }
        return null
    }

    public int generateNumber(int min, int max) {
        return Math.round((Math.random() * (max - min)) + min);
    }

    //todo: improve this methods to return array of sids/real String value
    public String getStringWaitingQuery(String query, def sql = activeSql, int waitTime = 180000) {
        int loading = 0;
        def row = ""
        int waiting = 1000
        while (loading < waitTime) {
            row = sql.firstRow(query)
            if (row != null && !row.isEmpty())
                break;
            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }
        return row
    }

    public List<GroovyRowResult> getIdsListWaitingQuery(String query, def sql = activeSql, int waitTime = 180000) {
        int loading = 0;
        def rows = []
        int waiting = 1000
        while (loading <= waitTime) {
            rows = []
            sql.eachRow(query) {
                rows << it.toRowResult()
            }
            if (rows.size() >= 1)
                break;
            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }
        return rows
    }

    public void waitForOpsRepoData(String itemType, String trackingIdentifier, String dataType) throws Exception {
        int waitTime = 60000
        int loading = 0;
        def ids = []
        int waiting = 1000
        while (loading < waitTime) {
            try {
                repository = OperationsProvider.getRepository();
                repository.getData("Claim", trackingIdentifier, "EC-CF")
                return
            } catch (Exception e) {
                if (!(e instanceof ValidationException &&
                        e.getMessage().contains("Property Claimsid for entity Claim was not found")))
                    throw e;
            }
            loading += waiting;
            sleep waiting;
            waiting = waiting > 5000 ? waiting : (int) (1.25 * waiting);
        }
    }

    public String checkClaimOperRepoData(String trackingIdentifier) {
        waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")
        def claimDataECCF = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimDataECCF, "Cannot find Ops Repository Claim Data EC-CF attachment for trackingID = " + trackingIdentifier);

        def claimItem = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claimItem, "Cannot find Ops Repository Claim for trackingID = " + trackingIdentifier);

        def claimItem2 = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE"))
        Assert.assertNotNull(claimItem2, "Cannot find Ops Repository Claim for trackingID = " + trackingIdentifier);
        return ""
    }

    public String checkEncounterOperRepoData(String trackingIdentifier) {
        checkClaimOperRepoData(trackingIdentifier)
        /*waitForOpsRepoData("Claim", trackingIdentifier, "EC-CF")

        //get Claim item for EC-CF
        def claim = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("EC-CF"))
        Assert.assertNotNull(claim, "Cannot find Ops Repository claim attachment for trackingID = " + trackingIdentifier);

        //get data of Claim item for EC-CF
        def claimData = repository.getData("Claim", trackingIdentifier, "EC-CF")
        Assert.assertNotNull(claimData, "Cannot find Ops Repository Claim Data attachment for trackingID = " + trackingIdentifier);*/

        //check if the two data items for current claim (first - initial, second - updated)
        def claimAtt = repository.getItem("Claim", trackingIdentifier, new ContentContext().forAttachment("NATIVE"))
        def attList = claimAtt.getAttachments()
        def initialClaimAttEDI = attList.get(0).getData()
        def updatedClaimAttEDI = attList.get(1).getData()
        Assert.assertNotEquals(updatedClaimAttEDI, initialClaimAttEDI, "OPS Claim attachment should not be equal")

        return ""
    }

    public String getInstanceTypeName(String instanceType) {
        String query = "select Description from enInstanceType where EnumID = $instanceType"
        def description = null; activeSql.eachRow(query) { description = "$it.Description" }
        return description
    }

    public String checkExtraGroupIds(String currentTransmissionSID) {
        def extraGroup = "select * from GroupIdentifier where TransmissionSID='" + currentTransmissionSID + "' and InstanceTypeID<>302"
        def exGroup; activeSql.eachRow(extraGroup) {
            exGroup = "$it.GroupIdentifierSID"
            Assert.assertNull(exGroup, "There is extra group for GroupIdentifier = " + extraGroup)
        }
    }

    public String checkClaimException(String transmissionSid) {
        def claimID = getClaimID(transmissionSid)
        Assert.assertNotEquals(claimID.size(), 0, "no claims were received for transmission SID = " + transmissionSid)
        print claimID
        def errors = []
        claimID.each {
            def exception = getException(it.ClaimSID, 302)
            print exception
            if (!exception) {
                errors << "Exception is missing for TransmissionSID =" + transmissionSid << "/n"
            }
        }

        if (errors) {
            errors.each {
                println it
            }
            Assert.fail("There were errors")
        }
        return ""
    }
/*    public void copyDataToFile() throws Exception {
        def myFile = new File('./src/test/resources/com/edifecs/qa/AssertionFile/OutboundEncounter.dat').getText('UTF-8')
        def newFile = new File(writeToFile, "NewFile.dat")
        PrintWriter printWriter = new PrintWriter(newFile)

        myFile.eachLine { currentLine, lineNumber ->
            if (lineNumber > 1)
                printWriter.println(currentLine)
        }
        printWriter.close()
    }*/
}