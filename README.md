# Проект автоматизации тестирования API на платформе [REQRES.IN](https://reqres.in/)
<img src="picsandmedia/apilogo.JPG">


## Содержание:

- [Технологии и инструменты](#Технологии-и-инструменты)
- [Выполняемые проверки](#Выполняемые-проверки)
- [Запуск тестов в Jenkins](#Запуск-тестов-в-Jenkins)
- [Allure отчёт](#Allure-отчёт)
- [Видео с примером запуска тестов в Selenoid](#video)
 

## <a name="Технологии и инструменты">Технологии и инструменты</a>
<p  align="center">
  <code><img width="8%" title="IntelliJ IDEA" src="picsandmedia/IDEA-logo.svg"></code>
  <code><img width="8%" title="Java" src="picsandmedia/java-logo.svg"></code>
  <code><img width="8%" title="Rest-Assured" src="picsandmedia/rest-assured-logo.svg"></code>
  <code><img width="8%" title="Gradle" src="picsandmedia/gradle-logo.svg"></code>
  <code><img width="8%" title="JUnit5" src="picsandmedia/junit5-logo.svg"></code>
  <code><img width="8%" title="Allure Report" src="picsandmedia/allure-Report-logo.svg"></code>
  <code><img width="8%" title="Jenkins" src="picsandmedia/jenkins-logo.svg"></code>
 </p>
 
Для API-тестирования используется `Rest-Assured` с применением подхода Lombok, что позволяет писать чистые и выразительные спецификации с минимальным количеством кода. 
Модели данных и спецификации запросов/ответов вынесены в отдельные слои для повышения читабельности и поддерживаемости тестов.

Общая инфраструктура: `JUnit 5` для запуска, `Gradle` для сборки, `Jenkins` для CI, `Allure Report` для визуализации результатов.


## <a name="Выполняемые проверки">Выполняемые проверки</a> 

API-тесты покрывают следующие сценарии работы с ресурсом `/users` и `/register`:

**1. Работа со списком пользователей (GET /users)**
-   [x] Получение и проверка полного списка пользователей (соответствие ID и имен)
-   [x] Проверка структуры ответа (наличие полей `id`, `email` у каждого пользователя)
-   [x] Проверка корректности работы пагинации (параметр `page`)
-   [x] Обработка запроса с задержкой ответа (параметр `delay`)

**2. Создание пользователя (POST /users)**
-   [x] Успешное создание пользователя с указанием полей `name`, `job`
-   [x] Проверка, что ответ содержит переданные данные и системные поля (`id`, `createdAt`)

**3. Обновление данных пользователя (PUT /users/{id}, PATCH /users/{id})**
-   [x] Полное обновление данных пользователя через `PUT` (проверка всех полей в ответе)
-   [x] Частичное обновление данных пользователя через `PATCH` (проверка обновления только указанного поля)
-   [x] Проверка наличия метки времени обновления (`updatedAt`) в ответе

**4. Удаление пользователя (DELETE /users/{id})**
-   [x] Корректное удаление пользователя (проверка статус-кода 204)

**5. Регистрация пользователя (POST /register)**
-   [x] Успешная регистрация с валидными данными (проверка получения `id` и `token`)
-   [x] Обработка ошибки регистрации с невалидными данными (проверка статус-кода и сообщения об ошибке)

**6. Обработка ошибок**
-   [x] Запрос к несуществующему ресурсу возвращает статус-код 404


## <a name="Запуск тестов в [Jenkins](https://jenkins.autotests.cloud/job/SiriusProject/)">Запуск тестов в Jenkins</a>
Локально каждая группа тестов запускается командой ```gradle clean <Tag>``` , где ```<Tag>``` - это:
- web_test
- cart_test
- catalog_test
- filter_test
- menu_test
- search_test

Для подобного гибкого запуска тестов в `Jenkins` была реализована параметризованная сборка с возможностью выбора той или иной группы тестов через Choice Parameter = TASK , а также возможность выбора других параметров:
```
clean
${TASK}
-Dselenoid=${SELENOID}
-DbrowserName=${BROWSER_NAME}
-DwindowSize=${WINDOW_SIZE}
-DbrowserVersion=${VERSION_OF_BROWSER}
-Dlogin=${LOGIN}
-Dpassword=${PASSWORD}
```
<img src="images/paramscrin.JPG" alt="JenkinsBuildParameters" width="950">
<img src="images/scrin1.JPG" alt="Jenkinsmain" width="950">

## <a name="Allure отчёт">Allure отчёт</a> 

После прогона тестов через страницу запусков в `Jenkins` есть возможность просмотреть результаты выполнения тестов в ` Report`
На странице отчёта отображено общее количество запущенных тестов и процентное соотношение успешных и упавших тестов, подкреплённых диаграммой.
<img src="images/scrin2.JPG" alt="Jenkinsallure1" width="950">

Также на вкладке "Test suites" можно открыть каждый тест и посмотреть детально, на каком этапе возникла ошибка. Помимо этого по результатам теста имеется скриншот и видео из `Selenoid`.
<img src="images/scrin3.JPG" alt="Jenkinsallure2" width="950">
