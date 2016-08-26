package com.loadtestgo.script.runner;

import com.loadtestgo.util.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fusesource.jansi.AnsiConsole;

/**
 * Run a set of scripts, taking screenshots, saving log info and record HAR data for each.
 */
public class Main {
    private static final double MAX_TIMEOUT_SECONDS = 10 * 1000 * 1000;
    public static String AppName = "PizzaScript Test Runner";

    static String fileName = null;
    static String outputDir = null;
    static double timeout = -1;

    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                String switchName = Args.stripLeadingDashes(arg);
                switch(switchName) {
                    case "v":
                    case "version":
                        printVersion();
                        System.exit(0);
                        break;
                    case "h":
                    case "help":
                        printHelp();
                        System.exit(0);
                        break;
                    case "o":
                    case "output":
                        i++;
                        if (i < args.length) {
                            outputDir = args[i];
                        } else {
                            printErrorWithHelp("-" + switchName + " requires a directory parameter");
                        }
                        break;
                    case "t":
                    case "timeout":
                        i++;
                        if (i < args.length) {
                            String t = args[i];
                            try {
                                timeout = Double.parseDouble(t);
                                if (timeout < 0) {
                                    printErrorWithHelp("Timeout must be greater than zero");
                                } else if (timeout > MAX_TIMEOUT_SECONDS) {
                                    printErrorWithHelp("Timeout must be less than " + MAX_TIMEOUT_SECONDS);
                                }
                            } catch (NumberFormatException e) {
                                printErrorWithHelp("Timeout " + t + " not a valid number");
                            }
                        } else {
                            printErrorWithHelp("-" + switchName + " requires a timeout parameter");
                        }
                        break;
                    default:
                        printErrorWithHelp("Unknown option " + switchName);
                        break;
                }
            } else {
                if (fileName != null) {
                    printErrorWithHelp("Only one file name can be specified.");
                }
                fileName = arg;
            }
        }
        if (fileName == null) {
            printErrorWithHelp("Must specify a file | directory | config file to run");
        }
    }

    private static void printErrorWithHelp(String s) {
        MainLog.logError(s);
        printHelp();
        System.exit(1);
    }

    private static void printError(String s) {
        MainLog.logError(s);
        System.exit(1);
    }

    private static void printHelp() {
        printVersion();

        MainLog.logInfo();
        MainLog.logInfo("script-runner [options] [file]|[directory]");
        MainLog.logInfo();
        MainLog.logInfo("  -help / -h           print this help");
        MainLog.logInfo("  -version / -v        print the version number");
        MainLog.logInfo("  -timeout / -t        specify the timeout in seconds (resolution ms)");
        MainLog.logInfo("  -output / -o <dir>   output screenshots and other results to this directory");
        MainLog.logInfo("                       output dir can be specified in json file");
        MainLog.logInfo("                       defaults to results-<timestamp>");
        MainLog.logInfo();
        MainLog.logInfo("Run a file:");
        MainLog.logInfo("  script-runner filename.js");
        MainLog.logInfo();
        MainLog.logInfo("Run all files in a directory (each file is ran as a separate test):");
        MainLog.logInfo("  script-runner dir");
        MainLog.logInfo();
        MainLog.logInfo("Run all tests specified by json config file:");
        MainLog.logInfo("  script-runner tests.json");
        MainLog.logInfo();
        MainLog.logInfo("Run all files in a directory with a timeout of 7.5 secs per test:");
        MainLog.logInfo("  script-runner dir -t 7.5");
        MainLog.logInfo();
    }

    private static void printVersion() {
        MainLog.logInfo(String.format("%s: %s", AppName, getVersion()));
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        processArgs(args);

        if (outputDir == null) {
            outputDir = "results-" + formatDate(new Date());
        }

        Worker worker = new Worker();
        RunnerTestResults runnerTestResults = new RunnerTestResults();
        worker.init(outputDir, runnerTestResults);

        File specifiedFile = new File(fileName);
        if (!specifiedFile.exists()) {
            printError("Unable to find file '" + fileName + "'");
        }

        List<File> files = new ArrayList<>();

        if (specifiedFile.isDirectory()) {
            File[] listOfFiles = specifiedFile.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".js")) {
                        files.add(file);
                    }
                }
            }
        } else {
            files.add(specifiedFile);
        }

        long timeoutInMs = 0;
        if (timeout > 0) {
            timeoutInMs = (long)(timeout * 1000);
        }

        System.exit(worker.runJobs(files, timeoutInMs) ? 0 : 1);
    }

    private static String formatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd-HH.mm.ss");
        return simpleDateFormat.format(date);
    }

    public static String getVersion() {
        Package thisPackage = Main.class.getPackage();
        String version = thisPackage.getImplementationVersion();
        if (version == null) {
            version = "dev";
        }
        return version;
    }
}

