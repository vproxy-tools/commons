package io.vproxy.base.util;

import io.vproxy.vfd.IP;
import io.vproxy.vfd.IPv4;
import io.vproxy.vfd.IPv6;

import java.util.Objects;

public abstract class Network {
    protected final IP ip;
    protected final ByteArray mask;
    protected final int maskNumber;

    protected Network(IP ip, ByteArray mask) {
        this.ip = ip;
        this.mask = mask;
        this.maskNumber = maskInt(mask.toJavaArray());
    }

    public static Network from(byte[] ip, byte[] mask) {
        return from(IP.from(ip), ByteArray.from(mask));
    }

    public static Network from(IP ip, ByteArray mask) {
        if (ip instanceof IPv4) {
            return new NetworkV4((IPv4) ip, mask);
        } else if (ip instanceof IPv6) {
            return new NetworkV6((IPv6) ip, mask);
        } else {
            throw new IllegalArgumentException("ip is not ipv4 nor ipv6: " + ip);
        }
    }

    public static Network from(String net) {
        if (!validNetworkStr(net)) {
            throw new IllegalArgumentException("not valid network " + net);
        }
        String ip = net.substring(0, net.lastIndexOf("/"));
        int mask = Integer.parseInt(net.substring(net.indexOf("/") + 1));

        return from(IP.from(ip), ByteArray.from(parseMask(mask)));
    }

    abstract public boolean contains(IP address);

    public boolean contains(Network that) {
        if (!contains(that.ip)) {
            return false;
        }
        return getMask() < that.getMask();
    }

    public IP getIp() {
        return ip;
    }

    public int getMask() {
        return maskNumber;
    }

    public byte[] getRawIpBytes() {
        return ip.bytes.toJavaArray();
    }

    public byte[] getRawMaskBytes() {
        return mask.toJavaArray();
    }

    public ByteArray getRawMaskByteArray() {
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return Objects.equals(ip, network.ip) &&
            Objects.equals(mask, network.mask);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(ip);
        result = 31 * result + Objects.hashCode(mask);
        return result;
    }

    @Override
    public String toString() {
        return ip.formatToIPString() + "/" + getMask();
    }

    // BEGIN UTILS:

    public static boolean validNetworkStr(String net) {
        if (!net.contains("/")) {
            return false;
        }
        String[] arrs = net.split("/");
        if (arrs.length != 2) {
            return false;
        }
        int intMask;
        try {
            intMask = Integer.parseInt(arrs[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        String nStr = arrs[0];
        byte[] nBytes = IP.parseIpString(nStr);
        if (nBytes == null) {
            return false;
        }
        byte[] maskBytes;
        try {
            maskBytes = parseMask(intMask);
        } catch (Exception e) {
            return false;
        }
        return validNetwork(nBytes, maskBytes);
    }

    public static byte[] parseMask(int mask) {
        if (mask > 128) { // mask should not greater than 128
            throw new IllegalArgumentException("unknown mask " + mask);
        } else if (mask > 32) {
            // ipv6
            return getMask(Utils.allocateByteArrayInitZero(16), mask);
        } else {
            // ipv4
            return getMask(Utils.allocateByteArrayInitZero(4), mask);
        }
        // maybe the mask <= 32 but is for ipv6
        // in this case, we handle it as an ipv4 mask
        // and do some additional work when
        // checking and comparing
    }

    // fill bytes into the `masks` array
    private static byte[] getMask(byte[] masks, int mask) {
        // because java always fill the byte with 0
        // so we only need to set 1 into the bit sequence
        // start from the first bit
        for (int i = 0; i < masks.length; ++i) {
            masks[i] = Utils.genPrefixByte(mask /* it's ok if mask < 0 or > 8 */);
            // the `to-do` bit sequence moves 8 bits forward each round
            // so subtract 8 from the integer represented mask
            mask -= 8;
        }
        return masks;
    }

    public static int maskInt(byte[] mask) {
        // run from end to start and check how many zeros
        int m = 0;
        for (int i = mask.length - 1; i >= 0; --i) {
            int cnt = Utils.zeros(mask[i]);
            if (cnt == 0)
                break;
            m += cnt;
        }
        return mask.length * 8 - m;
    }

    public static Network eraseToNetwork(IP ip, int mask) {
        byte[] ipbytes;
        byte[] maskbytes;
        if (ip instanceof IPv4) {
            ipbytes = ip.bytes.toNewJavaArray();
            maskbytes = new byte[4];
        } else {
            ipbytes = ip.bytes.toNewJavaArray();
            maskbytes = new byte[16];
        }
        getMask(maskbytes, mask);
        eraseToNetwork(ipbytes, maskbytes);
        return Network.from(ipbytes, maskbytes);
    }

    public static void eraseToNetwork(byte[] addr, byte[] mask) {
        if (addr.length < mask.length)
            throw new IllegalArgumentException();
        // first few bytes set to bitwise-and
        for (int i = 0; i < mask.length; ++i) {
            byte a = addr[i];
            byte m = mask[i];
            addr[i] = (byte) (a & m);
        }
        // last few bytes are all zero
        // because mask is zero
        for (int i = mask.length; i < addr.length; ++i) {
            addr[i] = 0;
        }
    }

    public static boolean validNetwork(byte[] addr, byte[] mask) {
        if (addr.length < mask.length)
            return false; // ipv4 and mask > 32, absolutely wrong
        // only check first few bytes in the loop
        for (int i = 0; i < mask.length; ++i) {
            byte a = addr[i];
            byte m = mask[i];
            if ((a & m) != a)
                return false;
        }
        // check whether last few bytes are all zero
        // because mask is zero
        for (int i = mask.length; i < addr.length; ++i) {
            byte a = addr[i];
            if (a != 0)
                return false;
        }
        return true;
    }

    public static boolean maskMatch(byte[] input, byte[] rule, byte[] mask) {
        // the mask and rule length might not match each other
        // see comments in parseMask()
        // and input length might not be the same as the rule
        // because we might apply an ipv4 rule to an ipv6 lb

        // let's consider all situations:
        // 1) input.length == rule.length > mask.length
        //    which means ipv6 input and ipv6 rule and mask <= 32
        //    so we check the first 4 bytes in the sequence
        // 2) input.length < rule.length > mask.length
        //    which means ipv4 input and ipv6 rule and mask <= 32
        //    in this case, all highest 32 bits of real mask are 0
        //    and the requester's ip address cannot be 0.0.0.0
        //    so returning `not matching` would be ok
        // 3) input.length < rule.length == mask.length
        //    which means ipv4 input and ipv6 rule and mask > 32
        //    the low bits are 0 for ipv4
        //    so if rule low bits [all 0] or [all 0][ffff], then check high bits
        //    otherwise directly returning `not matching` would be ok
        // 4) input.length > rule.length == mask.length
        //    which means ipv6 input and ipv4 rule
        //    so let's only compare the last 32 bits
        //    additionally:
        //    there might be deprecated `IPv4-Compatible IPv6 address` e.g.:
        //                                  127.0.0.1
        //    0000:0000:0000:0000:0000:0000:7f00:0001
        //    and there might be `IPv4-Mapped IPv6 address` e.g.:
        //                                  127.0.0.1
        //    0000:0000:0000:0000:0000:ffff:7f00:0001
        //    so let's then check whether the first few bits
        //    like this [all 0][ffff]
        // 5) input.length == rule.length == mask.length
        //    which means ipv4 input and ipv4 rule and mask <= 32
        //    or ipv6 input and ipv6 input and mask > 32
        //    just do normal check
        //    see implementation for detail

        if (input.length == rule.length && rule.length > mask.length) {
            // 1
            for (int i = 0; i < mask.length; ++i) {
                byte inputB = input[i];
                byte ruleB = rule[i];
                byte maskB = mask[i];
                if ((inputB & maskB) != ruleB)
                    return false;
            }
            return true;
        } else if (input.length < rule.length && rule.length > mask.length) {
            // 2
            return false;
        } else if (input.length < rule.length && rule.length == mask.length) {
            // 3
            // input =            [.......]
            //  rule = [..................]
            int lastLowIdx = rule.length - input.length - 1;
            int secondLastLowIdx = lastLowIdx - 1;
            // high part
            for (int i = 0; i < input.length; ++i) {
                byte inputB = input[i];
                byte ruleB = rule[i + rule.length - input.length];
                byte maskB = mask[i + rule.length - input.length];
                if ((inputB & maskB) != ruleB)
                    return false;
            }
            return Utils.lowBitsV6V4(rule, lastLowIdx, secondLastLowIdx);
        }
        // else:
        // for (4) and (5)

        int minLen = input.length;
        if (rule.length < minLen)
            minLen = rule.length;
        if (mask.length < minLen)
            minLen = mask.length;

        for (int i = 0; i < minLen; ++i) {
            byte inputB = input[input.length - i - 1];
            byte ruleB = rule[rule.length - i - 1];
            byte maskB = mask[mask.length - i - 1];

            if ((inputB & maskB) != ruleB)
                return false;
        }

        // then check for additional rules in (4)
        if (input.length > rule.length) {
            // input = [..................]
            //  rule =            [.......]
            int lastLowIdx = input.length - rule.length - 1;
            int secondLastLowIdx = lastLowIdx - 1;
            return Utils.lowBitsV6V4(input, lastLowIdx, secondLastLowIdx);
        }

        return true;
    }
}
