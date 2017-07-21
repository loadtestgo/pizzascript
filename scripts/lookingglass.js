// Script to lookup and log the IPv4 peered
// Autonomous System Numbers (ASN) of the domain

var b = pizza.open('http://bgp.he.net/');

var domains = ['walmart.com', 'amazon.com'];

b.type('#search_search', utils.randomElement(domains));

b.click("xpath://input[@name=\"commit\"]");

b.waitPageLoad();

b.waitForVisible('#tab_ipinfo');

b.click('#tab_ipinfo');

b.waitForElement('a:contains(AS)');

b.click('a:contains(AS)')

b.waitPageLoad();

console.log(b.getInnerText(".toppeertable"));