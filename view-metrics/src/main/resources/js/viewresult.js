pizza = pizza|| {};

pizza.viewresult = function() {
    var _pages = [];
    var _pageRows = [];
    var _pageInfoRows = [];
    var _pageIndex = 0;

    function formatTime(t1, t2) {
        return pizza.formatMillis(t2 - t1);
    }

    function addRequestTiming(requestClass, left, width, loadTime) {
        var timing = $("<div/>");
        timing.addClass("netBar");
        timing.addClass(requestClass);
        timing.css('left', ((left * 100) / loadTime).toFixed(2) + "%");
        timing.css('width', ((width * 100) / loadTime).toFixed(2) + "%");
        timing.css('min-width', '1px');
        return timing;
    }

    function addRequestMarker(row, requestClass, left, loadTime) {
        if (!left) {
            return;
        }
        var timing = $("<div/>");
        timing.addClass(requestClass);
        timing.addClass("netPageTimingBar");
        timing.addClass("netBar");
        timing.css('left', ((left * 100) / loadTime).toFixed(2) + "%");
        timing.css('display', 'block');
        row.append(timing);
    }

    function greaterThanZero(time) {
        if (time > 0) {
            return true;
        }
        return false;
    }

    function diffGreaterThanZero(timeStart, timeEnd) {
        if (timeStart >= 0 &&
            timeEnd > 0 &&
            timeEnd > timeStart) {
            return true;
        }
        return false;
    }

    function getContentTypeFull(headers) {
        var lookForHeader = "content-type";
        for (var i = 0; i < headers.length; ++i) {
            var header = headers[i];
            if (header.name.toLowerCase() === lookForHeader) {
                return header.value;
            }
        }

        return null;
    }

    function getContentType(headers) {
        var contentType = getContentTypeFull(headers);
        if (contentType == null) {
            return null;
        }

        var i = contentType.indexOf(';');
        if (i < 0) {
            return contentType.trim();
        }

        return contentType.substring(0, i).trim();
    }

    function clickRequestExpand(event) {
        var item = $(this);
        var request = event.data;

        if (item.hasClass("opened")) {
            item.removeClass("opened");
            item.next().remove();
        } else {
            item.addClass("opened");

            var rowInfo = mtl.execTemplate("#request-row-info-template", request);

            var requestItemTemplate = $("#details-item-template").html();
            var requestHeaderTemplate = $("#details-header-template").html();

            var headersTab = $(".tabHeadersBody", rowInfo);
            var i, header, body;
            body = headersTab.find("tbody");
            if (request.requestHeaders.length > 0) {
                body.append(mtl.template(requestHeaderTemplate, {name: "Request Headers"}));

                for (i = 0; i < request.requestHeaders.length; ++i) {
                    header = request.requestHeaders[i];
                    body.append(mtl.template(requestItemTemplate, header));
                }
            }

            if (request.responseHeaders.length > 0) {
                body.append(mtl.template(requestHeaderTemplate, {name: "Response Headers"}));

                for (i = 0; i < request.responseHeaders.length; ++i) {
                    header = request.responseHeaders[i];
                    body.append(mtl.template(requestItemTemplate, header));
                }
            }

            var detailsTable = $(".tabDetailsBody", rowInfo);
            body = detailsTable.find("tbody");

            var addValue = function (name, value) {
                if (typeof value !== 'undefined' && value != null && value !== "") {
                    body.append(mtl.template(requestItemTemplate, {name: name, value: value}));
                }
            };

            var addValueAlways = function (name, value) {
                body.append(mtl.template(requestItemTemplate, {name: name, value: value}));
            };

            $(".tabHeaders", rowInfo).click($(".tabHeadersBody", rowInfo), clickTabSwitched);
            $(".tabDetails", rowInfo).click($(".tabDetailsBody", rowInfo), clickTabSwitched);

            addValue("URL", request.url);
            if (request.protocol) {
                addValue("Protocol", request.protocol);
            }
            addValue("Method", request.method);
            var status = "" + request.statusCode + " " + request.statusText;
            addValue("Status", status);
            addValue("Date", new Date(request.startTime).toISOString());
            addValue("IP", request.ip);
            addValue("Connection Reused", request.connectionReused ? "yes" : "no");
            addValue("Connection Id", request.connectionId);

            if (request.initiatorUrl) {
                addValue("Initiator", request.initiatorUrl);
            }
            addValue("Resource Type", request.resourceType);
            addValue("Mime Type", request.mimeType);

            if (request.wsMessages) {
                var wsRecv = 0;
                var wsSend = 0;
                for (var j = 0; j < request.wsMessages.length; ++j) {
                    var msg = request.wsMessages[j];
                    if (msg.flow == "Sent") {
                        wsSend++;
                    } else {
                        wsRecv++;
                    }
                }
                addValue("WebSocket Frames Sent", wsSend);
                addValue("WebSocket Frames Recv", wsRecv);
            }

            if (request.fromCache) {
                addValue("From Cache", "yes");
            }
            if (request.requestHeadersSize > 0) {
                addValue("Request Headers Size", pizza.formatSizeInBytes(request.requestHeadersSize));
            }
            if (request.requestBodySize > 0) {
                addValue("Request Body Size", pizza.formatSizeInBytes(request.requestBodySize));
            }
            if (request.responseHeadersSize > 0) {
                addValue("Response Headers Size", pizza.formatSizeInBytes(request.responseHeadersSize));
            }
            addValue("Body Size", pizza.formatSizeInBytes(request.bodySize));
            if (request.bytesRecvCompressed > 0) {
                addValue("Compressed Size", pizza.formatSizeInBytes(request.bytesRecvCompressed));
            }

            addValue("Request State", request.state);
            addValue("Error", request.error);

            var r;

            if (request.postData) {
                var postTabHeader = $(".tabPostBody", rowInfo);
                var postTab = $(".tabPostDataBody", rowInfo);
                postTabHeader.click(postTab, clickTabSwitched);

                postTabHeader.css("display", "inline");

                body = postTab.find("tbody");

                body.append(mtl.template(requestHeaderTemplate, {name: "Body"}));
                addValueAlways("Content-Type", getContentTypeFull(request.requestHeaders));
                addValueAlways("Data", request.postData);

                if (getContentType(request.requestHeaders) === "application/x-www-form-urlencoded") {
                    r = pizza.parseQueryString(request.postData);
                    if (r.length > 0) {
                        body.append(mtl.template(requestHeaderTemplate, {name: "Params"}));
                        for (i = 0; i < r.length; ++i) {
                            addValueAlways(r[i][0], r[i][1]);
                        }
                    }
                }
            }

            r = pizza.parseQueryStringFromUrl(request.url);
            if (r.length > 0) {
                var paramsTabHeader = $(".tabUrlParams", rowInfo);
                var paramsTab = $(".tabUrlParamsBody", rowInfo);
                paramsTabHeader.click(paramsTab, clickTabSwitched);
                paramsTabHeader.css("display", "inline");

                body = paramsTab.find("tbody");

                body.append(mtl.template(requestHeaderTemplate, {name: "Url"}));
                body.append("<tr><td></td><td class=\"netInfoParamValue\">" + request.url + "</td></tr>");
                body.append(mtl.template(requestHeaderTemplate, {name: "Params"}));
                for (i = 0; i < r.length; ++i) {
                    addValueAlways(r[i][0], r[i][1]);
                }
            }

            if (request.wsMessages && request.wsMessages.length > 0) {
                var msgTabHeader = $(".tabMessages", rowInfo);
                var msgTab = $(".tabMessagesBody", rowInfo);
                msgTabHeader.click(msgTab, clickTabSwitched);
                msgTabHeader.css("display", "inline");

                body = msgTab.find("tbody");

                var websocketMsgTemplate = $("#websocket-msg-template").html();

                for (i = 0; i < request.wsMessages.length; ++i) {
                    var p = request.wsMessages[i];
                    body.append(mtl.template(websocketMsgTemplate, {
                        time: pizza.formatMillis(p.time),
                        flow: p.flow,
                        data: p.data,
                        len: p.len
                    }));
                }
            }

            item.after(rowInfo);
        }
    }

    function clickTabSwitched(event) {
        var item = $(this);
        var tab = event.data;

        var prev = item.parent().find("> .selected");
        prev.removeClass("selected");
        prev.removeAttr("selected");

        var prevTab = tab.parent().find("> .selected");
        prevTab.removeClass("selected");
        prevTab.removeAttr("selected");

        item.addClass('selected');
        tab.addClass('selected');
        item[0].setAttribute('selected', 'true');
        tab[0].setAttribute('selected', 'true');
    }

    var tooltip = null;

    function requestTimingsMouseEnter(event) {
        var request = event.data.request;
        var domLoaded = event.data.domLoaded;
        var pageLoaded = event.data.pageLoaded;
        var startTime = event.data.startTime;
        var navStart = event.data.navStart;
        var firstRender = event.data.firstRender;
        var firstContentfulRender = event.data.firstContentfulRender;

        tooltip = mtl.execTemplate("#info-tip-template",
            {
                x: -10000,
                y: -10000,
                startTime: pizza.formatMillisPlus(startTime),
                navStart: pizza.formatMillisPlus(navStart),
                domLoaded: pizza.formatMillisPlus(domLoaded),
                firstRender: pizza.formatMillisPlus(firstRender),
                firstContentfulRender: pizza.formatMillisPlus(firstContentfulRender),
                pageLoaded: pizza.formatMillisPlus(pageLoaded)
            });

        var timesItem = tooltip.find(".timeInfoEvents").parent();
        var itemTemplate = $("#info-tip-item-template").text();

        function addItem(text, css, start, elapsed) {
            timesItem.before(mtl.template(itemTemplate,
                {text: text, css: css, start: start, elapsed: elapsed}));
        }

        if (request.blockedTime > 0) {
            addItem("Blocking", "netBlockingBar", "0s", pizza.formatMillis(request.blockedTime));
        }

        if (diffGreaterThanZero(request.dnsStart, request.dnsEnd)) {
            addItem("DNS", "netResolvingBar", pizza.formatMillisPlus(request.dnsStart),
                pizza.formatMillis(request.dnsEnd - request.dnsStart));
        }

        if (diffGreaterThanZero(request.connectStart, request.connectEnd)) {
            var start = request.connectStart;
            var end = request.connectEnd;
            if (diffGreaterThanZero(request.sslStart, request.sslEnd)) {
                end = request.sslStart;
            }
            addItem("Connecting", "netConnectingBar", pizza.formatMillisPlus(start),
                pizza.formatMillis(end - start));
        }

        if (diffGreaterThanZero(request.sslStart, request.sslEnd)) {
            addItem("SSL", "netSslBar", pizza.formatMillisPlus(request.sslStart),
                pizza.formatMillis(request.sslEnd - request.sslStart));
        }

        addItem("Sending", "netSendingBar", pizza.formatMillisPlus(request.sendStart),
            pizza.formatMillis(request.sendEnd - request.sendStart));

        addItem("Waiting", "netWaitingBar", pizza.formatMillisPlus(request.recvHeadersEnd),
            pizza.formatMillis(request.recvHeadersEnd - request.sendEnd));

        addItem("Receiving", "netReceivingBar", pizza.formatMillisPlus(request.recvEnd),
            pizza.formatMillis(request.recvEnd - request.recvHeadersEnd));

        $('body').append(tooltip);

        positionTooltip(event);
    }

    function positionTooltip(event) {
        // Position on screen, no matter where the cursor is
        var x = event.clientX + 10;
        var y = event.clientY + 5;
        var right = tooltip.width() + x;
        var bottom = tooltip.height() + y;
        if (right > $(window).width()) {
            x = event.clientX - 10 - tooltip.width();
            if (x < 10) {
                x = 10;
            }
        }

        if (bottom > $(window).height()) {
            y = $(window).height() - 5 - tooltip.height();
        }

        tooltip.offset({top: y + $(window).scrollTop(), left: x + $(window).scrollLeft()});
    }

    function removeTooltip() {
        if (tooltip != null) {
            tooltip.remove();
            tooltip = null;
        }
    }

    function requestStatusMouseEnter(event) {
        removeTooltip();

        tooltip = mtl.execTemplate("#text-tip-template", {
            x: event.clientX + 10,
            y: event.clientY + 5,
            text: event.data
        });

        $('body').append(tooltip);
    }

    function hoverUrlEnter(event) {
        $(this).attr('colspan', 4);
        var row = $(this).parent();
        $(".netStatusCol", row).css('display', 'none');
        $(".netSizeCol", row).css('display', 'none');
        $(".netTimeCol", row).css('display', 'none');
        $(".netHrefLabel", row).css('display', 'none');
        $(".netFullHrefLabel", row).css('display', 'block');
    }

    function hoverUrlLeave(event) {
        $(this).removeAttr('colspan');
        var row = $(this).parent();
        $(".netStatusCol", row).css('display', '');
        $(".netSizeCol", row).css('display', '');
        $(".netTimeCol", row).css('display', '');
        $(".netHrefLabel", row).css('display', '');
        $(".netFullHrefLabel", row).css('display', '');
    }

    function buildDomainsTab(page, pageInfoRow) {
        var template = $("#domain-item-template").html();
        var summaryTemplate = $("#domain-summary-template").html();

        var tab = $(".tabDomainsBody", pageInfoRow);
        var tableBody = tab.find("tbody");

        var domains = [];
        var domainMap = {};
        var i = 0;
        var domain;
        for (; i < page.requests.length; ++i) {
            var request = page.requests[i];
            if ((request.error && request.error === "Blocked") || request.fromCache) {
                continue;
            }
            var host = pizza.getHostFromUrl(request.url);
            domain = domainMap[host];
            if (!domain) {
                domain = { requests: 0, size: 0, ip: [], host: host, rqHeadersSize: 0, rspHeadersSize: 0, uploadSize:0 };
                domainMap[host] = domain;
                domains.push(domain);
            }

            if (request.ip) {
                var j = domain.ip.indexOf(request.ip);
                if (j == -1) {
                    domain.ip.push(request.ip);
                }
            }

            domain.requests += 1;

            if (request.bytesRecvCompressed > 0) {
                domain.size += request.bytesRecvCompressed;
            } else {
                domain.size += request.bodySize;
            }
            if (request.requestHeadersSize) {
                domain.rqHeadersSize += request.requestHeadersSize;
            }
            if (request.responseHeadersSize) {
                domain.rspHeadersSize += request.responseHeadersSize;
            }
            if (request.requestBodySize) {
                domain.uploadSize += request.requestBodySize;
            }
        }

        var totals = { domains: domains.length, up: 0, down: 0,
            headersUp: 0, headersDown: 0, requests: 0};

        domains.sort(function(a, b){ return b.size-a.size; });
        for (i = 0; i < domains.length; ++i) {
            domain = domains[i];
            totals.down += domain.size;
            totals.up += domain.uploadSize;
            totals.headersUp += domain.rqHeadersSize;
            totals.headersDown += domain.rspHeadersSize;
            totals.requests += domain.requests;

            domain.ip = domain.ip.join(", ");
            pizza.inject(domain, ['size', 'uploadsize', 'rspHeadersSize', 'rqHeadersSize'], pizza.formatSize);

            tableBody.append(mtl.template(template, domain));
        }

        pizza.inject(totals, ['up', 'down', 'headersUp', 'headersDown'], pizza.formatSize);
        tableBody.append(mtl.template(summaryTemplate, totals));
    }

    function buildConsoleTab(page, pageInfoRow) {
        if (page.consoleMessages && page.consoleMessages.length > 0) {
            $(".tabConsole", pageInfoRow).css('display', '');

            var template =
                "<tr class=\"consoleMsg {{level}}\"><td class=\"consoleTime\">{{time}}</td>" +
                "<td class=\"consoleText\"><div>{{text}}</div></td><td class=\"consoleUrl\"><div>{{url}}</div></td></tr>";

            var table = $(".tabConsoleBody", pageInfoRow);
            var tableBody = table.find("tbody");

            for (var j = 0; j < page.consoleMessages.length; ++j) {
                var msg = page.consoleMessages[j];
                msg.time = formatTime(page.startTime, new Date(msg.timestamp));
                tableBody.append(mtl.template(template, msg));
            }
        } else {
            $(".tabConsole", pageInfoRow).css('display', 'none');
        }
    }

    function buildDetailsTab(page, pageInfoRow) {
        var detailsTemplate = $("#details-item-template").html();

        var detailsTable = $(".tabDetailsBody", pageInfoRow);
        var body = detailsTable.find("tbody");

        var add = function (name, value) {
            if (typeof value !== 'undefined' && value != null && value !== "") {
                body.append(mtl.template(detailsTemplate, {name: name, value: value}));
            }
        };

        add("Start URL", page.origUrl);
        if (page.url && page.origUrl !== page.url) {
            add("End URL", page.url);
        }

        add("Start Time", page.startTime);
        add("End Time", page.endTime);
        add("Duration", pizza.formatMillis(page.endTime - page.startTime));

        if (page.error) {
            add("Error", page.error);
        }

        add("Requests", page.requests.length);
        add("Size", pizza.formatSize(page.responseSize));
        if (page.numDomElements) {
            add("DOM Elements", page.numDomElements);
        }
        if (page.numFrames) {
            add("Frames", page.numFrames);
        }
        if (page.connects >= 0) {
            add("Connects", page.connects);
        }
        if (page.sslConnects >= 0) {
            add("SSL Connects", page.sslConnects);
        }
        if (page.dnsLookups >= 0) {
            add("DNS Lookups", page.dnsLookups);
        }
        if (page.domains >= 0) {
            add("Domains", page.domains);
        }

        add("Nav Start", pizza.formatMillis(page.navStartTime - page.startTime));
        if (page.navCommitTime) {
            add("Nav Commit", pizza.formatMillis(page.navCommitTime - page.startTime));
        }
        if (page.firstPaintTime) {
            add("First Render", pizza.formatMillis(page.firstPaintTime - page.startTime));
        }
        if (page.domContentLoadedTime) {
            add("DOM Load", pizza.formatMillis(page.domContentLoadedTime - page.startTime));
        }
        if (page.navEndTime) {
            add("Nav End", pizza.formatMillis(page.navEndTime - page.startTime));
        }

        add("State", page.state);
        add("Nav Type", page.navigationType);
        add("Process:Tab", page.processId + ":" + page.tabId);

        if (page.protocol) {
            add("Protocol", page.protocol);
        }
    }

    function buildRequestsTab(page, pageInfoRow) {
        var tBody = $('.requestTableBody', pageInfoRow);

        var startTime = page.startTime;
        var endTime = page.endTime;
        for (var j = 0; j < page.requests.length; ++j) {
            var request = page.requests[j];
            request._startTime = new Date(request.startTime).getTime();
            request._endTime = new Date(request._startTime + request.recvEnd).getTime();
        }

        var navStartTime = page.navStartTime;
        var domLoadTime = page.domContentLoadedTime;
        var navEndTime = page.navEndTime;
        var firstRenderTime = page.firstPaintTime;
        var firstContentfulRenderTime = page.firstContentfulPaintTime;

        var loadSpan = endTime - startTime;

        var domLoadSpan = domLoadTime - startTime;
        var navEndSpan = navEndTime - startTime;
        var navStartSpan = navStartTime - startTime;
        var firstRenderSpan = firstRenderTime - startTime;
        var firstContentfulRenderSpan = firstContentfulRenderTime - startTime;

        for (j = 0; j < page.requests.length; ++j) {
            request = page.requests[j];

            var row = $("<tr/>");
            row.addClass("netRow expandable");

            var statusText;
            if (request.fromCache) {
                statusText = "Cached";
                row.addClass("cached");
            } else if (request.error) {
                statusText = request.error;
                if (request.error == "Cancelled") {
                    row.addClass("warn");
                } else if (request.error == "Blocked") {
                    row.addClass("blocked");
                } else {
                    row.addClass("error");
                }
            } else if (request.state != 'Complete') {
                statusText = request.state;
                row.addClass("warn");
            } else {
                if (!request.statusText) {
                    statusText = request.statusCode;
                } else {
                    statusText = request.statusCode + " " + request.statusText;
                }
                if (request.statusCode >= 400) {
                    row.addClass("error");
                } else {
                    row.addClass("loaded");
                }
            }

            mtl.loadTemplate(row, "#request-template", {
                statusCode: statusText,
                requestSize: pizza.formatSize(request.bodySize),
                href: request.method + " " + request.url,
                url: request.url
            });

            var netBar = $(".netTimelineBar", row);

            var start = request._startTime - startTime;

            function addTimingBar(name, width, span) {
                var timing = addRequestTiming(name, start, width, loadSpan);
                if (span) {
                    timing.append(span);
                }
                netBar.append(timing);
            }

            if (greaterThanZero(request.blockedTime)) {
                addTimingBar("netBlockingBar", request.blockedTime);
            }

            if (diffGreaterThanZero(request.dnsStart, request.dnsEnd)) {
                addTimingBar("netResolvingBar", request.dnsEnd);
            }

            if (diffGreaterThanZero(request.connectStart, request.connectEnd)) {
                // SSL times are generally part of the connect time
                if (diffGreaterThanZero(request.sslStart, request.sslEnd)) {
                    addTimingBar("netConnectingBar", request.sslStart);
                } else {
                    addTimingBar("netConnectingBar", request.connectEnd);
                }
            }

            if (diffGreaterThanZero(request.sslStart, request.sslEnd)) {
                addTimingBar("netSslBar", request.sslEnd);
            }

            if (diffGreaterThanZero(request.sendStart, request.sendEnd)) {
                addTimingBar("netSendingBar", request.sendEnd);
            }

            if (diffGreaterThanZero(request.sendEnd, request.recvHeadersEnd)) {
                addTimingBar("netWaitingBar", request.recvHeadersEnd);
            }

            var end = request.recvEnd;
            if (end < 0) {
                end = 0;
            }
            var span = $("<span/>");
            span.addClass("netTimeLabel");
            span.text(pizza.formatMillis(end));
            addTimingBar("netReceivingBar", end, span);

            addRequestMarker(netBar, "netNavStartBar", navStartSpan, loadSpan);
            addRequestMarker(netBar, "netContentLoadBar", domLoadSpan, loadSpan);
            addRequestMarker(netBar, "netFirstRenderBar", firstRenderSpan, loadSpan);
            addRequestMarker(netBar, "netFirstContentfulRenderBar", firstContentfulRenderSpan, loadSpan);
            addRequestMarker(netBar, "netWindowLoadBar", navEndSpan, loadSpan);

            netBar.mouseenter({
                request: request,
                startTime: start,
                navStart: navStartSpan,
                domLoaded: domLoadSpan,
                firstRender: firstRenderSpan,
                firstContentfulRender: firstContentfulRenderSpan,
                pageLoaded: navEndSpan
            }, requestTimingsMouseEnter).mouseleave(removeTooltip);

            row.find(".netStatusLabel").mouseenter(statusText,
                requestStatusMouseEnter).mouseleave(removeTooltip);

            row.find(".netHrefCol").mouseenter(null, hoverUrlEnter).mouseleave(hoverUrlLeave);

            var sizeText = null;
            if (request.bytesRecvCompressed > 0) {
                sizeText = pizza.formatSizeInBytes(request.bytesRecvCompressed) + " (" +
                pizza.formatSizeInBytes(request.bodySize) + " uncompressed)"
            } else {
                sizeText = pizza.formatSizeInBytes(request.bodySize);
            }
            row.find(".netSizeLabel").mouseenter(sizeText,
                requestStatusMouseEnter).mouseleave(removeTooltip);

            row.click(request, clickRequestExpand);

            tBody.append(row);
        }

        var numRequests = page.requests.length;
        if (numRequests == 1) {
            numRequests = "1 Request";
        } else {
            numRequests = page.requests.length + " Requests";
        }

        var loadTime, onLoadTime;
        if (navEndTime > 0 && navStartTime > 0 && (navEndTime - navStartTime) >= 0) {
            loadTime = pizza.formatMillis(navEndTime - navStartTime);
        } else if (page.endTime > 0 && page.startTime > 0 && (page.endTime - page.startTime) >= 0) {
            loadTime = pizza.formatMillis(page.endTime - page.startTime);
        } else {
            loadTime = "";
        }

        if (domLoadTime > 0 && navStartTime > 0 && (domLoadTime - navStartTime) >= 0) {
            onLoadTime = pizza.formatMillis(domLoadTime - navStartTime);
        } else {
            onLoadTime = "n/a";
        }

        mtl.appendTemplate(tBody, "#page-summary-template", {
            numRequests: numRequests,
            size: pizza.formatSize(page.responseSize),
            sizeStats: "(" + pizza.formatSize(page.totalSize) + " uncompressed)",
            loadTime: loadTime + " (onload: " + onLoadTime + ")"
        });

        $(".tableSummaryRow", tBody).mouseenter({page: page}, pageRowMouseEnter).mouseleave(removeTooltip);
    }

    function expandPage(pageRow, pageInfoRow, page) {
        // Show the page details
        pageInfoRow.css('display', '');

        // Add tab switcher
        $(".tabRequests", pageInfoRow).click($(".tabRequestsBody", pageInfoRow), clickTabSwitched);
        $(".tabDetails", pageInfoRow).click($(".tabDetailsBody", pageInfoRow), clickTabSwitched);
        $(".tabConsole", pageInfoRow).click($(".tabConsoleBody", pageInfoRow), clickTabSwitched);
        $(".tabDomains", pageInfoRow).click($(".tabDomainsBody", pageInfoRow), clickTabSwitched);

        buildDetailsTab(page, pageInfoRow);
        buildDomainsTab(page, pageInfoRow);
        buildConsoleTab(page, pageInfoRow);
        buildRequestsTab(page, pageInfoRow);
    }

    function hidePage(pageRow, pageInfoRow, page) {
        pageInfoRow.css('display', 'none');
        $('.tabRequestsBody tbody', pageInfoRow).empty();
        $('.tabDetailsBody tbody', pageInfoRow).empty();
        $('.tabConsoleBody tbody', pageInfoRow).empty();
        $('.tabDomainsBody tbody', pageInfoRow).empty();
    }

    function clickPageExpand(event) {
        var pageRow = $(this);
        var pageInfoRow = event.data.pageInfoRow;
        var page = event.data.page;

        if (pageRow.hasClass("opened")) {
            hidePage(pageRow, pageInfoRow, page);
        } else {
            expandPage(pageRow, pageInfoRow, page);
        }
        pageRow.toggleClass("opened");
    }

    function getRequestEndTime(r) {
        if (r.recvEnd > 0) return r.recvEnd;
        if (r.recvHeadersEnd > 0) return r.recvHeadersEnd;
        if (r.sendEnd > 0) return r.sendEnd;
        if (r.sendStart > 0) return r.sendStart;
        if (r.sslEnd > 0) return r.sslEnd;
        if (r.sslStart > 0) return r.sslStart;
        if (r.connectEnd > 0) return r.connectEnd;
        if (r.connectStart > 0) return r.connectStart;
        if (r.dnsEnd > 0) return r.dnsEnd;
        if (r.dnsStart > 0) return r.dnsStart;

        return 0;
    }

    var _populateCalculatedData = function(result) {
        var pages = result.pages;
        result.totalSize = 0;
        result.responseSize = 0;

        var resultDomains = { num: 0 };

        for (var i = 0; i < pages.length; ++i) {
            var page = pages[i];
            var startTime = -1;
            var endTime = -1;
            var r = null;
            var connects = 0;
            var sslConnects = 0;
            var domains = { num: 0 };
            var dnsLookups = { num: 0 };

            for (var j = 0; j < page.requests.length; ++j) {
                r = page.requests[j];
                if (j == 0) {
                    startTime = r.startTime;
                    endTime = startTime + getRequestEndTime(r);
                } else {
                    if (r.startTime) {
                        if (r.startTime < startTime) {
                            startTime = r.startTime;
                        }
                        var rEnd = getRequestEndTime(r);
                        if (rEnd + r.startTime > endTime) {
                            endTime = rEnd + r.startTime;
                        }
                    }
                }
                if (diffGreaterThanZero(r.connectStart, r.connectEnd)) {
                    connects++;
                }
                if (diffGreaterThanZero(r.sslStart, r.sslEnd)) {
                    sslConnects++;
                }

                var host = pizza.getHostFromUrl(r.url);
                if (domains[host]) {
                    domains[host] = domains[host] + 1;
                } else {
                    domains[host] = 1;
                    domains.num += 1;
                }

                if (resultDomains[host]) {
                    resultDomains[host] = resultDomains[host] + 1;
                } else {
                    resultDomains[host] = 1;
                    resultDomains.num += 1;
                }

                // Any requests that are blocked by DNS will have a DNS time
                // there is only one actual lookup per domain though
                if (diffGreaterThanZero(r.dnsStart, r.dnsEnd)) {
                    if (dnsLookups[host]) {
                        dnsLookups[host] = dnsLookups[host] + 1;
                    } else {
                        dnsLookups[host] = 1;
                        dnsLookups.num += 1;
                    }
                }
            }

            if (page.navStartTime && startTime > page.navStartTime) {
                startTime = page.navStartTime;
            }
            if (page.navEndTime && endTime < page.navEndTime) {
                endTime = page.navEndTime;
            }

            page.startTime = new Date(+startTime);
            page.endTime = new Date(+endTime);
            page.connects = connects;
            page.dnsLookups = dnsLookups.num;
            page.sslConnects = sslConnects;
            page.domains = domains.num;

            var responseSize = 0;
            var totalSize = 0;
            for (j = 0; j < page.requests.length; ++j) {
                var request = page.requests[j];
                if (!request.fromCache) {
                    totalSize += request.bodySize;
                    if (request.bytesRecvCompressed >= 0) {
                        responseSize += request.bytesRecvCompressed;
                    } else {
                        responseSize += request.bodySize;
                    }
                }
            }

            page.totalSize = totalSize;
            page.responseSize = responseSize;

            result.totalSize += totalSize;
            result.responseSize += responseSize;
        }

        result.domains = resultDomains.num;
        result.calulatedSummary = true;
    };

    function pageRowMouseEnter(event) {
        removeTooltip();
        var page = event.data.page;
        tooltip = mtl.execTemplate("#page-popup-template", { x: -10000, y: -10000 });
        var table = $("tbody", tooltip);
        var template = '<tr><td></td><td colspan="2" class="timeInfoTipStartLabel">{{key}}</td><td>{{value}}</td></tr>';
        function add(key, value) {
            table.append(mtl.template(template, {key: key, value: value}));
        }

        add("URL", page.origUrl);
        if (page.url && page.origUrl !== page.url) {
            add("End URL", page.url);
        }

        add("Start Time", page.startTime);
        add("End Time", page.endTime);
        add("Duration", pizza.formatMillis(page.endTime - page.startTime));

        if (page.error) {
            add("Error", page.error);
        }

        add("Requests", page.requests.length);
        add("Size", pizza.formatSize(page.responseSize));

        $('body').append(tooltip);

        positionTooltip(event);
    }

    function _addPage(pageTable, page, options) {
        if (!options) {
            options = {};
        }

        var loadTime, onLoadTime;
        var navEndTime = page.navEndTime;
        var navStartTime = page.navStartTime;
        var domLoadTime = page.domContentLoadedTime;
        if (navEndTime > 0 && navStartTime > 0 && (navEndTime - navStartTime) >= 0) {
            loadTime = pizza.formatMillis(navEndTime - navStartTime);
        } else if (page.endTime > 0 && page.startTime > 0 && (page.endTime - page.startTime) >= 0) {
            loadTime = pizza.formatMillis(page.endTime - page.startTime);
        } else {
            loadTime = "";
        }

        if (domLoadTime > 0 && navStartTime > 0 && (domLoadTime - navStartTime) >= 0) {
            onLoadTime = pizza.formatMillis(domLoadTime - navStartTime);
        } else {
            onLoadTime = "n/a";
        }

        var v = {
            size: pizza.formatSize(page.responseSize),
            sizeStats: "(" + pizza.formatSize(page.totalSize) + " uncompressed)",
            loadTime: loadTime + " (onload: " + onLoadTime + ")"
        };

        if (page.origUrl) {
            v.name = page.origUrl;
        } else if (page.url) {
            v.name = page.url;
        }

        if (!options.ignoreName) {
            if (page.name) {
                v.name = page.name + " - " + v.name;
            }
            if (!v.name) {
                v.name = "Page " + (_pageIndex + 1);
            }
        }

        v.class = options.expandToggle ? "expandable" : "";

        var pageRow = mtl.execTemplate("#page-template-head", v);
        var pageInfoRow = mtl.execTemplate("#page-template", v);

        _pages.push(page);
        _pageRows.push(pageRow);
        _pageInfoRows.push(pageInfoRow);

        if (options.expanded) {
            expandPage(pageRow, pageInfoRow, page);
        } else {
            pageInfoRow.css('display', 'none');
        }

        if (options.expandToggle) {
            pageRow.click({pageInfoRow: pageInfoRow, page: page}, clickPageExpand);
            if (options.expanded) {
                pageRow.addClass("opened");
            }
        }

        pageRow.mouseenter({page: page}, pageRowMouseEnter).mouseleave(removeTooltip);

        pageTable.append(pageRow);
        pageTable.append(pageInfoRow);

        _pageIndex++;
    }

    function _setup(pageTable, result) {
        pageTable.empty();

        if (!result.calulatedSummary) {
            _populateCalculatedData(result);
        }

        _pages = [];
        _pageRows = [];
        _pageInfoRows = [];
        _pageIndex = 0;
    }

    function _build(pageTable, result, options) {
        _setup(pageTable, result);

        var pages = result.pages;
        for (var i = 0; i < pages.length; ++i) {
            _addPage(pageTable, pages[i], options);
        }
    }

    return {
        /**
         * Build list of expandable pages with the request waterfall
         *
         * Alternative to setup & addPage()
         */
        build: _build,

        /**
         * Call before adding the pages one at a time
         */
        setup: _setup,

        /**
         * Add a single page
         */
        addPage: _addPage,

        /**
         * Add summary data to results object.
         *
         * This will be recalculated each time you call this function
         */
        populateCalculatedData: _populateCalculatedData
    };
}();

pizza.getErrorForResult = function(result) {
    if (result.error.type === "Timeout") {
        return result.error.type + ": " +
            result.error.message + ' (after ' + pizza.formatMillis(result.runTime) + ')';
    } else {
        return result.error.type + ": " + result.error.message;
    }
};
