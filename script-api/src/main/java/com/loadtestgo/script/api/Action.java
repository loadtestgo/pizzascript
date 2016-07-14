package com.loadtestgo.script.api;

public interface Action {
    void mouseDown(Button b, int x, int y, Key key);
    void mouseUp(Button b, int x, int y, Key key);
    void type(Key key);
}
