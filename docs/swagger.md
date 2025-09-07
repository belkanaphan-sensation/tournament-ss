## 🔑 Авторизация в Swagger UI

1. **Логин через форму**

    * Перейдите в браузере по адресу:

      ```
      http://<host>:<port>/login
      ```
    * Введите логин и пароль в [login](http://localhost:8080/login)
    * После успешной аутентификации Spring выдаст cookie `JSESSIONID`.

2. **Получение куки**

    * Откройте DevTools в браузере (`F12` → вкладка *Application* → *Cookies*).
    * Найдите значение `JSESSIONID`.

3. **Авторизация в Swagger**

    * Перейдите в Swagger UI: [swagger](http://localhost:8080/swagger-ui/index.html)

      ```
      http://<host>:<port>/swagger-ui/index.html
      ```
    * Нажмите кнопку **Authorize**.
    * Вставьте скопированное значение `JSESSIONID` в поле авторизации.
    * Теперь все защищённые эндпоинты будут доступны.

4. **Ошибки в логах**

    * В логах могут появляться сообщения вида:

      ```
      No static resource .well-known/...
      ```
    * Эти ошибки связаны с Chrome DevTools и не влияют на работу приложения. Их можно игнорировать.
