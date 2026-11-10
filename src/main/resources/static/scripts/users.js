const form =
    document.querySelector("#user-form");

const nameInput =
    document.querySelector("#name");

const emailInput =
    document.querySelector("#email");

const passwordInput =
    document.querySelector("#password");

const message =
    document.querySelector("#message");

const tableBody =
    document.querySelector("#users-table-body");

const refreshUsersButton =
    document.querySelector("#refresh-users");

let editingUserId = null;

function fetchUsers() {

    fetch("/demo/all", {

        credentials: "include"

    })

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Impossible de charger les utilisateurs"
                );
            }

            return response.json();
        })

        .then(function (users) {

            tableBody.innerHTML = "";

            users.forEach(
                function (user) {

                    const row =
                        document.createElement("tr");

                    const idCell =
                        document.createElement("td");

                    idCell.textContent =
                        user.id;

                    const nameCell =
                        document.createElement("td");

                    nameCell.textContent =
                        user.name;

                    const emailCell =
                        document.createElement("td");

                    emailCell.textContent =
                        user.email;

                    const roleCell =
                        document.createElement("td");

                    const roleBadge =
                        document.createElement("span");

                    roleBadge.textContent =
                        user.role || "USER";


                    roleBadge.classList.add(
                        "role-badge"
                    );

                    if (user.role === "ADMIN") {

                        roleBadge.classList.add(
                            "role-admin"
                        );

                    } else {

                        roleBadge.classList.add(
                            "role-user"
                        );
                    }

                    roleCell.appendChild(
                        roleBadge
                    );

                    const actionsCell =
                        document.createElement("td");

                    const editButton =
                        document.createElement("button");

                    editButton.textContent =
                        "Modifier";

                    editButton.type =
                        "button";

                    editButton.classList.add(
                        "edit-button"
                    );

                    editButton.addEventListener(
                        "click",
                        function () {

                            editingUserId =
                                user.id;

                            nameInput.value =
                                user.name;

                            emailInput.value =
                                user.email;

                            passwordInput.value =
                                "";

                            passwordInput.required =
                                false;

                            form
                                .querySelector(
                                    "button[type='submit']"
                                )
                                .textContent =
                                "Enregistrer";


                            message.textContent =
                                "Modification de " +
                                user.name;
                        }
                    );

                    const deleteButton =
                        document.createElement("button");

                    deleteButton.textContent =
                        "Supprimer";

                    deleteButton.type =
                        "button";

                    deleteButton.classList.add(
                        "delete-button"
                    );

                    deleteButton.addEventListener(
                        "click",
                        function () {

                            deleteUser(
                                user.id
                            );
                        }
                    );

                    actionsCell.appendChild(
                        editButton
                    );

                    actionsCell.appendChild(
                        deleteButton
                    );

                    if (user.role !== "ADMIN") {

                        const promoteButton =
                            document.createElement(
                                "button"
                            );

                        promoteButton.textContent =
                            "Rendre admin";

                        promoteButton.type =
                            "button";

                        promoteButton.classList.add(
                            "admin-button"
                        );

                        promoteButton.addEventListener(
                            "click",
                            function () {

                                promoteUser(
                                    user.id,
                                    user.name
                                );
                            }
                        );

                        actionsCell.appendChild(
                            promoteButton
                        );
                    }

                    row.appendChild(
                        idCell
                    );

                    row.appendChild(
                        nameCell
                    );

                    row.appendChild(
                        emailCell
                    );

                    row.appendChild(
                        roleCell
                    );

                    row.appendChild(
                        actionsCell
                    );

                    tableBody.appendChild(
                        row
                    );
                }
            );
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );

            message.textContent =
                error.message;
        });
}

function promoteUser(
    id,
    name
) {

    fetch(
        "/admin/users/" +
        id +
        "/promote",
        {

            method: "PUT",

            credentials: "include"
        }
    )

        .then(function (response) {

            return response
                .text()
                .then(function (text) {

                    if (!response.ok) {

                        throw new Error(
                            text ||
                            "Impossible de rendre cet utilisateur administrateur"
                        );
                    }

                    return text;
                });
        })

        .then(function (serverMessage) {

            message.textContent =
                name +
                " est maintenant administrateur";


            fetchUsers();
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );

            message.textContent =
                error.message;
        });
}

form.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        if (editingUserId === null) {

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

            data.append(
                "password",
                passwordInput.value
            );

            fetch("/demo/add", {

                method: "POST",

                credentials: "include",

                body: data

            })

                .then(function (response) {

                    if (!response.ok) {

                        throw new Error(
                            "Impossible d'ajouter l'utilisateur"
                        );
                    }

                    return response.text();
                })

                .then(function (serverMessage) {

                    message.textContent =
                        serverMessage;

                    form.reset();

                    fetchUsers();
                })

                .catch(function (error) {

                    message.textContent =
                        error.message;
                });

        } else {

            const updatedUser = {

                name:
                nameInput.value,

                email:
                emailInput.value
            };

            fetch(
                "/demo/update/" +
                editingUserId,
                {

                    method: "PUT",

                    credentials: "include",

                    headers: {

                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            updatedUser
                        )
                }
            )

                .then(function (response) {

                    if (!response.ok) {

                        throw new Error(
                            "Impossible de modifier l'utilisateur"
                        );
                    }

                    return response.text();
                })

                .then(function (serverMessage) {

                    message.textContent =
                        serverMessage;

                    editingUserId = null;

                    form.reset();

                    passwordInput.required =
                        true;

                    form
                        .querySelector(
                            "button[type='submit']"
                        )
                        .textContent =
                        "Ajouter";

                    fetchUsers();
                })

                .catch(function (error) {

                    message.textContent =
                        error.message;
                });
        }
    }
);

function deleteUser(
    id
) {

    const confirmed =
        window.confirm(
            "Voulez-vous vraiment supprimer cet utilisateur ? Cette action est définitive."
        );

    if (!confirmed) {
        return;
    }

    fetch(
        "/demo/delete/" + id,
        {

            method: "DELETE",

            credentials: "include"
        }
    )

        .then(function (response) {

            return response
                .text()
                .then(
                    function (text) {

                        if (!response.ok) {

                            throw new Error(
                                text ||
                                "Impossible de supprimer l'utilisateur"
                            );
                        }


                        return text;
                    }
                );
        })

        .then(function (serverMessage) {

            message.textContent =
                serverMessage;

            fetchUsers();
        })

        .catch(function (error) {

            message.textContent =
                error.message;
        });
}

refreshUsersButton.addEventListener(
    "click",
    function () {

        fetchUsers();
    }
);
fetchUsers();