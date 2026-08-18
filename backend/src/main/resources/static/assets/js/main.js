"use strict";

(function () {
    var sidebarStorageKey = "adminHMD.sidebarMini";
    var desktopMedia = "(min-width: 992px)";

    function onReady(callback) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", callback);
            return;
        }

        callback();
    }

    function isDesktop() {
        return window.matchMedia(desktopMedia).matches;
    }

    function canUseStorage() {
        try {
            var testKey = sidebarStorageKey + ".test";
            window.localStorage.setItem(testKey, "1");
            window.localStorage.removeItem(testKey);
            return true;
        } catch (error) {
            return false;
        }
    }

    function getSavedMiniState(storageAvailable) {
        if (!storageAvailable) {
            return false;
        }

        return window.localStorage.getItem(sidebarStorageKey) === "true";
    }

    function saveMiniState(storageAvailable, isMini) {
        if (storageAvailable) {
            window.localStorage.setItem(sidebarStorageKey, String(isMini));
        }
    }

    onReady(function () {
        var body = document.body;
        var sidebarToggle = document.querySelector("[data-sidebar-toggle]");
        var closeButtons = document.querySelectorAll("[data-sidebar-close]");
        var sidebarLinks = document.querySelectorAll(".sidebar-nav .nav-link");
        var mediaQuery = window.matchMedia(desktopMedia);
        var storageAvailable = canUseStorage();

        function initValidation() {
            var forms = document.querySelectorAll(".needs-validation");

            Array.prototype.forEach.call(forms, function (form) {
                form.addEventListener("submit", function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }

                    form.classList.add("was-validated");
                });
            });
        }

        function initTableSearch() {
            var searchInputs = document.querySelectorAll("[data-table-search]");

            Array.prototype.forEach.call(searchInputs, function (input) {
                var tableId = input.getAttribute("data-table-search");
                var table = document.getElementById(tableId);

                if (!table) {
                    return;
                }

                input.addEventListener("input", function () {
                    var query = input.value.trim().toLowerCase();
                    var rows = table.querySelectorAll("tbody tr");

                    Array.prototype.forEach.call(rows, function (row) {
                        row.hidden = query !== "" && row.textContent.toLowerCase().indexOf(query) === -1;
                    });
                });
            });
        }

        initValidation();
        initTableSearch();
        if (!sidebarToggle) {
            return;
        }

        function setClass(element, className, enabled) {
            if (enabled) {
                element.classList.add(className);
            } else {
                element.classList.remove(className);
            }
        }

        function setToggleExpanded() {
            const expanded = isDesktop()
                ? !body.classList.contains("sidebar-mini")
                : body.classList.contains("sidebar-open");

            sidebarToggle.setAttribute("aria-expanded", String(expanded));
        }

        function closeMobileSidebar() {
            body.classList.remove("sidebar-open");
            setToggleExpanded();
        }

        function toggleSidebar() {
            if (isDesktop()) {
                body.classList.toggle("sidebar-mini");
                saveMiniState(storageAvailable, body.classList.contains("sidebar-mini"));
            } else {
                body.classList.toggle("sidebar-open");
            }

            setToggleExpanded();
        }

        function addCloseHandlers(items) {
            Array.prototype.forEach.call(items, function (item) {
                item.addEventListener("click", function () {
                    if (!isDesktop()) {
                        closeMobileSidebar();
                    }
                });
            });
        }

        if (getSavedMiniState(storageAvailable) && isDesktop()) {
            body.classList.add("sidebar-mini");
        }

        sidebarToggle.addEventListener("click", toggleSidebar);
        addCloseHandlers(closeButtons);
        addCloseHandlers(sidebarLinks);
        setToggleExpanded();

        function handleBreakpointChange() {
            if (isDesktop()) {
                body.classList.remove("sidebar-open");
                setClass(body, "sidebar-mini", getSavedMiniState(storageAvailable));
            } else {
                body.classList.remove("sidebar-mini");
            }

            setToggleExpanded();
        }

        if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener("change", handleBreakpointChange);
        } else if (mediaQuery.addListener) {
            mediaQuery.addListener(handleBreakpointChange);
        }
    });
})();
