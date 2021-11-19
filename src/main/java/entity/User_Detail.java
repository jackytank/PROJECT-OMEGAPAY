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
public class User_Detail {

    private String omegaAccount;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean gender;
    private Date birthday;
    private String address;
    private Date dayCreated;
    private String status;
    private String photo;
    private float omegaBalance;

    public User_Detail() {
    }

    public User_Detail(String omegaAccount, String firstName, String lastName, String email, String phone, boolean gender, Date birthday, String address, Date dayCreated, String status, String photo, float omegaBalance) {
        this.omegaAccount = omegaAccount;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
        this.address = address;
        this.dayCreated = dayCreated;
        this.status = status;
        this.photo = photo;
        this.omegaBalance = omegaBalance;
    }

    public String getOmegaAccount() {
        return omegaAccount;
    }

    public void setOmegaAccount(String omegaAccount) {
        this.omegaAccount = omegaAccount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean getGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDayCreated() {
        return dayCreated;
    }

    public void setDayCreated(Date dayCreated) {
        this.dayCreated = dayCreated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public float getOmegaBalance() {
        return omegaBalance;
    }

    public void setOmegaBalance(float omegaBalance) {
        this.omegaBalance = omegaBalance;
    }

}
