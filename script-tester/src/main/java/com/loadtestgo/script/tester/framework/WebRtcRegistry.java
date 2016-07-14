package com.loadtestgo.script.tester.framework;

import org.json.JSONObject;

import java.util.ArrayList;

public class WebRtcRegistry {
    private static WebRtcRegistry instance;
    private ArrayList<Session> sessions = new ArrayList<>();
    private int nextSessionId;

    public synchronized static WebRtcRegistry getInstance() {
        if (instance == null) {
            instance = new WebRtcRegistry();
        }
        return instance;
    }

    private WebRtcRegistry() {
        this.nextSessionId = 1;
    }

    public Session createSession() {
        synchronized (sessions) {
            int hostId = nextSessionId++;
            int clientId = nextSessionId++;
            Session session = new Session(hostId, clientId);
            sessions.add(session);
            return session;
        }
    }

    public Session getUnusedSession() {
        synchronized (sessions) {
            Session s = null;
            for (Session session : sessions) {
                s = session;
            }
            return s;
        }
    }

    public Session getSession(int id) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.clientId == id ||
                        session.hostId == id) {
                    return session;
                }
            }
            return null;
        }
    }

    public JSONObject getMessage(int id) {
        synchronized (sessions) {
            Session session = getSession(id);
            return session.getMessage(id);
        }
    }

    public void addMessage(int id, JSONObject msg) {
        synchronized (sessions) {
            Session session = getSession(id);
            session.addMessage(id, msg);
        }
    }

    static public class Session {
        public String sdpOffer;
        public String sdpAnswer;

        public int hostId;
        public int clientId;

        public ArrayList<JSONObject> hostMessages = new ArrayList<>();
        public ArrayList<JSONObject> clientMessages = new ArrayList<>();

        public Session(int hostId, int clientId) {
            this.hostId = hostId;
            this.clientId = clientId;
        }

        public JSONObject getMessage(int id) {
            if (hostId == id) {
                if (hostMessages.size() > 0) {
                    return hostMessages.remove(0);
                }
            } else if (clientId == id) {
                if (clientMessages.size() > 0) {
                    return clientMessages.remove(0);
                }
            }
            return null;
        }

        public void addMessage(int id, JSONObject msg) {
            if (hostId == id) {
                clientMessages.add(msg);
            } else if (clientId == id) {
                hostMessages.add(msg);
            }
        }
    }
}
