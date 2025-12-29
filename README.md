## JEasyWarps

**Ядро:** Paper / Spigot  
**Версия:** 1.16.5 - 1.21.11  
**Java:** 16+  

### Описание

**JEasyWarps** — лёгкий и удобный плагин для создания варпов с простым графическим интерфейсом.  
Игроки могут создавать варпы с цветными многословными названиями, устанавливать описание, менять иконку и оценивать чужие варпы.  
Администраторы имеют полный контроль: перезагрузка, удаление и редактирование чужих варпов.

### Возможности

- GUI с полностью настраиваемым видом через config.yml
- Поддержка многословных и цветных названий варпов
- Кастомные иконки для каждого варпа (/wicon)
- Описание варпа с цветами и переносом строк
- Система оценок (1–5 звёзд)
- Подтверждение удаления варпа
- Лимиты варпов по группам (LuckPerms)
- Все GUI-элементы (фон, кнопки, флаги предметов) настраиваются в конфиге

### Команды

#### Команды игрока  
| Команда                           | Описание                                   | Право               |
|-----------------------------------|--------------------------------------------|---------------------|
| `/warps`                          | Открыть главное меню варпов                | `jeasywarps.player` |
| `/warp <название>`                | Телепорт на варп                           | `jeasywarps.player` |
| `/wset <название>`                | Создать варп на текущей позиции            | `jeasywarps.player` |
| `/wdel <название>`                | Удалить свой варп (с подтверждением в GUI) | `jeasywarps.player` |
| `/wrename <старое> <новое>`       | Переименовать свой варп                    | `jeasywarps.player` |
| `/wsetlore <название> <описание>` | Установить описание своему варпу           | `jeasywarps.player` |
| `/wdellore <название>`            | Удалить описание у своего варпа            | `jeasywarps.player` |
| `/wicon <название> <материал>`    | Изменить иконку своего варпа               | `jeasywarps.player` |
#### Админ-команды
| Команда                            | Описание                       | Право              |
|------------------------------------|--------------------------------|--------------------|
| `/jewreload`                       | Перезагрузить плагин           | `jeasywarps.admin` |
| `/jewdelwarp <название>`           | Удалить любой варп             | `jeasywarps.admin` |
| `/jewrelore <название> <описание>` | Изменить описание любого варпа | `jeasywarps.admin` |
| `/jewdellore <название>`           | Удалить описание любого варпа  | `jeasywarps.admin` |

### Права (Permissions)
- `jeasywarps.player` — доступ ко всем командам игрока (доступны по умолчанию)
- `jeasywarps.admin` — админ-функции (рекомендуется давать только доверенным)

### Установка
- Поместите `JEasyWarps.jar` в папку `plugins`
- Перезапустите сервер
- (Опционально) Установите `LuckPerms` для лимитов по группам
- Настройте `plugins/JEasyWarps/config.yml` под себя
- Выполните `/jewreload` для применения изменений

### Настройка
- Всё оформление GUI, сообщения, лимиты и флаги иконок-предметов настраиваются в `config.yml`
- После изменений используйте `/jewreload`
```yml
# ╔══════════════════════════════════════════════════════════════════╗
# ║                     JEasyWarps — config.yml                      ║
# ║               Полностью настраиваемый конфиг плагина             ║
# ╚══════════════════════════════════════════════════════════════════╝

# Максимальная длина описания варпа (в символах, без цветовых кодов)
lore-max-length: 150

# Лимиты варпов по группам (требуется LuckPerms)
warp-limits:
  default: 3
  vip: 6
  premium: 10
  elite: 15

# Сообщения плагина
messages:
  prefix: "&6[JEasyWarps] &r"
  no-permission: "&cУ вас нет прав на эту команду."
  warp-not-found: "&cВарп &e%warp% &cне найден."
  teleport-success: "&aТелепортация на варп &e%warp%&a!"
  warp-exists: "&cВарп с именем &e%warp% &cуже существует."
  warp-limit-reached: "&cВы достигли лимита в &e%limit% &cварпов."
  warp-created: "&aВарп &e%warp% &aуспешно создан!"
  not-your-warp: "&cЭто не ваш варп!"
  warp-deleted: "&cВарп &e%warp% &cудалён."
  warp-renamed: "&aВарп переименован: &e%old% &7→ &e%new%"
  warp-lore-set: "&aОписание варпа обновлено."
  warp-lore-deleted: "&aОписание варпа удалено."
  lore-too-long: "&cОписание слишком длинное! Максимум: &e%max% &cсимволов."
  cannot-rate-own: "&cВы не можете оценивать свой варп!"
  already-rated: "&cВы уже оценили этот варп."
  warp-rated: "&aВы поставили &e%stars%★ &aварпу &e%warp%"
  invalid-material: "&cМатериал &e%material% &cне найден или не является предметом!"
  warp-icon-set: "&aИконка варпа изменена на &e%material%"

# ──────────────────────────────────────────────────────────────────
# GUI — настройки интерфейсов
# ──────────────────────────────────────────────────────────────────

gui:
  main:
    title: "&8✦ Варпы ✦"
    size: 54
    warps-start-slot: 10
    filler:
      material: BLACK_STAINED_GLASS_PANE
      name: " "
    no-warps:
      material: BARRIER
      slot: 22
      name: "&cНет доступных варпов"
      lore:
        - "&7Создайте свой варп командой"
        - "&f/wset <название>"
    warp-item:
      material: END_PORTAL_FRAME
      name: "&a%warp%"
      lore:
        - "&7Владелец: &f%owner%"
        - "&7Мир: &f%world%"
        - "&7Рейтинг: &f%rating%"
        - ""
        - "%lore_lines%"
        - ""
        - "&eЛКМ &8— &7Телепорт"
        - "&eПКМ &8— &7Оценить"
      owner-only-lore:
        - "&eShift + ЛКМ &8— &7Удалить"
      hide-flags:
        - HIDE_ATTRIBUTES
        - HIDE_POTION_EFFECTS
    no-description: "&7Нет описания"

  rate:
    size: 27
    title: "&8Оценить варп: %warp%"
    filler:
      material: BLACK_STAINED_GLASS_PANE
      name: " "
    back-item:
      material: ARROW
      slot: 22
      name: "&c◄ Назад"
      lore:
        - "&7Вернуться в главное меню"
    stars:
      1:
        material: GOLD_NUGGET
        amount: 1
        slot: 11
        name: "&c★☆☆☆☆ &7— Ужасно"
      2:
        material: GOLD_NUGGET
        amount: 2
        slot: 12
        name: "&6★★☆☆☆ &7— Плохо"
      3:
        material: GOLD_NUGGET
        amount: 3
        slot: 13
        name: "&e★★★☆☆ &7— Нормально"
      4:
        material: GOLD_NUGGET
        amount: 4
        slot: 14
        name: "&a★★★★☆ &7— Хорошо"
      5:
        material: GOLD_NUGGET
        amount: 5
        slot: 15
        name: "&b★★★★★ &7— Отлично"

  delete-confirm:
    size: 27
    title: "&8Удалить варп: %warp%"
    filler:
      material: BLACK_STAINED_GLASS_PANE
      name: " "
    confirm:
      material: LIME_CONCRETE
      slot: 11
      name: "&a✔ Подтвердить удаление"
      lore:
        - "&7Нажмите, чтобы &lбезвозвратно"
        - "&7удалить варп &e%warp%"
    cancel:
      material: RED_CONCRETE
      slot: 15
      name: "&c✖ Отмена"
      lore:
        - "&7Вернуться в меню варпов"
```
#### Примеры
- `Многословное имя`: /wset Прикольный варп
- `Цветное имя`: /wset &aЗелёный &bдомик
- `Иконка`: /wicon Прикольный варп DIAMOND_BLOCK (можно выбрать из появляемого списка ID предметов)
- `Описание`: /wsetlore Прикольный варп &aТут реально прикольно! &eОтвечаю!!

<img width="574" height="712" alt="Знімок_20251229_231449" src="https://github.com/user-attachments/assets/e077a43d-7de3-46f8-9e7c-870ccf5630cc" />
<img width="714" height="552" alt="Знімок_20251229_231529" src="https://github.com/user-attachments/assets/08834f70-c4f1-4b27-95ce-a138a8c00a22" />
<img width="886" height="545" alt="Знімок_20251229_231549" src="https://github.com/user-attachments/assets/4f73a67e-1a55-4767-82f2-dd41c4060aab" />
<img width="801" height="703" alt="Знімок_20251229_231654" src="https://github.com/user-attachments/assets/52b2f0f6-4d20-41b0-b2b8-03d652adddad" />
