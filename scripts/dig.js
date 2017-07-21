// Script using Google's hosted DIG (DNS) utility to log
// the Authoritative nameservers of the requested domain

var b = pizza.open("https://toolbox.googleapps.com/apps/dig/");

b.waitForVisible("#domain");

var domains = ["walmart.com", "amazon.com"];

b.type("#domain",utils.randomElement(domains));

var records = ["A", "AAAA", "ANY", "CNAME", "MX", "NS", "PTR", "SOA", "SRV", "TXT"];

// b.click("xpath://input[@value=\"" + records[5] + "\"]");
b.click("input[value=\"" + records[5] + "\"]");

b.waitForVisible('#response-text');

var html = b.getInnerHTML('#response-text');

var re = /href="(.*?)"/g;

var authNameServers = [];

while(match = re.exec(html)){
    authNameServers.push(match[1].split("@")[1]);
}

console.log(authNameServers);