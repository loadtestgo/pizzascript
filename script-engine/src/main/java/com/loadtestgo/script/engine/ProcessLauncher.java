package com.loadtestgo.script.engine;

import java.io.IOException;
import java.util.ArrayList;

public interface ProcessLauncher {
    void startBrowser(ArrayList<String> args) throws IOException, InterruptedException;
    void stopBrowser() throws IOException, InterruptedException;
}
