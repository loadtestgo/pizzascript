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
        "*://ad.doubleclick.net/*",
        "*://stats.g.doubleclick.net/*",
        "*://cm.g.doubleclick.net/*",
        "*://googleads.g.doubleclick.net/*",
        // Google Analytics
        "*://www.google-analytics.com/ga.js",
        "*://www.google-analytics.com/analytics.js",
        // Google tag management
        "*://www.googletagservices.com/tag/js/gpt.js",
        "*://www.googletagmanager.com/gtm.js",
        // Pingdom
        "*://rum-static.pingdom.net/prum.min.js",
        // quantserve tracking
        "*://edge.quantserve.com/quant.js",
        "*://pixel.quantserve.com/pixel/*",
        // scorecardresearch tracking
        "*://b.scorecardresearch.com/*",
        // Facebook
        "*://connect.facebook.net/*",
        "*://www.facebook.com/plugins/like.php",
        // Mathtag
        "*://pixel.mathtag.com/*",
        // Twitter
        "*://cdn.api.twitter.com/*/urls/count.json",
        // Pinterest
        "*://api.pinterest.com/*",
        // Adobe tracking
        "*://*.2o7.net/*",
        // Gigya
        "*://cdns.gigya.com/js/gigyaGAIntegration.js",
        "*://cdns.gigya.com/JS/socialize.js",
        // Krux visitor tracking
        "*://cdn.krxd.net/*",
        "*://beacon.krxd.net/*",
        // Clciktale
        "*://cdn.clicktale.net/*",
        // Optimizely
        "*://cdn.optimizely.com/js/*.js",
        // AudienceScience, decide what content to show
        "*://[\\w.-]+.revsci.net/*",
        // Rubicon Project
        "*://anvil.rubiconproject.com/*",
        "*://beacon.rubiconproject.com/*",
        // Orcale Bluekai marketing info
        "*://tags.bluekai.com/*",
        // Exelate adverts
        "*://loadus.exelator.com/*",
        "*://loadm.exelator.com/*",
        // Visual Revenue
        "*://[\\w.-]+.visualrevenue.com/vrs.js"
    };
}
