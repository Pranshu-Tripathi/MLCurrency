package com.example.mlcurrency;

public class Currency {
    String countryCode;
    String convertedValues;
    public Currency(String countryCode, String convertedValues){
        this.countryCode = countryCode;
        this.convertedValues = convertedValues;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getConvertedValues() {
        return convertedValues;
    }

    public void setConvertedValues(String convertedValues) {
        this.convertedValues = convertedValues;
    }
}
