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

## Лабораторная работа №3 (Benchmark)
Все команды выполнять из корня репозитория.

### Что нужно
- Docker / Docker Compose
- Python 3.12 (рекомендуется для скриптов построения графиков)

Установка зависимостей для PNG-графиков:
```bash
python3.12 -m pip install -r benchmark/scripts/requirements.txt
```

### Полный запуск ЛР3 (с графиками latency + ресурсов)
```bash
RUNS=100 \
ONLY_HEAVY=1 \
HEAVY_PROFILE_RPS=100,300,600,900,1200,2000 \
HEAVY_STAGE_DURATION=2m \
K6_CPUS=2 \
K6_MEMORY=4g \
bash benchmark/run_benchmark.sh
```

### Короткий отладочный запуск (1 прогон)
```bash
RUNS=1 \
ONLY_HEAVY=1 \
HEAVY_PROFILE_RPS=100,300,600,900,1200,2000 \
HEAVY_STAGE_DURATION=2m \
K6_CPUS=2 \
K6_MEMORY=4g \
bash benchmark/run_benchmark.sh 2>&1 | tee benchmark/results/last_run.log
```

### Перерисовать только latency-графики для готового прогона
```bash
python3.12 benchmark/scripts/analyze_k6_run.py \
  --k6-json benchmark/results/<STAMP>/run-001/k6-metrics.json \
  --out-dir benchmark/results/<STAMP>/run-001 \
  --scenario heavy_overload_recovery \
  --stage-duration 2m \
  --profile-rps 100,300,600,900,1200,2000
```

### Какие артефакты должны появиться в `benchmark/results/<STAMP>/run-001`
- `k6-summary.json`, `k6-metrics.json`
- `latency_over_time.png`, `latency_percentiles.png`, `latency_histogram.png`
- `latency_over_time_rps_100.png ... latency_over_time_rps_2000.png`
- `resources-summary.json`
- `resources_timeseries_app.csv`, `resources_timeseries_db.csv`
- `resources_app_over_time.png`, `resources_db_over_time.png`

## Лабораторная работа №7 (Интеграция с внешним сервисом + Mock)
В проект добавлен сценарий интеграции с внешним сервисом погоды Open-Meteo.

Контракт внешнего API:
- Open-Meteo Forecast API: `GET /v1/forecast`
- Документация: https://open-meteo.com/en/docs
- Используемые query-параметры: `latitude`, `longitude`, `current=temperature_2m,wind_speed_10m`

### Реализованный endpoint в приложении
- `GET /api/v1/locations/{id}/weather/current`
- Возвращает текущую температуру/скорость ветра для координат выбранной локации.

### Переключение mock/real через конфигурацию
Используются параметры:
- `external.weather.mode` (`mock` или `real`)
- `external.weather.mock-base-url`
- `external.weather.real-base-url`

Через переменные окружения:
- `EXTERNAL_WEATHER_MODE`
- `EXTERNAL_WEATHER_MOCK_BASE_URL`
- `EXTERNAL_WEATHER_REAL_BASE_URL`

### Mock-сервер
Mock реализован на WireMock:
- маппинги: `project-root/mock/weather/mappings/current-weather.json`
- в тестовом compose: сервис `weather-mock`

### E2E сценарии ЛР7
- `WeatherMockE2ECase` — проверка интеграции с mock-сервером
- `WeatherRealE2ECase` — проверка интеграции с реальным Open-Meteo

Локальный запуск:
```bash
cd project-root
./scripts/run_lab7_e2e.sh mock
./scripts/run_lab7_e2e.sh real
```

### CI pipeline
В `.gitlab-ci.yml` добавлены отдельные job:
- `e2e_mock_tests`
- `e2e_real_tests`

### Демонстрационное окружение
Из корня репозитория:
```bash
docker compose -f docker-compose.lab7.mock.yml up --build
docker compose -f docker-compose.lab7.real.yml up --build
```

