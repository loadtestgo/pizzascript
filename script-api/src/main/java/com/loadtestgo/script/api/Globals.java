package com.loadtestgo.script.api;

public class Globals {
    /**
     * Default Third Party block URLs.
     *
     * These include analytics, tracking, advertising and RUM beacons
     *
     * There's tons of these we could add, just adding some common ones.
     */
    public static String [] ThirdParty = new String[]{
        // Double click
        "^http(s)?://ad\\.doubleclick\\.net",
        "^http(s)?://stats\\.g\\.doubleclick\\.net",
        "^http(s)?://cm\\.g\\.doubleclick\\.net/",
        "^http(s)?://googleads\\.g\\.doubleclick\\.net",
        // Google Analytics
        "^http(s)?://www\\.google-analytics\\.com/ga\\.js",
        "^http(s)?://www\\.google-analytics\\.com/analytics\\.js",
        // Google tag management
        "^http(s)?://www\\.googletagservices\\.com/tag/js/gpt\\.js",
        "^http(s)?://www\\.googletagmanager\\.com/gtm\\.js",
        // Pingdom
        "^http(s)?://rum-static\\.pingdom\\.net/prum\\.min\\.js",
        // quantserve tracking
        "^http(s)?://edge\\.quantserve\\.com/quant\\.js",
        "^http(s)?://pixel\\.quantserve\\.com/pixel",
        // scorecardresearch tracking
        "^http(s)?://b\\.scorecardresearch\\.com",
        // Facebook
        "^http(s)?://connect\\.facebook\\.net/",
        "^http(s)?://www\\.facebook\\.com/plugins/like.php\\?",
        // Mathtag
        "^http(s)?://pixel\\.mathtag\\.com/",
        // Twitter
        "^http(s)?://cdn\\.api\\.twitter\\.com/\\d/urls/count.json\\?",
        // Pinterest
        "^http(s)?://api\\.pinterest\\.com/",
        // Adobe tracking
        "^http(s)?://[\\w\\.-]+\\.2o7\\.net/",
        // Gigya
        "^http(s)?://cdns\\.gigya\\.com/js/gigyaGAIntegration\\.js",
        "^http(s)?://cdns\\.gigya\\.com/JS/socialize\\.js",
        // Krux visitor tracking
        "^http(s)?://cdn\\.krxd\\.net",
        "^http(s)?://beacon\\.krxd\\.net",
        // Clciktale
        "^http(s)?://cdn\\.clicktale\\.net/",
        // Optimizely
        "^http(s)?://cdn\\.optimizely\\.com/js/\\d+.js",
        // AudienceScience, decide what content to show
        "^http(s)?://[\\w\\.-]+\\.revsci\\.net",
        // Rubicon Project
        "^http(s)?://anvil.rubiconproject\\.com",
        "^http(s)?://beacon\\.rubiconproject\\.com",
        // Orcale Bluekai marketing info
        "^http(s)?://tags\\.bluekai\\.com",
        // Exelate adverts
        "^http(s)?://loadus\\.exelator\\.com",
        "^http(s)?://loadm\\.exelator\\.com",
        // Visual Revenue
        "http(s)?://[\\w\\.-]+\\.visualrevenue\\.com/vrs\\.js"
    };
}
