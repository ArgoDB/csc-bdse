package ru.csc.bdse.kv;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Vitaliy.Bibaev
 */
public interface KeyValueApiEx extends KeyValueApi {
    Optional<byte[]> get(@NotNull String key, boolean includingDeleted);
}
