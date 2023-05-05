<p align="center">
	<br/>
  <a href="http://www.mydataharbor.com" target="_blank">
    <img  src="mydataharbor.png" alt="logo">
  </a>
</p>



<p align="center" class="print-break">
    <a href="https://mydataharbor.com" style="display:inline-block"><words type='updated' /></a>
    <a href="https://github.com/mydataharbor/mydataharbor/actions/workflows/maven.yml" target="_blank" style="display:inline-block" class="not-print">
       <img src="https://img.shields.io/github/actions/workflow/status/mydataharbor/mydataharbor/maven.yml?branch=main" alt="GitHub-CI">
    </a>
     <a href="https://github.com/mydataharbor/mydataharbor/releases" target="_blank" style="display:inline-block" class="not-print">
       <img src="https://img.shields.io/github/v/release/mydataharbor/mydataharbor" alt="查看发行的版本">
    </a>
    <a href="https://search.maven.org/search?q=com.mydataharbor" target="_blank" style="display:inline-block" class="not-print">
       <img src="https://img.shields.io/maven-central/v/com.mydataharbor/mydataharbor" alt="maven仓库">
    </a>
    <a href="https://github.com/mydataharbor/mydataharbor/releases" target="_blank" style="display:inline-block" class="not-print">
       <img src="https://img.shields.io/github/downloads/mydataharbor/mydataharbor/total" alt="下载数量">
    </a>
    <a href="https://github.com/mydataharbor/mydataharbor/blob/main/LICENSE" target="_blank" style="display:inline-block" class="not-print">
       <img src="https://img.shields.io/github/license/mydataharbor/mydataharbor" alt="开源协议">
    </a>
    <a href="https://mydataharbor.yuque.com/books/share/d5b1360e-d316-4be0-85de-b0958ac64267/pckin3" target="_blank" style="display:inline-block">
      <img src="https://img.shields.io/badge/plugins-清单-blue" alt="插件列表">
    </a>
</p>

欢迎前端、插件开发人员前来贡献代码，感兴趣的请联系我：1053618636@qq.com

## 简介/定位

:cn: 🚢 MyDataHarbor是一个致力于解决任意数据源到任意数据源的分布式、高扩展性、高性能、准实时的数据同步中间件。

它可以帮助用户可靠、快速、稳定的对海量数据进行准实时增量同步或者定时全量同步，主要定位是为实时交易系统服务，亦可用于大数据的数据同步（ETL领域）。

## 背景

在微服务的大背景下，实时交易系统的数据的分散存储已经成为常态，然而有时候我们需要对这些数据进行实时或者定时全量的同步到另外一个地方。

比如，一个公司的C部门的系统，需要用到A、B部门产生的数据，这时候避免不了进行全量或者增量的数据同步。再比如，数据库中的数据我要实时同步到elasticsearch、redis等等中进行搜索。

数据同步的应用场景在日常的分布式系统开发中非常常见，而且非常重要，一旦数据同步出现问题，将会导致数据不一致，引起其他严重的异常。

目前小公司的做法是在业务程序系统里修改代码，往目标数据源中写入数据，上点规模的公司的做法是，各个部门开发一套自己的同步小程序，没有管理，更可能没有监控，来一个需求开发一个、非常浪费资源，稳定性也得不到保障，而大公司则是有一套数据迁移平台（如阿里的精卫）。

MyDataHarbor在这种场景需求下应用而生！

## 特性

 ### 🚩分布式设计

   MyDataHarbor是一个在zookeeper上构建的分布式中间件，支持水平扩展，对节点进行分组，各分组下的机器形成一个子集群，任务在子集群隔离范围内进行负载均衡，防止单点故障。

 ### 🚩插件式设计

   高度合理的抽象、插件化的设计使得MyDataHarbor拥有很高扩展性，任何数据迁移的需求都可以通过开发插件完成。

 ### 🚩至少一次保障

   MyDataHarbor设计之初就考虑到数据丢失问题，引入微事务实现数据至少一次的承诺，保障数据不丢失（但是在极端情况下会重复）！

 ### 🚩插件自描述

   安装插件后中间件会自动识别这个插件的能力，并且生成用户UI友好的任务创建界面，不需要用户直接编写复杂的json配置。

 ### 🚩自由组合

   MyDataHarbor支持从不同的插件中复用各种组件，形一个新的pipeline管道，并且这些都是可以通过可视化的方式进行。

 ### 🚩任务监控

   对接java的jmx，每个任务都有详细的监控，实时查看任务的运行状态。

 ### 🚩批量支持

   为可以批量进行提交的写入源预留批量接口通道，有效提升数据迁移速度，摩托变汽车。

 ### 🚩ForkJoin

   对于DataSource无法多线程并发拉取的情况下（如jdbc游标取数据），内部引入forkjoin并发处理模型开启多线程处理，并且灵活的事务控制，让速度飞快的同时保证数据迁移的稳定、不丢失，汽车变高铁。

## 设计

MyDataHarbor唯一依赖的中间件是zookeeper，共有两个组件：mydataharbor-console、mydataharbor-server
   ![集群设计](./doc/image/cluster-design.png)
   ![节点任务设计](./doc/image/node-design.png)

## 支持的插件

详见  https://mydataharbor.yuque.com/staff-tzwgrd/uqew9p/pckin3

## 快速开始

MyDataHarbor的安装非常简单（启动前请先准备好zookeeper集群）：

### 下载二进制包

下载地址：[https://github.com/mydataharbor/mydataharbor/releases](https://github.com/mydataharbor/mydataharbor/releases)
下载列表：

      mydataharbor-console-xxx-bin.tar.gz
      mydataharbor-server-xxx-bin.tar.gz

> xxx是发行的版本号

### mydataharbor-console 

#### 解压

 ![image-20210812143819918](./doc/image/image-20210812143819918.png)

#### 配置

进入config目录，修改application.yml，主要修改如下配置 

```yaml
server:
  port: 8080 #console服务启动端口
zk: 127.0.0.1:2181 #zk地址
```

#### 运行

Windows系统下运行 start.bat<br>
Linux系统下运行 start.sh  关闭stop.sh 

> start.sh 脚本支持 jmx、debug、status参数 如：<br>
>  start.sh jmx   启动远程jmx支持 <br>
> start.sh debug 开启远程debug方式启动 <br>
> start.sh status 查看当前程序状态 

### mydataharbor-server 

#### 解压

![image-20210812144430744](./doc/image/image-20210812144430744.png)

#### 配置

修改config目录下的system.yml 
```yaml
zk: ["127.0.0.1:2181"] #zk地址
port: 1299 #server服务启动端口
group: biz001 #该节点所属组
pluginRepository: http://127.0.0.1:8080 #插件仓库地址
```
#### 运行
Windows系统下运行 start.bat<br>
Linux系统下运行 start.sh  关闭stop.sh 

> start.sh 脚本支持 jmx、debug、status参数 如：<br>
>  start.sh jmx   启动远程jmx支持 <br>
> start.sh debug 开启远程debug方式启动 <br>
> start.sh status 查看当前程序状态 

#### 验证
访问：mydataharbor-console  [http://127.0.0.1:8080](http://127.0.0.1:8080)
是否可以看到刚刚启动的节点 
 ![image-20210812143819918](./doc/image/demo.png)

## 其它

demo运行实例：http://mydataharbor.com:8080/

插件市场：https://www.mydataharbor.com/user/info.html

文档(语雀)：http://doc.mydataharbor.com

#### QQ群（**<u>*加群时需要验证项目star数，请star一下然后记下star数告诉管理员*</u>**）

![QQ群](./doc/image/qq-discuz.png)



## 更新日志

### 2.0.0版本

    1、新增 mydataharbor.ITaskStorage 接口，允许各组件在运行期持久化记录数据，并提供一个zookeeper的默认实现，每秒1次准实时同步，不影响性能。
    
    2、默认将任务的监控信息通过持久化接口近乎实时的展示在管理台
    
    3、任务修改重建功能
    
    4、调整rebalance算法，新机器加入，将转移当前管道数大于任务分配节点数的任务
    
    5、鉴于1.x使用用户可能较少，由于修复了一些拼写错误，接口名称变了，不再向1.x兼容，建议大家把任务移到2.x上，请谅解

### 2.0.1版本

    1、优化pipeline可视化创建，通过UI的方式自由组合各组件创建pipeline，无需自定义DataPipelineCreator

    2、插件可以重复安装，并且更新版本
    
    3、负载均衡和故障转移可以分开设置