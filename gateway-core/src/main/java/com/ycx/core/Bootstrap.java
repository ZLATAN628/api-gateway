package com.ycx.core;

public class Bootstrap {

    public static void main(String[] args) {
        // 加载网关核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println("working on " + config.getPort());
        // 插件初始化

        // 配置中心管理初始化，连接配置中心，监听配置的新增、修改、删除

        // 启动容器
        Container container = new Container(config);
        container.start();

        // 连接注册中心，将注册中心的实例加载到本地

        // 服务优雅关机

    }
}
