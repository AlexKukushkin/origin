package com.edifecs.qa.utils;

import java.util.ArrayList;

/**
 * Created by c-alexcucu on 9/5/2016.
 */
public class VerifyDates extends ArrayList<String> {
    public static void verifyDates(String payment_file, int initial_position){
        int position = initial_position;
        String[] arrayString = payment_file.split("[*\\r\\n]");

        for (int i = 0; i < arrayString.length; i++) {
            System.out.println(arrayString[i]);
        }

        //boolean isBigger = false;
        long date = 20160430;

        if (arrayString.length != 0) {
            for (int i = position; i < arrayString.length; i++) {
                if((arrayString[i] == "DTM")&&(Long.parseLong(arrayString[i + 2]) > date)){
                    System.out.println("The position of 'DTM' node is : " + i);
                    System.out.println("The date is more than 2016/04/30. The compliance exception is expected to be generated. The test is passed");
                    System.exit(1);
                }
            }
        }
    }
}
