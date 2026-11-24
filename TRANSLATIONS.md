# NexFusion Translations Guide

NexFusion welcomes translations from the community! All translations are maintained directly in the GitHub repository.

---

## Supported Languages

| Language | Locale Code | Resource Folder | Status |
|---|---|---|---|
| English (Default) | `en` | `app/src/main/res/values/` | Base |
| Simplified Chinese (简体中文) | `zh-CN` | `app/src/main/res/values-zh-rCN/` | Complete |

---

## How to Add a New Language

1. **Fork the Repository**:
   Fork the `NexFusion` repository and create a new feature branch (e.g. `feat/translate-es` or `feat/translate-ru`).

2. **Create the Resource Directory**:
   Inside `app/src/main/res/`, create a new folder named `values-<bcp47_code>`:
   * French: `values-fr`
   * German: `values-de`
   * Spanish: `values-es`
   * Russian: `values-ru`
   * Traditional Chinese: `values-zh-rTW`
   * Japanese: `values-ja`
   * Korean: `values-ko`
   * Portuguese (Brazil): `values-pt-rBR`

3. **Copy and Translate `strings.xml`**:
   Copy `app/src/main/res/values/strings.xml` into your new folder and translate the text inside `<string>...</string>` tags.

4. **Register the Language**:
   Add the language code to:
   * `AppPreferenceNames.kt`: Add the constant to `SUPPORTED_LANGUAGES` and `normalizeLanguage(...)`.
   * `AppLocale.kt`: Add mapping to `localeFor(...)`.
   * `SettingsScreen.kt`: Add the display name in the language selector dialog.

5. **Submit a Pull Request**:
   Open a pull request describing the language added.

---

## Guidelines for Translators

* **Placeholders**: Keep formatting arguments (`%s`, `%d`, `%1$s`, `%2$d`, etc.) unchanged.
* **Special Characters**:
  * Escape apostrophes as `\'` (e.g. `Don\'t Patch`).
  * Escape literal double-quotes as `\"`.
  * Do not escape standard Chinese or punctuation marks (`“`, `”`, `’`, `、`, `。`).
* **Untranslated Brand Names**:
  * Keep `NexFusion`, `LSPosed`, `GitHub`, `Debug`, `Release`, `Miuix`, and `Material 3` unchanged.
