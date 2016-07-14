var b = pizza.open("https://themenustar.com/webspace/menus.php?code=newyorkerpizzeria.mobile-webview.com");
b.click("a:contains(CRAFT PIZZA)");

css = "h4:contains(Large San Diego Craft Pizza)";
b.waitForVisible(css);

b.click(css);
css = "#option_36576";
b.waitForVisible(css);
b.click(css);

b.click("#option_36600");
b.click("#option_36580");
b.click("button:contains(Add To Cart)");
