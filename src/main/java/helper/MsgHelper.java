/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author balis
 */
public class MsgHelper {

    public static void alert(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static String promptInput(Component parent, String message) {
        return JOptionPane.showInputDialog(parent, message, "EduSys", JOptionPane.INFORMATION_MESSAGE);
    }

}
