# Secure Overlay — User Guide

## 1. Назначение

Этот документ описывает практический запуск, проверку и экспериментальное использование проекта Secure Overlay.

Основная документация:

- [README.md](README.md) — общее описание проекта, технологии, команды запуска и результаты.
- [ARCHITECTURE.md](ARCHITECTURE.md) — архитектура узла, DHT, безопасность, туннели и экспериментальная подсистема.
- [PROTOCOL.md](PROTOCOL.md) — формат Frame, типы сообщений и сетевой протокол.
- [USERGUIDE.md](USERGUIDE.md) — практические инструкции пользователя.

---

# 2. Требования

Для запуска требуется:

```text
Java 21+
Linux
Bash
Python 3
```

Дополнительно для построения графиков:

```text
python3-venv
python3-pip
matplotlib
```

Для захвата сетевого трафика:

```text
tcpdump
```

Проверка Java:

```bash
java -version
```

Ожидаемая версия:

```text
21.x
```

Проект использует Gradle Wrapper, поэтому отдельная установка Gradle не требуется.

---

# 3. Переход в каталог проекта

Пример:

```bash
cd ~/IdeaProjects/bank/secure-overlay
```

Проверить содержимое:

```bash
ls
```

В корне должны находиться как минимум:

```text
README.md
ARCHITECTURE.md
PROTOCOL.md
USERGUIDE.md

build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat

src/
scripts/
experiments/
```

---

# 4. Первичная проверка проекта

Выполнить:

```bash
./gradlew testClasses
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

Полный набор автоматических тестов:

```bash
./gradlew clean test
```

При успешной работе:

```text
BUILD SUCCESSFUL
```

---

# 5. Основной acceptance-сценарий

Рекомендуемый способ проверки всей системы:

```bash
./scripts/acceptance.sh
```

Сценарий автоматически выполняет:

```text
1. clean test
2. DHT scaling experiment
3. tunnel experiment
4. построение графиков
5. проверку созданных CSV и PNG
```

При успешном завершении выводится:

```text
=====================================
 ACCEPTANCE SUCCESSFUL
=====================================
```

Это основной воспроизводимый сценарий проекта.

---

# 6. Запуск DHT-эксперимента

Для отдельного запуска масштабного эксперимента:

```bash
./gradlew runExperiment
```

Эксперимент создаёт 12 логических узлов.

Используемые параметры:

```text
N = 12
k = 3
ALPHA = 3
R = 3
```

Проверяются топологии:

```text
RING
STAR
```

Для каждой топологии выполняются сценарии:

```text
normal
one-node-failed
```

Результаты сохраняются в:

```text
experiments/lookup-results.csv
```

Основные поля:

```text
topology
scenario
run
source_node_id
target_node_id
success
latency_ms
rpc_count
iterations
```

---

# 7. Просмотр результатов DHT

Первые строки:

```bash
head -10 experiments/lookup-results.csv
```

Посчитать общее число успешных lookup:

```bash
awk -F',' '
NR > 1 {
    total++
    if ($6 == "true") success++
}
END {
    print "total =", total
    print "success =", success
    print "success rate =", success / total * 100 "%"
}' experiments/lookup-results.csv
```

Контрольный результат:

```text
total = 120
success = 120
success rate = 100%
```

---

# 8. Запуск tunnel-эксперимента

Для проверки туннелей:

```bash
./gradlew runTunnelExperiment
```

Эксперимент включает:

```text
30 обычных передач сообщений
5 отказов активного relay
```

Результаты сохраняются:

```text
experiments/tunnel-results.csv
```

Основные поля:

```text
run
scenario
delivered
build_time_ms
end_to_end_latency_ms
recovery_time_ms
```

Контрольный результат одного из прогонов:

```text
35 observations
35 delivered
```

---

# 9. Проверка автоматического восстановления туннеля

Отдельный integration test:

```bash
./gradlew test \
  --tests "*TunnelRecoveryIntegrationTest" \
  --info
```

Сценарий:

```text
1. строится primary tunnel
2. отправляется первое сообщение
3. один активный relay останавливается
4. следующая отправка завершается ошибкой
5. автоматически строится backup tunnel
6. сообщение повторно отправляется
7. destination получает сообщение
```

Успешный результат:

```text
BUILD SUCCESSFUL
```

---

# 10. Проверка multi-hop tunnel

```bash
./gradlew test \
  --tests "*MultiHopTunnelIntegrationTest" \
  --info
```

Маршрут:

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

В логах должна появиться строка примерно:

```text
[TUNNEL] Destination received: SECRET THROUGH TWO RELAYS
```

---

# 11. Проверка криптографии туннеля

```bash
./gradlew test \
  --tests "*TunnelCryptoTest"
```

Проверяется:

```text
X25519 key agreement
AES-GCM encryption
AES-GCM decryption
совпадение derived key у обеих сторон
```

Ожидаемый результат:

```text# Secure Overlay — User Guide

## 1. Назначение

Этот документ описывает практический запуск, проверку и экспериментальное использование проекта Secure Overlay.

Основная документация:

- [README.md](README.md) — общее описание проекта, технологии, команды запуска и результаты.
- [ARCHITECTURE.md](ARCHITECTURE.md) — архитектура узла, DHT, безопасность, туннели и экспериментальная подсистема.
- [PROTOCOL.md](PROTOCOL.md) — формат Frame, типы сообщений и сетевой протокол.
- [USERGUIDE.md](USERGUIDE.md) — практические инструкции пользователя.

---

# 2. Требования

Для запуска требуется:

```text
Java 21+
Linux
Bash
Python 3
```

Дополнительно для построения графиков:

```text
python3-venv
python3-pip
matplotlib
```

Для захвата сетевого трафика:

```text
tcpdump
```

Проверка Java:

```bash
java -version
```

Ожидаемая версия:

```text
21.x
```

Проект использует Gradle Wrapper, поэтому отдельная установка Gradle не требуется.

---

# 3. Переход в каталог проекта

Пример:

```bash
cd ~/IdeaProjects/bank/secure-overlay
```

Проверить содержимое:

```bash
ls
```

В корне должны находиться как минимум:

```text
README.md
ARCHITECTURE.md
PROTOCOL.md
USERGUIDE.md

build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat

src/
scripts/
experiments/
```

---

# 4. Первичная проверка проекта

Выполнить:

```bash
./gradlew testClasses
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

Полный набор автоматических тестов:

```bash
./gradlew clean test
```

При успешной работе:

```text
BUILD SUCCESSFUL
```

---

# 5. Основной acceptance-сценарий

Рекомендуемый способ проверки всей системы:

```bash
./scripts/acceptance.sh
```

Сценарий автоматически выполняет:

```text
1. clean test
2. DHT scaling experiment
3. tunnel experiment
4. построение графиков
5. проверку созданных CSV и PNG
```

При успешном завершении выводится:

```text
=====================================
 ACCEPTANCE SUCCESSFUL
=====================================
```

Это основной воспроизводимый сценарий проекта.

---

# 6. Запуск DHT-эксперимента

Для отдельного запуска масштабного эксперимента:

```bash
./gradlew runExperiment
```

Эксперимент создаёт 12 логических узлов.

Используемые параметры:

```text
N = 12
k = 3
ALPHA = 3
R = 3
```

Проверяются топологии:

```text
RING
STAR
```

Для каждой топологии выполняются сценарии:

```text
normal
one-node-failed
```

Результаты сохраняются в:

```text
experiments/lookup-results.csv
```

Основные поля:

```text
topology
scenario
run
source_node_id
target_node_id
success
latency_ms
rpc_count
iterations
```

---

# 7. Просмотр результатов DHT

Первые строки:

```bash
head -10 experiments/lookup-results.csv
```

Посчитать общее число успешных lookup:

```bash
awk -F',' '
NR > 1 {
    total++
    if ($6 == "true") success++
}
END {
    print "total =", total
    print "success =", success
    print "success rate =", success / total * 100 "%"
}' experiments/lookup-results.csv
```

Контрольный результат:

```text
total = 120
success = 120
success rate = 100%
```

---

# 8. Запуск tunnel-эксперимента

Для проверки туннелей:

```bash
./gradlew runTunnelExperiment
```

Эксперимент включает:

```text
30 обычных передач сообщений
5 отказов активного relay
```

Результаты сохраняются:

```text
experiments/tunnel-results.csv
```

Основные поля:

```text
run
scenario
delivered
build_time_ms
end_to_end_latency_ms
recovery_time_ms
```

Контрольный результат одного из прогонов:

```text
35 observations
35 delivered
```

---

# 9. Проверка автоматического восстановления туннеля

Отдельный integration test:

```bash
./gradlew test \
  --tests "*TunnelRecoveryIntegrationTest" \
  --info
```

Сценарий:

```text
1. строится primary tunnel
2. отправляется первое сообщение
3. один активный relay останавливается
4. следующая отправка завершается ошибкой
5. автоматически строится backup tunnel
6. сообщение повторно отправляется
7. destination получает сообщение
```

Успешный результат:

```text
BUILD SUCCESSFUL
```

---

# 10. Проверка multi-hop tunnel

```bash
./gradlew test \
  --tests "*MultiHopTunnelIntegrationTest" \
  --info
```

Маршрут:

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

В логах должна появиться строка примерно:

```text
[TUNNEL] Destination received: SECRET THROUGH TWO RELAYS
```

---

# 11. Проверка криптографии туннеля

```bash
./gradlew test \
  --tests "*TunnelCryptoTest"
```

Проверяется:

```text
X25519 key agreement
AES-GCM encryption
AES-GCM decryption
совпадение derived key у обеих сторон
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

---

# 12. Проверка TLS

```bash
./gradlew test \
  --tests "*TlsDhtIntegrationTest"
```

В логах ожидается:

```text
[TLS] Connected using TLSv1.3
```

Далее должна успешно выполняться операция STORE и FIND_VALUE.

---

# 13. Захват TLS-трафика

Для подтверждения отсутствия открытого прикладного текста используется `tcpdump`.

Установить:

```bash
sudo apt install tcpdump
```

В первом терминале:

```bash
sudo tcpdump \
  -i lo \
  -s 0 \
  -w experiments/tls-check.pcap \
  'tcp port 9901'
```

Оставить capture включённым.

Во втором терминале:

```bash
./gradlew test \
  --tests "*TlsDhtIntegrationTest"
```

После завершения теста остановить `tcpdump`:

```text
Ctrl+C
```

Проверить файл:

```bash
ls -lh experiments/tls-check.pcap
```

---

# 14. Проверка plaintext в PCAP

Проверить контрольное значение:

```bash
strings experiments/tls-check.pcap \
  | grep "SECRET-VALUE"
```

Ожидаемый результат:

```text
пустой вывод
```

Проверить DHT key:

```bash
strings experiments/tls-check.pcap \
  | grep "tls-key"
```

Также ожидается пустой вывод.

Для просмотра пакетов:

```bash
tcpdump \
  -nn \
  -r experiments/tls-check.pcap \
  | head -30
```

Для графического анализа можно использовать Wireshark:

```bash
wireshark experiments/tls-check.pcap
```

---

# 15. Python-окружение для графиков

Если Python virtual environment ещё не создан:

```bash
sudo apt install python3-venv python3-pip
```

Создать:

```bash
python3 -m venv .venv
```

Активировать:

```bash
source .venv/bin/activate
```

После активации приглашение терминала будет выглядеть примерно так:

```text
(.venv) user@host:~/secure-overlay$
```

Установить matplotlib:

```bash
python -m pip install --upgrade pip
python -m pip install matplotlib
```

Проверка:

```bash
python -c "import matplotlib; print(matplotlib.__version__)"
```

---

# 16. Построение графиков

При активированном `.venv`:

```bash
python scripts/plot_experiments.py
```

Результат:

```text
experiments/plots/
```

Создаются:

```text
lookup-iterations.png
lookup-latency.png
lookup-rpc.png
lookup-success-rate.png

tunnel-build-time.png
tunnel-e2e-latency.png
tunnel-recovery.png
```

Проверить:

```bash
ls -lh experiments/plots
```

---

# 17. Интерпретация времени lookup

В CSV могут встречаться значения:

```text
0 ms
```

Это не означает физически нулевое время выполнения операции.

В текущей реализации latency вычисляется как:

```text
nanoseconds / 1_000_000
```

Поэтому значения менее одной миллисекунды округляются вниз до:

```text
0 ms
```

---

# 18. Основные экспериментальные результаты

Контрольная серия DHT:

```text
120 lookups
120 successful
100% success
```

Пример показателей:

```text
RING / normal
N = 30
mean = 102.33 ms
median = 58.50 ms
p95 = 180 ms
```

```text
STAR / normal
N = 30
mean = 24.23 ms
median = 26.50 ms
p95 = 65 ms
```

Tunnel experiment:

```text
Tunnel build:

N = 35
mean = 90.77 ms
median = 89.00 ms
min = 14 ms
max = 650 ms
p95 = 126 ms
```

```text
Tunnel E2E:

N = 30
mean = 32.63 ms
median = 30.50 ms
min = 13 ms
max = 66 ms
p95 = 65 ms
```

```text
Recovery:

N = 5
mean = 34.60 ms
median = 36.00 ms
min = 24 ms
max = 44 ms
```

---

# 19. Конфигурация

Основные параметры находятся в:

```text
src/main/resources/application.properties
```

Пример:

```properties
overlay.enabled=true

overlay.port=9001

overlay.node-id-bits=256

overlay.k-bucket-size=3

overlay.alpha=3

overlay.replication-factor=3

overlay.tunnel-relays=2

overlay.socket-timeout-ms=5000

overlay.record-ttl-ms=300000

overlay.replay-ttl-ms=300000
```

---

# 20. Тестовая конфигурация

В:

```text
src/test/resources/application.properties
```

используется:

```properties
overlay.enabled=false
```

Это необходимо, чтобы Spring Boot integration tests не запускали постоянно работающий TCP listener.

---

# 21. Запуск обычного Spring Boot приложения

Основной запуск:

```bash
./gradlew bootRun
```

Для явного запуска TCP node:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9001"
```

Если требуется другой порт:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9010"
```

---

# 22. Запуск нескольких узлов

Каждый узел должен использовать отдельный TCP port.

Например:

Терминал 1:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9001"
```

Терминал 2:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9002"
```

Терминал 3:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9003"
```

Для автоматизированных многоузловых сценариев рекомендуется использовать:

```bash
./gradlew runExperiment
```

---

# 23. Полезные тесты

Transport:

```bash
./gradlew test \
  --tests "*Frame*Test"
```

DHT:

```bash
./gradlew test \
  --tests "*Dht*Test"
```

Storage:

```bash
./gradlew test \
  --tests "*Storage*Test"
```

TLS:

```bash
./gradlew test \
  --tests "*Tls*Test"
```

Replay:

```bash
./gradlew test \
  --tests "*ReplayGuardTest"
```

Tunnel:

```bash
./gradlew test \
  --tests "*Tunnel*Test"
```

Statistics:

```bash
./gradlew test \
  --tests "*StatisticsTest"
```

---

# 24. Проверка FrameType

Wire protocol зависит от стабильных числовых кодов `FrameType`.

Запуск regression test:

```bash
./gradlew test \
  --tests "*FrameTypeTest"
```

Тест проверяет:

```text
фиксированные protocol codes
отсутствие duplicate codes
fromCode(code) round-trip
```

---

# 25. Экспериментальные файлы

Структура:

```text
experiments/
├── lookup-results.csv
├── tunnel-results.csv
├── tls-check.pcap
└── plots/
    ├── lookup-iterations.png
    ├── lookup-latency.png
    ├── lookup-rpc.png
    ├── lookup-success-rate.png
    ├── tunnel-build-time.png
    ├── tunnel-e2e-latency.png
    └── tunnel-recovery.png
```

---

# 26. Безопасность репозитория

Не добавлять в Git:

```text
identity-private.key
identity-public.key
```

Также не добавлять virtual environment:

```text
.venv/
```

Проверить:

```bash
git status
```

Перед commit желательно:

```bash
git diff
```

---

# 27. Рекомендуемый финальный сценарий перед сдачей

В корне проекта:

```bash
source .venv/bin/activate
```

Затем:

```bash
./scripts/acceptance.sh
```

После успешного выполнения проверить:

```bash
git status
```

И наличие результатов:

```bash
ls -lh experiments
ls -lh experiments/plots
```

---

# 28. Навигация по документации

Для общего знакомства с проектом:

[README.md](README.md)

Для изучения архитектуры:

[ARCHITECTURE.md](ARCHITECTURE.md)

Для изучения формата кадров и RPC:

[PROTOCOL.md](PROTOCOL.md)

Практические команды:

[USERGUIDE.md](USERGUIDE.md)

---

# 29. Возможные расширения

Основная версия проекта не требует дополнительных компонентов для выполнения базовых задач.

В дальнейшем могут быть добавлены:

```text
React dashboard
Spring REST API
Kafka event stream
Docker Compose
file transfer
netem scenarios
multi-seed bootstrap
```

React рекомендуется использовать только как слой визуализации.

Kafka рекомендуется использовать для событий и метрик:

```text
NODE_JOINED
LOOKUP_COMPLETED
RECORD_STORED
TUNNEL_BUILT
TUNNEL_REBUILT
EXPERIMENT_COMPLETED
```

Kafka не должна заменять собственный P2P transport Secure Overlay.

---

# 30. Состояние проекта

Рабочий acceptance-сценарий подтверждает работу:

```text
TCP framing
Kademlia lookup
DHT storage
replication
TLS 1.3
replay protection
multi-hop tunnel
end-to-end encryption
relay failure recovery
12-node experiments
CSV export
statistics
plot generation
```

При успешном запуске:

```bash
./scripts/acceptance.sh
```

финальный статус:

```text
ACCEPTANCE SUCCESSFUL
```
BUILD SUCCESSFUL
```

---

# 12. Проверка TLS

```bash
./gradlew test \
  --tests "*TlsDhtIntegrationTest"
```

В логах ожидается:

```text
[TLS] Connected using TLSv1.3
```

Далее должна успешно выполняться операция STORE и FIND_VALUE.

---

# 13. Захват TLS-трафика

Для подтверждения отсутствия открытого прикладного текста используется `tcpdump`.

Установить:

```bash
sudo apt install tcpdump
```

В первом терминале:

```bash
sudo tcpdump \
  -i lo \
  -s 0 \
  -w experiments/tls-check.pcap \
  'tcp port 9901'
```

Оставить capture включённым.

Во втором терминале:

```bash
./gradlew test \
  --tests "*TlsDhtIntegrationTest"
```

После завершения теста остановить `tcpdump`:

```text
Ctrl+C
```

Проверить файл:

```bash
ls -lh experiments/tls-check.pcap
```

---

# 14. Проверка plaintext в PCAP

Проверить контрольное значение:

```bash
strings experiments/tls-check.pcap \
  | grep "SECRET-VALUE"
```

Ожидаемый результат:

```text
пустой вывод
```

Проверить DHT key:

```bash
strings experiments/tls-check.pcap \
  | grep "tls-key"
```

Также ожидается пустой вывод.

Для просмотра пакетов:

```bash
tcpdump \
  -nn \
  -r experiments/tls-check.pcap \
  | head -30
```

Для графического анализа можно использовать Wireshark:

```bash
wireshark experiments/tls-check.pcap
```

---

# 15. Python-окружение для графиков

Если Python virtual environment ещё не создан:

```bash
sudo apt install python3-venv python3-pip
```

Создать:

```bash
python3 -m venv .venv
```

Активировать:

```bash
source .venv/bin/activate
```

После активации приглашение терминала будет выглядеть примерно так:

```text
(.venv) user@host:~/secure-overlay$
```

Установить matplotlib:

```bash
python -m pip install --upgrade pip
python -m pip install matplotlib
```

Проверка:

```bash
python -c "import matplotlib; print(matplotlib.__version__)"
```

---

# 16. Построение графиков

При активированном `.venv`:

```bash
python scripts/plot_experiments.py
```

Результат:

```text
experiments/plots/
```

Создаются:

```text
lookup-iterations.png
lookup-latency.png
lookup-rpc.png
lookup-success-rate.png

tunnel-build-time.png
tunnel-e2e-latency.png
tunnel-recovery.png
```

Проверить:

```bash
ls -lh experiments/plots
```

---

# 17. Интерпретация времени lookup

В CSV могут встречаться значения:

```text
0 ms
```

Это не означает физически нулевое время выполнения операции.

В текущей реализации latency вычисляется как:

```text
nanoseconds / 1_000_000
```

Поэтому значения менее одной миллисекунды округляются вниз до:

```text
0 ms
```

---

# 18. Основные экспериментальные результаты

Контрольная серия DHT:

```text
120 lookups
120 successful
100% success
```

Пример показателей:

```text
RING / normal
N = 30
mean = 102.33 ms
median = 58.50 ms
p95 = 180 ms
```

```text
STAR / normal
N = 30
mean = 24.23 ms
median = 26.50 ms
p95 = 65 ms
```

Tunnel experiment:

```text
Tunnel build:

N = 35
mean = 90.77 ms
median = 89.00 ms
min = 14 ms
max = 650 ms
p95 = 126 ms
```

```text
Tunnel E2E:

N = 30
mean = 32.63 ms
median = 30.50 ms
min = 13 ms
max = 66 ms
p95 = 65 ms
```

```text
Recovery:

N = 5
mean = 34.60 ms
median = 36.00 ms
min = 24 ms
max = 44 ms
```

---

# 19. Конфигурация

Основные параметры находятся в:

```text
src/main/resources/application.properties
```

Пример:

```properties
overlay.enabled=true

overlay.port=9001

overlay.node-id-bits=256

overlay.k-bucket-size=3

overlay.alpha=3

overlay.replication-factor=3

overlay.tunnel-relays=2

overlay.socket-timeout-ms=5000

overlay.record-ttl-ms=300000

overlay.replay-ttl-ms=300000
```

---

# 20. Тестовая конфигурация

В:

```text
src/test/resources/application.properties
```

используется:

```properties
overlay.enabled=false
```

Это необходимо, чтобы Spring Boot integration tests не запускали постоянно работающий TCP listener.

---

# 21. Запуск обычного Spring Boot приложения

Основной запуск:

```bash
./gradlew bootRun
```

Для явного запуска TCP node:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9001"
```

Если требуется другой порт:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9010"
```

---

# 22. Запуск нескольких узлов

Каждый узел должен использовать отдельный TCP port.

Например:

Терминал 1:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9001"
```

Терминал 2:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9002"
```

Терминал 3:

```bash
./gradlew bootRun \
  --args="--overlay.enabled=true --overlay.port=9003"
```

Для автоматизированных многоузловых сценариев рекомендуется использовать:

```bash
./gradlew runExperiment
```

---

# 23. Полезные тесты

Transport:

```bash
./gradlew test \
  --tests "*Frame*Test"
```

DHT:

```bash
./gradlew test \
  --tests "*Dht*Test"
```

Storage:

```bash
./gradlew test \
  --tests "*Storage*Test"
```

TLS:

```bash
./gradlew test \
  --tests "*Tls*Test"
```

Replay:

```bash
./gradlew test \
  --tests "*ReplayGuardTest"
```

Tunnel:

```bash
./gradlew test \
  --tests "*Tunnel*Test"
```

Statistics:

```bash
./gradlew test \
  --tests "*StatisticsTest"
```

---

# 24. Проверка FrameType

Wire protocol зависит от стабильных числовых кодов `FrameType`.

Запуск regression test:

```bash
./gradlew test \
  --tests "*FrameTypeTest"
```

Тест проверяет:

```text
фиксированные protocol codes
отсутствие duplicate codes
fromCode(code) round-trip
```

---

# 25. Экспериментальные файлы

Структура:

```text
experiments/
├── lookup-results.csv
├── tunnel-results.csv
├── tls-check.pcap
└── plots/
    ├── lookup-iterations.png
    ├── lookup-latency.png
    ├── lookup-rpc.png
    ├── lookup-success-rate.png
    ├── tunnel-build-time.png
    ├── tunnel-e2e-latency.png
    └── tunnel-recovery.png
```

---

# 26. Безопасность репозитория

Не добавлять в Git:

```text
identity-private.key
identity-public.key
```

Также не добавлять virtual environment:

```text
.venv/
```

Проверить:

```bash
git status
```

Перед commit желательно:

```bash
git diff
```

---

# 27. Рекомендуемый финальный сценарий перед сдачей

В корне проекта:

```bash
source .venv/bin/activate
```

Затем:

```bash
./scripts/acceptance.sh
```

После успешного выполнения проверить:

```bash
git status
```

И наличие результатов:

```bash
ls -lh experiments
ls -lh experiments/plots
```

---

# 28. Навигация по документации

Для общего знакомства с проектом:

[README.md](README.md)

Для изучения архитектуры:

[ARCHITECTURE.md](ARCHITECTURE.md)

Для изучения формата кадров и RPC:

[PROTOCOL.md](PROTOCOL.md)

Практические команды:

[USERGUIDE.md](USERGUIDE.md)

---

# 29. Возможные расширения

Основная версия проекта не требует дополнительных компонентов для выполнения базовых задач.

В дальнейшем могут быть добавлены:

```text
React dashboard
Spring REST API
Kafka event stream
Docker Compose
file transfer
netem scenarios
multi-seed bootstrap
```

React рекомендуется использовать только как слой визуализации.

Kafka рекомендуется использовать для событий и метрик:

```text
NODE_JOINED
LOOKUP_COMPLETED
RECORD_STORED
TUNNEL_BUILT
TUNNEL_REBUILT
EXPERIMENT_COMPLETED
```

Kafka не должна заменять собственный P2P transport Secure Overlay.

---

# 30. Состояние проекта

Рабочий acceptance-сценарий подтверждает работу:

```text
TCP framing
Kademlia lookup
DHT storage
replication
TLS 1.3
replay protection
multi-hop tunnel
end-to-end encryption
relay failure recovery
12-node experiments
CSV export
statistics
plot generation
```

При успешном запуске:

```bash
./scripts/acceptance.sh
```

финальный статус:

```text
ACCEPTANCE SUCCESSFUL
```
