package com.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.gateway.model.Order;
import com.gateway.model.OrderItem;
import com.gateway.exception.PipelineValidationException;
import com.gateway.patterns.builder.OrderBuilder;
import com.gateway.patterns.builder.OrderDirector;
import com.gateway.patterns.mediator.OrderWorkflowMediator;
import com.gateway.patterns.memento.OrderHistory;
import com.gateway.patterns.memento.OrderStateManager;
import com.gateway.patterns.observer.OrderEventObserver;
import com.gateway.patterns.observer.OrderStatusSubject;
import com.gateway.patterns.prototype.OrderTemplateRegistry;
import com.gateway.patterns.prototype.SavedOrderTemplate;
import com.gateway.patterns.strategy.EconomyShipping;
import com.gateway.patterns.strategy.ExpressShipping;
import com.gateway.patterns.strategy.InternationalShipping;
import com.gateway.patterns.strategy.ShippingQuote;
import com.gateway.patterns.strategy.ShippingService;
import com.gateway.patterns.templatemethod.DomesticFulfillmentWorkflow;
import com.gateway.patterns.templatemethod.ExpressFulfillmentWorkflow;
import com.gateway.patterns.templatemethod.FulfillmentWorkflow;
import com.gateway.patterns.templatemethod.FulfillmentWorkflowFactory;
import com.gateway.patterns.templatemethod.InternationalFulfillmentWorkflow;
import com.gateway.repository.OrderRepository;
import com.gateway.service.CustomerNotificationService;
import com.gateway.service.OrderFulfillmentService;

class AdditionalPatternsTests {

	@Test
	void builderCreatesValidStandardAndExpressOrdersAndRejectsIncompleteOrders() {
		OrderDirector director = new OrderDirector();
		Order standard = director.standardOrder(builder -> configure(builder, "standard-1", false, "STANDARD"));
		Order express = director.expressOrder(builder -> configure(builder, "express-1", false, "STANDARD"));

		assertEquals("RECEIVED", standard.getStatus());
		assertEquals("STANDARD", standard.getDeliveryPreference());
		assertEquals("EXPRESS", express.getDeliveryPreference());
		assertEquals(2, standard.getItems().get(0).getQuantity());
		assertThrows(IllegalArgumentException.class, () ->
				director.standardOrder(builder -> builder.id("incomplete")));
	}

	@Test
	void prototypeClonesDeeplyWithFreshIdentityAndResetRuntimeState() {
		Order original = order("template-source", false, "STANDARD");
		original.setStatus("COMPLETED");
		original.setShippingLabel("old label");
		original.setShippingCost(8.0);
		SavedOrderTemplate template = new SavedOrderTemplate("repeat-order", original);
		Order first = template.cloneForNewOrder("clone-1");
		Order second = template.cloneForNewOrder("clone-2");
		first.getItems().get(0).setName("changed clone item");

		assertNotEquals(first.getId(), second.getId());
		assertEquals("RECEIVED", first.getStatus());
		assertEquals(null, first.getShippingLabel());
		assertEquals(null, first.getShippingCost());
		assertEquals("sample item", original.getItems().get(0).getName());
		assertEquals("sample item", second.getItems().get(0).getName());

		OrderTemplateRegistry registry = new OrderTemplateRegistry();
		registry.register("repeat-order", original);
		assertNotEquals(registry.cloneTemplate("repeat-order").getId(),
				registry.cloneTemplate("repeat-order").getId());
	}

	@Test
	void strategyCanSwitchAtRuntimeAndRejectIneligibleRoutes() {
		Order domestic = order("shipping-domestic", false, "STANDARD");
		ShippingService service = new ShippingService(new EconomyShipping());
		ShippingQuote economy = service.calculate(domestic);
		ShippingQuote alternative = new ShippingQuote(16.0, 2, "Alternative Parcel");
		service.setStrategy(ignored -> alternative);
		ShippingQuote switched = service.calculate(domestic);

		assertTrue(switched.cost() > economy.cost());
		assertTrue(switched.estimatedDeliveryDays() < economy.estimatedDeliveryDays());
		assertThrows(IllegalArgumentException.class, () -> new InternationalShipping().calculate(domestic));
		assertThrows(IllegalArgumentException.class, () -> new ExpressShipping().calculate(
				order("not-express", false, "STANDARD")));
		assertTrue(new ExpressShipping().calculate(order("eligible-express", false, "EXPRESS")).cost() > 0);
		assertThrows(IllegalArgumentException.class, () -> new EconomyShipping().calculate(
				order("international-economy", true, "STANDARD")));
	}

	@Test
	void mementoRestoresOnlySafeInternalSnapshotsAndKeepsSnapshotsIsolated() {
		OrderStateManager manager = new OrderStateManager(new OrderHistory());
		Order current = order("checkpointed", false, "STANDARD");
		current.setStatus("RECEIVED");
		var checkpoint = manager.checkpoint(current, "initial");
		current.setStatus("VALIDATED");
		current.getItems().get(0).setName("mutated live order");

		Order restored = manager.restore(current, checkpoint.checkpointId());
		restored.getItems().get(0).setName("mutated restored copy");
		Order restoredAgain = manager.restore(current, checkpoint.checkpointId());
		assertEquals("sample item", restoredAgain.getItems().get(0).getName());
		assertEquals(1, manager.history(current.getId()).size());

		current.setStatus("PAYMENT_PROCESSED");
		current.setStatus("RECEIVED");
		assertThrows(IllegalArgumentException.class,
				() -> manager.restore(current, "not-a-checkpoint"));
		current.setStatus("FAILED");
		assertEquals("RECEIVED", manager.restore(current, checkpoint.checkpointId()).getStatus());
		manager.markExternalOperationStarted(current.getId(), "payment");
		assertThrows(IllegalStateException.class,
				() -> manager.restore(current, checkpoint.checkpointId()));
	}

	@Test
	void templateWorkflowsSelectRegionalDocumentsAndStopOnFailedSteps() {
		OrderStateManager stateManager = new OrderStateManager(new OrderHistory());
		OrderStatusSubject subject = new OrderStatusSubject();
		List<String> statuses = new ArrayList<>();
		OrderEventObserver observer = (id, event, details) -> statuses.add(event);
		subject.addObserver(observer);
		CustomerNotificationService notifications = mock(CustomerNotificationService.class);

		Order domestic = order("domestic-flow", false, "STANDARD");
		domestic.setStatus("PAYMENT_PROCESSED");
		new DomesticFulfillmentWorkflow(new ShippingService(new EconomyShipping()), stateManager,
				subject, notifications).fulfill(domestic, domestic.getId());
		assertEquals(List.of("SHIPPING_CALCULATED", "DOCUMENTS_GENERATED",
				"FULFILLMENT_DISPATCHED", "COMPLETED"), statuses);
		assertNotNull(domestic.getShippingLabel());
		assertNotNull(domestic.getCustomsInvoice());
		assertTrue(domestic.getShippingCost() > 0);
		verify(notifications).sendFulfillmentConfirmation(domestic, domestic.getId());

		Order international = order("international-flow", true, "STANDARD");
		international.setStatus("PAYMENT_PROCESSED");
		new InternationalFulfillmentWorkflow(new ShippingService(new InternationalShipping()), stateManager,
				subject, notifications).fulfill(international, international.getId());
		assertTrue(international.getCustomsInvoice().toLowerCase().contains("customs"));

		Order express = order("express-flow", true, "EXPRESS");
		express.setStatus("PAYMENT_PROCESSED");
		FulfillmentWorkflow expressWorkflow = new ExpressFulfillmentWorkflow(
				new ShippingService(new ExpressShipping()), stateManager, subject, notifications);
		expressWorkflow.fulfill(express, express.getId());
		assertEquals(3, express.getEstimatedDeliveryDays());
		assertThrows(RuntimeException.class, () -> new DomesticFulfillmentWorkflow(
				new ShippingService(new EconomyShipping()), stateManager, subject, notifications)
				.fulfill(order("wrong-region", true, "STANDARD"), "recipient"));
	}

	@Test
	void mediatorCoordinatesSuccessAndStopsAfterPaymentOrFulfillmentFailures() {
		OrderFulfillmentService service = mock(OrderFulfillmentService.class);
		FulfillmentWorkflowFactory workflowFactory = mock(FulfillmentWorkflowFactory.class);
		OrderStateManager stateManager = new OrderStateManager(new OrderHistory());
		FulfillmentWorkflow workflow = mock(FulfillmentWorkflow.class);
		OrderWorkflowMediator mediator = new OrderWorkflowMediator(service, workflowFactory, stateManager);
		Order successfulOrder = order("mediated-success", false, "STANDARD");
		when(workflowFactory.forOrder(successfulOrder)).thenReturn(workflow);
		when(service.processPayment(successfulOrder, "stripe")).thenReturn("Stripe");
		when(service.persist(successfulOrder)).thenReturn(successfulOrder);

		assertEquals(successfulOrder, mediator.submit(successfulOrder, "stripe", "recipient"));
		InOrder orderOfWork = inOrder(service, workflow);
		orderOfWork.verify(service).updateStatus(successfulOrder, "RECEIVED", "Order received");
		orderOfWork.verify(service).validateOrder(successfulOrder);
		orderOfWork.verify(service).updateStatus(successfulOrder, "VALIDATED", "Order passed validation");
		orderOfWork.verify(service).processPayment(successfulOrder, "stripe");
		orderOfWork.verify(workflow).fulfill(successfulOrder, "recipient");
		orderOfWork.verify(service).persist(successfulOrder);

		Order paymentFailure = order("payment-failure", false, "STANDARD");
		when(service.processPayment(paymentFailure, "stripe"))
				.thenThrow(new IllegalStateException("payment unavailable"));
		assertThrows(IllegalStateException.class,
				() -> mediator.submit(paymentFailure, "stripe", paymentFailure.getId()));
		verify(service).markFailed(any(Order.class), any(RuntimeException.class));
		verify(workflowFactory, never()).forOrder(paymentFailure);

		Order fulfillmentFailure = order("fulfillment-failure", false, "STANDARD");
		FulfillmentWorkflow failingWorkflow = mock(FulfillmentWorkflow.class);
		when(service.processPayment(fulfillmentFailure, "stripe")).thenReturn("Stripe");
		when(workflowFactory.forOrder(fulfillmentFailure)).thenReturn(failingWorkflow);
		doThrow(new IllegalStateException("dispatch failed"))
				.when(failingWorkflow).fulfill(fulfillmentFailure, fulfillmentFailure.getId());
		assertThrows(IllegalStateException.class,
				() -> mediator.submit(fulfillmentFailure, "stripe", fulfillmentFailure.getId()));
		verify(service, never()).persist(fulfillmentFailure);

		Order invalidTransition = order("already-paid", false, "STANDARD");
		invalidTransition.setStatus("PAYMENT_PROCESSED");
		assertThrows(IllegalStateException.class,
				() -> mediator.submit(invalidTransition, "stripe", invalidTransition.getId()));
	}

	@Test
	void mediatorIntegratesExistingValidationPaymentProxyDocumentsAndObserver() {
		OrderRepository repository = mock(OrderRepository.class);
		when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		OrderStatusSubject subject = new OrderStatusSubject();
		List<String> statuses = new ArrayList<>();
		subject.addObserver((id, event, details) -> statuses.add(event));
		OrderStateManager stateManager = new OrderStateManager(new OrderHistory());
		OrderFulfillmentService service = new OrderFulfillmentService(repository, subject);
		FulfillmentWorkflowFactory workflowFactory = new FulfillmentWorkflowFactory(
				stateManager, subject, new CustomerNotificationService());
		OrderWorkflowMediator mediator = new OrderWorkflowMediator(service, workflowFactory, stateManager);
		Order order = order("integrated-order", false, "STANDARD");

		Order result = mediator.submit(order, "stripe", "customer");

		assertEquals("COMPLETED", result.getStatus());
		assertNotNull(result.getShippingLabel());
		assertNotNull(result.getCustomsInvoice());
		assertNotNull(result.getShippingCost());
		assertEquals(List.of("RECEIVED", "VALIDATED", "PAYMENT_PROCESSED", "SHIPPING_CALCULATED",
				"DOCUMENTS_GENERATED", "FULFILLMENT_DISPATCHED", "COMPLETED"), statuses);
		verify(repository).save(order);
	}

	@Test
	void validationFailureCanRestoreCheckpointWithoutPretendingPaymentWasReversed() {
		OrderRepository repository = mock(OrderRepository.class);
		when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
		OrderStatusSubject subject = new OrderStatusSubject();
		OrderStateManager stateManager = new OrderStateManager(new OrderHistory());
		OrderFulfillmentService service = new OrderFulfillmentService(repository, subject);
		FulfillmentWorkflowFactory workflowFactory = new FulfillmentWorkflowFactory(
				stateManager, subject, new CustomerNotificationService());
		OrderWorkflowMediator mediator = new OrderWorkflowMediator(service, workflowFactory, stateManager);
		Order order = order("validation-recovery", false, "STANDARD");
		order.setInStock(false);

		assertThrows(PipelineValidationException.class,
				() -> mediator.submit(order, "stripe", order.getId()));
		assertEquals("FAILED", order.getStatus());
		var checkpoint = stateManager.history(order.getId()).get(0);
		assertEquals("RECEIVED", stateManager.restore(order, checkpoint.checkpointId()).getStatus());
		verify(repository).save(order);
	}

	private static Order order(String id, boolean international, String preference) {
		OrderDirector director = new OrderDirector();
		return "EXPRESS".equalsIgnoreCase(preference)
				? director.expressOrder(builder -> configure(builder, id, international, preference))
				: director.standardOrder(builder -> configure(builder, id, international, preference));
	}

	private static void configure(OrderBuilder builder, String id, boolean international, String preference) {
		builder.id(id)
				.amount(20.0)
				.riskScore(10)
				.inStock(true)
				.authenticated(true)
				.international(international)
				.customer("Test Customer", "customer@example.test")
				.shippingAddress("1 Example Street")
				.paymentMethod("stripe")
				.deliveryPreference(preference)
				.notification("SMS", "STANDARD")
				.addItem(new OrderItem("sample item", 2, 10.0, 0.5));
	}
}
