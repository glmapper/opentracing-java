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

import io.opentracing.Tracer.SpanBuilder;

/**
 *
 * 该接口提供了将给定的span变为 活动的 span 的功能以及获取当前 活动的 span/scope
 *
 * 在多线程环境下ScopeManager管理着各个线程的Scope，而每个线程中的Scope管理着该线程中的Span。
 * 这样当某个线程需要获取其线程中当前 活动的 span时，可以通过ScopeManager找到对应该线程的Scope，并从Scope中取出该线程 活动的 span
 *
 * 具体实现 ：ThreadLocalScopeManager 等
 *
 * 先来举个例子，对于MVC组件来说，如果我们想使用一个 span 来记录 mvc 的执行过程。一般我可以把span的开始放在 Filter 中，filterChain.doFilter方法执行之前产生，然后再finally块中来结束这个span,大概如下：
 *  // Span span = null      //  1
 *  try{
 *      // to create a new span
 *      span = serverReceive()
 *      // do something
 *      filterChain.doFilter(servletRequest, responseWrapper);
 *      // do something
 *  }finally{
 *      // to finish current span
 *      serverSend();
 *  }
 *
 *  假如现在有个问题是，在serverReceive 和 serverSend 这段过程中涉及到了其他组件也产生了span，比如说发起了一次 httpclient 调用。大概对应的tracer如下：
 *
 *  |mvcSpan|
 *      .
 *      .
 *      |httpclientSpan|
 *            .
 *            .
 *      |httpclientSpan|
 *      .
 *      .
 *  |mvcSpan|
 *
 *  这是典型的 child_of 关系   httpclientSpan child_of mvcSpan 且都在同一个线程中执行。OK，解法：
 *
 *  1、显示的申明一个 span ,如上面代码段中 1 的位置。这样span的作用域足够大，可以在finally 中通过显示的调用 span.finish 来结束。
 *     问题来了，这种情况下，如果我想在httpclientSpan的处理逻辑中使用mvcSpan怎么办呢？通过参数传递？那如果链路很长呢？显然这种方式是不可取的。
 *  2、使用 ThreadLocal 机制，在serverReceive中将当前span放到ThreadLocal中，httpclientSpan 作用时，从ThreadLocal中先拿出mvcSpan，然后作为 httpclientSpan 的父span.此时将httpclientSpan塞到ThreadLocal中。
 *     当httpclientSpan结束时，在将mvcSpan复原到ThreadLocal中。
 *
 *  opentracing 规范中提供的 ScopeManager 接口的实现类 ThreadLocalScopeManager 就是方式2的解法。这种方式可以让span在当前线程中很友好的传递。
 *
 * The {@link ScopeManager} interface abstracts both the activation of {@link Span} instances via
 * {@link ScopeManager#activate(Span, boolean)} and access to an active {@link Span}/{@link Scope}
 * via {@link ScopeManager#active()}.
 *
 * @see Scope
 * @see Tracer#scopeManager()
 */
public interface ScopeManager {

    /**
     * Make a {@link Span} instance active.
     *
     * @param span the {@link Span} that should become the {@link #active()}
     * @param finishSpanOnClose whether span should automatically be finished when {@link Scope#close()} is called
     * @return a {@link Scope} instance to control the end of the active period for the {@link Span}. It is a
     * programming error to neglect to call {@link Scope#close()} on the returned instance.
     *
     * span 注册到 scopeManager 是为了用于建立 span 间的如 parent-child 之类的关系，
     * 当方法嵌套调用 并去我们两个方法都想要进行追踪时，
     * 将 span 注册到 scopeManager 中是必须的，否则就只能将 span 作为方法的参数进行传递。
     *
     * 这个实际上就是将当前传入的这个span放到 ThreadLocal 中的过程。具体实现在 ThreadLocalScopeManager 中和 ThreadLocalScope 中来看
     *
     */
    Scope activate(Span span, boolean finishSpanOnClose);

    /**
     * Return the currently active {@link Scope} which can be used to access the currently active
     * {@link Scope#span()}.
     *
     * 返回当前 active 状态的{@link Scope} ，能够被用于访问当前 active 状态的 {@link Scope#span()}.
     *
     * <p>
     * If there is an {@link Scope non-null scope}, its wrapped {@link Span} becomes an implicit(隐式的) parent
     * (as {@link References#CHILD_OF} reference) of any
     * newly-created(新创建的) {@link Span} at {@link Tracer.SpanBuilder#startActive(boolean)} or {@link SpanBuilder#start()}
     * time rather than at {@link Tracer#buildSpan(String)} time.
     *
     * @return the {@link Scope active scope}, or null if none could be found.
     */
    Scope active();
}
