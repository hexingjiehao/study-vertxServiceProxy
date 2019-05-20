package com.xiongjie.dicovery;

import com.xiongjie.discovery.ServicePublic;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class ServicePublicTest {

    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(ServicePublic::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
            deploymentVerticleIds.add(ar);

            //将http发布为服务
            ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
            discovery.publish(HttpEndpoint.createRecord("world", "localhost", 8883, "/"), publish -> {
                Record record = publish.result();
                System.out.println("http service 发布成功! " + record.getName());
            });
        }));
    }

    @After
    public void destroy(TestContext context) {
        final List<Future> futures = new ArrayList<>();
        for (String id : deploymentVerticleIds) {
            final Future<Void> future = Future.future();
            vertx.undeploy(id, future);
            futures.add(future);
        }
        CompositeFuture.all(futures).setHandler(context.asyncAssertSuccess(ar ->
                vertx.close()
        ));
    }

    @Test
    public void servicePublicTest(TestContext context) {
        Async async = context.async();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        discovery.getRecord(new JsonObject().put("name", "world"), ar -> {

            ServiceReference reference = discovery.getReference(ar.result());
            HttpClient httpClient = reference.getAs(HttpClient.class);

            httpClient.getNow("/", response -> {
                response.handler(body -> {
                    System.out.println(body.toString());
                    reference.release();
                    async.complete();
                });
            });
        });
    }

    /**
     * 测试熔断器1,在具体的操作中设置回退，breaker.executeWithFallback
     */
    @Test
    public void circuitBreakerTest(TestContext context) {
        Async async = context.async();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        //创建熔断器对象，随意指定名字
        CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(10000)
        );

        breaker.executeWithFallback(
                future -> {
                    discovery.getRecord(new JsonObject().put("name", "world"), ar -> {
                        ServiceReference reference = discovery.getReference(ar.result());
                        HttpClient httpClient = reference.getAs(HttpClient.class);
                        httpClient.getNow("/", response -> {
                            response.handler(body -> {
                                future.complete(body.toString());
                                reference.release();
                            });
                        });
                    });
                }, v -> {
                    //服务熔断时进行的操作,当world服务没有发布时
                    return "world服务未发现！";
                })
                .setHandler(ar -> {
                    System.out.println(ar.result());
                    async.complete();
                });

    }


    /**
     * 测试熔断器2，在创建熔断器时设置反馈方法
     */
    @Test
    public void circuitBreakerTest2(TestContext context) {
        Async async = context.async();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        //创建熔断器对象，随意指定名字
        CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(10000)
        ).fallback(v -> {
            return "world服务未发现！";
        });

        breaker.execute(future -> {
            discovery.getRecord(new JsonObject().put("name", "world"), ar -> {
                ServiceReference reference = discovery.getReference(ar.result());
                HttpClient httpClient = reference.getAs(HttpClient.class);
                httpClient.getNow("/", response -> {
                    response.handler(body -> {
                        future.complete(body.toString());
                        reference.release();
                    });
                });
            });
        }).setHandler(ar -> {
            System.out.println(ar.result());
            async.complete();
        });
    }

    /**
     * 测试熔断器3，熔断器的重试次数和状态改变的操作,以及重试策略
     */
    @Test
    public void circuitBreakerTest3(TestContext context) {
        Async async = context.async();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        //创建熔断器对象，随意指定名字
        CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(1000)
        ).openHandler(v1 ->{
            System.out.println("服务没发现，熔断器开启");
        }).closeHandler(v2 ->{
            System.out.println("服务正常工作，熔断器关闭");
        }).halfOpenHandler(v3 ->{
            System.out.println("服务重试中，熔断器半开");
        }).retryPolicy(retryCount ->{
            return retryCount*1L;
        })
        .fallback(v -> {
            return "world服务未发现！";
        });

        breaker.execute(future -> {
            discovery.getRecord(new JsonObject().put("name", "world"), ar -> {
                ServiceReference reference = discovery.getReference(ar.result());
                HttpClient httpClient = reference.getAs(HttpClient.class);
                httpClient.getNow("/", response -> {
                    response.handler(body -> {
                        future.complete(body.toString());
                        reference.release();
                    });
                });
            });
        }).setHandler(ar -> {
            System.out.println(ar.result());
            async.complete();
        });
    }
}
