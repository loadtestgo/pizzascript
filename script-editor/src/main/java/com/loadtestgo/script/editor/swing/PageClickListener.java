package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.api.TestResult;

public interface PageClickListener {
    void pageClicked(TestResult testResult, String pageId);
}
