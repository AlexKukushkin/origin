/*package com.edifecs.qa.CAS_Segment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

*//**
 * Created by c-alexcucu on 8/15/2016.
 *//*

public class FileComparison {
    static boolean binaryDiff(File a, File b) throws IOException {
        if(a.length() != b.length()){
            return false;
        }

        final int BLOCK_SIZE = 128;
        InputStream aStream = new FileInputStream(a);
        InputStream bStream = new FileInputStream(b);
        byte[] aBuffer = new byte[BLOCK_SIZE];
        byte[] bBuffer = new byte[BLOCK_SIZE];
        while (true) {
            int aByteCount = aStream.read(aBuffer, 0, BLOCK_SIZE);
            bStream.read(bBuffer, 0, BLOCK_SIZE);
            if (aByteCount < 0) {
                return true;
            }
            if (!Arrays.equals(aBuffer, bBuffer)) {
                return false;
            }
        }
    }
}*/
