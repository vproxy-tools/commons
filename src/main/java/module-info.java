module io.vproxy.base {
    requires jdk.unsupported;
    requires vjson;

    exports io.vproxy.base;
    exports io.vproxy.base.connection;
    exports io.vproxy.base.dns;
    exports io.vproxy.base.dns.dnsserverlistgetter;
    exports io.vproxy.base.http;
    exports io.vproxy.base.processor.http1.builder;
    exports io.vproxy.base.processor.http1.entity;
    exports io.vproxy.base.prometheus;
    exports io.vproxy.base.util;
    exports io.vproxy.base.util.anno;
    exports io.vproxy.base.util.bitwise;
    exports io.vproxy.base.util.bytearray;
    exports io.vproxy.base.util.callback;
    exports io.vproxy.base.util.codec;
    exports io.vproxy.base.util.coll;
    exports io.vproxy.base.util.direct;
    exports io.vproxy.base.util.display;
    exports io.vproxy.base.util.exception;
    exports io.vproxy.base.util.file;
    exports io.vproxy.base.util.functional;
    exports io.vproxy.base.util.io;
    exports io.vproxy.base.util.log;
    exports io.vproxy.base.util.misc;
    exports io.vproxy.base.util.net;
    exports io.vproxy.base.util.nio;
    exports io.vproxy.base.util.objectpool;
    exports io.vproxy.base.util.promise;
    exports io.vproxy.base.util.ratelimit;
    exports io.vproxy.base.util.ringbuffer;
    exports io.vproxy.base.util.time;
    exports io.vproxy.base.util.time.impl;
    exports io.vproxy.base.util.unsafe;
    exports io.vproxy.base.util.web;
    exports io.vproxy.commons.graph;
    exports io.vproxy.commons.util;
    exports io.vproxy.vfd;
    exports io.vproxy.vpacket;
    exports io.vproxy.vpacket.dhcp;
    exports io.vproxy.vpacket.dhcp.options;
    exports io.vproxy.vpacket.dns;
    exports io.vproxy.vpacket.dns.rdata;
    exports io.vproxy.vpacket.tuples;
}
