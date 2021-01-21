# WebCacheServer

## 简介

`WebCacheServer`为个人项目，用于在浏览器访问网页地址时自动将内容缓存至特定目录。

如果存在多个请求同时访问同一个资源，该程序将同时下发同一资源，以达到节省网络流量的目的。

## 使用方法
在命令行中执行:
`java -jar WebCacheServer.jar`

在浏览器中访问以下地址即可达到缓存目的:
http://127.0.0.1/request/?url=[地址]

注意, 缓存默认存储于内存中, 如果缓存较多, 则容易造成内存相关问题。
指定参数`-dir`可将混存存储于本地目录中。

## 联络
使用以下电子邮件联络我weefic at whu.eud.cn
You may contact the author by e-mail: weefic at whu.eud.cn