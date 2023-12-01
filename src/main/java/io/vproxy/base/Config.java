package io.vproxy.base;

import io.vproxy.base.util.Utils;

import java.io.File;

public class Config {
    // the working directory of vproxy
    // null for the default dir
    private static String workingDirectory = null;

    public static String getWorkingDirectory() {
        String workingDirectory = Config.workingDirectory;
        if (workingDirectory == null) {
            workingDirectory = Utils.homefile(".vproxy");
        } else {
            return workingDirectory;
        }
        synchronized (Config.class) {
            if (Config.workingDirectory != null) {
                return Config.workingDirectory;
            }
            File dir = new File(workingDirectory);
            if (dir.exists()) {
                if (!dir.isDirectory()) {
                    throw new RuntimeException(dir + " exists but is not a directory");
                }
            } else {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("creating vproxy dir " + dir + " failed");
                }
            }
            Config.workingDirectory = workingDirectory;
        }
        return workingDirectory;
    }

    public static String workingDirectoryFile(String name) {
        return getWorkingDirectory() + File.separator + name;
    }
}
