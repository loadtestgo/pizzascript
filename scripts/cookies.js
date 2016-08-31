var b = pizza.open("www.google.com");

// Set a cookie 'which' with the 'chocolate chip' on the current domain
b.setCookie("which", "chocolate chip");

// List all cookies set in the browser
console.log(b.listCookies());
