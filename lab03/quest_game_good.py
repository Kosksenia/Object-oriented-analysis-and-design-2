import tkinter as tk
from tkinter import ttk, messagebox
import random
import math
from abc import ABC, abstractmethod

# ==================== ПАТТЕРН ЦЕПОЧКА ОБЯЗАННОСТЕЙ ====================

class QuestHandler(ABC):
    def __init__(self):
        self._next_handler = None
    
    def set_next(self, handler: 'QuestHandler') -> 'QuestHandler':
        self._next_handler = handler
        return handler
    
    def handle(self, player: 'Player', quest: 'Quest', boss: 'Boss' = None, game=None) -> tuple:
        if self._check_condition(player, quest, boss, game):
            self._on_success(player, quest, game)
            if self._next_handler:
                return self._next_handler.handle(player, quest, boss, game)
            return True, "✅ Все проверки пройдены! Квест завершён!"
        else:
            return False, self._get_failure_message(player, quest, boss, game)
    
    @abstractmethod
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        pass
    
    @abstractmethod
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        pass
    
    def _on_success(self, player: 'Player', quest: 'Quest', game):
        pass
    
    def get_requirement_text(self) -> str:
        return "Условие"
    
    def get_status_text(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        if self._check_condition(player, quest, boss, game):
            return "✅"
        return "❌"


class LevelHandler(QuestHandler):
    def __init__(self, required_level: int):
        super().__init__()
        self.required_level = required_level
    
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        return player.level >= self.required_level
    
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        return f"❌ Твой уровень слишком низок! Нужно достичь {self.required_level} уровня. (Твой уровень: {player.level})"
    
    def get_requirement_text(self) -> str:
        return f"🔰 Уровень {self.required_level}+"
    
    def get_status_text(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        return "✅" if player.level >= self.required_level else f"❌ ({player.level}/{self.required_level})"


class ItemHandler(QuestHandler):
    def __init__(self, required_item: str):
        super().__init__()
        self.required_item = required_item
    
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        return self.required_item in player.inventory
    
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        return f"❌ У тебя нет {self.required_item}! Выполни задание кузнеца."
    
    def get_requirement_text(self) -> str:
        return f"🗡️ {self.required_item}"
    
    def get_status_text(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        return "✅" if self.required_item in player.inventory else "❌"


class PreviousQuestHandler(QuestHandler):
    def __init__(self, required_quest_ids, quest_names=None):
        super().__init__()
        self.required_quest_ids = required_quest_ids if isinstance(required_quest_ids, list) else [required_quest_ids]
        self.quest_names = quest_names or self.required_quest_ids
    
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        for required_id in self.required_quest_ids:
            if required_id not in player.completed_quests:
                return False
        return True
    
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        failed_quests = []
        for i, required_id in enumerate(self.required_quest_ids):
            if required_id not in player.completed_quests:
                name = self.quest_names[i] if i < len(self.quest_names) else required_id
                failed_quests.append(name)
        return f"❌ Ты не прошёл квест(ы): {', '.join(failed_quests)}!"
    
    def get_requirement_text(self) -> str:
        if len(self.required_quest_ids) == 1:
            name = self.quest_names[0] if self.quest_names else self.required_quest_ids[0]
            return f"📜 Квест '{name}'"
        names = self.quest_names if self.quest_names else self.required_quest_ids
        return f"📜 Квесты: {', '.join(names)}"
    
    def get_status_text(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        all_completed = all(req_id in player.completed_quests for req_id in self.required_quest_ids)
        return "✅" if all_completed else "❌"


class ReputationHandler(QuestHandler):
    def __init__(self, required_reputation: int, faction: str = "Гильдия искателей"):
        super().__init__()
        self.required_reputation = required_reputation
        self.faction = faction
    
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        return player.reputation.get(self.faction, 0) >= self.required_reputation
    
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        current = player.reputation.get(self.faction, 0)
        return f"❌ {self.faction} не доверяет тебе! Нужно {self.required_reputation} очков репутации. (У тебя: {current})"
    
    def get_requirement_text(self) -> str:
        return f"🤝 Репутация {self.required_reputation}+"
    
    def get_status_text(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        current = player.reputation.get(self.faction, 0)
        return "✅" if current >= self.required_reputation else f"❌ ({current}/{self.required_reputation})"


class RewardHandler(QuestHandler):
    def __init__(self):
        super().__init__()
    
    def _check_condition(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> bool:
        return True
    
    def _get_failure_message(self, player: 'Player', quest: 'Quest', boss: 'Boss', game) -> str:
        return ""
    
    def _on_success(self, player: 'Player', quest: 'Quest', game):
        player.add_experience(quest.reward_exp)
        player.add_reputation(quest.required_reputation_faction, quest.reward_reputation)
        if quest.reward_item:
            player.add_item(quest.reward_item)
        player.gold += quest.reward_gold
        player.potions += quest.reward_potions
        player.completed_quests.append(quest.id)
        if game:
            game.show_floating_text(f"+{quest.reward_exp} опыта!", "#00b894")
            game.show_floating_text(f"+{quest.reward_gold} золота!", "#fdcb6e")
            if quest.reward_item:
                game.show_floating_text(f"+{quest.reward_item}!", "#0984e3")
    
    def get_requirement_text(self) -> str:
        return "🎁 Получение награды"


class QuestSystem:
    def __init__(self):
        self._chains = {}
    
    def build_chain_for_quest(self, quest: 'Quest') -> QuestHandler:
        if quest.id in self._chains:
            return self._chains[quest.id]
        
        handlers = []
        
        if quest.required_level > 0:
            handlers.append(LevelHandler(quest.required_level))
        
        if quest.required_item:
            handlers.append(ItemHandler(quest.required_item))
        
        if quest.required_quest or quest.required_quests:
            required_ids = quest.required_quests.copy()
            if quest.required_quest:
                required_ids.append(quest.required_quest)
            handlers.append(PreviousQuestHandler(required_ids))
        
        if quest.required_reputation > 0:
            handlers.append(ReputationHandler(quest.required_reputation, quest.required_reputation_faction))
        
        handlers.append(RewardHandler())
        
        for i in range(len(handlers) - 1):
            handlers[i].set_next(handlers[i + 1])
        
        self._chains[quest.id] = handlers[0] if handlers else None
        return self._chains[quest.id]
    
    def attempt_quest(self, player: 'Player', quest: 'Quest', boss: 'Boss' = None, game=None) -> tuple:
        chain = self.build_chain_for_quest(quest)
        if chain:
            return chain.handle(player, quest, boss, game)
        return True, "Квест выполнен!"
    
    def get_quest_requirements(self, quest: 'Quest') -> list:
        chain = self.build_chain_for_quest(quest)
        requirements = []
        
        current = chain
        while current and not isinstance(current, RewardHandler):
            requirements.append({
                'text': current.get_requirement_text(),
                'check': lambda p, q, b, c=current: c.get_status_text(p, q, b, None)
            })
            current = current._next_handler
        
        return requirements


# ==================== КЛАССЫ ИГРЫ ====================

class Player:
    def __init__(self, name: str):
        self.name = name
        self.level = 1
        self.hp = 100
        self.max_hp = 100
        self.damage = 15
        self.inventory = []
        self.completed_quests = []
        self.reputation = {"Гильдия искателей": 0}
        self.gold = 50
        self.potions = 2
        self.x = 400
        self.y = 300
        self.animation_frame = 0
        self.attack_animation = 0
    
    def heal(self, amount: int):
        self.hp = min(self.max_hp, self.hp + amount)
    
    def take_damage(self, amount: int):
        self.hp -= amount
        if self.hp < 0:
            self.hp = 0
    
    def is_alive(self) -> bool:
        return self.hp > 0
    
    def add_experience(self, exp: int):
        old_level = self.level
        if self.level < 10:
            self.level = min(10, self.level + 1)
            if self.level > old_level:
                self.max_hp = 100 + (self.level - 1) * 10
                self.damage = 15 + (self.level - 1) * 3
                return True
        return False
    
    def add_reputation(self, faction: str, amount: int):
        if faction in self.reputation:
            self.reputation[faction] = max(0, self.reputation[faction] + amount)
    
    def add_item(self, item: str):
        if item not in self.inventory:
            self.inventory.append(item)
    
    def remove_item(self, item: str):
        if item in self.inventory:
            self.inventory.remove(item)
    
    def use_potion(self) -> bool:
        if self.potions > 0:
            self.potions -= 1
            self.heal(50)
            return True
        return False


class Boss:
    def __init__(self):
        self.name = "Дракон Арканум"
        self.hp = 200
        self.max_hp = 200
        self.damage = 25
        self.is_defeated = False
    
    def take_damage(self, amount: int):
        self.hp -= amount
        if self.hp <= 0:
            self.hp = 0
            self.is_defeated = True
    
    def attack(self) -> int:
        return random.randint(self.damage - 10, self.damage + 5)


class NPC:
    def __init__(self, name: str, house_x: int, house_y: int, dialog: list, quest_id: str = None, 
                 face: str = "🧑", color: str = "#FFDAB9", is_villager: bool = True):
        self.name = name
        self.house_x = house_x  # Координаты дома
        self.house_y = house_y
        # NPC находится внутри дома
        self.x = house_x + 40  # Центр дома
        self.y = house_y + 30
        self.dialog = dialog
        self.quest_id = quest_id
        self.face = face
        self.color = color
        self.dialog_index = 0
        self.is_villager = is_villager
        self.hp = 100
        self.is_alive = True


class Enemy:
    def __init__(self, name: str, x: int, y: int, hp: int, damage: int, 
                 enemy_type: str, icon: str, color: str = "#e74c3c"):
        self.name = name
        self.x = x
        self.y = y
        self.hp = hp
        self.max_hp = hp
        self.damage = damage
        self.type = enemy_type
        self.icon = icon
        self.color = color
        self.attack_animation = 0


class Quest:
    def __init__(self, quest_id: str, name: str, description: str, 
                 required_level: int = 0, required_item: str = None,
                 required_quest: str = None, required_quests: list = None,
                 required_reputation: int = 0, required_reputation_faction: str = "Гильдия искателей",
                 reward_exp: int = 50, reward_gold: int = 100, reward_item: str = None,
                 reward_reputation: int = 10, reward_potions: int = 0):
        self.id = quest_id
        self.name = name
        self.description = description
        self.required_level = required_level
        self.required_item = required_item
        self.required_quest = required_quest
        self.required_quests = required_quests or []
        self.required_reputation = required_reputation
        self.required_reputation_faction = required_reputation_faction
        self.reward_exp = reward_exp
        self.reward_gold = reward_gold
        self.reward_item = reward_item
        self.reward_reputation = reward_reputation
        self.reward_potions = reward_potions


# ==================== RPG ИГРА ====================

class RPGGame:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("⚔️ DRAGON QUEST - RPG Adventure ⚔️")
        self.root.attributes('-fullscreen', True)
        self.root.configure(bg='#1a1a2e')
        self.root.bind('<Escape>', lambda e: self.toggle_fullscreen())
        
        self.colors = {
            'bg': '#1a1a2e', 'frame': '#16213e', 'text': '#e0e0e0',
            'success': '#00b894', 'error': '#d63031', 'warning': '#fdcb6e',
            'info': '#0984e3', 'hp': '#e74c3c', 'ground': '#2d5016', 'path': '#8B7355'
        }
        
        self.player = Player("Герой")
        self.quest_system = QuestSystem()
        
        # Дома и NPC внутри них
        self.buildings = [
            {"name": "🏠", "x": 300, "y": 220, "width": 80, "height": 60, "npc": None},
            {"name": "🔨", "x": 420, "y": 260, "width": 80, "height": 60, "npc": None},
            {"name": "🌾", "x": 200, "y": 300, "width": 80, "height": 60, "npc": None},
            {"name": "👑", "x": 330, "y": 310, "width": 80, "height": 60, "npc": None},
            {"name": "🏹", "x": 500, "y": 350, "width": 80, "height": 60, "npc": None},
        ]
        
        self.npcs = [
            NPC("Старейшина", 300, 220, 
                ["Приветствую, путник!", "Дракон Арканум терроризирует нашу деревню!", 
                 "Тебе нужно стать сильнее, чтобы победить его.",
                 "Начни с помощи жителям деревни!"],
                "quest_intro", "🧑", "#FFDAB9", True),
            NPC("Кузнец", 420, 260,
                ["Я могу выковать тебе Меч героя!", "Но мне нужны материалы: 5 железных руд.",
                 "Принеси руды из шахты на востоке.",
                 "После этого ты сможешь сразиться с драконом!"],
                "quest_sword", "⚒️", "#D2B48C", True),
            NPC("Фермер", 200, 300,
                ["Помоги мне! Волки нападают на моих овец!", "Убей 3 волков на северном поле.",
                 "Я щедро отблагодарю тебя.",
                 "Волки особенно активны по ночам!"],
                "quest_farmer", "👨", "#F4A460", True),
            NPC("Староста гильдии", 330, 310,
                ["Хочешь вступить в Гильдию искателей?", "Выполни несколько заданий для жителей.",
                 "Помоги фермеру и леснику, и я дам тебе рекомендацию.",
                 "Гильдия даст доступ к элитным квестам!"],
                "quest_reputation", "👑", "#FFD700", True),
            NPC("Лесник", 500, 350,
                ["Браконьеры вырубают лес!", "Накажи их, и я скажу доброе слово гильдии.",
                 "Найди браконьеров в западной части леса.",
                 "Также я могу дать тебе 3 зелья лечения, если попросишь!"],
                "quest_forester", "🏹", "#228B22", True),
        ]
        
        # Привязываем NPC к домам
        for i, npc in enumerate(self.npcs):
            self.buildings[i]["npc"] = npc
        
        self.enemies = []
        self.spawn_enemies()
        
        self.quests = {
            "quest_intro": Quest("quest_intro", "Приветствие старейшины", 
                                "Поговори со старейшиной", 
                                reward_exp=20, reward_gold=30),
            "quest_farmer": Quest("quest_farmer", "Помощь фермеру", 
                                 "Убей 3 волков", required_level=2,
                                 reward_exp=40, reward_gold=80, reward_reputation=15),
            "quest_forester": Quest("quest_forester", "Защита леса", 
                                   "Накажи браконьеров", required_level=3,
                                   reward_exp=50, reward_gold=100, reward_reputation=15),
            "quest_sword": Quest("quest_sword", "Меч героя", 
                                "Принеси 5 железных руд кузнецу", required_level=3, 
                                required_quest="quest_farmer",
                                reward_item="Меч героя", reward_exp=60, reward_gold=150),
            "quest_reputation": Quest("quest_reputation", "Вступление в гильдию", 
                                     "Получи рекомендации от фермера и лесника", 
                                     required_quests=["quest_farmer", "quest_forester"],
                                     reward_reputation=30, reward_exp=50),
            "quest_boss": Quest("quest_boss", "Победитель дракона", 
                               "Победи Дракона Арканума", required_level=5, 
                               required_item="Меч героя", required_reputation=50,
                               reward_exp=200, reward_gold=1000, 
                               reward_item="Амулет силы", reward_reputation=100, reward_potions=3),
        }
        
        for quest in self.quests.values():
            self.quest_system.build_chain_for_quest(quest)
        
        self.boss = None
        self.selected_npc = None
        self.animation_counter = 0
        self.farmer_wolves_killed = 0
        self.forester_bandits_killed = 0
        self.iron_ores = 0
        self.floating_texts = []
        self.particle_effects = []
        self.has_asked_forester_for_potions = False
        self.villager_attack_count = {}
        
        self.setup_ui()
        self.update_stats()
        self.animate()
    
    def toggle_fullscreen(self):
        is_fullscreen = self.root.attributes('-fullscreen')
        self.root.attributes('-fullscreen', not is_fullscreen)
    
    def spawn_enemies(self):
        self.enemies = [
            Enemy("Волк", 480, 220, 30, 8, "wolf", "🐺"),
            Enemy("Волк", 510, 240, 30, 8, "wolf", "🐺"),
            Enemy("Волк", 490, 260, 30, 8, "wolf", "🐺"),
            Enemy("Браконьер", 530, 300, 45, 12, "bandit", "🏹"),
            Enemy("Браконьер", 550, 320, 45, 12, "bandit", "🏹"),
        ]
        
        self.ore_nodes = [
            {"x": 560, "y": 200}, {"x": 580, "y": 210},
            {"x": 570, "y": 230}, {"x": 590, "y": 240}, {"x": 600, "y": 220},
        ]
    
    def setup_ui(self):
        main_container = tk.Frame(self.root, bg=self.colors['bg'])
        main_container.pack(fill=tk.BOTH, expand=True)
        
        self.canvas_container = tk.Canvas(main_container, bg=self.colors['bg'], highlightthickness=0)
        scrollbar = tk.Scrollbar(main_container, orient="vertical", command=self.canvas_container.yview)
        self.scrollable_frame = tk.Frame(self.canvas_container, bg=self.colors['bg'])
        
        self.scrollable_frame.bind("<Configure>", lambda e: self.canvas_container.configure(scrollregion=self.canvas_container.bbox("all")))
        self.canvas_container.create_window((0, 0), window=self.scrollable_frame, anchor="nw")
        self.canvas_container.configure(yscrollcommand=scrollbar.set)
        
        self.canvas_container.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")
        
        def on_mousewheel(event):
            self.canvas_container.yview_scroll(int(-1*(event.delta/120)), "units")
        self.canvas_container.bind("<MouseWheel>", on_mousewheel)
        
        # Заголовок
        title_frame = tk.Frame(self.scrollable_frame, bg=self.colors['bg'])
        title_frame.pack(pady=10)
        tk.Label(title_frame, text="⚔️ DRAGON QUEST - RPG ADVENTURE ⚔️", 
                font=('Arial', 20, 'bold'), fg=self.colors['success'], bg=self.colors['bg']).pack()
        tk.Label(title_frame, text="Используй паттерн Chain of Responsibility для проверки квестов | ESC - выход из полноэкранного режима",
                font=('Arial', 10), fg=self.colors['warning'], bg=self.colors['bg']).pack()
        
        main_frame = tk.Frame(self.scrollable_frame, bg=self.colors['bg'])
        main_frame.pack(fill=tk.BOTH, expand=True, padx=20, pady=10)
        
        # Левая панель - игровой мир
        game_frame = tk.Frame(main_frame, bg=self.colors['frame'], relief=tk.RAISED, bd=2)
        game_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        tk.Label(game_frame, text="🗺️ МИР ДЕРЕВНИ", font=('Arial', 14, 'bold'),
                fg=self.colors['warning'], bg=self.colors['frame']).pack(pady=5)
        
        self.canvas = tk.Canvas(game_frame, width=750, height=550, bg=self.colors['ground'], 
                                highlightthickness=2, highlightbackground=self.colors['warning'])
        self.canvas.pack(padx=10, pady=10)
        
        # Панель управления
        control_frame = tk.Frame(game_frame, bg=self.colors['frame'])
        control_frame.pack(pady=10)
        
        btn_style = {'font': ('Arial', 14, 'bold'), 'width': 4, 'height': 1,
                     'bg': self.colors['info'], 'fg': 'white', 'relief': tk.RAISED}
        
        tk.Button(control_frame, text="⬆️", command=lambda: self.move_player(0, -20), **btn_style).pack(side=tk.LEFT, padx=5)
        tk.Button(control_frame, text="⬅️", command=lambda: self.move_player(-20, 0), **btn_style).pack(side=tk.LEFT, padx=5)
        tk.Button(control_frame, text="⬇️", command=lambda: self.move_player(0, 20), **btn_style).pack(side=tk.LEFT, padx=5)
        tk.Button(control_frame, text="➡️", command=lambda: self.move_player(20, 0), **btn_style).pack(side=tk.LEFT, padx=5)
        tk.Button(control_frame, text="⚔️ АТАКА", command=self.attack_target, 
                 font=('Arial', 12, 'bold'), width=8, height=1,
                 bg=self.colors['error'], fg='white').pack(side=tk.LEFT, padx=10)
        tk.Button(control_frame, text="💬 ДИАЛОГ", command=self.interact,
                 font=('Arial', 12, 'bold'), width=8, height=1,
                 bg=self.colors['success'], fg='white').pack(side=tk.LEFT, padx=5)
        
        # Правая панель - информация
        right_frame = tk.Frame(main_frame, bg=self.colors['frame'], relief=tk.RAISED, bd=2, width=480)
        right_frame.pack(side=tk.RIGHT, fill=tk.BOTH, padx=(10, 0))
        right_frame.pack_propagate(False)
        
        # Создаем Canvas для прокрутки правой панели
        right_canvas = tk.Canvas(right_frame, bg=self.colors['frame'], highlightthickness=0)
        right_scrollbar = tk.Scrollbar(right_frame, orient="vertical", command=right_canvas.yview)
        right_inner_frame = tk.Frame(right_canvas, bg=self.colors['frame'])
        
        right_canvas.configure(yscrollcommand=right_scrollbar.set)
        right_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        right_canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        right_window = right_canvas.create_window((0, 0), window=right_inner_frame, anchor="nw")
        
        def configure_right_scroll(event):
            right_canvas.configure(scrollregion=right_canvas.bbox("all"))
            right_canvas.itemconfig(right_window, width=right_canvas.winfo_width())
        
        right_inner_frame.bind("<Configure>", configure_right_scroll)
        right_canvas.bind('<Configure>', lambda e: right_canvas.itemconfig(right_window, width=e.width))
        
        # Статистика игрока
        stats_frame = tk.LabelFrame(right_inner_frame, text="📊 СТАТИСТИКА ПЕРСОНАЖА", 
                                   font=('Arial', 11, 'bold'), fg=self.colors['success'],
                                   bg=self.colors['frame'], relief=tk.GROOVE)
        stats_frame.pack(fill=tk.X, padx=10, pady=5)
        
        self.stats_text = tk.Text(stats_frame, height=12, width=45, bg=self.colors['bg'], 
                                  fg=self.colors['text'], font=('Courier', 9), bd=0)
        self.stats_text.pack(padx=5, pady=5)
        
        # HP бар
        hp_frame = tk.Frame(right_inner_frame, bg=self.colors['frame'])
        hp_frame.pack(fill=tk.X, padx=10, pady=5)
        tk.Label(hp_frame, text="❤️ ЗДОРОВЬЕ:", font=('Arial', 10, 'bold'),
                fg=self.colors['hp'], bg=self.colors['frame']).pack()
        self.hp_bar = ttk.Progressbar(hp_frame, length=420, mode='determinate')
        self.hp_bar.pack(pady=5)
        
        # Квесты с требованиями
        quest_frame = tk.LabelFrame(right_inner_frame, text="📜 ДОСТУПНЫЕ КВЕСТЫ", 
                                    font=('Arial', 11, 'bold'), fg=self.colors['info'],
                                    bg=self.colors['frame'], relief=tk.GROOVE)
        quest_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)
        
        quest_canvas = tk.Canvas(quest_frame, bg=self.colors['frame'], highlightthickness=0, height=200)
        quest_scrollbar = tk.Scrollbar(quest_frame, orient="vertical", command=quest_canvas.yview)
        self.quest_inner_frame = tk.Frame(quest_canvas, bg=self.colors['frame'])
        
        quest_canvas.configure(yscrollcommand=quest_scrollbar.set)
        quest_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        quest_canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        quest_window = quest_canvas.create_window((0, 0), window=self.quest_inner_frame, anchor="nw")
        
        def configure_quest_scroll(event):
            quest_canvas.configure(scrollregion=quest_canvas.bbox("all"))
            quest_canvas.itemconfig(quest_window, width=quest_canvas.winfo_width())
        
        self.quest_inner_frame.bind("<Configure>", configure_quest_scroll)
        quest_canvas.bind('<Configure>', lambda e: quest_canvas.itemconfig(quest_window, width=e.width))
        
        self.quest_widgets = {}
        
        # Инвентарь
        inv_frame = tk.LabelFrame(right_inner_frame, text="🎒 ИНВЕНТАРЬ", 
                                  font=('Arial', 11, 'bold'), fg=self.colors['warning'],
                                  bg=self.colors['frame'], relief=tk.GROOVE)
        inv_frame.pack(fill=tk.X, padx=10, pady=5)
        
        self.inventory_listbox = tk.Listbox(inv_frame, height=4, bg=self.colors['bg'], 
                                            fg=self.colors['text'], font=('Arial', 10))
        self.inventory_listbox.pack(padx=5, pady=5, fill=tk.X)
        
        # Кнопки действий
        action_frame = tk.Frame(right_inner_frame, bg=self.colors['frame'])
        action_frame.pack(fill=tk.X, padx=10, pady=5)
        
        tk.Button(action_frame, text="💊 ИСПОЛЬЗОВАТЬ ЗЕЛЬЕ", command=self.use_potion,
                 bg=self.colors['success'], fg='white', font=('Arial', 10, 'bold'),
                 relief=tk.RAISED).pack(fill=tk.X, pady=2)
        tk.Button(action_frame, text="🐉 СРАЗИТЬСЯ С ДРАКОНОМ", command=self.start_boss_fight_window,
                 bg=self.colors['error'], fg='white', font=('Arial', 10, 'bold'),
                 relief=tk.RAISED).pack(fill=tk.X, pady=2)
        
        # Диалоговое окно
        dialog_frame = tk.Frame(right_inner_frame, bg=self.colors['bg'], relief=tk.RAISED, bd=2)
        dialog_frame.pack(fill=tk.X, padx=10, pady=5)
        self.dialog_label = tk.Label(dialog_frame, text="Подойди к домику и нажми ДИАЛОГ", 
                                     font=('Arial', 9), fg=self.colors['warning'], bg=self.colors['bg'],
                                     wraplength=420, justify=tk.LEFT)
        self.dialog_label.pack(pady=10, padx=10)
        
        # Статус-бар
        self.status_bar = tk.Label(self.root, text="✨ Добро пожаловать в Dragon Quest! ✨", 
                                   bd=1, relief=tk.SUNKEN, anchor=tk.W,
                                   bg=self.colors['frame'], fg=self.colors['text'])
        self.status_bar.pack(side=tk.BOTTOM, fill=tk.X)
        
        # Привязка клавиш
        self.root.bind('<Up>', lambda e: self.move_player(0, -20))
        self.root.bind('<Down>', lambda e: self.move_player(0, 20))
        self.root.bind('<Left>', lambda e: self.move_player(-20, 0))
        self.root.bind('<Right>', lambda e: self.move_player(20, 0))
        self.root.bind('<space>', lambda e: self.interact())
        self.root.bind('a', lambda e: self.attack_target())
        self.root.bind('h', lambda e: self.use_potion())
    
    def update_quest_list(self):
        for widget in self.quest_inner_frame.winfo_children():
            widget.destroy()
        
        self.quest_widgets.clear()
        
        available_quests = []
        
        if "quest_farmer" not in self.player.completed_quests and self.player.level >= 2:
            available_quests.append(("quest_farmer", "👨‍🌾 Помощь фермеру"))
        
        if "quest_forester" not in self.player.completed_quests and self.player.level >= 3:
            available_quests.append(("quest_forester", "🌲 Защита леса"))
        
        if "quest_sword" not in self.player.completed_quests and "Меч героя" not in self.player.inventory:
            if "quest_farmer" in self.player.completed_quests:
                available_quests.append(("quest_sword", f"🗡️ Меч героя ({self.iron_ores}/5 руды)"))
        
        if "quest_reputation" not in self.player.completed_quests:
            if "quest_farmer" in self.player.completed_quests and "quest_forester" in self.player.completed_quests:
                available_quests.append(("quest_reputation", "🤝 Вступление в гильдию"))
        
        if "quest_boss" not in self.player.completed_quests:
            if self.player.level >= 5 and "Меч героя" in self.player.inventory and self.player.reputation["Гильдия искателей"] >= 50:
                available_quests.append(("quest_boss", "🐉 Победитель дракона"))
        
        for quest_id, display_name in available_quests:
            quest = self.quests[quest_id]
            requirements = self.quest_system.get_quest_requirements(quest)
            
            quest_frame = tk.Frame(self.quest_inner_frame, bg=self.colors['frame'], relief=tk.GROOVE, bd=1)
            quest_frame.pack(fill=tk.X, padx=5, pady=3)
            
            name_label = tk.Label(quest_frame, text=display_name, font=('Arial', 10, 'bold'),
                                 fg=self.colors['warning'], bg=self.colors['frame'], anchor=tk.W)
            name_label.pack(fill=tk.X, padx=5, pady=2)
            
            desc_label = tk.Label(quest_frame, text=f"📝 {quest.description}", font=('Arial', 8),
                                 fg=self.colors['text'], bg=self.colors['frame'], anchor=tk.W)
            desc_label.pack(fill=tk.X, padx=5)
            
            req_frame = tk.Frame(quest_frame, bg=self.colors['frame'])
            req_frame.pack(fill=tk.X, padx=5, pady=2)
            
            for req in requirements:
                status = req['check'](self.player, quest, self.boss)
                req_label = tk.Label(req_frame, text=f"{status} {req['text']}", font=('Arial', 8),
                                    fg=self.colors['info'] if "✅" in status else self.colors['error'],
                                    bg=self.colors['frame'])
                req_label.pack(side=tk.LEFT, padx=(0, 10))
            
            reward_text = f"🏆 Награда: +{quest.reward_exp} опыта, +{quest.reward_gold} золота"
            if quest.reward_item:
                reward_text += f", +{quest.reward_item}"
            if quest.reward_potions > 0:
                reward_text += f", +{quest.reward_potions} зелий"
            
            reward_label = tk.Label(quest_frame, text=reward_text, font=('Arial', 7),
                                   fg=self.colors['success'], bg=self.colors['frame'], anchor=tk.W)
            reward_label.pack(fill=tk.X, padx=5, pady=2)
            
            self.quest_widgets[quest_id] = quest_frame
    
    def move_player(self, dx, dy):
        new_x = self.player.x + dx
        new_y = self.player.y + dy
        if 50 <= new_x <= 700 and 50 <= new_y <= 500:
            self.player.x = new_x
            self.player.y = new_y
            self.player.animation_frame = (self.player.animation_frame + 1) % 8
            self.draw_world()
            self.check_npc_proximity()
    
    def check_npc_proximity(self):
        for npc in self.npcs:
            if npc.is_alive:
                # Проверяем расстояние до центра дома (где находится NPC)
                distance = math.sqrt((self.player.x - npc.x)**2 + (self.player.y - npc.y)**2)
                if distance < 50:
                    self.dialog_label.config(text=f"📢 {npc.name}: Нажми ПРОБЕЛ или кнопку ДИАЛОГ чтобы поговорить")
                    self.selected_npc = npc
                    return
        self.dialog_label.config(text="")
        self.selected_npc = None
    
    def attack_target(self):
        # Сначала проверяем, есть ли рядом NPC для атаки
        if self.selected_npc and self.selected_npc.is_villager and self.selected_npc.is_alive:
            distance = math.sqrt((self.player.x - self.selected_npc.x)**2 + (self.player.y - self.selected_npc.y)**2)
            if distance < 50:
                # Урон по жителю
                damage = random.randint(15, 25)
                self.selected_npc.hp -= damage
                self.show_floating_text(f"-{damage}", self.colors['error'], self.selected_npc.x, self.selected_npc.y)
                
                # Увеличиваем счетчик атак на этого жителя
                if self.selected_npc.name not in self.villager_attack_count:
                    self.villager_attack_count[self.selected_npc.name] = 0
                self.villager_attack_count[self.selected_npc.name] += 1
                
                # Уменьшаем репутацию гильдии
                self.player.add_reputation("Гильдия искателей", -10)
                self.status_bar.config(text=f"😡 Ты атаковал {self.selected_npc.name}! Репутация гильдии -10!")
                self.show_floating_text("-10 репутации!", self.colors['error'])
                
                # Проверяем, умер ли житель
                if self.selected_npc.hp <= 0 or self.villager_attack_count[self.selected_npc.name] >= 5:
                    self.selected_npc.is_alive = False
                    self.selected_npc.hp = 0
                    self.status_bar.config(text=f"💀 {self.selected_npc.name} умер от твоих ударов!")
                    self.show_floating_text("Житель умер!", self.colors['error'])
                    
                    # Проверяем, жив ли хоть один житель
                    any_alive = any(npc.is_alive and npc.is_villager for npc in self.npcs)
                    if not any_alive:
                        messagebox.showerror("ИГРА ОКОНЧЕНА", 
                                            "Ты убил всех жителей деревни!\n"
                                            "Тебя выгнали из деревни.\n"
                                            "Игра окончена!")
                        self.root.quit()
                        return
                
                self.update_stats()
                self.draw_world()
                return
        
        # Если нет живого NPC рядом, атакуем врагов
        closest_enemy = None
        min_distance = 60
        
        for enemy in self.enemies:
            if enemy.hp > 0:
                distance = math.sqrt((self.player.x - enemy.x)**2 + (self.player.y - enemy.y)**2)
                if distance < min_distance:
                    min_distance = distance
                    closest_enemy = enemy
        
        if closest_enemy:
            self.player.attack_animation = 5
            damage = random.randint(self.player.damage - 5, self.player.damage + 5)
            closest_enemy.hp -= damage
            self.show_floating_text(f"-{damage}", self.colors['error'], closest_enemy.x, closest_enemy.y)
            
            if closest_enemy.hp <= 0:
                self.show_floating_text(f"Победа!", self.colors['success'])
                self.enemies.remove(closest_enemy)
                self.particle_effects.append({'x': closest_enemy.x, 'y': closest_enemy.y, 'life': 15})
                
                if closest_enemy.type == "wolf":
                    self.farmer_wolves_killed += 1
                    if self.farmer_wolves_killed >= 3 and "quest_farmer" not in self.player.completed_quests:
                        quest = self.quests["quest_farmer"]
                        self.quest_system.attempt_quest(self.player, quest, self.boss, self)
                        self.show_floating_text("Квест выполнен!", "#00b894")
                elif closest_enemy.type == "bandit":
                    self.forester_bandits_killed += 1
                    if self.forester_bandits_killed >= 2 and "quest_forester" not in self.player.completed_quests:
                        quest = self.quests["quest_forester"]
                        self.quest_system.attempt_quest(self.player, quest, self.boss, self)
                        self.show_floating_text("Квест выполнен!", "#00b894")
                
                self.update_stats()
            else:
                enemy_damage = random.randint(closest_enemy.damage - 5, closest_enemy.damage)
                self.player.take_damage(enemy_damage)
                self.show_floating_text(f"-{enemy_damage}", self.colors['hp'])
                self.status_bar.config(text=f"💥 {closest_enemy.name} атакует! -{enemy_damage} HP")
                
                if not self.player.is_alive():
                    self.game_over("defeat")
                    return
            
            self.update_stats()
            self.draw_world()
        else:
            self.status_bar.config(text="Рядом нет врагов!")
    
    def interact(self):
        if self.selected_npc and self.selected_npc.is_alive:
            self.show_dialog(self.selected_npc)
        else:
            for ore in self.ore_nodes:
                distance = math.sqrt((self.player.x - ore["x"])**2 + (self.player.y - ore["y"])**2)
                if distance < 40:
                    self.mine_ore(ore)
                    return
    
    def mine_ore(self, ore):
        self.iron_ores += 1
        self.ore_nodes.remove(ore)
        self.show_floating_text("+1 Руда!", "#FFD700")
        self.particle_effects.append({'x': ore["x"], 'y': ore["y"], 'life': 10})
        self.status_bar.config(text=f"⛏️ Ты добыл железную руду! Всего: {self.iron_ores}/5")
        self.draw_world()
        self.update_stats()
        
        if self.iron_ores >= 5 and "Меч героя" not in self.player.inventory:
            self.status_bar.config(text="✨ У тебя достаточно руды! Вернись к кузнецу за мечом! ✨")
            self.show_floating_text("Вернись к кузнецу!", "#00b894")
    
    def show_dialog(self, npc):
        # Лесник может дать зелья
        if npc.name == "Лесник" and not self.has_asked_forester_for_potions and "quest_forester" in self.player.completed_quests:
            choice = messagebox.askyesno("Просьба к леснику", 
                                        "Лесник: 'Ты помог мне с браконьерами. Хочешь получить 3 зелья лечения?'")
            if choice:
                self.player.potions += 3
                self.has_asked_forester_for_potions = True
                self.status_bar.config(text="🌿 Лесник дал тебе 3 зелья лечения!")
                self.show_floating_text("+3 зелья!", "#00b894")
                self.update_stats()
                self.dialog_label.config(text="Лесник: 'Возьми эти зелья, они тебе пригодятся в битве с драконом!'")
                return
        
        # Квест на меч
        if npc.quest_id == "quest_sword" and self.iron_ores >= 5 and "Меч героя" not in self.player.inventory:
            self.player.add_item("Меч героя")
            self.status_bar.config(text="🗡️ Кузнец выковал тебе Меч героя! Теперь ты сильнее! 🗡️")
            self.dialog_label.config(text="Кузнец: 'Вот твой меч! Теперь ты готов к битве с драконом!'")
            self.show_floating_text("Получен Меч героя!", "#fdcb6e")
            self.update_stats()
            self.draw_world()
            return
        
        # Квест на репутацию
        if npc.quest_id == "quest_reputation" and "quest_reputation" not in self.player.completed_quests:
            quest = self.quests["quest_reputation"]
            success, message = self.quest_system.attempt_quest(self.player, quest, self.boss, self)
            if success:
                self.status_bar.config(text="🤝 Староста гильдии принял тебя! Репутация +30")
                self.dialog_label.config(text="Староста: 'Добро пожаловать в Гильдию, искатель приключений!'")
                self.show_floating_text("Вступление в гильдию!", "#00b894")
            else:
                self.dialog_label.config(text=f"Староста: {message}")
            self.update_stats()
            return
        
        # Обычный диалог
        npc.dialog_index = (npc.dialog_index + 1) % len(npc.dialog)
        self.dialog_label.config(text=f"{npc.name}: {npc.dialog[npc.dialog_index]}")
        
        # Квест на приветствие
        if npc.quest_id == "quest_intro" and "quest_intro" not in self.player.completed_quests:
            quest = self.quests["quest_intro"]
            success, message = self.quest_system.attempt_quest(self.player, quest, self.boss, self)
            self.status_bar.config(text="✅ Квест 'Приветствие старейшины' выполнен! +20 опыта, +30 золота")
            self.show_floating_text("Квест выполнен!", "#00b894")
            self.update_stats()
    
    def use_potion(self):
        if self.player.use_potion():
            self.status_bar.config(text=f"💊 Ты использовал зелье! +50 HP (Осталось: {self.player.potions})")
            self.show_floating_text("+50 HP", self.colors['success'])
            self.update_stats()
            self.particle_effects.append({'x': self.player.x, 'y': self.player.y, 'life': 10})
        else:
            self.status_bar.config(text="❌ У тебя нет зелий! Попроси у лесника или выполни квесты!")
    
    def show_floating_text(self, text, color, x=None, y=None):
        if x is None:
            x = self.player.x
        if y is None:
            y = self.player.y - 30
        self.floating_texts.append({'text': text, 'x': x, 'y': y, 'life': 30, 'color': color})
    
    def update_stats(self):
        self.stats_text.config(state=tk.NORMAL)
        self.stats_text.delete(1.0, tk.END)
        
        stats = f"""
👤 {self.player.name}
━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔰 УРОВЕНЬ: {self.player.level}
❤️  HP: {self.player.hp}/{self.player.max_hp}
⚔️ УРОН: {self.player.damage}
💰 ЗОЛОТО: {self.player.gold}
💊 ЗЕЛЬЯ: {self.player.potions}

━━━━━━━━━━━━━━━━━━━━━━━━━━━
🤝 РЕПУТАЦИЯ:

  🏛️ Гильдия искателей: {self.player.reputation['Гильдия искателей']}

━━━━━━━━━━━━━━━━━━━━━━━━━━━
📜 ВЫПОЛНЕННЫЕ КВЕСТЫ:
        """
        
        if self.player.completed_quests:
            for q in self.player.completed_quests:
                quest_name = self.quests.get(q, Quest(q, q, "")).name
                stats += f"\n  ✅ {quest_name}"
        else:
            stats += "\n  • (нет выполненных квестов)"
        
        self.stats_text.insert(tk.END, stats)
        self.stats_text.config(state=tk.DISABLED)
        
        hp_percent = (self.player.hp / self.player.max_hp) * 100
        self.hp_bar['value'] = hp_percent
        
        self.inventory_listbox.delete(0, tk.END)
        if self.player.inventory:
            for item in self.player.inventory:
                self.inventory_listbox.insert(tk.END, f"• {item}")
        else:
            self.inventory_listbox.insert(tk.END, "• (пусто)")
        
        self.update_quest_list()
    
    def draw_world(self):
        self.canvas.delete("all")
        
        # Рисуем траву
        for i in range(0, 750, 50):
            for j in range(0, 550, 50):
                color = self.colors['ground'] if (i + j) % 100 == 0 else '#2a4a12'
                self.canvas.create_rectangle(i, j, i+50, j+50, fill=color, outline='')
        
        # Рисуем тропинки
        path_points = [(100, 275), (200, 275), (300, 275), (400, 275), (500, 275), (600, 275)]
        for i in range(len(path_points)-1):
            self.canvas.create_line(path_points[i][0], path_points[i][1], 
                                   path_points[i+1][0], path_points[i+1][1],
                                   width=35, fill=self.colors['path'], capstyle=tk.ROUND)
        
        # Рисуем дома и NPC внутри них
        for building in self.buildings:
            x, y = building["x"], building["y"]
            w, h = building["width"], building["height"]
            icon = building["name"]
            npc = building["npc"]
            
            # Стены дома
            self.canvas.create_rectangle(x, y, x+w, y+h, fill='#8B4513', outline='#5C2E00', width=2)
            # Крыша
            self.canvas.create_polygon(x-5, y, x+w//2, y-20, x+w+5, y, fill='#A0522D', outline='#5C2E00')
            # Окно
            self.canvas.create_rectangle(x+w//2-10, y+15, x+w//2+10, y+35, fill='#FFE4B5', outline='#5C2E00')
            # Иконка дома (без подписи)
            self.canvas.create_text(x+w//2, y-8, text=icon, font=('Arial', 14))
            
            # Если NPC жив, рисуем его внутри дома
            if npc and npc.is_alive:
                # Лицо NPC (без иконки, только лицо)
                self.canvas.create_oval(npc.x-10, npc.y-10, npc.x+10, npc.y+10, fill=npc.color, outline='#8B4513', width=2)
                # Глаза
                self.canvas.create_oval(npc.x-5, npc.y-5, npc.x-2, npc.y-2, fill='#000000')
                self.canvas.create_oval(npc.x+2, npc.y-5, npc.x+5, npc.y-2, fill='#000000')
                # Улыбка
                self.canvas.create_arc(npc.x-5, npc.y-1, npc.x+5, npc.y+5, start=0, extent=-180, style=tk.ARC, outline='#000000')
                # Подпись имени под домом
                self.canvas.create_text(x+w//2, y+h+12, text=npc.name, fill='white', font=('Arial', 9, 'bold'))
                
                # HP бар жителя
                hp_percent = (npc.hp / 100) * 100
                self.canvas.create_rectangle(x+10, y-5, x+w-10, y, fill='#333333', outline='')
                self.canvas.create_rectangle(x+10, y-5, x+10 + (w-20) * (hp_percent/100), y, fill='#00b894', outline='')
                
                # Диалоговое облачко если рядом
                if self.selected_npc == npc:
                    self.canvas.create_text(npc.x, npc.y-18, text="💬", font=('Arial', 14))
            elif npc and not npc.is_alive:
                # Мертвый NPC
                self.canvas.create_text(npc.x, npc.y, text="💀", font=('Arial', 16))
                self.canvas.create_text(x+w//2, y+h+12, text=npc.name, fill='gray', font=('Arial', 9, 'bold'))
        
        # Рисуем деревья
        tree_positions = [(120, 120), (150, 100), (180, 130), (600, 400), (630, 420), (660, 390)]
        for x, y in tree_positions:
            self.canvas.create_rectangle(x-8, y, x+8, y+30, fill='#5C2E00')
            self.canvas.create_oval(x-20, y-25, x+20, y, fill='#228B22', outline='#1a6b1a')
        
        # Рисуем руду
        for ore in self.ore_nodes:
            self.canvas.create_text(ore["x"], ore["y"], text="⛏️", font=('Arial', 28))
            self.canvas.create_text(ore["x"], ore["y"]-18, text="Руда", fill='#FFD700', font=('Arial', 8, 'bold'))
            if self.animation_counter % 30 < 15:
                self.canvas.create_oval(ore["x"]-15, ore["y"]-15, ore["x"]+15, ore["y"]+15, outline='#FFD700', width=1, dash=(2, 2))
        
        # Рисуем врагов
        for enemy in self.enemies:
            if enemy.attack_animation > 0:
                self.canvas.create_text(enemy.x, enemy.y, text="💥", font=('Arial', 20))
                enemy.attack_animation -= 1
            
            self.canvas.create_text(enemy.x, enemy.y, text=enemy.icon, font=('Arial', 28))
            hp_percent = (enemy.hp / enemy.max_hp) * 100
            self.canvas.create_rectangle(enemy.x-25, enemy.y-25, enemy.x+25, enemy.y-20, fill='#333333', outline='')
            self.canvas.create_rectangle(enemy.x-25, enemy.y-25, enemy.x-25 + 50 * (hp_percent/100), enemy.y-20, fill=enemy.color, outline='')
            self.canvas.create_text(enemy.x, enemy.y-30, text=enemy.name, fill='white', font=('Arial', 8, 'bold'))
        
        # Рисуем игрока
        player_icon = "⚔️" if self.player.attack_animation > 0 else "🧙"
        if self.player.attack_animation > 0:
            self.player.attack_animation -= 1
        self.canvas.create_text(self.player.x, self.player.y, text=player_icon, font=('Arial', 32))
        
        if self.player.animation_frame % 2 == 0:
            self.canvas.create_oval(self.player.x-15, self.player.y+10, self.player.x+15, self.player.y+20, fill='#666666', outline='')
        
        # Рисуем всплывающие тексты
        for text in self.floating_texts[:]:
            self.canvas.create_text(text['x'], text['y'], text=text['text'], fill=text['color'], font=('Arial', 10, 'bold'))
            text['y'] -= 1
            text['life'] -= 1
            if text['life'] <= 0:
                self.floating_texts.remove(text)
        
        # Рисуем частицы
        for particle in self.particle_effects[:]:
            self.canvas.create_oval(particle['x']-3, particle['y']-3, particle['x']+3, particle['y']+3, fill='#FFD700', outline='')
            particle['life'] -= 1
            if particle['life'] <= 0:
                self.particle_effects.remove(particle)
        
        # Границы карты
        self.canvas.create_rectangle(30, 30, 720, 520, outline='#8B4513', width=4)
        self.animation_counter += 1
    
    def animate(self):
        self.draw_world()
        self.root.after(50, self.animate)
    
    def start_boss_fight_window(self):
        boss_quest = self.quests["quest_boss"]
        success, message = self.quest_system.attempt_quest(self.player, boss_quest, None, self)
        
        if not success:
            messagebox.showwarning("Доступ запрещён", message)
            return
        
        self.boss = Boss()
        self.show_boss_battle_window()
    
    def show_boss_battle_window(self):
        battle_window = tk.Toplevel(self.root)
        battle_window.title("⚔️ БИТВА С ДРАКОНОМ АРКАНУМОМ ⚔️")
        battle_window.geometry("700x900")
        battle_window.configure(bg=self.colors['bg'])
        battle_window.transient(self.root)
        battle_window.grab_set()
        battle_window.resizable(False, False)
        
        battle_canvas_container = tk.Canvas(battle_window, bg=self.colors['bg'], highlightthickness=0)
        battle_scrollbar = tk.Scrollbar(battle_window, orient="vertical", command=battle_canvas_container.yview)
        battle_scrollable_frame = tk.Frame(battle_canvas_container, bg=self.colors['bg'])
        
        battle_scrollable_frame.bind(
            "<Configure>",
            lambda e: battle_canvas_container.configure(scrollregion=battle_canvas_container.bbox("all"))
        )
        
        battle_canvas_container.create_window((0, 0), window=battle_scrollable_frame, anchor="nw")
        battle_canvas_container.configure(yscrollcommand=battle_scrollbar.set)
        
        battle_canvas_container.pack(side="left", fill="both", expand=True)
        battle_scrollbar.pack(side="right", fill="y")
        
        def on_battle_mousewheel(event):
            battle_canvas_container.yview_scroll(int(-1*(event.delta/120)), "units")
        battle_canvas_container.bind("<MouseWheel>", on_battle_mousewheel)
        
        main_battle_frame = battle_scrollable_frame
        
        title_frame = tk.Frame(main_battle_frame, bg=self.colors['bg'])
        title_frame.pack(pady=10)
        tk.Label(title_frame, text="🐉 ДРАКОН АРКАНУМ 🐉", font=('Arial', 20, 'bold'),
                fg=self.colors['warning'], bg=self.colors['bg']).pack()
        tk.Label(title_frame, text="⚔️ ФИНАЛЬНАЯ БИТВА ⚔️", font=('Arial', 14, 'bold'),
                fg=self.colors['error'], bg=self.colors['bg']).pack()
        
        self.battle_canvas = tk.Canvas(main_battle_frame, width=600, height=250, 
                                       bg=self.colors['bg'], highlightthickness=2,
                                       highlightbackground=self.colors['warning'])
        self.battle_canvas.pack(pady=10, padx=10)
        
        self.boss_canvas_id = self.battle_canvas.create_text(300, 80, text="🐉", font=('Arial', 72))
        self.player_canvas_id = self.battle_canvas.create_text(300, 170, text="🧙", font=('Arial', 56))
        
        hp_frame = tk.Frame(main_battle_frame, bg=self.colors['bg'])
        hp_frame.pack(pady=10, padx=20, fill=tk.X)
        
        boss_frame = tk.LabelFrame(hp_frame, text="🐉 ДРАКОН", font=('Arial', 12, 'bold'),
                                   fg=self.colors['hp'], bg=self.colors['bg'])
        boss_frame.pack(fill=tk.X, pady=5)
        self.boss_hp_bar = ttk.Progressbar(boss_frame, length=600, mode='determinate')
        self.boss_hp_bar.pack(pady=5, padx=10)
        self.boss_hp_label = tk.Label(boss_frame, text="", font=('Arial', 11), 
                                      fg=self.colors['text'], bg=self.colors['bg'])
        self.boss_hp_label.pack(pady=2)
        
        player_frame = tk.LabelFrame(hp_frame, text="👤 ГЕРОЙ", font=('Arial', 12, 'bold'),
                                     fg=self.colors['success'], bg=self.colors['bg'])
        player_frame.pack(fill=tk.X, pady=5)
        self.battle_hp_bar = ttk.Progressbar(player_frame, length=600, mode='determinate')
        self.battle_hp_bar.pack(pady=5, padx=10)
        self.battle_hp_label = tk.Label(player_frame, text="", font=('Arial', 11),
                                        fg=self.colors['text'], bg=self.colors['bg'])
        self.battle_hp_label.pack(pady=2)
        
        log_frame = tk.LabelFrame(main_battle_frame, text="📜 ЛОГ БИТВЫ", 
                                  font=('Arial', 11, 'bold'), fg=self.colors['info'],
                                  bg=self.colors['frame'])
        log_frame.pack(pady=10, padx=20, fill=tk.BOTH, expand=True)
        
        self.battle_log = tk.Text(log_frame, height=12, width=70, bg='#0f0f1a', 
                                  fg=self.colors['warning'], font=('Consolas', 10),
                                  wrap=tk.WORD)
        scrollbar = tk.Scrollbar(log_frame, orient="vertical", command=self.battle_log.yview)
        self.battle_log.configure(yscrollcommand=scrollbar.set)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        self.battle_log.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        btn_frame = tk.Frame(main_battle_frame, bg=self.colors['bg'])
        btn_frame.pack(pady=30)
        
        attack_btn = tk.Button(btn_frame, text="⚔️ АТАКОВАТЬ ⚔️", 
                              command=lambda: self.boss_attack(battle_window),
                              bg=self.colors['error'], fg='white', 
                              font=('Arial', 18, 'bold'), 
                              width=14, height=2, relief=tk.RAISED)
        attack_btn.pack(side=tk.LEFT, padx=25)
        
        heal_btn = tk.Button(btn_frame, text="💊 ЛЕЧЕНИЕ 💊", 
                            command=lambda: self.boss_heal(battle_window),
                            bg=self.colors['success'], fg='white', 
                            font=('Arial', 18, 'bold'), 
                            width=14, height=2, relief=tk.RAISED)
        heal_btn.pack(side=tk.LEFT, padx=25)
        
        info_frame = tk.Frame(main_battle_frame, bg=self.colors['bg'])
        info_frame.pack(pady=15)
        
        self.battle_potion_label = tk.Label(info_frame, text=f"💊 Зелий: {self.player.potions}",
                                           font=('Arial', 13), fg=self.colors['warning'], 
                                           bg=self.colors['bg'])
        self.battle_potion_label.pack(side=tk.LEFT, padx=40)
        
        self.battle_damage_label = tk.Label(info_frame, text=f"⚔️ Урон: {self.player.damage}",
                                           font=('Arial', 13), fg=self.colors['info'], 
                                           bg=self.colors['bg'])
        self.battle_damage_label.pack(side=tk.LEFT, padx=40)
        
        hint_label = tk.Label(main_battle_frame, text="💡 Если кнопки не видны, прокрутите окно вниз колесиком мыши",
                             font=('Arial', 9), fg=self.colors['warning'], bg=self.colors['bg'])
        hint_label.pack(pady=5)
        
        self.update_boss_battle_ui()
        
        self.battle_log.insert(tk.END, "=" * 60 + "\n")
        self.battle_log.insert(tk.END, "⚔️ БИТВА НАЧАЛАСЬ! ⚔️\n")
        self.battle_log.insert(tk.END, "=" * 60 + "\n\n")
        self.battle_log.insert(tk.END, f"🐉 Дракон: {self.boss.hp}/{self.boss.max_hp} HP\n")
        self.battle_log.insert(tk.END, f"👤 Ты: {self.player.hp}/{self.player.max_hp} HP\n")
        self.battle_log.insert(tk.END, f"⚔️ Твой урон: {self.player.damage}\n")
        self.battle_log.insert(tk.END, "-" * 60 + "\n")
        self.battle_log.insert(tk.END, "Нажми АТАКОВАТЬ чтобы нанести удар!\n")
        self.battle_log.see(tk.END)
    
    def update_boss_battle_ui(self):
        if self.boss:
            boss_percent = (self.boss.hp / self.boss.max_hp) * 100
            self.boss_hp_bar['value'] = boss_percent
            self.boss_hp_label.config(text=f"Дракон: {self.boss.hp}/{self.boss.max_hp} HP")
        
        player_percent = (self.player.hp / self.player.max_hp) * 100
        self.battle_hp_bar['value'] = player_percent
        self.battle_hp_label.config(text=f"Герой: {self.player.hp}/{self.player.max_hp} HP")
        
        if hasattr(self, 'battle_potion_label'):
            self.battle_potion_label.config(text=f"💊 Зелий: {self.player.potions}")
            self.battle_damage_label.config(text=f"⚔️ Урон: {self.player.damage}")
    
    def boss_attack(self, battle_window):
        if self.boss.is_defeated:
            return
        
        self.battle_canvas.itemconfig(self.player_canvas_id, text="⚔️")
        battle_window.after(200, lambda: self.battle_canvas.itemconfig(self.player_canvas_id, text="🧙"))
        
        damage = random.randint(self.player.damage - 5, self.player.damage + 10)
        self.boss.take_damage(damage)
        self.battle_log.insert(tk.END, f"🗡️ Ты нанёс {damage} урона! (Осталось: {self.boss.hp})\n")
        self.battle_log.see(tk.END)
        
        if self.boss.is_defeated:
            self.battle_log.insert(tk.END, "\n" + "=" * 60 + "\n")
            self.battle_log.insert(tk.END, "🎉 ПОБЕДА! Ты победил Дракона Арканума! 🎉\n")
            self.battle_log.insert(tk.END, "=" * 60 + "\n\n")
            self.battle_log.insert(tk.END, "🏆 ПОЛУЧЕНА НАГРАДА:\n")
            self.battle_log.insert(tk.END, "   ⚡ +200 опыта\n")
            self.battle_log.insert(tk.END, "   💰 +1000 золота\n")
            self.battle_log.insert(tk.END, "   🗡️ +Амулет силы\n")
            self.battle_log.insert(tk.END, "   💊 +3 зелья\n")
            self.battle_log.insert(tk.END, "   🤝 +100 репутации\n")
            
            boss_quest = self.quests["quest_boss"]
            self.quest_system.attempt_quest(self.player, boss_quest, self.boss, self)
            self.update_stats()
            self.battle_canvas.itemconfig(self.boss_canvas_id, text="💀")
            
            self.game_over("victory", battle_window)
            return
        
        battle_window.after(500, lambda: self.battle_canvas.itemconfig(self.boss_canvas_id, text="🔥"))
        battle_window.after(700, lambda: self.battle_canvas.itemconfig(self.boss_canvas_id, text="🐉"))
        
        boss_damage = self.boss.attack()
        self.player.take_damage(boss_damage)
        self.battle_log.insert(tk.END, f"🐉 Дракон атакует! -{boss_damage} HP (Осталось: {self.player.hp})\n")
        self.battle_log.insert(tk.END, "-" * 40 + "\n")
        self.battle_log.see(tk.END)
        
        self.update_boss_battle_ui()
        
        if not self.player.is_alive():
            self.battle_log.insert(tk.END, "\n" + "=" * 60 + "\n")
            self.battle_log.insert(tk.END, "💀 ТЫ ПОГИБ В БИТВЕ... 💀\n")
            self.battle_log.insert(tk.END, "Дракон оказался слишком силён!\n")
            self.battle_log.insert(tk.END, "Попробуй снова, накопив больше здоровья и зелий!\n")
            self.battle_log.insert(tk.END, "=" * 60 + "\n")
            self.game_over("defeat", battle_window)
    
    def boss_heal(self, battle_window):
        if self.boss.is_defeated:
            return
        
        if self.player.use_potion():
            self.battle_log.insert(tk.END, f"💊 Ты использовал зелье! +50 HP (Осталось зелий: {self.player.potions})\n")
            self.update_boss_battle_ui()
            
            self.battle_canvas.itemconfig(self.player_canvas_id, text="💊")
            battle_window.after(200, lambda: self.battle_canvas.itemconfig(self.player_canvas_id, text="🧙"))
            
            battle_window.after(500, lambda: self.battle_canvas.itemconfig(self.boss_canvas_id, text="🔥"))
            battle_window.after(700, lambda: self.battle_canvas.itemconfig(self.boss_canvas_id, text="🐉"))
            
            boss_damage = self.boss.attack()
            self.player.take_damage(boss_damage)
            self.battle_log.insert(tk.END, f"🐉 Дракон атакует в ответ! -{boss_damage} HP\n")
            self.battle_log.insert(tk.END, "-" * 40 + "\n")
            self.battle_log.see(tk.END)
            self.update_boss_battle_ui()
            
            if not self.player.is_alive():
                self.battle_log.insert(tk.END, "\n" + "=" * 60 + "\n")
                self.battle_log.insert(tk.END, "💀 ТЫ ПОГИБ В БИТВЕ... 💀\n")
                self.battle_log.insert(tk.END, "Не хватило здоровья!\n")
                self.battle_log.insert(tk.END, "=" * 60 + "\n")
                self.game_over("defeat", battle_window)
        else:
            self.battle_log.insert(tk.END, "❌ У тебя нет зелий! Попроси у лесника или выполни квесты!\n")
            self.battle_log.see(tk.END)
    
    def game_over(self, result, battle_window=None):
        if battle_window:
            battle_window.destroy()
        
        if result == "victory":
            messagebox.showinfo("ПОБЕДА! 🎉", 
                               "Поздравляем! Ты победил Дракона Арканума!\n\n"
                               "Ты спас деревню от ужасного дракона и стал легендарным героем!\n"
                               "Твоё имя будет вписано в историю золотыми буквами!\n\n"
                               "Спасибо за игру!")
            self.root.quit()
        else:
            answer = messagebox.askyesno("ПОРАЖЕНИЕ 💀", 
                                        "Ты погиб в битве...\n\n"
                                        "Дракон оказался слишком силён.\n"
                                        "Хочешь попробовать снова?")
            if answer:
                self.restart_game()
            else:
                self.root.quit()
    
    def restart_game(self):
        self.root.destroy()
        new_game = RPGGame()
        new_game.run()
    
    def run(self):
        self.draw_world()
        self.root.mainloop()


if __name__ == "__main__":
    game = RPGGame()
    game.run()