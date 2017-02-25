var b = pizza.open('https://toolbox.googleapps.com/apps/dig/');

pizza.sleep(1000);
// b.waitPageLoad();  // script has been getting hung up

var domains = ['walmart.com', 'amazon.com'];

b.type('#domain', domains[Math.floor((Math.random() * domains.length - 1) + 1)]);

b.click('xpath://input[@value="NS"]');

b.waitForVisible('#response-text');

var html = b.getInnerHTML('xpath://*[@id="response-text"]');

var re = /href="(.*?)"/g;

var authNameServers = [];

while(match = re.exec(html)){

    authNameServers.push(match[1].split('@')[1]);

}

console.log(authNameServers);