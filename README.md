<div align="center">

<img src="assets/logo.png" width="128" alt="re:exeraless">

# re:exeraless

Android-клиент на базе exteraless.

[![Канал](https://img.shields.io/badge/Telegram-@reexteraless-2CA5E0?logo=telegram&logoColor=white)](https://t.me/reexteraless)

[⬇️ Скачать бета-версию 12.10.6](https://github.com/IzTrusheb/re-exteraless/releases/download/beta-fb3de866/exteraless-v12.10.6-beta-fb3de866.apk) · [Все релизы](https://github.com/IzTrusheb/re-exteraless/releases)

</div>

---

## Что это

re:exeraless — форк [exteraless](https://github.com/exteraless/exteraless).

Отдельный упор — **безопасность плагинов**. В exteraGram плагин не ограничен ни в чём;
здесь у него есть модель разрешений: плагин объявляет, что ему нужно, это
видно на экране установки, и получает он только заявленное.

Проект **в бете**: часть возможностей exteraGram ещё не перенесена. Если заметили, что
каких-то функций не хватает, — откройте issue или напишите в [канал](https://t.me/reexteraless?direct).

* Имя пакета: `re.exteraless.app`
* Что перенесено: экраны настроек, оформление и параметры чатов, иконпаки, полоса
  пилюль (Pill Stack), боковое меню, движок Python-плагинов

### Ссылки

* Канал: [@reexteraless](https://t.me/reexteraless)
* Исходники: [IzTrusheb/re-exteraless](https://github.com/IzTrusheb/re-exteraless)

### Сборка

1. Склонировать репозиторий вместе с подмодулями:

    ```bash
    git clone --recursive --shallow-submodules https://github.com/IzTrusheb/re-exteraless.git re-exteraless
    ```

    Если репозиторий уже склонирован без подмодулей:

    ```bash
    git submodule update --init --recursive --depth=1
    ```

2. Получить `TELEGRAM_APP_ID` и `TELEGRAM_APP_HASH` на [my.telegram.org](https://my.telegram.org/auth)
   и создать `local.properties` в корне проекта:

   ```properties
   TELEGRAM_APP_ID=<ваш_app_id>
   TELEGRAM_APP_HASH=<ваш_app_hash>
   ```

3. Для подписи APK положить свой `TMessagesProj/release.keystore` и дописать в `local.properties`:

   ```properties
   KEYSTORE_PASS=<пароль_хранилища>
   ALIAS_NAME=<имя_ключа>
   ALIAS_PASS=<пароль_ключа>
   ```

   Ключа в репозитории нет намеренно. Без него сборка не падает — APK подписывается
   отладочным ключом Android.

4. Для push-уведомлений положить свой `TMessagesProj/google-services.json`
   (Firebase, имя пакета `re.exteraless.app`).

5. Заменить метаданные проекта:

    - ключ Google Maps в записи `com.google.android.maps.v2.API_KEY` в `TMessagesProj/src/main/AndroidManifest.xml`;
    - `BaseRemoteHelper.CHANNEL_METADATA_ID` — числовой id вашего канала метаданных, без префикса `-100`.

6. Собрать: `./gradlew :TMessagesProj:assembleDebug` или открыть проект в Android Studio.

**Про ABI.** Release-сборка в GitHub Actions предназначена только для `arm64-v8a`.
Для Chaquopy используется Python 3.11. Переменная
`NATIVE_TARGET` задаёт цель: `arm64-v8a` (один ABI, быстрее), `universal` (оба),
`SKIP` (без нативной части — только Java и ресурсы).

### Сборка через GitHub Actions

Нужны два секрета репозитория:

* `LOCAL_PROPERTIES` — содержимое `local.properties` в base64:

  ```bash
  base64 -w0 local.properties
  ```

* `RELEASE_KEYSTORE` — файл ключа в base64:

  ```bash
  base64 -w0 TMessagesProj/release.keystore
  ```

Дальше запустить workflow **Release Build**. Готовый APK лежит в артефактах прогона.

### Авторы дизайна

Иконки и оформление, унаследованные от exteraGram, созданы его дизайнером —
[@the8055u](https://t.me/the8055u) и студией [@BlueprintDsgn](https://t.me/BlueprintDsgn).
Права на эти материалы принадлежат авторам.

### Благодарности

- [exteraless](https://github.com/exteraless/exteraless)

---

## English

### What this is

re:exeraless is a fork of [exteraless](https://github.com/exteraless/exteraless).

[Download beta 12.10.6](https://github.com/IzTrusheb/re-exteraless/releases/download/beta-fb3de866/exteraless-v12.10.6-beta-fb3de866.apk) · [All releases](https://github.com/IzTrusheb/re-exteraless/releases)

The special focus is **plugin security**. In exteraGram a plugin is not restricted in
any way; here it comes with a permission model and isolation: a plugin declares what it
needs, you see that on the install sheet, and it gets only what was declared.

The project is **in beta**: some exteraGram features have not been ported yet. If you
notice a missing feature, open an issue or write to the [channel](https://t.me/reexteraless?direct).

* Package name: `re.exteraless.app`

### Design credits

Icons and visual design inherited from exteraGram are the work of its designer,
[@the8055u](https://t.me/the8055u), and the [@BlueprintDsgn](https://t.me/BlueprintDsgn)
studio. Rights to those materials belong to their authors.

### Building

1. Clone with submodules:

    ```bash
    git clone --recursive --shallow-submodules https://github.com/IzTrusheb/re-exteraless.git re-exteraless
    ```

2. Get `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` from [my.telegram.org](https://my.telegram.org/auth)
   and put them into `local.properties` in the project root.

3. For release signing, drop your own `TMessagesProj/release.keystore` and add
   `KEYSTORE_PASS`, `ALIAS_NAME`, `ALIAS_PASS` to `local.properties`. No keystore is
   shipped with the repository; without one the build is signed with the Android debug key.

4. For push notifications, replace `TMessagesProj/google-services.json` with your own
   Firebase config for `re.exteraless.app`.

5. Build with `./gradlew :TMessagesProj:assembleDebug` or from Android Studio.

The GitHub Actions Release build targets `arm64-v8a` only and uses Python 3.11.
`NATIVE_TARGET` selects the target:
`arm64-v8a`, `universal` (both) or `SKIP` (no native part).

For CI, set two repository secrets — `LOCAL_PROPERTIES` and `RELEASE_KEYSTORE`, both
base64-encoded — and run the **Release Build** workflow. The APK ends up in the run
artifacts.
