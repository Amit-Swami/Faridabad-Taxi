package com.example.faridabadtaxirider.Model;

public class Rider {
    private String name,phone,avatarUrl,rates,carType;

    public Rider() {
    }

    public Rider(String name, String phone, String avatarUrl, String rates, String carType) {
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
        this.carType = carType;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }
}
