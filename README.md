# XpGet

Плагин для Paper 1.21+ на Java 21, который позволяет конвертировать ваш опыт в бутылочки опыта при наличии пустых стеклянных бутылочек. 
Поддерживает как фиксированное количество, так и автоматический расчёт максимума.
Для более удобной конвертации есть GUI интерфейс.

## ⚙️ Команда

| Команда                     | Описание                                                          |
|-----------------------------|-------------------------------------------------------------------|
| `/xpget`                    | Открыть GUI меню для конвертации опыта                            |
| `/xpget [количество \|max]` | Конвертирует указанное количество опыта или максимально возможное |
| `/xpget reload`             | Перезагрузки конфига                                              |

### Примеры:
- `/xpget 10` — создать 10 бутылочек опыта (если хватает опыта и бутылочек)
- `/xpget max` — создать максимально возможное количество бутылочек

## 🧾 Права (Permissions)

| Право          | По умолчанию | Описание                                        |
|----------------|-------------|-------------------------------------------------|
| `xpget.use`    | ✅ true      | Разрешает использование команды `/xpget`        |
| `xpget.reload` | op          | Разрешает использование команды `/xpget reload` |

## ⚙️ Конфигурация

```yaml
# XPGet Configuration
exp-per-bottle: 7 # Сколько опыта нужно для заполнения одной бутылочки

# Можно добавлять свои предметы, доступные actions: [close], [cmd]
gui:
  size: 27
  title: "<gold>Конвертация опыта</gold>"

  divider:
    enabled: true
    material: "GRAY_STAINED_GLASS_PANE"
    name: "<gray>"

  close:
    slot: 22
    material: "BARRIER"
    name: "<red>Закрыть"
    actions:
      - "[close]"

  #  main-menu:
  #    slot: 0
  #    material: "STONE"
  #    name: "Main Menu"
  #    lore:
  #      - "<yellow>Click to go to main menu"
  #    actions:
  #      - "[close]"
  #      - "[cmd] menu"

  items:
    one:
      slot: 11
      material: "EXPERIENCE_BOTTLE"
      amount: 1
      name: "<green>1 бутылочка опыта"
      lore:
        - "<gray>Нажмите, чтобы конвертировать"
        - ""
        - "<yellow>Требуется:"
        - "<gray>• <white>{exp} опыта</white>"
        - "<gray>• <white>1 пустая бутылочка</white>"

    stack:
      slot: 13
      material: "EXPERIENCE_BOTTLE"
      amount: 64
      name: "<yellow>64 бутылочки опыта"
      lore:
        - "<gray>Нажмите, чтобы конвертировать"
        - ""
        - "<yellow>Требуется:"
        - "<gray>• <white>{exp} опыта</white>"
        - "<gray>• <white>64 пустых бутылочек</white>"

    max:
      slot: 15
      material: "ENCHANTED_BOOK"
      amount: 1
      name: "<gold>Максимум"
      lore:
        - "<gray>Нажмите, чтобы конвертировать"
        - "<gray>в максимально возможное число бутылочек"
        - ""
        - "<yellow>Будет потрачено:"
        - "<gray>• <white>{exp} опыта</white>"
        - "<gray>• <white>{max_bottles} пустых бутылочек</white>"

messages:
  player-only: "<red>Эта команда только для игроков!"
  no-permission: "<red>У вас нет прав для использования этой команды!"

  usage: |
    <gold>Использование:
    <yellow>/xpget [количество|max]
    <gold>Примеры:
    <yellow>/xpget 10 <gray>- конвертировать опыт в 10 бутылочек
    <yellow>/xpget max <gray>- конвертировать максимально возможное количество
    <gold>Примечание: Можно конвертировать любое количество бутылочек

  config-reloaded: "<green>Конфигурация успешно перезагружена!"

  invalid-amount: "<red>Неверное количество! Используйте число или 'max'"

  not-enough-exp: "<red>Недостаточно опыта! Нужно: <yellow>{required}</yellow> опыта, у вас: <yellow>{current}"
  not-enough-empty: "<red>Недостаточно пустых бутылочек! Нужно: <yellow>{needed}</yellow>, у вас: <yellow>{current}"
  not-enough-space: "<red>Недостаточно места в инвентаре! Доступно места для: <yellow>{available}</yellow> бутылочек"

  no-empty-bottles: "<red>У вас нет пустых бутылочек!"
  not-enough-exp-for-one: "<red>У вас недостаточно опыта для создания хотя бы одной бутылочки!"
  not-enough-space-even: "<red>Недостаточно места в инвентаре даже с учетом замены бутылочек!"

  success-specific: "<green>Успешно конвертировано <yellow>{amount}</yellow> бутылочек опыта!"
  success-max: "<green>Успешно конвертировано <yellow>{actual}</yellow> бутылочек опыта!"
```

## 📦 Установка

1. Скачайте **последний релиз** из раздела [Releases](../../releases)
2. Поместите его в папку /plugins
3. Перезапустите сервер
4. Настройте `config.yml` ри необходимости и используйте `/xpget reload`
5. Можете пользоваться: используйте `/xpget` в игре

---

## 📄 Лицензия

Плагин распространяется под лицензией MIT.
