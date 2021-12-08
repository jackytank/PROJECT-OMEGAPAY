/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import java.util.Random;

/**
 *
 * @author balis
 */
public class UtilityHelper {

    public static int selectedTab;

    public static String toVND(float element) {
        return String.format("%,.0f", element) + " VND";
    }

    public static float toFloat(String value) {
        return value == null || value.isEmpty() ? 0 : Float.parseFloat(value);
    }
    
    // generate random string from a to z 
    public static String randomString(int length) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        System.out.println(generatedString);
        return generatedString;
    }
}
