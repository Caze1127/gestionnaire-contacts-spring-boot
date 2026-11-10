const accountName =
    document.querySelector(
        "#account-name"
    );

const accountInitial =
    document.querySelector(
        "#account-initial"
    );

const accountRole =
    document.querySelector(
        "#account-role"
    );

const profileForm =
    document.querySelector(
        "#profile-form"
    );

const nameInput =
    document.querySelector(
        "#account-name-input"
    );

const emailInput =
    document.querySelector(
        "#account-email-input"
    );

const profileMessage =
    document.querySelector(
        "#profile-message"
    );

const passwordForm =
    document.querySelector(
        "#password-form"
    );

const currentPasswordInput =
    document.querySelector(
        "#current-password"
    );

const newPasswordInput =
    document.querySelector(
        "#new-password"
    );

const confirmPasswordInput =
    document.querySelector(
        "#confirm-password"
    );

const passwordMessage =
    document.querySelector(
        "#password-message"
    );

let originalEmail = "";

function loadProfile() {

    fetch(
        "/profile",
        {
            credentials: "include"
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Impossible de charger le profil"
                );
            }

            return response.json();
        })

        .then(function (profile) {

            originalEmail =
                profile.email;

            accountName.textContent =
                profile.name;

            accountInitial.textContent =
                profile.name
                    ? profile.name
                        .charAt(0)
                        .toUpperCase()
                    : "?";

            accountRole.textContent =
                profile.admin
                    ? "Administrateur"
                    : "Utilisateur";

            nameInput.value =
                profile.name;


            emailInput.value =
                profile.email;
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );
        });
}

profileForm.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();


        const data =
            new URLSearchParams();

        data.append(
            "name",
            nameInput.value
        );

        data.append(
            "email",
            emailInput.value
        );

        fetch(
            "/account/profile",
            {

                method: "PUT",

                credentials: "include",

                body: data
            }
        )

            .then(function (response) {

                return response
                    .text()
                    .then(function (text) {

                        if (!response.ok) {

                            throw new Error(
                                text
                            );
                        }

                        return text;
                    });
            })

            .then(function (message) {

                profileMessage.textContent =
                    message;

                if (
                    emailInput.value !==
                    originalEmail
                ) {

                    profileMessage.textContent =
                        "E-mail modifié. Reconnexion nécessaire.";

                    setTimeout(
                        function () {

                            logoutAndRedirect();
                        },
                        1000
                    );

                    return;
                }

                loadProfile();
            })

            .catch(function (error) {

                profileMessage.textContent =
                    error.message;
            });
    }
);

passwordForm.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        if (
            newPasswordInput.value !==
            confirmPasswordInput.value
        ) {

            passwordMessage.textContent =
                "Les nouveaux mots de passe ne correspondent pas";

            return;
        }

        const data =
            new URLSearchParams();

        data.append(
            "currentPassword",
            currentPasswordInput.value
        );

        data.append(
            "newPassword",
            newPasswordInput.value
        );

        fetch(
            "/account/password",
            {

                method: "PUT",

                credentials: "include",

                body: data
            }
        )

            .then(function (response) {

                return response
                    .text()
                    .then(function (text) {

                        if (!response.ok) {

                            throw new Error(
                                text
                            );
                        }

                        return text;
                    });
            })

            .then(function () {

                passwordMessage.textContent =
                    "Mot de passe modifié. Reconnexion nécessaire.";

                passwordForm.reset();

                setTimeout(
                    function () {

                        logoutAndRedirect();
                    },
                    1000
                );
            })

            .catch(function (error) {

                passwordMessage.textContent =
                    error.message;
            });
    }
);

function logoutAndRedirect() {

    fetch(
        "/logout",
        {

            method: "POST",

            credentials: "include"
        }
    )

        .finally(
            function () {

                window.location.href =
                    "/login.html";
            }
        );
}

loadProfile();