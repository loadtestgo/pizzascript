(function($) {
    // TODO: make the node ID configurable
    var treeNode = $('#jsdoc-toc-nav');

    // initialize the tree
    treeNode.tree({
        autoEscape: false,
        closedIcon: '&#x21e2;',
        data: [{"label":"<a href=\"module-Browser.html\">Browser</a>","id":"module:Browser","children":[]},{"label":"<a href=\"module-CSV.html\">CSV</a>","id":"module:CSV","children":[]},{"label":"<a href=\"module-Cookie.html\">Cookie</a>","id":"module:Cookie","children":[]},{"label":"<a href=\"module-Data.html\">Data</a>","id":"module:Data","children":[]},{"label":"<a href=\"module-Frame.html\">Frame</a>","id":"module:Frame","children":[]},{"label":"<a href=\"module-HttpHeader.html\">HttpHeader</a>","id":"module:HttpHeader","children":[]},{"label":"<a href=\"module-HttpRequest.html\">HttpRequest</a>","id":"module:HttpRequest","children":[]},{"label":"<a href=\"module-Page.html\">Page</a>","id":"module:Page","children":[]},{"label":"<a href=\"module-Tab.html\">Tab</a>","id":"module:Tab","children":[]},{"label":"<a href=\"module-TestError.html\">TestError</a>","id":"module:TestError","children":[]},{"label":"<a href=\"module-TestResult.html\">TestResult</a>","id":"module:TestResult","children":[]},{"label":"<a href=\"module-assert.html\">assert</a>","id":"module:assert","children":[]},{"label":"<a href=\"module-console.html\">console</a>","id":"module:console","children":[]},{"label":"<a href=\"module-load.html\">load</a>","id":"module:load","children":[]},{"label":"<a href=\"module-pizza.html\">pizza</a>","id":"module:pizza","children":[]},{"label":"<a href=\"module-utils.html\">utils</a>","id":"module:utils","children":[]}],
        openedIcon: ' &#x21e3;',
        saveState: true,
        useContextMenu: false
    });

    // add event handlers
    // TODO
})(jQuery);
