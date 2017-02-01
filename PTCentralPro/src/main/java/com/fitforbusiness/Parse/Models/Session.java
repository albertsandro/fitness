package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Session")
public class Session extends ParseObject {

    public Session () {
    }

    public void setStartDate (String startDate) {
        put("startDate", startDate);
    }
    public String getStartDate () {
        return getString("startDate");
    }

    public void setEndDate (String endDate) {
        put("endDate", endDate);
    }
    public String getEndDate () {
        return getString("endDate");
    }

    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }

    public void setDuration (Number duration) {
        put("duration", duration);
    }
    public Number getDuration () {
        return getNumber("duration");
    }

    public void setSessionType(Number sessionType){
        put("sessionType", sessionType);
    }
    public Number getSessionType(){
        return getNumber("sessionType");
    }

    public void setGroupClientID(String groupClentID){
        put("groupClientID", groupClentID);
    }
    public String getGroupClientId(){
        return getString("groupClientID");
    }

    public void setImportantFromNativeCalendar (Boolean importantFromNativeCalendar) {
        put("importantFromNativeCalendar", importantFromNativeCalendar);
    }

    public Boolean getImportantFromNativeCalendar() {
        return getBoolean("importantFromNativeCalendar");
    }

    public void setNotes (String notes) {
        put("notes", notes);
    }
    public String getNotes () {
        return getString("Notes");
    }

    public void setRecurrenceRule (String recurrenceRule) {
        put("recurrenceRule", recurrenceRule);
    }
    public String getRecurrenceRule () {
        return getString("recurrenceRule");
    }

    public void setNativeCalEventIdentfier (String nativeCalEventIdentfier) {
        put("nativeCalEventIdentfier", nativeCalEventIdentfier);
    }

    public String getNativeCalEventIdenfier () {
        return getString("nativeCalEventIdentfier");
    }

    public void setStartTime (String startTime) {
        put("startTime", startTime);
    }

    public String getStartTime () {
        return getString("startTime");
    }

    public void setEndTime (String endTime) {
        put("endTime", endTime);
    }

    public String getEndTime () {
        return getString("endTime");
    }

    public void setTitle (String title) {
        put("title",title);
    }

    public String getTitle () {
        return getString("title");
    }

    public void setVenue (String venue) {
        put("venue", venue);
    }

    public String getVenue () {
        return getString("venue");
    }

    public void setPackageID (String packageID) {
        put("packageId", packageID);
    }

    public String getPackageID () {
        return getString("packageId");
    }

    public void setStatus (Number status) {
        put("status", status);
    }
    public Number getStatus () {
        return getNumber("status");
    }

    public void setWorkout (String workout) {
        put("workout", workout);
    }
    public String getWorkout () {
        return getString("workout");
    }

    public void setSessionStatus (SessionStatus sessionStatus) {
        put("sessionStatus", sessionStatus);
    }
    public SessionStatus getSessionStatus () {
        return (SessionStatus)get("sessionStatus");
    }

    public void setSessionWorkout (SessionWorkout sessionWorkout) {
        put("sessionWorkout", sessionWorkout);
    }
    public SessionWorkout getSessionWorkout () {
        return (SessionWorkout)get("sessionWorkout");
    }
    public void setClient (Client client) {
        put("client", client);
    }
    public Client getClient() {
        return (Client)get("client");
    }
    public void setGroup (Group group) {
        put("group", group);
    }
    public Group getGroup() {
        return (Group)get("group");
    }
}

