package com.foo.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.doAnswer;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.foo.ResultCaptor;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
        "spring.datasource.writer.jdbc-url=" + TransactionHandlingTest.WRITER_DATASOURCE,
        "spring.datasource.reader.jdbc-url=" + TransactionHandlingTest.READER_DATASOURCE,
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TransactionHandlingTest.TestRepository.class,
        TransactionHandlingTest.TestApplicationService.class,
        DataSourceConfiguration.class,
        WriterDatasourceProperties.class,
        TransactionManagerConfiguration.class,
        ReaderDatasourceProperties.class,
})
class TransactionHandlingTest {

    protected static final String WRITER_DATASOURCE = "jdbc:h2:mem:write";
    protected static final String READER_DATASOURCE = "jdbc:h2:mem:read";

    @Autowired
    private TestApplicationService testApplicationService;

    @SpyBean
    private DataSource dataSource;

    private ConnectionReadOnlyCaptor connectionReadOnlyCaptor;

    @BeforeEach
    void setUp() throws SQLException {
        connectionReadOnlyCaptor = new ConnectionReadOnlyCaptor();
        doAnswer(connectionReadOnlyCaptor).when(dataSource).getConnection();
    }

    @Test
    void Eine_Write_Transaktion_innerhalb_einer_ReadOnly_Transaktion_erzeugt_eine_Exception() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> testApplicationService.writeWithReadTransaction());
    }

    @Test
    void Zwei_unabhängige_Transaktionen_können_ReadOnly_und_Write_DB_Instanzen_nutzen() {
        testApplicationService.readAndWriteWithoutTransaction();

        AssertionsForClassTypes.assertThat(connectionReadOnlyCaptor.getResults())
                .asList()
                .containsExactly(READER_DATASOURCE, WRITER_DATASOURCE);
    }

    @Test
    void Zwei_Transaktionen_nutzen_die_Writer_DB_Instanz_innerhalb_einer_Write_Transaktion() {
        testApplicationService.readAndWriteWithWriteTransaction();

        AssertionsForClassTypes.assertThat(connectionReadOnlyCaptor.getResults())
                .asList()
                .containsExactly(WRITER_DATASOURCE);
    }

    @Test
    void Eine_ReadOnly_Transaktion_nutzt_eine_ReadOnly_DB_Instanz() {
        testApplicationService.readWithReadTransaction();

        AssertionsForClassTypes.assertThat(connectionReadOnlyCaptor.getResults())
                .asList()
                .containsExactly(READER_DATASOURCE);
    }

    @Test
    void Mehrere_ReadOnly_Transaktionen_nutzten_eine_ReadOnly_DB_Instanz() {
        testApplicationService.readsWithReadTransaction();

        AssertionsForClassTypes.assertThat(connectionReadOnlyCaptor.getResults())
                .asList()
                .containsExactly(READER_DATASOURCE);
    }

    @Test
    void Neue_Transaktionen_innerhalb_einer_Transaktion_ermitteln_ihre_DB_Instanz_neu() {
        testApplicationService.readWithNewTransactionAndWriteWithWriteTransaction();

        AssertionsForClassTypes.assertThat(connectionReadOnlyCaptor.getResults())
                .asList()
                .containsExactly(READER_DATASOURCE, WRITER_DATASOURCE, WRITER_DATASOURCE);
    }

    static class ConnectionReadOnlyCaptor extends ResultCaptor<Connection, String> {
        public ConnectionReadOnlyCaptor() {
            super((connection) -> {
                try {
                    return connection.getMetaData().getURL();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @TestComponent
    static class TestApplicationService {

        @Autowired
        TestRepository testRepository;

        @Transactional
        public void readAndWriteWithWriteTransaction() {
            testRepository.readWithReadTransaction();
            testRepository.writeWithWriteTransaction();
        }

        public void readAndWriteWithoutTransaction() {
            testRepository.readWithReadTransaction();
            testRepository.writeWithWriteTransaction();
        }

        @Transactional(readOnly = true)
        public void readWithReadTransaction() {
            testRepository.readWithReadTransaction();
        }

        @Transactional(readOnly = true)
        public void readsWithReadTransaction() {
            testRepository.readWithReadTransaction();
            testRepository.readWithReadTransaction();
        }

        @Transactional(readOnly = true)
        public void writeWithReadTransaction() {
            testRepository.writeWithWriteTransaction();
        }

        @Transactional
        public void readWithNewTransactionAndWriteWithWriteTransaction() {
            testRepository.readWithNewReadTransaction();
            testRepository.writeWithNewWriteTransaction();
        }
    }

    @TestComponent
    static class TestRepository {

        @Transactional(readOnly = true)
        public void readWithReadTransaction() {
        }

        @Transactional
        public void writeWithWriteTransaction() {
        }

        @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
        public void readWithNewReadTransaction() {
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void writeWithNewWriteTransaction() {
        }
    }
}
