#include <windows.h>
#include <commctrl.h>
#include <string>
#include <vector>

#pragma comment(lib, "comctl32.lib")

// ==================== КЛАССЫ БЕЗ ПАТТЕРНА ====================
// Проблема: нет единого интерфейса, разные списки для разных типов

class Cargo {
public:
    std::string name;
    double weight;
    std::string barcode;

    Cargo(const std::string& n, double w, const std::string& bc)
        : name(n), weight(w), barcode(bc) {}
};

class Container {
public:
    std::string name;
    std::string id;
    std::vector<Cargo*> cargos;
    std::vector<Container*> containers;

    Container(const std::string& n, const std::string& i)
        : name(n), id(i) {}

    ~Container() {
        for (auto c : cargos) delete c;
        for (auto c : containers) delete c;
    }

    double getTotalWeight() {
        double total = 0;
        for (auto c : cargos) total += c->weight;
        for (auto c : containers) total += c->getTotalWeight();
        return total;
    }
};

// ==================== СОЗДАНИЕ ТЕСТОВЫХ ДАННЫХ ====================
Container* CreateTestData() {
    Container* warehouse = new Container("Склад Москва", "WH-001");

    // Контейнер 1: морской
    Container* container1 = new Container("Морской контейнер 20ft", "CONT-001");
    container1->cargos.push_back(new Cargo("Ноутбуки Dell", 450, "BC-001"));
    container1->cargos.push_back(new Cargo("Мониторы Samsung", 300, "BC-002"));

    // Контейнер 2: рефрижератор
    Container* container2 = new Container("Рефрижератор Arctic", "CONT-002");
    container2->cargos.push_back(new Cargo("Мороженое", 200, "BC-003"));
    container2->cargos.push_back(new Cargo("Рыба замороженная", 350, "BC-004"));

    // Большой контейнер (вкладываем другие контейнеры)
    Container* bigContainer = new Container("Составной контейнер Mega", "CONT-010");
    bigContainer->containers.push_back(container1);
    bigContainer->containers.push_back(container2);

    // Добавляем на склад
    warehouse->containers.push_back(bigContainer);
    warehouse->cargos.push_back(new Cargo("Станок фрезерный", 1200, "BC-005"));
    warehouse->cargos.push_back(new Cargo("Мебель офисная", 400, "BC-006"));

    return warehouse;
}

// ==================== ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ ====================
Container* g_warehouse = nullptr;
HWND g_hTreeView = nullptr;
HWND g_hStatusBar = nullptr;
HWND g_hEditSearch = nullptr;

// ==================== ФУНКЦИИ ДЛЯ ОТОБРАЖЕНИЯ ====================

// Добавление контейнера и его содержимого в дерево
void AddContainerToTree(Container* cont, HWND hTree, HTREEITEM hParent, int level) {
    TVINSERTSTRUCT tv = { 0 };
    tv.hParent = hParent;
    tv.hInsertAfter = TVI_LAST;
    tv.item.mask = TVIF_TEXT;

    char text[512];
    std::string indent(level * 2, ' ');
    sprintf_s(text, "%s📦 %s [%s] | Вес: %.0f кг",
        indent.c_str(), cont->name.c_str(), cont->id.c_str(), cont->getTotalWeight());
    tv.item.pszText = text;

    HTREEITEM hItem = TreeView_InsertItem(hTree, &tv);

    // Добавляем грузы
    for (size_t i = 0; i < cont->cargos.size(); i++) {
        TVINSERTSTRUCT tv2 = { 0 };
        tv2.hParent = hItem;
        tv2.hInsertAfter = TVI_LAST;
        tv2.item.mask = TVIF_TEXT;

        sprintf_s(text, "    📄 %s | Вес: %.0f кг | ШК: %s",
            cont->cargos[i]->name.c_str(),
            cont->cargos[i]->weight,
            cont->cargos[i]->barcode.c_str());
        tv2.item.pszText = text;
        TreeView_InsertItem(hTree, &tv2);
    }

    // Добавляем вложенные контейнеры
    for (size_t i = 0; i < cont->containers.size(); i++) {
        AddContainerToTree(cont->containers[i], hTree, hItem, level + 1);
    }
}

// Обновление всего дерева
void RefreshTreeView() {
    TreeView_DeleteAllItems(g_hTreeView);

    TVINSERTSTRUCT tv = { 0 };
    tv.hParent = NULL;
    tv.hInsertAfter = TVI_LAST;
    tv.item.mask = TVIF_TEXT;

    char text[512];
    sprintf_s(text, "🏢 СКЛАД: %s [%s]", g_warehouse->name.c_str(), g_warehouse->id.c_str());
    tv.item.pszText = text;

    HTREEITEM hRoot = TreeView_InsertItem(g_hTreeView, &tv);

    // Добавляем контейнеры
    for (size_t i = 0; i < g_warehouse->containers.size(); i++) {
        AddContainerToTree(g_warehouse->containers[i], g_hTreeView, hRoot, 1);
    }

    // Добавляем отдельные грузы
    for (size_t i = 0; i < g_warehouse->cargos.size(); i++) {
        TVINSERTSTRUCT tv2 = { 0 };
        tv2.hParent = hRoot;
        tv2.hInsertAfter = TVI_LAST;
        tv2.item.mask = TVIF_TEXT;

        sprintf_s(text, "    📄 %s | Вес: %.0f кг | ШК: %s",
            g_warehouse->cargos[i]->name.c_str(),
            g_warehouse->cargos[i]->weight,
            g_warehouse->cargos[i]->barcode.c_str());
        tv2.item.pszText = text;
        TreeView_InsertItem(g_hTreeView, &tv2);
    }

    TreeView_Expand(g_hTreeView, hRoot, TVE_EXPAND);

    // Обновляем статус-бар
    char status[512];
    sprintf_s(status, "Общий вес: %.0f кг | ⚠️ ПРОБЛЕМА: контейнеры и грузы в РАЗНЫХ списках!",
        g_warehouse->getTotalWeight());
    SendMessageA(g_hStatusBar, SB_SETTEXT, 0, (LPARAM)status);
}

// ==================== ОКОННАЯ ПРОЦЕДУРА ====================
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        // Инициализация Common Controls
        INITCOMMONCONTROLSEX icex = { sizeof(INITCOMMONCONTROLSEX), ICC_TREEVIEW_CLASSES | ICC_BAR_CLASSES };
        InitCommonControlsEx(&icex);

        // Создание тестовых данных
        g_warehouse = CreateTestData();

        RECT rc;
        GetClientRect(hwnd, &rc);

        // Кнопки управления
        CreateWindowA("BUTTON", "❌ ДОБАВИТЬ ГРУЗ (ПРОБЛЕМА)", WS_CHILD | WS_VISIBLE,
            10, 10, 220, 35, hwnd, (HMENU)1, NULL, NULL);

        CreateWindowA("BUTTON", "❌ ДОБАВИТЬ КОНТЕЙНЕР (ПРОБЛЕМА)", WS_CHILD | WS_VISIBLE,
            10, 55, 220, 35, hwnd, (HMENU)2, NULL, NULL);

        // Поле поиска
        CreateWindowA("STATIC", "Поиск по штрих-коду:", WS_CHILD | WS_VISIBLE,
            10, 100, 120, 20, hwnd, NULL, NULL, NULL);
        g_hEditSearch = CreateWindowA("EDIT", "BC-001", WS_CHILD | WS_VISIBLE | WS_BORDER,
            10, 125, 180, 25, hwnd, NULL, NULL, NULL);
        CreateWindowA("BUTTON", "Поиск (НЕ РАБОТАЕТ)", WS_CHILD | WS_VISIBLE,
            200, 125, 110, 25, hwnd, (HMENU)3, NULL, NULL);

        // Дерево
        g_hTreeView = CreateWindowA(WC_TREEVIEWA, "", WS_CHILD | WS_VISIBLE | WS_BORDER |
            TVS_HASLINES | TVS_HASBUTTONS | TVS_LINESATROOT,
            10, 165, rc.right - 20, rc.bottom - 230, hwnd, NULL, NULL, NULL);

        // Статус-бар
        g_hStatusBar = CreateWindowA(STATUSCLASSNAMEA, "", WS_CHILD | WS_VISIBLE | SBARS_SIZEGRIP,
            0, 0, 0, 0, hwnd, NULL, NULL, NULL);

        // Пояснительный текст
        CreateWindowA("STATIC",
            "╔══════════════════════════════════════════════════════════╗\n"
            "║     ⚠️ ПРОБЛЕМЫ АРХИТЕКТУРЫ БЕЗ ПАТТЕРНА COMPOSITE      ║\n"
            "╠══════════════════════════════════════════════════════════╣\n"
            "║ 1. Склад хранит ДВА разных списка:                      ║\n"
            "║    - vector<Container*> containers                       ║\n"
            "║    - vector<Cargo*> looseCargos                          ║\n"
            "║                                                          ║\n"
            "║ 2. Контейнер хранит ДВА разных списка:                  ║\n"
            "║    - vector<Cargo*> cargos                               ║\n"
            "║    - vector<Container*> subContainers                    ║\n"
            "║                                                          ║\n"
            "║ 3. НЕТ единого интерфейса для всех элементов            ║\n"
            "║                                                          ║\n"
            "║ 4. Поиск по штрих-коду требует сложной рекурсии         ║\n"
            "║    (НЕ РЕАЛИЗОВАН в этой версии)                         ║\n"
            "║                                                          ║\n"
            "║ 5. Добавление нового типа (например, Паллета)           ║\n"
            "║    требует изменения ВСЕХ существующих классов!         ║\n"
            "╠══════════════════════════════════════════════════════════╣\n"
            "║ ❌ РЕАЛЬНОЕ ДОБАВЛЕНИЕ НЕ РЕАЛИЗОВАНО                   ║\n"
            "║    из-за сложности архитектуры!                          ║\n"
            "╚══════════════════════════════════════════════════════════╝",
            WS_CHILD | WS_VISIBLE | WS_BORDER,
            rc.right - 320, 10, 310, 400, hwnd, NULL, NULL, NULL);

        RefreshTreeView();
        break;
    }

    case WM_SIZE: {
        RECT rc;
        GetClientRect(hwnd, &rc);

        SendMessage(g_hStatusBar, WM_SIZE, 0, 0);

        RECT rcStatus;
        GetWindowRect(g_hStatusBar, &rcStatus);
        int statusHeight = rcStatus.bottom - rcStatus.top;

        SetWindowPos(g_hTreeView, NULL, 10, 165,
            rc.right - 20, rc.bottom - statusHeight - 195, SWP_NOZORDER);
        break;
    }

    case WM_COMMAND: {
        if (LOWORD(wParam) == 1) {
            MessageBoxA(hwnd,
                "⚠️ ПРОБЛЕМА АРХИТЕКТУРЫ ⚠️\n\n"
                "В версии БЕЗ паттерна добавление груза требует:\n"
                "1. Определять, добавлять в контейнер или на склад\n"
                "2. Работать с двумя разными списками\n"
                "3. Писать разную логику для каждого типа\n\n"
                "❌ Реализация слишком сложна и подвержена ошибкам!\n\n"
                "С паттерном Composite это решается одной строкой:\n"
                "parent->add(std::make_shared<Cargo>(...));",
                "Проблема архитектуры", MB_ICONERROR);
        }
        else if (LOWORD(wParam) == 2) {
            MessageBoxA(hwnd,
                "⚠️ ПРОБЛЕМА РАСШИРЯЕМОСТИ ⚠️\n\n"
                "В версии БЕЗ паттерна добавление контейнера требует:\n"
                "1. Изменения класса Warehouse (новый список)\n"
                "2. Изменения всех методов для работы с новым типом\n"
                "3. Дублирования кода\n\n"
                "❌ Каждый новый тип требует изменения всех классов!\n\n"
                "С паттерном Composite новый тип просто реализует\n"
                "интерфейс CargoComponent и готов к использованию.",
                "Проблема расширяемости", MB_ICONERROR);
        }
        else if (LOWORD(wParam) == 3) {
            char barcode[256];
            GetWindowTextA(g_hEditSearch, barcode, 255);
            char msg[512];
            sprintf_s(msg,
                "🔍 ПРОБЛЕМА ПОИСКА 🔍\n\n"
                "В версии БЕЗ паттерна поиск по штрих-коду требует:\n"
                "1. Рекурсивного обхода ДВУХ разных списков\n"
                "2. Раздельной логики для контейнеров и грузов\n"
                "3. Сложной реализации с множеством условий\n\n"
                "Искомый штрих-код: %s\n\n"
                "❌ Поиск НЕ РЕАЛИЗОВАН из-за сложности архитектуры!\n\n"
                "С паттерном Composite это одна строка:\n"
                "auto found = warehouse->findByBarcode(barcode);",
                barcode);
            MessageBoxA(hwnd, msg, "Проблема поиска", MB_ICONERROR);
        }
        break;
    }

    case WM_DESTROY:
        if (g_warehouse) delete g_warehouse;
        PostQuitMessage(0);
        break;

    default:
        return DefWindowProc(hwnd, msg, wParam, lParam);
    }
    return 0;
}

// ==================== ТОЧКА ВХОДА ====================
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    WNDCLASSA wc = { 0 };
    wc.lpfnWndProc = WndProc;
    wc.hInstance = hInstance;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.lpszClassName = "WarehouseBadClass";

    RegisterClassA(&wc);

    HWND hwnd = CreateWindowA("WarehouseBadClass",
        "❌ БЕЗ ПАТТЕРНА COMPOSITE - демонстрация проблем архитектуры",
        WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT, 1100, 650,
        NULL, NULL, hInstance, NULL);

    if (!hwnd) return 0;

    ShowWindow(hwnd, nCmdShow);
    UpdateWindow(hwnd);

    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    return msg.wParam;
}