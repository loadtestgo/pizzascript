package com.loadtestgo.script.editor.swing;

import org.fife.rsta.ac.LanguageSupport;
import org.fife.rsta.ac.js.JavaScriptCompletionProvider;
import org.fife.rsta.ac.js.JavaScriptLanguageSupport;
import org.fife.rsta.ac.js.SourceCompletionProvider;
import org.fife.rsta.ac.js.ast.type.ecma.v5.TypeDeclarationsECMAv5;
import org.fife.rsta.ac.js.completion.JSCompletionUI;
import org.fife.rsta.ac.js.engine.RhinoJavaScriptEngine;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;

import javax.swing.text.JTextComponent;
import java.util.ArrayList;

public class RhinoJavaScriptLanguageSupport extends JavaScriptLanguageSupport {
    private static final String ENGINE = RhinoJavaScriptEngine.RHINO_ENGINE;

    public RhinoJavaScriptLanguageSupport() {
        JavaScriptTokenMaker.setJavaScriptVersion("1.7");
        setECMAVersion(TypeDeclarationsECMAv5.class.getName(), getJarManager());
    }

    @Override
    protected JavaScriptCompletionProvider createJavaScriptCompletionProvider() {
        return new JavaScriptCompletionProvider(
            new MySourceCompletionProvider(), getJarManager(), this);
    }

    public void install(RSyntaxTextArea textArea)  {
        LanguageSupport support =
            (LanguageSupport)textArea.getClientProperty("org.fife.rsta.ac.LanguageSupport");
        if (support!=null) {
            support.uninstall(textArea);
        }
        super.install(textArea);
    }

    private class MySourceCompletionProvider extends SourceCompletionProvider {
        private ArrayList<Completion> myCompletions = new ArrayList<>();

        public MySourceCompletionProvider() {
            super(ENGINE, false);
            createMyCompletions();
        }

        private void createMyCompletions() {
            // add my completions here
            myCompletions.add(new MyBasicCompletion(this,
                "abstract", "this is the description", "this is the summary"));
        }

        public String getAlreadyEnteredText(JTextComponent comp) {
            String text = super.getAlreadyEnteredText(comp);
            if (text != null && text.length() > 0) {
                //only add the completions if text is entered, remove this check to always add them
                completions.addAll(myCompletions);
            }
            return text;
        }

        private class MyBasicCompletion extends BasicCompletion implements JSCompletionUI {
            public MyBasicCompletion(CompletionProvider provider,
                                     String replacementText,
                                     String shortDesc,
                                     String summary) {
                super(provider, replacementText, shortDesc, summary);
            }

            @Override
            public int getRelevance() {
                //keep me to the bottom of the completions
                return TEMPLATE_RELEVANCE;
            }
        }
    }
}