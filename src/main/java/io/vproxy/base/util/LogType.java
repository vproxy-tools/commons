package io.vproxy.base.util;

public enum LogType {
    ACCESS,
    ALERT,
    BUFFER_INSUFFICIENT,
    CONN_ERROR,
    EVENT_LOOP_ADD_FAIL,
    EVENT_LOOP_CLOSE_FAIL,
    FILE_ERROR,
    HEALTH_CHECK_CHANGE,
    IMPROPER_USE,
    INVALID_EXTERNAL_DATA,
    INVALID_INPUT_DATA,
    NO_CLIENT_CONN,
    NO_EVENT_LOOP,
    PROBE,
    RESOLVE_REPLACE,
    SERVER_ACCEPT_FAIL,
    SOCKET_ERROR,
    SSL_ERROR,
    SYS_ERROR,
    UNEXPECTED,
    USER_HANDLE_FAIL,
    INVALID_STATE,

    UNCLASSIFIED,
}
