package com.edifecs.qa.utils

import groovy.text.GStringTemplateEngine
import org.apache.commons.configuration.PropertiesConfiguration
import org.testng.Assert

/**
 * Created with IntelliJ IDEA.
 * User: InaG
 * Date: 7/1/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CopyFiles extends UtilsChecks{
    static def trackingIdentifier
    static def randomProviderClaimNumber
    static def randomNumber1
    static def randomNumber
    static def inboundClaimFormat
    static config = new PropertiesConfiguration("environment.properties")
    static final def testDataPath = config.getString("environment.testDataPath").replaceAll("/", "\\\\")
    static final def inboundInternalClaimUpdates = config.getString("environment.inboundInternalClaimUpdates").replaceAll("/", "\\\\")
    static final def receiveInboundAcks = config.getString("environment.receiveInboundAcks").replaceAll("/", "\\\\")

    public void copyXMLs(String fileType, String pathToFile, String pathToProperties){
        def TC1a = new File(testDataPath + pathToFile)
        def TC1b = new File(testDataPath + pathToProperties)
        def text = TC1a.getText()
        def text2 = TC1b.getText()
        def bindingFile
        def bindingProperties

        Assert.assertTrue(TC1a.exists(),testDataPath + pathToFile + " doesn't exist!")
        Assert.assertTrue(TC1b.exists(),testDataPath+pathToProperties+" doesn't exist!")

        if (fileType.equals("New")){
            randomProviderClaimNumber = new Random().nextLong().abs()
            randomNumber1 = new Random().nextInt(999999999)
            randomNumber = createControlNumber(randomNumber1)
            println "TrackingID  = " + randomProviderClaimNumber
            bindingFile = ["UniqueID": randomNumber, "ProviderClaimNumber": randomProviderClaimNumber, "TargetTrackingID": randomProviderClaimNumber]
        } else if ((fileType.equals("Update"))||(fileType.equals("Trigger"))||(fileType.equals("AdjustedClaim"))||(fileType.equals("AdjustedPayment"))){
            bindingFile = ["TargetTrackingID": randomProviderClaimNumber]
        }
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(bindingFile)

        bindingProperties = ["TargetTrackingID": randomProviderClaimNumber]
        def template2 = engine.createTemplate(text2).make(bindingProperties)

        createNewFile(inboundInternalClaimUpdates + "\\"+pathToProperties.substring(pathToProperties.lastIndexOf('/')+1), template2)
        createNewFile(inboundInternalClaimUpdates + "\\"+pathToFile.substring(pathToFile.lastIndexOf('/')+1), template)

    }

    public void copyAcks(String fileType, String pathToFile){
        inboundClaimFormat = null
        def TC1a = new File(testDataPath + pathToFile)
        Assert.assertTrue(TC1a.exists(),testDataPath + pathToFile +" doesn't exist!")
        def text = TC1a.getText()
        def controlNumber
        def binding
        def originatorTranIdentifier
        def providerClaimNumber
        switch(fileType){
            case "TA1":
                controlNumber = createControlNumber(getControlNumber(encTransmissionSID,"InterchangeControlNumber").toInteger())
                binding = ["IntContNum": controlNumber]
                break
            case "999":
                controlNumber = getControlNumber(encTransmissionSID,"GroupControlNumber").toInteger()
                binding = ["GroupContNum": controlNumber]
                break
            case "277":
                originatorTranIdentifier =  getIdentifierByEncounter("OriginatorTranIdentifier", encTransmissionSID)
                providerClaimNumber =  getIdentifierByEncounter("ProviderClaimNumber", encTransmissionSID)
                binding = ["OriginatorTranIdentifier": originatorTranIdentifier, "ProviderClaimNumber": providerClaimNumber]
        }
        def engine = new GStringTemplateEngine()
        def template = engine.createTemplate(text).make(binding)
        lastImportTimeStamp = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS.%<tL', new Date())
        sleep(2000)
        createNewFile(receiveInboundAcks+"\\"+pathToFile.substring(pathToFile.lastIndexOf('/')+1),template)
    }


}
