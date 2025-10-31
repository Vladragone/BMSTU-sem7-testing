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
document.addEventListener('DOMContentLoaded', () => __awaiter(void 0, void 0, void 0, function* () {
    const backButton = document.getElementById('backBtn');
    // Кнопка "Назад"
    if (backButton) {
        backButton.addEventListener('click', () => {
            window.location.href = '../start/start.html';
        });
    }
    // Получаем JWT токен из localStorage (предполагается, что он был сохранён после логина)
    const token = localStorage.getItem('authToken');
    if (!token) {
        // Если токена нет, перенаправляем на страницу логина
        window.location.href = 'login.html';
        return;
    }
    try {
        const response = yield fetch('http://localhost:8080/api/profile', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }
        // Ожидается объект с данными профиля:
        // { user: { id: <номер пользователя> }, regDate, gameNum, score }
        const profileData = yield response.json();
        // Находим элементы для отображения данных
        const userIdDisplay = document.getElementById('userIdDisplay');
        const regDateDisplay = document.getElementById('regDateDisplay');
        const gameNumDisplay = document.getElementById('gameNumDisplay');
        const scoreDisplay = document.getElementById('scoreDisplay');
        // Выводим данные в соответствующие элементы:
        if (userIdDisplay)
            userIdDisplay.textContent = profileData.user.id.toString();
        if (regDateDisplay) {
            const date = new Date(profileData.regDate);
            regDateDisplay.textContent = date.toLocaleString();
        }
        if (gameNumDisplay)
            gameNumDisplay.textContent = profileData.gameNum.toString();
        if (scoreDisplay)
            scoreDisplay.textContent = profileData.score.toString();
    }
    catch (error) {
        console.error('Ошибка при получении данных профиля:', error);
        const profileStats = document.getElementById('profileStats');
        if (profileStats) {
            profileStats.textContent = 'Ошибка загрузки профиля.';
        }
    }
    // Добавляем обработчик для кнопки "Рейтинг"
    const ratingButton = document.getElementById('ratingButton');
    if (ratingButton) {
        ratingButton.addEventListener('click', () => {
            // Перенаправляем пользователя на страницу рейтинга
            window.location.href = '../rating/rating.html';
        });
    }
}));
