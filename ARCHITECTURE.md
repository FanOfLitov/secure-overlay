# Secure Overlay Architecture

## 1. Назначение системы

Secure Overlay представляет собой децентрализованную overlay-сеть, в которой узлы самостоятельно выполняют:

- обнаружение других узлов;
- маршрутизацию по NodeID;
- хранение распределённых данных;
- проверку идентичности;
- построение защищённых соединений;
- построение многоузловых туннелей;
- восстановление после отдельных отказов.

Центральный каталог узлов не используется.

## 2. Общая архитектура

```text
                       Secure Overlay Node

+---------------------------------------------------------+
|                  Application Services                   |
+---------------------------------------------------------+
|                   Tunnel Management                     |
|                                                         |
| TunnelClient                                            |
| ResilientTunnelClient                                   |
| TunnelService                                           |
| TunnelStateStore                                        |
+---------------------------------------------------------+
|                    DHT / Discovery                      |
|                                                         |
| RoutingTable                                            |
| KBucket                                                 |
| IterativeNodeLookup                                     |
| BootstrapService                                        |
| DhtStorageService                                       |
| ReplicationService                                      |
| DhtStore                                                |
+---------------------------------------------------------+
|                 Identity / Security                     |
|                                                         |
| IdentityService                                         |
| NodeIdentity                                            |
| TLS Context                                             |
| TLS Peer Identity                                       |
| ReplayGuard                                             |
+---------------------------------------------------------+
|                  RPC / Protocol                         |
|                                                         |
| DhtRpcClient                                            |
| TunnelRpcClient                                         |
| FrameEncoder                                            |
| FrameDecoder                                            |
| JsonPayloadCodec                                        |
+---------------------------------------------------------+
|                         TCP                             |
+---------------------------------------------------------+
```

## 3. Identity

Каждый узел имеет долговременную пару ключей Ed25519.

```text
Ed25519 KeyPair
       |
       v
publicKey.getEncoded()
       |
       v
SHA-256
       |
       v
256-bit NodeID
```

Таким образом NodeID криптографически связан с публичным ключом узла.

Закрытый ключ сохраняется локально и не передаётся другим участникам сети.

## 4. Transport

Основной transport — TCP.

TCP представляет поток байтов и не сохраняет границы сообщений, поэтому Secure Overlay использует собственный frame protocol.

Каждый входящий TCP socket передаётся в:

```text
TcpServer
   |
   v
ConnectionHandler
```

Обработчики выполняются через `ExecutorService`.

## 5. Frame protocol

Каждый frame содержит:

```text
version
type
flags
requestId
payloadLength
payload
```

Размер фиксированного header:

```text
24 bytes
```

Максимальный payload:

```text
65536 bytes
```

`FrameDecoder` сначала валидирует длину, и только после этого выделяет буфер под payload.

Это предотвращает выделение произвольного объёма памяти из значения, полученного от удалённой стороны.

## 6. DHT

DHT использует идеи Kademlia.

### NodeID

```text
256 bits
```

### Distance

Расстояние между узлами:

```text
distance(A, B) = A XOR B
```

Чем меньше значение XOR, тем ближе NodeID.

### Routing table

Routing table состоит из k-bucket.

Основная экспериментальная конфигурация:

```text
k = 3
```

Контакты внутри bucket поддерживаются в LRU-порядке.

### Контакт

Контакт содержит:

```text
NodeID
host
port
lastSeen
```

## 7. FIND_NODE

Поиск выполняется итеративно.

```text
initiator
   |
   +----> candidate A
   |
   +----> candidate B
   |
   +----> candidate C
              |
              v
          new contacts
              |
              v
        next iteration
```

Используется:

```text
ALPHA = 3
```

Поэтому до трёх RPC могут выполняться параллельно.

Рабочий узел не имеет скрытого глобального списка всех NodeID.

## 8. Bootstrap

Экспериментальный стенд поддерживает:

```text
ring
star
```

Bootstrap-граф используется только как начальная схема знакомства узлов.

После получения контактов DHT lookup выполняется через routing tables.

## 9. DHT storage

Ключ прикладной записи преобразуется:

```text
String key
    |
    v
SHA-256
    |
    v
DHT NodeID
```

Запись содержит:

```text
key
value
version
expiresAtEpochMs
ownerPublicKey
signature
```

## 10. Подпись записи

DHT record подписывается Ed25519.

Проверяются:

- подпись;
- TTL;
- владелец;
- version.

Новая запись с меньшей version отклоняется.

Другой public key не может переписать существующую запись владельца.

## 11. Replication

Используется:

```text
R = 3
```

Запись размещается на нескольких ближайших узлах.

Если один holder становится недоступен, `FIND_VALUE` продолжает поиск среди других реплик.

## 12. TLS

Межузловые защищённые соединения используют:

```text
TLS 1.3
```

Сертификаты строятся на основе identity каждого узла.

Сервер включает:

```text
needClientAuth = true
```

Таким образом используется mutual TLS.

NodeID удалённого участника может быть вычислен непосредственно из public key TLS-сертификата.

## 13. Replay protection

Каждый Frame имеет:

```text
UUID requestId
```

`ReplayGuard` хранит недавно обработанные идентификаторы.

Повторно полученный requestId отклоняется в пределах replay window.

Это является прикладной replay/deduplication защитой поверх TLS.

## 14. Multi-hop tunnel

Маршрут выглядит следующим образом:

```text
Initiator
   |
   v
Relay 1
   |
   v
Relay 2
   |
   v
Destination
```

В текущей реализации требуется минимум:

```text
2 relay + destination
```

## 15. End-to-end tunnel encryption

При построении туннеля initiator создаёт ephemeral X25519 key pair.

Destination также генерирует ephemeral X25519 key pair.

```text
Initiator X25519 private
            +
Destination X25519 public
            |
            v
      shared secret
```

На destination выполняется зеркальная операция.

Полученный secret используется для derivation AES key.

```text
X25519
  |
  v
HKDF-SHA256
  |
  v
AES-256 key
```

Payload шифруется:

```text
AES/GCM/NoPadding
```

`tunnelId` используется как additional authenticated data.

## 16. Что знает relay

Relay хранит только:

```text
tunnelId -> nextHop
```

Relay не получает:

```text
AES endpoint key
plaintext
```

Поэтому обработка выглядит так:

```text
encrypted payload
       |
       v
    Relay 1
       |
       | no decrypt
       v
    Relay 2
       |
       | no decrypt
       v
 Destination
       |
       v
    decrypt
```

## 17. Tunnel recovery

Для отказоустойчивости используется primary и backup route.

```text
PRIMARY

initiator
   |
relay1
   |
relay2   X failure
   |
destination
```

После ошибки:

```text
send()
  |
  v
failure detected
  |
  v
build backup tunnel
  |
  v
retry payload
  |
  v
destination
```

Пример backup route:

```text
relay3
  |
relay4
  |
destination
```

В эксперименте выполнялось пять независимых отказов relay.

## 18. Experimental subsystem

Экспериментальный слой находится отдельно от сетевого ядра.

Он может знать полный список экспериментальных NodeID, потому что является orchestrator, а не частью рабочего DHT lookup.

Стенд создаёт 12 независимых логических узлов:

```text
N = 12
```

и выполняет:

```text
RING normal
RING one-node-failed
STAR normal
STAR one-node-failed
```

## 19. Экспериментальные данные

Lookup metrics:

```text
success
latency_ms
rpc_count
iterations
```

Tunnel metrics:

```text
build_time_ms
end_to_end_latency_ms
recovery_time_ms
delivered
```

Результаты сохраняются в CSV.

## 20. Статистическая обработка

Для выборок вычисляются:

```text
N
mean
median
min
max
p95
```

p95 используется только для выборок не менее 20 наблюдений.

Для пяти recovery experiments p95 не рассчитывается.

## 21. Зафиксированные результаты

DHT:

```text
120 lookups
120 successful
100% success
```

Tunnel:

```text
35 observations
35 delivered
100% delivery
```

Контрольный tunnel experiment:

```text
build mean       90.77 ms
build median     89.00 ms
build p95        126 ms

E2E mean         32.63 ms
E2E median       30.50 ms
E2E p95          65 ms

recovery mean    34.60 ms
recovery median  36.00 ms
recovery min     24 ms
recovery max     44 ms
```

## 22. PCAP validation

TLS traffic записывается через `tcpdump`.

Контрольные строки:

```text
SECRET-VALUE
tls-key
```

не обнаруживаются командой `strings` внутри pcap.

Следовательно прикладные значения не наблюдаются в захваченном TCP-трафике в открытом виде.

Это не является заявлением о полной анонимности системы; эксперимент подтверждает именно отсутствие контрольного plaintext в данном захвате.

## 23. Ограничения

Текущая реализация не утверждает:

- устойчивость к Sybil;
- полную анонимность;
- production-ready NAT traversal;
- Byzantine fault tolerance;
- глобальную Internet-scale scalability.

Все экспериментальные результаты относятся к локальному тестовому стенду.

## 24. Расширяемость

Архитектура позволяет отдельно добавить:

```text
REST API
React dashboard
Kafka metrics/events
file transfer
netem scenarios
Docker deployment
```

Эти компоненты не должны изменять базовый DHT transport.

В частности Kafka целесообразно применять только для observability/event processing:

```text
NODE_JOINED
LOOKUP_COMPLETED
RECORD_STORED
TUNNEL_BUILT
TUNNEL_REBUILT
EXPERIMENT_COMPLETED
```

а не как транспорт для P2P RPC.