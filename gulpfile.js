'use strict';

var fs = require('fs');
var es = require('event-stream');

var gulp = require('gulp');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var replace = require('gulp-replace');

gulp.task('enginejs', function(cbOuter) {
    var src = 'script-engine/src/main/resources/chrome/extension/pizza/';
    var dest = 'script-engine/build/resources/main/chrome/extension/pizza/';
    var i, r;

    var copyDirect = ['config.js', 'namespace.js', 'content.js'];
    for (i = 0; i < copyDirect.length; ++i) {
        copyDirect[i] = src + copyDirect[i];
    }

    var copyDirectFunc = function(cb) {
        gulp.src(copyDirect)
            .pipe(gulp.dest(dest))
            .on('end', cb);
    };

    var readFileList = /\"scripts\":\s*\[([^\]]*)\]/m;

    var replaceScripts = function(cb) {
        gulp.src(src + 'manifest.json')
            .pipe(replace(readFileList,
                '\"scripts\": [\"namespace.js\",\"config.js\",\"pizza.js\"]'))
            .pipe(gulp.dest(dest))
            .on('end', cb);
    };

    var data = fs.readFileSync(src + 'manifest.json', 'utf8');
    function cleanString(r) {
        r = r.replace(/\"/g,"");
        r = r.trim();
        return r;
    }

    var uglyFunc = function(cb) {
        var files = [];

        var f = data.match(readFileList)[1];
        var l = f.split(",");


        for (i = 0; i < l.length; ++i) {
            r = l[i];
            r = cleanString(r);
            if (r != "namespace.js" && r != "config.js") {
                r = src + r;
                files.push(r);
            }
        }

        gulp.src(files)
            .pipe(concat('pizza.js'))
            .pipe(uglify())
            .pipe(gulp.dest(dest))
            .on('end', cb);
    };

   var webAFunc =  function(cb) {
      var resources = /\"web_accessible_resources\":\s*\[([^\]]*)\]/m;
      var f = data.match(resources)[1];
      var l = f.split(",");

      var files = [];
      for (i = 0; i < l.length; ++i) {
          r = l[i];
          r = cleanString(r);
          files.push(src + r);
      }

      gulp.src(files)
          .pipe(gulp.dest(dest))
          .on('end', cb);
    };

    return gulp.series(
        copyDirectFunc,
        replaceScripts,
        uglyFunc,
        webAFunc,
        function(cb) {
           cb();
           cbOuter();
        }
    )();
});

gulp.task('view-metrics', function(cb) {
    var src = "view-metrics/src/main/resources/js/";
    var dest = "view-metrics/build/resources/main/js";

    return gulp.src(src + "*.js")
               .pipe(uglify())
               .pipe(gulp.dest(dest))
                .on('end', cb);
});
