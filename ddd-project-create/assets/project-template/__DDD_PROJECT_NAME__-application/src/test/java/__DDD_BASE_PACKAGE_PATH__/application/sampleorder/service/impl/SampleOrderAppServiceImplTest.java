package __DDD_BASE_PACKAGE__.application.sampleorder.service.impl;

import __DDD_BASE_PACKAGE__.application.sampleorder.command.CancelSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.ConfirmSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.command.CreateSampleOrderCommand;
import __DDD_BASE_PACKAGE__.application.sampleorder.model.SampleOrderView;
import __DDD_BASE_PACKAGE__.domain.sampleorder.exception.SampleOrderNotFoundException;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrder;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderId;
import __DDD_BASE_PACKAGE__.domain.sampleorder.model.SampleOrderStatus;
import __DDD_BASE_PACKAGE__.domain.sampleorder.repository.SampleOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleOrderAppServiceImplTest {

    @Mock
    private SampleOrderRepository repository;

    private SampleOrderAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SampleOrderAppServiceImpl(repository);
    }

    @Test
    void creates_order_with_generated_identity() {
        LocalDateTime now = LocalDateTime.of(2026, 7, 20, 10, 0);
        when(repository.add(any())).thenAnswer(invocation -> {
            SampleOrder source = invocation.getArgument(0);
            return SampleOrder.reconstitute(source.getId(), source.getProductCode(), source.getQuantity(),
                    source.getStatus(), now, now);
        });

        SampleOrderView result = service.create(new CreateSampleOrderCommand("SKU-1", 2));

        ArgumentCaptor<SampleOrder> captor = ArgumentCaptor.forClass(SampleOrder.class);
        verify(repository).add(captor.capture());
        assertFalse(captor.getValue().getId().getValue().isBlank());
        assertEquals(SampleOrderStatus.PENDING, result.getStatus());
        assertEquals(now, result.getCreateTime());
    }

    @Test
    void confirms_existing_order() {
        SampleOrder order = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);
        when(repository.findById(new SampleOrderId("order-1"))).thenReturn(Optional.of(order));
        when(repository.update(order)).thenReturn(order);

        SampleOrderView result = service.confirm(new ConfirmSampleOrderCommand("order-1"));

        assertEquals(SampleOrderStatus.CONFIRMED, result.getStatus());
        verify(repository).update(order);
    }

    @Test
    void cancels_existing_order() {
        SampleOrder order = SampleOrder.create(new SampleOrderId("order-1"), "SKU-1", 2);
        when(repository.findById(new SampleOrderId("order-1"))).thenReturn(Optional.of(order));
        when(repository.update(order)).thenReturn(order);

        SampleOrderView result = service.cancel(new CancelSampleOrderCommand("order-1"));

        assertEquals(SampleOrderStatus.CANCELLED, result.getStatus());
        verify(repository).update(order);
    }

    @Test
    void rejects_transition_for_missing_order() {
        SampleOrderId id = new SampleOrderId("missing");
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SampleOrderNotFoundException.class,
                () -> service.confirm(new ConfirmSampleOrderCommand("missing")));
        verify(repository, never()).update(any());
    }
}
