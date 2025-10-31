interface ProfileData {
    id: number;
    user: {
      id: number;
      username: string;
      email: string;
      role: string;
    };
    score: number;
    regDate: string;
    gameNum: number;
  }
  
  document.addEventListener('DOMContentLoaded', async () => {
    const backButton = document.getElementById('backBtn') as HTMLButtonElement | null;

    // Кнопка "Назад"
    if (backButton) {
        backButton.addEventListener('click', () => {
            window.location.href = '../start/start.html';
        });
    }

    const profileButton = document.getElementById('profileBtn') as HTMLButtonElement | null;

    // Кнопка "Назад"
    if (profileButton) {
        profileButton.addEventListener('click', () => {
            window.location.href = '../profile/profile.html';
        });
    }
    const ratingList = document.getElementById('ratingList') as HTMLDivElement | null;
    const ratingSwitch = document.getElementById('ratingSwitch') as HTMLInputElement | null;
    const ratingContent = document.getElementById('ratingContent') as HTMLDivElement | null;
    const yourRankDiv = document.getElementById('yourRank') as HTMLDivElement | null;
  
    // По умолчанию фильтр по очкам
    let currentFilter: 'points' | 'games' = 'points';
  
    // Получаем токен для запроса yourRank
    const token = localStorage.getItem('authToken');
  
    async function loadRating(): Promise<void> {
      if (ratingList) {
        ratingList.innerHTML = '<p>Загрузка рейтинга...</p>';
      }
      
      const endpoint = `http://localhost:8080/api/rating/top?filter=${currentFilter}`;
      
      try {
        const response = await fetch(endpoint, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error(`Ошибка HTTP: ${response.status}`);
        }
        
        const profiles: ProfileData[] = await response.json();
        
        if (ratingList) {
          // Очищаем содержимое и добавляем заголовок таблицы
          ratingList.innerHTML = '<div class="tableHeader">Топ-3 игроков</div>';
          profiles.slice(0, 3).forEach((profile, index) => {
            const p = document.createElement('p');
            p.className = 'ratingItem';
            if (currentFilter === 'points') {
              p.textContent = `${index + 1}. ${profile.user.username} — ${profile.score} очков`;
            } else {
              p.textContent = `${index + 1}. ${profile.user.username} — ${profile.gameNum} игр`;
            }
            ratingList.appendChild(p);
          });
        }
      } catch (error) {
        console.error('Ошибка при получении данных рейтинга:', error);
        if (ratingList) {
          ratingList.textContent = 'Ошибка загрузки рейтинга.';
        }
      }
    }
    
    async function loadYourRank(): Promise<void> {
      if (yourRankDiv) {
        yourRankDiv.innerHTML = '<p>Загрузка вашего места в рейтинге...</p>';
      }
      
      const rankEndpoint = `http://localhost:8080/api/rating/yourRank?filter=${currentFilter}`;
      
      try {
        const rankResponse = await fetch(rankEndpoint, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!rankResponse.ok) {
          throw new Error(`Ошибка HTTP: ${rankResponse.status}`);
        }
        
        const rankData = await rankResponse.json();
        if (yourRankDiv) {
          yourRankDiv.textContent = `Ваше место в рейтинге: ${rankData.rank}`;
        }
      } catch (error) {
        console.error('Ошибка при получении вашего места в рейтинге:', error);
        if (yourRankDiv) {
          yourRankDiv.textContent = 'Ошибка загрузки вашего места в рейтинге.';
        }
      }
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
        } else {
          ratingContent.classList.remove('games-mode');
          ratingContent.classList.add('points-mode');
        }
        loadRating();
        loadYourRank();
      });
    }
  });
  