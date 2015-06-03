/* 
   Copyright 2011 Rolf Kulemann, Pascal Bockhorn

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.limelight.videosdk;

import java.io.IOException;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;
import android.util.Log;

/**
 * This class configures the logger like resetting  the configuration, 
 * adding appender to the logger, configuring file appender 
 * which includes maximum number of backed up log files,maximum size
 * of log file until rolling and logging format
 */
class LogConfigurator {
    private Level mRootLevel = Level.DEBUG;
    private String mFilePattern = "%d - %p - %c - %t - %m%n";
    private String mFileName;
    private int mMaxBackupSize = 5;
    private long mMaxFileSize = 512 * 1024;
    private boolean mImmediateFlush = true;
    private boolean mUseFileAppender = true;
    private boolean mReset = true;
    private boolean mInternalDebug;

    public LogConfigurator() {
        //empty constructor.
    }

    /**
     * This method return the current logging level.
     * @param loggerName
     * @return Level
     */
    Level getLevel(final String loggerName){
        return Logger.getLogger(loggerName).getLevel();
    }

    /**
     * @param fileName Name of the log file
     */
    LogConfigurator(final String fileName) {
        setFileName(fileName);
    }

    /**
     * @param fileName
     *            Name of the log file
     * @param rootLevel
     *            Log level for the root logger
     */
    LogConfigurator(final String fileName, final Level rootLevel) {
        this(fileName);
        setRootLevel(rootLevel);
    }

    /**
     * @param fileName
     *            Name of the log file
     * @param rootLevel
     *            Log level for the root logger
     * @param filePattern
     *            Log pattern for the file appender
     */
    LogConfigurator(final String fileName, final Level rootLevel,final String filePattern) {
        this(fileName);
        setRootLevel(rootLevel);
        setFilePattern(filePattern);
    }

    /**
     * @param fileName
     *            Name of the log file
     * @param maxBackupSize
     *            Maximum number of backed up log files
     * @param maxFileSize
     *            Maximum size of log file until rolling
     * @param filePattern
     *            Log pattern for the file appender
     * @param rootLevel
     *            Log level for the root logger
     */
    LogConfigurator(final String fileName, final int maxBackupSize,
            final long maxFileSize, final String filePattern,
            final Level rootLevel) {
        this(fileName, rootLevel, filePattern);
        setMaxBackupSize(maxBackupSize);
        setMaxFileSize(maxFileSize);
    }

    /**
     * Method to configure logging.
     */
    void configure() {
        final Logger root = Logger.getRootLogger();

        if (isResetConfiguration()) {
            LogManager.getLoggerRepository().resetConfiguration();
        }

        LogLog.setInternalDebugging(isInternalDebugging());

        if (isUseFileAppender()) {
            configureFileAppender();
        }

        root.setLevel(getRootLevel());
    }

    /**
     * Sets the level of logger with name <code>loggerName</code>.
     * @param loggerName
     * @param level
     */
    void setLevel(final String loggerName, final Level level) {
        Logger.getLogger(loggerName).setLevel(level);
    }

    /**
     * Method to configure file appender.<br>
     * It includes maximum number of backed up log files,maximum size of log file 
     * until rolling and logging format.
     */
    private void configureFileAppender() {
        RollingFileAppender rollingAppender;
        final Layout fileLayout = new PatternLayout(getFilePattern());

        try {
            rollingAppender = new RollingFileAppender(fileLayout,getFileName());
        } catch (IOException e) {
            Log.e("LogConfigurator", "Exception configuring log system", e);
            return;
        }

        rollingAppender.setMaxBackupIndex(getMaxBackupSize());
        rollingAppender.setMaximumFileSize(getMaxFileSize());
        rollingAppender.setImmediateFlush(isImmediateFlush());

        final Logger root = Logger.getRootLogger();
        root.addAppender(rollingAppender);
    }

    /**
     * Return the log level of the root logger
     * 
     * @return Log level of the root logger
     */
    Level getRootLevel() {
        return mRootLevel;
    }

    /**
     * Sets log level for the root logger
     * 
     * @param level
     *            Log level for the root logger
     */
    void setRootLevel(final Level level) {
        this.mRootLevel = level;
    }

    /**
     * This method returns the Log pattern for the file appender.
     * @return filePattern
     */
    private String getFilePattern() {
        return mFilePattern;
    }

    /**
     * This method sets the Log pattern for the file appender.
     * @param filePattern
     */
    void setFilePattern(final String filePattern) {
        this.mFilePattern = filePattern;
    }

    /**
     * Returns the name of the log file
     * 
     * @return the name of the log file
     */
    String getFileName() {
        return mFileName;
    }

    /**
     * Sets the name of the log file
     * 
     * @param fileName
     *            Name of the log file
     */
    void setFileName(final String fileName) {
        this.mFileName = fileName;
    }

    /**
     * Returns the maximum number of backed up log files
     * 
     * @return Maximum number of backed up log files
     */
    private int getMaxBackupSize() {
        return mMaxBackupSize;
    }

    /**
     * Sets the maximum number of backed up log files
     * 
     * @param maxBackupSize
     *            Maximum number of backed up log files
     */
    void setMaxBackupSize(final int maxBackupSize) {
        this.mMaxBackupSize = maxBackupSize;
    }

    /**
     * Returns the maximum size of log file until rolling
     * 
     * @return Maximum size of log file until rolling
     */
    private long getMaxFileSize() {
        return mMaxFileSize;
    }

    /**
     * Sets the maximum size of log file until rolling
     * 
     * @param maxFileSize
     *            Maximum size of log file until rolling
     */
    void setMaxFileSize(final long maxFileSize) {
        this.mMaxFileSize = maxFileSize;
    }

    /**
     * This method returns the value of immediate flush.
     * @return immediateFlush
     */
    private boolean isImmediateFlush() {
        return mImmediateFlush;
    }

    /**
     * This method sets the value for immediate flush of log file.
     * @param immediateFlush
     */
    void setImmediateFlush(final boolean immediateFlush) {
        this.mImmediateFlush = immediateFlush;
    }

    /**
     * Returns true, if FileAppender is used for logging
     * 
     * @return True, if FileAppender is used for logging
     */
    private boolean isUseFileAppender() {
        return mUseFileAppender;
    }

    /**
     * @param useFileAppender
     *            the useFileAppender to set
     */
    void setUseFileAppender(final boolean useFileAppender) {
        this.mUseFileAppender = useFileAppender;
    }

    /**
     * This method resets the log4j configuration before applying this configuration.
     * Default is true.
     * @param boolean reset
     */
    void setResetConfiguration(final boolean reset) {
        this.mReset = reset;
    }

    /**
     * This method returns true if the log4j configuration should be reset before applying this configuration. 
     * @return resetConfiguration
     */
    private boolean isResetConfiguration() {
        return mReset;
    }

    /**
     * This method sets the internal debugging.
     * @param internalDebugging
     */
    void setInternalDebugging(final boolean internalDebugging) {
        this.mInternalDebug = internalDebugging;
    }

    /**
     * This method returns true if internal debugging is set.
     * @return
     */
    private boolean isInternalDebugging() {
        return mInternalDebug;
    }
}
