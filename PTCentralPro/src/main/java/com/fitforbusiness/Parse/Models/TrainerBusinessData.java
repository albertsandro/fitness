package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("TrainerBusinessData")
public class TrainerBusinessData extends ParseObject {

    public TrainerBusinessData () {

    }
    public void setStripeAccessToken  (String token) {
        put("stripeAccessToken ", token);
    }
    public String getStripeAccessToken  () {
        return getString("stripeAccessToken");
    }
    public void setPtLicenseNumber (String number) {
        put("ptLicenseNumber", number);
    }
    public String getPtLicenseNumber () {
        return getString("ptLicenseNumber");
    }
    public void setPtRenewalDate (Date date) {
        put("ptRenewalDate", date);
    }
    public Date getPtRenewalDate () {
        return getDate("ptRenewalDate");
    }

    public void setInsuranceNumber (String number) {
        put("insuranceNumber", number);
    }
    public String getInsuranceNumber () {
        return getString("insuranceNumber");
    }

    public void setInsuranceProvider (String provider) {
        put("insuranceProvider", provider);
    }
    public String getInsuranceProvider () {
        return getString("insuranceProvider");
    }

    public void setInsuranceRenewalDate (Date date) {
        put("insuranceRenewalDate", date);
    }
    public Date getInsuranceRenewalDate () {
        return getDate("insuranceRenewalDate");
    }

    public void setCompanyId (String id) {
        put("companyId", id);
    }
    public String getCompanyId () {
        return getString("companyId");
    }

    public void setCompanyName (String name) {
        put("companyName", name);
    }
    public String getCompanyName () {
        return getString("companyName");
    }

    public void setCompanyTaxId (String id) {
        put("companyTaxId", id);
    }
    public String getCompanyTaxId () {
        return getString("companyTaxId");
    }

    public void setAedRenewalDate (Date date) {
        put("aedRenewalDate", date);
    }
    public Date getAedRenewalDate () {
        return getDate("aedRenewalDate");
    }

    public void setCprRenewalDate (Date date) {
        put("cprRenewalDate", date);
    }
    public Date getCprRenewalDate () {
        return getDate("cprRenewalDate");
    }

    public void setFirstAidRenewalDate (Date date) {
        put("firstAidRenewalDate", date);
    }
    public Date getFirstAidRenewalDate () {
        return getDate("firstAidRenewalDate");
    }

    public void setWebsite (String website) {
        put("website", website);
    }
    public String getWebsite () {
        return getString("website");
    }

    public void setFacebook (String facebook) {
        put("facebook", facebook);
    }
    public String getFacebook () {
        return getString("facebook");
    }

    public void setTwitter (String twitter) {
        put("twitter", twitter);
    }
    public String getTwitter () {
        return getString("twitter");
    }

    public void setExperience (int experience) {
        put("experience", experience);
    }
    public int getExperience () {
        return getInt("experience");
    }

}