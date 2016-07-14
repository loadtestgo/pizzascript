'use strict';

var fs = require('fs');
var es = require('event-stream');

var gulp = require('gulp');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var replace = require('gulp-replace');

gulp.task('enginejs', function() {
    var tasks = [];

    var src = 'script-engine/src/main/resources/chrome/extension/pizza/';
    var dest = 'script-engine/build/resources/main/chrome/extension/pizza/';
    var i, r;

    var copyDirect = ['config.js', 'namespace.js', 'content.js'];
    for (i = 0; i < copyDirect.length; ++i) {
        copyDirect[i] = src + copyDirect[i];
    }

    tasks.push(
        gulp.src(copyDirect)
            .pipe(gulp.dest(dest)));

    var readFileList = /\"scripts\":\s*\[([^\]]*)\]/m;

    tasks.push(
        gulp.src(src + 'manifest.json')
            .pipe(replace(readFileList,
                '\"scripts\": [\"namespace.js\",\"config.js\",\"pizza.js\"]'))
            .pipe(gulp.dest(dest)));

    var files = [];

    var data = fs.readFileSync(src + 'manifest.json', 'utf8');

    var f = data.match(readFileList)[1];
    var l = f.split(",");

    function cleanString(r) {
        r = r.replace(/\"/g,"");
        r = r.trim();
        return r;
    }

    for (i = 0; i < l.length; ++i) {
        r = l[i];
        r = cleanString(r);
        if (r != "namespace.js" && r != "config.js") {
            r = src + r;
            files.push(r);
        }
    }

    tasks.push(
      gulp.src(files)
          .pipe(concat('pizza.js'))
          .pipe(uglify())
          .pipe(gulp.dest(dest)));

    var resources = /\"web_accessible_resources\":\s*\[([^\]]*)\]/m;
    f = data.match(resources)[1];
    l = f.split(",");

    files = [];
    for (i = 0; i < l.length; ++i) {
        r = l[i];
        r = cleanString(r);
        files.push(src + r);
    }

    tasks.push(gulp.src(files)
                   .pipe(gulp.dest(dest)));

    return es.merge(tasks);
});

gulp.task('view-metrics', function() {
    var src = "view-metrics/src/main/resources/js/";
    var dest = "view-metrics/build/resources/main/js";

    return gulp.src(src + "*.js")
               .pipe(uglify())
               .pipe(gulp.dest(dest));
});
