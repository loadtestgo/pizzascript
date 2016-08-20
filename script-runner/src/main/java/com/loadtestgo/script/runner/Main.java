package com.loadtestgo.script.runner;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.*;
import com.loadtestgo.script.har.HarWriter;
import com.loadtestgo.util.FileUtils;
import com.loadtestgo.util.Os;
import com.loadtestgo.util.Settings;
import com.loadtestgo.util.StringUtils;
import jline.console.ConsoleReader;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    public static String AppName = "PizzaScript Test Runner";

    static boolean saveHar = false;
    static String fileName = null;
    static String outputDir = null;

    private static void processArgs(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                String switchName = stripLeadingDashes(arg);
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
                            printError("-" + switchName + " requires a directory parameter");
                            System.exit(1);
                        }
                        break;
                    default:
                        printError("Unknown option " + switchName);
                        System.exit(1);
                        break;
                }
            } else {
                if (fileName != null) {
                    printError("Only one file name can be specified.");
                    System.exit(1);
                }
                fileName = arg;
            }
        }
    }

    private static void printError(String s) {
        System.out.println(s);

        printHelp();
    }

    private static void printHelp() {
        printVersion();

        System.out.println();
        System.out.println("script-runner [options] [file]|[directory]");
        System.out.println();
        System.out.println("  -help / -h           print this help");
        System.out.println("  -version / -v        print the version number");
        System.out.println("  -output / -o <dir>   output screenshots and other results to this directory");
        System.out.println("                       output dir can be specified in json file");
        System.out.println("                       defaults to results-<timestamp>");
        System.out.println();
        System.out.println("Run a file:");
        System.out.println("  script-runner filename.js");
        System.out.println();
        System.out.println("Run all files in a directory (each file is ran as a separate test):");
        System.out.println("  script-runner dir");
        System.out.println();
        System.out.println("Run tests all tests specified by json config file:");
        System.out.println("  script-runner tests.json");
        System.out.println();
    }

    private static void printVersion() {
        System.out.println(String.format("%s: %s", AppName, getVersion()));
    }

    private static String stripLeadingDashes(String arg) {
        if (arg == null) {
            return null;
        }

        if (arg.length() == 0) {
            return arg;
        }

        if (arg.charAt(0) == '-') {
            int pos = 1;
            if (arg.length() >= 2) {
                if (arg.charAt(1) == '-') {
                    pos = 2;
                }
            }
            return arg.substring(pos);
        }

        return arg;
    }

    public static void main(String[] args) {
        processArgs(args);

        // Make sure the settings are loaded from the current directory
        // before before a Swing GUI dialog changes it
        Settings.loadSettings();

        boolean success = true;

        EasyTestContext testContext = new EasyTestContext();
        JavaScriptEngine engine = new JavaScriptEngine();
        engine.init(testContext);

        if (fileName != null) {
            success = processFile(fileName, engine);
        }

        engine.finish();

        if (saveHar) {
            TestResult testResult = testContext.getTestResult();
            String harFile = "results.har";
            if (StringUtils.isSet(fileName)) {
                harFile = fileName += ".har";
            }
            try {
                System.out.println(String.format("Saving HAR file %s...", harFile));
                HarWriter.save(testResult, harFile);
            } catch (IOException e) {
                System.out.println(String.format("Unable to save har file: %s", e.getMessage()));
            }
        }

        System.exit(success ? 0 : 1);
    }

    private static boolean processFile(String filename, JavaScriptEngine engine) {
        try {
            File scriptFile  = new File(filename);
            if (!scriptFile.exists()) {
                throw new FileNotFoundException("Unable to find file '" + filename + "'");
            }
            String scriptContexts = FileUtils.readAllText(filename);
            if (scriptContexts == null) {
                throw new IOException("Error reading '" + filename + "'");
            }
            Object result = engine.runScript(scriptContexts, filename);
            if (result != null) {
                System.out.println(engine.valueToString(result));
            }
            return true;
        } catch (IOException|ScriptException e) {
            System.err.println(e.getMessage());
            return false;
        }
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

