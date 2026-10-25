package ru.secureoverlay.secure_overlay.dht.storage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DhtStore {

    private final Map<String, DhtRecord> records =
            new ConcurrentHashMap<>();


    public synchronized StoreResult put(
            DhtRecord record
    ) {

        if (record == null) {

            return new StoreResult(
                    false,
                    "NULL_RECORD"
            );
        }


        if (!DhtRecordSigner.verify(record)) {

            return new StoreResult(
                    false,
                    "INVALID_SIGNATURE"
            );
        }


        if (record.expired()) {

            return new StoreResult(
                    false,
                    "EXPIRED"
            );
        }


        DhtRecord current =
                records.get(
                        record.key()
                );


        /*
         * Первая запись.
         */
        if (current == null) {

            records.put(
                    record.key(),
                    record
            );

            return new StoreResult(
                    true,
                    "STORED"
            );
        }


        /*
         * Владелец уже существующего
         * DHT key меняться не может.
         */
        if (!current.ownerPublicKey()
                .equals(
                        record.ownerPublicKey()
                )) {

            return new StoreResult(
                    false,
                    "OWNER_MISMATCH"
            );
        }


        /*
         * Защита от rollback:
         * старая версия не должна
         * перезаписывать новую.
         */
        if (record.version()
                < current.version()) {

            return new StoreResult(
                    false,
                    "STALE_VERSION"
            );
        }


        /*
         * Та же версия.
         */
        if (record.version()
                == current.version()) {

            /*
             * Это полностью та же запись.
             *
             * Такое возможно при republish.
             */
            if (current.equals(record)) {

                return new StoreResult(
                        true,
                        "ALREADY_STORED"
                );
            }


            /*
             * Версия та же,
             * но содержимое отличается.
             */
            return new StoreResult(
                    false,
                    "VERSION_CONFLICT"
            );
        }


        /*
         * Более новая версия
         * от того же владельца.
         */
        records.put(
                record.key(),
                record
        );


        return new StoreResult(
                true,
                "UPDATED"
        );
    }


    public Optional<DhtRecord> get(
            String key
    ) {

        if (key == null) {return Optional.empty();}


        DhtRecord record = records.get(key);


        if (record == null) {

            return Optional.empty();
        }


        /*
         * При чтении сразу удаляем
         * просроченную запись.
         */
        if (record.expired()) {

            records.remove(key, record);

            return Optional.empty();
        }


        return Optional.of(record);
    }


    public int cleanupExpired() {

        int before = records.size();


        records.entrySet()
                .removeIf(entry -> entry.getValue().expired()
                );


        return before - records.size();
    }


    public int size() {

        return records.size();
    }
}