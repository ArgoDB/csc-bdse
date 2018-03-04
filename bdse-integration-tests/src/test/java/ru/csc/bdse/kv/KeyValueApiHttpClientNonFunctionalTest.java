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

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Test have to be implemented
 *
 * @author alesavin
 */
public class KeyValueApiHttpClientNonFunctionalTest {

    private static final String ZERO_NODE_NAME = "node-0";

    @ClassRule
    public static final GenericContainer node = new GenericContainer(new ImageFromDockerfile().withFileFromFile("target/bdse-kvnode-0.0.1-SNAPSHOT.jar", new File("../bdse-kvnode/target/bdse-kvnode-0.0.1-SNAPSHOT.jar")).withFileFromClasspath("Dockerfile", "kvnode/Dockerfile")).withEnv(Env.KVNODE_NAME, ZERO_NODE_NAME)
            .withExposedPorts(8080)
            .withStartupTimeout(Duration.of(30, SECONDS));

    private KeyValueApi api = newKeyValueApi();

    private KeyValueApi newKeyValueApi() {
        final String baseUrl = "http://localhost:" + node.getMappedPort(8080);
        return new KeyValueApiHttpClient(baseUrl);
    }

    @Test
    public void concurrentPuts() {
        // TODO simultanious puts for the same key value

    }

    @Test
    public void concurrentDeleteAndKeys() {
        //TODO simultanious delete by key and keys listing
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
            Set<String> result = api.getKeys("x");
        } catch (Throwable t) {
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void deleteByTombstone() {
        // TODO use tombstones to mark as deleted (optional)
    }

    @Test
    public void loadMillionKeys()  {
        //TODO load too many data (optional)
    }
}


