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

import java.util.Map;

/**
 * {@link Span} represents the OpenTracing specification's Span contract.
 *
 * 表示 OpenTracing 中的 span 规范
 *
 * @see Scope
 * @see ScopeManager
 * @see Tracer.SpanBuilder#start()
 * @see Tracer.SpanBuilder#startActive()
 */
public interface Span {
    /**
     * Retrieve(检索/获取) the associated(相关的) SpanContext.
     *
     * This may be called at any time, including after calls to finish().
     *
     * 这个方法可以在任何时候调用，包括在调用了 Span#finish()的时候
     * @return the SpanContext that encapsulates Span state that should propagate across process boundaries.
     */
    SpanContext context();

    /**
     * Set a key:value tag on the Span.
     */
    Span setTag(String key, String value);

    /** Same as {@link #setTag(String, String)}, but for boolean values. */
    Span setTag(String key, boolean value);

    /** Same as {@link #setTag(String, String)}, but for numeric values. */
    Span setTag(String key, Number value);

    /**
     * Log key:value pairs to the Span with the current walltime(全时工作的) timestamp.
     *
     * <p><strong>CAUTIONARY NOTE:</strong> not all Tracer implementations support key:value log fields end-to-end.
     * 并不是所有的Tracer实现都支持 key:value 的 端到端 日志字段
     * Caveat emptor(概不退货、买者自负、完全保障).
     *
     * <p>A contrived(人为的) example (using Guava, which is not required):
     * <pre><code>
     span.log(
     ImmutableMap.Builder<String, Object>()
     .put("event", "soft error")
     .put("type", "cache timeout")
     .put("waited.millis", 1500)
     .build());
     </code></pre>
     *
     * @param fields key:value log fields. Tracer implementations should support String, numeric, and boolean values;
     *               some may also support arbitrary Objects.
     * @return the Span, for chaining
     * @see Span#log(String)
     */
    Span log(Map<String, ?> fields);

    /**
     * Like log(Map&lt;String, Object&gt;), but with an explicit timestamp.
     *
     * <p><strong>CAUTIONARY NOTE:</strong> not all Tracer implementations support key:value log fields end-to-end.
     * Caveat emptor.
     *
     * @param timestampMicroseconds The explicit timestamp for the log record. Must be greater than or equal to the
     *                              Span's start timestamp.
     * @param fields key:value log fields. Tracer implementations should support String, numeric, and boolean values;
     *               some may also support arbitrary Objects.
     * @return the Span, for chaining
     * @see Span#log(long, String)
     */
    Span log(long timestampMicroseconds, Map<String, ?> fields);

    /**
     * Record an event at the current walltime timestamp.
     *
     * Shorthand for
     *
     * <pre><code>
     span.log(Collections.singletonMap("event", event));
     </code></pre>
     *
     * @param event the event value; often a stable identifier for a moment in the Span lifecycle
     * @return the Span, for chaining
     */
    Span log(String event);

    /**
     * Record an event at a specific timestamp.
     *
     * Shorthand for
     *
     * <pre><code>
     span.log(timestampMicroseconds, Collections.singletonMap("event", event));
     </code></pre>
     *
     * @param timestampMicroseconds The explicit timestamp for the log record. Must be greater than or equal to the
     *                              Span's start timestamp.
     * @param event the event value; often a stable identifier for a moment in the Span lifecycle
     * @return the Span, for chaining
     */
    Span log(long timestampMicroseconds, String event);

    /**
     * Sets a baggage item in the Span (and its SpanContext) as a key/value pair.
     *
     * Baggage enables powerful distributed context propagation functionality where arbitrary application data can be
     * carried along the full path of request execution throughout the system.
     *
     * Note 1: Baggage is only propagated to the future (recursive) children of this SpanContext.
     *         Baggage 只传播到此SpanContext的未来（递归）子节点。
     *
     * Note 2: Baggage is sent in-band with every subsequent local and remote calls, so this feature must be used with
     * care.
     *
     * Baggage 将在每次后续本地和远程 calls（调用时） 时发送，因此必须小心使用此功能。
     *
     * @return this Span instance, for chaining
     */
    Span setBaggageItem(String key, String value);

    /**
     * @return the value of the baggage item identified by the given key, or null if no such item could be found
     */
    String getBaggageItem(String key);

    /**
     * Sets the string name for the logical operation this span represents.
     *
     * @return this Span instance, for chaining
     *
     * 设置此span表示的逻辑运算的字符串名称。
     */
    Span setOperationName(String operationName);

    /**
     *
     * 调用span.finish()方法标志着span的结束，finish方法应该是对应span实例的最后一个调用的方法。
     * 在span中finish方法还只是校验和记录的作用，真正发送span的就是开头提到的tracer，tracer包含了sampler、report等全局的功能
     * 因此通常会在finish中调用了tracer.report(span)方法
     *
     * Sets the end timestamp to now and records the span.
     *
     * <p>With the exception of calls to {@link #context}, this should be the last call made to the span instance.
     * Future calls to {@link #finish} are defined as noops, and future calls to methods other than {@link #context}
     * lead to undefined behavior.
     *
     * @see Span#context()
     */
    void finish();

    /**
     *
     * 这里和上面的finish一样，只是指定了结束的时间。上面的方法中以当前时间作为结束时间
     * Sets an explicit end timestamp and records the span.
     *
     * <p>With the exception of calls to Span.context(), this should be the last call made to the span instance, and to
     * do otherwise leads to undefined behavior.
     *
     * @param finishMicros an explicit finish time, in microseconds since the epoch
     *
     * @see Span#context()
     */
    void finish(long finishMicros);
}
