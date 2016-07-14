package com.loadtestgo.script.api;

public class WebSocketMessage {
    public int time;
    public int len;
    public Flow flow;
    public String data; // truncated to 100 bytes
    public enum Flow {Received, Sent}
}
