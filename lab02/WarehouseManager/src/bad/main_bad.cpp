#include <windows.h>
#include <commctrl.h>
#include <string>
#include <vector>

#pragma comment(lib, "comctl32.lib")

// ==================== КЛАССЫ БЕЗ ПАТТЕРНА ====================

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

    Container* container1 = new Container("Морской контейнер 20ft", "CONT-001");
    container1->cargos.push_back(new Cargo("Ноутбуки Dell", 450, "BC-001"));
    container1->cargos.push_back(new Cargo("Мониторы Samsung", 300, "BC-002"));

    Container* container2 = new Container("Рефрижератор Arctic", "CONT-002");
    container2->cargos.push_back(new Cargo("Мороженое", 200, "BC-003"));
    container2->cargos.push_back(new Cargo("Рыба замороженная", 350, "BC-004"));

    Container* bigContainer = new Container("Составной контейнер Mega", "CONT-010");
    bigContainer->containers.push_back(container1);
    bigContainer->containers.push_back(container2);

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

void AddContainerToTree(Container* cont, HWND hTree, HTREEITEM hParent, int level) {
    TVINSERTSTRUCT tv = { 0 };
    tv.hParent = hParent;
    tv.hInsertAfter = TVI_LAST;
    tv.item.mask = TVIF_TEXT;

    char text[512];
    std::string indent(level * 2, ' ');
    sprintf_s(text, "%s[КОНТЕЙНЕР] %s [%s] | Вес: %.0f кг",
        indent.c_str(), cont->name.c_str(), cont->id.c_str(), cont->getTotalWeight());
    tv.item.pszText = text;
    HTREEITEM hItem = TreeView_InsertItem(hTree, &tv);

    for (size_t i = 0; i < cont->cargos.size(); i++) {
        TVINSERTSTRUCT tv2 = { 0 };
        tv2.hParent = hItem;
        tv2.hInsertAfter = TVI_LAST;
        tv2.item.mask = TVIF_TEXT;
        sprintf_s(text, "    [ГРУЗ] %s | Вес: %.0f кг | Штрих-код: %s",
            cont->cargos[i]->name.c_str(), cont->cargos[i]->weight, cont->cargos[i]->barcode.c_str());
        tv2.item.pszText = text;
        TreeView_InsertItem(hTree, &tv2);
    }

    for (size_t i = 0; i < cont->containers.size(); i++) {
        AddContainerToTree(cont->containers[i], hTree, hItem, level + 1);
    }
}

void RefreshTreeView() {
    TreeView_DeleteAllItems(g_hTreeView);

    TVINSERTSTRUCT tv = { 0 };
    tv.hParent = NULL;
    tv.hInsertAfter = TVI_LAST;
    tv.item.mask = TVIF_TEXT;

    char text[512];
    sprintf_s(text, "[СКЛАД] %s [%s]", g_warehouse->name.c_str(), g_warehouse->id.c_str());
    tv.item.pszText = text;
    HTREEITEM hRoot = TreeView_InsertItem(g_hTreeView, &tv);

    for (size_t i = 0; i < g_warehouse->containers.size(); i++) {
        AddContainerToTree(g_warehouse->containers[i], g_hTreeView, hRoot, 1);
    }

    for (size_t i = 0; i < g_warehouse->cargos.size(); i++) {
        TVINSERTSTRUCT tv2 = { 0 };
        tv2.hParent = hRoot;
        tv2.hInsertAfter = TVI_LAST;
        tv2.item.mask = TVIF_TEXT;
        sprintf_s(text, "    [ГРУЗ] %s | Вес: %.0f кг | Штрих-код: %s",
            g_warehouse->cargos[i]->name.c_str(),
            g_warehouse->cargos[i]->weight,
            g_warehouse->cargos[i]->barcode.c_str());
        tv2.item.pszText = text;
        TreeView_InsertItem(g_hTreeView, &tv2);
    }

    TreeView_Expand(g_hTreeView, hRoot, TVE_EXPAND);

    char status[512];
    sprintf_s(status, "Общий вес: %.0f кг | ПРОБЛЕМА: контейнеры и грузы хранятся в разных списках!",
        g_warehouse->getTotalWeight());
    SendMessageA(g_hStatusBar, SB_SETTEXT, 0, (LPARAM)status);
}

// ==================== ОКОННАЯ ПРОЦЕДУРА ====================
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
    case WM_CREATE: {
        INITCOMMONCONTROLSEX icex = { sizeof(INITCOMMONCONTROLSEX), ICC_TREEVIEW_CLASSES | ICC_BAR_CLASSES };
        InitCommonControlsEx(&icex);

        g_warehouse = CreateTestData();

        RECT rc;
        GetClientRect(hwnd, &rc);

        // Левая панель - кнопки (ширина 280)
        // Кнопка добавления груза
        CreateWindowA("BUTTON", "ДОБАВИТЬ ГРУЗ (ПРОБЛЕМА)", WS_CHILD | WS_VISIBLE,
            10, 10, 260, 35, hwnd, (HMENU)1, NULL, NULL);

        // Кнопка добавления контейнера
        CreateWindowA("BUTTON", "ДОБАВИТЬ КОНТЕЙНЕР (ПРОБЛЕМА)", WS_CHILD | WS_VISIBLE,
            10, 55, 260, 35, hwnd, (HMENU)2, NULL, NULL);

        // Метка поиска
        CreateWindowA("STATIC", "Поиск по штрих-коду:", WS_CHILD | WS_VISIBLE,
            10, 105, 120, 20, hwnd, NULL, NULL, NULL);

        // Поле ввода штрих-кода
        g_hEditSearch = CreateWindowA("EDIT", "BC-001", WS_CHILD | WS_VISIBLE | WS_BORDER,
            10, 128, 160, 25, hwnd, NULL, NULL, NULL);

        // Кнопка поиска
        CreateWindowA("BUTTON", "ПОИСК (НЕ РАБОТАЕТ)", WS_CHILD | WS_VISIBLE,
            175, 128, 260, 25, hwnd, (HMENU)3, NULL, NULL);

        // Дерево (ширина = ширина окна - 20 - ширина правой панели)
        g_hTreeView = CreateWindowA(WC_TREEVIEWA, "", WS_CHILD | WS_VISIBLE | WS_BORDER |
            TVS_HASLINES | TVS_HASBUTTONS | TVS_LINESATROOT,
            10, 170, rc.right - 340, rc.bottom - 200, hwnd, NULL, NULL, NULL);

        // Статус-бар
        g_hStatusBar = CreateWindowA(STATUSCLASSNAMEA, "", WS_CHILD | WS_VISIBLE | SBARS_SIZEGRIP,
            0, 0, 0, 0, hwnd, NULL, NULL, NULL);

        // Правая панель - пояснительный текст (ширина 320)
        CreateWindowA("STATIC",
            "=== ПРОБЛЕМЫ АРХИТЕКТУРЫ ===\n"
            "БЕЗ ПАТТЕРНА COMPOSITE\n\n"
            "1. Склад хранит ДВА разных списка:\n"
            "   - vector<Container*> containers\n"
            "   - vector<Cargo*> looseCargos\n\n"
            "2. Контейнер хранит ДВА разных списка:\n"
            "   - vector<Cargo*> cargos\n"
            "   - vector<Container*> subContainers\n\n"
            "3. НЕТ единого интерфейса для всех\n"
            "   элементов иерархии\n\n"
            "4. Поиск по штрих-коду требует\n"
            "   сложной рекурсии по ДВУМ спискам\n"
            "   (НЕ РЕАЛИЗОВАН в этой версии)\n\n"
            "5. Добавление нового типа\n"
            "   (например, Паллета) требует\n"
            "   изменения ВСЕХ существующих\n"
            "   классов!\n\n"
            "=== РЕАЛЬНОЕ ДОБАВЛЕНИЕ ===\n"
            "НЕ РЕАЛИЗОВАНО из-за\n"
            "сложности архитектуры!",
            WS_CHILD | WS_VISIBLE | WS_BORDER,
            rc.right - 330, 10, 320, 500, hwnd, NULL, NULL, NULL);

        RefreshTreeView();
        break;
    }

    case WM_SIZE: {
        RECT rc;
        GetClientRect(hwnd, &rc);

        // Обновляем статус-бар
        SendMessage(g_hStatusBar, WM_SIZE, 0, 0);

        RECT rcStatus;
        GetWindowRect(g_hStatusBar, &rcStatus);
        int statusHeight = rcStatus.bottom - rcStatus.top;

        // Обновляем размер дерева
        SetWindowPos(g_hTreeView, NULL, 10, 170,
            rc.right - 340, rc.bottom - statusHeight - 200, SWP_NOZORDER);

        // Обновляем позицию и размер правой панели
        HWND hRightPanel = GetWindow(hwnd, GW_CHILD);
        while (hRightPanel) {
            char className[256];
            GetClassNameA(hRightPanel, className, 255);
            // Находим статический текст (не статус-бар и не поле поиска)
            if (strcmp(className, "STATIC") == 0 &&
                hRightPanel != g_hStatusBar &&
                hRightPanel != g_hEditSearch) {
                SetWindowPos(hRightPanel, NULL, rc.right - 330, 10, 320, rc.bottom - 50, SWP_NOZORDER);
                break;
            }
            hRightPanel = GetNextWindow(hRightPanel, GW_HWNDNEXT);
        }
        break;
    }

    case WM_COMMAND: {
        if (LOWORD(wParam) == 1) {
            MessageBoxA(hwnd,
                "ПРОБЛЕМА АРХИТЕКТУРЫ\n\n"
                "В версии БЕЗ паттерна добавление груза требует:\n"
                "1. Определять, добавлять в контейнер или на склад\n"
                "2. Работать с двумя разными списками\n"
                "3. Писать разную логику для каждого типа\n\n"
                "С паттерном Composite это решается одной строкой:\n"
                "parent->add(std::make_shared<Cargo>(...));",
                "Проблема архитектуры", MB_ICONERROR);
        }
        else if (LOWORD(wParam) == 2) {
            MessageBoxA(hwnd,
                "ПРОБЛЕМА РАСШИРЯЕМОСТИ\n\n"
                "В версии БЕЗ паттерна добавление контейнера требует:\n"
                "1. Изменения класса Warehouse (новый список)\n"
                "2. Изменения всех методов для работы с новым типом\n"
                "3. Дублирования кода\n\n"
                "С паттерном Composite новый тип просто реализует\n"
                "интерфейс CargoComponent и готов к использованию.",
                "Проблема расширяемости", MB_ICONERROR);
        }
        else if (LOWORD(wParam) == 3) {
            char barcode[256];
            GetWindowTextA(g_hEditSearch, barcode, 255);
            char msg[512];
            sprintf_s(msg,
                "ПРОБЛЕМА ПОИСКА\n\n"
                "В версии БЕЗ паттерна поиск по штрих-коду требует:\n"
                "1. Рекурсивного обхода ДВУХ разных списков\n"
                "2. Раздельной логики для контейнеров и грузов\n"
                "3. Сложной реализации с множеством условий\n\n"
                "Искомый штрих-код: %s\n\n"
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
        "БЕЗ ПАТТЕРНА COMPOSITE - Демонстрация проблем архитектуры",
        WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT,
        1050, 600,  // Ширина 1050, Высота 600
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