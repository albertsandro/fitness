package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Sale")
public class Membership extends ParseObject {

    public Membership() {
    }

    public void setDescription (String description) {
        put("description", description);
    }
    public String getDescription() {
        return getString("description");
    }

    public void setNumberOfSessions (String numberOfSessions) {
        put ("numberOfSessions", numberOfSessions);
    }
    public String getNumberOfSessions () {
        return getString("numberOfSessions");
    }

    public void setTotalPackage (String totalPackage) {
        put("totalPackage", totalPackage);
    }
    public String getTotalPackage() {
        return getString("totalPackage");
    }

    public void setCostPerSession(String costPerSession) {
        put("costPerSession", costPerSession);
    }
    public String getCostPerSession() {
        return getString("costPerSession");
    }

    public void setGroupId(String groupId) {
        put("groupId", groupId);
    }
    public String getGroupId() {
        return getString("groupId");
    }

    public void setMembershipType(String membershipType) {
        put("membershipType", membershipType);
    }
    public String getMembershipType() {
        return getString("membershipType");
    }

    public void setRecurringPayment(Number recurringPayment) {
        put("recurringPayment", recurringPayment);
    }
    public Number getRecurringPayment() {
        return getNumber("recurringPayment");
    }

    public void setIntervalPeriod(String intervalPeriod) {
        put("intervalPeriod", intervalPeriod);
    }
    public String getIntervalPeriod() {
        return getString("intervalPeriod");
    }

    public void setCurrency(String currency) {
        put("currency", currency);
    }
    public String getCurrency() {
        return getString("currency");
    }

    public void setNumberOfInterval(String numberOfInterval) {
        put("numberOfInterval", numberOfInterval);
    }
    public String getNumberOfInterval() {
        return getString("numberOfInterval");
    }

    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }



}
