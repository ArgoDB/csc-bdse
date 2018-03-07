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

    ConcurrentKeyValueApi(@NotNull String name, @NotNull Storage storage) {
        this.name = name;
        this.storage = storage;
    }

    @Override
    public void put(String key, byte[] data) {
        withWriteLock(() -> storage.put(key, data));
    }

    @Override
    public Optional<byte[]> get(String key) {
        return Optional.ofNullable(withReadLock(() -> storage.get(key)));
    }

    @Override
    public Optional<byte[]> get(@NotNull String key, boolean includingDeleted) {
        return Optional.ofNullable(withReadLock(() -> storage.get(key, includingDeleted)));
    }

    @Override
    public Set<String> getKeys(String prefix) {
        return withReadLock(() -> storage.matchByPrefix(prefix, false));
    }

    @Override
    public void delete(String key) {
        withWriteLock(() -> storage.delete(key));
    }

    @Override
    public Set<NodeInfo> getInfo() {
        return Collections.singleton(new NodeInfo(name, NodeStatus.UP));
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

    private <T> T withReadLock(@NotNull Supplier<T> action) {
        try {
            lock.readLock().lock();
            ensureEnabled();
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void withWriteLock(@NotNull Runnable runnable) {
        try {
            lock.writeLock().lock();
            ensureEnabled();
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
