package org.rapidpm.jumpstart.vaadin.logic.event;

import org.rapidpm.jumpstart.vaadin.logic.event.anotations.HandleEvent;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by b.bosch on 11.12.2015.
 */
public class EventBus {

  private static final Map<Class<? extends Serializable>, SortedSet<EventHandlerInvocation>> HANDLER_MAP = new ConcurrentHashMap<>();
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

  private EventBus() {
  }

  public static void register(Object objectWithHandler) throws EventHandlerException {
    final Class classWithHandler = objectWithHandler.getClass();
    for (final Method handlerMethod : classWithHandler.getMethods()) {
      if (handlerMethod.isAnnotationPresent(HandleEvent.class)) {
        Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
          throw new EventHandlerException("Method with annotation " + HandleEvent.class.getSimpleName() + " can only have 1 parameter.");
        }
        Class<? extends Serializable> eventClass = (Class<? extends Serializable>) parameterTypes[0];
        addHandlerToBus(objectWithHandler, handlerMethod, eventClass);
      }
    }
  }

  private static void addHandlerToBus(Object objectWithHandler, Method handlerMethod, Class<? extends Serializable> eventClass) {
    HANDLER_MAP.compute(eventClass, (key, eventHandlerInvocations) ->
    {
      if (eventHandlerInvocations == null) {
        eventHandlerInvocations = new ConcurrentSkipListSet<>();
      }
      eventHandlerInvocations.add(new EventHandlerInvocation(objectWithHandler, handlerMethod));
      return eventHandlerInvocations;
    });
  }

  public static void unregister(Object objectWithHandler) {
    final Class classWithHandler = objectWithHandler.getClass();
    for (final Method handlerMethod : classWithHandler.getMethods()) {
      if (handlerMethod.isAnnotationPresent(HandleEvent.class)) {
        Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
          throw new EventHandlerException("Method with annotation " + HandleEvent.class.getSimpleName() + " can only have 1 parameter.");
        }
        Class<? extends Serializable> eventClass = (Class<? extends Serializable>) parameterTypes[0];
        SortedSet<EventHandlerInvocation> eventHandlerInvocations = HANDLER_MAP.get(eventClass);
        eventHandlerInvocations.stream().filter(eventHandlerInvocation -> eventHandlerInvocation.callsHandlerOf(objectWithHandler))
                .forEach(deadInvocation -> eventHandlerInvocations.remove(deadInvocation));
      }
    }
  }

  public static void fireSynchronousEvent(Serializable event) {
    final SortedSet<EventHandlerInvocation> handlers = HANDLER_MAP.getOrDefault(event.getClass(), new ConcurrentSkipListSet<>());
    handlers.forEach(eventHandlerDelegator -> eventHandlerDelegator.invokeHandler(event));
  }

  public static void fireAsyncEvent(Serializable event) {
    final SortedSet<EventHandlerInvocation> handlers = HANDLER_MAP.getOrDefault(event.getClass(), new ConcurrentSkipListSet<>());
    handlers.forEach(eventHandlerDelegator -> EXECUTOR_SERVICE.submit(() -> eventHandlerDelegator.invokeHandler(event)));
  }

}
