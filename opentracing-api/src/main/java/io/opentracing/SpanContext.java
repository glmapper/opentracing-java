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
 * SpanContext represents(表示) Span state that must propagate(传播) to descendant(子、后代) Spans and across process boundaries(跨进程边界).
 *
 * SpanContext is logically(逻辑上) divided(分隔/分开) into two pieces(片、块、部分):
 * (1) the user-level "Baggage" that propagates across Span
 * boundaries and (2) any Tracer-implementation-specific fields that are needed to identify or otherwise contextualize
 * the associated Span instance (e.g., a &lt;trace_id, span_id, sampled&gt; tuple).
 *
 *
 * 1/跨越 Span 边界传播的用户级 “Baggage”
 * 2/任何特定于Tracer实现的字段，用于标识或以其他方式关联Span实例,例如trace_id、span_id、sampled等
 *
 *
 * @see Span#setBaggageItem(String, String)
 * @see Span#getBaggageItem(String)
 */
public interface SpanContext {
    /**
     * @return all zero or more baggage items propagating along with the associated Span
     *
     * 与相关的 Span 一起传播的零个或多个baggage items
     *
     * @see Span#setBaggageItem(String, String)
     * @see Span#getBaggageItem(String)
     */
    Iterable<Map.Entry<String, String>> baggageItems();
}
