package com.fitforbusiness.Parse.Models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.File;
import java.util.Date;

/**
 * Created by Adeel on 6/7/2015.
 */
@ParseClassName("Accreditation")
public class Accreditation extends ParseObject {

    public Accreditation () {
    }

    public void setCecCourseName (String cecCourseName) {
        put("cecCourseName", cecCourseName);
    }
    public String getCecCourseName() {
        return  getString("cecCourseName");
    }

    public void setCourseNumber (String  courseNumber) {
        put("courseNumber", courseNumber);
    }
    public String getCourseNumber() {
        return  getString("courseNumber");
    }

    public void setDropBoxFilePath (String dropBoxFilePath) {
        put("dropBoxFilePath", dropBoxFilePath);
    }
    public String getDropBoxFilePath() {
        return  getString("dropBoxFilePath");
    }


    public String getRegisteredTrainingOrganisation() {
        return getString("registeredTrainingOrganisation");
    }
    public void setRegisteredTrainingOrgainsation(String registeredTrainingOrgainsation) {
        put("registeredTrainingOrganisation",registeredTrainingOrgainsation);
    }

    public void setDropboxFile (File dropboxFile) {
        put("dropboxFile", dropboxFile);
    }
    public File getDropboxFile() {
        return (File)get("dropboxFile");
    }

    public String getCompletedDate() {
        return getString("completedDate");
    }
    public void setCompletedDate(String completedDate) {
        put("completedDate", completedDate);
    }

    public Boolean getIsHours() {
        return getBoolean("isHours");
    }
    public void setIsHours(Boolean isHours) {
        put("isHours", isHours);
    }

    public String getPoints() {
        return getString("points");
    }
    public void setPoints(String points) {
        put("points", points);
    }

    public void setTrainer (Trainer trainer) {
        put("trainer", trainer);
    }
    public Trainer getTrainer () {
        return (Trainer) getParseUser("trainer");
    }
}
