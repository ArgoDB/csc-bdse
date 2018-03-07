package ru.csc.bdse.kv;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import ru.csc.bdse.storage.FileBasedStorage;
import ru.csc.bdse.util.Random;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

public class FileBasedStorageTest extends AbstractKeyValueApiTest {
    @Rule
    public final TemporaryFolder rule = new TemporaryFolder();
    private Path temporaryDirectory;

    @Before
    public void setUp() throws Exception {
        rule.create();
        temporaryDirectory = rule.newFolder().toPath();
        super.setUp();
    }

    @After
    public void tearDown() {
        rule.delete();
    }

    @Override
    protected KeyValueApi newKeyValueApi() {
        try {
            FileBasedStorage.createOrOpen(temporaryDirectory);
            return new ConcurrentKeyValueApi("testStorage", FileBasedStorage.createOrOpen(temporaryDirectory));
        } catch (IOException e) {
            System.err.println("Something went wrong: " + e);
            throw new UncheckedIOException("Cannot initialize/open database", e);
        }
    }

    @Test
    public void deleteByTombstone() {
        Assert.assertTrue(api instanceof KeyValueApiEx);
        KeyValueApiEx apiEx = (KeyValueApiEx) api;
        String key = Random.nextKey();
        byte[] value = Random.nextValue();
        apiEx.put(key, value);
        Optional<byte[]> bytes = apiEx.get(key);

        Assert.assertTrue(bytes.isPresent());
        Assert.assertArrayEquals(value, bytes.get());

        apiEx.delete(key);

        Assert.assertFalse(apiEx.get(key).isPresent());

        bytes = apiEx.get(key, true);
        Assert.assertTrue(bytes.isPresent());
        Assert.assertArrayEquals(value, bytes.get());
    }
}
