# рџ§Є Р›Р°Р±РѕСЂР°С‚РѕСЂРЅР°СЏ СЂР°Р±РѕС‚Р° в„–1

---


##  РљРѕРјР°РЅРґС‹ РґР»СЏ Р·Р°РїСѓСЃРєР° С‚РµСЃС‚РѕРІ

---

###  1. Р—Р°РїСѓСЃРє С‚РµСЃС‚РѕРІ
```bash
mvn clean test
```

---

###  2. Р“РµРЅРµСЂР°С†РёСЏ РѕС‚С‡С‘С‚Р°
```bash
mvn allure:serve
```


---

## РљРѕРЅС„РёРіСѓСЂР°С†РёСЏ РїР»Р°РіРёРЅР° `maven-surefire-plugin`


```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>

            <configuration>
                <!-- random - СЂР°РЅРґРѕРјРЅС‹Р№ РїРѕСЂСЏРґРѕРє
                     alphabetical - РїРѕ Р°Р»С„Р°РІРёС‚Сѓ
                     reversealphabetical - РѕР±СЂР°С‚РЅРѕРј РїРѕСЂСЏРґРєРµ
                     filesystem - РїРѕ РїРѕСЂСЏРґРєСѓ С„Р°Р№Р»РѕРІ -->
                <runOrder>random</runOrder>

                <!-- РљРѕР»РёС‡РµСЃС‚РІРѕ РїСЂРѕС†РµСЃСЃРѕРІ -->
                <forkCount>1</forkCount>
                <reuseForks>true</reuseForks>

                <!-- none - РїРѕСЃР»РµРґРѕРІР°С‚РµР»СЊРЅРѕ
                     methods - С‚РµСЃС‚С‹ РІРЅСѓС‚СЂРё РєР»Р°СЃСЃР° РїР°СЂР°Р»Р»РµР»СЊРЅРѕ
                     classes - С‚РµСЃС‚С‹ СЂР°Р·РЅС‹С… РєР»Р°СЃСЃРѕРІ РїР°СЂР°Р»Р»РµР»СЊРЅРѕ
                     both - РїР°СЂР°Р»Р»РµР»СЊРЅРѕ Рё РєР»Р°СЃСЃС‹, Рё РјРµС‚РѕРґС‹ -->
                <parallel>classes</parallel>

                <!-- РљРѕР»РёС‡РµСЃС‚РІРѕ РїРѕС‚РѕРєРѕРІ  -->
                <threadCount>4</threadCount>

            </configuration>
        </plugin>
    </plugins>
</build>
```



---

## Р›Р°Р±РѕСЂР°С‚РѕСЂРЅР°СЏ СЂР°Р±РѕС‚Р° в„–2

### Р—Р°РїСѓСЃРє С‚РµСЃС‚РѕРІ РІ Docker

1. Р—Р°РїСѓСЃРє unit С‚РµСЃС‚РѕРІ
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipITs=true -Dallure.results.directory=allure-results/unit test"
docker compose -f docker-compose.tests.yml down -v
```

2. Р—Р°РїСѓСЃРє integration С‚РµСЃС‚РѕРІ
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=true -DskipITs=false -Dit.test=**/*ITCase -Dallure.results.directory=allure-results/integration verify"
docker compose -f docker-compose.tests.yml down -v
```

3. Р—Р°РїСѓСЃРє E2E С‚РµСЃС‚РѕРІ
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=true -DskipITs=false -Dit.test=**/*E2ECase -Dallure.results.directory=allure-results/e2e verify"
docker compose -f docker-compose.tests.yml down -v
```

### РџР°СЂР°РјРµС‚СЂС‹ РѕРєСЂСѓР¶РµРЅРёСЏ РґР»СЏ РёР·РѕР»СЏС†РёРё Р‘Р”

1. TEST_DB_NAME, TEST_DB_USER, TEST_DB_PASS
2. Р”Р»СЏ РѕРґРЅРѕРІСЂРµРјРµРЅРЅРѕРіРѕ Р·Р°РїСѓСЃРєР° РЅРµСЃРєРѕР»СЊРєРёРјРё СЂР°Р·СЂР°Р±РѕС‚С‡РёРєР°РјРё РёСЃРїРѕР»СЊР·СѓР№С‚Рµ СЂР°Р·РЅС‹Рµ TEST_DB_NAME

### E2E РёРјРёС‚Р°С†РёСЏ Р·Р°РїСЂРѕСЃРѕРІ Рё Р·Р°С…РІР°С‚ С‚СЂР°С„РёРєР°

1. РћС‚РїСЂР°РІРєР° Р·Р°РїСЂРѕСЃРѕРІ
```bash
./project-root/scripts/e2e_requests.sh
```

2. Р—Р°С…РІР°С‚ С‚СЂР°С„РёРєР° (Linux)
```bash
./project-root/scripts/capture_traffic.sh traffic.pcap
```

3. Windows РІР°СЂРёР°РЅС‚ РѕС‚РїСЂР°РІРєРё Р·Р°РїСЂРѕСЃРѕРІ
```powershell
./project-root/scripts/e2e_requests.ps1
```

### CI/CD

1. Pipeline РґР»СЏ GitLab РЅР°С…РѕРґРёС‚СЃСЏ РІ `.gitlab-ci.yml`
2. РџРѕСЂСЏРґРѕРє: unit в†’ integration в†’ e2e в†’ report
3. РћС‚С‡РµС‚ Allure С„РѕСЂРјРёСЂСѓРµС‚СЃСЏ РЅР° СЌС‚Р°РїРµ report

### РџСЂРёРјРµС‡Р°РЅРёСЏ РїРѕ С‚СЂРµР±РѕРІР°РЅРёСЏРј Р›Р 2

1. Р’ РїСЂРѕРµРєС‚Рµ РЅРµС‚ service bus / message broker
2. РҐСЂР°РЅРµРЅРёРµ Р°РєС‚РёРІРЅС‹С… СЃРµСЃСЃРёР№ РїРѕР»СЊР·РѕРІР°С‚РµР»РµР№ РЅРµ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ, Р°СѓС‚РµРЅС‚РёС„РёРєР°С†РёСЏ stateless (JWT)
3. Р”Р»СЏ РїР°СЂР°Р»Р»РµР»СЊРЅРѕРіРѕ Р»РѕРєР°Р»СЊРЅРѕРіРѕ Р·Р°РїСѓСЃРєР° РёСЃРїРѕР»СЊР·СѓР№С‚Рµ СЂР°Р·РЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ TEST_DB_NAME Рё/РёР»Рё СЂР°Р·РЅС‹Рµ compose-РїСЂРѕРµРєС‚С‹.


### Параллельный запуск в ОДНОЙ БД (одна схема, одни таблицы)

1. Поднять одну общую БД один раз:
```bash
COMPOSE_PROJECT_NAME=shared docker compose -f docker-compose.tests.yml up -d db
```

2. В двух терминалах параллельно запускать тесты с уникальным `RUN_ID`:
```bash
# терминал 1
RUN_ID=run1 SHARED_DB=1 COMPOSE_PROJECT_NAME=shared ./project-root/scripts/run_local_ci.sh -u -i -e

# терминал 2
RUN_ID=run2 SHARED_DB=1 COMPOSE_PROJECT_NAME=shared ./project-root/scripts/run_local_ci.sh -u -i -e
```

3. После завершения обоих прогонов можно остановить БД:
```bash
COMPOSE_PROJECT_NAME=shared docker compose -f docker-compose.tests.yml down -v
```

Примечание: для параллельности используются уникальные идентификаторы сущностей (см. TestIds),
поэтому данные не конфликтуют при работе в одной БД/схеме/таблицах.
