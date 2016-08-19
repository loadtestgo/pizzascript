// Write to a file using Java APIs
//
// For this to run, access to Java APIs has to be enabled in settings.ini.
// Uncomment the line 'sandbox=false'
//
// This is for local testing only, on our servers these APIs can not be accessed.
var f = new java.io.FileWriter("example.txt");
try {
   f.write("Some text!\n");
} finally {
   f.close();
}