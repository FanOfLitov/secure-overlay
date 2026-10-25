# Secure Overlay Protocol

## 1. Назначение

Secure Overlay Protocol определяет формат сообщений между узлами overlay-сети.

Протокол работает поверх TCP.

Для защищённых соединений TCP оборачивается в TLS 1.3.

## 2. Frame

Каждое сообщение передаётся в виде Frame.

Структура:

```text
+---------+------+-------+----------------+----------------+---------+
| version | type | flags | requestId UUID | payload length | payload |
+---------+------+-------+----------------+----------------+---------+
```

Фиксированный header:

```text
24 bytes
```

## 3. Поля Frame

### version

Размер:

```text
1 byte
```

Текущая версия:

```text
1
```

### type

Размер:

```text
1 byte
```

Определяет назначение сообщения.

### flags

Размер:

```text
2 bytes
```

Зарезервировано для признаков и расширений протокола.

### requestId

Размер:

```text
16 bytes
```

UUID используется для:

- сопоставления request/response;
- replay detection;
- дедупликации.

### payloadLength

Размер:

```text
4 bytes
```

Network byte order:

```text
BIG_ENDIAN
```

### payload

Переменная длина.

Максимум:

```text
65536 bytes
```

## 4. Правила декодирования

Decoder обязан:

1. полностью считать header;
2. проверить `payloadLength`;
3. отклонить отрицательную длину;
4. отклонить значение выше `MAX_FRAME_PAYLOAD`;
5. только после проверки выделить payload buffer;
6. дочитать payload полностью;
7. отклонить неизвестный FrameType.

## 5. FrameType codes

Коды являются частью wire protocol и не должны изменяться без изменения версии протокола.

```text
1   PING
2   PONG

3   FIND_NODE_REQUEST
4   FIND_NODE_RESPONSE

5   STORE_REQUEST
6   STORE_RESPONSE

7   FIND_VALUE_REQUEST
8   FIND_VALUE_RESPONSE

9   TUNNEL_BUILD
10  TUNNEL_BUILD_OK
11  TUNNEL_BUILD_FAIL

12  TUNNEL_DATA
13  TUNNEL_ACK
14  TUNNEL_CLOSE

15  APP_MESSAGE
16  APP_ACK

17  ERROR
```

Стабильность кодов проверяется автоматическим `FrameTypeTest`.

## 6. PING

Request:

```text
PING
```

Response:

```text
PONG
```

Используется для проверки доступности узла.

## 7. FIND_NODE

### Request

```json
{
  "targetNodeId": "<hex NodeID>"
}
```

### Response

```json
{
  "contacts": [
    {
      "nodeId": "<hex>",
      "host": "127.0.0.1",
      "port": 9001
    }
  ]
}
```

Удалённый узел возвращает известные ему контакты, ближайшие к target по XOR distance.

## 8. STORE

### Request

Содержит подписанный DHT record:

```json
{
  "record": {
    "key": "example",
    "value": "value",
    "version": 1,
    "expiresAtEpochMs": 0,
    "ownerPublicKey": "...",
    "signature": "..."
  }
}
```

### Response

```json
{
  "stored": true,
  "reason": "STORED"
}
```

Возможные результаты:

```text
STORED
UPDATED
ALREADY_STORED

INVALID_SIGNATURE
EXPIRED
OWNER_MISMATCH
STALE_VERSION
VERSION_CONFLICT
NULL_RECORD
```

## 9. FIND_VALUE

### Request

```json
{
  "key": "example"
}
```

### Найденное значение

```json
{
  "found": true,
  "record": {
    "...": "..."
  },
  "contacts": []
}
```

### Значение не найдено локально

```json
{
  "found": false,
  "record": null,
  "contacts": [
    {
      "nodeId": "...",
      "host": "...",
      "port": 0
    }
  ]
}
```

Контакты используются для продолжения iterative lookup.

## 10. DHT record integrity

Подписываемые поля:

```text
key
value
version
expiresAtEpochMs
ownerPublicKey
```

Алгоритм:

```text
Ed25519
```

Изменение любого подписанного поля делает signature invalid.

## 11. TUNNEL_BUILD

Initiator генерирует:

```text
tunnelId
ephemeral X25519 key
```

Request содержит:

```json
{
  "tunnelId": "...",
  "initiatorEphemeralPublicKey": "...",
  "remainingRoute": []
}
```

Каждый relay:

1. извлекает первый `remainingRoute`;
2. сохраняет его как `nextHop`;
3. удаляет его из route;
4. пересылает запрос дальше.

## 12. TUNNEL_BUILD_OK

Destination генерирует свою ephemeral X25519 key pair.

Ответ:

```json
{
  "tunnelId": "...",
  "built": true,
  "destinationEphemeralPublicKey": "...",
  "reason": "BUILT"
}
```

После этого initiator и destination независимо получают одинаковый shared secret.

## 13. TUNNEL_BUILD_FAIL

Ошибка маршрута:

```text
TUNNEL_BUILD_FAIL
```

Пример reason:

```text
NEXT_HOP_FAILED
```

## 14. Tunnel key derivation

Алгоритм:

```text
X25519
   |
shared secret
   |
HKDF-SHA256
   |
AES-256 key
```

Контекст derivation включает:

```text
secure-overlay-tunnel:<tunnelId>
```

## 15. TUNNEL_DATA

Payload:

```json
{
  "tunnelId": "...",
  "encryptedPayload": {
    "nonce": "...",
    "ciphertext": "..."
  }
}
```

Шифрование:

```text
AES/GCM/NoPadding
```

Nonce:

```text
12 bytes
```

Authentication tag:

```text
128 bits
```

AAD:

```text
tunnelId
```

## 16. Relay processing

Relay получает:

```text
TUNNEL_DATA
```

и выполняет только:

```text
lookup nextHop by tunnelId
forward unchanged encryptedPayload
```

Relay не вызывает decrypt.

## 17. Destination processing

Destination получает:

```text
TUNNEL_DATA
```

Находит endpoint AES key по:

```text
tunnelId
```

После чего выполняет AES-GCM decrypt.

При успешной доставке возвращается:

```text
TUNNEL_ACK
```

## 18. TUNNEL_ACK

```json
{
  "tunnelId": "...",
  "delivered": true,
  "reason": "DELIVERED"
}
```

После recovery может использоваться:

```text
RECOVERED
```

## 19. TUNNEL_CLOSE

Содержит:

```text
tunnelId
```

Каждый relay пересылает close следующему hop и удаляет локальное tunnel state.

## 20. TLS transport

При TLS-конфигурации:

```text
Frame
  |
  v
TLS 1.3
  |
  v
TCP
```

Внешний наблюдатель TCP не должен видеть JSON/frame payload в открытом виде.

## 21. Mutual authentication

TLS server требует client certificate:

```text
needClientAuth = true
```

NodeID peer может быть рассчитан как:

```text
SHA-256(
    peerCertificate.publicKey.encoded
)
```

## 22. requestId validation

Response обязан использовать тот же:

```text
requestId
```

что и request.

RPC client проверяет соответствие.

Если request и response IDs различаются, response отклоняется.

## 23. Replay protection

На сервере используется окно недавно увиденных UUID.

```text
requestId
   |
   v
ReplayGuard
```

Первый request:

```text
accepted
```

Повтор:

```text
rejected
```

## 24. Error handling

Ошибки сетевого узла не должны приводить к нарушению структуры routing protocol.

В iterative lookup недоступный candidate помечается как failed, после чего поиск может продолжаться через другие contacts.

## 25. Версионирование

Текущая версия wire protocol:

```text
1
```

Любое несовместимое изменение:

- frame header;
- FrameType codes;
- field semantics;

должно сопровождаться изменением protocol version.