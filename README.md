# 经过几天几夜，我手撸了一个本地缓存框架
## 一、闲着没事干
   俗话说得好，有人的地方，就有江湖。有高性能的地方，就有 cache。 —— 迈克·霆别刃硕德
   
   我们知道缓存在项目中是非常重要的，使用缓存的目的是想通过提高服务器的性能从而提高应用的用户体验。在我们的编码生涯中或多或少会使用到各种缓存，例如Redis、MongoDB、Guava等等，但其实本地缓存框架是相对较少的，那么我们能否自己动手做一个属于我们自己的本地缓存框架呢？当然可以咯！做一个简单易用，可拓展的本地缓存框架既能够方便我们日后开发提高性能，也能够学习致用，提高自己的编码能力，岂不美滋滋...

## 二、JAVA本地缓存框架
### 特性

        简单易用、可拓展
        流式编程体验，纵享丝滑
        支持 Cache 固定大小
        支持自定义 Map 实现策略
        支持 expired 过期特性
        参考 Redis 思想，采用惰性删除 + 定时删除
        支持FIFO、LFU、LRU缓存淘汰机制
        采用读写锁保证线程安全
        基于 Spring 实现事件监听
        支持回调函数
        支持 load 初始化和 persist 持久化
        支持心跳统计检测机制
        
   参考了网上的许多相关资料以及书籍，都有很多好用的设计思想值得我们学习，但是其实很少有一份真正实现缓存框架的代码呈现出来学习，因此需要自己动手实践。只有真正动手实践了，才能发现原来bug可以有这么多。

### 如何使用
   我们可以通过具体需求来构建最适合的缓存，下面为大家写了一个构建例子：


      @Slf4j
      @Component
      public class Test {
          public static void main(String[] args) throws InterruptedException {

              String name = "Test";

              final CacheLoader<Integer, Integer> cacheLoader = new CacheLoader<Integer, Integer>() {
                  @Override
                  public Integer load(Integer key) {
                      return key;
                  }
                  @Override
                  public void loadAll(Map<Integer, CacheObject<Integer, Integer>> cacheMap) {
                  }
              };

              final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

              Cache<Integer, Integer> cache = CacheBs.<Integer, Integer>newInstance()
                      .setName(name)
                      // 设置 Cache 大小 （0代表不限制，默认是0）
                      .setCacheSize(40)
                      // 设置 Lock 是否公平 （默认是false）
                      .setFair(false)
                      // 设置 过期时间 （0代表永不过期，默认是0）
                      .setTimeout(1000 * 20)
                      // 设置 Load 加载
                      .setCacheLoader(cacheLoader)
                      // 设置 Persist 持久化
                      .setCachePersist(new RdbCachePersist<>(name, CacheConfig.PERSIST_PATH))
                      // 设置 RemoveCallback 回调函数
                      .setRemoveCallback((key, value, removeReason) -> log.info("<<缓存回调>> 缓存被删除, key=" + key + ", value=" + value + ", reason=" + removeReason))
                      // 设置 Spring Context，目的是为了事件监听，如果不想使用Spring或者有自造轮可自行进行修改
                      .setContext(context)
                      // 构造参数传入具体的淘汰缓存类，该类需要实现 Cache<K, V> 接口
                      // 并且提供有参构造函数(String.class, int.class, long.class, boolean.class)
                      .build(FIFOCache.class);


              // 使用完也不要忘记销毁哦
              cache.destroy();
          }
      }

   而 Cache 接口提供的操作，其实与 Map 是差不多相似的功能，简洁明了。

### 这里写的比较重要
   因为整体内容偏多，并不能一一讲解（其实是懒得写了），所以这里博主会对比较特别的点进行说明，感兴趣的小伙伴可以自行去Github进行下载使用。（别白嫖，记得三连，栓Q）

#### ① 采用建造者模式 + 反射

   当构造一个对象的参数变多并且变得繁琐时，建造者模式就是最好的选择。使用建造者模式可以使调用者不必知道内部组成的细节，只需要设置所需的参数，由建造者去构建对象。
   

         public Cache<K, V> build(Class<? extends Cache> targetClazz)
         

   build 方法通过反射可以根据参数生成具体的实现类，例如：FIFOCache，而不需要每一个实现类都去编写一个建造类，不过代价是传入的目标类除了需要实现 Cache<K, V> 接口之外，还需要提供有参构造函数(String.class, int.class, long.class, boolean.class)，也就是name、cacheSize、timeout、fair 这几个属性的构造。

#### ② 为什么使用读写锁保证同步
   前面也有说过该缓存是通过读写锁来保证线程安全的，那么可能会有同学会疑惑，为什么要使用读写锁呢？不能使用其他锁来保持线程安全呢？这是个好问题！

   保证线程安全常见的锁有 synchronized 和 ReentrantLock ，至于它俩的区别，网上有很多写得很详细的博客，这里就不过多说明。在我个人认为，如果有特别的需求（例如：支持响应中断、设置是否公平锁等）可以用 ReentrantLock，否则建议使用 synchronized。因为两者的性能差别不大，而且 synchronized 是内置于JDK的锁，在之后还有很多优化的空间。不过有一点是可以确定的，也就是 synchronized 和 ReentrantLock 都是独占锁，而考虑到缓存一般是读多写少的场景，所以为了更好地贴合场景，降低锁的粒度，提高性能，可以采用分段锁或者读写锁。

  至于为什么不用分段锁，说实话一开始博主考虑使用分段锁来实现，因为这样不仅仅在读方面，在写方面也有很好的并发。不过使用分段锁有一个劣势是：当某种情况下，需要加锁整个Cache时，则需要获取所有锁，这样实现不当，可能会导致开销更高。所以最终决定用读写锁，而读写锁具有  “读读不互斥，读写互斥，写写互斥”  的特点。

#### ③ 如何保证 load(K key) 只执行一次
   load 加载机制除了在初始化加载全部数据时，还可以在 get 出来数据为空时，自动加载并保存到cache里面，其实实现并不难，我们可以看一下伪代码：


    public V get(K key) {
        ReentrantReadWriteLock lock = this.lock;
        Map<K, V> cacheMap = this.cacheMap;
        lock.readLock().lock();
        try {
            V value = cacheMap.get(key);
            if (value != null) {
                return value;
            }
            value = this.cacheLoader.load(key);
            if (value == null) {
                return null;
            }
            cacheMap.put(key, value);
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }


   不知道有没有小伙伴发现问题，因为我们使用的写锁，所以说明有可能会有多个线程进来，那么 put 方法其实就可能会多次触发。而且更细心的同学还会发现，当 load 方法是一个很耗时的操作时，比如需要访问数据库，那么就有可能多次访问数据库，有点类似于缓存击穿，本来是想通过缓存来提高性能，没想到却在这里给败了。

   那么有没有办法当多个线程进来，让 load 方法只执行一次呢？其实是有的。在参考 <<JAVA并发编程实战>> 书籍中就有一个很巧妙的设计，我们可以看一下代码：
   

         public class Memoizerl<A, V> implements Computable<A, V> {
             private final Map<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();
             private final Computable<A, V> c;

             public Memoizerl(Computable<A, V> c) {
                 this.c = c;
             }

             @Override
             public V compute(A arg) throws InterruptedException, ExecutionException {
                 while (true) {
                     Future<V> f = cache.get(arg);
                     if (f == null) {
                         Callable<V> eval = new Callable<V>() {
                             @Override
                             public V call() throws Exception {
                                 return c.compute(arg);
                             }
                         };
                         FutureTask<V> ft = new FutureTask<V>(eval);
                         f = cache.putIfAbsent(arg, ft);
                         if (f == null) {
                             f = ft;
                             ft.run();
                         }
                         try {
                             return f.get();
                         } catch (CancellationException e) {
                             cache.remove(arg, f);
                         }
                     }
                 }
             }
         }
         

   compute 是一个计算很费时的方法，所以这里把计算的结果缓存起来，但是有个问题就是如果两个线程同时进入此方法中怎么保证只计算一次，这里最核心的地方在于使用了ConcurrentHashMap的putIfAbsent方法，同时只会写入一个FutureTask；

# 三、总结

   其实实现缓存的设计方法有很多，并没有最好的，只有最适合的平衡点（鱼和熊掌不可兼得），而该缓存框架也是存在部分问题：

  1、CacheValidKeysIterator迭代器是用于遍历有效非过期的Key，其为了避免遍历时和缓存的操作引发的线程安全，采用了深拷贝的方式，但是却弱一致性。
  
  2、LRUCache 与 LFUCache 是通过 CacheObject 类中的 lastAccess 与 accessCount 属性数据来实现的，但是这两个值并不是很准确，不过影响是可接受范围之内（可以考虑用原子类解决）。
  
  3、不支持自定义 Map 实现策略。
  
  4、load 的加载过程中如果报错，不处理 key，是为了防止反复访问DB、反复报错，这里是通过定时调度去 clear，但是当调度间隔大，则会占内存，总觉得这里处理得不够优雅。
  
   
   当然，最重要的是咱们能够学习到优秀的设计思想来应用到适合的场景就足够了，如果有其他暴露的问题或者可优化的点也欢迎各位大佬评论，望指点。
