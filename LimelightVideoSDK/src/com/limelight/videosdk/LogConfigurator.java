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

import org.apache.log4j.Layout;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.LogLog;

/**
 * This class configures the logger like resetting  the configuration, 
 * adding appender to the logger, configuring file appender 
 * which includes maximum number of backed up log files,maximum size
 * of log file until rolling and logging format
 */
class LogConfigurator {
    private Level rootLevel = Level.DEBUG;
    private String filePattern = "%d - %p - %c - %t - %m%n";
    private String fileName;
    private int maxBackupSize = 5;
    private long maxFileSize = 512 * 1024;
    private boolean immediateFlush = true;
    private boolean useFileAppender = true;
    private boolean resetConfiguration = true;
    private boolean internalDebugging = false;

    public LogConfigurator() {
        // TODO
    }

    Level getLevel(final String loggerName){
        return Logger.getLogger(loggerName).getLevel();
//        return Level.DEBUG;
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
    LogConfigurator(final String fileName, final Level rootLevel,
            final String filePattern) {
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
     * Sets the level of logger with name <code>loggerName</code>. Corresponds
     * to log4j.properties <code>log4j.logger.org.apache.what.ever=ERROR</code>
     * 
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
        final RollingFileAppender rollingFileAppender;
        final Layout fileLayout = new PatternLayout(getFilePattern());

        try {
            rollingFileAppender = new RollingFileAppender(fileLayout,
                    getFileName());
        } catch (Exception e) {
            throw new RuntimeException("Exception configuring log system", e);
        }

        rollingFileAppender.setMaxBackupIndex(getMaxBackupSize());
        rollingFileAppender.setMaximumFileSize(getMaxFileSize());
        rollingFileAppender.setImmediateFlush(isImmediateFlush());

        final Logger root = Logger.getRootLogger();
        root.addAppender(rollingFileAppender);
    }

    /**
     * Return the log level of the root logger
     * 
     * @return Log level of the root logger
     */
    Level getRootLevel() {
        return rootLevel;
    }

    /**
     * Sets log level for the root logger
     * 
     * @param level
     *            Log level for the root logger
     */
    void setRootLevel(final Level level) {
        this.rootLevel = level;
    }

    String getFilePattern() {
        return filePattern;
    }

    void setFilePattern(final String filePattern) {
        this.filePattern = filePattern;
    }

    /**
     * Returns the name of the log file
     * 
     * @return the name of the log file
     */
    String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the log file
     * 
     * @param fileName
     *            Name of the log file
     */
    void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the maximum number of backed up log files
     * 
     * @return Maximum number of backed up log files
     */
    int getMaxBackupSize() {
        return maxBackupSize;
    }

    /**
     * Sets the maximum number of backed up log files
     * 
     * @param maxBackupSize
     *            Maximum number of backed up log files
     */
    void setMaxBackupSize(final int maxBackupSize) {
        this.maxBackupSize = maxBackupSize;
    }

    /**
     * Returns the maximum size of log file until rolling
     * 
     * @return Maximum size of log file until rolling
     */
    long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets the maximum size of log file until rolling
     * 
     * @param maxFileSize
     *            Maximum size of log file until rolling
     */
    void setMaxFileSize(final long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    boolean isImmediateFlush() {
        return immediateFlush;
    }

    void setImmediateFlush(final boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    /**
     * Returns true, if FileAppender is used for logging
     * 
     * @return True, if FileAppender is used for logging
     */
    boolean isUseFileAppender() {
        return useFileAppender;
    }

    /**
     * @param useFileAppender
     *            the useFileAppender to set
     */
    void setUseFileAppender(final boolean useFileAppender) {
        this.useFileAppender = useFileAppender;
    }

    void setResetConfiguration(final boolean resetConfiguration) {
        this.resetConfiguration = resetConfiguration;
    }

    /**
     * Resets the log4j configuration before applying this configuration.
     * Default is true.
     * 
     * @return True, if the log4j configuration should be reset before applying
     *         this configuration.
     */
    boolean isResetConfiguration() {
        return resetConfiguration;
    }

    void setInternalDebugging(final boolean internalDebugging) {
        this.internalDebugging = internalDebugging;
    }

    boolean isInternalDebugging() {
        return internalDebugging;
    }
}
