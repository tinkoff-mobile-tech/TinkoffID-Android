# Документ по миграции

## 1.0.6 -> 1.1.0

Переименованы методы класса `TinkoffIdAuth`:
- `createTinkoffAuthIntent(callbackUrl: Uri): Intent` -> `createTinkoffAppAuthIntent(callbackUrl: Uri): Intent`
- `isTinkoffAuthAvailable(): Boolean ` -> `isTinkoffAppAuthAvailable(): Boolean `
