package io.vproxy.vpacket.dns;

public enum DNSClass {
    IN(1), // internet
    CH(3), // the CHAOS class
    HS(4), // Hesiod [Dyer 87]

    NONE(254, true), // only used as QClass
    ANY(255, true), // only used as QClass

    NOT_CLASS(-1), // some RR does not use this field as class name
    ;
    public final int code;
    public final boolean question;

    DNSClass(int code) {
        this(code, false);
    }

    DNSClass(int code, boolean question) {
        this.code = code;
        this.question = question;
    }
}
