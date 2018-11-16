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
package io.opentracing;

import io.opentracing.propagation.Format;

/**
 * Tracer is a simple, thin interface for Span creation and propagation across arbitrary transports.
 */
public interface Tracer {

    /**
     * @return the current {@link ScopeManager}, which may be a noop but may not be null.
     *
     * 返回当前ScopeManager;可能返回的是null
     */
    ScopeManager scopeManager();

    /**
     * @return the active {@link Span}. This is a shorthand for Tracer.scopeManager().active().span(),
     * and null will be returned if {@link Scope#active()} is null.
     *
     * 返回一个 active 状态的 span；实际上是 Tracer.scopeManager().active().span() 的简写，可能返回的是null
     */
    Span activeSpan();

    /**
     *
     * 返回一个SpanBuilder，用来构建span，并且制定了当前 span 的 operationName
     *
     *
     * Return a new SpanBuilder for a Span with the given `operationName`.
     *
     * <p>You can override the operationName later via {@link Span#setOperationName(String)}.
     *
     * <p>A contrived example:
     * <pre><code>
     *   Tracer tracer = ...
     *
     *   // Note: if there is a `tracer.active()` Scope, its `span()` will be used as the target
     *   // of an implicit CHILD_OF Reference for "workScope.span()" when `startActive()` is invoked.
     *   try (Scope workScope = tracer.buildSpan("DoWork").startActive()) {
     *       workScope.span().setTag("...", "...");
     *       // etc, etc
     *   }
     *
     *   // It's also possible to create Spans manually, bypassing the ScopeManager activation.
     *   Span http = tracer.buildSpan("HandleHTTPRequest")
     *                     .asChildOf(rpcSpanContext)  // an explicit parent
     *                     .withTag("user_agent", req.UserAgent)
     *                     .withTag("lucky_number", 42)
     *                     .start();
     * </code></pre>
     */
    SpanBuilder buildSpan(String operationName);

    /**
     * Inject a SpanContext into a `carrier` of a given type, presumably for propagation across process boundaries.
     *
     * <p>Example:
     * <pre><code>
     * Tracer tracer = ...
     * Span clientSpan = ...
     * TextMap httpHeadersCarrier = new AnHttpHeaderCarrier(httpRequest);
     * tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, httpHeadersCarrier);
     * </code></pre>
     *
     * @param <C> the carrier type, which also parametrizes the Format.
     * @param spanContext the SpanContext instance to inject into the carrier
     * @param format the Format of the carrier
     * @param carrier the carrier for the SpanContext state. All Tracer.inject() implementations must support
     *                io.opentracing.propagation.TextMap and java.nio.ByteBuffer.
     *
     * @see io.opentracing.propagation.Format
     * @see io.opentracing.propagation.Format.Builtin
     */
    <C> void inject(SpanContext spanContext, Format<C> format, C carrier);

    /**
     * Extract a SpanContext from a `carrier` of a given type, presumably after propagation across a process boundary.
     *
     * <p>Example:
     * <pre><code>
     * Tracer tracer = ...
     * TextMap httpHeadersCarrier = new AnHttpHeaderCarrier(httpRequest);
     * SpanContext spanCtx = tracer.extract(Format.Builtin.HTTP_HEADERS, httpHeadersCarrier);
     * ... = tracer.buildSpan('...').asChildOf(spanCtx).startActive();
     * </code></pre>
     *
     * If the span serialized state is invalid (corrupt, wrong version, etc) inside the carrier this will result in an
     * IllegalArgumentException. If the span serialized state is missing the method returns null.
     *
     * @param <C> the carrier type, which also parametrizes the Format.
     * @param format the Format of the carrier
     * @param carrier the carrier for the SpanContext state. All Tracer.extract() implementations must support
     *                io.opentracing.propagation.TextMap and java.nio.ByteBuffer.
     *
     * @return the SpanContext instance holding context to create a Span, null otherwise.
     *
     * @see io.opentracing.propagation.Format
     * @see io.opentracing.propagation.Format.Builtin
     */
    <C> SpanContext extract(Format<C> format, C carrier);


    /**
     * 基于 builder 模式来构建 Tracer
     */
    interface SpanBuilder {

        /**
         * A shorthand for addReference(References.CHILD_OF, parent).
         *
         * <p>
         * If parent==null, this is a noop.
         */
        SpanBuilder asChildOf(SpanContext parent);

        /**
         * A shorthand for addReference(References.CHILD_OF, parent.context()).
         *
         * <p>
         * If parent==null, this is a noop.
         */
        SpanBuilder asChildOf(Span parent);

        /**
         * Add a reference from the Span being built to a distinct (usually parent) Span. May be called multiple times
         * to represent multiple such References.
         *
         * <p>
         * If
         * <ul>
         * <li>the {@link Tracer}'s {@link ScopeManager#active()} is not null, and
         * <li>no <b>explicit</b> references are added via {@link SpanBuilder#addReference}, and
         * <li>{@link SpanBuilder#ignoreActiveSpan()} is not invoked,
         * </ul>
         * ... then an inferred {@link References#CHILD_OF} reference is created to the
         * {@link ScopeManager#active()} {@link SpanContext} when either {@link SpanBuilder#startActive(boolean)} or
         * {@link SpanBuilder#start} is invoked.
         *
         * @param referenceType the reference type, typically one of the constants defined in References
         * @param referencedContext the SpanContext being referenced; e.g., for a References.CHILD_OF referenceType, the
         *                          referencedContext is the parent. If referencedContext==null, the call to
         *                          {@link #addReference} is a noop.
         *
         * @see io.opentracing.References
         */
        SpanBuilder addReference(String referenceType, SpanContext referencedContext);

        /**
         * Do not create an implicit {@link References#CHILD_OF} reference to the {@link ScopeManager#active()}).
         */
        SpanBuilder ignoreActiveSpan();

        /** Same as {@link Span#setTag(String, String)}, but for the span being built. */
        SpanBuilder withTag(String key, String value);

        /** Same as {@link Span#setTag(String, boolean)}, but for the span being built. */
        SpanBuilder withTag(String key, boolean value);

        /** Same as {@link Span#setTag(String, Number)}, but for the span being built. */
        SpanBuilder withTag(String key, Number value);

        /** Specify a timestamp of when the Span was started, represented in microseconds since epoch. */
        SpanBuilder withStartTimestamp(long microseconds);

        /**
         * Returns a newly started and activated {@link Scope}.
         *
         * <p>
         * The returned {@link Scope} supports try-with-resources. For example:
         * <pre><code>
         *     通过tracer 构造了一个spanName为 "..." 的span并将其启动与激活，且当scope close时，span也会finish。
         *     try (Scope scope = tracer.buildSpan("...").startActive(true)) {
         *         // (Do work)
         *         scope.span().setTag( ... );  // etc, etc
         *     }
         *     // Span does finishes automatically only when 'finishSpanOnClose' is true
         * </code></pre>
         *
         * <p>
         * If
         * <ul>
         * <li>the {@link Tracer}'s {@link ScopeManager#active()} is not null, and
         * <li>no <b>explicit</b> references are added via {@link SpanBuilder#addReference}, and
         * <li>{@link SpanBuilder#ignoreActiveSpan()} is not invoked,
         * </ul>
         * ... then an inferred {@link References#CHILD_OF} reference is created to the
         * {@link ScopeManager#active()}'s {@link SpanContext} when either
         * {@link SpanBuilder#start()} or {@link SpanBuilder#startActive} is invoked.
         *
         * <p>
         * Note: {@link SpanBuilder#startActive(boolean)} is a shorthand for
         * {@code tracer.scopeManager().activate(spanBuilder.start(), finishSpanOnClose)}.
         *
         * @param finishSpanOnClose whether span should automatically be finished when {@link Scope#close()} is called
         *         set true :在scope.close()时同时调用span.finish()，这样就会导致我们所追踪的方法catch到异常或要在catch与finally中添加tag变得不可能，
         *                   因为在scope.close()后我们已经无法获取到那个包裹着我们所需要的span的scope了，也就无法通过scope获取到span。
         *
         *         set false:导致由于无法获取到当时的scope而无法获取到想要的span的情况
         *                   如果我们设置为false，那个span是没有finish的，而我们又无法获取到当时的span了，
         *                   所以除非我们在try语句中手动的设置scope.span.finish()，否则span永远没有finish。
         *
         *
         *
         *          opentracing推荐的方式是start一个span后，在需要的时候active激活它，也就是注册到scopeManager中
         *              Span span = tracer.buildSpan("someWork").start();
         *              try (Scope scope = tracer.scopeManager().activate(span, false)) {
         *                 // Do things.
         *              } catch(Exception ex) {
         *                 Tags.ERROR.set(span, true);
         *                 span.log(Map.of(Fields.EVENT, "error", Fields.ERROR_OBJECT, ex, Fields.MESSAGE, ex.getMessage()));
         *              } finally {
         *                 // 手动finish
         *                 span.finish();
         *              }
         *
         *
         *          这里可以想到的是，如果finishSpanOnClose不设置为false。
         *          则会在 try 块中 finish span ，那么这样一来就会造成我们在后面的catch 或者 finally块中无法在对这个span进行操作了，因为scope#close时，span#finish了
         *
         *
         *
         * @return a {@link Scope}, already registered via the {@link ScopeManager}
         *
         * @see ScopeManager
         * @see Scope
         */
        Scope startActive(boolean finishSpanOnClose);

        /**
         * @deprecated use {@link #start} or {@link #startActive} instead.
         */
        @Deprecated
        Span startManual();

        /**
         * Like {@link #startActive()}, but the returned {@link Span} has not been registered via the
         * {@link ScopeManager}.
         *
         * @see SpanBuilder#startActive(boolean)
         * @return the newly-started Span instance, which has *not* been automatically registered
         *         via the {@link ScopeManager}
         */
        Span start();
    }
}
