/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import entity.User_Login;
import java.util.Random;

/**
 *
 * @author balis
 */
public class SendPhone {

    private static final String ACCOUNT_SID = "";

    private static final String AUTH_TOKEN = "";

    private static final String PHONE_NUMBER = "";

    public static String SMSCode;

    public static void send(String toPhone) {
        SMSCode = generateVerifyCode();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new PhoneNumber("+84" + toPhone),
                new PhoneNumber(PHONE_NUMBER),
                "Your OmegaPay verification code is: " + SMSCode
        ).create();
    }

    //generate random verification code of length 6
    private static String generateVerifyCode() {
        return "" + (100000 + new Random().nextInt(900000));
    }

    public static boolean isCodeValid(String input, User_Login user_Login) {
        return input.equals(SMSCode);
    }
}
