/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author balis
 */
public class DateHelper {
    
    public static SimpleDateFormat formatter = new SimpleDateFormat();
    
    public static Date toDate(String string, String pattern) {
        try {
            formatter.applyPattern(pattern);
            return formatter.parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String toString(Date date, String pattern) {
        formatter.applyPattern(pattern);
        return formatter.format(date);
    }
}
