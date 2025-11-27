package org.firstinspires.ftc.teamcode.util;

import com.acmerobotics.dashboard.config.Config;
import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DataLogger {
    private PrintWriter writer;
    private final String fileName;
    private int maxFileCount = 10;
    private static final int BUFFER_SIZE = 65536; // 64KB
    private long startTime;
    private final StringBuilder lineBuilder = new StringBuilder();

    public DataLogger(String fileName) {
        this.fileName = fileName;
    }

    public DataLogger(String fileName, int maxFileCount) {
        this.fileName = fileName;
        this.maxFileCount = maxFileCount;
    }

    public void startLogging(String... headers) {
        if (!LoggerConfig.LogToSdCard) {
            return;
        }

        try {
            // Saves to /sdcard/FIRST/data/logs/
            File directory = new File(Environment.getExternalStorageDirectory().getPath() + "/FIRST/data/logs");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Clean up old files if we exceed the limit
            File[] files = directory.listFiles((dir, name) -> name.endsWith(".csv"));
            if (files != null && files.length >= maxFileCount) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                // Delete oldest files until we have space for the new one
                for (int i = 0; i <= files.length - maxFileCount; i++) {
                    files[i].delete();
                }
            }

            File file = new File(directory, fileName + ".csv");
            
            /*
             * Performance Note:
             * A large 64KB buffer (BUFFER_SIZE) is used to minimize the impact on the main robot loop.
             * 
             * 1. Buffering: The `log()` method writes to this RAM buffer, which takes nanoseconds.
             * 2. Flushing: The buffer only writes to the OS roughly once every 8-9 seconds (at 50Hz loop).
             * 3. OS Handling: When the buffer flushes, it hands data to the Android/Linux kernel cache 
             *    almost instantly (< 10us). The OS handles the slow write to physical flash storage 
             *    asynchronously in the background.
             * 
             * This ensures the main thread is never blocked by slow SD card I/O.
             */
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file), BUFFER_SIZE));
            
            startTime = System.nanoTime();
            
            // Write CSV Header
            StringBuilder headerLine = new StringBuilder("# Timestamp");
            for (String header : headers) {
                headerLine.append(",").append(header);
            }
            writer.println(headerLine.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            writer = null;
        }
    }

    public void log(double... values) {
        if (writer != null) {
            lineBuilder.setLength(0);
            // Use nanoTime for high-precision physics calculations
            lineBuilder.append((System.nanoTime() - startTime) / 1.0E9); 
            for (double value : values) {
                lineBuilder.append(",").append(value);
            }
            writer.println(lineBuilder.toString());
        }
    }

    public void logComment(String message) {
        if (writer != null) {
            double timestamp = (System.nanoTime() - startTime) / 1.0E9;
            writer.println("# " + String.format(Locale.US, "%.3f", timestamp) + " " + message);
        }
    }

    public void close() {
        if (writer != null) {
            // PrintWriter and BufferedWriter flush automatically on close.
            writer.close();
            writer = null;
        }
    }

    public static String getLogFileName(String opModeName, String logSuffix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = sdf.format(new Date());
        return timestamp + "_" + opModeName + "_" + logSuffix;
    }

    @Config
    public static class LoggerConfig {
        public static boolean LogToSdCard = false;
    }
}
