package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.NavigationType;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import com.loadtestgo.util.Dirs;
import com.loadtestgo.util.Os;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileUpload extends JavaScriptTest {
    @Test
    public void submitForm() throws IOException {
        File fileToUpload = new File(Dirs.getTmp(), "upload.txt");

        String fileContents = "This is the content of the file";
        try (
            BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(fileToUpload))))
        {
            bw.write(fileContents);
        }

        String formUrl = getTestUrl("files/fileUpload.html");
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
                "b.setFile('#file', '%s');\n" +
                "b.submit('#fileUploadForm');\n" +
                "b.waitPageLoad();" +
                "b.verifyText('%s');\n",
            formUrl,
            escapeWindowsPath(fileToUpload.getAbsolutePath()),
            fileContents);

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());

        Page post = result.getLastPage();
        assertEquals(NavigationType.FormSubmit, post.getNavigationType());

        HttpRequest postRequest = getFirstRequest(post);
        assertEquals(postRequest.getMethod(), "POST");
        assertNotNull(postRequest.getPostData());
    }

    @Test
    public void submitFormFileNotFound() throws IOException {
        String formUrl = getTestUrl("files/fileUpload.html");

        String dummyFile = "/tmp/not_actually_a_file/that/should/exist";
        if (Os.isWin()) {
            dummyFile = "C:\\tmp\\not_actually_a_file\\that\\should\\exist";
        }

        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
                "b.setFile('#file', '%s');\n" +
                "b.submit('#fileUploadForm');\n" +
                "b.waitPageLoad();\n",
            formUrl,
            escapeWindowsPath(dummyFile));

        TestResult result = runScript(script);

        assertError(String.format("file %s not found", dummyFile), result);
    }

    private String escapeWindowsPath(String path) {
        return path.replace("\\", "\\\\");
    }
}
