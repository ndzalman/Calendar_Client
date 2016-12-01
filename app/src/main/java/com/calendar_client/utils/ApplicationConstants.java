package com.calendar_client.utils;
public class ApplicationConstants {

//   public static final String SERVER_IP = "10.0.0.100"; // nadav IP
   public static final String SERVER_IP = "10.0.0.104"; // anael IP
//   public static final String SERVER_IP = "localhost";
    public static final String PORT = "8080";


    public static final String LOGIN_URL = "http://" + SERVER_IP +":" + PORT + "/CalendarServer/rest/users/checkUser";
    public static final String SIGN_UP_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/insertUser";
    public static final String ADD_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/addEvent";
    public static final String EDIT_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/updateEvent";
    public static final String DELETE_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/removeEvent";

    public static final String REFRESH_TOKEN_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/insertTokenToUser";
    public static final String GET_ALL_USERS_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/getAllUsers";


}
