package com.loadtestgo.script.runner;

import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeFinder;
import com.loadtestgo.script.runner.config.TestConfig;
import com.loadtestgo.script.runner.config.JsonConfigParser;
import com.loadtestgo.util.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import com.loadtestgo.util.log.CustomLogger;
import org.fusesource.jansi.AnsiConsole;
import org.json.JSONException;

/**
 * Run a set of scripts, taking screenshots, saving log info and record HAR data for each.
 */
public class Main {
    private static final double MAX_TIMEOUT_SECONDS = 10 * 1000 * 1000;
    public static String AppName = "PizzaScript";

    private String fileName = null;
    private String outputDir = null;
    private boolean writeJunitXmlFile = false;
    private double timeout = -1;
    private Settings overrideSettings = new Settings();

    public static void main(String[] args) {
        Main main = new Main();
        main.instanceMain(args);
    }

    private void processArgs(String[] args) {
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
                    case "junit":
                        writeJunitXmlFile = true;
                        break;
                    case "s":
                    case "set":
                        i++;
                        if (i < args.length) {
                            addSettingOverride(args[i]);
                        } else {
                            printErrorWithHelp("-" + switchName + " requires a parameter");
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
            printErrorWithHelp("Must specify a file, directory or config file to run");
        }
    }

    private void addSettingOverride(String settingsString) {
        Matcher m = IniFile.KeyValuePattern.matcher(settingsString);
        if (m.matches()) {
            String key = m.group(1).trim();
            String value = m.group(2).trim();
            overrideSettings.set(key, value);
        } else {
            printErrorWithHelp(String.format("Override setting \'%s\' must match /%s/\n" +
                "For example this is a valid override: -s \"chrome.logs=true\"",
                settingsString, IniFile.KeyValuePattern.toString()));
        }
    }

    private void printErrorWithHelp(String s) {
        stderr(s);
        stdout();
        printHelp();
        System.exit(1);
    }

    private void printError(String s) {
        stderr(s);
        System.exit(1);
    }

    private void printHelp() {
        printVersion();

        stdout();
        stdout("pizzascript [options] [file]|[directory]");
        stdout();
        stdout("  --help / -h          print this help");
        stdout("  --junit              save output in junit format junit.xml under directory specified by -output");
        stdout("                       defaults to results-<timestamp>/junit.xml");
        stdout("  --output / -o <dir>  output screenshots and other results to this directory");
        stdout("                       output dir can be specified in json file");
        stdout("                       defaults to results-<timestamp>");
        stdout("  --set / -s <setting> override a setting from settings.ini");
        stdout("                       --set / -s can be repeated to override multiple settings");
        stdout("  --timeout / -t <t>   specify a per test timeout in seconds");
        stdout("                       default is no timeout");
        stdout("  --version / -v       print the version number");
        stdout();
        stdout("Run a file:");
        stdout("  pizzascript filename.js");
        stdout();
        stdout("Run all files in a directory (each file is ran as a separate test):");
        stdout("  pizzascript dir");
        stdout();
        stdout("Run all tests specified by json config file (must end with .json):");
        stdout("  pizzascript tests.json");
        stdout();
        stdout("Run all files in a directory with a timeout of 7.5 secs per test:");
        stdout("  pizzascript dir -t 7.5");
        stdout();
    }

    private void printVersion() {
        stdout(String.format("%s: %s", AppName, getVersion()));
    }

    private void instanceMain(String[] args) {
        AnsiConsole.systemInstall();

        processArgs(args);

        if (outputDir == null) {
            outputDir = "results-" + outputDirectoryFormatDate(new Date());
        }

        File specifiedFile = new File(fileName);
        if (!specifiedFile.exists()) {
            printError("Unable to find file '" + fileName + "'");
        }

        long defaultTimeoutInMs = 0;
        if (timeout > 0) {
            defaultTimeoutInMs = (long)(timeout * 1000);
        }

        TestConfig testConfig = null;

        if (specifiedFile.isDirectory()) {
            List<File> files = new ArrayList<>();
            File[] listOfFiles = specifiedFile.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".js")) {
                        files.add(file);
                    }
                }
            }

            // Make sure the list of tests are in a consistent order in case there
            // are order dependencies
            Collections.sort(files);

            testConfig = getFilesAsTests(files);
        } else {
            if (specifiedFile.getName().endsWith(".json")) {
                try {
                    testConfig = JsonConfigParser.parseFile(specifiedFile);

                } catch (JSONException e) {
                    printError(String.format("Error parsing config file '%s': %s", specifiedFile.getPath(), e.getMessage()));
                }
            } else {
                List<File> files = new ArrayList<>();
                files.add(specifiedFile);
                testConfig = getFilesAsTests(files);
            }
        }

        // Apply the command line timeout if none specified
        for (RunnerTest test : testConfig.getTests()) {
            if (test.getTimeout() <= 0) {
                test.setTimeout(defaultTimeoutInMs);
            }
        }

        RunnerTestResults runnerTestResults = new RunnerTestResults();
        runnerTestResults.setWriteJUnitXmlFile(writeJunitXmlFile);

        CustomLogger stdoutLogger = new CustomLogger() {
            @Override
            public void info(final String str, final Object... arguments) {
                runnerTestResults.info(str);
            }

            @Override
            public void warn(final String str, final Object... arguments) {
                runnerTestResults.info(str);
            }
        };

        Settings settings = IniFile.loadSettings(stdoutLogger);
        settings.printSettings(stdoutLogger);

        if (overrideSettings.count() > 0) {
            runnerTestResults.info("Applying overrides");
            overrideSettings.printSettings(stdoutLogger);
            settings.putAll(overrideSettings);
        }

        File chromeExecutable = ChromeFinder.findChrome(settings, stdoutLogger);
        if (chromeExecutable == null) {
            System.exit(-1);
        } else {
            runnerTestResults.info(String.format("Using Chrome '%s'", chromeExecutable.getAbsolutePath()));
        }

        runnerTestResults.info("Starting tests");

        Worker worker = new Worker(settings);
        worker.init(outputDir, runnerTestResults, chromeExecutable);

        System.exit(worker.runJobs(testConfig) ? 0 : 1);
    }

    private static TestConfig getFilesAsTests(List<File> files) {
        TestConfig testConfig = new TestConfig();

        for (File file : files) {
            RunnerTest test = new RunnerTest();
            test.setFile(file);
            test.setName(file.getName());
            testConfig.addTest(test);
        }

        return testConfig;
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
    }

    private static void stderr(String message) {
        System.err.println(message);
    }
}

