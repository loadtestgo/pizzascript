------------------------------------
PizzaScript Editor and Script Runner
------------------------------------


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


Editor
------

The PizzaScript Editor makes writing & debugging scripts a breeze.  It features:

 - A JavaScript debugger with breakpoints and variable inspection

 - An interactive console (very useful when writing scripts)

 - Element selector generation

 - Syntax highlighting, with errors underlined


To open the editor:

  > bin/script-editor


Open a file in IDE:

  > bin/script-editor samples/google.js


Run without a GUI:

  > bin/script-editor -console


Runner
------

The PizzaScript Runner will run a set of scripts from the command line.  It features:

  - On-going log of scripts that succeeded / failed

  - Saves off HAR file for each script

  - Saves a screenshot (taken at the end of each script)

  - Saves any console.log() output the script wrote


To run all scripts in a directory:

  > bin/script-runner scripts


To see a list of options:

  > bin/script-runner -h


Example Scripts
---------------

Some example scripts can be found under 'scripts/'.


API Documentation
-----------------

API documentation is under 'docs/'

The file 'docs/pizzascript.js' can be used for auto-completion with JavaScript IDEs.
