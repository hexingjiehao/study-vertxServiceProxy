package com.xiongjie.rest;

import com.xiongjie.eb.MyproxyVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class MyProxyServiceRestTest {

    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MyProxyRestVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
            deploymentVerticleIds.add(ar);
            vertx.deployVerticle(MyproxyVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar2 -> {
                deploymentVerticleIds.add(ar2);
            }));
        }));

        vertx.deployVerticle(MyproxyVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
            deploymentVerticleIds.add(ar);
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
    public void sayHelloRestTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8884, "localhost", "/", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                async.complete();
            });
        });
    }

}
