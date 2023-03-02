package io.vproxy.vpacket.dns;

public class InvalidDNSPacketException extends Exception {
    public InvalidDNSPacketException(String message) {
        super(message);
    }

    public InvalidDNSPacketException(String message, Throwable cause) {
        super(message, cause);
    }
}
