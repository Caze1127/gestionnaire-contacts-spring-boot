<div align="center">

# 📇 Gestionnaire de contacts
### Application web full-stack — Java · Spring Boot · MySQL

**Un carnet de contacts personnel avec authentification, rôles utilisateurs, tableaux de bord et interface claire/sombre.**

![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?logo=gradle&logoColor=white)
![Frontend](https://img.shields.io/badge/HTML%20%7C%20CSS%20%7C%20JavaScript-Frontend-2563EB)

[**Fonctionnalités**](#-fonctionnalités) · [**Captures**](#-aperçu-de-lapplication) · [**Installation**](#-installation-et-exécution) · [**Architecture**](#-architecture-du-projet)

</div>

---

## 🎯 Présentation

Ce projet est une **application web de gestion de contacts** développée avec **Java et Spring Boot** côté serveur et **HTML, CSS et JavaScript natif** côté interface. Les données sont persistées dans **MySQL** grâce à **Spring Data JPA**.

L'application permet à chaque utilisateur authentifié d'organiser son propre carnet de contacts. Elle propose des fonctionnalités d'administration accessibles aux comptes disposant du rôle `ADMIN`.

> **Portée :** projet de démonstration fonctionnelle en environnement local. Il n'est pas présenté comme un service prêt pour un déploiement public en production. Voir les [points de vigilance](#-sécurité-et-limites-actuelles).

## ✨ Fonctionnalités

| Espace | Fonctionnalités |
|---|---|
| **Authentification** | Création de compte, connexion, déconnexion et gestion de session via Spring Security |
| **Compte utilisateur** | Consultation et modification du nom, de l'adresse e-mail et du mot de passe |
| **Carnet de contacts** | Ajout, consultation, modification et suppression de contacts appartenant à l'utilisateur connecté |
| **Fiches détaillées** | Téléphone, e-mail, catégorie, entreprise, adresse, anniversaire, notes et photo |
| **Organisation** | Catégories `Famille`, `Amis`, `Travail`, `Autre`, favoris, recherche et tris |
| **Import/export** | Export CSV et import CSV depuis l'interface |
| **Tableau de bord** | Nombre de contacts, favoris et prochains anniversaires (sur 30 jours) |
| **Administration** | Gestion des comptes, promotion en administrateur et statistiques globales |
| **Interface** | Navigation multipage, affichage adaptatif et thèmes clair/sombre |
| **Mot de passe oublié** | Jeton de réinitialisation valable 30 minutes ; lien affiché dans la console **en développement**, sans envoi d'e-mail |

## 🖼️ Aperçu de l'application

### Accueil — modes clair et sombre

<p align="center">
  <img src="../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/accueil-clair.png" alt="Accueil en mode clair" width="49%" />
  <img src="../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/accueil-sombre.png" alt="Accueil en mode sombre" width="49%" />
</p>

### Gestion des contacts

![Formulaire et liste de contacts](../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/liste-contacts.png)

Recherche, filtres par catégorie, favoris, actions de gestion et import/export CSV.

### Fiche d'un contact

![Fiche détaillée d'un contact](../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/fiche-contact.png)

### Tableau de bord administrateur

![Statistiques administrateur](../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/dashboard-administration.png)

<details>
<summary><strong>Voir d'autres captures : comptes et utilisateurs</strong></summary>

#### Gestion des utilisateurs

![Gestion des utilisateurs](../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/gestion-utilisateurs.png)

#### Gestion du compte

![Gestion du compte](../faq/Projet%20info/gestionnaire-contacts-kit-github/docs/screenshots/mon-compte.png)

</details>

*Les données de démonstration identifiantes figurant sur certaines captures ont été masquées pour la publication.*

## 🧰 Technologies utilisées

| Couche | Technologies |
|---|---|
| Langage serveur | Java 17 |
| Framework web | Spring Boot 4.1.0, Spring Web |
| Authentification et autorisations | Spring Security, BCrypt, rôles `USER` / `ADMIN` |
| Persistance | Spring Data JPA / Hibernate, MySQL |
| Interface | HTML5, CSS3, JavaScript natif (`fetch`) |
| Build et tests | Gradle Wrapper, JUnit, configuration Testcontainers/MySQL |
| Environnement | Docker Compose pour MySQL en local |

Les versions correspondent au fichier `build.gradle` fourni avec le projet.

## 🏗️ Architecture du projet

```text
SQL2/
├── src/
│   ├── main/
│   │   ├── java/com/appli2/accessingdatamysql/
│   │   │   ├── config/          # Sécurité et configuration
│   │   │   ├── domain/          # Entités JPA : User, Contact, PasswordResetToken
│   │   │   ├── service/         # Logique métier
│   │   │   ├── util/            # Validation des données
│   │   │   ├── *Controller.java # Routes HTTP et endpoints REST
│   │   │   ├── *Repository.java # Accès aux données
│   │   │   └── AccessingDataMysqlApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── scripts/     # JavaScript client
│   │       │   ├── styles/      # Feuilles CSS
│   │       │   └── *.html      # Pages web
│   │       └── application.properties  # Configuration locale (non publiée si sensible)
│   └── test/                   # Tests et configuration Testcontainers
├── docs/screenshots/          # Captures d'écran anonymisées
├── gradle/                    # Gradle Wrapper
├── gradlew
├── gradlew.bat
├── build.gradle
├── settings.gradle
├── compose.yaml
└── README.md
```

### Principe de fonctionnement

1. **Frontend** : les pages HTML utilisent JavaScript pour appeler les endpoints HTTP avec `fetch`.
2. **Controllers** : exposent les routes liées à l'authentification, aux utilisateurs, aux contacts et à l'administration.
3. **Services** : appliquent les règles métier, notamment l'accès aux contacts du compte connecté.
4. **Repositories** : s'appuient sur Spring Data pour enregistrer et lire les entités dans MySQL.
5. **Sécurité** : Spring Security gère l'authentification et les autorisations par rôle.

### Modèle de données

- `User` : identité, e-mail, mot de passe haché et rôle.
- `Contact` : coordonnées, catégorie, favori, notes, anniversaire, photo et utilisateur propriétaire.
- `PasswordResetToken` : jeton temporaire associé à un compte, expirant au bout de 30 minutes.

## 🚀 Installation et exécution

### Prérequis

- **JDK 17** installé et configuré.
- **Docker** avec la commande `docker compose` pour démarrer MySQL localement.
- Un terminal PowerShell (Windows) ou un terminal Unix.
- Une configuration MySQL valide dans `src/main/resources/application.properties` (ou des variables d'environnement équivalentes).

### 1. Récupérer le projet

```powershell
git clone <URL_DU_REPOSITORY>
cd <NOM_DU_DOSSIER>
```

Remplacer ces deux valeurs par l'URL et le nom du repository une fois celui-ci créé sur GitHub.

### 2. Configurer la base de données

Une configuration d'exemple figure dans `src/main/resources/application.properties.example` (à ajouter depuis le kit de publication). La recopier si nécessaire sous le nom `application.properties`, puis l'adapter à l'environnement local.

```powershell
Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
```

Le fichier `application.properties` **ne doit pas contenir de véritables identifiants publiés**. Le mot de passe peut être fourni par la variable `SPRING_DATASOURCE_PASSWORD`.

Si votre `compose.yaml` utilise des variables, créer un fichier local `.env` à partir du modèle `.env.example`, puis définir les mots de passe :

```powershell
Copy-Item .env.example .env
# Modifier ensuite .env avec vos propres valeurs locales
```

> La configuration Docker Compose et la configuration de Spring doivent utiliser les **mêmes identifiants**. Le fichier `.env` de Docker Compose n'est pas automatiquement injecté dans le processus Java lancé séparément par `gradlew`.

### 3. Démarrer MySQL

```powershell
docker compose up -d
```

### 4. Lancer l'application

```powershell
.\gradlew.bat bootRun
```

Puis ouvrir **http://localhost:8080/login.html** dans un navigateur.

Les comptes créés depuis l'inscription ont le rôle `USER` ; les fonctions d'administration nécessitent un compte déjà configuré avec le rôle `ADMIN`.

### 5. Lancer les tests

```powershell
.\gradlew.bat test
```

Le code inclut un test de démarrage de contexte Spring et une configuration Testcontainers/MySQL. **La réussite de l'ensemble des tests n'a pas été vérifiée pour cette publication** ; Docker et des dépendances compatibles sont nécessaires pour les tests qui démarrent MySQL.

## 🔐 Sécurité et limites actuelles

- Les mots de passe utilisateur sont hachés avec **BCrypt**.
- Les opérations sur un contact passent par la vérification de son propriétaire dans le service métier.
- Les accès aux routes administrateur sont restreints au rôle `ADMIN`.
- **CSRF est désactivé** dans la configuration Spring Security fournie : ce paramètre doit être corrigé avant toute exposition publique d'une application utilisant des sessions navigateur.
- Le lien de réinitialisation de mot de passe est **affiché dans les logs du serveur** et **n'est pas envoyé par e-mail**.
- L'import CSV est géré dans le navigateur et repose sur des créations de contacts successives ; les cas d'erreurs et de fichiers volumineux méritent des tests complémentaires.
- La configuration MySQL locale doit utiliser des secrets non versionnés avant toute publication du code.

## 📌 Pistes d'amélioration

- Réactiver et intégrer correctement la protection CSRF.
- Ajouter des tests automatisés métier, API et sécurité.
- Envoyer les liens de réinitialisation par e-mail au moyen d'un service adapté.
- Mettre en place une gestion de configuration par environnement et une procédure d'initialisation du premier administrateur.
- Ajouter la pagination des contacts, une validation des imports CSV et des messages d'erreur plus explicites.

## 👤 Auteur

Projet personnel de **Youssef Mohamed El Cadhi**, présenté dans le cadre d'un portfolio de développement.

[Profil GitHub](https://github.com/Caze1127)
