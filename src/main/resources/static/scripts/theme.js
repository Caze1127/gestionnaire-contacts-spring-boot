(function () {

    function applyTheme(
        theme
    ) {

        if (
            theme === "dark"
        ) {

            document.body
                .classList
                .add(
                    "dark-mode"
                );

        } else {

            document.body
                .classList
                .remove(
                    "dark-mode"
                );
        }
    }

    function getInitialTheme() {

        const saved =
            localStorage
                .getItem(
                    "theme"
                );

        if (saved) {
            return saved;
        }

        if (
            window.matchMedia
            &&
            window
                .matchMedia(
                    "(prefers-color-scheme: dark)"
                )
                .matches
        ) {

            return "dark";
        }


        return "light";
    }

    function createButton() {

        if (
            document.querySelector(
                "#theme-toggle"
            )
        ) {
            return;
        }

        const button =
            document.createElement(
                "button"
            );

        button.id =
            "theme-toggle";

        button.type =
            "button";

        button.className =
            "theme-toggle";

        function updateButton() {

            const dark =
                document.body
                    .classList
                    .contains(
                        "dark-mode"
                    );

            button.textContent =
                dark
                    ? "☀️"
                    : "🌙";

            button.title =
                dark
                    ? "Mode clair"
                    : "Mode sombre";
        }

        button.addEventListener(
            "click",
            function () {

                const dark =
                    document.body
                        .classList
                        .toggle(
                            "dark-mode"
                        );

                localStorage.setItem(
                    "theme",
                    dark
                        ? "dark"
                        : "light"
                );

                updateButton();
            }
        );

        document.body.appendChild(
            button
        );

        updateButton();
    }

    applyTheme(
        getInitialTheme()
    );

    createButton();

})();
