package com.xiongjie.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import java.util.Random;

public class WebUtilVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        //Router不等于Route
        Router router = Router.router(vertx);
        Route route = router.route("/");
        route.handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.setChunked(true);  //设置分块
            response.write("good1\n"); //写入数据
            //1秒之后调用下一个handler
            routingContext.vertx().setTimer(1000, tid -> routingContext.next());
        }).handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("good2\n");
            routingContext.vertx().setTimer(1000, tid -> routingContext.next());
        }).handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.write("good3\n");
            routingContext.response().end();
        });

        //阻塞块这里有点疑惑
        Route route2 = router.route("/block");
        route2.blockingHandler(routingContext -> {
            System.out.println("睡眠1秒");
            System.out.println("睡眠结束");
            routingContext.request().setExpectMultipart(true);
            routingContext.next();
        }).handler(routingContext -> {
            routingContext.response().write("block\n");
            routingContext.response().end();
        });

        /**
         * 注意：一下路径是相同效果，/的多少
         * /a/b
         * /a/b/
         * /a/b//
         *
         * 另外：/a/* 表示/a目录下的所有后缀
         *
         * route.order(数字)，表示同一个请求的响应结果的执行先后顺序
         */
        Route route3 = router.route(HttpMethod.GET,"/user/:name/:age/");
        route3.handler(routingContext -> {
            String name = routingContext.request().getParam("name");
            String age = routingContext.request().getParam("age");
            System.out.println("name=" + name + ",age=" + age);
            routingContext.response().end("name=" + name + ",age=" + age);
        });

        //请求头的类型
        Route route4 = router.route().consumes("text/plain");
        route4.handler(routingContext -> {
            routingContext.response().end("请求头为text/plain的路由被调用");
        });

        //路径参数传递
        router.get("/context").handler(routingContext -> {
            routingContext.put("good","morning");
            routingContext.next();
        });
        router.get("/context").handler(routingContext -> {
            String res=routingContext.get("good");
            routingContext.response().end(res);
        });

        //子路由的使用
        Router router2 = Router.router(vertx);
        router2.get("/hello").handler(routingContext -> {
           routingContext.response().end("hello,subRouter");
        });
        router.mountSubRouter("/sub",router2);

        //重新路由
        router.get("/notfound").handler(routingContext ->{
            String res=routingContext.get("var");
            routingContext.response().setStatusCode(404).end("NOT FOUND "+res);
        });

        router.get("/reroute").handler(ctx ->{
           if(ctx.statusCode()==-1){
               ctx.put("var","hello").reroute("/notfound");
           }else{
               ctx.response().end("重路由正常");
           }
        });

        //异常处理
        router.get("/fail").handler(ctx ->{
            int random=new Random().nextInt(10);
            System.out.println("随机数："+random);
            if(random>5){
                ctx.response().end("正常执行");
            }else{
                throw new RuntimeException("异常发生");
            }
        });
        //只有异常时才调用
        router.get("/fail").failureHandler(ctx ->{
            int statusCode = ctx.statusCode();
            System.out.println("异常状态码"+statusCode);
            ctx.response().end("Sorry! Not today");

        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8882);
    }

}
