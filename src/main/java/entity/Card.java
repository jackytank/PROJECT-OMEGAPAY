/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.Date;

/**
 *
 * @author dell
 */
public class Card {

    private int cardID;
    private String omegaAccount;
    private String cardNumber;
    private String PIN;
    private Date expirationDate;
    private String cardHolderName;
    private String billingAddress;
    private float cardBalance;
    private String cardName;

    public Card() {
    }

    public Card(int cardID, String omegaAccount, String cardNumber, String PIN, Date expirationDate, String cardHolderName, String billingAddress, float cardBalance, String cardName) {
        this.cardID = cardID;
        this.omegaAccount = omegaAccount;
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.expirationDate = expirationDate;
        this.cardHolderName = cardHolderName;
        this.billingAddress = billingAddress;
        this.cardBalance = cardBalance;
        this.cardName = cardName;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public String getOmegaAccount() {
        return omegaAccount;
    }

    public void setOmegaAccount(String omegaAccount) {
        this.omegaAccount = omegaAccount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public float getCardBalance() {
        return cardBalance;
    }

    public void setCardBalance(float cardBalance) {
        this.cardBalance = cardBalance;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }
}
