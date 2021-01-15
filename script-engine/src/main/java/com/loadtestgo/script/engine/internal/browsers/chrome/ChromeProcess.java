package com.loadtestgo.script.engine.internal.browsers.chrome;

import com.loadtestgo.script.engine.*;
import com.loadtestgo.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;

public class ChromeProcess {
    private UserContext userContext;
    private String profileDir;
    private String baseTmpDir;
    private String cacheDir;
    private String pizzaExtensionPath;
    private String extensionExpandPath;
    private Process process;
    private ChromeSettings settings;
    private ProcessLauncher processLauncher;
    private EngineSettings engineSettings;
    private EngineContext engineContext;

    public ChromeProcess(TestContext testContext) {
        init(testContext, new ChromeSettings());
    }

    public ChromeProcess(TestContext testContext, ChromeSettings settings) {
        init(testContext, settings);
    }

    private void init(TestContext testContext, ChromeSettings settings) {
        this.settings = settings;
        this.userContext = testContext.getUserContext();
        this.baseTmpDir = Path.join(testContext.getTestTmpDir(), "chrome/");

        // The Chrome profile dir, where files are cached and preferences saved
        this.profileDir = Path.join(baseTmpDir, "userdata");

        // Set the cache dir, this is always cleared
        this.cacheDir = Path.join(baseTmpDir, "cache");

        // The directory where we expand our extension
        this.extensionExpandPath = Path.join(baseTmpDir, "extensions");

        // The path to our Pizza extension (our back channel for communicating
        // directly with Chrome).
        this.pizzaExtensionPath = Path.join(extensionExpandPath, "pizza");

        this.engineSettings = testContext.getEngineSettings();
        this.engineContext = testContext.getEngineContext();
    }

    public void start() {
        start(null);
    }

    public void start(ProcessLauncher processLauncher) {
        File executable = engineContext.getChromeExecutable();
        if (executable == null) {
            throw new ScriptException("Unable to find Chrome executable.");
        }

        setupProfile();

        ArrayList<String> args = new ArrayList<>();
        args.add(executable.getAbsolutePath());
        buildArgs(args);

        Logger.info("Launching Chrome: {}", Joiner.join(args, " "));
        try {
            if (processLauncher == null) {
                ProcessBuilder builder = new ProcessBuilder(args);
                process = builder.start();
            } else {
                this.processLauncher = processLauncher;
                processLauncher.startBrowser(args);
            }
        } catch (IOException|InterruptedException e) {
            Logger.error(e);
        }
    }

    private void buildArgs(ArrayList<String> args) {
        // No first run tab
        args.add("--no-first-run");

        // this line crashes chrome for some reason
        // args.add("--silent-launch");

        // Logs also crash Chrome on Windows 10 / OSX at the time of writing.
        // Seems like a bug that could happen on any OS, so disable by default,
        // and print a warning if enabled.
        if (engineSettings.saveChromeLogs()) {
            Logger.warn("Enabling Chrome debug logging to 'userdata/chrome_debug.log'...  Chrome has some threading safety issues beware of lockups...");

            // Enable logging to userdata/chrome_debug.log
            args.add("--enable-logging");

            // Log level for above
            args.add("--v=1");
        }

        if (settings.ignoreCertErrors) {
            args.add("--ignore-certificate-errors");

            // Also needed to add '--test-type' to avoid warnings.
            // Docs say it also does the following (much of which is covered by other command line params below):
            //   - It avoids creating application stubs in ~/Applications on mac.
            //   - It makes exit codes slightly more correct
            //   - Windows navigation jumplists arent updated crbug.com/389375
            //   - Doesn't start some chrome StartPageService
            //   - Disables initializing chromecast service
            //   - "Component extensions with background pages are not enabled during tests because they generate a lot
            //      of background behavior that can interfere."
            //   - When quitting the browser, it disables additional checks that may stop that quitting process. (like
            //     unsaved form modifications or unhandled profile notifications..)
            // NOTE: if you run into issues with some the command line params below no longer working it might be
            // work always turning 'test-type' ON.
            args.add("--test-type");
        }

        if (settings.enableQuic) {
            args.add("--enable-quic");
        }

        if (settings.forceQuicForHost != null) {
            args.add(String.format("--origin-to-force-quic-on=%s", settings.forceQuicForHost));
        }

        if (settings.startMaximized) {
            args.add("--start-maximized");
        }

        // Disable extension updates
        args.add("--extensions-update-frequency=999999");
        args.add("--disable-component-updater");

        // Disable the downloading of the safe browsing data
        args.add("--safebrowsing-disable-auto-update");

        // Don't display a dialog if we are not the default browser
        args.add("--no-default-browser-check");

        // Don't show a dialog when we enable the debugger, Chrome normally shows this dialog
        // when an extension tries to invoke DevTools.
        args.add("--silent-debugger-extension-api");

        // Allow extensions on chrome:// URLS
        // This is disabled since we don't really need to see data about chrome:// tabs
        // loading.  But it can be interesting for the curious i suppose...
        // args.add("--extensions-on-chrome-urls");

        // Disable save password popup
        // The following no longer works with current versions of Chrome 56+
        // Instead we disable this in Preferences.json that we use to create the chrome profile
        args.add("--disable-save-password-bubble");

        if (Os.isLinux()) {
            // Avoid potential instability of using Gnome Keyring or KDE wallet. crbug.com/571003
            args.add("--password-store=basic");
        }

        args.add("--disable-device-discovery-notifications");

        // Load our profile
        args.add(String.format("--user-data-dir=%s", profileDir));

        // Set the cache dir
        args.add(String.format("--disk-cache-dir=%s", cacheDir));

        // Disable chrome apps and extensions
        args.add("--disable-default-apps");
        args.add("--disable-component-update"); // Don't update the browser 'components' listed at chrome://components/

        // Disables Domain Reliability Monitoring, which tracks whether the browser has difficulty contacting
        // Google-owned sites and uploads reports to Google.
        args.add("--disable-domain-reliability");

        // Disable reporting to UMA, but allows for collection
        args.add("--metrics-recording-only");

        // Disable crash reporting
        args.add("--disable-breakpad");

        if (Os.isMac()) {
            // Use mock keychain on Mac to prevent blocking permissions dialogs
            args.add("--use-mock-keychain");
        }

        // Disables client-side phishing detection
        args.add("--disable-client-side-phishing-detection");

        // From the Chrome command line docs:
        //   "Disable default component extensions with background pages - useful for performance
        //    tests where these pages may interfere with perf results."
        args.add("--disable-component-extensions-with-background-pages");

        // Disable google account sync (you'd have to login to google for this to be active)
        args.add("--disable-sync");

        // Disable auto translation (NO LONGER WORKS since April 2017)
        // https://codereview.chromium.org/2819813002/
        args.add("--disable-translate");

        // From the Chrome command line docs:
        //   "Disable several subsystems which run network requests in the background. This is
        //    for use when doing network performance testing to avoid noise in the measurements."
        args.add("--disable-background-networking");

        if (settings.args != null) {
            for (String arg : settings.args) {
                if (arg!= null) {
                    args.add(arg);
                }
            }
        }

        // Load our extension
        args.add(String.format("--load-extension=%s", pizzaExtensionPath));

        // Whitelist the extension so the extension can capture tabs
        args.add(String.format("--whitelisted-extension-id=%s", getExtensionId()));

        args.add("about:blank");
    }

    private void setupProfile() {
        // Setup and clean the Chrome user directory
        cleanProfile();

        // Setup and clean the Chrome cache
        cleanCache();

        if (settings.unpackExtension) {
            extractExtension();
        }

        savePreferences();

        // Looks like we need to write a "First Run" file.  If we don't do this
        // Chrome will delete the default profile we just wrote.
        File firstRun = new File(profileDir, "First Run");
        try {
            firstRun.createNewFile();
        } catch (IOException e) {
            throw new ScriptException(String.format("Unable to write Chrome First Run file:\n  %s", e.getMessage()));
        }
    }

    private void savePreferences() {
        InputStream input = ChromeProcess.class.getResourceAsStream("/chrome/Preferences.json");
        if (input == null) {
            Logger.error("Unable to find internal Chrome preferences template, using default instead");
            return;
        }

        String preferencesFile = Path.join(profileDir, "Default/Preferences");
        File file = new File(preferencesFile);
        file.getParentFile().mkdirs();

        // Merge preferences file
        if (settings.preferences == null) {
            try (FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(input, output);
            } catch (Exception e) {
                Logger.error(e, "Unable to copy Chrome preferences template, using default instead");
            }
        } else {
            try {
                List<String> lines = IOUtils.readLines(input);
                StringBuffer jsonInAsString = new StringBuffer();

                // Remove comments
                for (String line : lines) {
                    jsonInAsString.append(line.replaceAll("//.*$", ""));
                }

                JSONObject jsonPreferences = new JSONObject(jsonInAsString.toString());
                mergeJson(jsonPreferences, settings.preferences);

                final int INDENT_SPACES = 2;
                String mergedJsonAsString = jsonPreferences.toString(INDENT_SPACES);

                try (FileOutputStream output = new FileOutputStream(file)) {
                    output.write(mergedJsonAsString.getBytes(UTF_8));
                }
            } catch (IOException e) {
                Logger.error(e, "Unable to merge Chrome preferences with template, using default preferences instead");
            }
        }
    }

    private void mergeJson(JSONObject orig, Map<String, Object> overrides) {
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean) {
                orig.put(entry.getKey(), value);
            } else if (value instanceof Object[]) {
                orig.put(entry.getKey(), new JSONArray(value));
            } else if (value instanceof Map) {
                JSONObject object = orig.optJSONObject(key);
                if (object == null) {
                    object = new JSONObject();
                    orig.put(key, object);
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>)value;
                mergeJson(object, map);
            }
        }
    }

    private void extractExtension() {
        // Extract and install our extension

        String extensionExpandPath = getExtensionExpandPath();
        new File(extensionExpandPath).mkdirs();

        String destExtensionDir = getPizzaExtensionPath();
        try {
            URL resourceDirectory = this.getClass().getResource("/chrome/extension/pizza");
            ResourceUtils.copyDirectory(resourceDirectory, new File(destExtensionDir));

            String configFile = Path.join(destExtensionDir, "config.js");

            Map<String, String> map = new HashMap<>();
            map.put("pizza.id", String.valueOf(userContext.getWorkerId()));
            if (userContext.getEngineContext().getWebSocketIp() != null) {
                map.put("pizza.host", String.valueOf(userContext.getEngineContext().getWebSocketIp().getHostAddress()));
            } else {
                map.put("pizza.host", "localhost");
            }
            map.put("pizza.port", String.valueOf(userContext.getEngineContext().getWebSocketServer().getPort()));

            File configFileDest = new File(configFile + ".new");
            File configFileSrc = new File(configFile);

            Template template = new Template(configFileSrc, map);
            template.write(configFileDest);

            FileUtils.deleteQuietly(configFileSrc);
            FileUtils.moveFile(configFileDest, configFileSrc);
        } catch (ClosedByInterruptException e) {
            throw new ScriptException(String.format("Browser setup interrupted"));
        } catch (IOException e) {
            throw new ScriptException(String.format("Unable to copy extension to Chrome temp dir:\n  %s", e.getMessage()));
        }
    }

    public void cleanProfile() {
        File profileDir = new File(getProfileDir());
        if (profileDir.exists()) {
            if (!settings.reuseProfile) {
                try {
                    FileUtils.cleanDirectory(profileDir);
                } catch (IOException|IllegalArgumentException e) {
                    Logger.error("Unable to clean Chrome profile directory, is another Chrome process open?");
                }
            } else {
                File defaultsDir = new File(profileDir, "Default");
                List<String> dirsToClean =
                    Arrays.asList("Local Storage", "Session Storage", "Pepper Data");

                for (String dirName : dirsToClean) {
                    File dir = new File(defaultsDir, dirName);
                    try {
                        if (dir.exists()) {
                            FileUtils.cleanDirectory(dir);
                        }
                    } catch (IOException|IllegalArgumentException e) {
                        Logger.error("Unable to clean Chrome profile directory, " +
                                "is another Chrome process open? {}", dirName);
                    }
                }

                List<String> filesToClean =
                    Arrays.asList("Cookies", "Cookies-journal", "Last Session", "Last Tabs");

                for (String fileName : filesToClean) {
                    File file = new File(defaultsDir, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        } else {
            profileDir.mkdirs();
        }
    }

    public void cleanCache() {
        // Always clear this
        File cacheDir = new File(getCacheDir());
        if (cacheDir.exists()) {
            try {
                FileUtils.cleanDirectory(cacheDir);
            } catch (IOException|IllegalArgumentException e) {
                Logger.error("Unable to clean Chrome cache directory, is another Chrome process open?");
            }
        } else {
            cacheDir.mkdirs();
        }
    }

    public String getProfileDir() {
        return profileDir;
    }

    public String getPizzaExtensionPath() {
        return pizzaExtensionPath;
    }

    public String getExtensionExpandPath() {
        return extensionExpandPath;
    }

    public void close() {
        if (process != null) {
            Logger.info("Closing Chrome...");

            // Forcibly kill the chrome process, chrome does not exit properly
            // (on OSX at least) when you call Process.destroy().  This causes
            // a problem with our unit tests as there are a ton of chrome processes
            // open hanging our test server (the process keep their TCP connections
            // to our web server open).
            boolean java17Fallback = false;
            try {
                Method method = Process.class.getMethod("destroyForcibly");
                method.invoke(process);
            } catch (NoSuchMethodException|InvocationTargetException|IllegalAccessException e) {
                java17Fallback = true;
            }

            if (java17Fallback) {
                process.destroy();
                // Don't wait for exit (on OSX it takes 30 seconds for the process to exit),
                // when we call destroy, this doesn't normally cause problems however.
            }

            process = null;
        } else {
            try {
                if (processLauncher != null) {
                    Logger.info("Closing Chrome...");
                    processLauncher.stopBrowser();
                    processLauncher = null;
                }
            } catch (IOException|InterruptedException e) {
                Logger.info("Problem closing browser...", e);
            }
        }
    }

    private String getExtensionId() {
        try {
            return ChromeExtensionId.GenerateIdForPath(pizzaExtensionPath);
        } catch (Exception e) {
            Logger.error(e, "Unable to generate id for extension");
            return "unknown";
        }
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public File getBrowserLogFile() {
        return new File(getProfileDir(), "chrome_debug.log");
    }
}
