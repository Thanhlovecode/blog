# 📝 Blog Platform — Spring Boot REST API

A production-grade **Blog REST API** built with **Spring Boot 3.5** and **Java 21**, featuring JWT authentication, Redis-backed high-performance view counting, image uploads via Cloudinary, and Google OAuth2 login.

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| **Runtime** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 3.5.4 |
| **Database** | MySQL 8.0 + HikariCP connection pool |
| **Caching** | Redis 8.4 (Lettuce + connection pooling) |
| **Migration** | Flyway |
| **Security** | Spring Security + JWT (RSA) + Google OAuth2 |
| **Rate Limiting** | Bucket4j (backed by Redis) |
| **Image Storage** | Cloudinary |
| **Email** | Spring Mail (Gmail SMTP) |
| **Build** | Maven Wrapper |
| **Containerization** | Docker Compose |

---

## 🏗️ Architecture

```
com.example.blog
├── controller/      # REST endpoints
├── service/         # Business logic interfaces
│   └── implement/   # Service implementations
├── repository/      # Spring Data JPA repositories
├── domain/          # JPA entities (User, Post, Comment, Tag, Follow, Profile...)
├── dto/             # Request/Response DTOs
├── mapper/          # Entity ↔ DTO mappers
├── config/          # Spring config (Security, Redis, Cloudinary, JWT, CORS...)
├── security/        # Custom security filters
├── scheduler/       # Scheduled tasks (view count batch sync)
├── annotation/      # Custom annotations (rate limit, caching...)
├── validator/       # Custom Bean Validation validators
├── exception/       # Custom exceptions
├── advice/          # Global exception handlers
├── event/           # Application events
├── enums/           # Enum types
├── constants/       # Constants
└── utils/           # Utility classes
```

---

## 🔑 Key Features

### Core
- **Posts** — CRUD with slug generation, tags, rich content
- **Comments** — Nested comment system on posts
- **Tags** — Tag management and post categorization
- **User Profiles** — Profile management with avatar upload
- **Follow System** — Follow/unfollow users

### Performance
- **Redis View Counter** — Async view counting with `INCR` + dirty-set tracking, batch-synced to MySQL every 30s via scheduled task
- **JSON Cache Aspect** — AOP-based Redis caching for read-heavy endpoints
- **Virtual Threads** — Java 21 virtual threads for high-throughput I/O
- **HikariCP** — Tuned connection pool with delayed acquisition
- **Response Compression** — Gzip for JSON/HTML/CSS/JS responses

### Security
- **JWT Authentication** — RSA key-pair (RS256) access tokens
- **Google OAuth2** — Login with Google via ID token verification
- **Rate Limiting** — Per-IP/user rate limiting with Bucket4j + Redis
- **Password Policy** — Passay-based password validation
- **OTP** — Time-limited OTP for password reset

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+**
- **Docker & Docker Compose**
- **Maven 3.9+** (or use included `mvnw`)

### 1. Clone the repository

```bash
git clone https://github.com/Thanhlovecode/blog.git
cd blog
```

### 2. Configure environment variables

Copy the example and fill in your credentials:

```bash
cp .env.example .env
```

Required variables in `.env`:

```properties
# MySQL
DBMS_DATABASE=jdbc:mysql://localhost:3306/blog_db
DBMS_USERNAME=root
DBMS_PASSWORD=

# MySQL Docker
MYSQL_ROOT_PASSWORD=<your_password>
MYSQL_DATABASE=blog_db
MYSQL_USER=bloguser
MYSQL_PASSWORD=<your_password>

# Redis
REDIS_PASSWORD=<your_password>

# Cloudinary (Image Uploads)
CLOUD_NAME=<your_cloud_name>
API_KEY=<your_api_key>
API_SECRET=<your_api_secret>

# Google OAuth2
GOOGLE_CLIENT_ID=<your_google_client_id>

# Email (optional)
EMAIL=<your_email>
EMAIL_PASSWORD=<your_app_password>
```

### 3. Start infrastructure (MySQL + Redis)

```bash
docker compose -f environment/docker-compose-dev.yml --env-file .env up -d
```

### 4. Generate RSA key pair (first time only)

```bash
mkdir -p src/main/resources/certs
openssl genrsa -out src/main/resources/certs/private.pem 2048
openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem
```

### 5. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API is available at **`http://localhost:8080/api/v1`**.

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/login` | Login (JWT) |
| `POST` | `/api/v1/auth/google` | Login with Google |
| `POST` | `/api/v1/auth/register` | Register |
| `GET` | `/api/v1/posts` | List posts |
| `GET` | `/api/v1/posts/{slug}` | Get post by slug |
| `POST` | `/api/v1/posts` | Create post |
| `PUT` | `/api/v1/posts/{id}` | Update post |
| `DELETE` | `/api/v1/posts/{id}` | Delete post |
| `GET/POST` | `/api/v1/comments/**` | Comment operations |
| `GET/POST` | `/api/v1/tags/**` | Tag operations |
| `GET/PUT` | `/api/v1/profiles/**` | Profile operations |
| `POST/DELETE` | `/api/v1/follows/**` | Follow/unfollow |
| `POST` | `/api/v1/password/**` | Password reset (OTP) |

---

## 📂 Project Structure

```
blog/
├── environment/
│   └── docker-compose-dev.yml   # MySQL + Redis for development
├── src/main/
│   ├── java/com/example/blog/   # Application source code
│   └── resources/
│       ├── application-dev.yml  # Dev profile config
│       ├── application-pro.yml  # Production profile config
│       ├── certs/               # RSA keys (gitignored)
│       └── db/migration/        # Flyway SQL migrations
├── .env                         # Environment variables (gitignored)
├── pom.xml                      # Maven dependencies
└── mvnw / mvnw.cmd              # Maven Wrapper
```

---

## 📜 License

This project is for educational and portfolio purposes.
