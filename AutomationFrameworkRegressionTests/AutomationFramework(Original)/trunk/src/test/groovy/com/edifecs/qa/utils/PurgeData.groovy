package com.edifecs.qa.utils


import org.testng.annotations.Test

/**
 * Created by IntelliJ IDEA.
 * User: InaG
 * Date: 11/16/11
 * Time: 3:09 PM
 * Wrapper around command line purge tools
 */


class PurgeData {
    static def ECRootPath = System.getenv('ECRootPath')
    static def toolsPath = "$ECRootPath/TM/ServiceManager/tools"
    static def opsPath = "$ECRootPath/EUO/Common/components/ops-repository"


  //  @Test
    /**
     * calls delete transmission tool
     */
    public static void deleteTransmissions() {
        def deletePath = "$toolsPath/delete-transmission"
        def commandName = 'run_delete_transmission.bat'
        def xmlFile = new File('src/main/resources/PurgeAllTransmissionsSample.xml')
        def options = "-debug"

        ConsoleUtils.executeCommand(deletePath, "$commandName $xmlFile.absolutePath $options")
        System.out.println();

    }

   // @Test

    /**
     * calls purge operation repository tool
     */
    public static void purgeOPS() {
        def purgePath = "$opsPath/purge"
        def commandName = 'purge.bat'
        def xmlFile = new File('src/main/resources/OpsRepositoryPurgeSample.xml')
        def options = "-debug"

        ConsoleUtils.executeCommandWithY(purgePath, "$commandName $xmlFile.absolutePath $options")
    }

    @Test
    /*
    delete tracer, Outbound and errors folders
    */
    public static void deleteFolders() {
        def workPath = "$ECRootPath/EUO/CLM"
        def errorsFolder = new File("$workPath/errors")
        def tracerFolder = new File("$workPath/RIM/working/~.tracer")
        def outboundFolder = new File("$workPath/RIM/working/Outbound")
        def errorClaimFolder = new File("$workPath/RIM/working/ErrorClaim")

        errorsFolder.deleteDir()
        tracerFolder.deleteDir()
        outboundFolder.deleteDir()
        errorClaimFolder.deleteDir()

    }

    public static void main(String[] args) {

        deleteTransmissions()
        purgeOPS()
        deleteFolders()
    }

}