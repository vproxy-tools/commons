package io.vproxy.vpacket;

import io.vproxy.base.util.ByteArray;
import io.vproxy.base.util.Utils;

import java.util.Objects;

public class VXLanPacket extends AbstractPacket {
    private int flags = 0b00001000; // this flag almost never changes
    private int reserved1 = 0;
    private int vni;
    private int reserved2 = 0;
    private EthernetPacket packet;

    @Override
    public String from(PacketDataBuffer raw) {
        return from(raw, false);
    }

    public String from(PacketDataBuffer raw, boolean allowPartial) {
        ByteArray bytes = raw.pktBuf;
        if (bytes.length() < 1 + 3 + 3 + 1) {
            return "input packet length too short for a vxlan packet";
        }
        flags = bytes.uint8(0);
        reserved1 = bytes.uint24(1);
        vni = bytes.uint24(4);
        reserved2 = bytes.uint8(7);
        // for now, we only consider it being this type of ethernet packet
        packet = new EthernetPacket();
        packet.recordParent(this);
        String err = packet.from(raw.sub(8), allowPartial);
        if (err != null) {
            return err;
        }

        this.raw = raw;
        return null;
    }

    @Override
    protected ByteArray buildPacket(int flags) {
        return ByteArray.allocate(8)
            .set(0, (byte) this.flags)
            .int24(1, reserved1)
            .int24(4, vni)
            .set(7, (byte) reserved2)
            .concat(packet.getRawPacket(flags));
    }

    @Override
    protected void __updateChecksum() {
        __updateChildrenChecksum();
    }

    @Override
    protected void __updateChildrenChecksum() {
        packet.updateChecksum();
    }

    @Override
    public VXLanPacket copy() {
        var ret = new VXLanPacket();
        ret.flags = flags;
        ret.reserved1 = reserved1;
        ret.vni = vni;
        ret.reserved2 = reserved2;
        ret.packet = packet.copy();
        ret.packet.recordParent(ret);
        return ret;
    }

    @Override
    public void clearAllRawPackets() {
        clearRawPacket();
        packet.clearAllRawPackets();
    }

    @Override
    public String description() {
        return "vxlan"
            + ",vni=" + vni
            + "," + packet.description();
    }

    @Override
    public String toString() {
        return "VXLanPacket{" +
            "flags=" + Utils.toBinaryString(flags) +
            ", reserved1=" + reserved1 +
            ", vni=" + Utils.runAvoidNull(() -> vni, "null") +
            ", reserved2=" + reserved2 +
            ", packet=" + packet +
            '}';
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        clearRawPacket();
        this.flags = flags;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        clearRawPacket();
        this.reserved1 = reserved1;
    }

    public int getVni() {
        return vni;
    }

    public void setVni(int vni) {
        clearRawPacket();
        this.vni = vni;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        clearRawPacket();
        this.reserved2 = reserved2;
    }

    public EthernetPacket getPacket() {
        return packet;
    }

    public void setPacket(EthernetPacket packet) {
        clearRawPacket();
        this.packet = packet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VXLanPacket that = (VXLanPacket) o;
        return flags == that.flags &&
            vni == that.vni &&
            Objects.equals(packet, that.packet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, vni, packet);
    }
}
