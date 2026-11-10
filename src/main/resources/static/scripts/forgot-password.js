const form =
    document.querySelector(
        "#forgot-password-form"
    );

const emailInput =
    document.querySelector(
        "#forgot-email"
    );

const message =
    document.querySelector(
        "#forgot-message"
    );

form.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        const data =
            new URLSearchParams();

        data.append(
            "email",
            emailInput.value
        );

        fetch(
            "/forgot-password",
            {

                method: "POST",

                body: data
            }
        )

            .then(function (response) {

                return response.text();
            })

            .then(function (text) {

                message.textContent =
                    text;

                form.reset();
            })

            .catch(function () {

                message.textContent =
                    "Impossible d'effectuer la demande";
            });
    }
);