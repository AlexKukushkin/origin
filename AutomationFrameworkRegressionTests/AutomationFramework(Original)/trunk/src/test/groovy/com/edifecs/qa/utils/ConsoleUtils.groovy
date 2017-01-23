package com.edifecs.qa.utils

/**
 * Created by IntelliJ IDEA.
 * User: InaG
 * Date: 11/16/11
 * Time: 4:08 PM
 *  Execute A Command using Ant
 */
class ConsoleUtils {
    /**
     * executes a command
     * @param imageLocation
     * @param command
     * @return
     */

    def static executeCommand(String imageLocation, String command) {
        AntBuilder ant = new AntBuilder()

        def osExecutablePrefix = getOsExecutablePrefix()
        ant.exec(dir: imageLocation, executable: getExecutable(), inputstring: "",
                resultproperty: "Result",
                errorproperty: "ErrorStream") {
            ant.arg(line: "${osExecutablePrefix} ${command}")
        }
    }

    /**
     * executes a command and if it is interactive responds with 'Y'
     * @param imageLocation
     * @param command
     * @return
     */
    def static executeCommandWithY(String imageLocation, String command) {
        AntBuilder ant = new AntBuilder()

        def osExecutablePrefix = getOsExecutablePrefix()
        ant.exec(dir: imageLocation, executable: getExecutable(), inputstring: "",
                resultproperty: "Result",
                errorproperty: "ErrorStream") {
            ant.arg(line: "${osExecutablePrefix} echo Y| ${command}")
        }
    }


    private static String getExecutable() {
        System.getenv("OS").startsWith("Windows") ? "cmd.exe" : "/bin/sh"
    }

    private static String getOsExecutablePrefix() {
        System.getenv("OS").startsWith("Windows") ? "/C" : ""
    }

}
