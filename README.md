# DHorse项目
DHorse是一个简单易用、以应用为中心的云原生DevOps系统，具有持续集成、持续部署、微服务治理等功能，主要特点：部署简单、操作简洁、功能快速。

# 项目地址

* [GitHub](https://github.com/512team/dhorse)

* [Gitee](https://gitee.com/i512team/dhorse)

* [512技术园](http://512.team)

## 架构
 ![Image text](./static/images/architecture.jpg)

## 主要特性
* 简洁的操作界面
* 以应用为中心，屏蔽K8S的底层概念
* 无需安装Docker即可构建镜像
* 无需安装Maven即可打包
* 无需安装Node即可打包
* 支持SpringBoot应用部署
* 支持Node应用部署
* 支持多环境部署
* 支持日志收集
* 支持链路追踪

## 主要技术
Springboot、Mybatis、Mybatis-plus、Maven-core、Jib-core、Layuimini、Smart-doc、H2、Mysql等。

## 快速开始

1. 环境要求

| Harbor | kubernetes
| :----: | :----: |
| >=2.0.0 | [1.18.x, 1.26.x]

2. 下载安装文件

| Java版本 | Linux、Mac或Cygwin | Windows
| :-----: | :----: | :----: | 
| 1.8 | [下载](https://github.com/512team/dhorse/releases/download/v1.1.0/dhorse-v1.1.0-jdk1.8-bin-unix.tar.gz) | [下载](https://github.com/512team/dhorse/releases/download/v1.1.0/dhorse-v1.1.0-jdk1.8-bin-windows.zip)
| >=11 | [下载](https://github.com/512team/dhorse/releases/download/v1.1.0/dhorse-v1.1.0-bin-unix.tar.gz) | [下载](https://github.com/512team/dhorse/releases/download/v1.1.0/dhorse-v1.1.0-bin-windows.zip)

3. 下载文件之后，然后解压

```bash
$  tar -xzf dhorse-*.tar.gz
```

4. 进入解压目录并查看文件列表

```bash
$  cd dhorse-* && ls -l
```

5. 内容如下

```bash
total 24
drwxr-xr-x 2 root root   115 Oct  6 19:56 bin
drwxr-xr-x 2 root root    48 Oct  6 19:56 conf
drwxr-xr-x 3 root root    46 Oct  6 19:56 lib
-rw-r--r-- 1 root root 11558 Dec 10  2021 LICENSE
-rw-r--r-- 1 root root  5141 Dec 26  2021 NOTICE
-rw-r--r-- 1 root root  1337 Jan 21  2022 README.txt
drwxr-xr-x 8 root root    93 Sep 23 16:09 static
```

6. 启动服务

```bash
$  bin/dhorse-start.sh
```

7. 在浏览器里输入地址：`http://127.0.0.1:8100`，并输入登录信息：admin/admin，登录之后如下图所示

 ![Image text](./static/images/home.jpg)

8. 最后，关闭服务

```bash
$  bin/dhorse-stop.sh
```

## 了解更多

* [操作手册](https://github.com/512team/dhorse-doc/blob/main/guide/%E6%93%8D%E4%BD%9C%E6%89%8B%E5%86%8C.md)

* [配置文件](https://github.com/512team/dhorse-doc/blob/main/guide/%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6.md)

* [多环境标识](https://github.com/512team/dhorse-doc/blob/main/guide/%E5%A4%9A%E7%8E%AF%E5%A2%83%E6%A0%87%E8%AF%86.md)

* [日志收集](https://github.com/512team/dhorse-doc/blob/main/guide/%E6%97%A5%E5%BF%97%E6%94%B6%E9%9B%86.md)

* [链路追踪](https://github.com/512team/dhorse-doc/blob/main/guide/%E9%93%BE%E8%B7%AF%E8%BF%BD%E8%B8%AA.md)

* [打包说明](https://github.com/512team/dhorse-doc/blob/main/guide/%E6%89%93%E5%8C%85%E8%AF%B4%E6%98%8E.md)

* [运行源码](https://github.com/512team/dhorse-doc/blob/main/guide/%E8%BF%90%E8%A1%8C%E6%BA%90%E7%A0%81.md)

## 社区交流

感谢您的支持和关注，如想了解更多内容，欢迎加入群聊。

 ![Image text](./static/images/weixin.jpg)

## 开源许可

本软件遵守Apache开源许可协议2.0，详情《 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)》。