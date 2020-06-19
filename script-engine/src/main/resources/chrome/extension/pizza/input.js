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

    var char2key = {
        '\t': 9,
        '\n': 13,
        ' ': 32,
        '0': 48,
        '1': 49,
        '2': 50,
        '3': 51,
        '4': 52,
        '5': 53,
        '6': 54,
        '7': 55,
        '8': 56,
        '9': 57,
        ':': 58,
        ';': 59,
        '<': 60,
        '=': 61,
        '>': 62,
        '?': 63,
        '@': 64,
        'a': 65,
        'A': 65,
        'b': 66,
        'B': 66,
        'c': 67,
        'C': 67,
        'd': 68,
        'D': 68,
        'e': 69,
        'E': 69,
        'f': 70,
        'F': 70,
        'g': 71,
        'G': 71,
        'h': 72,
        'H': 72,
        'i': 73,
        'I': 73,
        'j': 74,
        'J': 74,
        'k': 75,
        'K': 75,
        'l': 76,
        'L': 76,
        'm': 77,
        'M': 77,
        'n': 78,
        'N': 78,
        'o': 79,
        'O': 79,
        'p': 80,
        'P': 80,
        'q': 81,
        'Q': 81,
        'r': 82,
        'R': 82,
        's': 83,
        'S': 83,
        't': 84,
        'T': 84,
        'u': 85,
        'U': 85,
        'v': 86,
        'V': 86,
        'w': 87,
        'W': 87,
        'x': 88,
        'X': 88,
        'y': 89,
        'Y': 89,
        'z': 90,
        'Z': 90,
        '^': 160,
        '!': 161,
        '"': 162,
        '#': 163,
        '$': 164,
        '%': 165,
        '&': 166,
        '_': 167,
        '(': 168,
        ')': 169,
        '*': 170,
        '+': 171,
        '|': 172,
        '-': 173,
        '{': 174,
        '}': 175,
        '~': 176,
        ',': 188,
        '.': 190,
        '/': 191,
        '`': 192,
        '[': 219,
        '\\': 220,
        ']': 221,
        '\'': 222
    };

    // This is not a straight reverse of the char2key list,
    // There's overloading in both (many ids map to same values).
    var key2char = {
        9: '\t',
        13: '\n',
        32: ' ',
        48: '0',
        49: '1',
        50: '2',
        51: '3',
        52: '4',
        53: '5',
        54: '6',
        55: '7',
        56: '8',
        57: '9',
        58: ':',
        59: ';',
        60: '<',
        61: '=',
        62: '>',
        63: '?',
        64: '@',
        65: 'A',
        66: 'B',
        67: 'C',
        68: 'D',
        69: 'E',
        70: 'F',
        71: 'G',
        72: 'H',
        73: 'I',
        74: 'J',
        75: 'K',
        76: 'L',
        77: 'M',
        78: 'N',
        79: 'O',
        80: 'P',
        81: 'Q',
        82: 'R',
        83: 'S',
        84: 'T',
        85: 'U',
        86: 'V',
        87: 'W',
        88: 'X',
        89: 'Y',
        90: 'Z',
        96: '0',
        97: '1',
        98: '2',
        99: '3',
        10: '4',
        101: '5',
        102: '6',
        103: '7',
        104: '8',
        105: '9',
        106: '*',
        107: '+',
        109: '-',
        110: '.',
        111: '/',
        160: '^',
        161: '!',
        162: '"',
        163: '#',
        164: '$',
        165: '%',
        166: '&',
        167: '_',
        168: '(',
        169: ')',
        170: '*',
        171: '+',
        172: '|',
        173: '-',
        174: '{',
        175: '}',
        176: '~',
        188: ',',
        190: '.',
        191: '/',
        192: '`',
        219: '[',
        220: '\\',
        221: ']',
        222: '\''
    };

    // US keyboard
    var char2DOMCodeUSKeyboard = {
        '\t': 'Tab',
        '\n': 'Enter',
        ' ': 'Space',
        '0': 'Digit0',
        '1': 'Digit1',
        '2': 'Digit2',
        '3': 'Digit3',
        '4': 'Digit4',
        '5': 'Digit5',
        '6': 'Digit6',
        '7': 'Digit7',
        '8': 'Digit8',
        '9': 'Digit9',
        ':': 'Semicolon', // +shift
        ';': 'Semicolon',
        '<': 'Comma', // +shift
        '=': 'Equal',
        '>': 'Period', // +shift
        '?': 'Slash', // +shift
        '@': 'Digit2', // +shift
        'a': 'KeyA',
        'A': 'KeyA',
        'b': 'KeyB',
        'B': 'KeyB',
        'c': 'KeyC',
        'C': 'KeyC',
        'd': 'KeyD',
        'D': 'KeyD',
        'e': 'KeyE',
        'E': 'KeyE',
        'f': 'KeyF',
        'F': 'KeyF',
        'g': 'KeyG',
        'G': 'KeyG',
        'h': 'KeyH',
        'H': 'KeyH',
        'i': 'KeyI',
        'I': 'KeyI',
        'j': 'KeyJ',
        'J': 'KeyJ',
        'k': 'KeyK',
        'K': 'KeyK',
        'l': 'KeyL',
        'L': 'KeyL',
        'm': 'KeyM',
        'M': 'KeyM',
        'n': 'KeyN',
        'N': 'KeyN',
        'o': 'KeyO',
        'O': 'KeyO',
        'p': 'KeyP',
        'P': 'KeyP',
        'q': 'KeyQ',
        'Q': 'KeyQ',
        'r': 'KeyR',
        'R': 'KeyR',
        's': 'KeyS',
        'S': 'KeyS',
        't': 'KeyT',
        'T': 'KeyT',
        'u': 'KeyU',
        'U': 'KeyU',
        'v': 'KeyV',
        'V': 'KeyV',
        'w': 'KeyW',
        'W': 'KeyW',
        'x': 'KeyX',
        'X': 'KeyX',
        'y': 'KeyY',
        'Y': 'KeyY',
        'z': 'KeyZ',
        'Z': 'KeyZ',
        '^': 'Digit6', // +shift
        '!': 'Digit1', // +shift
        '"': 'Quote', // +shift
        '#': 'Digit3', // +shift
        '$': 'Digit4', // +shift
        '%': 'Digit5', // +shift
        '&': 'Digit7', // +shift
        '_': 'Minus', // +shift
        '(': 'Digit9', // +shift
        ')': 'Digit0', // +shift
        '*': 'Digit8', // +shift
        '+': 'Equal', // +shift
        '|': 'Backslash', // +shift
        '-': 'Minus',
        '{': 'BracketLeft',
        '}': 'BracketRight',
        '~': 'Backquote', // +shift
        ',': 'Comma',
        '.': 'Period',
        '/': 'Slash',
        '`': 'Backquote',
        '[': 'BracketLeft',
        '\\': 'Backslash',
        ']': 'BracketRight',
        '\'': 'Quote'
    };

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

    var keyChar = function(text, rawText) {
        var a = {
            type: KeyEventType.Char,
            text: text,
            unmodifiedText: rawText
        }
        // Special guy
        if (text === '\r') {
            a.keyIdentifier = "Enter";
        }
        return a;
    };

    function assignKeyIdentifiers(event, keyCode, char) {
        // Well we should probably assign more of these but I only
        // found problems when not assigning these ones (so far...)
        if (keyCode === 91) {
            event.keyIdentifier = "Meta";
        } else if (keyCode === 13) {
            event.keyIdentifier = "Enter";
        }

        // Unique DOM defined string value describing the meaning of the key in the context
        // of active modifiers, keyboard layout, etc (e.g., 'AltGr') (default: "").
        if (char in char2key) {
            event.key = char;
        }

        // Unique DOM defined string value for each physical key (e.g., 'KeyA') (default: "").
        if (char in char2DOMCodeUSKeyboard) {
            event.code = char2DOMCodeUSKeyboard[char];
        }
    }

    var keyDown = function(code, char, modifiers) {
        var a = {
            type: KeyEventType.RawKeyDown,
            windowsVirtualKeyCode: code, // Only windowsV key code is understood right now
                                         // by Chrome right now
            nativeVirtualKeyCode: code,
            unmodifiedText: "",
            modifiers: modifiers
        };
        assignKeyIdentifiers(a, code, char);
        console.log(a);
        return a;
    };

    var keyUp = function(code, char, modifiers) {
        var a = {
            type: KeyEventType.KeyUp,
            windowsVirtualKeyCode: code, // Only windowsV key code is understood
                                         // by Chrome right now
            nativeVirtualKeyCode: code,
            unmodifiedText: '',
            modifiers: modifiers
        };
        assignKeyIdentifiers(a, code, char);
        console.log(a);
        return a;
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

    // Convert JavaScript char to Windows1252
    function convertToKeyCode(char) {
        if (char in char2key) {
            return char2key[char];
        } else {
            return 0;
        }
    }

    // Convert Windows1252 code to JavaScript char
    function convertKeyCodeToChar(code) {
        if (code in key2char) {
            return key2char[code];
        } else {
            return String.fromCharCode(code);
        }
    }

    function processKeys(events, text, modifiers) {
        for (var i = 0; i < text.length; i++) {
            var char = text.charAt(i);
            var code = convertToKeyCode(char);
            events.push(keyDown(code, char, modifiers));
            // '\n' always gets converted to Enter
            if (code === 13) {
                char = '\r';
            }
            events.push(keyChar(char, char));
            events.push(keyUp(code, char, modifiers));
        }
    }

    function getModifier(key) {
        if (key === Key.Control || key === Key.LeftControl || key === Key.RightControl) {
            return ModifierMask.ControlKey;
        } else if (key === Key.Shift || key === Key.LeftShift || key === Key.RightShift) {
            return ModifierMask.ShiftKey;
        } else if (key === Key.Alt || key === Key.LeftAlt || key === Key.RightAlt) {
            return ModifierMask.AltKey;
        } else if (key === Key.Meta || key === Key.LeftMeta || key === Key.RightMeta) {
            return ModifierMask.MetaKey;
        }
        return 0;
    }

    function processKeysNumberOrString(events, item, modifiers, keyDowns) {
        if (pizza.isString(item)) {
            processKeys(events, item, modifiers);
        } else if (pizza.isNumber(item)) {
            var keyModifier = getModifier(item);
            if (keyModifier !== 0) {
                var keyPressIndex = keyDowns.indexOf(item);
                if (keyPressIndex > -1) {
                    modifiers &= (~(keyModifier));
                    events.push(keyUp(item, modifiers));
                    keyDowns.slice(keyPressIndex, 1);
                } else {
                    modifiers |= keyModifier;
                    events.push(keyDown(item, modifiers));
                    keyDowns.push(item);
                }
            } else {
                events.push(keyDown(item, modifiers));
                var v = convertKeyCodeToChar(item);
                if (modifiers === 0) {
                    events.push(keyChar(v, v));
                }
                events.push(keyUp(item, modifiers));
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
            var keyModifier = getModifier(item);
            modifiers &= (~(keyModifier));
            events.push(keyUp(item, modifiers));
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
