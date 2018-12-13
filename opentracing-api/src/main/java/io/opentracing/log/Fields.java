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
package io.opentracing.log;

/**
 * The following log fields are recommended(推荐) for instrumentors(组件、仪器) who are trying to capture(捕获) more
 * information about a logged event(已记录的事件). Tracers may expose(暴露) additional(更多的/额外的) features(功能/特性) based on these
 * standardized(标准化) data points.
 *
 * 结合 Span 里面的 log 方法,log方法提供的能力就是将这些 Fields 添加到 span 中。
 * 每个Span日志都有一个特定的时间戳（必须介于 Span 的开始和结束时间戳之间）和一个或多个字段。
 * 这些个log事件会出现在这个span开始时间到结束时间这段时间里,下面这个span中的Exception就表示在某个时间点发生了 event="error"  error.kind=Exception的事件
 *
 * |start| - - - - Exception - - - - |end|
 *
 * @see <a href="https://github.com/opentracing/specification/blob/master/semantic_conventions.md">https://github.com/opentracing/specification/blob/master/semantic_conventions.md</a>
 */
public class Fields {
    private Fields() {
    }

    /**
     * The type or "kind" of an error (only for event="error" logs). E.g., "Exception", "OSError"
     *
     */
    public static final String ERROR_KIND = "error.kind";

    /**
     * The actual Throwable/Exception/Error object instance itself. E.g., A java.lang.UnsupportedOperationException instance
     */
    public static final String ERROR_OBJECT = "error.object";

    /**
     * A stable identifier(标识) for some notable(显著) moment(时刻) in the lifetime of a Span(Span的生命周期内). For instance, a mutex(互斥)
     * lock acquisition(获得) or release(释放) or the sorts of lifetime events(各种生命周期事件) in a browser page load described
     * in the Performance.timing specification(时序规范). E.g.,
     *
     *
     * from Zipkin, "cs", "sr", "ss", or "cr". Or,
     * more generally, "initialized" or "timed out". For errors, "error"
     *
     * 对于zipkin来说，有 "cs", "sr", "ss", or "cr" ，或者更加普遍的事件，比如，初始化，
     * 对于errors，有"error"
     *
     * 这里有必要解释下"cs", "sr", "ss", or "cr"
     *
     * 1、"cs" ：client send，也就是客户端发起，一般可以认为是触发span开始事件
     * 2、"cr" ：client receive ,客户端接受，一般意味着一个span结束
     * 3、"ss" : server send ,服务端发起，这种情况是服务端请求处理完毕，发送结果给客户端的节点事件，这里也通常意味着一个span的结束
     * 4、"sr" : server receive ,服务端接收，服务端接收到请求开始的事件节点，这里通常意味着一个新span的开始
     *
     */
    public static final String EVENT = "event";

    /**
     * A concise(简洁的), human-readable(可读的), one-line(单行的) message explaining(解释) the event. E.g., "Could not connect
     * to backend", "Cache invalidation succeeded"
     *
     * 例如："Could not connect to backend" 无法连接到后端
     *      "Cache invalidation succeeded" 缓存失效成功
     */
    public static final String MESSAGE = "message";

    /**
     * A stack trace in platform-conventional(常规) format; may or may not pertain(属于) to an error.
     */
    public static final String STACK = "stack";
}
