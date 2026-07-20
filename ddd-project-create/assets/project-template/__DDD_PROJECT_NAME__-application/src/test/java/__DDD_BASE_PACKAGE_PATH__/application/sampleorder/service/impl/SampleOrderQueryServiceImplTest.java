package __DDD_BASE_PACKAGE__.application.sampleorder.service.impl;

import __DDD_BASE_PACKAGE__.application.sampleorder.model.PageView;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.application.sampleorder.port.SampleOrderQueryPort;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderCriteria;
import __DDD_BASE_PACKAGE__.application.sampleorder.query.SampleOrderPageCriteria;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleOrderQueryServiceImplTest {

    @Mock
    private SampleOrderQueryPort queryPort;

    private SampleOrderQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SampleOrderQueryServiceImpl(queryPort);
    }

    @Test
    void delegates_single_query_to_query_port() {
        SampleOrderCriteria criteria = new SampleOrderCriteria("order-1");
        Optional<SampleOrderView> expected = Optional.empty();
        when(queryPort.find(criteria)).thenReturn(expected);

        assertSame(expected, service.get(criteria));
        verify(queryPort).find(criteria);
    }

    @Test
    void delegates_page_query_to_query_port() {
        SampleOrderPageCriteria criteria = new SampleOrderPageCriteria(SampleOrderStatus.PENDING, 1, 20);
        PageView<SampleOrderView> expected = new PageView<>(0, 1, 20, List.of());
        when(queryPort.page(criteria)).thenReturn(expected);

        assertSame(expected, service.page(criteria));
        verify(queryPort).page(criteria);
    }
}
