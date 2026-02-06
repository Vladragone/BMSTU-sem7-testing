# Лабораторная работа №2 — Тестирование (Unit / Integration / E2E)

## О проекте
Проект — многокомпонентное Spring Boot приложение. Тесты организованы в три уровня:
- Unit (мок‑тесты бизнес‑логики)
- Integration (работа сервисов/репозиториев с БД)
- E2E (демонстрационный сценарий через HTTP API)

## Структура тестов
- Unit: `src/test/java/**/**Test.java`
- Integration: `src/test/java/**/**ITCase.java`
- E2E: `src/test/java/**/**E2ECase.java`

## Требования к окружению
- Docker / Docker Compose
- Java 17 (в контейнере используется Temurin 17)

## Быстрый запуск локально
Все команды выполнять из корня репозитория.

### 1) Поднять одну общую БД (для параллельных прогонов)
```bash
COMPOSE_PROJECT_NAME=shared docker compose -f docker-compose.tests.yml up -d db
```

### 2) Запуск тестов через скрипт
Скрипт `project-root/scripts/run_local_ci.sh` поддерживает флаги:
- `-u` — unit
- `-i` — integration
- `-e` — e2e
- `-r` — сформировать Allure‑отчёт и поднять локальный сервер

Примеры:
```bash
# Все этапы подряд
./project-root/scripts/run_local_ci.sh -u -i -e

# Только unit
./project-root/scripts/run_local_ci.sh -u

# Полный прогон + отчёт
./project-root/scripts/run_local_ci.sh -u -i -e -r
```

### 3) Параллельный запуск в одной БД (одна схема, одни таблицы)
Используется уникальность идентификаторов сущностей (см. `TestIds`).

В двух терминалах:
```bash
RUN_ID=run1 SHARED_DB=1 COMPOSE_PROJECT_NAME=shared ./project-root/scripts/run_local_ci.sh -u -i -e
RUN_ID=run2 SHARED_DB=1 COMPOSE_PROJECT_NAME=shared ./project-root/scripts/run_local_ci.sh -u -i -e
```

### 4) Остановить общую БД
```bash
COMPOSE_PROJECT_NAME=shared docker compose -f docker-compose.tests.yml down -v
```

## Allure отчёт
Результаты складываются в `project-root/allure-results` и подкаталоги `unit/integration/e2e`.
Отчёт генерируется скриптом:
```bash
cd project-root
./scripts/allure_report.sh
```

Просмотр отчёта:
```bash
cd project-root/target/site/allure-maven-plugin
python -m http.server 8000
```
Открыть в браузере: `http://localhost:8000`

## CI/CD (GitHub Actions)
Pipeline находится в `.github/workflows/ci.yml` и запускает этапы:
`unit → integration → e2e → report`.

## Особенности реализации
- Интеграционные/E2E тесты работают с PostgreSQL.
- Данные изолируются за счёт уникальных идентификаторов (`TestIds`).
- При параллельных запусках используется одна БД и одна схема, конфликтов нет.
- Allure отчёты собираются через локальный Allure CLI (`.allure`).

## Полезные файлы
- `docker-compose.tests.yml` — окружение для тестов
- `project-root/scripts/run_local_ci.sh` — локальный запуск
- `project-root/scripts/allure_report.sh` — генерация отчёта
- `project-root/src/test/resources/sql/schema.sql` — схема БД
- `project-root/src/test/resources/sql/seed.sql` — начальные данные

