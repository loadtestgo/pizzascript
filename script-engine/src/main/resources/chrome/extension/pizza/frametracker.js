//
// Track frame nodes as they are added to the DOM.
//
// The main purpose is to find DevTools frame ids for frame nodes.
// The frame id can be converted to an execution content id via
// contexttracker.
//
// Provides functions to querying frames in the current DOM.
//
// Uses Devtools DOM.* APIs
//
pizza.main.frametracker = function() {
    var _nodeIdToFrameId = [];
    var _frameIdToContentNodeId = [];
    var _frames = [];
    var _rootNodeId = null;
    var _tabId = null;

    var _documentUpdated = function(params) {
        _nodeIdToFrameId = [];
        _rootNodeId = null;
    };

    var _processNodeList = function(nodes) {
        for (var i = 0; i < nodes.length; ++i) {
            _processNode(nodes[i]);
        }
    };

    var _processNode = function(node) {
        if (node.frameId && node.nodeId) {
            _nodeIdToFrameId[node.nodeId] = node.frameId;
            _frames[node.nodeId] = node;
            _frameIdToContentNodeId[node.frameId] = node.contentDocument.nodeId;
        }

        if (node.children) {
            _processNodeList(node);
        }
    };

    var _setChildNodes = function(params) {
        _processNodeList(params.nodes);
    };

    var _getFrameIdForNodeId = function(nodeId) {
        return _nodeIdToFrameId[nodeId];
    };

    var _handleEvent = function(method, params) {
        switch (method) {
            case 'DOM.documentUpdated':
                _documentUpdated(params);
                break;
            case 'DOM.setChildNodes':
                _setChildNodes(params);
                break;
            default:
                // do nothing
                break;
        }
    };

    var _sendCommand = function(cmd, params, respondFunc) {
        params = params || {};
        chrome.debugger.sendCommand({ tabId: _tabId }, cmd, params,
            function (response) {
                if (response) {
                    respondFunc(response);
                } else {
                    respondFunc({ error: chrome.runtime.lastError });
                }
            });
    };

    var _getRootId = function(callback) {
        if (_rootNodeId) {
            callback(_rootNodeId);
        } else {
            _sendCommand('DOM.getDocument', {}, function(response) {
                console.log(response);
                if (response.root) {
                    console.log("rootId", response.root.nodeId);
                    _rootNodeId = response.root.nodeId;
                    callback(_rootNodeId);
                } else {
                    callback(null, response.error)
                }
            });
        }
    };

    var _copyAttributes = function(dest, props, src) {
        for (var i = 0; i < src.length; i += 2) {
            var val = src[i];
            if (props.indexOf(val) > 0) {
                dest[val] = src[i+1];
            }
        }
    };

    var _resolveFrameNode = function(nodeId, callback) {
        _sendCommand('DOM.resolveNode', { nodeId: nodeId }, function(response) {
            if (response.error) {
                callback( { error: response.error });
            } else {
                _sendCommand('DOM.requestNode', { objectId: response.object.objectId }, function(response) {
                    var frameId = _nodeIdToFrameId[nodeId];
                    if (frameId) {
                        var r = _frames[nodeId];
                        var value = {
                            type: r.localName,
                            frameId: r.frameId
                        };
                        _copyAttributes(value,
                            ["id", "name", "src", "width", "height", "style"],
                            r.attributes);
                        callback({ value: value });
                    } else {
                        callback({ error: "No frames matching selector" });
                    }
                });
            }
        });
    };

    var _resolveFrameNodes = function(nodeIds, callback) {
        var wait = pizza.waitAll();
        var elements = [];
        var errors = [];
        for (var i = 0; i < nodeIds.length; ++i) {
            _resolveFrameNode(nodeIds[i], wait.add(function(response) {
                if (response.value) {
                    elements.push(response.value);
                } else if (response.error) {
                    errors.push(response.error);
                }
            }));
        }
        wait.done(function() {
            var response = {};
            if (elements.length > 0) {
                response.value = elements;
            } else if (errors.length > 0) {
                response.error = errors;
            } else {
                response.value = [];
            }
            callback(response);
        });
    };

    var _listFramesForNode = function(selector, nodeId, callback) {
        _sendCommand('DOM.querySelectorAll', { nodeId: nodeId, selector: selector }, function(response) {
            if (response.nodeIds) {
                _resolveFrameNodes(response.nodeIds, callback);
            } else {
                callback({ error: "Element not found for selector: " + selector });
            }
        });
    };

    var _queryFramesNoOffset = function(selector, frameId, callback) {
        if (frameId) {
            _listFramesForNode(selector, _frameIdToContentNodeId[frameId], callback);
        } else {
            _getRootId(function(rootId) {
                _listFramesForNode(selector, rootId, callback);
            });
        }
    };

    var _splitOnSpacesWithEscapes = function(str) {
        var a = [];
        var start = 0;
        for (var i = 0; i < str.length; ++i) {
            var c = str.charAt(i);
            if (c === '\\') {
                ++i;
            } else if (c == ' ') {
                a.push(str.substring(start, i));
                start = i;
            }
        }
        if (str.length != 0) {
            a.push(str.substring(start));
        }
        return a;
    };

    var _parseIndexFromSelector = function(selector) {
        var m = selector.match(/^(.*):(eq|nth)\((\d+)\)$/);
        if (m) {
            return { css: m[1], index: Number(m[3]) };
        }
        return { css: selector, index: -1 };
    };

    var _queryFrameFrameSelector = function(selector, frameId, callback) {
        var selectorArray = _splitOnSpacesWithEscapes(selector);
        if (selectorArray.length === 0) {
            callback({ error: "zero length selector" });
        }

        var frame = null;

        var search = function(nodeId, i, callback) {
            var sel = _parseIndexFromSelector(selectorArray[i]);
            _sendCommand('DOM.querySelectorAll',
                { nodeId: nodeId, selector: sel.css },
                function(response) {
                    if (response.nodeIds) {
                        _resolveFrameNodes(response.nodeIds, function (response) {
                            if (!response.value) {
                                callback();
                            }

                            if (sel.index > -1) {
                                if (response.value.length > sel.index) {
                                    response.value = [response.value[sel.index]];
                                }
                            }

                            i++;

                            if (i >= selectorArray.length && response.value.length > 0) {
                                if (frame == null) {
                                    frame = response.value[0];
                                }
                                callback();
                            } else {
                                async.each(response.value, function (value, callback) {
                                    search(_frameIdToContentNodeId[value.frameId], i, callback);
                                }, function (r) {
                                    callback(r);
                                });
                            }
                        });
                    } else {
                        callback({ error: response });
                    }
                });
        };

        var respondWithResults = function(err) {
            if (err) {
                callback({ error: err });
            } else if (!frame) {
                callback({ error: "no frame matching selector" });
            } else {
                callback({ value: frame })
            }
        };

        if (frameId) {
            search(_frameIdToContentNodeId[frameId], 0, respondWithResults);
        } else {
            _getRootId(function(rootId) {
                search(rootId, 0, respondWithResults);
            });
        }
    };

    var _queryFrame = function(selector, frameId, callback) {
        var sel = _parseIndexFromSelector(selector);
        // If there's no index on the selector, just pick the first one
        if (sel.index < 0) {
            sel.index = 0;
        }
        _queryFramesNoOffset(sel.css, frameId, function(response) {
            if (response.value) {
                if (response.value.length > sel.index) {
                    var frame = response.value[sel.index];
                    callback({ value: frame });
                } else {
                    callback({ error: "frame index out of bounds, found only " + response.value.length });
                }
            } else {
                callback(response);
            }
        });
    };

    var _queryFrames = function(selector, frameId, callback) {
        var sel = _parseIndexFromSelector(selector);
        _queryFramesNoOffset(sel.css, frameId, function(response) {
            if (response.value) {
                if (sel.index === -1) {
                    callback(response);
                } else if (response.value.length > sel.index) {
                    var frame = response.value[sel.index];
                    callback({ value: [frame] });
                } else {
                    callback({ error: "frame index out of bounds, found only " + response.value.length });
                }
            } else {
                callback(response);
            }
        });
    };

    var _queryFramesRecursive = function(selector, nodeId, callback) {
        var wait = pizza.waitAll();
        var errors = [];
        var querySelector = function(nodeId, parent) {
            _sendCommand('DOM.querySelectorAll',
                         { nodeId: nodeId, selector: selector },
                         wait.add(function(response) {
                if (response.nodeIds) {
                    var nodeIds = response.nodeIds;
                    if (nodeIds.length === 0) {
                        return;
                    }
                    var elements = null;
                    if (parent instanceof Array) {
                        elements = parent;
                    } else {
                        parent.frames = [];
                        elements = parent.frames;
                    }
                    for (var i = 0; i < nodeIds.length; ++i) {
                        _resolveFrameNode(nodeIds[i], wait.add(function(response) {
                            if (response.value) {
                                elements.push(response.value);
                                querySelector(_frameIdToContentNodeId[response.value.frameId], response.value);
                            } else if (response.error) {
                                errors.push(response.error);
                            }
                        }));
                    }
                } else {
                    errors.push({ error: "Elements not found for selector: " + selector });
                }
            }));
        };
        var elements = [];
        querySelector(nodeId, elements);
        wait.done(function() {
            var response = {};
            if (elements.length > 0) {
                response.value = elements;
            } else if (errors.length > 0) {
                response.error = errors;
            } else {
                response.value = [];
            }
            callback(response);
        });
    };

    var _highlight = function(objectId, color, callback) {
        _getRootId(function(rootId) {
            _sendCommand('DOM.requestNode', { objectId: objectId }, function(response) {
                var borderColor = { r: 0, g: 0, b: 0 };
                _sendCommand('DOM.highlightNode',
                    { nodeId: response.nodeId,
                      highlightConfig:
                        { showInfo: true,
                          contentColor: color,
                          borderColor: borderColor,
                          marginColor: borderColor } },
                    function(response) { callback(response); });
            });
        });
    };

    var _clearHighlight = function() {
        _sendCommand('DOM.hideHighlight', {}, function() {});
    };

    var _setFileInputFiles = function(objectId, files, callback) {
        _getRootId(function(rootId) {
            _sendCommand('DOM.requestNode', { objectId: objectId },
                function(response) {
                    _sendCommand('DOM.setFileInputFiles',
                        { nodeId: response.nodeId,
                            files: files },
                        function (result) {
                            callback(result);
                        });
                });
        });
    };

    var _queryAllFrames = function(selector, callback) {
        _getRootId(function(rootId) {
            _queryFramesRecursive(selector, rootId, callback);
        });
    };

    var _setTab = function(tabId) {
        _tabId = tabId;
    };

    return {
        handleEvent: _handleEvent,
        getFrameIdForNodeId: _getFrameIdForNodeId,
        queryFrame: _queryFrame,
        queryFrameFrameSelector: _queryFrameFrameSelector,
        queryFrames: _queryFrames,
        queryAllFrames: _queryAllFrames,
        highlight: _highlight,
        clearHighlight: _clearHighlight,
        setFileInputFiles: _setFileInputFiles,
        setTab: _setTab
    };
};

