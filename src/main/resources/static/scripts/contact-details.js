const params =
    new URLSearchParams(
        window.location.search
    );

const contactId =
    params.get("id");

const contactPhoto =
    document.querySelector("#contact-photo");

const contactAvatar =
    document.querySelector("#contact-avatar");

const photoInput =
    document.querySelector("#photo-input");

const choosePhotoButton =
    document.querySelector("#choose-photo-button");

const deletePhotoButton =
    document.querySelector("#delete-photo-button");

const photoMessage =
    document.querySelector("#photo-message");

if (!contactId) {

    window.location.href =
        "/contacts.html";
}

function loadContact() {

    fetch(
        "/contacts/" + contactId,
        {
            credentials: "include"
        }
    )

        .then(function (response) {

            if (response.status === 404) {

                throw new Error(
                    "Contact introuvable"
                );
            }

            if (!response.ok) {

                throw new Error(
                    "Impossible de charger le contact"
                );
            }

            return response.json();
        })

        .then(function (contact) {

            document
                .querySelector("#detail-name")
                .textContent =
                contact.name;

            document
                .querySelector("#contact-initial")
                .textContent =
                contact.name
                    ? contact.name
                        .charAt(0)
                        .toUpperCase()
                    : "?";

            document
                .querySelector("#detail-category")
                .textContent =
                contact.category ||
                "Autre";

            document
                .querySelector("#detail-email")
                .textContent =
                contact.email || "—";

            document
                .querySelector("#detail-phone")
                .textContent =
                contact.phone || "—";

            document
                .querySelector("#detail-company")
                .textContent =
                contact.company || "—";

            document
                .querySelector("#detail-address")
                .textContent =
                contact.address || "—";

            document
                .querySelector("#detail-birthday")
                .textContent =
                formatDate(
                    contact.birthday
                );

            document
                .querySelector("#detail-favorite")
                .textContent =
                contact.favorite
                    ? "⭐ Oui"
                    : "Non";

            document
                .querySelector("#detail-notes")
                .textContent =
                contact.notes ||
                "Aucune note";

            loadPhoto();
        })

        .catch(function (error) {

            document
                .querySelector("#detail-name")
                .textContent =
                error.message;
        });
}

function loadPhoto() {

    const imageUrl =
        "/contacts/" +
        contactId +
        "/photo?t=" +
        Date.now();

    contactPhoto.onload =
        function () {

            contactPhoto.hidden = false;

            contactAvatar.hidden = true;
        };

    contactPhoto.onerror =
        function () {

            contactPhoto.hidden = true;

            contactAvatar.hidden = false;
        };

    contactPhoto.src =
        imageUrl;
}

choosePhotoButton.addEventListener(
    "click",
    function () {

        photoInput.click();
    }
);

photoInput.addEventListener(
    "change",
    function () {

        const file =
            photoInput.files[0];


        if (!file) {
            return;
        }

        const data =
            new FormData();


        data.append(
            "photo",
            file
        );

        fetch(
            "/contacts/" +
            contactId +
            "/photo",
            {

                method: "POST",

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

                photoMessage.textContent =
                    message;

                photoInput.value = "";

                loadPhoto();
            })

            .catch(function (error) {

                photoMessage.textContent =
                    error.message;
            });
    }
);

deletePhotoButton.addEventListener(
    "click",
    function () {

        fetch(
            "/contacts/" +
            contactId +
            "/photo",
            {

                method: "DELETE",

                credentials: "include"
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

                photoMessage.textContent =
                    message;

                contactPhoto.hidden = true;

                contactAvatar.hidden = false;
            })

            .catch(function (error) {

                photoMessage.textContent =
                    error.message;
            });
    }
);

function formatDate(date) {

    if (!date) {
        return "—";
    }

    const value =
        new Date(
            date + "T00:00:00"
        );

    return value.toLocaleDateString(
        "fr-FR",
        {
            day: "numeric",
            month: "long",
            year: "numeric"
        }
    );
}

loadContact();