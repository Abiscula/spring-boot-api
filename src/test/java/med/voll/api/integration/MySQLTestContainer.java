package med.voll.api.integration;

import org.testcontainers.containers.MySQLContainer;

public class MySQLTestContainer {

    private static final MySQLContainer<?> container;

    static {
        container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("vollmed")
                .withUsername("root")
                .withPassword("root");
        container.start();
    }

    public static MySQLContainer<?> getInstance() {
        return container;
    }
}
