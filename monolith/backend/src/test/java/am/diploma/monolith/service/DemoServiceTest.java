package am.diploma.monolith.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private DemoService demoService;

    private List<String> captureAllQueries() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        demoService.resetData();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(entityManager, atLeastOnce()).createNativeQuery(captor.capture());
        return captor.getAllValues();
    }

    @Test
    @DisplayName("Executes TRUNCATE CASCADE on all tables")
    void resetData_executesTruncate() {
        List<String> queries = captureAllQueries();

        assertTrue(queries.stream().anyMatch(q -> q.contains("TRUNCATE") && q.contains("CASCADE")));
    }

    @Test
    @DisplayName("Inserts both seed customers with correct data")
    void resetData_insertsSeedCustomers() {
        List<String> queries = captureAllQueries();

        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO customer") && q.contains("Alice Johnson") && q.contains("10000.00")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO customer") && q.contains("Bob Smith") && q.contains("50.00")));
    }

    @Test
    @DisplayName("Inserts all four seed products with correct data")
    void resetData_insertsSeedProducts() {
        List<String> queries = captureAllQueries();

        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO product") && q.contains("Laptop Pro 15") && q.contains("1299.99")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO product") && q.contains("Wireless Mouse") && q.contains("29.99")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO product") && q.contains("USB-C Hub") && q.contains("49.99")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("INSERT INTO product") && q.contains("Mechanical Keyboard") && q.contains("89.99")));
    }

    @Test
    @DisplayName("Resets all four sequences to correct starting values")
    void resetData_resetsAllSequences() {
        List<String> queries = captureAllQueries();

        assertTrue(queries.stream().anyMatch(q ->
                q.contains("ALTER SEQUENCE customer_id_seq RESTART WITH 3")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("ALTER SEQUENCE product_id_seq RESTART WITH 5")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("ALTER SEQUENCE orders_id_seq RESTART WITH 1")));
        assertTrue(queries.stream().anyMatch(q ->
                q.contains("ALTER SEQUENCE order_item_id_seq RESTART WITH 1")));
    }

    @Test
    @DisplayName("Executes exactly 11 native queries: 1 truncate + 2 customers + 4 products + 4 sequences")
    void resetData_executesExpectedNumberOfQueries() {
        List<String> queries = captureAllQueries();

        assertEquals(11, queries.size());
    }
}
