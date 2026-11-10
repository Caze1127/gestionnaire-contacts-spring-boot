const contactForm =
    document.querySelector("#contact-form");

const contactNameInput =
    document.querySelector("#contact-name");

const contactEmailInput =
    document.querySelector("#contact-email");

const contactPhoneInput =
    document.querySelector("#contact-phone");

const contactCategoryInput =
    document.querySelector("#contact-category");

const contactCompanyInput =
    document.querySelector("#contact-company");

const contactAddressInput =
    document.querySelector("#contact-address");

const contactBirthdayInput =
    document.querySelector("#contact-birthday");

const contactNotesInput =
    document.querySelector("#contact-notes");

const contactMessage =
    document.querySelector("#contact-message");

const contactsTableBody =
    document.querySelector("#contacts-table-body");

const refreshContactsButton =
    document.querySelector("#refresh-contacts");

const contactSearchInput =
    document.querySelector("#contact-search");

const contactSortInput =
    document.querySelector("#contact-sort");

const contactsCount =
    document.querySelector("#contacts-count");

const filterAllButton =
    document.querySelector("#filter-all");

const filterFavoritesButton =
    document.querySelector("#filter-favorites");

const categoryButtons =
    document.querySelectorAll(".category-filter");

const exportCsvButton =
    document.querySelector("#export-csv");

const importCsvButton =
    document.querySelector("#import-csv");

const csvFileInput =
    document.querySelector("#csv-file-input");

let editingContactId = null;

let contactsList = [];

let currentFilter = "ALL";

function fetchContacts() {

    fetch("/contacts", {
        credentials: "include"
    })

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Impossible de charger les contacts"
                );
            }

            return response.json();
        })

        .then(function (contacts) {

            contactsList = contacts;

            applyFilters();
        })

        .catch(function (error) {

            console.error(
                "Erreur :",
                error
            );
        });
}

function applyFilters() {

    const search =
        contactSearchInput
            .value
            .toLowerCase()
            .trim();

    let contacts =
        contactsList.filter(
            function (contact) {

                const values = [

                    contact.name,
                    contact.email,
                    contact.phone,
                    contact.category,
                    contact.company,
                    contact.address

                ];


                return values.some(
                    function (value) {

                        return String(
                            value || ""
                        )
                            .toLowerCase()
                            .includes(search);
                    }
                );
            }
        );

    if (currentFilter === "FAVORITES") {

        contacts =
            contacts.filter(
                function (contact) {

                    return contact.favorite === true;
                }
            );

    } else if (currentFilter !== "ALL") {

        contacts =
            contacts.filter(
                function (contact) {

                    return contact.category ===
                        currentFilter;
                }
            );
    }

    contacts =
        sortContacts(
            contacts
        );


    displayContacts(
        contacts
    );
}

function sortContacts(contacts) {

    const sorted =
        [...contacts];

    const sortValue =
        contactSortInput.value;

    if (sortValue === "AZ") {

        sorted.sort(
            function (a, b) {

                return a.name.localeCompare(
                    b.name,
                    "fr",
                    {
                        sensitivity: "base"
                    }
                );
            }
        );
    }

    if (sortValue === "ZA") {

        sorted.sort(
            function (a, b) {

                return b.name.localeCompare(
                    a.name,
                    "fr",
                    {
                        sensitivity: "base"
                    }
                );
            }
        );
    }

    if (sortValue === "FAVORITES") {

        sorted.sort(
            function (a, b) {

                return Number(
                        Boolean(b.favorite)
                    ) -
                    Number(
                        Boolean(a.favorite)
                    );
            }
        );
    }

    if (sortValue === "CATEGORY") {

        sorted.sort(
            function (a, b) {

                return String(
                    a.category || "Autre"
                ).localeCompare(
                    String(
                        b.category || "Autre"
                    ),
                    "fr"
                );
            }
        );
    }

    if (sortValue === "NEWEST") {

        sorted.sort(
            function (a, b) {

                return getContactDate(b) -
                    getContactDate(a);
            }
        );
    }

    if (sortValue === "OLDEST") {

        sorted.sort(
            function (a, b) {

                return getContactDate(a) -
                    getContactDate(b);
            }
        );
    }

    return sorted;
}

function getContactDate(contact) {

    if (!contact.createdAt) {
        return 0;
    }

    return new Date(
        contact.createdAt
    ).getTime();
}

function displayContacts(contacts) {

    contactsTableBody.innerHTML = "";

    contactsCount.textContent =
        contacts.length +
        " contact(s)";

    contacts.forEach(
        function (contact) {

            const row =
                document.createElement("tr");

            const contactCell =
                document.createElement("td");

            const profile =
                document.createElement("div");

            profile.classList.add(
                "contact-list-profile"
            );

            const visual =
                document.createElement("div");

            visual.classList.add(
                "contact-list-visual"
            );

            const avatar =
                document.createElement("div");

            avatar.classList.add(
                "contact-list-avatar"
            );

            avatar.textContent =
                contact.name
                    ? contact.name
                        .charAt(0)
                        .toUpperCase()
                    : "?";

            const photo =
                document.createElement("img");

            photo.classList.add(
                "contact-list-photo"
            );

            photo.alt =
                "Photo de " +
                contact.name;

            photo.hidden = true;


            photo.onload =
                function () {

                    photo.hidden = false;

                    avatar.hidden = true;
                };

            photo.onerror =
                function () {

                    photo.hidden = true;

                    avatar.hidden = false;
                };

            photo.src =
                "/contacts/" +
                contact.id +
                "/photo";

            visual.appendChild(
                avatar
            );

            visual.appendChild(
                photo
            );

            const name =
                document.createElement("span");

            name.textContent =
                contact.name;

            profile.appendChild(
                visual
            );

            profile.appendChild(
                name
            );

            contactCell.appendChild(
                profile
            );

            const emailCell =
                document.createElement("td");

            emailCell.textContent =
                contact.email;


            const phoneCell =
                document.createElement("td");

            phoneCell.textContent =
                contact.phone;


            const categoryCell =
                document.createElement("td");

            categoryCell.textContent =
                contact.category || "Autre";


            const favoriteCell =
                document.createElement("td");


            const favoriteButton =
                document.createElement("button");

            favoriteButton.type =
                "button";

            favoriteButton.classList.add(
                "favorite-button"
            );

            if (contact.favorite === true) {

                favoriteButton.textContent =
                    "★";

                favoriteButton.classList.add(
                    "favorite-active"
                );

            } else {

                favoriteButton.textContent =
                    "☆";
            }

            favoriteButton.addEventListener(
                "click",
                function () {

                    toggleFavorite(
                        contact.id
                    );
                }
            );

            favoriteCell.appendChild(
                favoriteButton
            );

            const actionsCell =
                document.createElement("td");


            const viewButton =
                document.createElement("button");

            viewButton.textContent =
                "Voir";

            viewButton.type =
                "button";

            viewButton.classList.add(
                "view-button"
            );

            viewButton.addEventListener(
                "click",
                function () {

                    window.location.href =
                        "/contact.html?id=" +
                        contact.id;
                }
            );

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

                    editingContactId =
                        contact.id;

                    contactNameInput.value =
                        contact.name || "";

                    contactEmailInput.value =
                        contact.email || "";

                    contactPhoneInput.value =
                        contact.phone || "";

                    contactCategoryInput.value =
                        contact.category || "Autre";

                    contactCompanyInput.value =
                        contact.company || "";

                    contactAddressInput.value =
                        contact.address || "";

                    contactBirthdayInput.value =
                        contact.birthday || "";

                    contactNotesInput.value =
                        contact.notes || "";

                    contactForm
                        .querySelector(
                            "button[type='submit']"
                        )
                        .textContent =
                        "Enregistrer";

                    contactMessage.textContent =
                        "Modification de " +
                        contact.name;


                    window.scrollTo({
                        top: 0,
                        behavior: "smooth"
                    });
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

                    deleteContact(
                        contact.id
                    );
                }
            );

            actionsCell.appendChild(
                viewButton
            );

            actionsCell.appendChild(
                editButton
            );

            actionsCell.appendChild(
                deleteButton
            );


            row.appendChild(
                contactCell
            );

            row.appendChild(
                emailCell
            );

            row.appendChild(
                phoneCell
            );

            row.appendChild(
                categoryCell
            );

            row.appendChild(
                favoriteCell
            );

            row.appendChild(
                actionsCell
            );


            contactsTableBody.appendChild(
                row
            );
        }
    );
}

function toggleFavorite(id) {

    fetch(
        "/contacts/" +
        id +
        "/favorite",
        {

            method: "PUT",

            credentials: "include"
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Impossible de modifier le favori"
                );
            }

            return response.text();
        })

        .then(function () {

            fetchContacts();
        })

        .catch(function (error) {

            contactMessage.textContent =
                error.message;
        });
}

contactForm.addEventListener(
    "submit",
    function (event) {

        event.preventDefault();

        if (editingContactId === null) {

            const data =
                new URLSearchParams();


            data.append(
                "name",
                contactNameInput.value
            );

            data.append(
                "email",
                contactEmailInput.value
            );

            data.append(
                "phone",
                contactPhoneInput.value
            );

            data.append(
                "category",
                contactCategoryInput.value
            );

            data.append(
                "company",
                contactCompanyInput.value
            );

            data.append(
                "address",
                contactAddressInput.value
            );


            if (
                contactBirthdayInput.value !== ""
            ) {

                data.append(
                    "birthday",
                    contactBirthdayInput.value
                );
            }


            data.append(
                "notes",
                contactNotesInput.value
            );


            fetch("/contacts", {

                method: "POST",

                credentials: "include",

                body: data

            })

                .then(function (response) {

                    if (!response.ok) {

                        throw new Error(
                            "Impossible d'ajouter le contact"
                        );
                    }

                    return response.json();
                })

                .then(function () {

                    contactMessage.textContent =
                        "Contact ajouté";

                    contactForm.reset();

                    fetchContacts();
                })

                .catch(function (error) {

                    contactMessage.textContent =
                        error.message;
                });

        } else {

            const updatedContact = {

                name:
                contactNameInput.value,

                email:
                contactEmailInput.value,

                phone:
                contactPhoneInput.value,

                category:
                contactCategoryInput.value,

                company:
                contactCompanyInput.value,

                address:
                contactAddressInput.value,

                birthday:
                    contactBirthdayInput.value || null,

                notes:
                contactNotesInput.value
            };

            fetch(
                "/contacts/" +
                editingContactId,
                {

                    method: "PUT",

                    credentials:
                        "include",

                    headers: {

                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            updatedContact
                        )
                }
            )

                .then(function (response) {

                    if (!response.ok) {

                        throw new Error(
                            "Impossible de modifier le contact"
                        );
                    }

                    return response.text();
                })

                .then(function (message) {

                    contactMessage.textContent =
                        message;

                    editingContactId =
                        null;

                    contactForm.reset();

                    contactForm
                        .querySelector(
                            "button[type='submit']"
                        )
                        .textContent =
                        "Ajouter";

                    fetchContacts();
                });
        }
    }
);

function deleteContact(
    id
) {

    const contact =
        contactsList.find(
            function (item) {

                return item.id === id;
            }
        );

    const name =
        contact
            ? contact.name
            : "ce contact";


    const confirmed =
        window.confirm(
            "Voulez-vous vraiment supprimer "
            + name
            + " ?"
        );

    if (!confirmed) {
        return;
    }

    fetch(
        "/contacts/" + id,
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
                                "Impossible de supprimer le contact"
                            );
                        }

                        return text;
                    }
                );
        })

        .then(function (serverMessage) {

            contactMessage.textContent =
                serverMessage;

            editingContactId = null;

            contactForm.reset();

            fetchContacts();
        })

        .catch(function (error) {

            contactMessage.textContent =
                error.message;
        });
}

function setActiveFilter(button) {

    document
        .querySelectorAll(
            ".filter-button"
        )
        .forEach(
            function (filterButton) {

                filterButton
                    .classList
                    .remove(
                        "active-filter"
                    );
            }
        );


    button.classList.add(
        "active-filter"
    );
}

filterAllButton.addEventListener(
    "click",
    function () {

        currentFilter = "ALL";

        setActiveFilter(
            filterAllButton
        );

        applyFilters();
    }
);

filterFavoritesButton.addEventListener(
    "click",
    function () {

        currentFilter =
            "FAVORITES";

        setActiveFilter(
            filterFavoritesButton
        );

        applyFilters();
    }
);

categoryButtons.forEach(
    function (button) {

        button.addEventListener(
            "click",
            function () {

                currentFilter =
                    button.dataset.category;

                setActiveFilter(
                    button
                );

                applyFilters();
            }
        );
    }
);

contactSearchInput.addEventListener(
    "input",
    function () {

        applyFilters();
    }
);

contactSortInput.addEventListener(
    "change",
    function () {

        applyFilters();
    }
);

function csvValue(value) {

    const text =
        String(value ?? "");

    return "\"" +
        text.replaceAll(
            "\"",
            "\"\""
        ) +
        "\"";
}

function exportContacts() {

    if (contactsList.length === 0) {

        contactMessage.textContent =
            "Aucun contact à exporter";

        return;
    }

    const rows = [

        "Nom;Email;Téléphone;Catégorie;Favori;Entreprise;Adresse;Anniversaire;Notes"

    ];

    contactsList.forEach(
        function (contact) {

            rows.push(
                [
                    csvValue(contact.name),
                    csvValue(contact.email),
                    csvValue(contact.phone),
                    csvValue(contact.category),
                    csvValue(
                        contact.favorite
                            ? "Oui"
                            : "Non"
                    ),
                    csvValue(contact.company),
                    csvValue(contact.address),
                    csvValue(contact.birthday),
                    csvValue(contact.notes)
                ].join(";")
            );
        }
    );

    const blob =
        new Blob(
            [
                "\uFEFF" +
                rows.join("\n")
            ],
            {
                type:
                    "text/csv;charset=utf-8"
            }
        );

    const url =
        URL.createObjectURL(blob);


    const link =
        document.createElement("a");

    link.href = url;

    link.download =
        "contacts.csv";

    document.body.appendChild(
        link
    );

    link.click();

    document.body.removeChild(
        link
    );

    URL.revokeObjectURL(
        url
    );

    contactMessage.textContent =
        "Contacts exportés";
}

function parseCsvLine(line) {

    const values = [];

    let current = "";

    let quotes = false;

    for (
        let i = 0;
        i < line.length;
        i++
    ) {

        const char =
            line[i];

        if (char === "\"") {

            if (
                quotes &&
                line[i + 1] === "\""
            ) {

                current += "\"";

                i++;

            } else {

                quotes =
                    !quotes;
            }

        } else if (
            char === ";" &&
            !quotes
        ) {

            values.push(
                current
            );

            current = "";

        } else {

            current += char;
        }
    }

    values.push(
        current
    );

    return values;
}

function importOneContact(contact) {

    const data =
        new URLSearchParams();

    data.append(
        "name",
        contact.name
    );

    data.append(
        "email",
        contact.email
    );

    data.append(
        "phone",
        contact.phone
    );

    data.append(
        "category",
        contact.category
    );

    data.append(
        "company",
        contact.company
    );

    data.append(
        "address",
        contact.address
    );

    data.append(
        "notes",
        contact.notes
    );

    if (contact.birthday) {

        data.append(
            "birthday",
            contact.birthday
        );
    }

    return fetch(
        "/contacts",
        {

            method: "POST",

            credentials: "include",

            body: data
        }
    )

        .then(function (response) {

            if (!response.ok) {

                throw new Error(
                    "Erreur import"
                );
            }

            return response.json();
        })

        .then(function (created) {

            if (!contact.favorite) {

                return created;
            }

            return fetch(
                "/contacts/" +
                created.id +
                "/favorite",
                {

                    method: "PUT",

                    credentials:
                        "include"
                }
            );
        });
}

function importCsv(file) {

    file.text()

        .then(function (text) {

            const lines =
                text
                    .replace(
                        /^\uFEFF/,
                        ""
                    )
                    .split(/\r?\n/)
                    .filter(
                        function (line) {

                            return line
                                .trim()
                                .length > 0;
                        }
                    );

            const contacts =
                [];

            for (
                let i = 1;
                i < lines.length;
                i++
            ) {

                const values =
                    parseCsvLine(
                        lines[i]
                    );

                const name =
                    (values[0] || "")
                        .trim();

                const email =
                    (values[1] || "")
                        .trim();


                if (
                    name === "" ||
                    email === ""
                ) {

                    continue;
                }

                contacts.push({

                    name: name,

                    email: email,

                    phone:
                        (values[2] || "")
                            .trim(),

                    category:
                        (values[3] || "Autre")
                            .trim(),

                    favorite:
                        (values[4] || "")
                            .trim()
                            .toLowerCase() ===
                        "oui",

                    company:
                        (values[5] || "")
                            .trim(),

                    address:
                        (values[6] || "")
                            .trim(),

                    birthday:
                        (values[7] || "")
                            .trim(),

                    notes:
                        (values[8] || "")
                            .trim()
                });
            }

            return Promise.allSettled(
                contacts.map(
                    function (contact) {

                        return importOneContact(
                            contact
                        );
                    }
                )
            );
        })

        .then(function (results) {

            const success =
                results.filter(
                    function (result) {

                        return result.status ===
                            "fulfilled";
                    }
                ).length;

            contactMessage.textContent =
                success +
                " contact(s) importé(s)";

            csvFileInput.value =
                "";

            fetchContacts();
        })

        .catch(function (error) {

            contactMessage.textContent =
                error.message;
        });
}

exportCsvButton.addEventListener(
    "click",
    exportContacts
);

importCsvButton.addEventListener(
    "click",
    function () {

        csvFileInput.click();
    }
);

csvFileInput.addEventListener(
    "change",
    function () {

        const file =
            csvFileInput.files[0];

        if (file) {

            importCsv(file);
        }
    }
);

refreshContactsButton.addEventListener(
    "click",
    function () {

        contactSearchInput.value = "";

        contactSortInput.value =
            "AZ";

        currentFilter =
            "ALL";

        setActiveFilter(
            filterAllButton
        );

        fetchContacts();
    }
);

fetchContacts();