package com.xiongjie.rest;

import com.xiongjie.eb.MyProxyService;
import com.xiongjie.eb.MyproxyVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class RouterFactoryTest {

    private MyProxyService myProxyService;
    private Vertx vertx;
    private final Collection<String> deploymentVerticleIds = new ArrayList<>();

    @Before
    public void init(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(MyproxyVerticle::new, new DeploymentOptions(), context.asyncAssertSuccess(ar -> {
            deploymentVerticleIds.add(ar);
            myProxyService = MyProxyService.createProxy(vertx);

            OpenAPI3RouterFactory.create(vertx, "src/main/resources/openAPI.yaml", ar2 -> {
                if (ar2.succeeded()) {
                    OpenAPI3RouterFactory routerFactory = ar2.result();
                    routerFactory.addHandlerByOperationId("sayHello", routingContext -> {
                        //调用服务,进行内部实现
                        myProxyService.sayHello(context.asyncAssertSuccess(res -> {
                            routingContext.response().end(res);
                        }));
                    });

                    Router router = routerFactory.getRouter();
                    vertx.createHttpServer()
                            .requestHandler(router)
                            .listen(8882);
                } else {
                    System.out.println("创建路由工厂失败！");
                }
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

    /**
     * yaml文件中主要是针对URL进行参数和结果的限制
     * @param context
     */
    @Test
    public void routerFactoryTest(TestContext context) {
        Async async = context.async();
        Vertx vertx=Vertx.vertx();
        WebClient client = WebClient.create(vertx);
        client.get(8882,"localhost","/hello").send(ar ->{
            System.out.println(ar.result().body());
            async.complete();
        });
    }
}
