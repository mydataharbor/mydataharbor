
![](mydataharbor.png)

## 简介

MyDataHarbor是一个致力于解决任意数据源到任意数据端的分布式、高扩展性、高性能、事务级的中间件。

## 背景

在微服务的大背景下，实时交易系统的数据的分散存储已经成为常态，然而有时候我们需要对这些数据进行实时或者定时全量的进行同步到另外一个地方。

比如，一个公司的C部门的系统，需要用到A、B部门产生的数据，这时候避免不了进行全量或者增量的数据同步。再比如，数据库中的数据我要实时同步到elasticsearch、redis等等中进行搜索。

数据同步的应用场景在日常的分布式系统开发中非常常见，而且非常重要，一旦数据同步出现问题，将会导致数据不一致，引起其他异常。

目前一般公司的做法是，各个部门开发一套自己的同步小程序，没有管理，更可能没有监控，来一个需求开发一个、非常浪费资源，稳定性也得不到保障。

MyDataHarbor在这种场景需求下应用而生！

## 特性

1. ### 分布式设计

   MyDataHarbor是一个在zookeeper上构建的分布式中间件，支持对主机进行分组，各分组下的机器形成一个子集群，任务在子集群隔离范围内进行负载均衡，防止单点故障。

2. ### 插件式设计

   高度合理的抽象、插件化的设计使得MyDataHarbor拥有很高扩展性，任何数据迁移的需求都可以通过开发插件完成。

3. ### 事务支持

   MyDataHarbor设计之初就考虑到数据丢失问题，引入事务的支持保障数据不丢失！

4. ### 插件自描述

   安装插件后中间件会自动识别这个插件的能力，并且生成用户UI友好的任务创建界面，不需要用户直接编写复杂的json配置。

5. ### 任务监控

   对接java的jmx，每个任务都有详细的监控，实时查看任务的运行状态。

## 宏观设计

MyDataHarbor唯一依赖的中间件是zookeeper，共有两个组件：mydataharbor-console、mydataharbor-server

- mydataharbor-console

  该应用是一个springboot应用，内部实现了对整个集群的管理，插件仓库服务/管理，可视化任务管理。该应用内部默认使用了H2数据库记录插件仓库信息，所以只能单机部署，如果后面有需要的话可以把插件仓库服务独立部署，或者使用mysql等独立的数据库服务器。

- mydataharbor-server

  该应用是数据搬移任务工作的具体环境，提交的任务都会分配到该节点上，该应用是一个可以大规模部署的纯java应用，依赖zookeeper做分布式协调。

## 安装使用

MyDataHar的安装非常简单：

- 下载zookeeper、mydataharbor-console、mydataharbor-server

- 启动zookeeper，参考网络教程

- 启动mydataharbor-console

  -  java -jar -Dzk=127.0.0.1:2181 mydataharbor-console-1.0-SNAPSHOT.jar

- 启动mydataharbor-server

  - 配置config目标下的system.yml
    ```yaml
    zk: ["127.0.0.1:2181"] #zk地址
    port: 1299 #启动端口
    group: biz001 #该节点所属组
    pluginRepository: http://127.0.0.1:8080 #插件仓库地址
  
  - 启动
  
    java -jar mydataharbor-server-1.0-SNAPSHOT.jar
  
- 验证：

  访问：mydataharbor-console  http://127.0.0.1:8080

  是否可以看到刚刚启动的节点