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
需要的环境：
| Java | Harbor |
| :-----: | :----: |
| >=11 | >=2.0.0 |

下载安装文件：[Linux、Mac和Cygwin](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.9.1-beta/dhorse-0.9.1-beta-x64_bin-unix.tar.gz)，[Windows](https://github.com/tiandizhiguai/dhorse/releases/download/release-0.9.1-beta/dhorse-0.9.1-beta-x64_bin-windows.zip)，然后解压：

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

在浏览器里访问地址：`http://127.0.0.1:8100`，并在出现的登录页面中输入账号：admin，密码：admin，页面如下：

![Image text](./static/images/home.jpg)

最后，关闭服务：

```bash
$  bin/dhorse-stop.sh
```

## 开源许可

本软件遵守Apache开源许可协议2.0，详情《 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)》。

## 社区文章
《[DHorse系列文章之操作手册](https://blog.csdn.net/huashetianzu/article/details/127560678)》

《[DHorse系列文章之镜像制作](https://blog.csdn.net/huashetianzu/article/details/127376460)》

《[DHorse系列文章之maven打包](https://blog.csdn.net/huashetianzu/article/details/127481538)》

《[DHorse系列文章之Dubbo项目解决方案](https://blog.csdn.net/huashetianzu/article/details/127560873)》

《[DHorse系列文章之日志收集](https://blog.csdn.net/huashetianzu/article/details/127697038)》

《[DHorse系列文章之多环境标识](https://blog.csdn.net/huashetianzu/article/details/127696995)》

## 社区交流

感谢您的使用，如果想了解更多内容，请加入如下群聊。

<img  src="./static/images/weixin.jpg" align='left'/> 
