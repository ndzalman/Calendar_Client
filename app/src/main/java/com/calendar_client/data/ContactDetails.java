package com.calendar_client.data;

/**
 * Created by Nadav on 29/11/2016.
 */

public class ContactDetails {

    private String name;
    private String phone;

    public ContactDetails(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
