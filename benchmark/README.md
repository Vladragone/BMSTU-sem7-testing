# ЛР №3: Benchmark производительности

Набор benchmark оценивает производительность backend-приложения (`project-root`) по:
- задержкам HTTP (время + перцентили + гистограмма),
- устойчивости под ростом нагрузки и восстановлению после перегруза,
- утилизации ресурсов контейнеров `app` и `db` (CPU/RAM/диск).

## Что запускается
- SUT в изолированном docker-compose на каждый прогон:
  - `app` (Spring Boot),
  - `db` (PostgreSQL),
  - `cadvisor` (метрики контейнеров),
  - `prometheus` (сбор метрик cAdvisor).
- генератор нагрузки `k6` в отдельном контейнере с фиксированными лимитами CPU/RAM.

## Сценарии нагрузки
Файл: `benchmark/k6/lab3_scenarios.js`

1. `login_burst`:
- массовый одновременный login/registration.

2. `rating_stable_load`:
- стабильная нагрузка на чтение/сериализацию (`GET /api/v1/ratings?limit=100`).

3. `heavy_overload_recovery`:
- рост до перегруза на write-flow (`POST /gamesessions` + `POST /gamerounds`),
- затем возврат к стабильному уровню и этап восстановления.

## Запуск
Команды из корня репозитория:

```bash
chmod +x benchmark/run_benchmark.sh
./benchmark/run_benchmark.sh
```

По умолчанию выполняется 100 прогонов (`RUNS=100`).

Пример короткой отладки:
```bash
RUNS=3 REBUILD_IMAGE_EACH_RUN=0 SEED_USERS=50 ./benchmark/run_benchmark.sh
```

## Настраиваемые параметры
Через env:
- `RUNS` (default: `100`)
- `SEED_USERS` (default: `200`)
- `LOGIN_VUS` (default: `80`)
- `STABLE_RATE` (default: `60`)
- `OVERLOAD_RATE` (default: `140`)
- `REBUILD_IMAGE_EACH_RUN` (default: `1`)
- `K6_CPUS` (default: `1.0`)
- `K6_MEMORY` (default: `512m`)

## Артефакты результатов
После кампании:
- `benchmark/results/<timestamp>/campaign_config.json`
- `benchmark/results/<timestamp>/run-XXX/k6-summary.json`
- `benchmark/results/<timestamp>/run-XXX/k6-metrics.json`
- `benchmark/results/<timestamp>/run-XXX/resources-summary.json`
- `benchmark/results/<timestamp>/run-XXX/latency_timeseries.csv`
- `benchmark/results/<timestamp>/run-XXX/latency_percentiles.csv`
- `benchmark/results/<timestamp>/run-XXX/latency_histogram.csv`
- `benchmark/results/<timestamp>/run-XXX/*.png` (если установлен `matplotlib`)
- `benchmark/results/<timestamp>/runs_summary.csv`
- `benchmark/results/<timestamp>/final_summary.json`

## Графики
`benchmark/scripts/analyze_k6_run.py` строит:
- график latency во времени,
- распределение latency по перцентилям,
- histogram latency.

Для PNG-графиков:
```bash
pip install -r benchmark/scripts/requirements.txt
```

## Соответствие требованиям ЛР
1. Статистика на 100+ испытаний: `RUNS=100`, агрегация в `final_summary.json`.
2. Несколько сценариев: деградация, max-нагрузка, восстановление.
3. Равные условия: каждый прогон в новом compose-проекте с одинаковой конфигурацией.
4. Метрики времени: time-series, percentiles (0.5/0.75/0.9/0.95/0.99), histogram.
5. Проверка нескольких параметров: login, read/serialization, heavy write.
6. Ресурсы: Prometheus + cAdvisor, min/max/median по CPU/RAM/disk в JSON.
7. Использован готовый генератор нагрузки: `k6`.
10. Фиксация ресурсов: лимиты контейнеров + конфиг кампании в JSON.
11. Полный цикл a-f автоматизирован в `benchmark/run_benchmark.sh`.
