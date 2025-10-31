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
const loginForm = document.getElementById('loginForm');
const loginButton = document.getElementById('loginButton');
// Элемент для вывода сообщений об ошибках
const errorMsg = document.getElementById('errorMsg');
function validateLoginForm() {
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    if (!usernameInput || !passwordInput || !loginButton) {
        return false;
    }
    const username = usernameInput.value;
    const password = passwordInput.value;
    const usernameValid = username.length >= 6;
    const passwordValid = password.length >= 6;
    // Очищаем сообщение об ошибке при каждом вводе
    if (errorMsg) {
        errorMsg.textContent = '';
    }
    loginButton.disabled = !(usernameValid && passwordValid);
    return usernameValid && passwordValid;
}
document.addEventListener('DOMContentLoaded', () => {
    const backButton = document.getElementById('backBtn');
    // Кнопка "Назад"
    if (backButton) {
        backButton.addEventListener('click', () => {
            window.location.href = '../start/start.html';
        });
    }
    if (!loginForm || !loginButton) {
        console.error('Форма входа или кнопка входа не найдены');
        return;
    }
    // Слушаем ввод в полях формы и валидируем
    loginForm.addEventListener('input', validateLoginForm);
    // Обработка нажатия кнопки "Войти"
    loginButton.addEventListener('click', (event) => __awaiter(void 0, void 0, void 0, function* () {
        event.preventDefault();
        const usernameInput = document.getElementById('username');
        const passwordInput = document.getElementById('password');
        if (!usernameInput || !passwordInput || !errorMsg) {
            console.error('Ошибка: недоступные элементы формы');
            return;
        }
        // Снова проверяем форму
        if (!validateLoginForm()) {
            errorMsg.textContent = 'Логин и пароль должны быть не короче 6 символов';
            return;
        }
        const username = usernameInput.value;
        const password = passwordInput.value;
        // Очищаем сообщение об ошибке перед отправкой
        errorMsg.textContent = '';
        try {
            const response = yield fetch('http://localhost:8080/api/auth', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username,
                    password
                })
            });
            if (response.ok) {
                const data = yield response.json();
                console.log('Ответ от сервера:', data);
                if (data.token) {
                    localStorage.setItem('authToken', data.token);
                }
                // Немедленный переход на страницу профиля без задержки
                window.location.href = '../profile/profile.html';
            }
            else {
                // Ошибка (4xx/5xx)
                const errorData = yield response.json();
                console.error('Ошибка входа:', errorData.message || 'Неизвестная ошибка');
                errorMsg.textContent = errorData.message || 'Неизвестная ошибка при входе';
            }
        }
        catch (error) {
            console.error('Ошибка при отправке запроса:', error);
            errorMsg.textContent = 'Ошибка при отправке запроса к серверу';
        }
    }));
});
