var pizza = pizza || {};

pizza.parseQueryString = function(qs)
{
    var r = [];

    // Delete leading question mark, if there is one
    if (qs.charAt(0) == '?') {
        qs= qs.substring(1);
    }

    // Parse it
    var re = /([^=&]+)(=([^&]*))?/g;
    while (match= re.exec(qs))
    {
        var key = decodeURIComponent(match[1].replace(/\+/g,' '));
        var value = match[3] ? pizza.decodeUriComponent(match[3]) : '';
        r.push([key, value]);
    }

    return r;
};

pizza.parseQueryStringFromUrl = function(url) {
    var q = url.indexOf("?");
    if (q < 0) {
        return [];
    }

    return pizza.parseQueryString(url.substring(q));
};

// This function is an error tolerant version of the builtin
// function decodeURIComponent(), modified to also change pluses into
// spaces, so that it is suitable for query string decoding.
pizza.decodeUriComponent = function(value)
{
    value = value.replace(/\+/g,' ');

    value = value.replace(/%([EF][0-9A-F])%([89AB][0-9A-F])%([89AB][0-9A-F])/gi,
        function(code, hex1, hex2, hex3)
        {
            var n1= parseInt(hex1, 16) - 0xE0;
            var n2= parseInt(hex2, 16) - 0x80;
            if (n1 == 0 && n2 < 32) {
                return code;
            }
            var n3 = parseInt(hex3, 16) - 0x80;
            var n= (n1<<12) + (n2<<6) + n3;
            if (n > 0xFFFF) {
                return code;
            }
            return String.fromCharCode(n);
        });

    value = value.replace(/%([CD][0-9A-F])%([89AB][0-9A-F])/gi,
        function(code, hex1, hex2)
        {
            var n1 = parseInt(hex1, 16) - 0xC0;
            if (n1 < 2) {
                return code;
            }
            var n2 = parseInt(hex2, 16) - 0x80;
            return String.fromCharCode((n1 << 6) + n2);
        });

    value = value.replace(/%([0-7][0-9A-F])/gi,
        function(code, hex)
        {
            return String.fromCharCode(parseInt(hex, 16));
        });

    return value;
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
        if (url.charAt(startHost) == '/') {
            startHost += 1;
        }
        if (url.charAt(startHost) == '/') {
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

pizza.formatSize = function(bytes) {
    if (bytes == -1 || bytes == undefined) {
        return "?";
    } else if (bytes == 0) {
        return "0 B";
    } else if (bytes < 1000) {
        return bytes + " B";
    } else if (bytes < (1000 * 1000)) {
        return Math.round(bytes / 1000) + " KB";
    } else if (bytes < (1000 * 1000 * 1000)) {
        return Math.round(bytes * 10 / (1000 * 1000)) / 10 + " MB";
    } else {
        return Math.round(bytes * 10 / (1000 * 1000 * 1000)) / 10 + " GB";
    }
};

pizza.formatSizeInBytes = function(bytes) {
    return pizza.formatInt(bytes) + " B";
};

pizza.formatInt = function(bytes) {
    return bytes.toLocaleString();
};

pizza.formatMillis = function(time) {
    if (time === undefined || time < 0) {
        return "n/a";
    } else if (time === 0) {
        return "0ms";
    } else if (time < 1000) {
        return time + "ms";
    } else {
        var a = 10;
        return Math.round(time * a / 1000) / a + "s";
    }
};

pizza.formatMillisPlus = function(time) {
    if (time > 0) {
        return "+" + pizza.formatMillis(time);
    } else if (time === 0) {
        return pizza.formatMillis(time);
    } else {
        return "n/a";
    }
};

pizza.formatTimeAgo = function(time) {
    var now = Date.now();
    if (pizza.isDate(time)) {
        time = time.getTime();
    }
    var ago = Math.floor((now - time) / 1000);
    return pizza.formatTimeSpan(ago);
};

/**
 * Format a length of time in seconds to our time span format.
 * @param time
 * @returns {string}
 */
pizza.formatTimeSpan = function(time) {
    var minute = 60;
    var hour = 60 * 60;
    var day = 24 * hour;

    time = Math.floor(time);
    if (time < minute) {
        return time + "s";
    } else if (time < hour) {
        return (Math.floor(time / 60)) + "m " + (time % 60) + "s";
    } else if (time < day) {
        time = Math.floor(time / 60);
        return (Math.floor(time / 60)) + "h " + (time % 60) + "m";
    } else {
        var days = Math.floor(time / day);
        var hours = Math.floor((time % day) / hour);
        var minutes = Math.floor((time % hour) / minute);
        return days + "d " + hours + "h " + minutes + "m";
    }
};

pizza.formatUnixDate = function(unixTime) {
    var d = new Date(new Number(unixTime));
    return pizza.formatDate(d);
};

pizza.formatDate = function(dateTime) {
    var date = pizza.formatDate2(dateTime);
    return date.date + " " +  date.time;
};

pizza.formatDate2 = function(dateTime) {
    var date = [dateTime.getMonth() + 1, dateTime.getDate(), dateTime.getFullYear()];
    var time = [dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds()];
    var suffix = ( time[0] < 12 ) ? "AM" : "PM";
    time[0] = ( time[0] < 12 ) ? time[0] : time[0] - 12;
    time[0] = time[0] || 12;
    for ( var i = 1; i < 3; i++ ) {
        if ( time[i] < 10 ) {
            time[i] = "0" + time[i];
        }
    }

    var lang = navigator.userLanguage || navigator.language;
    if (lang.toLowerCase() != "en-us") {
        var tmp = date[0];
        date[0] = date[1];
        date[1] = tmp;
    }

    return { date: date.join("/"), time: time.join(":") + " " + suffix };
};

pizza.formatString = function(str, maxlen, def) {
    if (str == null || str == "") {
        str = def;
    }
    if (str.length > maxlen) {
        str = str.substr(0, maxlen-4) + "...";
    }
    return str;
};

pizza.isEmpty = function(obj) {
    if (Object.keys) {
        return Object.keys(obj).length === 0;
    } else {
        for (var prop in obj) {
            if (obj.hasOwnProperty(prop)) {
                return false;
            }
        }
        return true;
    }
};

pizza.inject = function(object, propArray, func) {
   for (var i = 0; i < propArray.length; ++i) {
       var prop = propArray[i];
       object[prop] = func(object[prop]);
   }
};

pizza.pushAll = function(a, b) {
    b.forEach(function(v) {a.push(v)}, a);
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

pizza.isFunction = function(v) {
    return (typeof v == 'function' || v instanceof Function);
};

pizza.setCookie = function(cookie, value, expiresDays) {
    if (!expiresDays) {
        expiresDays = 1;
    }
    var d = new Date();
    d.setTime(d.getTime() + (expiresDays*24*60*60*1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cookie + "=" + value + "; " + expires;
};

pizza.getCookie = function(cookie) {
    var n = cookie + "=";
    var cs = document.cookie.split(';');
    for(var i = 0; i < cs.length; i++) {
        var c = cs[i].trim();
        if (c.indexOf(n) == 0) {
            return c.substring(n.length,c.length);
        }
    }
    return "";
};

pizza.parseFloat = function(value) {
    if (/^(\-|\+)?([0-9]+(\.[0-9]+)?)$/.test(value)) {
        return Number(value);
    }
    return NaN;
};

pizza.parseInt = function(value) {
    if (/^(\-|\+)?([0-9]+)$/.test(value)) {
        return Number(value);
    }
    return NaN;
};

pizza.isValidEmail = function(value) {
    /* don't replace me with the RFC regex bro, 'invalid' emails are allowed by all email servers */
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
};

/**
 * Templating Library
 *
 * Supports loading very basic templates inline from the the HTML document.
 *
 * E.g.
 *
 * <script type="text/html" id="text-tip-template">
 *     <b>{{name}}</b>
 * </script>
 * var template = $("#text-tip-template").html();
 * var html = mtl.template(requestHeaderTemplate, { name: "Hello!" })
 * element.append(html);
 */
var mtl = mtl || {};

mtl.template = function(str, obj) {
    return str.replace(/{{([^}]+)}}/g, function(w, key) {
        if (!obj) {
            return "";
        }
        var v = $.trim(key).split('.');
        var s, o = obj;
        for (var i = 0; i < v.length; ++i) {
            s = o = o[v[i]];
            if (o === undefined) {
                return "";
            }
        }
        return s;
    });
};

mtl.loadTemplate = function(element, template, obj) {
    var templateElement = $(template);
    var html = templateElement.html();
    var text = mtl.template(html, obj);
    return $(element).html(text);
};

mtl.execTemplate = function(template, obj) {
    var templateElement = $(template);
    var html = templateElement.html();
    var text = mtl.template(html, obj);
    return $(text);
};

mtl.afterTemplate = function(element, template, obj) {
    var html = $(template).html();
    var text = mtl.template(html, obj);
    return $(element).after(text);
};

mtl.appendTemplate = function(element, template, obj) {
    var html = $(template).html();
    var text = mtl.template(html, obj);
    return $(element).append(text);
};

pizza.validator = function(form) {
    var validateList = [];

    function validateAll() {
        var ok = true;
        var firstErrorItem = null;
        for (var i = 0; i < validateList.length; ++i) {
            var o = validateList[i];
            if (o.input) {
                $(o.input).each(function (i, e) {
                    var item = updateErrorText($(this), o.validateFunc);
                    if (item) {
                        ok = false;
                        if (!firstErrorItem) {
                            firstErrorItem = item;
                        }
                    }
                });
            } else {
                var item = o.validateFunc();
                if (item) {
                    ok = false;
                    if (!firstErrorItem) {
                        firstErrorItem = item;
                    }
                }
            }
        }

        if (firstErrorItem) {
            firstErrorItem.focus();
        }

        return ok;
    }

    function updateErrorText(input, validateFunc, container) {
        if (!container) {
            container = input.parents(".feedback-item");
        }
        var error = validateFunc(input.val());
        container.find('.validation-error').remove();
        if (error) {
            container.addClass("has-error");
            var r = '<p class="help-block validation-error" style="margin-bottom: 0">' + error + '</p>';
            container.append(r);
            return input;
        } else {
            container.find('.validation-error').remove();
            container.removeClass("has-error");
            return null;
        }
    }

    function addActiveInputCheck(input, validateFunc) {
        validateList.push({
            input: input,
            validateFunc: validateFunc
        });

        $(input).change(function (event) {
            if (updateErrorText($(this), validateFunc)) {
                $(this).focus();
            }
        });
    }

    function addInputCheck(input, validateFunc) {
        validateList.push({
            input: input,
            validateFunc: validateFunc
        });
    }

    function addCheck(validateFunc) {
        validateList.push({
            validateFunc: validateFunc
        });
    }

    function validateOnSubmit(form) {
        form.on("submit", function() {
            try {
                return validateAll();
            } catch (e) {
                console.log(e);
                return false;
            }
        });
    }

    if (form) {
        validateOnSubmit(form);
    }

    return {
        addCheck: addCheck,
        addInputCheck: addInputCheck,
        addActiveInputCheck: addActiveInputCheck,
        validate: validateAll,
        validateOnSubmit: validateOnSubmit
    };
};


/**
 * Datatables render helpers.
 *
 * These function are highly LTGO specific!
 */

var dt = dt || {};

/**
 * Display a link, with a default name
 *
 * @param data the text to display
 * @param type the type of rendering (as per datatable's column.render function)
 * @param link the link address
 * @returns {*} the text to display
 */
dt.link = function(data, type, link, defaultName) {
    if (!defaultName) {
        defaultName = "not named";
    }
    if (type == "display") {
        if (!data) {
            data = "<i>" + defaultName + "</i>";
        }
        return "<a href=\"" + link + "\">" + data + "</a>";
    } else {
        if (!data) {
            data = defaultName;
        }
        return data;
    }
};

dt.linkIfSet = function(data, type, link) {
    if (!data) {
        return "";
    }
    if (type == "display") {
        return "<a href=\"" + link + "\">" + data + "</a>";
    } else {
        return data;
    }
};

dt.linkTruncate = function(data, type, link, defaultName) {
    if (!defaultName) {
        defaultName = "not named";
    }
    if (type == "display") {
        if (!data) {
            data = "<i>" + defaultName + "</i>";
        } else {
            data = pizza.formatString(data, 40, "n/a");
        }
        return "<a href=\"" + link + "\">" + data + "</a>";
    } else {
        if (!data) {
            data = defaultName;
        }
        return data;
    }
};

/**
 * Render a UNIX date in our default format
 * @param data the unix date (milliseconds from the UNIX epoch)
 * @param type the type of rendering (as per datatable's column.render function)
 * @param row the whole row (this is passed directly in by datatables.
 * @returns {*}
 */
dt.unixDate = function(data, type, row) {
    if (data > 0) {
        if (type == "display" || type == "filter") {
            return pizza.formatUnixDate(data);
        } else {
            return data;
        }
    } else {
        return "";
    }
};

dt.valid = function(data, type, row) {
    if (data) {
        return data;
    } else {
        return "";
    }
};

dt.timeAgo = function(data,type,row) {
    if (data > 0) {
        if (type == "display" || type == "filter") {
            return pizza.formatTimeAgo(data);
        } else {
            return data;
        }
    } else {
        return "";
    }
};

dt.timeSpan = function(data,type,row) {
    if (data > 0) {
        if (type == "display" || type == "filter") {
            return pizza.formatTimeSpan(data / 1000);
        } else {
            return data;
        }
    } else {
        return "";
    }
};
