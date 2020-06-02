/**
 * This file must be standalone as it is injected into pages as needed
 */
pizza.automation = {
    uniqueId: '$pza_liueqb38vn2s023b',

    /**
     * the 'init' function injected into pages when certain automation APIs are
     * invoked.
     */
    init: function () {
        var StatusCode = {
            STALE_ELEMENT_REFERENCE: 10,
            UNKNOWN_ERROR: 13
        };

        var DomNodeType = {
            ELEMENT: 1,
            DOCUMENT: 9
        };

        var ELEMENT_KEY = 'ELEMENT';

        var XPATH_PREFIX = 'xpath:';
        var XPATH_PREFIX_2 = '//';

        function Cache() {
            this.cache_ = {};
            this.nextId_ = 1;
            this.idPrefix_ = Math.random().toString();
        }

        Cache.prototype = {
            storeItem: function(item) {
                for (var i in this.cache_) {
                    if (item == this.cache_[i])
                        return i;
                }
                var id = this.idPrefix_  + '-' + this.nextId_;
                this.cache_[id] = item;
                this.nextId_++;
                return id;
            },

            retrieveItem: function(id) {
                var item = this.cache_[id];
                if (item)
                    return item;
                var error = new Error('not in cache');
                error.code = StatusCode.STALE_ELEMENT_REFERENCE;
                error.message = 'element is not attached to the page document';
                throw error;
            },

            clearStale: function() {
                for (var id in this.cache_) {
                    var node = this.cache_[id];
                    if (!this.isNodeReachable_(node)) {
                        delete this.cache_[id];
                    }
                }
            },

            isNodeReachable_: function(node) {
                var nodeRoot = getNodeRoot(node);
                return (nodeRoot == document);
            }
        };

        function getNodeRoot(node) {
            while (node.parentNode) {
                node = node.parentNode;
            }
            return node;
        }

        function getPageCache(opt_doc) {
            var doc = opt_doc || document;
            var key = uniqueId;
            if (!(key in doc))
                doc[key] = new Cache();
            return doc[key];
        }

        function wrap(value) {
            if (typeof(value) == 'object' && value != null) {
                var nodeType = value['nodeType'];
                if (nodeType == DomNodeType.ELEMENT || nodeType == DomNodeType.DOCUMENT) {
                    var wrapped = {};
                    var root = getNodeRoot(value);
                    wrapped[ELEMENT_KEY] = getPageCache(root).storeItem(value);
                    return wrapped;
                }

                var obj = (typeof(value.length) == 'number') ? [] : {};
                for (var prop in value)
                    obj[prop] = wrap(value[prop]);
                return obj;
            }
            return value;
        }

        function unwrap(value, cache) {
            if (typeof(value) == 'object' && value != null) {
                if (ELEMENT_KEY in value) {
                    return cache.retrieveItem(value[ELEMENT_KEY]);
                }

                var obj = (typeof(value.length) == 'number') ? [] : {};
                for (var prop in value) {
                    obj[prop] = unwrap(value[prop], cache);
                }
                return obj;
            }
            return value;
        }

        var calcElementMidPosition = function(region) {
            var left = region.left;
            var top = region.top;
            var height = region.height;
            var width = region.width;
            if (left < 0) {
                width += left;
                left = 0;
                if (width < 0) {
                    width = 0;
                }
            }
            if (top < 0) {
                height += top;
                top = 0;
                if (height < 0) {
                    height = 0;
                }
            }
            var x = left + width / 2;
            var y = top + height / 2;
            return {x: Math.floor(x), y: Math.floor(y)};
        };

        /**
         * Find element that hides the given element if any
         */
        function findTopElement(element, region) {
            var pos = calcElementMidPosition(region);
            if (pos.x < 0 || pos.y < 0) {
                console.log(region, pos);
                return null;
            }
            var top = document.elementFromPoint(pos.x, pos.y);
            if (top && top !== element) {
                var parent = top;
                while (true) {
                    parent = parent.parentNode;
                    if (!parent) {
                        return getElementSelector(top);
                    } else if (parent === element) {
                        return null;
                    }
                }
            }
            return null;
        }

        var _moveElementOnScreenAndGetRegion = function(selector) {
            var element = _findElement(selector);

            // find the element and scroll into view if necessary
            element.scrollIntoView(false);

            // now get updated region, and check that the element is
            // not hidden by other elements
            var region = getElementRegion(element, selector);
            var e = findTopElement(element, region);
            if (e) {
                throw {
                    type: "HiddenByElement",
                    message: "Element '" + selector +"' hidden by '" + e +  "'"
                };
            }

            return region;
        };

        // Find a bounding box for the element.  Element should be directly under center.
        // There can be shapes other than rectangles for elements and these won't be fully fit
        // the bounding box, and may actually have multiple bounding boxes stored for them.
        // There is an assumption that elements are rectangular in other parts of the code
        // that call this function, and while this is largely true it won't work in the
        // general case for SVG elements.  That's something that should be worked on as
        // needed, its better to make sure the existing click() / hover() / visibility code
        // works right now.
        function getElementRegion(element, selector) {
            // We try 2 methods to determine element region. Try the first client rect,
            // and then the bounding client rect.
            // SVG is one case that doesn't have a first client rect.
            var clientRects = element.getClientRects();
            if (clientRects.length === 0) {
                var box = element.getBoundingClientRect();
                if (box.width === 0 && box.height === 0) {
                    throw {
                        type: "EmptyBoundingBox",
                        message: "Element '" + selector + "' found but not visible"
                    };
                }
                if (element.tagName.toLowerCase() === 'area') {
                    var coords = element.coords.split(',');
                    if (element.shape.toLowerCase() === 'rect') {
                        if (coords.length !== 4) {
                            throw {
                                type: "InvalidAreaBoundingBox",
                                message: "Failed to detect the region of the area for element '" + selector + "'"
                            };
                        }
                        var leftX = Number(coords[0]);
                        var topY = Number(coords[1]);
                        var rightX = Number(coords[2]);
                        var bottomY = Number(coords[3]);
                        return {
                            'left': leftX,
                            'top': topY,
                            'width': rightX - leftX,
                            'height': bottomY - topY
                        };
                    } else if (element.shape.toLowerCase() === 'circle') {
                        if (coords.length !== 3) {
                            throw {
                                type: "InvalidCircleBoundingBox",
                                message: "Failed to detect the region of the area for element '" + selector + "'"
                            };
                        }
                        var centerX = Number(coords[0]);
                        var centerY = Number(coords[1]);
                        var radius = Number(coords[2]);
                        return {
                            'left': Math.max(0, centerX - radius),
                            'top': Math.max(0, centerY - radius),
                            'width': radius * 2,
                            'height': radius * 2
                        };
                    } else if (element.shape.toLowerCase() === 'poly') {
                        if (coords.length < 2) {
                            throw {
                                type: "InvalidPolyBoundingBox",
                                message: "Failed to detect the region of the area for element '" + selector + "'"
                            };
                        }
                        var minX = Number(coords[0]);
                        var minY = Number(coords[1]);
                        var maxX = minX;
                        var maxY = minY;
                        for (i = 2; i < coords.length; i += 2) {
                            var x = Number(coords[i]);
                            var y = Number(coords[i + 1]);
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                        return {
                            'left': minX,
                            'top': minY,
                            'width': maxX - minX,
                            'height': maxY - minY
                        };
                    } else {
                        throw {
                            type: "UnknownShape",
                            message: "Element '" + selector + "' shape=" + element.shape + " is not supported"
                        };
                    }
                }
                return {
                    'left': box.left,
                    'top': box.top,
                    'width': box.width,
                    'height': box.height
                };
            } else {
                var clientRect = clientRects[0];
                return {
                    'left': clientRect.left,
                    'top': clientRect.top,
                    'width': clientRect.right - clientRect.left,
                    'height': clientRect.bottom - clientRect.top
                };
            }
        }

        function isXPath(selector) {
            if (selector.slice(0, XPATH_PREFIX.length) === XPATH_PREFIX) {
                return true;
            }

            if (selector.slice(0, XPATH_PREFIX_2.length) === XPATH_PREFIX_2) {
                return true;
            }

            return false;
        }

        function getXPath(selector) {
            if (selector.slice(0, XPATH_PREFIX.length) === XPATH_PREFIX) {
                return selector.slice(XPATH_PREFIX.length, selector.length);
            }

            return selector;
        }

        var _findElement = function(selector) {
            if (isXPath(selector)) {
                return findElementXpath(selector);
            } else {
                var items = findElementsCss(selector);

                if (items.length == 0) {
                    throw "Unable to find element for selector \'" + selector + "\'!";
                }

                return items[0];
            }
        };

        function findElementXpath(selector) {
            var xpath = getXPath(selector);
            var result = null;

            try {
                result = document.evaluate(
                    xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
            } catch (e) {
                var message = e.message;
                throw message.replace("Failed to execute 'evaluate' on 'Document': ", "");
            }

            if (result == null) {
                throw "Unable to find element for xpath \'" + xpath + "\'!";
            }
            return result;
        }

        function escapeRegExp(str) {
            return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        }

        function findElementsCss(selector) {
            var commands = parseSelectorCommands(selector);
            var items = [];
            var e = null;
            var j = 0;
            var t = null;
            try {
                for (var i = 0; i < commands.length; ++i) {
                    var cmd = commands[i];
                    var newItems = [];

                    switch (cmd.cmd) {
                        case 'contains':
                            for (j = 0; j < items.length; ++j) {
                                e = items[j];
                                t = e.innerText;
                                if (t && t.indexOf(cmd.params) >= 0) {
                                    newItems.push(e);
                                }
                            }
                            break;
                        case 'icontains':
                            var reg = RegExp(escapeRegExp(cmd.params), "i");
                            for (j = 0; j < items.length; ++j) {
                                e = items[j];
                                t = e.innerText;
                                if (t && reg.test(t)) {
                                    newItems.push(e);
                                }
                            }
                            break;
                        case 'nth':
                        case 'eq':
                            if (cmd.params >= 0 && cmd.params < items.length) {
                                newItems.push(items[cmd.params]);
                            }
                            break;
                        case 'css':
                            if (i == 0) {
                                newItems = document.querySelectorAll(cmd.params);
                            } else {
                                for (j = 0; j < items.length; ++j) {
                                    e = items[j];
                                    newItems = newItems.concat(e.querySelectorAll(cmd.params));
                                }
                            }
                            break;
                        case 'id':
                            if (i == 0) {
                                e = document.getElementById(cmd.params);
                                if (e) {
                                    newItems = [e];
                                } else {
                                    newItems = [];
                                }
                            } else {
                                for (j = 0; j < items.length; ++j) {
                                    e = items[j];
                                    newItems = newItems.concat(e.getElementById(cmd.params));
                                }
                            }
                            break;
                    }
                    items = newItems;
                    if (items.length == 0) {
                        break;
                    }
                }
            } catch (e) {
                var message = e.message;
                throw message.replace("Failed to execute 'querySelectorAll' on 'Document': ", "");
            }
            return items;
        }

        var _findElementAll = function(selector) {
            if (isXPath(selector)) {
                return findElementsXpath(selector);
            } else {
                return findElementsCss(selector);
            }
        };

        function findElementsXpath(selector) {
            var xpath = getXPath(selector);
            var result = [];

            try {
                var it = document.evaluate(
                    xpath, document, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
                var next = null;
                do {
                    next = it.iterateNext();
                    if (next != null) {
                        result.push(next);
                    }
                } while (next != null);
            } catch (e) {
                var message = e.message;
                throw message.replace("Failed to execute 'evaluate' on 'Document': ", "");
            }

            if (result.length == 0) {
                throw "Unable to find element for xpath \'" + xpath + "\'!";
            }

            return result;
        }

        var _elementExists = function(selector) {
            if (isXPath(selector)) {
                return elementXpathExists(selector);
            } else {
                return elementCssExists(selector);
            }
        };

        function elementXpathExists(selector) {
            var xpath = getXPath(selector);
            var result = null;

            try {
                result = document.evaluate(
                    xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
            } catch (e) {
                var message = e.message;
                throw new Error(message.replace("Failed to execute 'evaluate' on 'Document': ", ""));
            }

            return (result != null);
        }

        function elementCssExists(selector) {
            var items = findElementsCss(selector);
            return (items.length > 0);
        }

        function parseSelectorCommands(selector) {
            var commandArray = [];
            var last = 0;
            var open = -1, end = -1;
            var cmd = null;
            var param = null;
            var skipNext = false;
            var css = "";
            var i = 0;
            var State = {
                BASE : 0,
                ID : 1,
                ID_NUM : 2
            };
            var state = State.BASE;
            function pushCommand() {
                if (state == State.ID_NUM) {
                    commandArray.push({ cmd: 'id', params: selector.substring(last, i) });
                } else if (cmd == 'contains' || cmd == 'icontains' || cmd == 'nth' || cmd == 'eq') {
                    commandArray.push({ cmd: cmd, params: param });
                } else {
                    commandArray.push({ cmd: 'css', params: selector.substring(last, i) });
                }
                last = i;
                cmd = null;
                param = null;
            }
            var splitters = ['>', ',', '*', ":", " ", "\t"];
            for (; i < selector.length; ++i) {
                if (skipNext) {
                    continue;
                }
                var v = selector[i];
                if (state == State.ID) {
                    if (v >= '0' && v <= '9') {
                        last = i;
                        state = State.ID_NUM;
                    } else {
                        state = State.BASE;
                    }
                } else if (state == State.ID_NUM) {
                    if (splitters.indexOf(v) >= 0) {
                        pushCommand();
                        state = State.BASE;
                    }
                } else {
                    if (v == '\\') {
                        skipNext = true;
                    } else if (v == ':') {
                        pushCommand();
                    } else if (v == '(') {
                        cmd = selector.substring(last + 1, i);
                        open = i;
                    } else if (v == ')') {
                        param = selector.substring(open + 1, i);
                        end = i;
                    } else if (v == '#') {
                        state = State.ID;
                    }
                }
            }
            pushCommand();
            return commandArray;
        }

        // Focus the target element in order to send keys to it.
        var _focus = function(selector) {
            var element = _findElement(selector);
            var doc = element.ownerDocument || element;
            var prevActiveElement = doc.activeElement;
            // Blur the previously selected element
            if (element != prevActiveElement && prevActiveElement) {
                prevActiveElement.blur();
            }
            // Focus our target element.
            element.focus();
            // Select all the text, so that it is removed when we start typing
            if (element != prevActiveElement && element.value &&
                element.value.length && element.setSelectionRange) {
                try {
                    element.setSelectionRange(0, element.value.length);
                } catch (error) {
                    if (!(error instanceof DOMException && error.code == DOMException.INVALID_STATE_ERR)) {
                        throw error;
                    }
                }
            }
            // Sanity check that the new active element is the target element
            if (element !== doc.activeElement) {
                throw new Error('cannot focus element');
            }
        };

        // Submit the target element if it is a form
        // otherwise walk the DOM hierarchy looking for the first parent form and submit
        // that
        var _submitForm = function(selector) {
            var element = _findElement(selector);
            if (element.tagName === 'FORM') {
                element.submit();
                return;
            } else {
                while (element.parentNode) {
                    element = element.parentNode;
                    if (element.tagName === 'FORM') {
                        element.submit();
                        return;
                    }
                }
            }
            throw 'Unable to find form for selector \'' + selector + '\'';
        };

        /**
         * Calls a given function with element id wrapping.
         *
         * This allows elements to passed back and forth between the caller and the
         * web frame that this script is running in.
         *
         * The inputs to and outputs of the function will be unwrapped and wrapped
         * respectively, unless otherwise specified. This wrapping involves converting
         * between cached object reference IDs and actual JS objects. The cache will
         * automatically be pruned each call to remove stale references.
         *
         * @param {function(...[*]) : *} func The function to invoke.
         * @param {!Array.<*>} args The array of arguments to supply to the function,
         *     which will be unwrapped before invoking the function.
         * @param {boolean=} opt_unwrappedReturn Whether the function's return value
         *     should be left unwrapped.
         * @return {*} An object containing a status and value property, where status
         *     is a WebDriver status code and value is the wrapped value. If an
         *     unwrapped return was specified, this will be the function's pure return
         *     value.
         */
        var _callFunction = function(func, args, opt_unwrappedReturn) {
            var cache = getPageCache();
            cache.clearStale();

            if (opt_unwrappedReturn) {
                return func.apply(null, unwrap(args, cache));
            }

            var status = 0;
            var returnValue = null;
            try {
                returnValue = wrap(func.apply(null, unwrap(args, cache)));
            } catch (error) {
                status = error.code || StatusCode.UNKNOWN_ERROR;
                returnValue = error.message;
            }
            return {
                status: status,
                value: returnValue
            }
        };

        function getElementSelector(element) {
            var tag = null;
            var matches = null;
            var foundId = false;
            var i = 0;
            for (var e = element; e && e != document.body; e = e.parentElement) {
                var add = e.tagName.toLowerCase();
                if (e.id) {
                    add = '#' + e.id;
                    foundId = true;
                } else if (e.classList) {
                    for (i = 0; i < e.classList.length; ++i) {
                        add += "." + e.classList[i];
                    }
                }

                if (tag) {
                    tag = add + ' > ' + tag;
                } else {
                    tag = add;
                }

                try {
                    matches = document.querySelectorAll(tag);
                    if (matches) {
                        if (matches.length == 1) {
                            return tag;
                        } else if (foundId && matches.length > 0) {
                            for (i = 0; i < matches.length; ++i) {
                                if (matches[i] == element) {
                                    if (i > 0) {
                                        tag += ":nth(" + i + ")";
                                    }
                                    break;
                                }
                            }
                            return tag;
                        }
                    }
                } catch (e) {
                    // HTML allows you to set ids that are not valid selectors,
                    // we should have a fallback for that...
                }
            }

            return tag;
        }

        function getElementPath(element) {
            var tag = null;
            for (var e = element; e && e !== document.body; e = e.parentElement) {
                var add = e.tagName.toLowerCase();
                if (e.id) {
                    add += '#' + e.id;
                }

                if (e.classList) {
                    for (var i = 0; i < e.classList.length; ++i) {
                        add += "." + e.classList[i];
                    }
                }

                if (tag) {
                    tag = add + ' > ' + tag;
                } else {
                    tag = add;
                }
            }

            return tag;
        }

        var _queryElementWrap = function(element) {
            var v = { tag: element.tagName.toLowerCase(), attributes: {} };
            for (var i = 0; i < element.attributes.length; i++) {
                var a = element.attributes[i];
                v.attributes[a.name] = a.value;
            }

            var sel = getElementSelector(element);
            if (sel) {
                v.selector = sel;
            }
            sel = getElementPath(element);
            if (sel) {
                v.path = sel;
            }

            // Output some useful visibility info
            v.visible = false;
            try {
                v.region = getElementRegion(element);
                var e = findTopElement(element, v.region);
                if (e) {
                    v.hiddenBy = e;
                } else {
                    v.visible = true;
                }
            } catch (e) {
                // got exception trying to find visible area,
                // v.visible is false by default
            }

            var style = window.getComputedStyle(element);
            if (style.visibility === 'hidden') {
                v.visibility = style.visibility;
            }
            if (!v.region) {
                if (style.display === 'none') {
                    v.display = style.display;
                } else {
                    var parent = element;
                    while (true) {
                        parent = parent.parentNode;
                        if (!parent) {
                            break;
                        }
                        var ps = window.getComputedStyle(parent);
                        if (ps && ps.display === 'none') {
                            v.hiddenParent = getElementSelector(parent);
                            break;
                        }
                    }
                }
            }
            if (style.zIndex !== 'auto') {
                v.zIndex = style.zIndex;
            }
            var t = element.innerText;
            if (t) {
                if (t.length > 50) {
                    v.text = t.substr(0, 50) + "...";
                } else {
                    v.text = t;
                }
            }

            return v;
        };

        return {
            StatusCode: StatusCode,
            ELEMENT_KEY: ELEMENT_KEY,
            callFunction: _callFunction,
            findElement: _findElement,
            findElementAll: _findElementAll,
            queryElementWrap: _queryElementWrap,
            elementExists: _elementExists,
            submitForm: _submitForm,
            focus: _focus,
            moveElementOnScreenAndGetRegion: _moveElementOnScreenAndGetRegion
        };
    }
};