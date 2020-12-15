pizza.main.input = function() {
    var EventType = {
        MousePressed: "mousePressed",
        MouseReleased: "mouseReleased",
        MouseMoved: "mouseMoved"
    };

    var MouseButton = {
        Left: "left",
        Middle: "middle",
        Right: "right",
        None: undefined
    };

    var ModifierMask = {
        AltKey: 1 << 0,
        ControlKey: 1 << 1,
        MetaKey: 1 << 2,
        ShiftKey: 1 << 3,
        NumLockKey: 1 << 4
    };

    var KeyEventType = {
        Char: "char",
        KeyDown: "keyDown",
        KeyUp: "keyUp",
        RawKeyDown: "rawKeyDown"
    };

    var Key = {
        Shift: 0x10,
        Control: 0x11,
        Alt: 0x12,
        Meta: 0x5b,
        LeftShift: 0xa0,
        RightShift: 0xa1,
        LeftControl: 0xa2,
        RightControl: 0xa3,
        LeftAlt: 0xa4,
        RightAlt: 0xa5,
        LeftMeta: 0x5b,
        RightMeta: 0x5c
    };

    var KEY_CHAR = 0; // The character typed
    var KEY_KEY = 1;  // The key name pressed according to JavaScript event
    var KEY_CODE = 2; // The key id pressed according to JavaScript event
    var KEY_MOD = 3;  // The modifier mask to apply
    var KEY_1252 = 4; // The code point of character.  See: https://en.wikipedia.org/wiki/Windows-1252

    var keyDefinitions = [
        ['\t', 'Tab', 9, 0, 9],
        ['\n', 'Enter', 13, 0, 13],
        [' ', 'Space', 32, 0, 32],
        ['!', 'Digit1', 49, ModifierMask.ShiftKey, 33],
        ['"', 'Quote', 222, ModifierMask.ShiftKey, 34],
        ['#', 'Digit3', 51, ModifierMask.ShiftKey, 35],
        ['$', 'Digit4', 52, ModifierMask.ShiftKey, 36],
        ['%', 'Digit5', 53, ModifierMask.ShiftKey, 37],
        ['^', 'Digit6', 54, ModifierMask.ShiftKey, 38],
        ['&', 'Digit7', 55, ModifierMask.ShiftKey, 39],
        ['\'', 'Quote', 222, 0, 39],
        ['(', 'Digit9', 57, ModifierMask.ShiftKey, 40],
        [')', 'Digit0', 48, ModifierMask.ShiftKey, 41],
        ['*', 'Digit8', 56, ModifierMask.ShiftKey, 42],
        ['+', 'Equal', 187, ModifierMask.ShiftKey, 43],
        [',', 'Comma', 188, 0, 44],
        ['-', 'Minus', 189, 0, 45],
        ['.', 'Period', 190, 0, 46],
        ['/', 'Slash', 191, 0, 47],
        ['0', 'Digit0', 48, 0, 48],
        ['1', 'Digit1', 49, 0, 49],
        ['2', 'Digit2', 50, 0, 50],
        ['3', 'Digit3', 51, 0, 51],
        ['4', 'Digit4', 52, 0, 52],
        ['5', 'Digit5', 53, 0, 53],
        ['6', 'Digit6', 54, 0, 54],
        ['7', 'Digit7', 55, 0, 55],
        ['8', 'Digit8', 56, 0, 56],
        ['9', 'Digit9', 57, 0, 57],
        [':', 'Semicolon', 59, ModifierMask.ShiftKey, 58],
        [';', 'Semicolon', 59, 0, 59],
        ['<', 'Comma', 188, ModifierMask.ShiftKey, 60],
        ['=', 'Equal', 187, 0, 61],
        ['>', 'Period', 190, ModifierMask.ShiftKey, 62],
        ['?', 'Slash', 191, ModifierMask.ShiftKey, 63],
        ['@', 'Digit2', 50, ModifierMask.ShiftKey, 64],
        ['a', 'KeyA', 65, 0, 97 ],
        ['A', 'KeyA', 65, ModifierMask.ShiftKey, 65 ],
        ['b', 'KeyB', 66, 0, 98 ],
        ['B', 'KeyB', 66, ModifierMask.ShiftKey, 66 ],
        ['c', 'KeyC', 67, 0, 99 ],
        ['C', 'KeyC', 67, ModifierMask.ShiftKey, 67 ],
        ['d', 'KeyD', 68, 0, 100 ],
        ['D', 'KeyD', 68, ModifierMask.ShiftKey, 68 ],
        ['e', 'KeyE', 69, 0, 101 ],
        ['E', 'KeyE', 69, ModifierMask.ShiftKey, 69 ],
        ['f', 'KeyF', 70, 0, 102 ],
        ['F', 'KeyF', 70, ModifierMask.ShiftKey, 70 ],
        ['g', 'KeyG', 71, 0, 103 ],
        ['G', 'KeyG', 71, ModifierMask.ShiftKey, 71 ],
        ['h', 'KeyH', 72, 0, 104 ],
        ['H', 'KeyH', 72, ModifierMask.ShiftKey, 72 ],
        ['i', 'KeyI', 73, 0, 105 ],
        ['I', 'KeyI', 73, ModifierMask.ShiftKey, 73 ],
        ['j', 'KeyJ', 74, 0, 106 ],
        ['J', 'KeyJ', 74, ModifierMask.ShiftKey, 74 ],
        ['k', 'KeyK', 75, 0, 107 ],
        ['K', 'KeyK', 75, ModifierMask.ShiftKey, 75 ],
        ['l', 'KeyL', 76, 0, 108 ],
        ['L', 'KeyL', 76, ModifierMask.ShiftKey, 76 ],
        ['m', 'KeyM', 77, 0, 109 ],
        ['M', 'KeyM', 77, ModifierMask.ShiftKey, 77 ],
        ['n', 'KeyN', 78, 0, 110 ],
        ['N', 'KeyN', 78, ModifierMask.ShiftKey, 78 ],
        ['o', 'KeyO', 79, 0, 111 ],
        ['O', 'KeyO', 79, ModifierMask.ShiftKey, 79 ],
        ['p', 'KeyP', 80, 0, 112 ],
        ['P', 'KeyP', 80, ModifierMask.ShiftKey, 80 ],
        ['q', 'KeyQ', 81, 0, 113 ],
        ['Q', 'KeyQ', 81, ModifierMask.ShiftKey, 81 ],
        ['r', 'KeyR', 82, 0, 114 ],
        ['R', 'KeyR', 82, ModifierMask.ShiftKey, 82 ],
        ['s', 'KeyS', 83, 0, 115 ],
        ['S', 'KeyS', 83, ModifierMask.ShiftKey, 83 ],
        ['t', 'KeyT', 84, 0, 116 ],
        ['T', 'KeyT', 84, ModifierMask.ShiftKey, 84 ],
        ['u', 'KeyU', 85, 0, 117 ],
        ['U', 'KeyU', 85, ModifierMask.ShiftKey, 85 ],
        ['v', 'KeyV', 86, 0, 118 ],
        ['V', 'KeyV', 86, ModifierMask.ShiftKey, 86 ],
        ['w', 'KeyW', 87, 0, 119 ],
        ['W', 'KeyW', 87, ModifierMask.ShiftKey, 87 ],
        ['x', 'KeyX', 88, 0, 120 ],
        ['X', 'KeyX', 88, ModifierMask.ShiftKey, 88 ],
        ['y', 'KeyY', 89, 0, 121 ],
        ['Y', 'KeyY', 89, ModifierMask.ShiftKey, 89 ],
        ['z', 'KeyZ', 90, 0, 122 ],
        ['Z', 'KeyZ', 90, ModifierMask.ShiftKey, 90 ],
        ['[', 'BracketLeft', 219, 0, 91],
        ['\\', 'Backslash', 220, 0, 92],
        [']', 'BracketRight', 221, 0, 93],
        ['_', 'Minus', 189, ModifierMask.ShiftKey, 95],
        ['`', 'Backquote', 192, 0, 96],
        ['{', 'BracketLeft', 219, ModifierMask.ShiftKey, 123],
        ['|', 'Backslash', 220, ModifierMask.ShiftKey, 124],
        ['}', 'BracketRight', 221, ModifierMask.ShiftKey, 125],
        ['~', 'Backquote', 192, ModifierMask.ShiftKey, 126],
    ];

    var charToKey = {};
    for (var i = 0; i < keyDefinitions.length; i++) {
        var key = keyDefinitions[i];
        if (key) {
            charToKey[key[KEY_CHAR]] = key;
        }
    }

    var keyDefinitionsNoChar = [
        [0, 'Shift', 0x10, ModifierMask.ShiftKey, 0x10],
        [0, 'Control', 0x11, ModifierMask.ControlKey, 0x11],
        [0, 'Alt', 0x12, ModifierMask.AltKey, 0x12],
        [0, 'LeftShift', 0xa0, ModifierMask.ShiftKey, 0xa0],
        [0, 'RightShift', 0xa1, ModifierMask.ShiftKey, 0xa1],
        [0, 'LeftControl', 0xa2, ModifierMask.ControlKey, 0xa2],
        [0, 'RightControl', 0xa3, ModifierMask.ControlKey, 0xa3],
        [0, 'LeftAlt', 0xa4, ModifierMask.AltKey, 0xa4],
        [0, 'RightAlt', 0xa5, ModifierMask.AltKey, 0xa5],
        [0, 'Meta', 0x5b, ModifierMask.LeftMeta, 0x5b],
        [0, 'LeftMeta', 0x5b, ModifierMask.MetaKey, 0x5b],
        [0, 'RightMeta', 0x5c, ModifierMask.MetaKey, 0x5c],
    ];

    var jsKeyCodeToKey = {};
    for (var i = 0; i < keyDefinitions.length; i++) {
        var key = keyDefinitions[i];
        if (key && key[KEY_MOD] === 0) {
            jsKeyCodeToKey[key[KEY_CODE]] = key;
        }
    }

    var _dispatchEvents = function(cmd, events, callback) {
        var i = 0;
        var dispatchEvent = function(response) {
            if (response && response.error) {
                callback({error: response.error});
            } else {
                if (i >= events.length) {
                    callback();
                } else {
                    var event = events[i++];
                    pizza.devtools.sendCommand(cmd, event, dispatchEvent);
                }
            }
        };

        dispatchEvent();
    };

    function mouseMove(x, y) {
        return {
            type: EventType.MouseMoved,
            x: x,
            y: y,
            modifiers: 0
        };
    }

    function mouseDown(button, x, y) {
        return {
            type: EventType.MousePressed,
            x: x,
            y: y,
            modifiers: 0,
            button: button,
            clickCount: 1
        };
    }

    function mouseUp(button, x, y) {
        return {
            type: EventType.MouseReleased,
            x: x,
            y: y,
            modifiers: 0,
            button: button,
            clickCount: 1
        };
    }

    var _click = function(x, y, callback) {
        // If we pass in decimal values the events will be discarded.
        x = Math.floor(x);
        y = Math.floor(y);

        var events = [
            mouseMove(x, y),
            mouseDown(pizza.input.MouseButton.Left, x, y),
            mouseUp(pizza.input.MouseButton.Left, x, y)
        ];

        pizza.input.dispatchEvents('Input.dispatchMouseEvent', events, callback);
    };

    var _mouseMove = function(x, y, callback) {
        // If we pass in decimal values the events will be discarded.
        x = Math.floor(x);
        y = Math.floor(y);

        var events = [
            mouseMove(x, y)
        ];

        pizza.input.dispatchEvents('Input.dispatchMouseEvent', events, callback);
    };

    function assignKeyIdentifiers(event, char, key) {
        if (char == '\n') {
            event.key = key[KEY_KEY];
        } else if (char == '\t') {
            event.key = key[KEY_KEY];
        } else {
            event.key = char;
        }
    }

    var keyChar = function(char, key, modifiers) {
        var text = char;
        if (key[KEY_CODE] === 13) {
            text = '\r';
        }
        var a = {
            type: KeyEventType.Char,
            text: text,
            modifiers: key[KEY_MOD],
            windowsVirtualKeyCode: key[KEY_1252],
            nativeVirtualKeyCode: key[KEY_1252],
            code: key[KEY_KEY]
        }
        assignKeyIdentifiers(a, char, key);
        // console.log("keyChar", a);
        return a;
    };

    var keyDown = function(char, key, modifiers) {
        var a = {
            type: KeyEventType.RawKeyDown,
            windowsVirtualKeyCode: key[KEY_CODE],
            nativeVirtualKeyCode: key[KEY_CODE],
            unmodifiedText: '',
            modifiers: key[KEY_MOD],
            code: key[KEY_KEY]
        };
        assignKeyIdentifiers(a, char, key);
        // console.log("keyDown", a);
        return a;
    };

    var keyUp = function(char, key, modifiers) {
        var a = {
            type: KeyEventType.KeyUp,
            windowsVirtualKeyCode: key[KEY_CODE],
            nativeVirtualKeyCode: key[KEY_CODE],
            unmodifiedText: '',
            modifiers: key[KEY_MOD],
            code: key[KEY_KEY]
        };
        assignKeyIdentifiers(a, char, key);
        // console.log("keyUp", a);
        return a;
    };

    function getKeyForChar(char) {
        var key = charToKey[char];
        if (!key) {
            key = [char, char, 0, 0, char.codePointAt(0)];
        }
        return key;
    }

    function getKeyForItem(item) {
        var key = jsKeyCodeToKey[item];
        if (!key) {
            key = [0, 0, item, 0, item];
        }
        return key;
    }

    function processKeys(events, text, modifiers) {
        var chars = Array.from(text); // split text by character
        console.log(chars, text);
        for (var i = 0; i < chars.length; i++) {
            var char = chars[i];
            var key = getKeyForChar(char);
            events.push(keyDown(char, key, modifiers));
            events.push(keyChar(char, key, modifiers));
            events.push(keyUp(char, key, modifiers));
        }
    }

    function processKeysNumberOrString(events, item, modifiers, keyDowns) {
        if (pizza.isString(item)) {
            processKeys(events, item, modifiers);
        } else if (pizza.isNumber(item)) {
            var key = getKeyForItem(item);
            var keyModifier = key[KEY_MOD];
            var char = key[KEY_CHAR];
            if (keyModifier !== 0) {
                var keyPressIndex = keyDowns.indexOf(item);
                if (keyPressIndex > -1) {
                    modifiers &= (~(keyModifier));
                    events.push(keyUp(char, key, modifiers));
                    keyDowns.slice(keyPressIndex, 1);
                } else {
                    modifiers |= keyModifier;
                    events.push(keyDown(char, key, modifiers));
                    keyDowns.push(item);
                }
            } else {
                events.push(keyDown(char, key, modifiers));
                if (modifiers === 0) {
                    events.push(keyChar(char, key, modifiers));
                }
                events.push(keyUp(char, key, modifiers));
            }
        }
        return modifiers;
    }

    function type(events, text) {
        var modifiers = 0;
        var keyDowns = [];
        var i = 0;
        if (pizza.isArray(text)) {
            for (i = 0; i < text.length; i++) {
                modifiers = processKeysNumberOrString(events, text[i], modifiers, keyDowns);
            }
        } else {
            processKeysNumberOrString(events, text, modifiers, keyDowns);
        }
        for (i = keyDowns.length - 1; i >= 0; --i) {
            var item = keyDowns[i];
            var key = getKeyForItem(item);
            var keyModifier = key[KEY_MOD];
            modifiers &= (~(keyModifier));
            events.push(keyUp(char, key, modifiers));
        }
    }

    var _type = function(text, callback) {
        var events = [];
        if (typeof text == 'array' || text instanceof Array) {
            for (var i = 0; i < text.length; ++i) {
                type(events, text[i]);
            }
        } else {
            type(events, text);
        }
        pizza.input.dispatchEvents('Input.dispatchKeyEvent', events, callback);
    };

    var _sendKey = function(key, callback) {
        var events = [];
        var modifiers = 0;
        processKey(events, key, modifiers);
        pizza.input.dispatchEvents('Input.dispatchKeyEvent', events, callback);
    };

    return {
        dispatchEvents: _dispatchEvents,
        EventType: EventType,
        MouseButton: MouseButton,
        ModifierMask: ModifierMask,
        click: _click,
        mouseMove: _mouseMove,
        type: _type,
        sendKey: _sendKey
    };
};
