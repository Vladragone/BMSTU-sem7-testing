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
    const profileButton = document.getElementById('profileBtn');
    // Кнопка "Назад"
    if (profileButton) {
        profileButton.addEventListener('click', () => {
            window.location.href = '../profile/profile.html';
        });
    }
    const ratingList = document.getElementById('ratingList');
    const ratingSwitch = document.getElementById('ratingSwitch');
    const ratingContent = document.getElementById('ratingContent');
    const yourRankDiv = document.getElementById('yourRank');
    // По умолчанию фильтр по очкам
    let currentFilter = 'points';
    // Получаем токен для запроса yourRank
    const token = localStorage.getItem('authToken');
    function loadRating() {
        return __awaiter(this, void 0, void 0, function* () {
            if (ratingList) {
                ratingList.innerHTML = '<p>Загрузка рейтинга...</p>';
            }
            const endpoint = `http://localhost:8080/api/rating/top?filter=${currentFilter}`;
            try {
                const response = yield fetch(endpoint, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (!response.ok) {
                    throw new Error(`Ошибка HTTP: ${response.status}`);
                }
                const profiles = yield response.json();
                if (ratingList) {
                    // Очищаем содержимое и добавляем заголовок таблицы
                    ratingList.innerHTML = '<div class="tableHeader">Топ-3 игроков</div>';
                    profiles.slice(0, 3).forEach((profile, index) => {
                        const p = document.createElement('p');
                        p.className = 'ratingItem';
                        if (currentFilter === 'points') {
                            p.textContent = `${index + 1}. ${profile.user.username} — ${profile.score} очков`;
                        }
                        else {
                            p.textContent = `${index + 1}. ${profile.user.username} — ${profile.gameNum} игр`;
                        }
                        ratingList.appendChild(p);
                    });
                }
            }
            catch (error) {
                console.error('Ошибка при получении данных рейтинга:', error);
                if (ratingList) {
                    ratingList.textContent = 'Ошибка загрузки рейтинга.';
                }
            }
        });
    }
    function loadYourRank() {
        return __awaiter(this, void 0, void 0, function* () {
            if (yourRankDiv) {
                yourRankDiv.innerHTML = '<p>Загрузка вашего места в рейтинге...</p>';
            }
            const rankEndpoint = `http://localhost:8080/api/rating/yourRank?filter=${currentFilter}`;
            try {
                const rankResponse = yield fetch(rankEndpoint, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    }
                });
                if (!rankResponse.ok) {
                    throw new Error(`Ошибка HTTP: ${rankResponse.status}`);
                }
                const rankData = yield rankResponse.json();
                if (yourRankDiv) {
                    yourRankDiv.textContent = `Ваше место в рейтинге: ${rankData.rank}`;
                }
            }
            catch (error) {
                console.error('Ошибка при получении вашего места в рейтинге:', error);
                if (yourRankDiv) {
                    yourRankDiv.textContent = 'Ошибка загрузки вашего места в рейтинге.';
                }
            }
        });
    }
    // Начальная загрузка рейтинга и места
    loadRating();
    loadYourRank();
    // Обработчик переключения фильтра
    if (ratingSwitch && ratingContent) {
        ratingSwitch.addEventListener('change', () => {
            currentFilter = ratingSwitch.checked ? 'games' : 'points';
            if (currentFilter === 'games') {
                ratingContent.classList.remove('points-mode');
                ratingContent.classList.add('games-mode');
            }
            else {
                ratingContent.classList.remove('games-mode');
                ratingContent.classList.add('points-mode');
            }
            loadRating();
            loadYourRank();
        });
    }
}));
