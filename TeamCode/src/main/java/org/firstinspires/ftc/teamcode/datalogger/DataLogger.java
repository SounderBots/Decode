package org.firstinspires.ftc.teamcode.datalogger;

import android.content.Context;
import android.os.Environment;

import com.acmerobotics.dashboard.config.Config;
import com.bylazar.configurables.annotations.Configurable;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Config
@Configurable
public class DataLogger {
    private PrintWriter writer;
    private final String fileName;
    private int maxFileCount = 10;
    private static final int BUFFER_SIZE = 65536; // 64KB
    private long startTime;
    private final StringBuilder lineBuilder = new StringBuilder();

    public static boolean LogToSdCard = true;
    
    // "AUTO" = Try external SD first, fallback to internal
    // "/sdcard" = Force internal
    // "/storage/XXXX-XXXX" = Force specific external
    public static String StoragePath = "AUTO";

    public DataLogger(String fileName) {
        this.fileName = fileName;
    }

    public DataLogger(String fileName, int maxFileCount) {
        this.fileName = fileName;
        this.maxFileCount = maxFileCount;
    }

    public void initializeLogging(String... headers) {
        if (!LogToSdCard) {
            return;
        }

        try {
            File baseDir = null;
            
            // Handle AUTO mode or explicit path
            if (StoragePath.equals("AUTO") || StoragePath.equals("/storage")) {
                baseDir = findExternalSdCard();
                if (baseDir == null) {
                    System.out.println("DataLogger: No external SD card found, falling back to internal storage");
                    baseDir = new File(Environment.getExternalStorageDirectory(), "FIRST/data/logs");
                }
            } else {
                // User specified a custom path (e.g. "/sdcard" or specific UUID)
                if (StoragePath.equals("/sdcard")) {
                    baseDir = new File(Environment.getExternalStorageDirectory(), "FIRST/data/logs");
                } else {
                    baseDir = new File(StoragePath + "/FIRST/data/logs");
                }
            }
            
            if (!baseDir.exists()) {
                if (!baseDir.mkdirs()) {
                    System.out.println("DataLogger ERROR: Failed to create directory " + baseDir.getAbsolutePath());
                }
            }
            File directory = baseDir;
            System.out.println("DataLogger: Logging to " + directory.getAbsolutePath());

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
            System.out.println("DataLogger: Creating file " + file.getAbsolutePath());
            
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
            System.out.println("DataLogger: File created successfully");
            
            startTime = System.nanoTime();
            
            // Write CSV Header
            StringBuilder headerLine = new StringBuilder("# Timestamp");
            for (String header : headers) {
                headerLine.append(",").append(header);
            }
            writer.println(headerLine.toString());
            
        } catch (Exception e) {
            System.out.println("DataLogger ERROR: " + e.getMessage());
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
            writer.flush();
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

    /**
     * Finds the external SD card mount point on Control Hub using Android APIs.
     * Returns the App-Specific logs directory on the external SD card, or null if not found.
     */
    private static File findExternalSdCard() {
        try {
            Context context = AppUtil.getDefContext();
            // getExternalFilesDirs returns all shared/external storage volumes where the app can write.
            // Index 0 is usually the internal emulated storage.
            // Index 1+ are usually external SD cards.
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            
            if (externalFilesDirs != null && externalFilesDirs.length > 1) {
                for (int i = 1; i < externalFilesDirs.length; i++) {
                    File dir = externalFilesDirs[i];
                    if (dir != null) {
                        File logsDir = new File(dir, "logs");
                        if (!logsDir.exists()) {
                            if (!logsDir.mkdirs()) {
                                System.err.println("DataLogger: Failed to create external log dir: " + logsDir.getAbsolutePath());
                                continue;
                            }
                        }
                        System.out.println("DataLogger: Found external SD card path: " + logsDir.getAbsolutePath());
                        return logsDir;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("DataLogger: Error finding external SD card: " + e.getMessage());
        }
        return null;
    }
}