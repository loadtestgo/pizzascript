// Just an example URL, this URL doesn't actually have an upload form!
pizza.open("www.example.com/uploadfile");

// Set the file path for the file input element matching the '#file' selector
b.setFile('#file', '%s');

// Submit the form
b.submit('#fileUploadForm');

// Wait until the file upload has completed
b.waitPageLoad();
