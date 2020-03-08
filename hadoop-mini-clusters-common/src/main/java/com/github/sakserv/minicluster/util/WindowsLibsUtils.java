package com.github.sakserv.minicluster.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsLibsUtils {

    // Logger
    private static final Logger LOG = LoggerFactory.getLogger(WindowsLibsUtils.class);

    public static void setHadoopHome() {
        // Set hadoop.home.dir to point to the Window's lib dir
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                String windowsLibDir = getHadoopHome();
                LOG.info("WINDOWS: Setting hadoop.home.dir: {}", windowsLibDir);
                System.setProperty("hadoop.home.dir", windowsLibDir);                //String lib = "";

                System.load(new File(windowsLibDir + Path.SEPARATOR + "lib" + Path.SEPARATOR + "hadoop.dll").getCanonicalPath());
                System.load(new File(windowsLibDir + Path.SEPARATOR + "lib" + Path.SEPARATOR + "hdfs.dll").getCanonicalPath());

                addDir(new File(windowsLibDir + Path.SEPARATOR + "lib").getCanonicalPath());
            } catch (IOException e) {
                LOG.error("WINDOWS: ERROR: setHadoopHome ");
            }
        }
    }

    public static String getHadoopHome() throws IOException {
        if (System.getProperty("HADOOP_HOME") != null) {
            LOG.info("HADOOP_HOME: " + System.getProperty("HADOOP_HOME"));
            return System.getProperty("HADOOP_HOME");
        } else if (System.getenv("HADOOP_HOME") != null) { //takes the hadoop home from system environment variable
            LOG.info("HADOOP_HOME: " + System.getenv("HADOOP_HOME"));
            return System.getenv("HADOOP_HOME");
        } else {

            File windowsLibDir = new File("." + Path.SEPARATOR + "windows_libs" + Path.SEPARATOR + System.getProperty("hdp.release.version"));

            if (!windowsLibDir.exists()) {
                windowsLibDir = new File(".." + Path.SEPARATOR + windowsLibDir);
                if (!windowsLibDir.exists()) {
                    LOG.error("WINDOWS: ERROR: Could not find windows native libs");
                }
            }
            return windowsLibDir.getCanonicalPath();
        }
    }

    /**
     * This enables the java.library.path to be modified at runtime
     * From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
     */
    public static void addDir(String s) throws IOException {
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (String path : paths) {
                if (s.equals(path)) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);
            System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get field handle to set library path");
        }
    }

}
