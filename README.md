# Tinkoff ID

`Tinkoff ID` - SDK для авторизации пользователей Android приложений с помощью аккаунта Тинькофф.

## Установка

Для начала работы добавьте следующую строчку в `build.gradle`:

```groovy
implementation "ru.tinkoff.core.tinkoffauth:tinkoff-id:${version}"
```

## Требования к приложению

Для работы SDK необходимо следующее:

+ Зарегистрированный идентификатор авторизуемого приложения (`client_id`)
+ Зарегистрированная авторизуемым приложением [App Link](https://developer.android.com/training/app-links), которая будет использоваться для возврата в приложение после авторизации
+ Tinkoff ID SDK поддерживает авторизацию через Тинькофф только на api >= 23, на более ранних версиях получить авторизационные данные не будет возможности.

## Интеграция

Все необходимое взаимодействие в библиотеке идет только через класс `TinkoffIdAuth`.

1. Необходимо добавить в лэйаут кнопку `TinkoffIdSignInButton.kt`.
   Пример по ее настройке доступен в `PartnerActivity.kt` и `activity_partner.xml`. Размер кнопки настраивается через атрибут
   `app:tinkoff_id_size`, который может иметь значение `compact` или `standard` (по умолчанию). Подробное описание
   различий находится в комментарии в `TinkoffIdSignInButton.kt`.
   Вид кнопок:
   
   compact
   ![compact.png](imgs/tinkoff_id_sign_in_button/compact.png)
   
   standard
   ![standard.png](imgs/tinkoff_id_sign_in_button/standard.png)
   
2. Необходимо создать объект `TinkoffIdAuth(applicationContext, clientId)` - это основной класс для работы с библиотекой.
3. Проверить возможность авторизации пользователя через Тинькофф - `tinkoffIdAuth.isTinkoffAuthAvailable()`
4. Запустить партнерскую авторизацию, передав ваш App Link (по данному аплинку приложение группы Тинькофф вернется обратно после процесса авторизации)
```kotlin
    val intent = tinkoffIdAuth.createTinkoffAuthIntent(partnerUri)
    startActivity(intent)
```
5. После того как пользователь пройдет флоу авторизации в приложении Тинькофф произойдет переход в ваше приложение. В `intent.data` будет храниться информация по авторизации.
6. Вы можете проверить успешность авторизации методом
```kotlin
    // метод вернет статус SUCCESS или CANCELLED_BY_USER
    tinkoffIdAuth.getStatusCode(uriFromIntentData)
}
```
7. Если статус успешен, то можно вызвать `getTinkoffTokenPayload(uri).getResponse()`, необходимо выполнять `getResponse()`  на io потоке, удобными для вас средствами
```kotlin
    tinkoffIdAuth.getTinkoffTokenPayload(uriFromIntentData).getResponse()
```
8. После этого вы получите `TinkoffTokenPayload`, содержащий креденшиалы
9. Желательно реализовать безопасное хранение tinkoffTokenPayload.refreshToken в приложении, так как он необходим для перевыпуска токенов

### Перевыпуск авторизационных данных

Для перевыпуска accessToken необходимо использовать метод `tinkoffIdAuth.obtainTokenPayload(refreshToken)`

В него нужно передать refreshToken полученный ранее. Выполнять вызов  `getResponse()` необходимо не на ui потоке

### Отзыв авторизационных данных

Иногда может возникнуть ситуация когда полученные авторизационные данные более не нужны. Например, при выходе, смене или отключении аккаунта пользователя в авторизованном приложении. В таком случае, приложению необходимо выполнить отзыв авторизационных данных с помощью методов:
```kotlin
    fun signOutByAccessToken(accessToken: String): TinkoffCall<Unit>
    fun signOutByRefreshToken(refreshToken: String): TinkoffCall<Unit>
```

### Структура TinkoffTokenPayload

В результате успешной авторизации приложение получает объект `TinkoffTokenPayload`, содержащий следующие свойства:

+ `accessToken` - токен для обращения к API Тинькофф
+ `refreshToken` - токен, необходимый для получения нового `accessToken`
+ `idToken` - идентификатор пользователя в формате JWT
+ `expiresIn` - время, через которое `accessToken` станет неактуальным и нужно будет получить новый с помощью `obtainTokenPayload(refreshToken)`

## Дополнительно

SDK поставляется с примером приложения, где можно посмотреть работу авторизации.