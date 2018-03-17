package ru.csc.bdse.app.v11.model;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.util.ProtoUtils;
import ru.csc.bdse.kv.KeyValueApi;

import java.io.UncheckedIOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component("phoneBookV11")
public class PhoneBook implements PhoneBookApi<PhoneBookRecord> {

    @NotNull
    private final KeyValueApi kvApi;

    public PhoneBook(@NotNull KeyValueApi kvApi) {
        this.kvApi = kvApi;
    }

    @Override
    public void put(@NotNull PhoneBookRecord record) {
        kvApi.put(makeKey(record.firstName, record.lastName), ProtoUtils.encode(record));
    }

    @Override
    public void delete(@NotNull PhoneBookRecord record) {
        kvApi.delete(makeKey(record.firstName, record.lastName));
    }

    @Override
    @NotNull
    public Set<PhoneBookRecord> get(char literal) {
        return kvApi.getKeys(String.valueOf(literal)).stream()
                .map(kvApi::get)
                .map(data -> {
                    try {
                        return ProtoUtils.decodeV11(data.get());
                    } catch (InvalidProtocolBufferException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private String makeKey(String firstName, String lastName) {
        return lastName + firstName;
    }
}
