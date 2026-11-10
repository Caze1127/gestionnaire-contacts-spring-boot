const registerForm = document.querySelector("#register-form");
const nameInput = document.querySelector("#register-name");
const emailInput = document.querySelector("#register-email");
const passwordInput = document.querySelector("#register-password");
const registerMessage = document.querySelector("#register-message");
const backLoginButton = document.querySelector("#back-login-button");

registerForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const data = new URLSearchParams();

    data.append("name", nameInput.value);
    data.append("email", emailInput.value);
    data.append("password", passwordInput.value);

    fetch("/register", {
        method: "POST",
        body: data
    })
        .then(function (response) {
            return response.text().then(function (text) {
                if (!response.ok) {
                    throw new Error(text);
                }

                return text;
            });
        })
        .then(function (message) {
            registerMessage.textContent = message;

            setTimeout(function () {
                window.location.href = "login.html";
            }, 1000);
        })
        .catch(function (error) {
            registerMessage.textContent = error.message;
        });
});

backLoginButton.addEventListener("click", function () {
    window.location.href = "login.html";
});