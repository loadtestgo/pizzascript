// Script for ordering a pizza
var b = pizza.open("https://themenustar.com/webspace/menus.php?code=newyorkerpizzeria.mobile-webview.com");

// Click the pizza type we wants
b.click("a:contains(CRAFT PIZZA)");

// Wait for the pizza size menu to be displayed, then click the size we want
css = "h4:contains(Large San Diego Craft Pizza)";
b.waitForVisible(css);
b.click(css);

// Add the toppings, once the topping are displayed
css = "label:contains(Mushroom)";
b.waitForVisible(css);
b.click(css);

// Add the rest of the toppings
b.click("label:contains(Feta)");
b.click("label:contains(Fresh Basil)");

// Add to cart
b.click("button:contains(Add To Cart)");
