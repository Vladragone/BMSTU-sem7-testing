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


