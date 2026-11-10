const totalUsers =
    document.querySelector(
        "#admin-total-users"
    );

const users =
    document.querySelector(
        "#admin-users"
    );

const admins =
    document.querySelector(
        "#admin-admins"
    );

const totalContacts =
    document.querySelector(
        "#admin-total-contacts"
    );

const favorites =
    document.querySelector(
        "#admin-favorites"
    );

const family =
    document.querySelector(
        "#admin-family"
    );

const friends =
    document.querySelector(
        "#admin-friends"
    );

const work =
    document.querySelector(
        "#admin-work"
    );

const other =
    document.querySelector(
        "#admin-other"
    );

function loadAdminStats() {

    fetch(
        "/admin/stats",
        {
            credentials: "include"
        }
    )

        .then(function (response) {

            if (response.status === 403) {

                window.location.href =
                    "/index.html";

                throw new Error(
                    "Accès administrateur requis"
                );
            }

            if (!response.ok) {

                throw new Error(
                    "Impossible de charger les statistiques"
                );
            }

            return response.json();
        })

        .then(function (stats) {

            totalUsers.textContent =
                stats.totalUsers;

            users.textContent =
                stats.users;

            admins.textContent =
                stats.admins;

            totalContacts.textContent =
                stats.totalContacts;

            favorites.textContent =
                stats.favorites;

            family.textContent =
                stats.family;

            friends.textContent =
                stats.friends;

            work.textContent =
                stats.work;

            other.textContent =
                stats.other;
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );
        });
}

loadAdminStats();