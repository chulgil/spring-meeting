package me.chulgil.spring.meeting.infra;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(SpringRunner.class)
@TestPropertySource("/application-dev.properties")
public class AbstractContainerBaseTest {

    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer("postgres:10.3")
                .withDatabaseName("meeting")
                .withUsername("cglee")
                .withPassword("rprruqmboedmhzyl");
    }
}
