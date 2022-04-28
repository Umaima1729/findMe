package com.example.findme;

public class Contact {
    public String phoneNumber;
    public Contact(String contact){
        phoneNumber = contact;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
