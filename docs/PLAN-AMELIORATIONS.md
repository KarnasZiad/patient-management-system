# Plan d'amélioration – Patient Management Microservices

Chaque étape est indépendante, testable, et fait l'objet d'un commit séparé.
Légende : ✅ fait · ⏳ en cours · ⬜ à faire

---

## Phase 0 – Pouvoir lancer l'application en local (prérequis)

| # | Étape | Problème | Solution | Statut |
|---|---|---|---|---|
| 0.1 | `docker-compose.yml` | Aucun moyen simple de tout lancer | Compose avec 2× PostgreSQL, Kafka (KRaft, sans Zookeeper), les 5 services, healthchecks et `depends_on` | ✅ |
| 0.2 | `.gitignore` | `docker-compose.yml` est ignoré | Retirer la ligne | ✅ |
| 0.3 | Dockerfiles | Image `openjdk:21-jdk` obsolète (dépréciée sur Docker Hub), JDK complet inutile en runtime | Passer à `eclipse-temurin:21-jre` | ✅ |
| 0.4 | analytics-service | Pas de `server.port` → 8080, alors que l'infra attend 4002 | `server.port=4002` | ✅ |
| 0.5 | Fichier `.env.example` | Secrets (JWT, mots de passe DB) en dur | Variables lues depuis `.env` | ✅ |

**Validation :** `docker compose up --build` → les tests d'intégration passent (`mvn test` dans `integration-tests`).

---

## Phase 1 – Corrections de bugs

| # | Étape | Problème | Solution | Statut |
|---|---|---|---|---|
| 1.1 | Validation login | `@Valid` manquant dans `AuthController` → contraintes du DTO ignorées | Ajouter `@Valid` + handler d'erreurs de validation (400 lisible) | ✅ |
| 1.2 | Gateway prod | `http:/host.docker.internal:4005` (un `/` manquant) | Corriger l'URL | ✅ |
| 1.3 | Message d'erreur | `"already exists" + email` collés | Message propre | ✅ |
| 1.4 | 404 | `PatientNotFoundException` → 400 ; `DELETE` sur un id inexistant → 204 silencieux | Renvoyer 404 dans les deux cas | ✅ |
| 1.5 | Cohérence création | Si billing (gRPC) échoue, le patient reste en base sans compte | `@Transactional` : rollback si billing échoue ; erreur 503 claire | ✅ |
| 1.6 | Dates invalides | `LocalDate.parse("abc")` → 500 | Handler `DateTimeParseException` → 400 | ✅ |

**Validation :** tests d'intégration ajoutés pour chaque cas (400/404/503).

---

## Phase 1bis – Qualité du code & suppression du hardcoding

| # | Étape | Problème | Solution | Statut |
|---|---|---|---|---|
| Q.1 | Config gateway | URLs des services dupliquées dans `application.yml` / `application-prod.yml` | Un seul fichier, URLs via variables d'env avec valeurs par défaut | ✅ |
| Q.2 | Constantes métier | Durée JWT, topic Kafka, groupId, type d'événement, adresse billing en dur | Propriétés `application.properties` (`jwt.expiration`, `kafka.topic.patient`…) | ✅ |
| Q.3 | Stub billing | Renvoie toujours `"12345"` | `accountId` généré (UUID) | ✅ |
| Q.4 | Tests & `.http` | URL et identifiants recopiés partout | URL via `-Dapi.base-url`, classe de base commune ; environnements `http-client.env.json` | ✅ |
| Q.5 | Infra CDK | Secret JWT, ports, chaîne Kafka en dur | Secret généré par Secrets Manager, constantes | ✅ |
| Q.6 | Données de santé dans les logs | Noms / emails loggés en clair | Ne logger que les identifiants | ✅ |
| Q.7 | Kafka producer | Échec d'envoi non détecté (envoi asynchrone), pas de clé | Callback `whenComplete`, clé = patientId | ✅ |
| Q.8 | Client gRPC | Canal jamais fermé, pas de timeout | `@PreDestroy` + deadline configurable | ✅ |
| Q.9 | DTO | Verbeux, dates en `String`, `registeredDate` absent de la réponse | `record` Java, `LocalDate` | ⬜ |
| Q.10 | Format d'erreur | `Map` ad hoc, différent selon les services | `ProblemDetail` (RFC 9457) partout | ⬜ |
| Q.11 | Versions Maven | Spring Boot 3.4.0 vs 3.4.1, versions recopiées dans chaque pom | Pom parent commun (springdoc déjà aligné en 2.7.0 : la 2.6.0 cassait /api-docs/auth) (aligne aussi l'étape 4.2) | ⬜ |

---

## Phase 2 – Sécurité

| # | Étape | Problème | Solution | Statut |
|---|---|---|---|---|
| 2.1 | Secret JWT | En dur dans la stack CDK | Lu depuis variable d'env / Secrets Manager (CDK) | ⬜ |
| 2.2 | Validation JWT dans le gateway | Un appel HTTP vers auth-service à chaque requête | Le gateway valide la signature lui-même (même clé) et transmet `X-User-Email` / `X-User-Role` aux services | ⬜ |
| 2.3 | Autorisation par rôle | Le rôle n'est jamais vérifié | `DELETE` réservé à `ADMIN` (403 sinon) | ⬜ |
| 2.4 | Refresh token | Token valable 10 h, non révocable | Access token court (15 min) + refresh token stocké en base, endpoints `/refresh` et `/logout` | ⬜ |

---

## Phase 3 – Fonctionnalités

| # | Étape | Solution | Statut |
|---|---|---|---|
| 3.1 | Inscription | `POST /auth/register` (email unique, mot de passe hashé BCrypt, rôle `USER` par défaut) | ⬜ |
| 3.2 | `GET /patients/{id}` | Endpoint de lecture unitaire | ⬜ |
| 3.3 | Pagination & recherche | `GET /patients?page=0&size=20&name=...` | ⬜ |
| 3.4 | Événements Kafka complets | `PATIENT_UPDATED`, `PATIENT_DELETED` en plus de `PATIENT_CREATED` | ⬜ |
| 3.5 | Billing réel | Base PostgreSQL, entité `BillingAccount`, idempotence (un compte par patient) | ⬜ |
| 3.6 | Analytics réel | Stockage des événements + endpoint `GET /analytics/stats` (nb patients créés/supprimés…) | ⬜ |

---

## Phase 4 – Qualité & robustesse

| # | Étape | Solution | Statut |
|---|---|---|---|
| 4.1 | Tests unitaires | JUnit 5 + Mockito sur les services (`PatientService`, `AuthService`, `JwtUtil`) | ⬜ |
| 4.2 | Proto partagés | Module Maven `proto-contracts` utilisé par patient, billing et analytics (fin des copies) | ⬜ |
| 4.3 | Résilience | Timeout gRPC (deadline) + retry / circuit breaker (Resilience4j) | ⬜ |
| 4.4 | Health checks | Spring Boot Actuator (`/actuator/health`) + utilisation dans docker-compose | ⬜ |
| 4.5 | CI | GitHub Actions : build + tests de chaque service à chaque push | ⬜ |
| 4.6 | Observabilité | Logs corrélés (traceId) via Micrometer Tracing ; option Zipkin | ⬜ |

---

## Phase 5 – Optionnel

| # | Étape | Statut |
|---|---|---|
| 5.1 | Frontend (React) : page login + liste/CRUD patients | ⬜ |
| 5.2 | Infra CDK déployable sur le vrai AWS (images ECR, pas de MSK coûteux, sans NAT) | ⬜ |
