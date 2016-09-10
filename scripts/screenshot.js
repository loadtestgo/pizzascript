//
// Capture screenshot from Chrome, and then write it to a file using Java API
//
// NOTE: When sandboxing is enabled in the settings.ini this won't run,
// when sandboxing is enabled access to Java APIs is prohibited
//
var b = pizza.open("www.google.com");
var screenshot = b.screenshot();
var file = new java.io.FileOutputStream("google.png");
try {
  file.write(screenshot.bytes);
} finally {
  file.close();
}