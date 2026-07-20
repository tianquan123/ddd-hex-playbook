package __DDD_BASE_PACKAGE__.infra.sampleorder.adapter;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.mapper.SampleOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SampleOrderPersistenceAdapterTest.Configuration.class)
@ContextConfiguration(classes = SampleOrderPersistenceAdapterTest.Configuration.class)
class SampleOrderPersistenceAdapterTest {

    @Autowired
    private SampleOrderPersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTable() {
        jdbcTemplate.update("delete from sample_order");
    }

    @Test
    void adds_and_reconstitutes_order() {
        SampleOrder saved = adapter.add(
                SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2));

        assertEquals(SampleOrderStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getCreateTime());
        assertEquals(saved.getCreateTime(), saved.getUpdateTime());

        Optional<SampleOrder> loaded = adapter.findById(new SampleOrderId("order-1"));
        assertTrue(loaded.isPresent());
        assertEquals("SKU-1", loaded.orElseThrow().getProductCode());
    }

    @Test
    void updates_domain_state() {
        SampleOrder saved = adapter.add(
                SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2));
        saved.confirm();

        SampleOrder updated = adapter.update(saved);

        assertEquals(SampleOrderStatus.CONFIRMED, updated.getStatus());
        assertEquals(SampleOrderStatus.CONFIRMED,
                adapter.findById(new SampleOrderId("order-1")).orElseThrow().getStatus());
    }

    @Test
    void returns_view_without_leaking_domain_or_data_object() {
        adapter.add(SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2));

        SampleOrderView view = adapter.find(new SampleOrderCriteria("order-1")).orElseThrow();

        assertEquals("order-1", view.getId());
        assertEquals(SampleOrderStatus.PENDING, view.getStatus());
    }

    @Test
    void pages_by_status_with_stable_metadata() {
        adapter.add(SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2));
        SampleOrder confirmed = adapter.add(
                SampleOrder.create(new SampleOrderId("order-2"), "SKU-2", 1));
        confirmed.confirm();
        adapter.update(confirmed);

        PageView<SampleOrderView> page = adapter.page(
                new SampleOrderPageCriteria(SampleOrderStatus.CONFIRMED, 1, 20));

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getPageNum());
        assertEquals(20, page.getPageSize());
        assertEquals("order-2", page.getRecords().getFirst().getId());
    }

    @Test
    void rejects_update_for_missing_row() {
        SampleOrder missing = SampleOrder.create(new SampleOrderId("missing"), "SKU-1", 2);
        missing.confirm();

        assertThrows(SampleOrderNotFoundException.class, () -> adapter.update(missing));
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    @MapperScan(basePackageClasses = SampleOrderMapper.class)
    static class Configuration {
        @Bean
        SampleOrderPersistenceAdapter sampleOrderPersistenceAdapter(SampleOrderMapper mapper) {
            return new SampleOrderPersistenceAdapter(mapper);
        }
    }
}
