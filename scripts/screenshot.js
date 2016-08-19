//
// Capture screenshot from Chrome, and then write it to a file using Java API
//
// For this to run, access to Java APIs has to be enabled in settings.ini.
// Uncomment the line 'sandbox=false'
//
var b = pizza.open("www.google.com");
var screenshot = b.screenshot();
var file = new java.io.FileOutputStream("google.png");
try {
  file.write(screenshot.bytes);
} finally {
  file.close();
}