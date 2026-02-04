# 🧪 Лабораторная работа №1

---


##  Команды для запуска тестов

---

###  1. Запуск тестов
```bash
mvn clean test
```

---

###  2. Генерация отчёта
```bash
mvn allure:serve
```


---

## Конфигурация плагина `maven-surefire-plugin`


```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>

            <configuration>
                <!-- random - рандомный порядок
                     alphabetical - по алфавиту
                     reversealphabetical - обратном порядке
                     filesystem - по порядку файлов -->
                <runOrder>random</runOrder>

                <!-- Количество процессов -->
                <forkCount>1</forkCount>
                <reuseForks>true</reuseForks>

                <!-- none - последовательно
                     methods - тесты внутри класса параллельно
                     classes - тесты разных классов параллельно
                     both - параллельно и классы, и методы -->
                <parallel>classes</parallel>

                <!-- Количество потоков  -->
                <threadCount>4</threadCount>

            </configuration>
        </plugin>
    </plugins>
</build>
```



---

## Лабораторная работа №2

### Запуск тестов в Docker

1. Запуск unit тестов
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipITs=true -Dallure.results.directory=target/allure-results/unit test"
docker compose -f docker-compose.tests.yml down -v
```

2. Запуск integration тестов
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=true -DskipITs=false -Dit.test=**/*ITCase -Dallure.results.directory=target/allure-results/integration verify"
docker compose -f docker-compose.tests.yml down -v
```

3. Запуск E2E тестов
```bash
docker compose -f docker-compose.tests.yml up -d db
docker compose -f docker-compose.tests.yml run --rm tests "./scripts/wait-for-db.sh db 5432 60 && ./mvnw -B -DskipTests=true -DskipITs=false -Dit.test=**/*E2ECase -Dallure.results.directory=target/allure-results/e2e verify"
docker compose -f docker-compose.tests.yml down -v
```

### Параметры окружения для изоляции БД

1. TEST_DB_NAME, TEST_DB_USER, TEST_DB_PASS
2. Для одновременного запуска несколькими разработчиками используйте разные TEST_DB_NAME

### E2E имитация запросов и захват трафика

1. Отправка запросов
```bash
./project-root/scripts/e2e_requests.sh
```

2. Захват трафика (Linux)
```bash
./project-root/scripts/capture_traffic.sh traffic.pcap
```

3. Windows вариант отправки запросов
```powershell
./project-root/scripts/e2e_requests.ps1
```

### CI/CD

1. Pipeline для GitLab находится в `.gitlab-ci.yml`
2. Порядок: unit → integration → e2e → report
3. Отчет Allure формируется на этапе report

### Примечания по требованиям ЛР2

1. В проекте нет service bus / message broker
2. Хранение активных сессий пользователей не используется, аутентификация stateless (JWT)
3. Для параллельного локального запуска используйте разные значения TEST_DB_NAME и/или разные compose-проекты.
