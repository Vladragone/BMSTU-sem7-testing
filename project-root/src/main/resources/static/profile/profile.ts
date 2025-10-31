document.addEventListener('DOMContentLoaded', async () => {
    const backButton = document.getElementById('backBtn') as HTMLButtonElement | null;

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
      const response = await fetch('http://localhost:8080/api/profile', {
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
      const profileData = await response.json();
      
      // Находим элементы для отображения данных
      const userIdDisplay = document.getElementById('userIdDisplay');
      const regDateDisplay = document.getElementById('regDateDisplay');
      const gameNumDisplay = document.getElementById('gameNumDisplay');
      const scoreDisplay = document.getElementById('scoreDisplay');
      
      // Выводим данные в соответствующие элементы:
      if (userIdDisplay) userIdDisplay.textContent = profileData.user.id.toString();
      if (regDateDisplay) {
        const date = new Date(profileData.regDate);
        regDateDisplay.textContent = date.toLocaleString();
      }
      if (gameNumDisplay) gameNumDisplay.textContent = profileData.gameNum.toString();
      if (scoreDisplay) scoreDisplay.textContent = profileData.score.toString();
    } catch (error) {
      console.error('Ошибка при получении данных профиля:', error);
      const profileStats = document.getElementById('profileStats');
      if (profileStats) {
        profileStats.textContent = 'Ошибка загрузки профиля.';
      }
    }
  
    // Добавляем обработчик для кнопки "Рейтинг"
    const ratingButton = document.getElementById('ratingButton') as HTMLButtonElement | null;
    if (ratingButton) {
      ratingButton.addEventListener('click', () => {
        // Перенаправляем пользователя на страницу рейтинга
        window.location.href = '../rating/rating.html';
      });
    }

});

