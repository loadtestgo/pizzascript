Requirements
------------

+ Java 8
+ Chrome 40+ (*)

* Works with Stable and Release channels for the most part, but the block* and redirect*
  functions are only supported on the Dev Channel right now.


Setup
-----

If Chrome is installed and in the PATH or installed normally the script editor will
generally find it.  Failing this you can set the path to Chrome (and other settings)
in the settings.ini file.  See the settings.ini file included with this download for
details.


Running
-------

Open the IDE:

  > bin/script-editor

Open a file in IDE:

  > bin/script-editor samples/google.js

Run a script from the console (no IDE):

  > bin/script-editor -console samples/google.js

Run the interactive console:

  > bin/script-editor -console


Example Scripts
---------------

Some example scripts can be found under 'scripts/'.


API Documentation
-----------------

API documentation is under 'docs/'

The file 'docs.js' under the top level directory can be used for auto-completion with various IDEs.
