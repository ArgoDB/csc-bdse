package ru.csc.bdse.kv;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import ru.csc.bdse.util.Env;
import ru.csc.bdse.util.Random;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientNonFunctionalTest {

    private static final String ZERO_NODE_NAME = "node-0";
    private static final int CONCURRENT_THREADS = 30;

    @ClassRule
    public static final GenericContainer node = new GenericContainer(new ImageFromDockerfile()
            .withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar",
                    new File("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar"))
            .withFileFromClasspath("Dockerfile", "kvnode/Dockerfile"))
            .withEnv(Env.KVNODE_NAME, ZERO_NODE_NAME)
            .withExposedPorts(8080)
            .withStartupTimeout(Duration.of(30, SECONDS));

    private KeyValueApi api = newKeyValueApi();

    private KeyValueApi newKeyValueApi() {
        final String baseUrl = "http://localhost:" + node.getMappedPort(8080);
        return new KeyValueApiHttpClient(baseUrl);
    }

    @Test
    public void concurrentPuts() throws InterruptedException {
        String key = Random.nextKey();
        Thread[] threads = IntStream.range(0, CONCURRENT_THREADS)
                .mapToObj(i -> new Thread(() -> api.put(key, String.valueOf(i).getBytes())))
                .peek(Thread::start)
                .toArray(Thread[]::new);
        for (Thread thread : threads) {
            thread.join();
        }

        Optional<byte[]> bytes = api.get(key);
        Assert.assertTrue(bytes.isPresent());
        Assert.assertEquals(String.valueOf(CONCURRENT_THREADS), new String(bytes.get()));
    }

    @Test
    public void concurrentDeleteAndKeys() throws InterruptedException {
        fillWithRandomValues(10_000, 30);
        Thread[] threads = IntStream.range(0, CONCURRENT_THREADS)
                .mapToObj(i -> new Thread(() -> {
                    Set<String> keys = api.getKeys("");
                    while(!keys.isEmpty()) {
                        keys.stream().findAny().ifPresent(api::delete);
                        keys = api.getKeys("");
                    }
                }))
                .peek(Thread::start)
                .toArray(Thread[]::new);

        for (Thread thread : threads) {
            thread.join();
        }
    }

    @Test
    public void actionUpDown() {
        api.action(ZERO_NODE_NAME, NodeAction.UP);
        Set<NodeInfo> info = api.getInfo();
        Optional<NodeInfo> zeroNode = info.stream().filter(x -> ZERO_NODE_NAME.equals(x.getName())).findFirst();
        Assert.assertTrue(zeroNode.isPresent());
        Assert.assertEquals(NodeStatus.UP, zeroNode.get().getStatus());

        api.action(ZERO_NODE_NAME, NodeAction.DOWN);
        info = api.getInfo();
        zeroNode = info.stream().filter(x -> ZERO_NODE_NAME.equals(x.getName())).findFirst();
        Assert.assertTrue(zeroNode.isPresent());
        Assert.assertEquals(NodeStatus.DOWN, zeroNode.get().getStatus());
    }

    @Test
    public void putWithStoppedNode() {
        api.action(ZERO_NODE_NAME, NodeAction.UP);
        String key = Random.nextKey();
        byte[] firstValue = Random.nextValue();
        byte[] secondValue = Random.nextValue();
        if (Arrays.equals(firstValue, secondValue)) {
            secondValue[0]++;
        }
        api.put(key, firstValue);

        api.action(ZERO_NODE_NAME, NodeAction.DOWN);
        boolean exceptionThrown = false;
        try {
            api.put(key, secondValue);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);

        api.action(ZERO_NODE_NAME, NodeAction.UP);
        Optional<byte[]> result = api.get(key);
        Assert.assertTrue(result.isPresent());
        Assert.assertArrayEquals(firstValue, result.get());
    }

    @Test
    public void getWithStoppedNode() {
        api.action(ZERO_NODE_NAME, NodeAction.DOWN);
        boolean exceptionThrown = false;
        try {
            api.get(Random.nextKey());
        } catch (Throwable t) {
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void getKeysByPrefixWithStoppedNode() {
        api.action(ZERO_NODE_NAME, NodeAction.DOWN);
        boolean exceptionThrown = false;
        try {
            api.getKeys("x");
        } catch (Throwable t) {
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);
    }

    /**
     * Moved into FileBasedStorageTest
     */
//    @Test
//    public void deleteByTombstone() {
//        use tombstones to mark as deleted (optional)
//    }

    @Test
    public void loadMillionKeys() {
        fillWithRandomValues(1_000_000, 40);
    }

    private void fillWithRandomValues(int count, int valueSize) {
        java.util.Random random = new java.util.Random();
        byte[] value = new byte[valueSize];
        for (int i = 0; i < count; i++) {
            random.nextBytes(value);
            api.put(Random.nextKey(), value);
        }
    }
}


