点赞服务在社交类平台中，是一个最常见的功能。每天会有成千上万的用户对其进行操作，在这看似简单的功能背后，蕴藏了不少设计哲学。让我们从领域驱动设计出发，从零设计、开发一套高性能的通用点赞服务。

通过本 Chat 您将学习到：
1. 领域驱动设计核心组件及应用
2. 领域事件实战
3. CQRS 架构实战


本 Chat 主要面向于中、高级程序员；或对高性能系统设计有兴趣的码农。

### 1 什么是点赞系统
> 点赞是互联网中常见的交互方式，系统根据用户的点赞操作来跟踪用户的行为，并对用户的喜好进行分析。

点赞，在互联网中是一个比较简单的操作。用户看到自己喜欢的信息，点击“赞”按钮，点亮“赞”操作，再次点击，取消之前的“赞”操作。是不是很 easy？

![点赞](http://litao851025.gitee.io/books-image/chat/like/like_unlike.png)

但，一个明星微博的点赞数可能高达几十万，甚至上百万。一个热点新闻，可能有多个用户同时点赞，如何处理这些极端情况呢？

### 2 系统设计要点

如果要构建一套通用点赞系统，首先需要对系统所涉及角色、主功能进行梳理。

#### 2.1 系统角色
> 系统角色主要涉及 点赞发起者 和 点赞目标对象。

这两个角色，本质上是对其他对象的一种引用。从领域设计角度，应该是对其他限界上下文中聚合的引用。两个角色，本身没有唯一标识，并且根据属性确定其相等性。因此，符合值对象建模规范，应该作为值对象处理。

通常情况下，点赞发起者对应系统的用户（有的系统会有多个用户系统）。但，点赞的目标对象可能会有很多，如新闻、评论、帖子等等。

![点赞发起者&目标](http://litao851025.gitee.io/books-image/chat/like/owner_target.png)


对象 | 含义 |  建模方式
---|---|---
Owner | 点赞发起者 | 值对象
Target |点赞目标对象 |  值对象

#### 2.2 系统功能用例
> 系统功能，主要围绕系统角色展开。

点赞发起者 Owner，它可以选择一个点赞对象 Target 进行点击操作。当显示目标对象 Target 时，需要判断该 Target 是否已经点过赞。

对于点赞对象 Target，只有一个应用场景，就是在显示时，获取总的点赞数量。

![用户用例](http://litao851025.gitee.io/books-image/chat/like/user_case.png)



用例 | 含义
---|---
点击 | Owner 对特定 Target 的点击行为。如果没有点赞，则点赞；如果已经点赞，则取消点赞
是否点赞 | 判断特定 Owner 对 特定 Target 是否已经点赞
获取点赞数量 | 获取特定 Target 总的点赞数量



#### 2.3 赞功能设计
> 点赞发起者点击“赞”按钮，点亮“赞”操作，再次点击，取消之前的“赞”操作。



##### 2.3.1 **识别建模类型**
赞可以接收点击操作，并更新内部状态。同一个点赞发起者对同一个点赞目标进行“赞”和“取消”操作时，针对的应该是同一个“赞”实例，需要唯一标识对操作进行跟踪。综上可见“Like”是一个实体。

##### 2.3.2 **实体建模**
> 首先，需要明确实体的名称，在这里，我们简单命名为 Like。

**行为建模**

分析下 Like 的业务行为，Like 对外操作只提供一个 click 方法，当触发 click 操作时，Like 在 Submitted 和 Cancelled 之间进行切换。当 Like 状态发生变化时，需要发布内部事件，需要对内部事件进行建模。

业务方法 | 含义 | 事件 | 业务规则
---|---|---|---
click | 用户点击行为 | 无 | 无
submit| 点赞 | LikeSubmittedEvent | 当用户未点赞时触发
cancel | 取消点赞 | LikeCancelledEvent | 当用户已经点赞时触发

为了避免 Like 的臃肿，我们将 Like 的状态进行单独建模。构建一个单独的值对象，并将状态相关的操作下推到该值对象中。在此，我们称为 LikeStatus。

**属性建模**

Like 所关联的对象，其中包括 Target、Owner 和 LikeStatus，三个都是值对象。

属性 | 类型 | 含义
---|---|---
owner | Owner | 点赞发起者
target | Target | 点赞目标对象
status | LikeStatus | 点赞状态

**创建方式建模**

Like 的创建方式比较简单，没有太复杂的业务验证，因此，采用静态方法对其进行创建。


##### 2.3.3 **小结**
Like 是一个比较复杂的聚合，具体结构如下：

![Like 聚合](http://litao851025.gitee.io/books-image/chat/like/like_agg.png)

赞功能所涉及的对象见下表。

对象 | 含义 |  建模方式
---|---|---
Like | 赞 | 实体&聚合根
LikeStatus | 赞状态 |  值对象
LikeSubmittedEvent | 点赞事件 |  内部领域事件
LikeCancelledEvent | 取消赞事件 |  内部领域事件


#### 2.4 日志功能设计
> Like 代表的是当前点赞状态，对于多次点击，只会记录最后的结果，而中间的过程数据丢失了。


日志，本身不属于业务功能，但对用户行为分析非常重要，我们应该将用户的所有操作保存下来。我们称这些过程数据为 LikeLogger。

##### 2.4.1 **识别建模类型**
日志主要用于记录谁（Owner）对什么（Target）进行哪个操作（Action），在创建后就不在改变。基本符合值对象建模条件，但，我们如何对其进行持久化呢？

一般情况下，值对象的持久化依赖于包含它的实体，值对象会随着实体的持久化而持久化。但，Logger 是个整体概念，本身不属于任何实体。在这种情况下，我们可以将其建模成一个不变实体，一来借助实体进行持久化，二来避免对实体的修改。

##### 2.4.2 **不变实体建模**
LikeLogger 为不变实体，内部所包含的属性，不允许修改。

**属性建模**

LikeLogger 所包含属性如下：
属性 | 类型 | 含义
---|---|---
owner | Owner | 点赞发起者
target | Target | 点赞目标对象
actionType | ActionType | 操作类型

**创建方式建模**

LikeLogger 支持 Like 和 Cancel 两种类型的日志，可以根据 ActionType 构建静态方法，以完成各自的创建。

方法 |  含义
---|---
createLikeAction | 创建点赞日志
createCancelAction | 创建取消点赞日志

##### 2.4.3 小结
LikeLogger 所涉及对象包括：

对象 | 含义 |  建模方式
---|---|---
LikeLogger | 赞日志 | 不变实体
ActionType | 操作类型 |  值对象

#### 2.5 计数功能设计
> 最简单的计数功能，便是通过 SQL 对 Like 进行 “count group by” 来完成，但在高并发系统中，group by 是一大忌讳。

从单一职责原则角度，Like 承载了过多的责任，将统计功能强加到服务于业务的 Like 也非常不合适。因此，我们对计数功能进行独立的业务建模。 我们称为 TargetCount。

##### 2.5.1 **识别建模类型**
TargetCount 需要根据点赞和取消点赞对计数进行增减操作。对于同一个 Target，需要持续跟踪其数量变化。可见，TargetCount 为一个实体。

##### 2.5.2 **实体建模**

**行为建模**

TargetCount 的操作，主要有 **incr** 和 **decr** 两个业务操作。在进行 count 更新时，存在一个业务规则，及 count 不能小于零。

业务方法 | 含义 | 事件 | 业务规则
---|---|---|---
incr | 增加点赞数 | 无 | 无
decr | 减少点赞数 | 无 | count 必须大于零

**属性建模**

TargetCount 的属性包括：

属性 | 类型 | 含义
---|---|---
target | Target | 点赞目标对象
count | Long | 总的点赞数

**创建方式建模**

TargetCount 的创建方式比较简单，因此，采用静态方法对其进行创建。


##### 2.5.3 **小结**
计数功能所涉及对象包括：

对象 | 含义 |  建模方式
---|---|---
TargetCount | 点赞目标计数 | 实体

#### 2.6 用例走查
> 用例走查，主要从用例角度，验证当前设计是否满足业务需要。

用例 | 支持方式
---|---
点击 | 由 Like 聚合的 click 方法进行支持
是否点赞 | 由 Like 聚合的 LikeStatus 进行支持
获取点赞数量 | 由 TargetCount 的计数进行支持

> LikeLogger 不直接服务于业务，仍旧有很大意义。

#### 2.7 架构设计
> 到现在，整个系统的核心组件就设计完成了，接下来，我们需要将其组装起来，以形成一个可用系统。

这设计架构前，有几个非功能性需求需要考虑。
- 点击行为的高并发
- 获取计数的高并发
- Like 与 Logger、 Count 的数据一致性

##### 2.7.1 **点击行为的高并发**
> 点击行为是典型的写操作，需要对写操作进行优化。

对于写操作优化，常见的策略包括：
- 数据散列。也就是我们常说的分库分表，将写操作分散到多个数据库实例中，从而提升系统的整体吞吐。
- 先入队列，后台消费。将用户请求添加到队列，启动后台线程，从队列中获取请求，并挨个消费。这种策略的最大特点就是可以起到消峰的作用，将瞬间巨大的请求缓存起来，不会对后台服务造成很大冲击。
 
对于一致性要求高的业务场景（比如支付），数据散列方案是唯一解决方案；对于一致性要求不高的业务场景（比如咱们的点赞系统），队列方案是最佳解决方案。

在此，我们使用队列方案来应对点击行为的高并发。即用户提交点击请求并不会直接调用业务方法，而是将请求放入消息队列；后台订阅线程从消息队列中获取请求，在调用业务方法执行业务逻辑。

##### 2.7.2 **获取计数的高并发**
> 获取计数是典型的读操作，需要对读操作进行优化。

对读操作的优化，主要是使用缓存进行访问加速。我们使用 Redis 来加速访问。

具体的操作如下：
- 首先从 Redis 中获取计数信息，如果命中，直接返回
- 如果 Redis 未命中，从数据库中获取计数，将结果添加到 Redis 中，然后返回
- 当计数发生变化时，清理 Redis 的过期数据

##### 2.7.3 **Like 与 Logger、 Count 的数据一致性**
> 在系统中 Like、Logger、Count 是三个聚合根，我们需要保证三者的数据一致性。

系统中操作入口只有 Like 聚合，当 Like 发生变化时，Logger 和 Count 都需要跟着联动起来。Like 与 Count、Logger 具有很强的因果关系，这也是领域事件建模的信号。

> 在常规操作中，我们会在操作完 Like 后，调用 Logger、Count 相关接口直接进行业务操作。但在 DDD 中，是绝对不允许的，一个操作只能对一个聚合根进行处理，聚合根之间的同步只能基于事件通过最终一致性解决。

我们可以基于内存总线和内部事件，通过订阅 Like 相关事件在内存中完成与 Logger、Count 的数据同步；也可以使用专用消息队列和外部事件，完成多个系统间的数据同步。

> 考虑到系统读写的扩展性，在此，我们使用消息队列和外部事件完成数据一致性保障。

Like 在执行完业务操作后，将内部领域事件直接发布到内存总线（EventBus），Exporter 组件从内存总线中获取领域事件，将其转换为外部事件，并发送到消息队列中。LikeEventConsumer 组件负责从消息队列中获取事件，调用 Logger、Count 相关业务接口以完成业务操作。

##### 2.7.4 **小结**
综上分析，我们的最终架构如下：

![系统架构](http://litao851025.gitee.io/books-image/chat/like/review.png)

### 3 项目搭建
> 该项目使用 Spring Boot 作为主要开发框架。

项目依赖组件：
组件 | 含义
---|---
spring-boot-starter-web | Web
flyway | 数据库管理
Junit | 测试
lombok | 自动生成getter、setter

> 随着功能的增加，将为项目添加更多依赖。

#### 3.1. 项目生成
浏览器中输入 https://start.spring.io/ ，打开 spring-boot 项目生成器，按照下列配置生成项目：

名称 | 值
---|---
项目类型 | maven
语言 | java
Boot版本 | 2.1.4
group | com.geekhalo
artifact | like
dependency | web、data-jpa、data-redis、flyway、lombok

点击“Generate Project”，生成并下载项目。
将下载的项目解压，得到一个完整的 maven 项目，打开熟悉的 ide，将项目导入到 ide 中。

> 我们生成了一个空的 Spring Boot 项目，稍后的所有操作都会基于这个项目完成。

#### 3.2 添加 ddd 支持
> 该项目主要基于领域模型进行构建，选择一个好的 DDD 封装会事半功倍。

[geekhale-ddd](https://gitee.com/litao851025/geekhalo-ddd) 是笔者针对 DDD 核心组件进行的封装，可以减少大量的重复代码，本实例就是基于此进行的构建。

添加 Geekhalo DDD 相关依赖。
```
<!-- 添加 ddd 相关支持-->
<dependency>
	<groupId>com.geekhalo</groupId>
	<artifactId>gh-ddd-lite</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>com.geekhalo</groupId>
	<artifactId>gh-ddd-lite-spring</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 添加 code gen 依赖，将自动启用 EndpointCodeGenProcessor 处理器-->
<!--编译时有效即可，运行时，不需要引用-->
<dependency>
	<groupId>com.geekhalo</groupId>
	<artifactId>gh-ddd-lite-codegen</artifactId>
	<version>1.0.1-SNAPSHOT</version>
	<scope>provided</scope>
</dependency>
```
添加代码生成器插件。
```
<plugin>
	<groupId>com.mysema.maven</groupId>
	<artifactId>apt-maven-plugin</artifactId>
	<version>1.1.3</version>
	<executions>
		<execution>
			<goals>
				<goal>process</goal>
			</goals>
			<configuration>
				<outputDirectory>target/generated-sources/java</outputDirectory>
				<processors>
					<!--添加 Querydsl 处理器-->
					<processor>com.querydsl.apt.QuerydslAnnotationProcessor</processor>
					<!--添加 DDD 处理器-->
					<processor>com.geekhalo.ddd.lite.codegen.DDDCodeGenProcessor</processor>
				</processors>
			</configuration>
		</execution>
	</executions>
</plugin>
```

#### 3.3 添加 protostuff 支持
> protostuff 是对 protobuf 的一种封装，大大提升了对象序列化和反序列化的性能。

本实例，使用 protostuff 完成 redis 的序列化和反序列化。

添加 protostuff 依赖。
```
<dependency>
	<groupId>com.dyuproject.protostuff</groupId>
	<artifactId>protostuff-runtime</artifactId>
	<version>${dyuproject.version}</version>
</dependency>
<dependency>
	<groupId>com.dyuproject.protostuff</groupId>
	<artifactId>protostuff-core</artifactId>
	<version>${dyuproject.version}</version>
</dependency>
<dependency>
	<groupId>com.dyuproject.protostuff</groupId>
	<artifactId>protostuff-api</artifactId>
	<version>${dyuproject.version}</version>
</dependency>
```

添加全局属性，以指定版本。
```
<properties>
	<dyuproject.version>1.0.8</dyuproject.version>
</properties>
```

#### 3.4 添加 swagger2 支持
> Swagger 是一个 Rest API 管理工具，提供的 Swagger-ui，可以方便的进行接口测试。

本实例中，使用 swagger-ui 进行接口测试。

添加 swagger 依赖。
```
<dependency>
	<groupId>io.springfox</groupId>
	<artifactId>springfox-swagger2</artifactId>
	<version>2.7.0</version>
</dependency>
<dependency>
	<groupId>io.springfox</groupId>
	<artifactId>springfox-swagger-ui</artifactId>
	<version>2.7.0</version>
</dependency>
```
在 main 函数所在类添加 @EnableSwagger2 注解，以启用 Swagger 功能。
```

@SpringBootApplication
@EnableSwagger2
public class LikeApplicationBootstrap {

	public static void main(String[] args) {
		SpringApplication.run(LikeApplicationBootstrap.class, args);
	}
}
```
#### 3.5 完善配置信息
> Spring Boot 的配置信息主要在 application.properties 文件中。

该实例所使用的中间件主要包括 MySQL 和 Redis，需要对其进行配置。

添加 MySQL 配置。
```
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/db_like?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.username=root
spring.datasource.password=
```

添加 Redis 配置。
```
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.database=1
```

由于，使用 jpa 作为持久化工具，为了方便调试，把 JPA 的 SQL 打印出来。
```
spring.jpa.show-sql=true
```

### 4 核心组件开发

#### 4.1 Owner & Target
> Owner 和 Target 是与系统交互的两大角色，贯穿系统的核心流程。

根据上述业务分析可以知，Owner 和 Target 应该以值对象进行建模，其中还需要兼顾多种类型的场景。

###### 4.1.1 **Owner**
```
@Embeddable
@Data
public class Owner implements ValueObject {

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "owner_type", updatable = false, nullable = false)
    @Convert(converter = CodeBasedOwnerTypeConverter.class)
    private OwnerType type;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "owner_id", updatable = false, nullable = false)
    private Long id;

    /**
     * 创建类型为 User 的 Owner
     * @param id
     * @return
     */
    public static Owner applyUser(Long id){
        Preconditions.checkArgument(id != null);
        Owner owner = new Owner();
        owner.setType(OwnerType.USER);
        owner.setId(id);
        return owner;
    }
}
```
值对象 **Owner** 存在以下特点：
- 实现 **ValueObject** 接口，以标记为值对象。
- 所有属性的 setter 全部为 private，构建不可变对象
- 提供静态的 applyUser 方法，以保证 Owner 的完整构建
- 添加 JPA 相关注解，以方便持久化处理

Owner 使用 OwnerType 来表示类别，OwnerType 定义如下：
```
@GenCodeBasedEnumConverter
public enum OwnerType implements CodeBasedEnum<OwnerType>, SelfDescribedEnum {
    USER(1, "普通用户");
    private final int code;
    private final String descr;

    OwnerType(int code, String descr) {
        this.code = code;
        this.descr = descr;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.descr;
    }
}
```
OwnerType 具有如下特点：
- 实现 CodeBasedEnum 接口，使用 code 进行持久化。
- 实现 SelfDescribedEnum 接口，可以通过 getDescription 方法获取描述信息
- 使用 @GenCodeBasedEnumConverter 自动生成 JPA 转化器

自动生成的 JPA 转为器如下：
```
public final class CodeBasedOwnerTypeConverter implements AttributeConverter<OwnerType, Integer> {
  public Integer convertToDatabaseColumn(OwnerType i) {
    return i == null ? null : i.getCode();
  }

  public OwnerType convertToEntityAttribute(Integer i) {
    if (i == null) return null;
    for (OwnerType value : OwnerType.values()){
    	if (value.getCode() == i){
    		return value; 
    	}
    }
    return null;
  }
}
```
该转化器使用 code 在枚举和持久化类型间进行转化。

###### 4.1.2 **Target**
> Target 和 Owner 非常类似，在这就不做太多解释。

**Target** 定义如下：
```
@Data
@Embeddable
public class Target implements ValueObject {
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "target_type", updatable = false, nullable = false)
    @Convert(converter = CodeBasedTargetTypeConverter.class)
    private TargetType type;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "target_id", updatable = false, nullable = false)
    private Long id;

    /**
     * 创建类型为 News 的 Target
     * @param newsId
     * @return
     */
    public static Target applyNews(Long newsId){
        Preconditions.checkArgument(newsId != null);
        Target target = new Target();
        target.setType(TargetType.NEWS);
        target.setId(newsId);
        return target;
    }
}
```

**TargetType** 定义如下：
```
@GenCodeBasedEnumConverter
public enum TargetType implements CodeBasedEnum<TargetType>, SelfDescribedEnum {
    NEWS(1, "新闻");
    private final int code;
    private final String desc;

    TargetType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return desc;
    }
}
```

#### 4.2 Like 模块
> Like 是该系统的核心，承载核心业务逻辑。

##### 4.2.1 **Like**
> Like 本身是一个实体，也是一个独立的聚合。

```
@GenSpringDataRepository
@com.geekhalo.ddd.lite.codegen.repository.Index(value = {"owner", "target"}, unique = true)

@QueryEntity
@Data
@Entity
@Table(name = "tb_like")
public class Like extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Owner owner;

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private Target target;

    @Setter(AccessLevel.PRIVATE)
    @Convert(converter = CodeBasedLikeStatusConverter.class)
    private LikeStatus status;


    private Like(){

    }

    public static Like create(Owner owner, Target target){
        Preconditions.checkArgument(owner != null);
        Preconditions.checkArgument(target != null);

        Like like = new Like();
        like.setOwner(owner);
        like.setTarget(target);
        // 进行初始化操作，以构建完整的对象实例
        like.init();
        return like;
    }

    private void init(){
        setStatus(LikeStatus.CANCELLED);
    }

    public void click(){
        getStatus().click(this);
    }

    void cancel(){
        setStatus(LikeStatus.CANCELLED);
        LikeCancelledEvent likeCancelledEvent = new LikeCancelledEvent(this);
        registerEvent(likeCancelledEvent);
    }

    void submit(){
        setStatus(LikeStatus.SUBMITTED);
        LikeSubmittedEvent likeSubmittedEvent = new LikeSubmittedEvent(this);
        registerEvent(()-> likeSubmittedEvent);
    }

    public boolean isLiked() {
        return getStatus() == LikeStatus.SUBMITTED;
    }
}
```
**Like** 聚合有以下几个特点：
- 继承自 JpaAggregate，以拥有聚合的通用操作
- owner、target 属性 setter 为 private，在对象创建后，便不会进行修改
- status 属性 setter 为 private，不允许直接进行修改，只能通过 init、submit、cancel 等业务方法进行修改
- 提供静态的 create 方法，充当 Like 的构造器，以完成 Like 的完整构建，其中包括参数验证、init 方法调用
- submit、cancel 业务方法完成 status 的修改的同时，通过 registerEvent 方法进行领域事件的注册
- 存在很多的注解，包括 JPA 的映射注解、Querydsl 的标记注解、GenXXX 的代码生成注解

**Like** 对应的建表 SQL 如下：
```
CREATE TABLE `tb_like` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `owner_id` bigint(20) NOT NULL,
  `owner_type` int(11) NOT NULL,
  `status` int(11) DEFAULT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_owner_target` (`owner_type`,`owner_id`,`target_type`,`target_id`)
);
```

##### 4.2.2 **LikeStatus**
> LikeStatus 主要用于表示 Like 的状态，并根据当前状态触发对于的业务逻辑。

LikeStatus 是基于枚举构建的值对象。
```
@GenCodeBasedEnumConverter
public enum LikeStatus implements CodeBasedEnum<LikeStatus> {
    SUBMITTED(1){
        @Override
        public void click(Like like) {
            like.cancel();
        }
    },
    CANCELLED(0){
        @Override
        public void click(Like like) {
            like.submit();
        }
    };

    private final int code;

    LikeStatus(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public abstract void click(Like like);
}
```
需要说明的是 click 方法，LikeStatus 会根据当前状态，调用 Like 的不同方法。

##### 4.2.3 **LikeEvents**
> LikeSubmittedEvent 和 LikeCancelledEvent 是内部领域事件。

```
@Value
public class LikeSubmittedEvent extends AbstractAggregateEvent<Long, Like> {
    public LikeSubmittedEvent(Like source) {
        super(source);
    }

    public LikeSubmittedEvent(String id, Like source) {
        super(id, source);
    }
}
```
LikeSubmittedEvent  继承自 AbstractAggregateEvent<Long, Like> ，以拥有聚合内部事件的特性。

```
@Value
public class LikeCancelledEvent extends AbstractAggregateEvent<Long, Like> {
    public LikeCancelledEvent(Like source) {
        super(source);
    }
}
```
LikeCancelledEvent 与 LikeSubmittedEvent 基本一致，在此不做过多解释。

> 有的同学会存在一些疑问，两个领域事件内部没有任何属性，这样的空类有什么意义？

> 第一，父类 AbstractAggregateEvent 已经提供了很多通用信息。
其次，Like 聚合已经含有大量上下文数据。
最后，类型本身就是非常有意义的信息。

##### 4.2.4 **LikeRepository**
> 每个聚合都会有一个自己的 Repository 类。

```
public interface LikeRepository extends BaseLikeRepository{
    List<Like> getByOwnerAndTargetIn(Owner owner, List<Target> targets);
}
```
**LikeRepository** 继承自 BaseLikeRepository，并添加 getByOwnerAndTargetIn 方法。getByOwnerAndTargetIn 符合 Spring Data 的命名规则，框架会为其提供具体的实现。

BaseLikeRepository 怎么来的呢？
```
interface BaseLikeRepository extends AggregateRepository<Long, Like>, Repository<Like, Long>, QuerydslPredicateExecutor<Like> {
  Long countByOwner(Owner owner);

  default Long countByOwner(Owner owner, Predicate predicate) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    booleanBuilder.and(QLike.like.owner.eq(owner));;
    booleanBuilder.and(predicate);
    return this.count(booleanBuilder.getValue());
  }

  List<Like> getByOwner(Owner owner);

  List<Like> getByOwner(Owner owner, Sort sort);

  default List<Like> getByOwner(Owner owner, Predicate predicate) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    booleanBuilder.and(QLike.like.owner.eq(owner));;
    booleanBuilder.and(predicate);
    return Lists.newArrayList(findAll(booleanBuilder.getValue()));
  }

  default List<Like> getByOwner(Owner owner, Predicate predicate, Sort sort) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    booleanBuilder.and(QLike.like.owner.eq(owner));;
    booleanBuilder.and(predicate);
    return Lists.newArrayList(findAll(booleanBuilder.getValue(), sort));
  }

  Page<Like> findByOwner(Owner owner, Pageable pageable);

  default Page<Like> findByOwner(Owner owner, Predicate predicate, Pageable pageable) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();
    booleanBuilder.and(QLike.like.owner.eq(owner));;
    booleanBuilder.and(predicate);
    return findAll(booleanBuilder.getValue(), pageable);
  }

  Optional<Like> getByOwnerAndTarget(Owner owner, Target target);
}
```
**BaseLikeRepository** 是由框架自动生成的，确切来说是根据 Like 类上的 @GenSpringDataRepository 注解生成的。

框架读取 Like 的 @Index(value = {"owner", "target"}, unique = true) 注解，并以此生成大量的查询方法。我们无需关心这些方法的实现，直接使用即可。在运行时，框架会自动完成代理对象的创建，并为我们提供具体的实现逻辑。

##### 4.2.5 **LikeApplication**
> LikeApplication 根据用户用例进行构建。

```
@Service
public class LikeApplication extends AbstractApplication {
    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DomainEventBus domainEventBus;

    public void click(Owner owner, Target target){
        syncerFor(this.likeRepository)
                .publishBy(domainEventBus)
                .loadBy(() -> this.likeRepository.getByOwnerAndTarget(owner, target))
                .instance(()-> Like.create(owner, target))
                .update(like -> like.click())
                .call();
    }

    public List<Like> getByOwnerAndTargets(Owner owner, List<Target> targets){
        return this.likeRepository.getByOwnerAndTargetIn(owner, targets);
    }
}
```
**LikeApplication** 继承自 AbstractApplication，以获取通用方法。

需要说明的是 click 方法。xxxxFor 是 AbstractApplication 提供的一组通用模板方法，使用 builder 模式对流程进行定制。有兴趣的同学，可以查看下源码。

XXXFor 包括：

方法 | 含义
---|---
creatorFor | 返回 Creator，完成对创建逻辑的封装
updaterFor | 返回 Updater，完成对更新逻辑的封装
syncerFor | 返回 Syncer，完成对同步逻辑的封装。当持久存储中存在时，进行更新；不存在时进行创建


#### 4.3 LikeLogger 模块

##### 4.3.1 **LikeLogger**
> LikeLogger 是一个不变实体。

```
@GenSpringDataRepository
@QueryEntity
@Data
@Entity
@Table(name = "tb_like_logger")
public class LikeLogger extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Owner owner;

    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Target target;

    @Convert(converter = CodeBasedActionTypeConverter.class)
    @Setter(AccessLevel.PRIVATE)
    private ActionType actionType;

    private LikeLogger(){

    }

    public static LikeLogger createLikeAction(Owner owner, Target target){
        LikeLogger likeLogger = new LikeLogger();
        likeLogger.setActionType(ActionType.LIKE);
        likeLogger.setOwner(owner);
        likeLogger.setTarget(target);
        return likeLogger;
    }

    public static LikeLogger createCancelAction(Owner owner, Target target){
        LikeLogger likeLogger = new LikeLogger();
        likeLogger.setActionType(ActionType.CANCEL);
        likeLogger.setOwner(owner);
        likeLogger.setTarget(target);
        return likeLogger;
    }
}
```

**LikeLogger** 具体一下特定：
- 继承自 JpaAggregate 
- 所有属性的 setter 都是 private，创建后便不能修改
- 提供 createLikeAction、createCancelAction 等静态方法提供 LikeLogger 的创建
 
**LikeLogger** 对应的建表 SQL 如下：
```
CREATE TABLE `tb_like_logger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `action_type` int(11) DEFAULT NULL,
  `owner_id` bigint(20) NOT NULL,
  `owner_type` int(11) NOT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);
```

##### 4.3.2 **ActionType**
> ActionType 是个基于枚举的值对象。

```
@GenCodeBasedEnumConverter
public enum ActionType implements CodeBasedEnum<ActionType> {
    LIKE(1), CANCEL(0);

    private final int code;

    ActionType(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
```

##### 4.3.3 **LikeLoggerRepository**
> LikeLoggerRepository 是聚合 LikeLogger 的存储仓库。

```
public interface LikeLoggerRepository extends BaseLikeLoggerRepository{
}
```
BaseLikeLoggerRepository 为框架自动生成，在此不做过多介绍。


##### 4.3.4 **LikeLoggerApplication**
```
@Service
public class LikeLoggerApplication extends AbstractApplication {

    @Autowired
    private LikeLoggerRepository repository;


    public void createLikeAction(Owner owner, Target target) {
        creatorFor(this.repository)
                .instance(()-> LikeLogger.createLikeAction(owner, target))
                .call();
    }

    public void createCancelAction(Owner owner, Target target) {
        creatorFor(this.repository)
                .instance(()-> LikeLogger.createCancelAction(owner, target))
                .call();
    }
}
```
LikeLoggerApplication 相对来说比较简单，直接使用 Creator 完成聚合实例的存储。


#### 4.4 TargetCount 模块

##### 4.4.1 **TargetCount**
> TargetCount 以聚合根进行建模。

```
@GenSpringDataRepository
@Index(value = "target", unique = true)

@QueryEntity
@Data
@Entity
@Table(name = "tb_target_count")
public class TargetCount extends JpaAggregate {
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    private Target target;

    @Setter(AccessLevel.PRIVATE)
    private Long count;

    private TargetCount(){

    }

    public static TargetCount create(Target target){
        TargetCount count = new TargetCount();
        count.setTarget(target);
        count.init();
        return count;
    }

    private void init(){
        setCount(0L);
    }

    public void incr(int by){
        setCount(getCount() + by);
    }

    public void decr(int by){
        setCount(getCount() - by);
    }

    private void setCount(Long count){
        if (count >= 0){
            this.count = count;
        }else {
            this.count = 0L;
        }
    }
}
```
**TargetCount** 具有以下特点：
- 继承自 JpaAggregate
- 属性 target 的 setter 为 private，创建后便不可修改
- 属性 count 的 setter 为 private，只能通过 init、incr、decr 等业务方法访问
- 属性 count 的 setter，添加业务规则，当输入小于零，直接设置为零，以避免出现负值数值的尴尬
- 提供静态的 create 方法，完成 TargetCount 的创建，已维护创建的完整性

**TargetCount** 所对应的建表 SQL 如下：
```
CREATE TABLE `tb_target_count` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `version` int(11) NOT NULL,
  `count` bigint(20) DEFAULT NULL,
  `target_id` bigint(20) NOT NULL,
  `target_type` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_target` (`target_type`,`target_id`)
);
```

###### 4.4.2 **TargetCountRepository**
> TargetCountRepository 是聚合 TargetCount 所对应的存储仓库。

```
public interface TargetCountRepository extends BaseTargetCountRepository{
    List<TargetCount> getByTargetIn(List<Target> targets);
}
```
**TargetCountRepository** 继承自 BaseTargetCountRepository，并添加查询方法 getByTargetIn。

###### 4.4.3 **TargetCountApplication**

```
@Service
public class TargetCountApplication extends AbstractApplication {
    @Autowired
    private TargetCountRepository targetCountRepository;

    @Autowired
    private RedisTemplate<Target, TargetCount> targetCountRedisTemplate;

    public void incr(Target target, int by){
        this.syncerFor(this.targetCountRepository)
                .loadBy(() -> this.targetCountRepository.getByTarget(target))
                .instance(() -> TargetCount.create(target))
                .update(targetCount -> targetCount.incr(by))
                .call();
        this.targetCountRedisTemplate.delete(target);
    }

    public void decr(Target target, int by){
        this.syncerFor(this.targetCountRepository)
                .loadBy(()-> this.targetCountRepository.getByTarget(target))
                .instance(()-> TargetCount.create(target))
                .update(targetCount -> targetCount.decr(by))
                .call();
        this.targetCountRedisTemplate.delete(target);
    }

    public List<TargetCount> countOfTargets(List<Target> targets){
        List<TargetCount> result = Lists.newArrayList();
        List<TargetCount> dataFromCache = this.targetCountRedisTemplate.opsForValue().multiGet(targets);
        for (int i= 0; i< targets.size();i++){
            TargetCount targetCount = dataFromCache.get(i);
            if (targetCount == null){
                Target target = targets.get(i);
                Optional<TargetCount> targetCountOptional = this.targetCountRepository.getByTarget(target);
                if (targetCountOptional.isPresent()){
                    this.targetCountRedisTemplate.opsForValue().set(target, targetCountOptional.get());
                    targetCount = targetCountOptional.get();
                }

            }
            result.add(targetCount);
        }
        return result;
    }
}
```


**TargetCountApplication** 具有以下特征：
- 继承自 AbstractApplication
- 提供 incr、decr等命令方法，在命令方法执行完成后，调用 RedisTemplate 对 Redis 的过期数据进行清理。
- **countOfTargets** 方法首先从 Redis 中获取数据，如果缓存未命中，则从 MySQL 中获取，并同步到 Redis 中
 
###### 4.4.4 **RedisTemplateConfiguration**
> 在 TargetCountApplication 中，使用 RedisTemplate 对 Redis 进行操作，其中 RedisTemplate Bean 的声明就在 RedisTemplateConfiguration 中。

```
@Configuration
public class RedisTemplateConfiguration {

    @Bean
    public RedisTemplate<Target, TargetCount> targetCountRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Target, TargetCount> targetCountRedisTemplate = new RedisTemplate<>();
        targetCountRedisTemplate.setConnectionFactory(redisConnectionFactory);
        targetCountRedisTemplate.setKeySerializer(new RedisSerializer<Target>() {
            @Override
            public byte[] serialize(Target target) throws SerializationException {
                String key = new StringBuilder()
                        .append("target:").append(target.getType().getCode()).append(":")
                        .append(target.getId())
                        .toString();

                return key.getBytes();
            }

            @Override
            public Target deserialize(byte[] bytes) throws SerializationException {
                return null;
            }
        });
        targetCountRedisTemplate.setValueSerializer(new RedisSerializer<TargetCount>(){
            private final RuntimeSchema<TargetCount> schema =
                    RuntimeSchema.createFrom(TargetCount.class, new DefaultIdStrategy());

            @Override
            public byte[] serialize(TargetCount targetCount) throws SerializationException {
                if (targetCount == null) {
                    return new byte[0];
                }else {
                    return ProtobufIOUtil.toByteArray(targetCount,
                            this.schema,
                            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                }
            }

            @Override
            public TargetCount deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0){
                    return null;
                }
                TargetCount t = schema.newMessage();
                ProtobufIOUtil.mergeFrom(bytes, t, schema);
                return t;
            }
        });
        return targetCountRedisTemplate;
    }
}

```
**RedisTemplateConfiguration** 通过 Spring 的 Java Config 声明 RedisTemplate Bean，创建逻辑如下：
- 设置 redisConnectionFactory
- 使用字符串拼接方式，构建 KeySerializer，Key 规则为 target:{targetTypeCode}:{targetTypeId}
- 使用 protostuff 构建 ValueSerializer

#### 4.5 RedisBasedQueue 模块
> 实例项目中有使用分布式消息队列，而是使用 Redis 的 List 结构模拟队列行为。

我们使用 Redis 的 List 结构来模拟分布式队列。
```
@Component
public class RedisBasedQueue {
    private static final String COMMAND_CLICK = "queue.command.click";
    private static final String EVENT_LIKE = "queue.event.like";
    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    private FastJsonConfig fastJsonConfig;

    private RedisBasedQueue(){
        this.fastJsonConfig = new FastJsonConfig();
        ParserConfig parserConfig = new ParserConfig(true);
        parserConfig.setAutoTypeSupport(true);
        fastJsonConfig.setParserConfig(parserConfig);
        fastJsonConfig.setSerializeConfig(new SerializeConfig(true));
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);
    }

    public void pushClickCommand(OwnerAndTarget ownerAndTarget){
        redisTemplate.boundListOps(COMMAND_CLICK).leftPush(JSON.toJSONString(ownerAndTarget, this.fastJsonConfig.getSerializerFeatures()));
    }

    public OwnerAndTarget popClickCommand(){
        String json = redisTemplate.boundListOps(COMMAND_CLICK).rightPop(1, TimeUnit.SECONDS);
        if (StringUtils.isEmpty(json)){
            return null;
        }
        return JSON.parseObject(json, OwnerAndTarget.class, this.fastJsonConfig.getParserConfig());
    }

    public void pushLikeEvent(AbstractLikeEvent event){
        redisTemplate.boundListOps(EVENT_LIKE).leftPush(JSON.toJSONString(event, this.fastJsonConfig.getSerializerFeatures()));
    }

    public AbstractLikeEvent popLikeEvent(){
        String json = redisTemplate.boundListOps(EVENT_LIKE).rightPop(1, TimeUnit.SECONDS);
        if (StringUtils.isEmpty(json)){
            return null;
        }
        return (AbstractLikeEvent) JSON.parse(json, this.fastJsonConfig.getParserConfig());
    }
}
```
**RedisBasedQueue** 主要完成：
- 使用 FastJSON 进行序列化与反序列化
- 提供 pushClickCommand 和 popClickCommand 方法，处理点击命令
- 提供 pushLikeEvent 和 popLikeEvent 方法，处理赞事件

### 5 核心流程开发
> 核心组件开发完成后，接下来需要通过核心流程将其整体串起来。

#### 5.1 用户点击流程
> 该流程主要用于收集用户点击，并将点击命令提交到队列中。

```

@Api("LikeApi")
@RestController
@RequestMapping("like")
public class LikeController {
    @Autowired
    private LikeApplication likeApplication;

    @Autowired
    private RedisBasedQueue redisBasedQueue;

    @ResponseBody
    @PostMapping("click")
    public ResultVo<Void> click(@RequestBody OwnerAndTarget ownerAndTarget){
        this.redisBasedQueue.pushClickCommand(ownerAndTarget);
        return ResultVo.success();
    }
}
```
**LikeController** 接收用户提交数据，并将其 push 到 Redis 队列中。

#### 5.2 点击命令处理流程
> 该流程，从队列中获取点击命令，并调用业务方法执行业务命令。

```
@Service
public class ClickCommandConsumer implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickCommandConsumer.class);

    @Autowired
    private LikeApplication likeApplication;
    @Autowired
    private RedisBasedQueue redisBasedQueue;

    private ExecutorService executorService;

    @Override
    public void start() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("ClickCommandConsumer-Thread-%d")
                .daemon(true)
                .build();
        this.executorService = Executors.newSingleThreadExecutor(basicThreadFactory);
        this.executorService.submit(new CommandRunner());
    }

    @Override
    public void stop() {
        this.executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private class CommandRunner implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                    OwnerAndTarget ownerAndTarget = redisBasedQueue.popClickCommand();
                    if (ownerAndTarget != null){
                        try {
                            LOGGER.info("hand click command {}", ownerAndTarget);
                            likeApplication.click(ownerAndTarget.getOwner(), ownerAndTarget.getTarget());
                        }catch (Exception e){
                            LOGGER.error("failed to handle command {}", ownerAndTarget, e);
                        }

                    }
            }
        }
    }
}
```
**ClickCommandConsumer** 主要完成：
- 实现 SmartLifecycle，通过 Spring 的生命周期，管理线程池生命周期
- start 方法，创建 ExecutorService，并启动后台任务
- stop 方法，关闭线程池
- CommandRunner，从 redisBasedQueue 获取 OwnerAndTarget，并调用 likeApplication 的业务方法

#### 5.3 领域事件发布流程
> 该流程，主要将内部领域事件转化成外部领域事件，并将其 push 到 Redis 队列中。

```
@Component
public class RedisBasedQueueExporter {
    @Autowired
    private RedisBasedQueue redisBasedQueue;

    @EventListener
    public void handle(LikeSubmittedEvent likeSubmittedEvent){
        SubmittedEvent submittedEvent = new SubmittedEvent();
        submittedEvent.setOwner(likeSubmittedEvent.getSource().getOwner());
        submittedEvent.setTarget(likeSubmittedEvent.getSource().getTarget());
        this.redisBasedQueue.pushLikeEvent(submittedEvent);
    }


    @EventListener
    public void handle(LikeCancelledEvent likeCancelledEvent){
        CanceledEvent canceledEvent = new CanceledEvent();
        canceledEvent.setOwner(likeCancelledEvent.getSource().getOwner());
        canceledEvent.setTarget(likeCancelledEvent.getSource().getTarget());
        this.redisBasedQueue.pushLikeEvent(canceledEvent);
    }

}
```
**RedisBasedQueueExporter** 使用 Spring 的 EventListener 机制，接收内部领域事件，并完成到队列的转化。


#### 5.4 领域事件处理流程
> 该流程，用于从队列中获取领域事件，然后进行业务处理。

```
@Service
public class LikeEventConsumer implements SmartLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(LikeEventConsumer.class);

    @Autowired
    private LikeLoggerApplication likeLoggerApplication;

    @Autowired
    private TargetCountApplication targetCountApplication;

    @Autowired
    private RedisBasedQueue redisBasedQueue;

    private ExecutorService executorService;

    @Override
    public void start() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder()
                .namingPattern("LiekEventConsumer-Thread-%d")
                .daemon(true)
                .build();
        executorService = Executors.newSingleThreadExecutor(basicThreadFactory);
        executorService.submit(new DispatcherTask());
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private class DispatcherTask implements Runnable{

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                AbstractLikeEvent abstractEvent = redisBasedQueue.popLikeEvent();
                if (abstractEvent != null){
                    try {
                        LOGGER.info("handle like event {}", abstractEvent);
                        if (abstractEvent instanceof SubmittedEvent){
                            handle((SubmittedEvent) abstractEvent);
                        }
                        if (abstractEvent instanceof CanceledEvent){
                            handle((CanceledEvent) abstractEvent);
                        }
                    }catch (Exception e){
                        LOGGER.error("failed to handle event {}", abstractEvent, e);
                    }
                }
            }
        }
    }

    private void handle(SubmittedEvent submittedEvent){
        saveLogger(submittedEvent);
        updateCount(submittedEvent);
    }

    private void handle(CanceledEvent canceledEvent){
        saveLogger(canceledEvent);
        updateCount(canceledEvent);
    }

    private void saveLogger(SubmittedEvent submittedEvent){
        this.likeLoggerApplication.createLikeAction(submittedEvent.getOwner(), submittedEvent.getTarget());
    }


    private void saveLogger(CanceledEvent canceledEvent){
        this.likeLoggerApplication.createCancelAction(canceledEvent.getOwner(), canceledEvent.getTarget());
    }

    private void updateCount(SubmittedEvent submittedEvent){
        this.targetCountApplication.incr(submittedEvent.getTarget(), 1);
    }



    private void updateCount(CanceledEvent canceledEvent){
        this.targetCountApplication.decr(canceledEvent.getTarget(), 1);
    }
}
```

**LikeEventConsumer** 与 ClickCommandConsumer 非常类似，主要用于管理线程池生命周期；启动后台任务，从 Redis 队列中 获取 LikeEvent 并调用 LikeLoggerApplication 和 TargetCountApplication 执行业务逻辑。


### 6 项目测试
> 到此，我们的项目就开发完了，让我们通过 Swagger 进行简单测试。

#### 6.1 启动项目
> 首先，启动我们的项目。

在终端上使用 mvn clean spring-boot:run ，启动项目。
当控制台中出现以下信息，说明项目启动成功。

![启用成功](http://litao851025.gitee.io/books-image/chat/like/swagger_start.png)

在浏览器中输入 http://127.0.0.1:8080/swagger-ui.html ，打开 swagger-ui ，如下：

![swagger-ui](http://litao851025.gitee.io/books-image/chat/like/swagger-ui.png)

#### 6.2 like click
在 like-controller 中的 click api 中输入以下数据。
```
{
  "owner": {
    "id": 1,
    "type": "USER"
  },
  "target": {
    "id": 1,
    "type": "NEWS"
  }
}
```
点击 “Try it out” 执行情况，获得以下结果。

![like click result](http://litao851025.gitee.io/books-image/chat/like/swagger-like-click-result.png)

从控制台中，可以看到如下输出。

![like click console](http://litao851025.gitee.io/books-image/chat/like/swagger-like-click-console.png)

从控制台中可以看到三条 insert 语句。查看数据库，数据已经进来。

![target count](http://litao851025.gitee.io/books-image/chat/like/swagger-like-result-count.png)

![like logger](http://litao851025.gitee.io/books-image/chat/like/swagger-like-result-logger.png)

#### 6.3 like get-by-targets
在 like 的 get-by-targets api 中输入以下数据：
```
{
  "owner": {
    "id": 1,
    "type": "USER"
  },
  "targets": [
    {
      "id": 1,
      "type": "NEWS"
    }
  ]
}
```
点击“Try it out”，获取查询结果。

![get by targets result](http://litao851025.gitee.io/books-image/chat/like/swagger-get-by-targets-result.png)

说明，已经成功获取数据。

#### 6.4 Count count-of-targets
在 target count 的 count-of-targets API 中输入以下数据：
```
{
  "values": [
    {
      "id": 1,
      "type": "NEWS"
    }
  ]
}
```
点击“Try it out”，获取执行结果。

![count of target result](http://litao851025.gitee.io/books-image/chat/like/swagger-cout-of-targets-result.png)

可见，点赞数量为 1。

### 7 结尾
打完收工...