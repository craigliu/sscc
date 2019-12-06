# 一个强一致性缓存方案的实现

受到石衫专栏中关于缓存和数据库一致性方案的启发，写了一个简单的实现(SSCC=Simple Strong Consistent Cache)。原理是对同一个key的写和读进行排队。当一个读请求进来时首先会删除对应key的缓存数据，当写数据库操作完成后会将新值填回缓存。对读操作，如果缓存中取不到值，会等待一段时间后重试，当到达设置的超时时间还未能取到值会抛出异常或者直接从数据库读值。在内存中维护多个队列，不同key的读请求和写请求被散列到对应的队列里。每个队列有一个线程顺序处理队列中的请求。

**注意**: 代码主要是为了迅速验证实现方案，有很多不严谨的地方。如果用在生产环境上需要完善和优化。

#### 架构图

![架构图](https://raw.githubusercontent.com/craigliu/sscc/master/strong-consistent-cache.png)



#### 搭建和运行

1. 启动测试用的mysql和redis

```shell
docker-compose up -d
```
2. 运行test case

```shell
mvn test -Dtest=com.rtbasia.sscc.PerformanceTest
```






