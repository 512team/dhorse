# DHorse项目
DHorse是一个简单易用的DevOps开发平台，主要特点：部署简单、功能快速、操作简洁。

## 架构
 ![Image text](./static/images/architecture.jpg)

## 主要特性
* 简洁的操作界面
* 支持Springboot应用部署
* 无需安装Maven即可打包
* 无需安装Docker即可构建镜像
* 支持多环境部署
* 支持日志收集
* 支持链路追踪

## 主要技术
Springboot、Mybatis、Mybatis-plus、Maven-core、Jib-core、Layuimini、Smart-doc、H2、Mysql等。

## 快速开始

环境要求：
| Harbor | kubernetes
| :----: | :----: |
| >=2.0.0 | [1.13.x, 1.24.x]

下载安装文件：
| Java版本 | Linux、Mac或Cygwin | Windows
| :-----: | :----: | :----: | 
| 1.8 | [下载](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.10.1-beta/dhorse-0.10.1-beta-1.8-x64_bin-unix.tar.gz) | [下载](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.10.1-beta/dhorse-0.10.1-beta-1.8-x64_bin-windows.zip)
| >=11 | [下载](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.10.1-beta/dhorse-0.10.1-beta-x64_bin-unix.tar.gz) | [下载](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.10.1-beta/dhorse-0.10.1-beta-x64_bin-windows.zip)

下载文件之后，然后解压：

```bash
$  tar -xzf dhorse-*.tar.gz
```

进入解压目录并查看文件列表：

```bash
$  cd dhorse-* && ls -l
```

内容如下：

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

启动服务：

```bash
$  bin/dhorse-start.sh
```

在浏览器里输入地址：`http://127.0.0.1:8100`，并输入登录名：admin，密码：admin，登录之后如下图所示：

 ![Image text](./static/images/home.jpg)

最后，关闭服务：

```bash
$  bin/dhorse-stop.sh
```

了解更多：

《[DHorse操作手册](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E6%93%8D%E4%BD%9C%E6%89%8B%E5%86%8C.md)》

《[DHorse配置文件](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6.md)》

《[DHorse镜像制作](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E9%95%9C%E5%83%8F%E5%88%B6%E4%BD%9C.md)》

《[DHorse打包](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/maven%E6%89%93%E5%8C%85.md)》

《[Dubbo应用解决方案](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/Dubbo%E5%BA%94%E7%94%A8%E8%A7%A3%E5%86%B3%E6%96%B9%E6%A1%88.md)》

《[DHorse多环境标识](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E5%A4%9A%E7%8E%AF%E5%A2%83%E6%A0%87%E8%AF%86.md)》

《[DHorse日志收集](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E6%97%A5%E5%BF%97%E6%94%B6%E9%9B%86.md)》

《[DHorse链路追踪](https://github.com/tiandizhiguai/dhorse-doc/blob/main/guide/%E9%93%BE%E8%B7%AF%E8%BF%BD%E8%B8%AA.md)》

## 社区交流

感谢您的关注和支持，如想了解更多内容，请加入如下群聊。

 ![Image text](./static/images/weixin.jpg)

## 开源许可

本软件遵守Apache开源许可协议2.0，详情《 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)》。