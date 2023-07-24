# Документ по миграции

## 1.0.6 -> 1.1.0

В связи с добавлением альтернативного способа авторизации через веб Тинькофф с помощью WebView,
переработана логика метода `createTinkoffAuthIntent(callbackUrl: Uri): Intent`. Теперь внутри него, на основе значения
`isTinkoffAppAuthAvailable(): Boolean`, происходит создание Intent для открытия или приложения Тинькофф,
или `TinkoffWebViewAuthActivity` (для прохождения авторизации в вебе).

Рекомендуется использовать `createTinkoffAuthIntent(callbackUrl: Uri): Intent`, чтобы пользователю в любом случае была
доступна авторизации через Тинькофф:

**Before**:

```kotlin
if (tinkoffPartnerAuth.isTinkoffAuthAvailable()) { 
    val intent = tinkoffPartnerAuth.createTinkoffAuthIntent(callbackUrl)
    startActivity(intent)
} else {
    // The logic of disabling authorization via Tinkoff
}
```

**After**:

```kotlin
val intent = tinkoffPartnerAuth.createTinkoffAuthIntent(callbackUrl)
startActivity(intent)
```

Изменения в методах класса `TinkoffIdAuth`:
- `createTinkoffAuthIntent(callbackUrl: Uri): Intent` -> `createTinkoffAppAuthIntent(callbackUrl: Uri): Intent`
- `isTinkoffAuthAvailable(): Boolean` -> `isTinkoffAppAuthAvailable(): Boolean`
