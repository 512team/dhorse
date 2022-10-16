# DHorse项目
DHorse是一个简单易用的DevOps开发平台，主要特点：部署简单、功能快速、操作简洁。

## 架构
![Image text](./static/images/architecture.jpg)

## 主要特性
* 简洁的操作界面
* 支持Springboot项目部署
* 无需安装Maven即可打包
* 无需安装Docker即可构建镜像
* 支持日志收集
* 支持多环境部署

## 主要技术
Springboot、Mybatis、Mybatis-plus、Maven-core、Jib-core、Layuimini、Smart-doc、H2、Mysql等。

## 快速开始
首先需要安装大于等于Java11的环境。

下载安装文件：[Linux、Mac和Cygwin](https://gitee.com/tiandizhiguai/release2/raw/master/dhorse-1.0.2-x64_bin.tar.gz)，[Windows](https://gitee.com/tiandizhiguai/release2/raw/master/dhorse-1.0.2-x64_bin.tar.gz)，然后解压：

```bash
$  tar -xzf dhorse-*.tar.gz
```

进入解压目录并查看文件内容：

```bash
$  cd dhorse-* && ls -l
```

文件内容如下：

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

在浏览器里访问地址：`http://127.0.0.1:8100`，并在出现的登录页面中输入账号：admin，密码：admin后，页面如下：

![Image text](./static/images/home.jpg)

最后，关闭服务：

```bash
$  bin/dhorse-stop.sh
```

## 开源许可

本软件遵守Apache开源许可协议2.0，详情《 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)》。

## 社区交流

如果想了解更多内容，请加如下微信（邀请进群），感谢您的使用。

<img  src="./static/images/weixin.jpg" align='left'/> 
