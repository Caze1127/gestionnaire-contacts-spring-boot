const params =
    new URLSearchParams(
        window.location.search
    );

const token =
    params.get("token");

const form =
    document.querySelector(
        "#reset-password-form"
    );

const passwordInput =
    document.querySelector(
        "#new-reset-password"
    );

const confirmInput =
    document.querySelector(
        "#confirm-reset-password"
    );

const message =
    document.querySelector(
        "#reset-message"
    );

if (!token) {

    message.textContent =
        "Lien de réinitialisation invalide";

    form.hidden = true;
}

form.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        if (
            passwordInput.value !==
            confirmInput.value
        ) {

            message.textContent =
                "Les mots de passe ne correspondent pas";

            return;
        }

        const data =
            new URLSearchParams();

        data.append(
            "token",
            token
        );

        data.append(
            "newPassword",
            passwordInput.value
        );

        fetch(
            "/reset-password",
            {

                method: "POST",

                body: data
            }
        )

            .then(function (response) {

                return response
                    .text()
                    .then(
                        function (text) {

                            if (!response.ok) {

                                throw new Error(
                                    text
                                );
                            }

                            return text;
                        }
                    );
            })

            .then(function () {

                message.textContent =
                    "Mot de passe modifié. Vous pouvez maintenant vous connecter.";

                form.hidden = true;
            })

            .catch(function (error) {

                message.textContent =
                    error.message;
            });
    }
);