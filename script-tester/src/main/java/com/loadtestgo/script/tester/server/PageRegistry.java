package com.loadtestgo.script.tester.server;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.util.StringUtils;
import org.pmw.tinylog.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageRegistry {
    ArrayList<PageInfo> pageInfo = new ArrayList<>();
    ArrayList<Server> servers = new ArrayList<>();

    public PageRegistry() {
    }

    public void registerClass(Class c, String baseUrl) {
        if (!baseUrl.startsWith("/")) {
            baseUrl = "/" + baseUrl;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        PageInfo pageInfo = new PageInfo(c);
        pageInfo.baseUrl = baseUrl;

        // Get the method URLs for the class
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Page.class)) {
                String url = null;
                Page page = method.getAnnotation(Page.class);
                if (!StringUtils.isSet(page.path())) {
                    url = method.getName();
                } else {
                    String path = page.path();
                    if (path.charAt(0) == '/') {
                        url = path.substring(1);
                    } else {
                        Logger.error("Path annotation for {}:{} must start with '/'",
                                c.getCanonicalName(), method.getName());
                    }
                }
                if (url != null) {
                    EndPoint endPoint = new EndPoint();
                    endPoint.method = method;
                    endPoint.desc = page.desc();
                    endPoint.url = url;
                    pageInfo.methodsMap.put(url, endPoint);
                }
            }
        }

        if (pageInfo.methodsMap.size() == 0) {
            Logger.error("At least one method in Class {} must be annotated with @Handler(\"/some/url\")", c);
            return;
        }

        this.pageInfo.add(pageInfo);
    }

    PageInfo getPageInfoForUrl(String url) {
        for (PageInfo pageInfo : this.pageInfo) {
            if (url.startsWith(pageInfo.baseUrl)) {
                return pageInfo;
            }
        }
        return null;
    }

    public ArrayList<PageInfo> getPageInfo() {
        return pageInfo;
    }

    public void registerServers(ArrayList<Server> badServers) {
        this.servers = badServers;
    }

    public ArrayList<Server> servers() {
        return servers;
    }

    static public class EndPoint {
        Method method;
        String url;
        String desc;
    }

    static public class PageInfo {
        public Class pageClass;
        public String baseUrl;
        public Map<String, EndPoint> methodsMap;

        PageInfo(Class pageClass) {
            this.pageClass = pageClass;
            this.methodsMap = new HashMap<>();
        }
    }
}
