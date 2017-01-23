package com.edifecs.qa.utils

import com.edifecs.ops.OperationsProvider
import com.edifecs.ops.OperationsRepository
import groovy.sql.Sql
import groovy.text.GStringTemplateEngine
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: anastasiaz
 * Date: 10/11/11
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    def testDataPath = "./src/test/resources/com/edifecs/qa/"
    def writeToFile = "./src/test/resources/com/edifecs/qa/Validation/"
    public static def ECRootPath = null
    public static def inboundFileConnector
    public static def inboundInternalClaimUpdates
    public static def inboundMAO
    public static def CLMWorkingPath = "/EUO/CLM/RIM/working"
//    def CLMWorkingPath ="//enclm02-vm.enqa.edifecs.local/Edifecs/EUO/CLM/RIM/working"
    public static def paymentAsClaimUpdate
    public static def receiveInboundAcks
    public static def activeDBURL
    public static def activeDBUser
    public static def activeDBPswd
    public static def activeSql
    public static def currentTransmissionSID
    public def encTransmissionSID
    public static def lastImportTimeStamp
    public static def ackTransmissionSID
    public static def encAdjTransmissionSID
    public static def claimXML
    public static def encounterXML
//    def opsRepositoryDBURL
//    def opsRepositoryDBbUser
//    def opsRepositoryDBpswd
//    def opsRepositorySQL
    public static def enablementSql
    public static def enablementDBURL
    public static def enablementDBUser
    public static def enablementDBPswd
    public static def OperationsRepository repository

    public static def DataPath


//    public static Date myTestDate = null;
//    public Date myTestDate = null;

    @BeforeClass
    public static void initAll() {

        DataPath = "./src/test/resources/com/edifecs/qa/CopyClaim";

        if (ECRootPath == null) {
            ECRootPath = System.getenv("ECRootPath");
            Assert.assertNotNull(ECRootPath, "ECRootPath is missing");
            println "ECRootPath = $ECRootPath"
        }

        if (activeDBURL == null || enablementDBURL == null) {
            println "INITIALISING DATABASE PROPERTIES"
            def registryPath = ECRootPath + "/TM/ServiceManager/classes/edifecs-registry.xml";
//        def registryPath = "//enclm01-vm.enqa.edifecs.local/classes/edifecs-registry.xml";

            Assert.assertTrue(new File(registryPath).exists(), " Edifecs-registry.xlm file is missing. TM is not installed properly");
            def registry = new XmlParser().parse(registryPath);

            activeDBURL = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'url' }[0].value.text()
            activeDBUser = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'username' }[0].value.text()
            activeDBPswd = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'password' }[0].value.text()
            activeSql = Sql.newInstance(activeDBURL, activeDBUser, "manager", "net.sourceforge.jtds.jdbc.Driver")
            enablementDBURL = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'url' }[0].value.text()
            enablementDBUser = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'username' }[0].value.text()
            enablementDBPswd = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'password' }[0].value.text()
            enablementSql = Sql.newInstance(enablementDBURL, enablementDBUser, "manager", "net.sourceforge.jtds.jdbc.Driver")

            println "DATABASE PROPERTIES INITIALISED"
        }

        if (repository ) {
            println System.getProperty("user.dir");
            repository = OperationsProvider.getRepository();
            Assert.assertTrue(repository != null, "Failed to find OperationsRepository")
        }

        if (inboundFileConnector == null) {
            inboundFileConnector = ECRootPath + CLMWorkingPath + "/Inbound/Claim";
//        inboundFileConnector = CLMWorkingPath+"/Inbound/Claim";
            Assert.assertTrue(new File(inboundFileConnector).exists(), " Inbound File Connector doesn't exist");

            inboundInternalClaimUpdates = ECRootPath + CLMWorkingPath + "/Inbound/InternalClaimUpdates";
//        inboundInternalClaimUpdates = CLMWorkingPath+"/Inbound/InternalClaimUpdates";
            inboundMAO = ECRootPath + CLMWorkingPath + "/Inbound/MAOAck";
            Assert.assertTrue(new File(inboundFileConnector).exists(), " Internal Claim Updates File Connector doesn't exist");

            paymentAsClaimUpdate = ECRootPath + CLMWorkingPath + "/Inbound/835AsClaimUpdate";
//        paymentAsClaimUpdate = CLMWorkingPath+"/Inbound/835AsClaimUpdate";
            Assert.assertTrue(new File(inboundFileConnector).exists(), " Create Outbound File Connector doesn't exist");

            receiveInboundAcks = ECRootPath + CLMWorkingPath + "/Inbound/ReceiveInboundAcks";
//        receiveInboundAcks = CLMWorkingPath+"/Inbound/ReceiveInboundAcks";
            Assert.assertTrue(new File(inboundFileConnector).exists(), " Process Claim Acknowledgement File Connector doesn't exist");
        }
    }
    /*  Check System Variables for TM,  XES and CLM*/

//    @Test(groups = "EnvIntegration")
//    void checkSysVariables() {
//        ECRootPath = System.getenv("ECRootPath");
//        Assert.assertNotNull(ECRootPath, "ECRootPath is missing");
//    }

    /* Verify if Edifecs registry exists. get DB connection*/

//    @Test(dependsOnMethods = "checkSysVariables", groups = "EnvIntegration")
//    @Test(groups = "EnvIntegration")
    void verifyEdifecsRegistry() {
        def registryPath = ECRootPath + "/TM/ServiceManager/classes/edifecs-registry.xml";
//        def registryPath = "//enclm01-vm.enqa.edifecs.local/classes/edifecs-registry.xml";

        Assert.assertTrue(new File(registryPath).exists(), " Edifecs-registry.xlm file is missing. TM is not installed properly");
        def registry = new XmlParser().parse(registryPath);

        activeDBURL = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'url' }[0].value.text()
        activeDBUser = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'username' }[0].value.text()
        activeDBPswd = registry.'**'.component.findAll { it.@name == 'active' }[0].property.grep { it.@name == 'password' }[0].value.text()
        activeSql = Sql.newInstance(activeDBURL, activeDBUser, "manager", "net.sourceforge.jtds.jdbc.Driver")
        enablementDBURL = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'url' }[0].value.text()
        enablementDBUser = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'username' }[0].value.text()
        enablementDBPswd = registry.'**'.component.findAll { it.@name == 'enablement' }[0].property.grep { it.@name == 'password' }[0].value.text()
        enablementSql = Sql.newInstance(enablementDBURL, enablementDBUser, "manager", "net.sourceforge.jtds.jdbc.Driver")

    }

    /* Verify if com.edifecs.opsrepository.xml exists. get Ops DB connection*/

//    @Test(dependsOnMethods = "verifyEdifecsRegistry", groups = "EnvIntegration")
//    @Test(groups = "EnvIntegration")
    void verifyOpsRepository() {
        println System.getProperty("user.dir");
        repository = OperationsProvider.getRepository();
        Assert.assertTrue(repository != null, "Failed to find OperationsRepository")
    }

    /* Verify CLM inbound default XES file connectors*/
    //todo why here ??? inbound file connector is initialized
//    @Test(dependsOnMethods = "verifyOpsRepository", groups = "EnvIntegration")
    @Test(groups = "EnvIntegration")
    void checkIfFileConnectorsExist() {
        inboundFileConnector = ECRootPath + CLMWorkingPath + "/Inbound/Claim";
//        inboundFileConnector = CLMWorkingPath+"/Inbound/Claim";
        Assert.assertTrue(new File(inboundFileConnector).exists(), " Inbound File Connector doesn't exist");

        inboundInternalClaimUpdates = ECRootPath + CLMWorkingPath + "/Inbound/InternalClaimUpdates";
//        inboundInternalClaimUpdates = CLMWorkingPath+"/Inbound/InternalClaimUpdates";
        inboundMAO = ECRootPath + CLMWorkingPath + "/Inbound/MAOAck";
        Assert.assertTrue(new File(inboundFileConnector).exists(), " Internal Claim Updates File Connector doesn't exist");

        paymentAsClaimUpdate = ECRootPath + CLMWorkingPath + "/Inbound/835AsClaimUpdate";
//        paymentAsClaimUpdate = CLMWorkingPath+"/Inbound/835AsClaimUpdate";
        Assert.assertTrue(new File(inboundFileConnector).exists(), " Create Outbound File Connector doesn't exist");

        receiveInboundAcks = ECRootPath + CLMWorkingPath + "/Inbound/ReceiveInboundAcks";
//        receiveInboundAcks = CLMWorkingPath+"/Inbound/ReceiveInboundAcks";
        Assert.assertTrue(new File(inboundFileConnector).exists(), " Process Claim Acknowledgement File Connector doesn't exist");
    }

    public static void createNewFile(path, content) {
        File file = new File(path) << content
        Assert.assertTrue(file.exists(), "File " + path + " was not created");
    }

    public static void copySourceFile(path) {
        File file = new File(path).newDataInputStream()
        Assert.assertTrue(file.exists(), "File " + path + " was not created");
    }

    public static void sendDestFolder(path) {
        File file = new File(path).newDataOutputStream()
        Assert.assertTrue(file.exists(), "File " + path + " was not created");
    }

    /**
     * Creates a control number string from a number
     * Completes the number with zeroes
     * @param numberToCompleteWithZeroes the number
     * @return a string with numbers
     */
    public static String createControlNumber(int numberToCompleteWithZeroes) {
        String str = ""
        str += numberToCompleteWithZeroes

        def strLength = str.length()
        if (strLength == 9) {
            return str
        } else if (strLength > 9) {
            throw new RuntimeException("Why control number is more than a billion")
        }
        int times = 9 - strLength

        String zeroesStr = "0" * times

        println "Generated control number : " + zeroesStr + str
        return zeroesStr + str
    }

    public String createCurrentTimeSQLFormat() {
        def today = new Date()
        String currentTimeSQLFormat = String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS', today)
        println "File Submitted, CurrentTime = " + currentTimeSQLFormat;
        return currentTimeSQLFormat
    }

    public void copyFileAndPropertiesToConnector(String fileName, String fileFolder, String connector, def binding, boolean withProperties = true) {
//        String randomNumber = createControlNumber( new Random().nextInt(999999999))
//        println randomNumber
//
//        def binding = ["UniqueID": randomNumber]
        def engine = new GStringTemplateEngine()

        def TC1a = new File("$testDataPath/$fileFolder/$fileName")
        Assert.assertTrue(TC1a.exists(), "$testDataPath/$fileFolder/$fileName doesn't exist!")
        def text = TC1a.getText()
        def template = engine.createTemplate(text).make(binding)

        if (withProperties) {
            def TC1b = new File("$testDataPath/$fileFolder/${fileName}.properties")
            Assert.assertTrue(TC1b.exists(), "$testDataPath/$fileFolder/${fileName}.properties doesn't exist!")
            def text1 = TC1b.getText()
            def template1 = engine.createTemplate(text1).make(binding)

            createNewFile(connector + "\\${fileName}.properties", template1)
        }

        createNewFile(connector + "\\${fileName}", template)
    }

    public void copyFileToConnector(String fileName, String fileFolder, String connector, def binding) {
        copyFileAndPropertiesToConnector(fileName, fileFolder, connector, binding, false)
    }

}


