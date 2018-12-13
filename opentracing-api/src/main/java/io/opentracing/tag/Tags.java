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
package io.opentracing.tag;

/**
 * The following span tags are recommended for instrumentors who are trying to capture more
 * semantic(语义) information about the spans. Tracers may expose additional features based on these
 * standardized data points. Tag names follow a general structure of namespacing.
 *
 * 这里简单说下和logs的区别：
 * 1、Tags，没有时间的概念，不关乎在哪个事件节点发生。只管往 span里面set就可以，通常会放一些当前span中一些属性信息，比如对于 一个MVC 产生的span，tag里面通常会放请求的url,method,请求大小，响应大小，响应的statueCode，当前span的类型等
 * 2、logs，参考Filed中的解释，logs是记录具体在这个span生命周期中某个时间点发生的事件，具有时序性
 *
 * @see <a href="https://github.com/opentracing/specification/blob/master/semantic_conventions.md">https://github.com/opentracing/specification/blob/master/semantic_conventions.md</a>
 */

public final class Tags {
    private Tags() {
    }

    /**
     * A constant for setting the span kind to indicate that it represents a server span.
     * 代表一个 server 类型的 span
     */
    public static final String SPAN_KIND_SERVER = "server";

    /**
     * A constant for setting the span kind to indicate that it represents a client span.
     * 代表一个 client 类型的 span
     */
    public static final String SPAN_KIND_CLIENT = "client";

    /**
     * A constant for setting the span kind to indicate that it represents a producer span, in a messaging scenario.
     * 代表一个 producer 类型的 span
     */
    public static final String SPAN_KIND_PRODUCER = "producer";

    /**
     * A constant for setting the span kind to indicate that it represents a consumer span, in a messaging scenario.
     * 代表一个 consumer 类型的 span
     */
    public static final String SPAN_KIND_CONSUMER = "consumer";

    /**
     * The service name for a span, which overrides any default "service name" property defined
     * in a tracer's config. This tag is meant to only be used when a tracer is reporting spans
     * on behalf of another service (for example, a service mesh reporting on behalf of the services
     * it is proxying). This tag does not need to be used when reporting spans for the service the
     * tracer is running in.
     *
     * @see #PEER_SERVICE
     *
     * span 的 service name。它会覆盖 tracer 配置中定义的任何默认“service name”属性、
     * 此tag 仅在tracer 代表另一个 service reporting spans 时使用(比如, service mesh reporting 代表  services 是代理)
     *
     *
     */
    public static final StringTag SERVICE = new StringTag("service");

    /**
     * HTTP_URL records the url of the incoming request.
     * 当前请求的 url
     */
    public static final StringTag HTTP_URL = new StringTag("http.url");

    /**
     * HTTP_STATUS records the http status code of the response.
     *
     * response返回的状态码
     *
     */
    public static final IntTag HTTP_STATUS = new IntTag("http.status_code");

    /**
     * HTTP_METHOD records the http method. Case-insensitive.
     *
     * HTTP_METHOD http 方法类型
     *
     */
    public static final StringTag HTTP_METHOD = new StringTag("http.method");

    /**
     * PEER_HOST_IPV4 records IPv4 host address of the peer.
     *
     * PEER_HOST_IPV4 记录IPv4对等的主机地址。
     *
     */
    public static final IntOrStringTag PEER_HOST_IPV4 = new IntOrStringTag("peer.ipv4");

    /**
     * PEER_HOST_IPV6 records the IPv6 host address of the peer.
     *
     * PEER_HOST_IPV6 记录IPv6对等的主机地址。
     */
    public static final StringTag PEER_HOST_IPV6 = new StringTag("peer.ipv6");

    /**
     * PEER_SERVICE records the service name of the peer service.
     *
     * PEER_SERVICE 记录对等服务的服务名称
     *
     * @see #SERVICE
     */
    public static final StringTag PEER_SERVICE = new StringTag("peer.service");

    /**
     * PEER_HOSTNAME records the host name of the peer.
     *
     * PEER_HOST_IPV6 记录 host name
     */
    public static final StringTag PEER_HOSTNAME = new StringTag("peer.hostname");

    /**
     * PEER_PORT records the port number of the peer.
     *
     * 记录 port
     */
    public static final IntTag PEER_PORT = new IntTag("peer.port");

    /**
     * SAMPLING_PRIORITY determines the priority of sampling this Span.
     *
     * SAMPLING_PRIORITY 决定 sapn 采样的优先级。
     */
    public static final IntTag SAMPLING_PRIORITY = new IntTag("sampling.priority");

    /**
     * SPAN_KIND hints at the relationship between spans, e.g. client/server.
     *
     * SPAN_KIND 表明 span 之间的关系,例如客户机/服务器。
     */
    public static final StringTag SPAN_KIND = new StringTag("span.kind");

    /**
     * COMPONENT is a low-cardinality identifier of the module, library, or package that is instrumented.
     */
    public static final StringTag COMPONENT = new StringTag("component");

    /**
     * ERROR indicates whether a Span ended in an error state.
     *
     * ERROR 用来标记当前 span 是不是以 error 状态结束的
     */
    public static final BooleanTag ERROR = new BooleanTag("error");

    /**
     * DB_TYPE indicates the type of Database.
     * For any SQL database, "sql". For others, the lower-case database category, e.g. "cassandra", "hbase", or "redis"
     *
     * 数据库类型
     *
     */
    public static final StringTag DB_TYPE = new StringTag("db.type");

    /**
     * DB_INSTANCE indicates the instance name of Database.
     * If the jdbc.url="jdbc:mysql://127.0.0.1:3306/customers", instance name is "customers".
     *
     * 数据库实例名
     */
    public static final StringTag DB_INSTANCE = new StringTag("db.instance");

    /**
     * DB_USER indicates the user name of Database, e.g. "readonly_user" or "reporting_user"
     *
     * 数据库账户名
     */
    public static final StringTag DB_USER = new StringTag("db.user");

    /**
     * DB_STATEMENT records a database statement for the given database type.
     * For db.type="SQL", "SELECT * FROM wuser_table". For db.type="redis", "SET mykey "WuValue".
     *
     * database statement
     *
     */
    public static final StringTag DB_STATEMENT = new StringTag("db.statement");

    /**
     * MESSAGE_BUS_DESTINATION records an address at which messages can be exchanged.
     * E.g. A Kafka record has an associated "topic name" that can be extracted by the instrumented
     * producer or consumer and stored using this tag.
     *
     * 消息总线
     */
    public static final StringTag MESSAGE_BUS_DESTINATION = new StringTag("message_bus.destination");
}
