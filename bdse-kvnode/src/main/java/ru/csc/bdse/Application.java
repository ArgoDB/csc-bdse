package ru.csc.bdse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.csc.bdse.kv.ConcurrentKeyValueApi;
import ru.csc.bdse.kv.KeyValueApi;
import ru.csc.bdse.storage.FileBasedStorage;
import ru.csc.bdse.storage.Storage;
import ru.csc.bdse.util.Env;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.UUID;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static String randomNodeName() {
        return "kvnode-" + UUID.randomUUID().toString().substring(4);
    }

    @Bean
    KeyValueApi node() {
        String nodeName = Env.get(Env.KVNODE_NAME).orElseGet(Application::randomNodeName);
        try {
            Storage storage = FileBasedStorage.createOrOpen(Paths.get(nodeName));
            return new ConcurrentKeyValueApi(nodeName, storage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
