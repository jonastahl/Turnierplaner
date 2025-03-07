# Tournament planer

# Tournament Planner

This project is a comprehensive tournament planning application that leverages modern web technologies like Vue3 (in the
frontend),
Quarkus (for the backend), Keycloak (for authentication) to provide a seamless user experience.
The application also supports mail sending for verification and reminder mails.

## Getting Started

First you need to create a `.env` file under the root directory with the following content:

```
MAIL_FROM=${ your mail address }
MAIL_HOST=${ your mail host }
MAIL_USER=${ your mail address }
MAIL_PASS=${ your mail password }
```

This is necessary for the mail sending feature.

## Starting the application in development mode

The frontend and backend are structured as a mono-repo, so you can easily start the applications in development mode
with the following command:

```shell
mvn compile quarkus:dev 
```

### Build Quarkus+Vue
If you just want to build the whole application you can run:
```shell
mvn package
```
Don't compile frontend

```shell
mvn package -DnoFrontendComp
```
Don't compile backend
```shell
mvn package -DnoBackendComp
```

### Clean
```shell
mvn clean
```

## Starting the application in production mode

To start the application in production mode, you can use the docker compose file, which will start the application in a
production-ready environment:

```shell
docker compose up
```

This will start a containerized version of the application, which will be accessible at `http://localhost:8080`.
Before using the application, you need to import the realm settings for Keycloak.

### Keycloak

Keycloak is a open source identity and access management solution. It is used in this application to manage user
authentication and authorization.

First you can optionally overwrite the default new keycloak admin user and keycloak database user by creating a `.env`
file under the root folder with the following content:

```env
KEYCLOAK_ADMIN_USER=${ your keycloak admin user }
KEYCLOAK_ADMIN_PASS=${ your keycloak admin password }

KC_DB_USERNAME=${ the username for the keycloak database }
KC_DB_PASSWORD=${ the password for the keycloak database }
```

#### Importing the realm settings

You then have to login into the admin console with your admin account and password. If you didn't set a account and
password
the default is `admin` `admin`. Then you need to import the Quarkus realm under:

- Realm settings in the menu.
- Point to the Action menu in the top right corner of the realm settings screen, and select Partial import.
- A prompt then appears where you can select the file you want to import. Please select the file `keycloak_realm.json`
  located under the folder `src/main/resources/`
- Click Import.