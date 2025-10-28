# Chimu

Backend приложение на Spring Boot для системы управления Game Jam.

## Технологии

- Kotlin 1.9.25 + Spring Boot 3.5.6
- PostgreSQL 16 + Flyway
- Spring Security + JWT
- MinIO для файлов
- Gradle 8.14.3 (Java 21)

## Быстрый старт

### 1. Запуск БД

```bash
docker-compose up -d
```

### 2. Настройка переменных окружения

Создайте `.env`:

```env
DB_URL=jdbc:postgresql://localhost:5432/chimu
DB_USER=postgres
DB_PASSWORD=277353

JWT_SECRET=<base64_secret>
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

Генерация секрета: `openssl rand -base64 64`

### 3. Запуск

```bash
./gradlew bootRun
```

Приложение: `http://localhost:8080`

## API

### Основные endpoints

- `POST /api/auth/register` - Регистрация
- `POST /api/auth` - Вход
- `POST /api/auth/refresh` - Обновление токена
- `POST /api/auth/logout` - Выход
- `GET /api/users/me` - Текущий пользователь

Подробная документация в [Wiki](https://github.com/setixxx/Chimu/wiki).

## Структура

```
src/main/kotlin/setixx/software/Chimu/
├── domain/         # Entity модели
├── repository/     # JPA репозитории
├── service/        # Бизнес-логика
├── security/       # JWT и Security
├── web/            # REST контроллеры
└── scheduler/      # Фоновые задачи
```

## Лицензия

GNU GPL v3.0
