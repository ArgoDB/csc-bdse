package ru.csc.bdse.kv;

import org.jetbrains.annotations.NotNull;
import ru.csc.bdse.storage.Storage;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public class ConcurrentKeyValueApi implements KeyValueApiEx {
    private final String name;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(false);
    private final Storage storage;
    private NodeStatus status = NodeStatus.UP;

    public ConcurrentKeyValueApi(@NotNull String name, @NotNull Storage storage) {
        this.name = name;
        this.storage = storage;
    }

    @Override
    public void put(String key, byte[] data) {
        withWriteLockIfEnabled(() -> storage.put(key, data));
    }

    @Override
    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(withReadLockIfEnabled(() -> storage.get(key)));
    }

    @Override
    public Optional<byte[]> get(@NotNull String key, boolean includingDeleted) {
        return Optional.ofNullable(withReadLockIfEnabled(() -> storage.get(key, includingDeleted)));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return withReadLockIfEnabled(() -> storage.matchByPrefix(prefix, false));
    }

    @Override
    public void delete(String key) {
        withWriteLockIfEnabled(() -> storage.delete(key));
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Collections.singleton(withReadLock(() -> new NodeInfo(name, status)));
    }

    @Override
    public void action(String node, NodeAction action) {
        if (node.equals(name)) {
            withWriteLock(() -> {
                if (NodeAction.UP.equals(action)) {
                    status = NodeStatus.UP;
                } else if (NodeAction.DOWN.equals(action)) {
                    status = NodeStatus.DOWN;
                } else {
                    throw new IllegalArgumentException("unknown action: " + action);
                }
            });
        }
    }

    private <T> T withReadLockIfEnabled(@NotNull Supplier<T> action) {
        return withReadLock(() -> {
            ensureEnabled();
            return action.get();
        });
    }

    private void withWriteLockIfEnabled(@NotNull Runnable runnable) {
        withWriteLock(() -> {
            ensureEnabled();
            runnable.run();
        });
    }

    private <T> T withReadLock(@NotNull Supplier<T> action) {
        try {
            lock.readLock().lock();
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void withWriteLock(@NotNull Runnable runnable) {
        try {
            lock.writeLock().lock();
            runnable.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void ensureEnabled() {
        if (NodeStatus.DOWN.equals(status)) {
            throw new IllegalStateException("Node \"" + name + "\" disabled");
        }
    }
}
