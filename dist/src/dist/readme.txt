-------------------------------
PizzaScript IDE and Test Runner
-------------------------------


Requirements
------------

+ Java 8
+ Chrome 40+


Setup
-----

If Chrome is installed and in the PATH or installed normally the script editor will
generally find it.  Failing this you can set the path to Chrome (and other settings)
in the settings.ini file.  See the settings.ini file included with this download for
details.


PizzaScript IDE
---------------

The PizzaScript Editor makes writing & debugging scripts a breeze.  It features:

 - A JavaScript debugger with breakpoints and variable inspection

 - An interactive console (very useful when writing scripts)

 - Element selector generation

 - Syntax highlighting, with errors underlined


To open the script editor:

  > bin/pizzascript-ide


Open a file in IDE:

  > bin/pizzascript-ide samples/google.js


Run without a GUI:

  > bin/pizzascript-ide -console


PizzaScript Runner
------------------

The PizzaScript Runner will run a set of scripts from the command line.  It features:

  - On-going log of scripts that succeeded / failed

  - Saves off HAR file for each script ran

  - Saves off video and screenshots for each sript ran

  - Saves any console.log() written by the script

  - JUnit output


To run all scripts in a directory:

  > bin/pizzascript scripts

While this is sometimes convenient, often more control is needed, see the 'Running a
Test Suite' section for a more complete way, that offers more options.

To see a list of options:

  > bin/pizzascript -h


Example Scripts
---------------

Some example scripts can be found under 'scripts/'.


API Documentation
-----------------

API documentation is under 'docs/'

The file 'docs/pizzascript.js' can be used for auto-completion with JavaScript IDEs.


Running a Test Suite
--------------------

A list of scripts to run can be specified in a 'json' file.

  // comments are allowed :)
  {
    "name": "My Test Suite",          // the test suite name
    "timeout": 60,                    // the default timeout in seconds for each test
    "settings": {                     // any settings.ini settings can be overridden here
      "verbose": true
    },
    "tests": [                        // the list of tests
      {
        "file": "basic.js",           // path to script file relative to json file
        "timeout": 10,                // optional timeout of script in seconds
        "name": "open url"            // optional name for the test, displayed in test output
      },
      { "file": "sitelogin.js" },
      { "file": "verifytext.js" }
  ]
}

The test script can be run as follows:

  > bin/pizzascript scripts/testsuite.json


More Info
---------

Further information can be found on the Wiki:

    https://github.com/loadtestgo/pizzascript/wiki
