const usersCard =
    document.querySelector("#users-card");

const adminCard =
    document.querySelector("#admin-card");

const userInfo =
    document.querySelector("#user-info");

const totalContacts =
    document.querySelector("#total-contacts");

const totalFavorites =
    document.querySelector("#total-favorites");

const birthdayList =
    document.querySelector("#birthday-list");

function loadProfile() {

    fetch("/profile", {
        credentials: "include"
    })

        .then(function (response) {

            if (response.status === 401) {

                window.location.href =
                    "/login.html";

                throw new Error(
                    "Utilisateur non connecté"
                );
            }

            if (!response.ok) {

                throw new Error(
                    "Impossible de récupérer le profil"
                );
            }

            return response.json();
        })


        .then(function (profile) {

            userInfo.textContent =
                profile.email;

            if (profile.admin === true) {

                usersCard.hidden = false;

                adminCard.hidden = false;
            }
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );

            userInfo.textContent =
                "Impossible de charger le profil";
        });
}

function loadContacts() {

    fetch("/contacts", {
        credentials: "include"
    })

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Impossible de récupérer les contacts"
                );
            }

            return response.json();
        })

        .then(function (contacts) {

            totalContacts.textContent =
                contacts.length;

            const favorites =
                contacts.filter(
                    function (contact) {

                        return contact.favorite === true;
                    }
                );

            totalFavorites.textContent =
                favorites.length;

            displayUpcomingBirthdays(
                contacts
            );
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );
        });
}

function displayUpcomingBirthdays(contacts) {

    const upcoming = [];

    contacts.forEach(
        function (contact) {

            if (!contact.birthday) {
                return;
            }

            const days =
                getDaysUntilBirthday(
                    contact.birthday
                );

            if (
                days >= 0 &&
                days <= 30
            ) {

                upcoming.push({
                    contact: contact,
                    days: days
                });
            }
        }
    );

    upcoming.sort(
        function (a, b) {

            return a.days -
                b.days;
        }
    );

    birthdayList.innerHTML = "";

    if (upcoming.length === 0) {

        const empty =
            document.createElement("p");

        empty.classList.add(
            "birthday-empty"
        );

        empty.textContent =
            "Aucun anniversaire dans les 30 prochains jours.";

        birthdayList.appendChild(
            empty
        );

        return;
    }

    upcoming.forEach(
        function (item) {

            const card =
                document.createElement("a");

            card.classList.add(
                "birthday-card"
            );

            card.href =
                "/contact.html?id=" +
                item.contact.id;

            const avatar =
                document.createElement("div");

            avatar.classList.add(
                "birthday-avatar"
            );

            avatar.textContent =
                item.contact.name
                    ? item.contact.name
                        .charAt(0)
                        .toUpperCase()
                    : "?";

            const content =
                document.createElement("div");

            const name =
                document.createElement("strong");

            name.textContent =
                item.contact.name;

            const date =
                document.createElement("span");

            date.textContent =
                formatBirthday(
                    item.contact.birthday
                );

            const countdown =
                document.createElement("span");

            countdown.classList.add(
                "birthday-countdown"
            );

            countdown.textContent =
                getBirthdayMessage(
                    item.days
                );

            content.appendChild(
                name
            );

            content.appendChild(
                date
            );

            content.appendChild(
                countdown
            );

            card.appendChild(
                avatar
            );

            card.appendChild(
                content
            );

            birthdayList.appendChild(
                card
            );
        }
    );
}

function getDaysUntilBirthday(
    birthday
) {

    const today =
        new Date();

    today.setHours(
        0,
        0,
        0,
        0
    );

    const parts =
        birthday.split("-");

    const month =
        Number(parts[1]) - 1;

    const day =
        Number(parts[2]);

    let nextBirthday =
        new Date(
            today.getFullYear(),
            month,
            day
        );

    nextBirthday.setHours(
        0,
        0,
        0,
        0
    );

    if (
        nextBirthday <
        today
    ) {

        nextBirthday =
            new Date(
                today.getFullYear() + 1,
                month,
                day
            );
    }

    const difference =
        nextBirthday -
        today;

    return Math.round(
        difference /
        (
            1000 *
            60 *
            60 *
            24
        )
    );
}

function getBirthdayMessage(days) {

    if (days === 0) {
        return "Aujourd'hui 🎉";
    }

    if (days === 1) {
        return "Demain";
    }

    return "Dans " +
        days +
        " jours";
}

function formatBirthday(birthday) {

    const parts =
        birthday.split("-");

    const date =
        new Date(
            2000,
            Number(parts[1]) - 1,
            Number(parts[2])
        );

    return date.toLocaleDateString(
        "fr-FR",
        {
            day: "numeric",
            month: "long"
        }
    );
}

loadProfile();
loadContacts();