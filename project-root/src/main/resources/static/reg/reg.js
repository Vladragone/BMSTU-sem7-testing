"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
document.addEventListener('DOMContentLoaded', () => {
    const backButton = document.getElementById('backBtn');
    // Кнопка "Назад"
    if (backButton) {
        backButton.addEventListener('click', () => {
            window.location.href = '../start/start.html';
        });
    }
    const registerForm = document.getElementById('registerForm');
    const registerButton = document.getElementById('registerButton');
    const errorMessages = {
        username: document.getElementById('usernameError'),
        email: document.getElementById('emailError'),
        password: document.getElementById('passwordError'),
    };
    // Функция для проверки латиницы
    function isLatin(str) {
        return /^[A-Za-z0-9]+$/.test(str);
    }
    // Функция для сброса сообщений об ошибках
    function resetErrorMessages() {
        Object.values(errorMessages).forEach(el => el.style.display = 'none');
    }
    // Функция для валидации формы
    function validateForm() {
        let isValid = true;
        const inputs = registerForm.querySelectorAll('input');
        inputs.forEach(input => {
            if (!input.validity.valid) {
                const errorMessage = errorMessages[input.name];
                errorMessage.textContent = input.validationMessage;
                errorMessage.style.display = 'block';
                isValid = false;
            }
        });
        return isValid;
    }
    // Обработчик отправки формы
    registerForm.addEventListener('submit', (event) => __awaiter(void 0, void 0, void 0, function* () {
        event.preventDefault();
        resetErrorMessages();
        const formData = new FormData(registerForm);
        const username = formData.get('username');
        const email = formData.get('email');
        const password = formData.get('password');
        // Проверка на латиницу
        if (!isLatin(username)) {
            errorMessages.username.textContent = "Логин должен содержать только латиницу и цифры.";
            errorMessages.username.style.display = 'block';
            return;
        }
        if (!isLatin(password)) {
            errorMessages.password.textContent = "Пароль должен содержать только латиницу и цифры.";
            errorMessages.password.style.display = 'block';
            return;
        }
        // Валидация формы
        if (!validateForm()) {
            return;
        }
        const userData = {
            username: username,
            email: email,
            password: password,
        };
        try {
            const response = yield fetch('/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });
            console.log('Статус ответа:', response.status);
            const data = yield response.json();
            console.log('Ответ от сервера:', data); // Выводим ответ для отладки
            if (!response.ok) {
                // Обработка ошибки от сервера
                if (data.message) {
                    if (data.message.includes("Username")) {
                        errorMessages.username.textContent = data.message;
                        errorMessages.username.style.display = 'block';
                    }
                    else if (data.message.includes("Email")) {
                        errorMessages.email.textContent = data.message;
                        errorMessages.email.style.display = 'block';
                    }
                    else {
                        alert(`Ошибка регистрации: ${data.message}`);
                    }
                }
                else if (data.error) {
                    // Если в ответе есть поле error
                    alert(`Ошибка регистрации: ${data.error}`);
                }
                else {
                    alert("Ошибка регистрации: Неизвестная ошибка.");
                }
                return;
            }
            // Успешная регистрация
            if (data.message) {
                alert("Вы успешно зарегистрированы! Теперь авторизуйтесь.");
                window.location.href = '../login/login.html';
            }
            else {
                alert("Регистрация прошла успешно, но сервер не вернул сообщение.");
            }
        }
        catch (error) {
            console.error("Ошибка при отправке данных: ", error);
            if (error instanceof Error) {
                alert(`Произошла ошибка при регистрации: ${error.message}`);
            }
            else {
                alert("Произошла ошибка при регистрации.");
            }
        }
    }));
    // Валидация полей в реальном времени
    const inputs = registerForm.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('input', () => {
            const errorMessage = errorMessages[input.name];
            if (!input.validity.valid) {
                errorMessage.textContent = input.validationMessage;
                errorMessage.style.display = 'block';
            }
            else {
                errorMessage.style.display = 'none';
            }
            let valid = true;
            inputs.forEach(i => {
                if (!i.validity.valid) {
                    valid = false;
                }
            });
            registerButton.disabled = !valid;
        });
    });
});
