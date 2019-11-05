## Requirements

+ Java 8 (Oracle JDK or OpenJDK w/ OpenJFX)
+ NodeJS (if you need to build release package or docs)
+ Gulp (if you need to build release package)


## Build

    git clone git@github.com:loadtestgo/pizzascript.git
    cd pizzascript

To build the script editor package:

    ./gradlew dist:distZip

This will save a zip file to:

    dist/build/distributions/pizzascript-0.2.5.zip

To build a flat dir for the editor or runner:

    ./gradlew script-editor:installApp

This will package the shell scripts and all dependent JARS to the following directory:

    script-editor/build/install/script-editor


## Release Packaging

In release mode all the JavaScript files are concatenated and minified, to speed up
loading of the extension.

We use NodeJS & Gulp to minify the JS files (NodeJS is also needed to generate docs).

Install NodeJS 0.12+:

On OSX:

    brew install nodejs

On Debian (replace \_ with just the underscore):

    curl -sL https://deb.nodesource.com/setup\_0.12 | sudo bash -
    sudo apt-get install nodejs

Install Gulp as follows (sudo is necessary):

    npm install gulp
    npm install gulp-uglify gulp-concat gulp-replace event-stream

To generate a release build:

    gradle clean dist:distZip -Prelease


### Build docs:

JSDocs is used to generate the docs.

    npm install jsdoc
    npm install https://github.com/hegemonic/baseline/tarball/master
    gradle docs

These can be checked in and will appear on pizzascript.org once merged with the master
branch.
