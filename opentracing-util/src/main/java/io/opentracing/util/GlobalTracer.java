/*
 * Copyright 2016-2018 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.opentracing.util;

import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.propagation.Format;

import java.util.concurrent.Callable;

/**
 * Global tracer that forwards all methods to another tracer that can be
 * configured by calling {@link #register(Tracer)}.
 *
 * GlobalTracer 将所有的方法转发到另外一个 tracer，这个tracer 可以通过 GlobalTracer#register(Tracer) 进行注册
 *
 * <p>
 * The {@linkplain #register(Tracer) register} method should only be called once
 * during the application initialization phase.<br>
 * If the {@linkplain #register(Tracer) register} method is never called,
 * the default {@link NoopTracer} is used.
 *
 * register 方法只会在应用初始化阶段被调用一次，如果register方法没有被调用，则默认使用 NoopTracer 带代替GlobalTracer实例
 *
 *
 *
 * <p>
 * Where possible, use some form of dependency injection (of which there are
 * many) to access the `Tracer` instance. For vanilla application code, this is
 * often reasonable and cleaner for all of the usual DI reasons.
 *
 * 在可能的情况下，使用某种形式的依赖注入（其中有很多）来访问Tracer实例。对于vanilla（平常的）应用程序代码，对于所有常见的DI原因，这通常是合理且更清晰的。
 *
 * <p>
 * That said, instrumentation for packages that are themselves statically
 * configured (e.g., JDBC drivers) may be unable to make use of said DI
 * mechanisms for {@link Tracer} access, and as such they should fall back on
 * {@link GlobalTracer}. By and large, OpenTracing instrumentation should
 * always allow the programmer to specify a {@link Tracer} instance to use for
 * instrumentation, though the {@link GlobalTracer} is a reasonable fallback or
 * default value.
 *
 * 也就是说，本身静态配置的软件包（例如，JDBC驱动程序）的工具可能无法利用所述DI机制进行Tracer访问，因此它们应该回退到GlobalTracer上。
 * 总的来说，OpenTracing instrumentation 应始终允许程序员去指定Tracer 实例 用于检测，尽管GlobalTracer是合理的回退或默认值（NoopTracer）。
 *
 */
public final class GlobalTracer implements Tracer {

    /**
     * Singleton instance.
     * <p>
     * Since we cannot prevent people using {@linkplain #get() GlobalTracer.get()} as a constant,
     * this guarantees that references obtained before, during or after initialization
     * all behave as if obtained <em>after</em> initialization once properly initialized.<br>
     * As a minor additional benefit it makes it harder to circumvent the {@link Tracer} API.
     *
     * 因为我们不能阻止人们使用GlobalTracer.get()作为一个常量。
     * 这保证了在初始化之前，期间或之后获得的引用都表现得好像在初始化之后初始化之后获得的那样。
     * 这样做带来的一个小小的好处是它难以规避 Tracer 的 API。
     *
     */
    private static final GlobalTracer INSTANCE = new GlobalTracer();

    /**
     * The registered {@link Tracer} delegate or the {@link NoopTracer} if none was registered yet.
     * Never {@code null}.
     */
    private static volatile Tracer tracer = NoopTracerFactory.create();

    private GlobalTracer() {
    }

    /**
     * Returns the constant {@linkplain GlobalTracer}.
     * <p>
     * All methods are forwarded to the currently configured tracer.<br>
     * Until a tracer is {@link #register(Tracer) explicitly configured},
     * the {@link io.opentracing.noop.NoopTracer NoopTracer} is used.
     *
     * @return The global tracer constant.
     * @see #register(Tracer)
     *
     *
     * 所有方法都转发到当前配置的tracer
     */
    public static Tracer get() {
        return INSTANCE;
    }

    /**
     * Identify whether a {@link Tracer} has previously been registered.
     * <p>
     * This check is useful in scenarios where more than one component may be responsible
     * for registering a tracer. For example, when using a Java Agent, it will need to determine
     * if the application has already registered a tracer, and if not attempt to resolve and
     * register one itself.
     *
     * @return Whether a tracer has been registered
     */
    public static synchronized boolean isRegistered() {
        // 不是 NoopTracer 类型即表明当前 tracer 是被注册过的，因为默认是 NoopTracer
        return !(GlobalTracer.tracer instanceof NoopTracer);
    }

    /**
     * Register a {@link Tracer} to back the behaviour of the {@link #get() global tracer}.
     * <p>
     * The tracer is provided through a {@linkplain Callable} that will only be called if the global tracer is absent.
     * Registration is a one-time operation. Once a tracer has been registered, all attempts at re-registering
     * will return {@code false}.
     * <p>
     * Every application intending to use the global tracer is responsible for registering it once
     * during its initialization.
     *
     * @param provider Provider for the tracer to use as global tracer.
     * @return {@code true} if the provided tracer was registered as a result of this call,
     * {@code false} otherwise.
     * @throws NullPointerException  if the tracer provider is {@code null} or provides a {@code null} Tracer.
     * @throws RuntimeException      any exception thrown by the provider gets rethrown,
     *                               checked exceptions will be wrapped into appropriate runtime exceptions.
     *
     *  注册Tracer以支持 global tracer 的行为.这个tracer 通过 Callable提供，并且只有在缺少 global tracer 时才会调用。
     *  注册是一次性操作,只要tracer被注册了，所有重新注册的尝试都将返回false。
     *  每个打算使用 global tracer 的应用程序都负责在初始化期间注册一次。
     *
     */
    public static synchronized boolean registerIfAbsent(final Callable<Tracer> provider) {
        requireNonNull(provider, "Cannot register GlobalTracer from provider <null>.");
        if (!isRegistered()) {
            try {
                final Tracer suppliedTracer = requireNonNull(provider.call(), "Cannot register GlobalTracer <null>.");
                if (!(suppliedTracer instanceof GlobalTracer)) {
                    GlobalTracer.tracer = suppliedTracer;
                    return true;
                }
            } catch (RuntimeException rte) {
                throw rte; // Re-throw as-is
            } catch (Exception ex) {
                throw new IllegalStateException("Exception obtaining tracer from provider: " + ex.getMessage(), ex);
            }
        }
        return false;
    }

    /**
     * Register a {@link Tracer} to back the behaviour of the {@link #get() global tracer}.
     * <p>
     * Registration is a one-time operation, attempting to call it more often will result in a runtime exception.
     * <p>
     * Every application intending to use the global tracer is responsible for registering it once
     * during its initialization.
     *
     * @param tracer Tracer to use as global tracer.
     * @throws RuntimeException if there is already a current tracer registered
     * @see #registerIfAbsent(Callable)
     * @deprecated Please use 'registerIfAbsent' instead which does not attempt a double registration.
     *
     * 这个和上面一样，register 中调用了 registerIfAbsent
     *
     */
    @Deprecated
    public static void register(final Tracer tracer) {
        if (!registerIfAbsent(provide(tracer))
                && !tracer.equals(GlobalTracer.tracer)
                && !(tracer instanceof GlobalTracer)) {
            throw new IllegalStateException("There is already a current global Tracer registered.");
        }
    }

    @Override
    public ScopeManager scopeManager() {
        return tracer.scopeManager();
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return tracer.buildSpan(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        tracer.inject(spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return tracer.extract(format, carrier);
    }

    @Override
    public Span activeSpan() {
        return tracer.activeSpan();
    }

    @Override
    public String toString() {
        return GlobalTracer.class.getSimpleName() + '{' + tracer + '}';
    }

    private static Callable<Tracer> provide(final Tracer tracer) {
        return new Callable<Tracer>() {
            public Tracer call() {
                return tracer;
            }
        };
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
}
