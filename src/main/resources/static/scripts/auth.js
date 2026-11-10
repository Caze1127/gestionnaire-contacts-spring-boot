const logoutButton =
    document.querySelector("#logout-button");


if (logoutButton) {

    logoutButton.addEventListener(
        "click",
        function () {

            fetch("/logout", {

                method: "POST",

                credentials: "include"

            })

                .then(function (response) {

                    if (!response.ok) {

                        throw new Error(
                            "Erreur pendant la déconnexion"
                        );
                    }

                    window.location.href =
                        "/login.html";
                })

                .catch(function (error) {

                    console.error(
                        "Erreur :",
                        error
                    );
                });
        }
    );
}