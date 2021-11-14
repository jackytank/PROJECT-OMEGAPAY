/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

import entity.User_Login;

/**
 *
 * @author balis
 */
public class AuthUser {

    public static User_Login user = null;

    public static void clear() {
        AuthUser.user = null;
    }

    public static boolean isLogin() {
        return AuthUser.user != null;
    }
}
