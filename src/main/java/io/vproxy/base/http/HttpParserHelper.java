package io.vproxy.base.http;

import io.vproxy.base.processor.http1.builder.ChunkBuilder;
import io.vproxy.base.processor.http1.builder.HeaderBuilder;
import io.vproxy.base.processor.http1.builder.HttpEntityBuilder;
import io.vproxy.base.util.ByteArray;
import io.vproxy.base.util.Logger;
import io.vproxy.base.util.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class HttpParserHelper {
    /*
     * 0 => idle ~> 1 (if request) or -> 22 (if response)
     * 1 => method ~> SP -> 2
     * 2 => uri ~> SP -> 3 or \r\n -> 4
     * 3 => version ~> \r\n -> 4
     * 4 => end-first-line ~> -> 5 or \r\n -> 9
     * 5 => header-key ~> ":" -> 6
     * 6 => header-split ~> -> 7 or \r\n -> 8
     * 7 => header-value ~> \r\n -> 8
     * 8 => end-one-header ~> -> 5 or \r\n -> 9
     * 9 => end-all-headers ~> (if content-length) -> 10 or (if transfer-encoding:chunked) -> 11 or end -> 0
     * 10 => body ~> end -> 0
     * 11 => chunk ~> ";" -> 12 or \r\n -> 14
     * 12 => chunk-extension-split ~> -> 13 or \r\n -> 14
     * 13 => chunk-extension ~> \r\n -> 14
     * 14 => end-chunk-size ~> (if chunk-size) -> 15 or (trans -> 27 or data -> (if !chunk-size) -> 17 or \r\n -> 25)
     * 15 => chunk-content ~> \r\n -> 16
     * 16 => end-chunk-content ~> \r\n -> 26
     * 17 => trailer-key ~> ":" -> 18
     * 18 => trailer-split ~> -> 19 or \r\n -> 20
     * 19 => trailer-value ~> \r\n -> 20
     * 20 => end-one-trailer ~> -> 17 or \r\n -> 25
     * 21 => end-all ~> 0
     *
     * 22 => response-version ~> SP -> 23
     * 23 => status ~> SP -> 24
     * 24 => reason ~> \r\n -> 4
     *
     * 25 => end-all-trailers ~> -> 21
     * 26 => end-chunk ~> -> 11
     * 27 => end-all-chunks ~> -> 14
     */
    public static final int STATE_IDLE = 0;
    public static final int STATE_END_ALL_HEADERS = 9;
    public static final int STATE_BODY = 10;
    public static final int STATE_CHUNK_BEGIN = 11;
    public static final int STATE_CHUNK_CONTENT = 15;
    public static final int STATE_END_CHUNK = 26;
    public static final int STATE_END_ALL_CHUNKS = 27;
    public static final int STATE_END_ALL_TRAILERS = 25;

    static final Set<Integer> terminateStatesParseAllMode = Set.of(
        STATE_IDLE
    );
    static final Set<Integer> terminateStatesStepsMode = Set.of(
        STATE_END_ALL_HEADERS,
        STATE_BODY,
        STATE_CHUNK_CONTENT,
        STATE_END_CHUNK,
        STATE_END_ALL_CHUNKS,
        STATE_END_ALL_TRAILERS,
        STATE_IDLE
    );
    static final Set<Integer> hasNextState = Set.of(
        STATE_END_ALL_HEADERS,
        STATE_BODY,
        STATE_CHUNK_CONTENT,
        STATE_END_CHUNK,
        STATE_END_ALL_CHUNKS,
        STATE_END_ALL_TRAILERS
    );

    final Handler[] handlers = new Handler[]{
        null, // 0 entry
        null, // 1 req
        null, // 2 req
        null, // 3 req
        this::state4,
        this::state5,
        this::state6,
        this::state7,
        this::state8,
        this::state9,
        this::state10,
        this::state11,
        this::state12,
        this::state13,
        this::state14,
        this::state15,
        this::state16,
        this::state17,
        this::state18,
        this::state19,
        this::state20,
        this::state21,
        null, // 22 resp
        null, // 23 resp
        null, // 24 resp
        this::state25,
        this::state26,
        this::state27,
    };

    interface Handler {
        int handle(byte b) throws Exception;
    }

    public static class Params {
        public boolean segmentedParsing = false;
        public boolean buildResult = true;
        public boolean headersOnly = false;

        public Params() {
        }

        public Params(Params that) {
            this.segmentedParsing = that.segmentedParsing;
            this.buildResult = that.buildResult;
            this.headersOnly = that.headersOnly;
        }

        public Params setSegmentedParsing(boolean segmentedParsing) {
            this.segmentedParsing = segmentedParsing;
            return this;
        }

        public Params setBuildResult(boolean buildResult) {
            this.buildResult = buildResult;
            return this;
        }

        public Params setHeadersOnly(boolean headersOnly) {
            this.headersOnly = headersOnly;
            return this;
        }
    }

    abstract int getState();

    abstract void setState(int state);

    abstract HttpEntityBuilder getHttpEntity();

    private final Params params;

    private HeaderBuilder header;
    private List<HeaderBuilder> headers;
    private byte[] buf;
    private int bufOffset;
    private HeaderBuilder trailer;
    private List<HeaderBuilder> trailers;

    HttpParserHelper(Params params) {
        this.params = new Params(params);
    }

    int doSwitch(byte b) throws Exception {
        return handlers[getState()].handle(b);
    }

    int nextState(int state) throws Exception {
        switch (state) {
            case STATE_END_ALL_HEADERS: return state9Trans();
            case STATE_BODY: return end();
            case STATE_CHUNK_CONTENT: return 16;
            case STATE_END_CHUNK: return 11;
            case STATE_END_ALL_CHUNKS: return 14;
            case STATE_END_ALL_TRAILERS: return state25Trans();
            default: throw new Exception("unknown state for method `nextState`: " + state);
        }
    }

    private int end() {
        header = null;
        headers = null;
        getHttpEntity().dataLength = -1;
        getHttpEntity().isChunked = false;
        buf = null;
        bufOffset = 0;
        getHttpEntity().chunk = null;
        trailer = null;
        trailers = null;
        return 0;
    }

    private int state4(byte b) throws Exception {
        if (b == '\r') {
            // do nothing
            return 4;
        } else if (b == '\n') {
            setState(9);
            return state9(null);
        } else {
            setState(5);
            return state5(b);
        }
    }

    private int state5(byte b) throws Exception {
        if (header == null) {
            header = new HeaderBuilder();
        }

        if (b == ':') {
            setState(6);
            return state6(b);
        } else {
            header.key.append((char) b);
            return 5;
        }
    }

    private int state6(byte b) throws Exception {
        if (b == ':') {
            return 7;
        } else {
            throw new Exception("invalid header: " + header + ", invalid splitter " + (char) b);
        }
    }

    private int state7(byte b) {
        if (b == '\r') {
            // ignore
            return 7;
        } else if (b == '\n') {
            return 8;
        } else {
            if (b != ' ' || header.value.length() != 0) { // leading spaces of the value are ignored
                header.value.append((char) b);
            }
            return 7;
        }
    }

    private int state8(byte b) throws Exception {
        if (headers == null) {
            headers = new LinkedList<>();
            getHttpEntity().headers = headers;
        }
        if (header != null) {
            assert Logger.lowLevelDebug("received header " + header);
            var hdr = header.key.toString();
            if (hdr.equalsIgnoreCase("content-length")) {
                var len = header.value.toString().trim();
                assert Logger.lowLevelDebug("found Content-Length: " + len);
                getHttpEntity().dataLength = parseNonNegativeLen(len);
            } else if (hdr.equalsIgnoreCase("transfer-encoding")) {
                var encoding = header.value.toString().trim().toLowerCase();
                assert Logger.lowLevelDebug("found Transfer-Encoding: " + encoding);
                if (encoding.equals("chunked")) {
                    getHttpEntity().isChunked = true;
                }
            } else if (hdr.equalsIgnoreCase("host")) {
                var host = header.value.toString().trim();
                assert Logger.lowLevelDebug("found Host: " + host);
                getHttpEntity().lastHostHeader = host;
            }
            headers.add(header);
            header = null;
        }

        if (b == '\r') {
            // ignore
            return 8;
        } else if (b == '\n') {
            setState(9);
            return state9(null);
        } else {
            setState(5);
            return state5(b);
        }
    }

    private int parseNonNegativeLen(String len) throws Exception {
        int intLen;
        try {
            intLen = Integer.parseInt(len);
        } catch (NumberFormatException e) {
            throw new Exception("invalid Content-Length: " + len);
        }
        if (intLen < 0) {
            throw new Exception("invalid Content-Length: " + len);
        }
        if (getHttpEntity().dataLength >= 0) {
            // already exists
            throw new Exception("duplicated Content-Length: orig: " + getHttpEntity().dataLength + ", new: " + intLen);
        }
        return intLen;
    }

    // it's for state transferring
    private int state9(@SuppressWarnings("unused") Byte b) {
        if (params.headersOnly) {
            return end();
        }
        if (params.segmentedParsing) {
            return 9;
        }
        return state9Trans();
    }

    private int state9Trans() {
        if (headers == null) {
            return end();
        }
        if (getHttpEntity().dataLength >= 0) {
            if (getHttpEntity().dataLength == 0) {
                return end();
            } else {
                return 10;
            }
        } else if (getHttpEntity().isChunked) {
            return 11;
        }
        assert Logger.lowLevelDebug("Content-Length and Transfer-Encoding both not found");
        return end();
    }

    private int state10(byte b) {
        if (params.segmentedParsing) {
            return 10;
        }
        int contentLength = getHttpEntity().dataLength;
        var entity = getHttpEntity();
        if (entity.body == null) {
            buf = Utils.allocateByteArray(contentLength);
            bufOffset = 0;
            entity.body = ByteArray.from(buf);
        }
        buf[bufOffset++] = b;
        if (bufOffset == buf.length) {
            buf = null; // gc
            return end();
        }
        return 10;
    }

    private int state11(byte b) throws Exception {
        if (getHttpEntity().chunk == null) {
            getHttpEntity().chunk = new ChunkBuilder();
        }
        if (b == ';') {
            return 12;
        } else if (b == '\r') {
            // ignore
            return 11;
        } else if (b == '\n') {
            setState(14);
            return state14(null);
        } else {
            getHttpEntity().chunk.size.append((char) b);
            return 11;
        }
    }

    private int state12(byte b) throws Exception {
        if (b == '\r') {
            // ignore
            return 12;
        } else if (b == '\n') {
            setState(14);
            return state14(null);
        } else {
            if (getHttpEntity().chunk.extension == null) {
                getHttpEntity().chunk.extension = new StringBuilder();
            }
            getHttpEntity().chunk.extension.append((char) b);
            return 13;
        }
    }

    private int state13(byte b) throws Exception {
        if (b == '\r') {
            // ignore
            return 13;
        } else if (b == '\n') {
            setState(14);
            return state14(null);
        } else {
            getHttpEntity().chunk.extension.append((char) b);
            return 13;
        }
    }

    // this method may be called before entering state 14
    // it's for state transferring
    private int state14(Byte b) throws Exception {
        int size;
        if (getHttpEntity().chunk == null) {
            size = 0;
        } else {
            try {
                size = Integer.parseInt(getHttpEntity().chunk.size.toString().trim(), 16);
            } catch (NumberFormatException e) {
                throw new Exception("invalid chunk size: " + getHttpEntity().chunk.size);
            }
            if (size < 0) {
                throw new Exception("invalid chunk size: " + size);
            }
        }
        if (size != 0) {
            getHttpEntity().dataLength = size;
            return 15;
        } else {
            if (b == null) { // called from other states
                // end chunk
                if (getHttpEntity().chunks == null) {
                    getHttpEntity().chunks = new LinkedList<>();
                }
                getHttpEntity().chunks.add(getHttpEntity().chunk);
                getHttpEntity().chunk = null;
                return 27;
            } else {
                if (b == '\r') {
                    // ignore
                    return 14;
                } else if (b == '\n') {
                    setState(25);
                    return state25(null);
                } else {
                    setState(17);
                    return state17(b);
                }
            }
        }
    }

    private int state15(byte b) {
        if (params.segmentedParsing) {
            return 15;
        }
        var chunkSize = getHttpEntity().dataLength;
        if (getHttpEntity().chunk.content == null) {
            buf = Utils.allocateByteArray(chunkSize);
            bufOffset = 0;
            getHttpEntity().chunk.content = ByteArray.from(buf);
        }
        buf[bufOffset++] = b;
        if (bufOffset == buf.length) {
            buf = null; // gc
            return 16;
        }
        return 15;
    }

    private int state16(byte b) throws Exception {
        if (getHttpEntity().chunks == null) {
            getHttpEntity().chunks = new LinkedList<>();
        }
        if (getHttpEntity().chunk != null) {
            getHttpEntity().chunks.add(getHttpEntity().chunk);
            getHttpEntity().chunk = null;
        }

        if (b == '\r') {
            // ignore
            return 16;
        } else if (b == '\n') {
            setState(26);
            return state26(null);
        } else {
            throw new Exception("invalid chunk end: `" + ((char) b) + "`");
        }
    }

    private int state17(byte b) throws Exception {
        if (trailer == null) {
            trailer = new HeaderBuilder();
        }

        if (b == ':') {
            setState(18);
            return state18(b);
        } else {
            trailer.key.append((char) b);
            return 17;
        }
    }

    private int state18(byte b) throws Exception {
        if (b == ':') {
            return 19;
        } else {
            throw new Exception("invalid trailer: " + header + ", invalid splitter " + (char) b);
        }
    }

    private int state19(byte b) {
        if (b == '\r') {
            // ignore
            return 19;
        } else if (b == '\n') {
            return 20;
        } else {
            if (b != ' ' || trailer.value.length() != 0) { // leading spaces are ignored
                trailer.value.append((char) b);
            }
            return 19;
        }
    }

    private int state20(byte b) throws Exception {
        if (trailers == null) {
            trailers = new LinkedList<>();
        }
        if (trailer != null) {
            assert Logger.lowLevelDebug("received trailer " + trailer);
            trailers.add(trailer);
            trailer = null;
        }

        if (b == '\r') {
            // ignore
            return 20;
        } else if (b == '\n') {
            getHttpEntity().trailers = trailers;
            trailers = null;
            setState(25);
            return state25(null);
        } else {
            setState(17);
            return state17(b);
        }
    }

    // it's for state transferring
    private int state21(@SuppressWarnings("unused") Byte b) {
        return end();
    }

    private int state25(@SuppressWarnings("unused") Byte b) {
        if (params.segmentedParsing) {
            return 25;
        }
        return state25Trans();
    }

    private int state25Trans() {
        setState(21);
        return state21(null);
    }

    private int state26(@SuppressWarnings("unused") Byte b) throws Exception {
        if (params.segmentedParsing) {
            return 26;
        }
        if (b == null)
            return 11;
        setState(11);
        return state11(b);
    }

    private int state27(@SuppressWarnings("unused") Byte b) throws Exception {
        if (params.segmentedParsing) {
            return 27;
        }
        if (b == null)
            return 14;
        setState(14);
        return state14(b);
    }
}
