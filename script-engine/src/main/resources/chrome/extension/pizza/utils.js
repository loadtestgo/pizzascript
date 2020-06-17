/**
 * Check if a URL is an internal chrome URL or an actual outside navigation
 * @param url
 * @returns {boolean}
 */
pizza.isExternalUrl = function(url) {
    if (url.indexOf("chrome:") == 0) {
        return false;
    }

    if (url.indexOf("chrome-extension:") == 0) {
        return false;
    }

    if (url.indexOf("chrome-devtools:") == 0) {
        return false;
    }

    if (url.indexOf("about:") == 0) {
        return false;
    }

    return true;
};

/**
 * Are base urls equal (base url meaning url minus #fragment)
 */
pizza.baseUrlEquals = function(url1, url2) {
    var stripFragment = /#[^#]*$/;
    var baseUrl1 = url1.replace(stripFragment, "");
    var baseUrl2 = url2.replace(stripFragment, "");
    return baseUrl1 === baseUrl2;
};

/**
 * Strip out just the hostname from a URL
 * @param url
 * @returns {*}
 */
pizza.getHostFromUrl = function(url) {
    // http:
    var firstColon = url.indexOf(":");
    if (firstColon > 0) {
        var startHost = firstColon + 1;
        if (url.charAt(startHost) === '/') {
            startHost += 1;
        }
        if (url.charAt(startHost) === '/') {
            startHost += 1;
        }
        var portColon = url.indexOf(":", startHost);
        var firstSlash = url.indexOf("/", startHost);
        var endHost = url.length;
        if (firstSlash >= 0 && endHost > firstSlash) {
            endHost = firstSlash;
        }
        if (portColon >= 0 && endHost > portColon) {
            endHost = portColon;
        }
        return url.substring(startHost, endHost);
    }
    return null;
};

/**
 * Escape special regex sequences from a string, so the string
 * can be used to create a regex for matching the original string.
 * @param string
 * @returns {string}
 */
pizza.regexEscape = function(string) {
    return string.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
};

/**
 * Create a new regexp for the given string representation of
 * a regexp.  This is useful for marshalling a regexp from
 * json data.
 *
 * @param string
 * @returns {RegExp}
 */
pizza.regexFromString = function(string) {
    var firstIndex = string.indexOf("/");
    var lastIndex = string.lastIndexOf("/");
    if (firstIndex < 0 || lastIndex <= firstIndex) {
        return new RegExp(string);
    } else {
        var match = string.substring(firstIndex + 1, lastIndex);
        var params = string.substring(lastIndex + 1, string.length);
        return new RegExp(match, params);
    }
};

/**
 * Slightly weird way to do heredocs, might be easier in many cases to read from file.
 *
 *  var css = pizza.heredoc(function() {/*
 *  heredoc goes here
 *  NOTE: the next line should actually be without the space between '*' and '/'!
 *  * /});
 *
 *  Won't work with minification of course.
 *
 * @param functionWithComment
 * @returns {string}
 */
pizza.heredoc = function(functionWithComment) {
    return functionWithComment.toString().match(/\/\*\s*([\s\S]*?)\s*\*\//m)[1].replace(/(\/\*[\s\S]*?\*) \//g, '$1/');
};

/**
 * Wait on multiple callbacks.  Broken promises, be damned!
 *
 * Call a function when multiple callbacks are complete.  This is done by wrapping your callbacks
 * in a function that potentially calls your done function.  Exceptions are caught automatically
 * in the callback wrapper and done is call regardless.
 *
 * var wait = pizza.waitAll();
 *
 * // First, use wait.add() to add callback wrappers
 * something.asyncFunction1(wait.add(function(args..) { ... do callback work... }));
 * something.asyncFunction2(wait.add(function(args..) { ... do callback work... }));
 *
 * // Then add your done function.  Once all callbacks are complete this function will
 * // be called.
 * wait.done(function() { console.log("all are done!") });
 *
 * You can remove a callback by calling wait.remove(yourFunction);
 *
 * You can assign a done callback before adding callbacks by calling addCallbacksLater()
 * first, and then callbacksAdded() once all callbacks are added and you are ready for
 * the done function to be called.  These function nest so the 'wait' object can be passed
 * around safely to other functions that add their own callbacks.
 *
 * Since we use function wrapping this avoids some of the problems with repeated nested
 * scoping and operations happening in serial that could happen in parallel.
 */
pizza.waitAll = function(name, log) {
    var _name = name;
    var _doneCallback = null;
    var _log = log;
    var _waitingFor = [];
    var _id = 0;
    var _waiting = 1;

    var _add = function() {
        var name = null;
        var func = null;
        for (var i = 0; i < arguments.length; ++i) {
            var arg = arguments[i];
            if (pizza.isString(arg)) {
                name = arg;
            } else if (pizza.isFunction(arg)) {
                func = arg;
            }
        }
        if (_log) {
            console.log(_name, "adding " + name);
        }
        var id = _id++;
        var f = function() {
            if (func) {
                func.apply(this, arguments);
            }
            _ack(id);
        };
        _waitingFor.push( { name: name, f: f, waiting: true, id: id });
        return f;
    };

    var _find = function(id) {
        for (var i = 0; i < _waitingFor.length; ++i) {
            var item = _waitingFor[i];
            if (item.id === id) {
                return item;
            }
        }
        return null;
    };

    var _ack = function(id) {
        var item = _find(id);

        if (_log) {
            console.log(_name, "acking " + item.name);
        }

        if (item.waiting) {
            item.waiting = false;
        }

        _checkDone();
    };

    var _checkDone = function() {
        if (_waiting <= 0) {
            for (var i = 0; i < _waitingFor.length; ++i) {
                var item = _waitingFor[i];
                if (item.waiting) {
                    if (_log) {
                        console.log(_name, "waiting on " + item.name);
                    }
                    return;
                }
            }

            if (_log) {
                console.log(_name, "done");
            }

            if (_doneCallback) {
                _doneCallback();
            } else {
                console.log(_name, "Would be done, but no done callback specified");
            }
        }
    };

    var _remove = function(f) {
        for (var i = 0; i < _waitingFor.length; ++i) {
            var item = _waitingFor[i];
            if (item.f === f) {
                _waitingFor.splice(i, 1);
                return;
            }
        }
    };

    var _waitMore = function() {
        _waiting = _waiting + 1;
        if (_log) {
            console.log(_name, "waiting for another round of callbacks " + _waiting)
        }
    };

    var _waitLess = function() {
        _waiting = _waiting - 1;

        if (_log) {
            console.log(_name, "finished waiting on a round of callbacks " + _waiting);
        }

        if (_waiting <= 0 && !_doneCallback) {
            console.log(_name, "Did you forget to call done(func)? Or add too many addCallbacksLater() calls?");
        }

        _checkDone();
    };

    var _done = function(done) {
        if (done) {
            _doneCallback = done;
        }

        _waitLess();
    };

    return {
        add: _add,
        remove: _remove,
        done: _done,
        addCallbacksLater: _waitMore,
        callbacksAdded: _waitLess
    };
};

/**
 * Null version of pizza.waitAll, used when you don't need to a done callback,
 * but are required to provide a pizza.waitAll object.
 */
pizza.waitNull = function(name, log) {
    return {
        add: function() {},
        remove: function() {},
        done: function() {},
        addCallbacksLater: function() {},
        callbacksAdded: function() {}
    };
};

pizza.isArray = function(v) {
    if (!v) { return false; }
    return (typeof v == 'array' || v instanceof Array);
};

pizza.isString = function(v) {
    return (typeof v == 'string' || v instanceof String);
};

pizza.isDate = function(v) {
    return (typeof v == 'date' || v instanceof Date);
};

pizza.isNumber = function(v) {
    return (typeof v == 'number' || v instanceof Number);
};

pizza.isBoolean = function(v) {
    return (typeof v === "boolean" || v instanceof Boolean);
}

pizza.isFunction = function(v) {
    return (typeof v == 'function' || v instanceof Function);
};

pizza.copyProperty = function(dest, id, src) {
    var val = src[id];
    if (val != undefined) {
        dest[id] = val;
    }
};

pizza.arrayDelete = function(array, item) {
    var i = array.indexOf(item);
    return array.slice(i, 1);
};

/**
 * Add startsWith function to JavaScript strings
 */
if (typeof String.prototype.startsWith != 'function') {
    if (typeof String.prototype.startsWith != 'function') {
        String.prototype.startsWith = function (str){
            return this.slice(0, str.length) == str;
        };
    }
}

pizza.hasAtLeastOneProperty = function(object, props) {
    for (var i = 0; i < props.length; ++i) {
        var prop = props[i];
        if (object.hasOwnProperty(prop)) {
            return true;
        }
    }
    return false;
};

pizza.each = function(a, fEachItem, fResults) {
    var i = 0;
    var doneCallback = function() {
        if (i < a.length) {
            fEachItem.apply(null, [a[i], doneCallback]);
        } else {
            fResults();
        }
        i++;
    };
    doneCallback();
};
