const loginForm = document.querySelector("#login-form");
const emailInput = document.querySelector("#login-email");
const passwordInput = document.querySelector("#login-password");
const loginMessage = document.querySelector("#login-message");
const registerButton = document.querySelector("#register-button");

registerButton.addEventListener("click", function () {
    window.location.href = "register.html";
});

loginForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const data = new URLSearchParams();

    data.append("email", emailInput.value);
    data.append("password", passwordInput.value);

    fetch("/demo/login", {
        method: "POST",
        credentials: "include",
        body: data
    })
        .then(function (response) {

            return response.text()
                .then(function (text) {

                    if (!response.ok) {
                        throw new Error(text);
                    }

                    return text;
                });
        })

        .then(function (serverMessage) {

            loginMessage.textContent = serverMessage;

            window.location.href = "index.html";
        })

        .catch(function (error) {

            console.error("Erreur :", error);

            loginMessage.textContent = error.message;
        });
});