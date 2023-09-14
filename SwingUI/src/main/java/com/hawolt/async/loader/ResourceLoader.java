package com.hawolt.async.loader;

import com.hawolt.async.ExecutorManager;
import com.hawolt.client.cache.ExceptionalSupplier;
import com.hawolt.cryptography.MD5;
import com.hawolt.generic.data.Unsafe;
import com.hawolt.http.layer.IResponse;
import com.hawolt.http.layer.impl.OkHttpResponse;
import com.hawolt.io.Core;
import com.hawolt.io.JsonSource;
import com.hawolt.io.RunLevel;
import com.hawolt.logger.Logger;
import com.hawolt.sql.Hikari;
import com.hawolt.sql.ResultSetTransformer;
import com.hawolt.util.other.StaticConstant;
import okhttp3.Request;
import org.h2.tools.Server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created: 16/08/2023 18:21
 * Author: Twitter @hawolt
 **/

public class ResourceLoader {
    private static final Path database = StaticConstant.APPLICATION_CACHE.resolve("h2");
    private static final ExecutorService service = ExecutorManager.registerService(
            "resource-loader-jobs",
            Executors.newFixedThreadPool(8)
    );

    private static final Map<String, List<ResourceConsumer<?, byte[]>>> pending = new HashMap<>();
    private static final Map<String, byte[]> cache = new HashMap<>();
    private static final Map<String, Object> locks = new HashMap<>();
    private static final Set<String> hashes = new HashSet<>();
    private static final Object lock = new Object();

    static {
        try (InputStream configStream = RunLevel.get("sql/config.json")) {
            Server h2Server = Server.createTcpServer("-ifNotExists").start();
            if (h2Server.isRunning(true)) {
                Logger.info(h2Server.getStatus());
            } else {
                Logger.fatal("Could not start H2 server");
            }
            Hikari.setup(
                    String.format("jdbc:h2:tcp://localhost:9092/%s;mode=MySQL", database),
                    JsonSource.of(configStream)
            );
            try (InputStream statementStream = RunLevel.get("sql/statement.sql")) {
                try (Connection connection = Hikari.getManager().getConnection()) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(Core.read(statementStream).toString());
                    }
                }
            }
            try (Connection connection = Hikari.getManager().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT HASH FROM CACHE")) {
                    try (ResultSet set = statement.executeQuery()) {
                        List<String> list = ResultSetTransformer.parse(set, o -> o[0].toString());
                        Collections.addAll(hashes, list.toArray(String[]::new));
                    }
                }
            } catch (SQLException e) {
                Logger.warn("Failed to query database for hashes");
            }
        } catch (SQLException | IOException e) {
            Logger.fatal("Unable to initialize embedded in-memory cache database");
            Logger.error(e);
        }
    }

    private static <T> T convert(Object in) {
        return Unsafe.cast(in);
    }

    private static void load(String path, ResourceConsumer<?, byte[]> consumer, Runnable runnable) {
        String hash = MD5.hash(path);
        Object lock;
        synchronized (ResourceLoader.lock) {
            if (!locks.containsKey(hash)) locks.put(hash, new Object());
            lock = locks.get(hash);
        }
        synchronized (lock) {
            if (cached(hash)) {
                try {
                    consumer.consume(path, Unsafe.cast(consumer.transform(get(hash))));
                } catch (Exception e) {
                    consumer.onException(path, e);
                }
            } else {
                if (pending.containsKey(hash)) {
                    pending.get(hash).add(consumer);
                } else {
                    pending.put(hash, new ArrayList<>());
                    pending.get(hash).add(consumer);
                    service.execute(runnable);
                }
            }
        }

    }


    public static void loadResource(String uri, ExceptionalSupplier<byte[]> supplier, ResourceConsumer<?, byte[]> consumer) {
        service.execute(() -> load(uri, consumer, () -> {
            try {
                handle(uri, supplier.get());
            } catch (Exception e) {
                exceptional(uri, e);
            }
        }));
    }

    public static void loadResource(String uri, ResourceConsumer<?, byte[]> consumer) {
        service.execute(() -> load(uri, consumer, () -> {
            Request request = new Request.Builder()
                    .url(uri)
                    .header("User-Agent", StaticConstant.USER_AGENT)
                    .get()
                    .build();
            try {
                IResponse response = OkHttpResponse.from(request);
                handle(uri, response.response());
            } catch (IOException e) {
                exceptional(uri, e);
            }
        }));
    }

    public static void loadLocalResource(String name, ResourceConsumer<?, byte[]> consumer) {
        service.execute(() -> load(name, consumer, () -> {
            try (InputStream stream = RunLevel.get(name)) {
                handle(name, Core.read(stream).toByteArray());
            } catch (IOException e) {
                exceptional(name, e);
            }
        }));
    }

    public static void forceLoadResource(String name, ExceptionalSupplier<byte[]> supplier, ResourceConsumer<?, byte[]> consumer) {
        service.execute(() -> {
            try {
                String hash = MD5.hash(name);
                pending.put(hash, new ArrayList<>());
                pending.get(hash).add(consumer);
                consume(name, hash, supplier.get());
            } catch (Exception e) {
                exceptional(name, e);
            }
        });
    }

    private static void handle(String path, byte[] b) {
        String hash = MD5.hash(path);
        store(path, hash, b);
        consume(path, hash, b);
    }

    private static void store(String o, String hash, byte[] b) {
        service.execute(() -> {
            try (Connection connection = Hikari.getManager().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO CACHE VALUES(?,?,?) ON DUPLICATE KEY UPDATE DATA=?")) {
                    statement.setString(1, o);
                    statement.setString(2, hash);
                    statement.setBytes(3, b);
                    statement.setBytes(4, b);
                    statement.execute();
                    ResourceLoader.cache(hash, b);
                    Logger.debug("stored '{}' in database as {}", o, hash);
                }
            } catch (SQLException e) {
                Logger.error(e);
            }
        });
    }

    private static void consume(String o, String hash, byte[] b) {
        if (b.length != 0) {
            synchronized (locks.getOrDefault(hash, new Object())) {
                if (!pending.containsKey(hash)) Logger.error("attempt to load unknown value '{}' from cache", o);
                List<ResourceConsumer<?, byte[]>> list = new ArrayList<>(pending.get(hash));
                for (ResourceConsumer<?, byte[]> consumer : list) {
                    try {
                        consumer.consume(o, convert(consumer.transform(b)));
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }
                pending.remove(hash);
            }
        } else {
            exceptional(o, new IOException(String.format("%s has loaded with 0 bytes", hash)));
        }
    }

    private static void exceptional(String o, Exception e) {
        List<ResourceConsumer<?, byte[]>> list = new ArrayList<>(pending.get(o));
        for (ResourceConsumer<?, byte[]> consumer : list) {
            consumer.onException(o, e);
        }
    }

    private static boolean cached(String hash) {
        return hashes.contains(hash);
    }

    private static void cache(String hash, byte[] b) {
        cache.put(hash, b);
        hashes.add(hash);
    }

    private static byte[] get(String hash) throws SQLException, IOException {
        if (cache.containsKey(hash)) return cache.get(hash);
        try (Connection connection = Hikari.getManager().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT DATA FROM CACHE WHERE HASH=?")) {
                statement.setString(1, hash);
                try (ResultSet set = statement.executeQuery()) {
                    byte[] b = ResultSetTransformer.singleton(set, o -> o.length > 0 && o[0] != null ? (byte[]) o[0] : new byte[0]);
                    if (b.length == 0) {
                        throw new IOException(String.format("Cache contains bad binary for hash %s", hash));
                    } else {
                        return b;
                    }
                }
            }
        }
    }
}