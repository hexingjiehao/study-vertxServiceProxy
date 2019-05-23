package com.xiongjie.same;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class SameServiceVerticleTest {

    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(SameServiceVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
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
    public void sameServiceTest(TestContext context) {
        Async async = context.async();
        Vertx vertx=Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        client.get(8881,"localhost","/").send(ar ->{
            System.out.println(ar.result().body());
            async.complete();
        });
    }

}
