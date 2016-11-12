// Call Java APIs
//
// NOTE: When sandboxing is enabled in the settings.ini this won't run,
// when sandboxing is enabled access to Java APIs is prohibited
//
var f = new java.io.FileWriter("example.txt");
try {
   f.write("Some text!\n");
} finally {
   f.close();
}