/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author balis
 */
//this helper use to limit the number of characters in JTextField
public class JTextFieldLimit extends PlainDocument {

    private int limit;

    public JTextFieldLimit(int limit) {
        super();
        this.limit = limit;
    }

    public JTextFieldLimit(int limit, int upper) {
        super();
        this.limit = limit;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        if ((getLength() + str.length()) <= limit) {
            super.insertString(offset, str, attr);
        }
    }

}
