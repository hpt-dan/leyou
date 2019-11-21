# 乐优商城开发
# 一：需求
## 1.1：后台管理
### 1.1.1：分类的增删改
### 1.1.2：品牌的增删改，品牌的搜索
### 1.1.3：规格参数的增删改
### 1.1.4：商品的增删改
## 1.2：搜索
#### 1.2.1：输入关键子，在搜索页面显示与该关键字的有关的品牌分类。和该关键字有关的可以供搜索的规格参数和对应的值。商品的一些简介，并且提供分页
#### 1.2.2：我们点击品牌和分类和这些规格参数能够对搜索结果进行过滤。
## 1.3：登录注册
### 1.3.1：能够用短信进行注册
### 1.3.2：单点登录，并保证客服的安全性
## 1.4：购物车
### 1.4.1：商品详情页的显示
### 1.4.2：无登录的购物车
### 1.4.3：登录状态下的购物车
### 1.4.4：登录后，将无登录的购物车里的商品进行合并
## 1.5：订单支付
### 1.5.1：能够下订单
### 1.5.2：订单的微信支付
### 1.5.3：订单的定时清理

## 1.6：项目的整体架构
  该微服务项目整体架构：SpringBoot、SpringCloud、Nginx、Tomacat、RestFul风格。项目实现商品浏览、ES全局搜索、无状态登录、短信注册、购物车、订单、支付。
# 二：后台管理
## 2.1：微服务列表(根据该服务是否会被别的微服务调用)：
1：网关微服务
2：注册微服务
3：公共模块
4：商品微服务(Service,Pojo,Interface)
5：上传微服务
## 2.2：需求流程：
1:分类的展示，添加，修改，删除
2:品牌的展示，含有分页和搜索，添加(图片的上传)，修改，删除
3:商品的新增，修改，删除,商品的上架，下架。
4:点击第三级类，查询显示对应类的规格参数组，点击规格参数组，显示对应的规格参数
5:规格参数组和规格参数的添加，修改，删除。
## 2.3：技术点的分析：
#### 项目集群和nginx:
整个项目是前后端分离的前后端分布式项目，支持高并发高可用。通过nginx的反向代理，使前后端项目共享一个服务集群。我们只需要访问nginx，根据域名代理到项目的服务。
#### 文件的上传原理:
图片的上传在很多地方都会用到，所有将文件上传变为一个微服务。图片不能保存在服务器内部，会让服务器产生额外的加载负担。所以我们把图片上传到nginx。
#### 为什么要绕过网关：
但是文件上传的过成中会经过网关，图片流量很大，发生读写，对网络的负担加重，在高并发的时候，会使网络堵塞，使zull网关不能用，整个网络瘫痪，所以在图片上传到nginx，我们要绕过网关缓存(即为经过网关不发生读写)。
#### 未什么要使用阿里的OSS：
单机存储，存储有限
并发能力差
数据没有备份，有单点的风险
阿里的OSS提供海量的，安全的低成本的，高可用的云存储。数据持久性不低于99.99%，且具有restFul的API。
#### 解决跨域问题:
因为我们前后端使分离，前端发送ajax请求会引起跨域问题。用cors也叫跨域资源共享，解决跨域问题。CORS通信和Ajax请求没有区别，浏览器会在请求头携带一些消息。我们由此来判断期是否跨域，然后在响应头添加一些消息，通过过滤器完成。
在发送特殊请求的时候，先发送一次预检请求，浏览器先询问服务器，是否能确定响应(需要在响应头添加一些信息),如果得到确定回答，发送正式的请求。
规格参数和规格参数组为什么这样设计:
数据库的设计，既要减少表的重复性字段，又要简化开发
因为很多的电商网站，对于同类的商品的规格参数属性基本是一样的，但是不同类的规格参数的属性是不一致的。如果按照之前的数据库设计理念，表的每一个字段都应该是规格参数的属性，表中的数据就是各个规格参数对应的值。所以有两种设计理念，所有的规格参数放在一张表中，由于商品种类众多，导致这张表很大，效率很低。如果是为每一个类设计一个规格参数表，这样表的数量很多很多，十分不合理。通过浏览京东发现每一个类的规格参数属性一样，规格参数的值不一样，所以对规格参数属性提取出来，一类对应于一个规格参数模板。商品的规格参数都 保存在spu中，里面
#### spu和sku的解释:
Spu和sku是一对多关系。所以spu是sku通用的商品属性，然后sku是自己特有的商品属性。所以购买的商品是sku+spu。
Spu和sku的一些特殊字段的解释:
浏览商品的详情页的时候，有些规格参数只有一个值，有些规格参数对应好几个值。所以对spu的规格参数分为通用规格参数和特有规格参数。每一个商品的规格参数应该是spu的规格参数+spu特有规格参数取出的值。
规格参数保存的是json字符串。Key为规格参数的id,值为规格参数的值。

# 三：搜索
## 3.1：服务列表
搜索微服务

## 3.2：需求流程
在搜索栏输入文字点击搜索能看到商品列表，类的集合，品牌的集合，可搜索的规格参数
点击过滤条件，对商品列表进行再次过滤
点击每一个商品进入商品详情页
## 3.3：实现流程
因为我们修改商品，我们需要对索引库进行修改，静态详情页需要进行修改。因为我们修改商品必须进行上架和下架。
商品下架我们删除商品的详情页和商品对应的索引库。商品上架的时候我们创建该商品的静态页面和索引库
需要中间插件RabbitMQ，因为商品的上架下架和生成索引库和静态页是不同的服务，应该降低不通过服务的影响，且是异步的异步的。我们在商品的上架把spuId放到上架的消息队列中，下架把spuId放到下架的消息队列中。让搜索服务，和生成静态页的服务监听这两个消息队列，当从消息队列中取出值，对索引库，和静态页面进行操作。
## 3.4：技术点的分析
### 3.4.1：生成静态页面的技术
因为不同人在不同的地点，访问同一个商品的详情页是一样的，所以我们应把访问详情页像访问静态页面一样，减少服务器压力。因此使用页面静态化技术(Thymeleaf)
Thymeleaf的技术内幕：运行上下文，模板解析器，模板引擎。运行上下文是用于保存页面渲染的数据，模板解析器用于读取模板的位置，模板的文件名，模板的文件类型。模板引擎用于解析模板的引擎，输出解析后的文件。
生成的静态页面放在nginx,然后利用nginx的动态代理，通过restFul风格来访问静态页面。
### 3.4.2：消息队列(RabbitMQ)
#### 概述：
消息队列是典型的生产者消费者模型，生产者不断的向队列中生产消息，消费者不断的向队列中获取消息。消息的生产和接受都是异步的在，没有业务的侵入，就实现生产者和消费者之间的解耦。
#### RabbitMQ五种消息模型：
1：简单消息模型：一个生产者对应一个消费者
2：work消息模型：一个生产者对应多个消费者
#### 订阅模型
中间多了一个交换机，只具有接受生产者的消息，不具有存储功能，接受生产者的消息，然后转换给多个队列，这样生产者的一条消息可以被多个消费者使用。
3：订阅模型-Fanout：广播模式，生产者生产的消息所有的队列都能接受
4：订阅模型-Direct：队列和交换机根据指定的RoutingKey进行绑定。交换机发送					  消息的时候也指定RoutingKey,只有两个RoutingKey一致才					   能接受到消息
5：订阅消息模型-Topic：Direct的基础上添加一些通配符。
#### 注意：
消息的确认机制ACK，消费者获取消息会向RabbitMQ发送ACK,告知消息被接受，消息确认机制分为两种：手动确认，自动确认。
#### RabbitMQ常见的面试题：
#### 避免消息得丢失
所以对一些比较重要的消息，我们采取手动的确认方式，防止RabbitMQ中消息丢失。但是在消费者消费之前消息对列就宕机了。我们需要对消息进行持久化，对列交换机都得持久化。
#### 避免消息的堆积
对同一个队列让多个消费者监听，实现消息的争抢，加快消息的消费
#### 避免消息的重复的消费
保证接口幂等，就不怕消息被重复的消费。比如一些接口天生幂等，比如查找修改，删除。对一些不幂等的接口如新增我们要根据具体的业务，在消费端通过业务判断是否执行过。
保证消息的有序性
在并发性要求不高的，让消息有序的发送到一个消息队列上，并让一个消费者来消费。
对于并发性要求高的，用多个队列接受消息 ，让一组消息通过hash的方式分派到固定的对立中。

### 3.4.3：索引库Elasticsearch
#### 为什么使用Elasticsearch保存搜索界面的信息
因为商品的数量十分的多，数据库的查询又是一行一行的查询，所以如果直接从数据库中查询效率很底下，采用Elasticsearch没有采用solr，因为Elasticsearch配置简单，而且具有接近实时更新性。
#### 怎样创建索引库和添加数据
首先参照一些比较出名的电商网站，比如京东的搜索页面包含的内容。然后设计我们存储到我们索引库的数据结构，goods对象。根据spuId查询数据，然后封装成goods对象，然后存储到索引库中。
搜索页面(前台传递：当前页数，搜索关键字，过滤的集合map集合)
第一部分：分页展示商品，首先构造自定义搜索条件，然后添加搜索条件(也就是把搜索栏输入的值)。然后在自定义搜索中添加分页条件。然后根据搜索条件查询结构。然后取出sku集合，存到分页对象中。然后响应到前台界面。
第二部分是：分类，品牌，可搜索规格参数展示，首先自定义搜索，然后添加搜索条件，然后添加分类id的聚合,添加品牌的id聚合。再根据第三级分类查询该类的规格参数集合。根据遍历该集合，对集合中的每一个元素添加聚合。然后根据搜索条件进行查询，得到Map<String, List<?>>，然后将该数据返回给客服端渲染。
#### 过滤查询：
前台传递过滤条件是map集合，只需要在之前添加搜索条件的时候，加上过滤条件。遍历map集合对每一个元素添加过滤条件。

Elasticsearch采用的是一个集群，开发的时候部署到Linux上。
# 四：登录注册
## 4.1：需求流程
能够用短信进行
能够进行登录
服务之间的鉴权
## 4.2：微服务列表
用户微服务
消息微服务
鉴定中心微服务
## 4.3：实现流程
### 4.3.1：注册
首先输入用户名然后会发送一个异步请求到后台接口，看看用户名是否重复，然后输入手机号也会发送一个异步请求到后台接口，看看该手机号是否注册。我们点击获取验证码，会发送一个异步请求参数为手机号，然后生成一个六位的随机数，然后手机号和验证码放到消息队列中(数据是一个map集合)，sms微服务则一直监控着这个消息对列，当消息队列中有消息则消息微服务消费消息并发送消息(采用阿里大于平台)。然后将验证码也放到redis中(map结构)，并设置有效期时间一般为5分钟。表单数据填写完毕，提交表单数据，后台获取数据后需要从redis取出验证码，进行判断如果验证码不正确直接给前端响应验证码不正确。然后对密码采用加盐加密，然后存到数据库中
注意：因为在redis中可用设置数据的有效时间，所以验证码存在redis中。
### 4.3.2：无状态登录
填写用户名和密码提交数据，跳转到鉴权中心，然后调用用户服务查询，用户名从数据库查询用户(每次从数据库查询的时候，需要进行非空判断)，然后将查询到的数据库中的密码和输入的密码进行解密比对，如果比对成功，返回用户信息。然后采用私钥加密生成token，然后将JWT放到cookie中响应到客服端。因为每次请求浏览器都会自动携带cookie使用httponly,避免XSS攻击。这样每次用户访问服务端都携带用户的token。然后浏览器再发送一个异步请求，鉴定中心采用公钥解密判断是否携带用户的tooken,和从redis中查询tooken是否在黑名单中。如果满足条件，则向浏览器响应。
#### 注意：
#### tooken保存问题：
localStorage：不用担心cookie禁用，不会随着浏览器的自动发送减少不必要的请求头。但是每次请求不会自定携带JWT,需要额外的前端代码，而且会遭受到XSS攻击
Cookie：会随着浏览器发送，不会遭遇脚本的攻击。但是会携带多余的数据，会遭到跨站攻击。

#### Zuul敏感头过滤：
因为Zull内部有默认的过滤器，会对请求头和响应头信息进行从组。ZuulProperties中的SensitiveHeaders中的属性，来获取敏感列表添加到忽略名单中。在配置文件将zuul.sensitive-headers=对默认配置进行覆盖。

### 4.3.3：退出登录：
当点击退出登录的时候，获取用户的tooken然后获取tookenId,放到redis中，然后设置有效时间为token的剩余有效时间(也就是黑名单)。

JWT：Heard头部，声明定义签名算法。载荷，tooken的有效数据。签名，验证tooken数据是否完整。

#### 令牌的更新：
每次刷新页面，都去鉴定中心生成一次新的token(前台传递id,secret做参数)，保证用户的体验性。
验证用户是否登录：
因为网关是整个服务的唯一入口，所以网关处进行用户登录的校验。利用zull的过滤器在路由前对请求进行拦截，然后使用公钥进行token进行解密，然后再向redis中查询是否再黑名单中(用户浏览商品的界面，用户去注册中心进行登录，等等，需要直接放行，这就是白名单)，如果满足进行放行。

### 4.3.4：服务之间的鉴权：
#### 概述
因为有时候当请求绕过网关，这样我们的所有服务都暴漏，这样我们的服务就会存在很严重的安全问题。所以我们就设计成每个服务都有自己tooken每一个服务都允许特定的服务来访问，保证了服务的安全性。数据库中设计了两张表一张是存储每个服务的具体信息。
另外一张表则存储每一个服务能被其他服务访问的列表。
首先是每个服务获取自己的tooken,每个微服通过Feign，务定时的调用鉴权中心传递自己的id和服务名，然后生成自己的tooken，保存在cookie中。但是在鉴权中心中是调用自己的service生成同tooken。当设置定时时间到了后，再次生成该服务的JWT，然后将tooken添加到请求头中。网关采用的是网关的过滤器添加的。其他的服务是通过Feign的拦截器添加到请求头。
每个服务添加鉴权，看看那个服务能访问。在服务设置拦截器(网关采用网关的过滤器)，拦截请求，获取tooken,使用公钥进行解析获取目标服务。本服务能被访问的用配置文件加配置类导进当前拦截器中允许访问列表中，解析的目标服务是否在允许访问列表中。如果在允许访问，如果不在就禁止访问。
#### 定时任务：
Spring已经集成了定时任务的支持，只需要添加指定的注释就可以生效：启动类上加@EnableScheduling,在定时类的方法上加上@Scheduled就能使该方法能够定时执行。
总结：技术 redis + RabbitMQ + 阿里大于平台的短信发送 ;
# 五：购物车
## 5.1：服务列表
购物车服务
## 5.2：需求页表
无状态的购物车
有状态的购物车
无状态的购物车和有状态的购物车的合并
## 5.3：业务流程
#### 无状态的购物存储在Localstorage
Localstorage是html5在客服段存储数据的。并且存储的数据没有时间限制。当我们点击加入购物车的时候，页面会跳转到购物车商品列表页，然后将Localstorage里面的skuId的集合传输到后台接口，查询最新的商品信息。所以我们删除商品和修改商品的数量直接在前端进行更改。
登录状态的购物车存储在redis中。因为购物车的数据变化频率较高，所以不建议存储在MySQL数据库中。直接使用使用一个工具类直接解析载荷，不进行签名验证。把用户的信息放到ThreadLocal,解决多线程资源共享问题，保证用户资源都有自己的资源，互不干扰。
登录后，我往购物车添加商品的时候，将数据提交到后台，然后我们skuI去redis中进行查询，如果查询到了，将redis的商品的加入数量进行更改，如果商品不存在，将这个商品以及数量入redis中(不设置存活时间，永久存储)。然后发送一次异步请求，查询redis中购物车商品。
登录后购物车的合并：跳转到购物车列表前，判断用户是否登录，如果登录将LocalStorage的商品提交到后台保存，这个过程和登录状态的往购物车添加数据是一样的业务，这个值把商品的集合添加到redis中。然后直接清空LocalStorage。如果没有登录就是为登录状态下的购物车显示形式。
# 六：下单支付

# 七：架构技术(springboot,springcloud)
## 7.1：原因
SpringBoot简化了基于spring应用开发。解决了之前的复杂配置，解决了复杂的依赖管理。电商项目必须做到高并发高可用，所以首选创建微服务项目。SpringCloud架构微服务项目。和spring无缝链接，简化开发。
SpringCloud集成了世界上先进的技术Eurake,Zull,Feign,Ribbon,Hystrix

## 7.2：Eurake
1：用于服务的注册和拉取，并且对注册的服务进行监控。这样可以保证服务调用者，更加简洁的调用服务，也可以让服务拉去着知道被拉取服务的状态的状态。所以每次服务的调用需要根据服务的名字取调用服务的地址，然后再进行服务的访问。
2：而且还集成了Ribbon，让消费服务访问多个同名的服务中的一个。
3：集成了Hysrix，防止雪崩，用于保护我们的服务，采用的方式是服务降级
#### 服务降级原理：
A:让每个微服务对应一个线程池，每次访问先访问线程池，然后再访问服务，当访问超时(默认1秒)，或者线程池满了，则快速响应一句话。
B:在一定时间内访问20次，其中有10失败。服务则处于断开状态，沉睡5秒，服务处于半断开状态，允许放一次请求，如果成功服务处于闭合，如果失败，服务进入断开状态，循环。
## 7.3：Feign
用于服务间的通信，集成了Ribbon和Hystrix。服务的消费者通过Feign来访问Eurake，拉去服务提供者的地址，然后通过Feign来访问服务的提供者。
## 7.4：Zuul
是项目的唯一入口，核心功能就是用来过滤和路由。也集成了Ribbon和Hystrix。客户通过地址，访问到网关，网关路由到注册中心根据服务的名字拉去要访问的地址，然后访问服务。
在网关处可以定义一个过滤器，用来验证用户是否登录。
## 7.5：springboot,SpringCloud项目架构的api
### 7.5.1：Springboot:
#### 依赖
```
<parent>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-parent</artifactId>  
    <version>2.1.3.RELEASE</version>  
</parent>  

  
<dependency>  
        <groupId>org.springframework.boot</groupId>  
        <artifactId>spring-boot-starter-web</artifactId>  
 </dependency>  
 ```

### 7.5.2：配置文件
```
ly:
  jwt:
    pubKeyPath: D:/heima/rsa/id_rsa.pub # D:/heima/rsa/id_rsa.pub # 公钥地址
    priKeyPath: D:/heima/rsa/id_rsa # D:/heima/rsa/id_rsa # 私钥地址
    user:
      expire: 30 # 过期时间,单位分钟
      cookieName: LY_TOKEN # cookie名称
      cookieDomain: leyou.com # cookie的域
   
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private String priKeyPath;
    private UserTokenProperties user = new UserTokenProperties();

    @Data
    public class UserTokenProperties {
        private int expire;
        private String cookieName;
        private String cookieDomain;
    }
}

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeInterceptors implements RequestInterceptor {

    @Autowired
    private JwtProperties props；

    public void apply(RequestTemplate template) {
        String token = tokenHolder.getToken();
        template.header(props.getApp().getHeaderName(), token);
    }
}
```
#### 总结：
配置文件值添加到属性类中，通过属性类中set方法，获取值通过get方法。
```
启动类
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
//@SpringBootApplication：
项目中导入依赖，自动配置。
启动类包下全部被扫描，可以使用spring注解。
```
### 7.5.3：拦截器(一个注解@configuration两个接口HandlerInterceptor，WebMvcConfigurer)
#### 1：继承HandlerInterceptor
```
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override  //请求到达controller之前。
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("preHandle method is now running!");
        return true;   
//true代表放行。false代表拦截，业务完全不执行，只执行after..拦截方法
    }
    @Override//请求执行完
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        log.debug("postHandle method is now running!");
    }
    @Override//视图渲染完执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.debug("afterCompletion method is now running!");
    }
}
```
#### 2：拦截器添加
```
@Configuration
public class MvcConfig implements WebMvcConfigurer{
 
    // 重写接口中的addInterceptors方法，添加自定义拦截器
@Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 通过registry来注册拦截器，通过addPathPatterns来添加拦截路径
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
    }
}
```
### 7.5.4：集成mybatis
#### a:导入依赖
```
<!-- 通用mapper -->
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper-spring-boot-starter</artifactId>
    <version>2.1.5</version>
</dependency>
```
#### b:创建mapper
```
public interface UserMapper extends Mapper<User>{
}
在要操作的实体类上添加@Table(name = "tb_user")
```
#### c:开启mapper类扫描
在启动类上添加@MapperScan("cn.itcast.mapper")

#### d:配置yml文件
```
mybatis:
 	 # mybatis 查询的结构集封装
  	type-aliases-package=cn.itcast.pojo
 	 # mapper.xml文件位置,如果没有映射文件，请注释掉，不然启动报错
  	mapper-locations=classpath:mappers/*.xml
  	# 驼峰映射 
  	configuration:
    		map-underscore-to-camel-case: true
```
### 7.5.5：SpringCloud
配置中心(可以配置多个，保证高可用，但是相互之间得注册)
#### a:导入依赖
```
<dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>  
```
#### b:在引导类上添加@EnableEurekaServer  
#### c:配置文件(如果是其他服务和注册中心之间的关系，和该配置类似。)
```
eureka:  
  client:  
    service-url:  
      defaultZone: http://127.0.0.1:10086/eureka  
    fetch-registry: false   //不从注册中心拉去服务
    register-with-eureka: false  //不向注册中心注册服务  
```
### 7.5.6：网关的配置
#### a:导入依赖
```
<dependency>  
      <groupId>org.springframework.cloud</groupId>  
      <artifactId>spring-cloud-starter-netflix-zuul</artifactId>  
</dependency>  
```
#### b:在启动类上添加@EnableZuulProxy
#### c:配置yml文件的编写
```
zuul:  
  prefix: /api # 添加路由前缀  
  routes:  
    item-service: /item/** # 将商品微服务映射到/item/**  
    upload-service: /upload/**  
    search-service: /search/**  
eureka:  
  client:  
    service-url:  
      defaultZone: http://127.0.0.1:10086/eureka  
```
### 7.5.7：服务之间的访问(A服务要调用别的服务)
#### a:导入依赖(在调用者导入依赖,服务的接口也需要导入依赖)
```
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
#### b:在启动类添加注释@EnableFeignClients
#### c:配置接口
@FeignClient("user-service")
public interface UserClient {
    
    @GetMapping("query")
    UserDTO queryUserByUsernameAndPassword(@RequestParam("username") String username,
                                           @RequestParam("password") String password);

    @GetMapping("address")
    AddressDTO queryAddressById(@RequestParam("id") Long id);
}
#### d:直接调用和spring其他类中的对象一样
### 7.5.8：基于ZullFilter类的过滤器(登录校验)
```
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeFilter extends ZuulFilter {
    @Autowired
    private PrivilegeTokenHolder tokenHolder;
    @Autowired
    private JwtProperties props;
    @Override
    public String filterType() {
        return "pre";   //pre:路由之前执行。Routing:路由过程中执行。Error:请求错误执行。Post:在routing,error之后执行
    }
    /**
     * PRE_DECORATION_FILTER 是Zuul默认的处理请求头的过滤器，我们放到这个之后执行
     * @return 顺序
     */
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;   //return 后面的值越小，优先级越高
    }
    @Override
    public boolean shouldFilter() {
        return true;   //为true执行过滤器，fasle不经过走run方法
    }
    @Override
    public Object run() throws ZuulException {
        //需要把token加入到请求头中

        RequestContext currentContext = RequestContext.getCurrentContext();
        //添加请求头，key：value格式
        String token = tokenHolder.getToken();
        String headerName = props.getApp().getHeaderName();
        currentContext.addZuulRequestHeader(headerName,token);
        return null;
    }
}
```
#### 总结：
项目中的服务被网关访问的，则必须在yml文件配置路由，但如common模块只被别的服务依赖，page模块仅从消息队列获取消息，其他服务调用，发送消息模块，仅从消息队列接消息，注册中心是网关拉取服务的点，所以都不需要添加路由。
项目中服务需要被别的服务访问，或者自己访问别的服务访问，或者网关进行访问，则需要配置配置中心。

