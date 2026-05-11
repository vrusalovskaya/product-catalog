package org.catalog.testsupport;

import org.testcontainers.containers.MySQLContainer;

public final class MySqlTestContainer {

    public static final MySQLContainer<?> INSTANCE =
            new MySQLContainer<>("mysql:8.4.3")
                    .withDatabaseName("product_catalog_test")
                    .withUsername("test_user")
                    .withPassword("test_pass")
                    .withUrlParam("useSSL", "false")
                    .withUrlParam("allowPublicKeyRetrieval", "true")
                    .withReuse(true);

    static {
        INSTANCE.start();
    }

    private MySqlTestContainer() {
    }
}
