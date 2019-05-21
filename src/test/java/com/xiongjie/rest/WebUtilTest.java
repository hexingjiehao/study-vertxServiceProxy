package com.xiongjie.rest;

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
public class WebUtilTest {

    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(WebUtilVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
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
    public void nextHandlerTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                //分块响应
                async.complete();
            });
        });
    }

    @Test
    public void blockHandlerTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/block", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                //有点问题
                async.complete();
            });
        });
    }

    @Test
    public void capPathParaTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/user/xiongjie/24/", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                async.complete();
            });
        });
    }

    @Test
    public void contextTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/context", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                async.complete();
            });
        });
    }

    @Test
    public void subRouterTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/sub/hello", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                async.complete();
            });
        });
    }

    @Test
    public void errorHandlerTest(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(8882, "localhost", "/fail", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                async.complete();
            });
        });
    }

}
