package com.calendar_client.utils;

/**
 * Created by anael on 17/10/16.
 */

public class ApplicationConstants {

    public static final String SERVER_IP = "10.0.0.105"; // nadav IP
//    public static final String SERVER_IP = "192.168.1.11"; // anael IP
    public static final String PORT = "8080";


    public static final String LOGIN_URL = "http://" + SERVER_IP +":" + PORT + "/CalendarServer/rest/users/checkUser";
    public static final String SIGN_UP_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/insertUser";
    public static final String ADD_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/addEvent";
    public static final String EDIT_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/updateEvent";



}
