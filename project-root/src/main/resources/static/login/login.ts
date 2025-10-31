const loginForm = document.getElementById('loginForm') as HTMLFormElement | null;
const loginButton = document.getElementById('loginButton') as HTMLButtonElement | null;

// Элемент для вывода сообщений об ошибках
const errorMsg = document.getElementById('errorMsg') as HTMLDivElement | null;

function validateLoginForm(): boolean {
    const usernameInput = document.getElementById('username') as HTMLInputElement | null;
    const passwordInput = document.getElementById('password') as HTMLInputElement | null;

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
    const backButton = document.getElementById('backBtn') as HTMLButtonElement | null;

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
    loginButton.addEventListener('click', async (event: Event) => {
        event.preventDefault();

        const usernameInput = document.getElementById('username') as HTMLInputElement | null;
        const passwordInput = document.getElementById('password') as HTMLInputElement | null;

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
            const response = await fetch('http://localhost:8080/api/auth', {
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
                const data = await response.json();
                console.log('Ответ от сервера:', data);
                if (data.token) {
                    localStorage.setItem('authToken', data.token);
                }
                // Немедленный переход на страницу профиля без задержки
                window.location.href = '../profile/profile.html';
            } else {
                // Ошибка (4xx/5xx)
                const errorData = await response.json();
                console.error('Ошибка входа:', errorData.message || 'Неизвестная ошибка');
                errorMsg.textContent = errorData.message || 'Неизвестная ошибка при входе';
            }
        } catch (error) {
            console.error('Ошибка при отправке запроса:', error);
            errorMsg.textContent = 'Ошибка при отправке запроса к серверу';
        }
    });
});
