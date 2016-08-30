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
        stderr(s);
        printHelp();
        System.exit(1);
    }

    private static void printError(String s) {
        stderr(s);
        System.exit(1);
    }

    private static void printHelp() {
        printVersion();

        stdout();
        stdout("script-runner [options] [file]|[directory]");
        stdout();
        stdout("  -help / -h           print this help");
        stdout("  -version / -v        print the version number");
        stdout("  -timeout / -t        specify the timeout in seconds (resolution ms)");
        stdout("  -output / -o <dir>   output screenshots and other results to this directory");
        stdout("                       output dir can be specified in json file");
        stdout("                       defaults to results-<timestamp>");
        stdout();
        stdout("Run a file:");
        stdout("  script-runner filename.js");
        stdout();
        stdout("Run all files in a directory (each file is ran as a separate test):");
        stdout("  script-runner dir");
        stdout();
        stdout("Run all tests specified by json config file:");
        stdout("  script-runner tests.json");
        stdout();
        stdout("Run all files in a directory with a timeout of 7.5 secs per test:");
        stdout("  script-runner dir -t 7.5");
        stdout();
    }

    private static void printVersion() {
        stdout(String.format("%s: %s", AppName, getVersion()));
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        processArgs(args);

        if (outputDir == null) {
            outputDir = "results-" + outputDirectoryFormatDate(new Date());
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

    private static String outputDirectoryFormatDate(Date date) {
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

    private static void stdout() {
        stdout("");
    }

    private static void stdout(String message) {
        System.out.println(message);
        org.pmw.tinylog.Logger.info(message);
    }

    private static void stderr(String message) {
        System.err.println(message);
        org.pmw.tinylog.Logger.error(message);
    }
}

