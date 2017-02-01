package com.fitforbusiness.database;

public class Table {
    public static final String ID = "_id", TRAINER_ID = "trainer_id",
            DELETED = "deleted", UPDATED = "updated", ACTIVE = "active",
            TITLE = "title", SYNC_ID = "sync_id", CREATED = "created";


    public static final String DROP_TABLE = "drop table if exists %s";

    public static final String UPDATE_TRIGGER = "CREATE TRIGGER %s_change AFTER UPDATE ON %s"
            + " BEGIN "
            + " UPDATE %s SET updated = CURRENT_TIMESTAMP WHERE _id = new._id; "
            + " END";

    public static final String DROP_TRIGGER = "DROP TRIGGER %s_change";


    public static class Client {
        public static final String TABLE_NAME = "client";

        public static final String ID = "_id",
                CLIENT_ID = "client_id",
                FIRST_NAME = "first_name",
                LAST_NAME = "last_name",
                EMAIL = "email",
                CONTACT_NO = "contact_no",
                EMERGENCY_CONTACT_ADDRESS = "emergency_contact_address",
                EMERGENCY_CONTACT_NUMBER = "emergency_contact_number",
                DOB = "dob",
                GENDER = "gender",
                MEDICAL_NOTES = "medical_notes",
                ALLERGIES = "allergies",
                PHOTO_URL = "photo_url";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + CLIENT_ID + " integer, "
                + FIRST_NAME + " text not null, "
                + LAST_NAME + " text, "
                + EMAIL + "  text not null, "
                + CONTACT_NO + "  text, "
                + EMERGENCY_CONTACT_ADDRESS + "  text, "
                + EMERGENCY_CONTACT_NUMBER + "  text, "
                + DOB + "  datetime, "
                + GENDER + "  integer, "
                + MEDICAL_NOTES + "  text, "
                + ALLERGIES + "  text, "
                + PHOTO_URL + "  text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";

    }

    public static class SessionMeasurements {
        public static final String TABLE_NAME = "session_measurements";

        public static final String
                ID = "_id",
                SESSION_MEASUREMENT_ID = "session_measurement_id",
                SESSION_ID = "session_id",
                WORKOUT_ID = "workout_id",
                EXERCISE_ID = "exercise_id",
                MEASUREMENT_ID = "measurement_id",
                SET_NO = "set_no",
                MEASURED_VALUE = "measured_value",
                TARGET_VALUE = "target_value",
                ORDER_VALUE = "order_value";;

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + SESSION_MEASUREMENT_ID + " integer, "
                + SESSION_ID + " integer, "
                + WORKOUT_ID + " integer, "
                + EXERCISE_ID + " integer, "
                + MEASUREMENT_ID + " integer, "
                + SET_NO + " integer, "
                + MEASURED_VALUE + " integer, "
                + TARGET_VALUE + " integer, "
                + ORDER_VALUE + " integer default 0, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }

    public static class Sessions {
        public static final String TABLE_NAME = "sessions";

        public static final String
                ID = "_id",
                SESSION_ID = "session_id",
                TITLE = "title",
                VENUE = "venue",
                START_DATE = "start_date",
                END_DATE = "end_date",
                START_TIME = "start_time",
                END_TIME = "end_time",
                SESSION_TYPE = "session_type",
                SESSION_STATUS = "session_status",
                GROUP_ID = "group_id",
                NOTES = "notes",
                NATIVE_ID = "native_id",
                IS_NATIVE = "is_native",
                PACKAGE_ID = "package_id",
                RECURRENCE_RULE = "recurrence_rule";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + SESSION_ID + " integer, "
                + TITLE + " text, "
                + VENUE + " text, "
                + START_DATE + " datetime, "
                + END_DATE + " datetime, "
                + START_TIME + " time, "
                + END_TIME + " time, "
                + SESSION_TYPE + " integer default 0, "
                + GROUP_ID + " integer, "
                + NOTES + " text, "
                + SESSION_STATUS + " integer default 0 ,"
                + PACKAGE_ID + " text, "
                + NATIVE_ID + " long default 0, "
                + RECURRENCE_RULE + " text, "
                + SYNC_ID + " text, "
                + IS_NATIVE + " integer default 0, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }


    public static class Group {
        public static final String TABLE_NAME = "groups";

        public static final String
                ID = "_id",
                GROUP_ID = "group_id",
                NAME = "name",
                TRAINER_ID = "trainer_id",
                PHOTO_URL = "photo_url";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + GROUP_ID + " integer, "
                + NAME + " text not null, "
                + TRAINER_ID + "  integer, "
                + PHOTO_URL + "  text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }

    public static class GroupClients {
        public static final String TABLE_NAME = "group_clients";

        public static final String
                ID = "_id",
                GROUP_ID = "group_id",
                CLIENT_ID = "client_id";
        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + GROUP_ID + " integer, "
                + CLIENT_ID + " integer, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";

    }


    public static class Exercise {
        public static final String TABLE_NAME = "exercise";

        public static final String
                ID = "_id",
                EXERCISE_ID = "exercise_id",
                NAME = "name",
                MUSCLE_GROUP = "muscle_group",
                DESCRIPTION = "description",
                TAG = "tag",
                PHOTO_URL = "photo_url";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + EXERCISE_ID + " integer, "
                + NAME + " text not null , "
                + MUSCLE_GROUP + " text  , "
                + DESCRIPTION + " text  , "
                + TAG + " text  , "
                + PHOTO_URL + "  text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }

    public static class Workout {
        public static final String TABLE_NAME = "workout";

        public static final String ID = "_id",
                WORKOUT_ID = "workout_id",
                NAME = "name",
                DESCRIPTION = "description",
                PHOTO_URL = "photo_url";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + WORKOUT_ID + " integer, "
                + NAME + " text, "
                + DESCRIPTION + " text, "
                + PHOTO_URL + " text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }

    public static class WorkoutExercises {
        public static final String TABLE_NAME = "workout_exercises";

        public static final String
                ID = "_id",
                WORKOUT_ID = "workout_id",
                EXERCISE_ID = "exercise_id";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + WORKOUT_ID + " integer, "
                + EXERCISE_ID + " integer, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";

    }


    public static class ExerciseMeasurements {
        public static final String TABLE_NAME = "exercise_measurements";

        public static final String
                ID = "_id",
                EXERCISE_ID = "exercise_id",
                MEASUREMENT_ID = "measurement_id";
        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + EXERCISE_ID + " integer, "
                + MEASUREMENT_ID + " integer, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";
    }

    public static class TrainerProfileDetails {
        public static final String TABLE_NAME = "trainer";

        public static final String
                ID = "_id",
                TRAINER_ID = "trainer_id",
                FIRST_NAME = "first_name",
                LAST_NAME = "last_name",
                EMAIL_ID = "email_id",
                PHONE_NO = "phone_no",
                EMERGENCY_CONTACT = "emergency_contact_name",
                EMERGENCY_CONTACT_NO = "emergency_contact_no",
                DATE_OF_BIRTH = "date_of_birth",
                GENDER = "gender",
                COMPANY_NAME = "company_name",
                COMPANY_ID = "company_id",
                TAX_ID = "tax_id",
                INSURANCE_MEMBERSHIP_NO = "insurance_membership_no",
                INSURANCE_EXPIRY_DATE = "insurance_expiry_date",
                INSURANCE_PROVIDER = "insurance_provider",
                PT_LICENSE_NUMBER = "pt_license_number",
                PT_LICENSE_RENEWAL_DATE = "pt_license_renewal_date",
                FIRST_AID_CERT_RENEWAL_DATE = "first_aid_cert_renewal_date",
                CPR_CERT_RENEWAL_DATE = "cpr_cert_renewal_date",
                AED_CERT_RENEWAL_DATE = "aed_cert_renewal_date",
                WEBSITE = "website",
                EXPERIENCE = "experience",
                FACEBOOK_ID = "facebook_id",
                TWITTER_ID = "twitter_id";


        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " integer primary key autoincrement, "
                + TRAINER_ID + " integer not null unique, "
                + FIRST_NAME + " text, "
                + LAST_NAME + " text, "
                + EMAIL_ID + " text, "
                + PHONE_NO + " text, "
                + EMERGENCY_CONTACT + " text, "
                + EMERGENCY_CONTACT_NO + " text, "
                + DATE_OF_BIRTH + " datetime, "
                + GENDER + " integer, "
                + COMPANY_NAME + " text, "
                + COMPANY_ID + " text, "
                + TAX_ID + " text, "
                + INSURANCE_MEMBERSHIP_NO + " text, "
                + INSURANCE_EXPIRY_DATE + " datetime, "
                + INSURANCE_PROVIDER + " text, "
                + PT_LICENSE_NUMBER + " text, "
                + PT_LICENSE_RENEWAL_DATE + " datetime, "
                + FIRST_AID_CERT_RENEWAL_DATE + " datetime, "
                + CPR_CERT_RENEWAL_DATE + " datetime, "
                + AED_CERT_RENEWAL_DATE + " datetime, "
                + WEBSITE + " text, "
                + EXPERIENCE + " text,"
                + FACEBOOK_ID + " text, "
                + TWITTER_ID + " text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";


    }

    public static class Measurement {
        public static final String TABLE_NAME = "measurement";

        public static final String
                ID = "_id",
                MEASUREMENT_ID = "measurement_id",
                NAME = "name";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME
                + " ( "
                + ID + " integer primary key autoincrement, "
                + MEASUREMENT_ID + " integer, "
                + NAME + " text not null unique, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";

    }

    public static class TrainerProfileAccreditation {
        public static final String TABLE_NAME = "trainer_profile_accreditation";

        public static final String
                ID = "_id",
                ACCREDITATION_ID = "accreditation_id",
                COURSE_NAME = "course_name",
                COURSE_NO = "course_no",
                POINTS_HOURS = "points",
                IS_POINT = "is_point",
                REGISTERED_TRAINING_ORGANIZATION = "registered_training_organization",
                COMPLETED_DATE = "completed_date",
                LINKED_FILE = "linked_file";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + ACCREDITATION_ID + " integer , "
                + COURSE_NAME + " text, "
                + COURSE_NO + " text, "
                + POINTS_HOURS + " decimal, "
                + IS_POINT + " integer default 0, "
                + REGISTERED_TRAINING_ORGANIZATION + " text, "
                + COMPLETED_DATE + " datetime, "
                + LINKED_FILE + " text, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";


    }

    public static class AssessmentField {


        public static final String TABLE_NAME = "assessment_field";

        public static final String
                ID = "_ID",
                FIELD_ID = "field_id",
                SORT_ORDER = "sort_order",
                TITLE = "title",
                TYPE = "type",
                FORM_ID = "form_id";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + FIELD_ID + " integer , "
                + SORT_ORDER + " integer, "
                + TITLE + " integer, "
                + TYPE + " integer, "
                + FORM_ID + " integer, "
                + DELETED + " integer default 0, "
                + UPDATED + " datetime default current_timestamp )";

    }

    public static class AssessmentForms {
        public static final String TABLE_NAME = "assessment_forms";

        public static final String ID = "_id",
                FORM_ID = "form_id",
                FORM_NAME = "form_name",
                FORM_TYPE = "form_type",
                TRAINER_ID = "trainer_id";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + FORM_ID + " integer , "
                + FORM_NAME + " text, "
                + FORM_TYPE + " integer default 2, "
                + TRAINER_ID + " integer, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";


    }

    public static class CompletedAssessmentForm {
        public static final String TABLE_NAME = "completed_assessment_form";

        public static final String ID = "_id",
                FORM_ID = "form_id",
                FORM_NAME = "form_name",
                TRAINER_ID = "trainer_id",
                GROUP_ID = "group_id",
                COMPLETED_FORM_TYPE = "completed_form_type",
                FORM_TYPE = "form_type";


        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + FORM_ID + " integer , "
                + FORM_NAME + " text, "
                + TRAINER_ID + " integer, "
                + GROUP_ID + " integer, "
                + FORM_TYPE + " integer default 0, "
                + COMPLETED_FORM_TYPE + " integer default 2, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";


    }

    public static class CompletedAssessmentFormField {


        public static final String TABLE_NAME = "completed_assessment_form_field";

        public static final String
                ID = "_ID",
                FIELD_ID = "field_id",
                SORT_ORDER = "sort_order",
                TITLE = "title",
                TYPE = "type",
                ANSWER = "answer",
                FORM_ID = "form_id";

        public static final String CREATE_TABLE = "create table "
                + TABLE_NAME + " ( "
                + ID + " integer primary key autoincrement, "
                + FIELD_ID + " integer , "
                + SORT_ORDER + " integer, "
                + TITLE + " integer, "
                + TYPE + " integer, "
                + ANSWER + " integer, "
                + FORM_ID + " integer, "
                + SYNC_ID + " text, "
                + DELETED + " integer default 0, "
                + CREATED + " datetime default current_timestamp, "
                + UPDATED + " datetime default current_timestamp )";

    }

    public static class StripePayment {
        public static final String TABLE_NAME = "stripePayment";
        public static final String ID = "_id",
                PAYMENT_ID = "payment_id",  /// charge id
        DESCRIPTION = "description",    //description
        QUANTITY = "session",    //quantity
        PACKAGE_TOTAL = "package_total", // amount
        GROUP_ID = "group_id",  // group id
        CURRENCY = "currency",
        CLIENT_ID = "client_id", // clinet id
        CUSTOMER_ID = "customer", //customer id
        CUSTOMER_MAIL = "customer_mail",    //customer mail
        CREATED = "created";  // created

public static final String CREATE_TABLE = "create table "
        + TABLE_NAME + " ( "
        + ID + " integer primary key autoincrement, "
        + PAYMENT_ID + " text , "
        + DESCRIPTION + " text, "
        + QUANTITY + " integer, "
        + CLIENT_ID + " integer, "
        + PACKAGE_TOTAL + " integer, "
        + GROUP_ID + " integer, "
        + CUSTOMER_ID + " text, "
        + CUSTOMER_MAIL + " text , "
        + CURRENCY + " text, "
        + SYNC_ID + " text, "
        + DELETED + " integer default 0, "
        + CREATED + " long, "
        + UPDATED + " datetime default current_timestamp )";
        }

public static class Membership {
    public static final String TABLE_NAME = "membership";
    public static final String ID = "_id",
            MEMBERSHIP_ID = "membership_id",
            DESCRIPTION = "description",
            SESSION = "session",    //no of sessions
            COST_PER_SESSION = "cost_per_session",
            PACKAGE_TOTAL = "package_total",
            GROUP_ID = "group_id",
            MEMBERSHIP_TYPE = "membership_type",
            RECURRING_PAYMENT = "recurring_payment",
            INTERVAL_PERIOD = "interval_period",
            NO_OF_INTERVALS = "no_of_intervals",
            CURRENCY = "currency", STATUS = "status";

    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + " ( "
            + ID + " integer primary key autoincrement, "
            + MEMBERSHIP_ID + " integer , "
            + DESCRIPTION + " text, "
            + SESSION + " integer, "
            + COST_PER_SESSION + " integer, "
            + PACKAGE_TOTAL + " integer, "
            + GROUP_ID + " integer, "
            + MEMBERSHIP_TYPE + " integer default 0, "
            + RECURRING_PAYMENT + " integer default 0, "
            + INTERVAL_PERIOD + " integer, "
            + NO_OF_INTERVALS + " integer , "
            + CURRENCY + " text, "
            + STATUS + " text, "
            + SYNC_ID + " text, "
            + DELETED + " integer default 0, "
            + CREATED + " datetime default current_timestamp, "
            + UPDATED + " datetime default current_timestamp )";
}

public static class GroupMembership {
    public static final String TABLE_NAME = "group_membership";
    public static final String ID = "_id",
            GROUP_MEMBERSHIP_ID = "group_membership_id",
            MEMBERSHIP_ID = "membership_id",
            GROUP_ID = "group_id",
            CLIENT_ID = "client_id",
            IS_PAID = "is_paid";

    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + " ( "
            + ID + " integer primary key autoincrement, "
            + GROUP_MEMBERSHIP_ID + " integer , "
            + MEMBERSHIP_ID + " integer , "
            + GROUP_ID + " integer, "
            + CLIENT_ID + " integer , "
            + IS_PAID + " integer default 0, "
            + SYNC_ID + " text, "
            + DELETED + " integer default 0, "
            + CREATED + " datetime default current_timestamp, "
            + UPDATED + " datetime default current_timestamp )";
}

public static class SyncLog {
    public static final String TABLE_NAME = "sync_log";
    public static final String ID = "_id",
            SYNC_LOG_ID = "sync_log_id",
            START_TIME = "start_time",
            FINISH_TIME = "finish_time";

    public static final String CREATE_TABLE = "create table "
            + TABLE_NAME + " ( "
            + ID + " integer primary key autoincrement, "
            + SYNC_LOG_ID + " integer, "
            + START_TIME + " datetime default current_timestamp, "
            + FINISH_TIME + " datetime default current_timestamp, "
            + SYNC_ID + " text, "
            + DELETED + " integer default 0, "
            + CREATED + " datetime default current_timestamp, "
            + UPDATED + " datetime default current_timestamp )";
}
}
