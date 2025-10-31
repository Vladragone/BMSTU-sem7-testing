interface UserData {
    username: string;
    email: string;
    password: string;
}

interface ErrorMessages {
    username: HTMLSpanElement;
    email: HTMLSpanElement;
    password: HTMLSpanElement;
}

document.addEventListener('DOMContentLoaded', () => {
    const backButton = document.getElementById('backBtn') as HTMLButtonElement | null;

    // Кнопка "Назад"
    if (backButton) {
        backButton.addEventListener('click', () => {
            window.location.href = '../start/start.html';
        });
    }
    const registerForm = document.getElementById('registerForm') as HTMLFormElement;
    const registerButton = document.getElementById('registerButton') as HTMLButtonElement;

    const errorMessages: ErrorMessages = {
        username: document.getElementById('usernameError') as HTMLSpanElement,
        email: document.getElementById('emailError') as HTMLSpanElement,
        password: document.getElementById('passwordError') as HTMLSpanElement,
    };

    // Функция для проверки латиницы
    function isLatin(str: string): boolean {
        return /^[A-Za-z0-9]+$/.test(str);
    }

    // Функция для сброса сообщений об ошибках
    function resetErrorMessages(): void {
        Object.values(errorMessages).forEach(el => el.style.display = 'none');
    }

    // Функция для валидации формы
    function validateForm(): boolean {
        let isValid = true;
        const inputs = registerForm.querySelectorAll('input') as NodeListOf<HTMLInputElement>;

        inputs.forEach(input => {
            if (!input.validity.valid) {
                const errorMessage = errorMessages[input.name as keyof ErrorMessages];
                errorMessage.textContent = input.validationMessage;
                errorMessage.style.display = 'block';
                isValid = false;
            }
        });

        return isValid;
    }

    // Обработчик отправки формы
    registerForm.addEventListener('submit', async (event: Event) => {
        event.preventDefault();
        resetErrorMessages();

        const formData = new FormData(registerForm);
        const username = formData.get('username') as string;
        const email = formData.get('email') as string;
        const password = formData.get('password') as string;

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

        const userData: UserData = {
            username: username,
            email: email,
            password: password,
        };

        try {
            const response = await fetch('/api/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(userData),
            });

            console.log('Статус ответа:', response.status);

            const data = await response.json();
            console.log('Ответ от сервера:', data); // Выводим ответ для отладки

            if (!response.ok) {
                // Обработка ошибки от сервера
                if (data.message) {
                    if (data.message.includes("Username")) {
                        errorMessages.username.textContent = data.message;
                        errorMessages.username.style.display = 'block';
                    } else if (data.message.includes("Email")) {
                        errorMessages.email.textContent = data.message;
                        errorMessages.email.style.display = 'block';
                    } else {
                        alert(`Ошибка регистрации: ${data.message}`);
                    }
                } else if (data.error) {
                    // Если в ответе есть поле error
                    alert(`Ошибка регистрации: ${data.error}`);
                } else {
                    alert("Ошибка регистрации: Неизвестная ошибка.");
                }
                return;
            }

            // Успешная регистрация
            if (data.message) {
                alert("Вы успешно зарегистрированы! Теперь авторизуйтесь.");
                window.location.href = '../login/login.html';
            } else {
                alert("Регистрация прошла успешно, но сервер не вернул сообщение.");
            }
        } catch (error) {
            console.error("Ошибка при отправке данных: ", error);

            if (error instanceof Error) {
                alert(`Произошла ошибка при регистрации: ${error.message}`);
            } else {
                alert("Произошла ошибка при регистрации.");
            }
        }
    });

    // Валидация полей в реальном времени
    const inputs = registerForm.querySelectorAll('input') as NodeListOf<HTMLInputElement>;
    inputs.forEach(input => {
        input.addEventListener('input', () => {
            const errorMessage = errorMessages[input.name as keyof ErrorMessages];
            if (!input.validity.valid) {
                errorMessage.textContent = input.validationMessage;
                errorMessage.style.display = 'block';
            } else {
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
