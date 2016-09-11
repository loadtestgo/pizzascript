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

  - Saves off HAR file for each script

  - Saves a screenshot (taken at the end of each script)

  - Saves any console.log() output the script wrote


To run all scripts in a directory:

  > bin/pizzascript scripts


To see a list of options:

  > bin/pizzascript -h


Example Scripts
---------------

Some example scripts can be found under 'scripts/'.


API Documentation
-----------------

API documentation is under 'docs/'

The file 'docs/pizzascript.js' can be used for auto-completion with JavaScript IDEs.
