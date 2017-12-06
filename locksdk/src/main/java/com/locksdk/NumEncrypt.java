package com.locksdk;

import java.util.Arrays;

/**
 * Created by Sujiayong on 2017/12/6.
 *  加密工具
 */

public class NumEncrypt {

    private char[] ShuffleTable = new char[]{
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            '3', '4', '8', '7', '0', '9', '1', '6', '2', '5',
            '1', '6', '2', '0', '4', '9', '8', '3', '7', '5',
            '4', '2', '0', '5', '1', '7', '9', '3', '6', '8',
            '4', '1', '8', '7', '0', '6', '2', '3', '9', '5',
            '2', '5', '7', '9', '3', '4', '8', '6', '1', '0',
            '4', '5', '7', '9', '6', '3', '1', '0', '2', '8',
            '4', '0', '7', '8', '2', '6', '1', '5', '3', '9',
            '1', '0', '3', '9', '8', '6', '7', '5', '4', '2',
            '1', '0', '2', '3', '4', '7', '9', '5', '6', '8',
            '4', '3', '9', '6', '7', '2', '8', '1', '5', '0',
            '2', '1', '0', '5', '6', '9', '8', '4', '7', '3',
            '2', '3', '9', '6', '1', '4', '7', '8', '5', '0',
            '3', '9', '4', '7', '0', '1', '2', '5', '6', '8',
            '3', '0', '1', '9', '6', '4', '8', '5', '7', '2',
            '4', '8', '5', '6', '7', '3', '9', '0', '1', '2',
            '2', '4', '8', '3', '5', '7', '9', '0', '6', '1',
            '2', '8', '5', '6', '1', '9', '3', '4', '7', '0',
            '1', '3', '9', '6', '2', '5', '8', '4', '7', '0',
            '1', '4', '8', '3', '6', '5', '7', '2', '9', '0',
            '1', '5', '7', '9', '8', '3', '4', '0', '6', '2',
            '2', '0', '3', '9', '7', '5', '4', '1', '6', '8',
            '2', '0', '1', '3', '9', '5', '8', '6', '7', '4',
            '3', '2', '0', '5', '6', '4', '7', '8', '9', '1',
            '4', '7', '6', '5', '1', '3', '2', '9', '8', '0',
            '1', '9', '4', '8', '5', '2', '3', '6', '7', '0',
            '9', '7', '6', '4', '2', '1', '8', '3', '5', '0',
            '9', '8', '5', '3', '0', '6', '7', '2', '1', '4',
            '2', '9', '4', '8', '5', '6', '7', '3', '1', '0',
            '1', '2', '0', '5', '7', '4', '9', '3', '6', '8',
            '9', '1', '4', '7', '6', '3', '2', '0', '5', '8',
            '9', '0', '3', '8', '4', '1', '5', '6', '7', '2',
            '0', '8', '5', '3', '9', '7', '4', '1', '6', '2',
            '0', '9', '4', '7', '5', '2', '8', '6', '3', '1',
            '3', '1', '9', '6', '8', '7', '2', '0', '4', '5',
            '3', '5', '7', '9', '1', '6', '2', '8', '4', '0',
            '3', '6', '2', '0', '9', '7', '5', '4', '1', '8',
            '3', '7', '6', '5', '4', '9', '8', '1', '2', '0',
            '2', '6', '1', '0', '4', '3', '7', '8', '9', '5',
            '2', '7', '6', '5', '3', '1', '9', '0', '4', '8',
            '3', '8', '5', '6', '2', '4', '7', '1', '9', '0',
            '4', '9', '1', '7', '2', '5', '8', '6', '3', '0',
            '4', '0', '3', '9', '6', '5', '7', '8', '1', '2',
            '4', '0', '2', '3', '8', '7', '9', '1', '6', '5',
            '5', '2', '0', '4', '3', '9', '8', '6', '7', '1',
            '1', '7', '6', '5', '2', '3', '4', '8', '9', '0',
            '1', '8', '5', '6', '9', '7', '3', '0', '4', '2',
            '3', '0', '2', '1', '9', '6', '7', '8', '5', '4',
            '5', '3', '9', '6', '7', '4', '1', '8', '2', '0',
            '5', '4', '8', '7', '9', '2', '1', '0', '6', '3',
            '5', '1', '7', '9', '2', '4', '8', '6', '3', '0',
            '7', '3', '9', '6', '5', '2', '8', '4', '1', '0',
            '7', '0', '2', '3', '6', '1', '9', '4', '5', '8',
            '5', '6', '2', '0', '8', '3', '7', '1', '9', '4',
            '5', '9', '4', '7', '2', '6', '3', '8', '1', '0',
            '5', '0', '3', '9', '1', '7', '4', '2', '6', '8',
            '5', '0', '2', '3', '7', '9', '8', '6', '1', '4',
            '6', '2', '0', '4', '3', '1', '7', '8', '9', '5',
            '6', '3', '9', '1', '4', '7', '2', '0', '5', '8',
            '5', '7', '6', '4', '0', '2', '9', '1', '3', '8',
            '5', '8', '1', '6', '4', '9', '2', '3', '7', '0',
            '6', '4', '8', '7', '3', '9', '5', '1', '2', '0',
            '6', '5', '7', '9', '2', '1', '3', '8', '4', '0',
            '8', '6', '2', '0', '3', '5', '7', '1', '9', '4',
            '6', '1', '2', '0', '7', '5', '9', '4', '3', '8',
            '9', '0', '2', '3', '5', '6', '7', '8', '1', '4',
            '0', '2', '1', '4', '7', '5', '9', '3', '6', '8',
            '0', '3', '9', '5', '2', '4', '8', '6', '7', '1',
            '8', '5', '7', '1', '9', '2', '4', '6', '3', '0',
            '8', '7', '6', '4', '1', '2', '9', '0', '3', '5',
            '0', '4', '8', '7', '6', '5', '2', '3', '9', '1',
            '0', '5', '7', '8', '4', '3', '9', '1', '6', '2',
            '7', '9', '4', '1', '3', '2', '8', '6', '5', '0',
            '0', '6', '2', '9', '1', '4', '8', '3', '7', '5',
            '0', '7', '6', '4', '2', '3', '5', '8', '9', '1',
            '6', '7', '1', '4', '9', '5', '8', '3', '2', '0',
            '6', '8', '5', '1', '4', '3', '7', '2', '9', '0',
            '7', '5', '1', '8', '0', '3', '9', '2', '6', '4',
            '8', '4', '1', '7', '2', '5', '9', '0', '6', '3',
            '8', '1', '5', '3', '0', '9', '2', '6', '7', '4',
            '6', '9', '4', '7', '8', '3', '2', '0', '1', '5',
            '7', '4', '8', '1', '3', '6', '2', '5', '9', '0',
            '8', '5', '4', '7', '3', '6', '9', '1', '2', '0',
            '7', '6', '2', '0', '3', '9', '8', '5', '1', '4',
            '7', '1', '6', '4', '9', '3', '2', '8', '5', '0',
            '9', '3', '1', '5', '0', '7', '2', '4', '6', '8',
            '9', '4', '8', '7', '6', '1', '3', '5', '2', '0',
            '7', '8', '5', '6', '3', '1', '9', '0', '4', '2',
            '7', '4', '3', '8', '2', '6', '1', '9', '0', '5',
            '8', '0', '3', '1', '9', '7', '5', '2', '6', '4',
            '6', '0', '3', '8', '2', '1', '4', '9', '7', '5',
            '6', '0', '2', '3', '7', '1', '5', '8', '9', '4',
            '7', '2', '0', '4', '9', '1', '5', '3', '6', '8',
            '8', '2', '0', '4', '1', '9', '5', '6', '7', '3',
            '8', '3', '9', '6', '5', '4', '7', '1', '2', '0',
            '8', '0', '2', '3', '5', '9', '1', '6', '7', '4',
            '9', '2', '0', '4', '8', '6', '7', '5', '1', '3',
            '4', '5', '7', '8', '1', '6', '3', '9', '2', '0',
            '9', '6', '2', '0', '5', '7', '1', '4', '3', '8',
            '0', '1', '3', '8', '4', '6', '7', '5', '9', '2'};

    private void reverseStr(char[] str) {
        int len = strlen(str);
        char swap;
        int prePos, postPos;
        if (len == 0)
            return;
        prePos = 0;
        postPos = len - 1;
        while (prePos < postPos) {
            swap = str[postPos];
            str[postPos] = str[prePos];
            str[prePos] = swap;
            prePos++;
            postPos--;
        }
    }

    private char getShuffleTblIndex(int len, char[] str) {
        if (len == 0)
            return 75;
        if (len == 1)
            return (char) ((5 * ((str[0] - 0x30) + 1) + 2) % 100);
        if (len == 2)
            return (char) ((((str[0] - 0x30) + 1) * (str[1] - 0x30 + 1)) % 100);
        int indexVal = 0;
        for (int i = 1; i < len; i++) {
            indexVal += (str[i] - 0x30);
            indexVal += 3;
        }
        return (char) ((indexVal * ((str[0] - 0x30) + 1)) % 100);
    }

    private char getCrcChar(char[] str, int len) {
        char crc = 0;
        for (int i = 0; i < len; i++) {
            if (i % 2 == 0) {
                crc += (str[i] - 0x30);
            } else {
                crc ^= (str[i] - 0x30);
            }
        }
        return (char) ((crc % 10) + 0x30);
    }

    private int strlen(char[] str) {
        int index = 0;
        for (int i = 0; i < str.length; i++) {
            if (str[i] == '\0') {
                break;
            }
            index++;
        }
        return index;
    }

    public void encryptStr(char[] str) {
        int len = strlen(str);
        char[] savedStr = new char[15];
        char[] crcStr = new char[15];
        System.arraycopy(str, 0, crcStr, 0, len);
        crcStr[len] = getCrcChar(str, len);
        reverseStr(crcStr);
        System.arraycopy(crcStr, 0, savedStr, 0, crcStr.length);
        int index, offset;
        for (int i = 0; i < (len + 1); i++) {
            index = getShuffleTblIndex(i, savedStr);
            offset = crcStr[i] - 0x30;
            str[i] = ShuffleTable[index * 10 + offset];
        }
    }

    public char decryptStr(char[] str) {
        int len = strlen(str);
        int index, offset;
        for (int i = 0; i < len; i++) {
            index = getShuffleTblIndex(i, str);
            for (offset = 0; offset < 10; offset++) {
                if (ShuffleTable[index * 10 + offset] == str[i]) {
                    break;
                }
            }
            if (offset == 10)
                return 1;
            str[i] = (char) (0x30 + offset);
        }
        reverseStr(str);
        if (str[len - 1] != getCrcChar(str, len - 1))
            return 2;
        str[len - 1] = 0;
        return 0;
    }

//    public static void main(String[] args) {
//        NumEncrypt numEncrypt = new NumEncrypt();
//        String[] s = new String[]{"0000000", "0000001", "0000002", "0000003"
//                , "0000049", "0000078", "0000100", "1000100", "7050120", "0990999", "0030002"
//                , "9999999", "8999999"};
//
//        char[] a = new char[10];
//
//        for (int i = 0; i < s.length; i++) {
//            System.arraycopy(s[i].toCharArray(), 0, a, 0, s[i].length());
//
//            System.out.println(Arrays.toString(a));
//
//            numEncrypt.encryptStr(a);
//
//            System.out.println(Arrays.toString(a));
//
//            numEncrypt.decryptStr(a);
//
//            System.out.println(Arrays.toString(a));
//            System.out.println("-----------------------------------------------------");
//        }
//
//
//    }
}
