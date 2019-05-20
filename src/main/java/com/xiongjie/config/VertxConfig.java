package com.xiongjie.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class VertxConfig {

    public static void main(String[] args) {
        jsonConfig();
        yamlConfig();
        propertiesConfig();
        listenerConfig();
        streamConfig();
        futureConfig();
    }

    private static void jsonConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions json = new ConfigStoreOptions()
                .setType("json")
                .setConfig(new JsonObject().put("key", "value"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(json);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        retriever.getConfig(ar ->{
            if (ar.failed()) {
                System.out.println("失败："+ar.result());
            }else{
                System.out.println("成功："+ar.result());
            }
            vertx.close();
        });
    }

    //需要存放在classpath下
    private static void yamlConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions yaml = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "xiongjie.yaml"));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(yaml);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        retriever.getConfig(ar ->{
            if (ar.failed()) {
                System.out.println("失败："+ar.result());
            }else{
                System.out.println("成功："+ar.result());
            }
            vertx.close();
        });
    }

    private static void propertiesConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions properties = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "hexingjie.properties")
                                           .put("raw-data", true)
                          );

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(properties);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        retriever.getConfig(ar ->{
            if (ar.failed()) {
                System.out.println("失败："+ar.result());
            }else{
                System.out.println("成功："+ar.result());
            }
            vertx.close();
        });
    }

    //暂时没用到
    private static void listenerConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions properties = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "listener.properties")
                        .put("raw-data", true)
                );

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(properties);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        System.out.println("cacheConfig: "+retriever.getCachedConfig());
        retriever.listen(listen ->{
            JsonObject previous=listen.getPreviousConfiguration();
            JsonObject json=listen.getPreviousConfiguration();
            System.out.println("旧的："+previous);
            System.out.println("新的："+json);
            vertx.close();
        });
    }

    //用的少
    private static void streamConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions properties = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "listener.properties")
                        .put("raw-data", true)
                );

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .setScanPeriod(2000)
                .addStore(properties);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        retriever.configStream()
            .endHandler(v1 ->{
                System.out.println("检索器关闭："+v1);
            })
            .exceptionHandler(v2 ->{
                System.out.println("检索器异常："+v2.getMessage());
            })
            .handler(v3 ->{
                System.out.println("检索器正常："+v3.toString());
                vertx.close();
            });
    }

    //使用future作为结果，目前公司在使用
    private static void futureConfig(){
        Vertx vertx =Vertx.vertx();
        ConfigStoreOptions properties = new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(false)
                .setConfig(new JsonObject().put("path", "listener.properties")
                        .put("raw-data", true)
                );

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(properties);

        ConfigRetriever retriever = ConfigRetriever.create(vertx,options);
        Future<JsonObject> future=ConfigRetriever.getConfigAsFuture(retriever);

        future.setHandler(ar ->{
            if (ar.failed()) {
                System.out.println("失败："+ar.result());
            }else{
                System.out.println("成功："+ar.result());
            }
            vertx.close();
        });
    }
}
