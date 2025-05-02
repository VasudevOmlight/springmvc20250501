document.addEventListener('DOMContentLoaded', () => {

    // Отримуємо всі необхідні DOM елементи
    const searchInput = document.getElementById('searchInput');
    const tableBody = document.getElementById('productTableBody');
    const selectAllHeader = document.getElementById('select-all-header');
    const selectedPositionsCell = document.getElementById('selected-positions');
    const selectedUnitsCell = document.getElementById('selected-units');
    const selectedValueCell = document.getElementById('selected-value');
    const bodyElement = document.body; // Отримуємо тіло документа для data-атрибута

    // Перевіряємо наявність усіх необхідних елементів
    if (!searchInput || !tableBody || !selectAllHeader || !selectedPositionsCell || !selectedUnitsCell || !selectedValueCell || !bodyElement) {
        console.error("Одна або кілька необхідних DOM елементів не знайдено. Ініціалізація скрипта не відбулася.");
        // Можна додати повідомлення для користувача, якщо потрібно
        return; // Зупиняємо виконання скрипта, якщо критичні елементи відсутні
    }

    // Тепер, коли ми впевнені, що tableBody існує, можемо отримати рядки
    const tableRows = tableBody.getElementsByTagName('tr');


    // Отримуємо поточну локаль та виправляємо формат для Intl.NumberFormat
    let currentLocale = bodyElement.getAttribute('data-locale') || 'en-US';
    // Замінюємо '_' на '-' для сумісності з Intl.NumberFormat
    currentLocale = currentLocale.replace('_', '-');


    // Створюємо форматувальник чисел один раз для поточної локалі
    // Перевіряємо, чи підтримується Intl.NumberFormat
    let localeNumberFormatter;
    try {
        localeNumberFormatter = new Intl.NumberFormat(currentLocale, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    } catch (e) {
        console.error("Intl.NumberFormat не підтримується або локаль '" + currentLocale + "' недійсна. Використовується форматування за замовчуванням.", e);
        // Якщо Intl.NumberFormat недоступний або локаль недійсна, створюємо простий форматувальник
        localeNumberFormatter = {
            format: function(number) {
                // Просте форматування за замовчуванням (крапка як роздільник)
                return parseFloat(number).toFixed(2);
            }
        };
    }

    // Форматування цін у видимих комірках таблиці
    function formatVisiblePrices() {
        // Проходимо по всіх рядках таблиці
        for (let i = 0; i < tableRows.length; i++) {
            const row = tableRows[i];
            // Перевіряємо, чи рядок видимий (стиль display не 'none')
            if (row.style.display !== 'none') {
                // Знаходимо комірку ціни за класом
                const priceCell = row.querySelector('.product-price-cell');
                // Знаходимо чекбокс у цьому ж рядку, щоб отримати сиру ціну
                const checkbox = row.querySelector('.product-checkbox');

                if (priceCell && checkbox) {
                    // Отримуємо сире значення ціни з data-атрибута чекбокса
                    const rawPrice = parseFloat(checkbox.getAttribute('data-raw-price')) || 0.0;
                    // Форматуємо сиру ціну за допомогою створеного форматувальника
                    priceCell.textContent = localeNumberFormatter.format(rawPrice);
                }
            }
        }
    }

    //  Функція для оновлення таблиці підсумків (без змін логіки розрахунку)
    function updateSummary() {
        let positions = 0;
        let units = 0;
        let value = 0.0;

        // Проходимо тільки по ВИДИМИХ рядках
        for (let i = 0; i < tableRows.length; i++) {
            const row = tableRows[i];
            // Перевіряємо, чи рядок видимий (стиль display не 'none')
            if (row.style.display !== 'none') {
                const checkbox = row.querySelector('.product-checkbox');
                // Перевіряємо, чи чекбокс існує і чи він вибраний
                if (checkbox && checkbox.checked) {
                    positions++;
                    // Отримуємо значення кількості як ціле число
                    const amount = parseInt(checkbox.getAttribute('data-amount'), 10) || 0;
                    // ЗЧИТУЄМО СИРЕ ЧИСЛОВЕ ЗНАЧЕННЯ ЦІНИ З АТРИБУТУ data-raw-price
                    const price = parseFloat(checkbox.getAttribute('data-raw-price')) || 0.0;
                    units += amount;
                    value += amount * price;
                }
            }
        }

        // Оновлюємо вміст осередків таблиці підсумків
        if (selectedPositionsCell) {
            selectedPositionsCell.textContent = positions;
        }
        if (selectedUnitsCell) {
            selectedUnitsCell.textContent = units;
        }
        // Форматуємо загальну вартість згідно з поточною локаллю, використовуючи створений форматувальник
        if (selectedValueCell) {
            selectedValueCell.textContent = localeNumberFormatter.format(value);
        }
    }

    //  Функція для фільтрації таблиці (клієнтський пошук)
    function filterTable() {
        const searchText = searchInput ? searchInput.value.toLowerCase() : '';
        // Отримуємо рядки ТІЛЬКИ якщо tableBody існує
        const currentTableRows = tableBody ? tableBody.getElementsByTagName('tr') : [];

        for (let i = 0; i < currentTableRows.length; i++) {
            const row = currentTableRows[i];
            // Індекси комірок Назва (3) та Опис (4)
            const nameCell = row.cells.length > 3 ? row.cells[3] : null;
            const descriptionCell = row.cells.length > 4 ? row.cells[4] : null;

            if (nameCell || descriptionCell) {
                const nameText = nameCell ? nameCell.textContent.toLowerCase() : '';
                const descriptionText = descriptionCell ? descriptionCell.textContent.toLowerCase() : '';
                const isMatch = nameText.includes(searchText) || descriptionText.includes(searchText);
                row.style.display = isMatch ? '' : 'none';
            } else {
                row.style.display = searchText ? 'none' : '';
            }
        }
        //  Виклик: Оновлюємо підсумки ПІСЛЯ  фільтрації
        updateSummary();
        //  Виклик нової функції:  Форматуємо ціни у видимих рядках після фільтрації
        formatVisiblePrices();
    }

    //  Функція для вибору/зняття вибору всіх ВИДИМИХ чекбоксів
    function toggleSelectAll() {
        let targetState = true; // За замовчуванням - вибрати все

        // Отримуємо рядки ТІЛЬКИ якщо tableBody існує
        const currentTableRows = tableBody ? tableBody.getElementsByTagName('tr') : [];

        // Визначаємо, чи потрібно вибрати чи зняти вибір
        let foundUnchecked = false;
        let hasVisibleCheckbox = false;
        for (let i = 0; i < currentTableRows.length; i++) {
            if (currentTableRows[i].style.display !== 'none') { // Тільки видимі рядки
                hasVisibleCheckbox = true;
                const checkbox = currentTableRows[i].querySelector('.product-checkbox');
                if (checkbox && !checkbox.checked) {
                    foundUnchecked = true;
                    break;
                }
            }
        }
        if (!foundUnchecked && hasVisibleCheckbox) {
            targetState = false;
        }

        // Застосовуємо новий стан до всіх ВИДИМИХ чекбоксів
        for (let i = 0; i < currentTableRows.length; i++) {
            if (currentTableRows[i].style.display !== 'none') { // Тільки видимі рядки
                const checkbox = currentTableRows[i].querySelector('.product-checkbox');
                if (checkbox) {
                    checkbox.checked = targetState;
                }
            }
        }

        // ВИКЛИК: Оновлюємо підсумки ПІСЛЯ зміни стану чекбоксів
        updateSummary();
        //  Викликаємо форматування цін, хоча зазвичай не потрібно після зміни вибору
        // formatVisiblePrices(); // Можна закоментувати, якщо не викликає проблем і не потрібно
    }


    //  Додавання обробників подій

    if (searchInput) {
        searchInput.addEventListener('input', filterTable);
    }

    if (selectAllHeader) {
        selectAllHeader.addEventListener('click', toggleSelectAll);
    }

    if (tableBody) {
        tableBody.addEventListener('change', (event) => {
            if (event.target.classList.contains('product-checkbox')) {
                updateSummary();
                // --- Викликаємо форматування цін, хоча зазвичай не потрібно після зміни вибору ---
                // formatVisiblePrices(); // Можна закоментувати
            }
        });
    }


    //  Ініціалізація при завантаженні - Викликаємо filterTable, яка в свою чергу викличе updateSummary та formatVisiblePrices
    if (tableBody) { // Перевірка, що таблиця існує перед ініціалізацією
        // Застосовуємо початковий фільтр (якщо є в полі пошуку з URL) та виконуємо перші підрахунки/форматування
        filterTable();
        // Викликаємо форматування цін ще раз після завантаження та фільтрації, для впевненості
        formatVisiblePrices();
    } else {
        console.error("Елемент 'productTableBody' не знайдено при початковому завантаженні. Скрипт ініціалізації не завершено коректно.");
        // Можна додати повідомлення для користувача
    }


    console.log("Product list script loaded. Initializing with locale:", currentLocale);

});