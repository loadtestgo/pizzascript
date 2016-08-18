var b = pizza.open("http://demo.nopcommerce.com/");

b.click("a.ico-login")
b.waitPageLoad();

b.type("#Email", "mark@loadtestgo.com")
b.type("#Password", "pizzascript")

b.click("input.login-button")
b.waitPageLoad();