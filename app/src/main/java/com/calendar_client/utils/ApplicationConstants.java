package com.calendar_client.utils;

import com.calendar_client.data.Reminder;

import java.util.ArrayList;
import java.util.List;

public class ApplicationConstants {

   //public static final String SERVER_IP = "10.0.2.2"; // nadav IP
//    private static final String SERVER_IP = "192.168.1.11"; // anael IP
        private static final String SERVER_IP = "calendario.eu-gb.mybluemix.net"; // anael IP
    //   public static final String SERVER_IP = "localhost";
    private static final String PORT = "8080";


//    public static final String LOGIN_URL = "http://" + SERVER_IP +":" + PORT + "/CalendarServer/rest/users/checkUser";
//    public static final String SIGN_UP_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/insertUser";
//    public static final String ADD_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/addEvent";
//    public static final String EDIT_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/updateEvent";
//    public static final String DELETE_EVENT_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/removeEvent";
//
//    public static final String REFRESH_TOKEN_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/insertTokenToUser";
//    public static final String GET_ALL_USERS_URL = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/getAllUsers";
//
//    public static final String GET_ALL_EVENTS = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/getAllEvents";
//
//    public static final String GET_ALL_SHARED_EVENTS = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/getAllSharedEvents";
//    public static final String GET_UPCOMING_EVENTS = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/getUpcomingEvents";
//    public static final String GET_EVENTS_OF_TODAY = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/getEventsByDay";
//
//    public static final String REMOVE_USER_FROM_EVENT = "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/users/removeUserFromEvent";
//
//    public static final String UPDATE_USER = "http://" + SERVER_IP +":" + PORT + "/CalendarServer/rest/users/updateUser";
//
//    public static final String GET_EVENT_BY_ID= "http://"  + SERVER_IP +":"+ PORT + "/CalendarServer/rest/events/getEventsByEventId";

    public static final String LOGIN_URL = "http://" + SERVER_IP + "/rest/users/checkUser";
    public static final String SIGN_UP_URL = "http://"  + SERVER_IP + "/rest/users/insertUser";
    public static final String ADD_EVENT_URL = "http://"  + SERVER_IP + "/rest/events/addEvent";
    public static final String EDIT_EVENT_URL = "http://"  + SERVER_IP + "/rest/events/updateEvent";
    public static final String DELETE_EVENT_URL = "http://"  + SERVER_IP + "/rest/events/removeEvent";

    public static final String REFRESH_TOKEN_URL = "http://"  + SERVER_IP + "/rest/users/insertTokenToUser";
    public static final String GET_ALL_USERS_URL = "http://"  + SERVER_IP + "/rest/users/getAllUsers";

    public static final String GET_ALL_EVENTS = "http://"  + SERVER_IP + "/rest/events/getAllEvents";

    public static final String GET_ALL_SHARED_EVENTS = "http://"  + SERVER_IP  + "/rest/events/getAllSharedEvents";
    public static final String GET_UPCOMING_EVENTS = "http://"  + SERVER_IP  + "/rest/events/getUpcomingEvents";
    public static final String GET_EVENTS_OF_TODAY = "http://"  + SERVER_IP  + "/rest/events/getEventsByDay";

    public static final String REMOVE_USER_FROM_EVENT = "http://"  + SERVER_IP  + "/rest/users/removeUserFromEvent";

    public static final String UPDATE_USER = "http://" + SERVER_IP + "/rest/users/updateUser";

    public static final String GET_EVENT_BY_ID= "http://"  + SERVER_IP  + "/rest/events/getEventsByEventId";
}
