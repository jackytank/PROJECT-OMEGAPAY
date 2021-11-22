/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

/**
 *
 * @author balis
 */
public class UtilityHelper {

    public static String toVND(float element) {
        return String.format("%,.0f", element) + " VND";
    }

    public static float toFloat(String value) {
        return value == null || value.isEmpty() ? 0 : Float.parseFloat(value);
    }
}
