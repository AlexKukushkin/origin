package com.edifecs.qa.utils;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by c-alexcucu on 8/29/2016.
 */

public class InputOutputStream extends ArrayList<String> {
    //public static String lineSeparator = System.getProperty("LineSeparator");

    //Validation part
    public static void validateFiles(String assertion, String outbound_encounter, int initial_position) throws IOException {

/*        FileInputStream firstTargetFile = new FileInputStream(new File("./src/test/resources/com/edifecs/qa/Validation/Assert.dat"));
        String assert_file = IOUtils.toString(firstTargetFile, "UTF-8");*/

        int position = initial_position;
        String assert_1 = assertion.replace("\'", "\r\n");
        String assert_2 = assert_1.replace('~','*');
        String[] arrayAssertion = assert_2.split("[*\\r\\n]");

/*        for (int i = 0; i < arrayAssertion.length; i++) {
            System.out.println(arrayAssertion[i]);
        }*/
/*        System.out.print("Found Index : ");
        System.out.println(assert_2.indexOf("NM1"));*/

        System.out.println("Assertion file : ");
        System.out.println("------------------");
        System.out.println(assert_2);
        System.out.println("=========================================================================================");
        System.out.println();

/*        FileInputStream secondTargetFile = new FileInputStream(new File("./src/test/resources/com/edifecs/qa/Validation/OutboundEncounter.dat"));
        String outbound = IOUtils.toString(secondTargetFile, "UTF-8");*/

        String outbound_1 = outbound_encounter.replace("\'", "\r\n");
        String outbound_2 = outbound_1.replace('~','*');

        System.out.println("Outbound encounter - attachment : ");
        System.out.println("-----------------------------------");
        System.out.println(outbound_2);


        //String[] arrayAssertion = assert_2.split("\\*");
        String[] arrayOutbound = outbound_2.split("[*\\r\\n]");
        //arrayOutbound = outbound_2.split("\\r\\n");
        boolean isEqual = true;

        //i = 41;
        if (arrayAssertion.length == arrayOutbound.length) {
            for (int i = position; i < arrayAssertion.length; i++) {
                if( !arrayAssertion[i].equals("{ignore}") &&
                        !arrayAssertion[i].equals( arrayOutbound[i]) ){
                    isEqual = false;
                    System.out.println("The values are not equal : ");
                    System.out.println("--------------------------");
                    System.out.println("Value of the node from assertion file on position [i] = " + i + " is " + arrayAssertion[i]);
                    System.out.println("Value of the node from outbound attachment file on position [i] = " + i + " is " + arrayOutbound[i]);
                    System.out.println();
                }
            }
            if (isEqual)
            {
                System.out.println();
                System.out.println("The validation is complete. The test is passed.");
            }
            else
            {
                System.out.println("The files are wrong. The test is failed.");
            }
            //System.out.println("The test is passed.");
        }
    }
}
