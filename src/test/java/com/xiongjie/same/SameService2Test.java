package com.xiongjie.same;

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
public class SameService2Test {

    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(ServicePublic::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
            deploymentVerticleIds.add(ar);

            vertx.deployVerticle(DiffServiceVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar2 -> {
                deploymentVerticleIds.add(ar2);

                //将http发布为服务
                ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
                discovery.publish(HttpEndpoint.createRecord("world", "localhost", 8883, "/"), publish ->
                    discovery.publish(HttpEndpoint.createRecord("world", "localhost", 8880, "/"), publish2 ->
                        discovery.publish(HttpEndpoint.createRecord("world", "localhost", 8880, "/hello"), publish3 -> {})
                    )
                );
            }));
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
    public void sameService2Test(TestContext context) {
        Async async = context.async();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);

        //创建熔断器对象，随意指定名字
        CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(5)
                        .setTimeout(2000)
                        .setFallbackOnFailure(true)
                        .setResetTimeout(10000)
        ).fallback(v -> "world服务未发现！");

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
