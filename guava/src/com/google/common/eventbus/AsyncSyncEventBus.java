package com.google.common.eventbus;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus.LoggingHandler;

/**
 * An {@link EventBus} that mix synchronous event dispatching of {@link EventBus} and asynchronous of {@link AsyncEventBus}
 * 
 * In case of synchronous dispatching this is the calling thread that executes the dispatching otherwise it is the specified executor
 * 
 * @author Nicolas Gaud
 *
 */
public class AsyncSyncEventBus extends EventBus {

	private Dispatcher syncDispatcher = Dispatcher.perThreadDispatchQueue();

	public AsyncSyncEventBus(String identifier, Executor executor, Class<? extends Annotation> annotation) {
		super(identifier, executor, Dispatcher.legacyAsync(), LoggingHandler.INSTANCE,annotation);
	}

	public AsyncSyncEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler, Class<? extends Annotation> annotation) {
		super("default", executor, Dispatcher.legacyAsync(), subscriberExceptionHandler,annotation);
	}

	public AsyncSyncEventBus(Executor executor, Class<? extends Annotation> annotation) {
		super("default", executor, Dispatcher.legacyAsync(), LoggingHandler.INSTANCE,annotation);
	}

	public void fire(Object event) {
		Iterator<Subscriber> eventSubscribers = getSubscribers().getSubscribers(event);
		if (eventSubscribers.hasNext()) {
			this.syncDispatcher.dispatch(event, eventSubscribers);
		} else if (!(event instanceof DeadEvent)) {
			// the event had no subscribers and was not itself a DeadEvent
			this.fire(new DeadEvent(this, event));
		}
	}

	/*
	 * public AsyncSyncEventBus(String identifier, Executor executor) { super(identifier, executor); }
	 * 
	 * public AsyncSyncEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler) { super(executor, subscriberExceptionHandler); }
	 * 
	 * public AsyncSyncEventBus(Executor executor) { super(executor);
	 * 
	 * }
	 * 
	 * public void fire(Object event) { Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());
	 * 
	 * boolean dispatched = false; for (Class<?> eventType : dispatchTypes) { subscribersByTypeLock.readLock().lock(); try { Set<EventSubscriber> wrappers = subscribersByType .get(eventType);
	 * 
	 * if (!wrappers.isEmpty()) { dispatched = true; for (EventSubscriber wrapper : wrappers) { eventsToDispatch.get().offer(new EventWithSubscriber(event, wrapper)); } } } finally { subscribersByTypeLock.readLock().unlock(); } }
	 * 
	 * if (!dispatched && !(event instanceof DeadEvent)) { fire(new DeadEvent(this, event)); }
	 * 
	 * fireQueuedEvents(); }
	 * 
	 * void fireQueuedEvents() { // don't dispatch if we're already dispatching, that would allow // reentrancy // and out-of-order events. Instead, leave the events to be dispatched // after the in-progress dispatch is complete. if (isDispatching.get()) { return; }
	 * 
	 * isDispatching.set(true); try { Queue<EventWithSubscriber> events = eventsToDispatch.get(); EventWithSubscriber eventWithSubscriber; while ((eventWithSubscriber = events.poll()) != null) { synchronousDispatch(eventWithSubscriber.event, eventWithSubscriber.subscriber); } } finally { isDispatching.remove(); eventsToDispatch.remove(); } }
	 * 
	 * void synchronousDispatch(Object event, EventSubscriber wrapper) { try { wrapper.handleEvent(event); } catch (InvocationTargetException e) { try { subscriberExceptionHandler.handleException( e.getCause(), new SubscriberExceptionContext(this, event, wrapper .getSubscriber(), wrapper.getMethod())); } catch (Throwable t) { // If the exception handler throws, log it. There isn't much // else to do! Logger.getLogger(AsyncSyncEventBus.class.getName()) .log(Level.SEVERE, String.format( "Exception %s thrown while handling exception: %s", t, e.getCause()), t); } } }
	 */
}
