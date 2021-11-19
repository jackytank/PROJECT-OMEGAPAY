/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author dell
 */
public class User_Login {

    private String omegaAccount;
    private String username;
    private String password;

    public User_Login() {
    }

    public User_Login(String omegaAccount, String username, String password) {
        this.omegaAccount = omegaAccount;
        this.username = username;
        this.password = password;
    }

    public String getOmegaAccount() {
        return omegaAccount;
    }

    public void setOmegaAccount(String omegaAccount) {
        this.omegaAccount = omegaAccount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
