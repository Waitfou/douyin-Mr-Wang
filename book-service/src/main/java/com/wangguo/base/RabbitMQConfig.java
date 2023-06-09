package com.wangguo.base;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    /**
     * 根据模型编写代码
     * 1、定义交换机
     * 2、定义队列
     * 3、创建交换机
     * 4、创建队列
     * 5、队列和交换机的绑定
     */
    //ctrl + shift + u快速转换为大写
    public static final String EXCHANGE_MSG = "exchange_msg";
    public static final String QUEUE_SYS_MSG = "queue_sys_msg";

    @Bean(EXCHANGE_MSG)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_MSG) // 构建交换机
                .durable(true)  // 使用topic
                .build(); // 设置持久化，重启mq后依然存在
    }

    // 定义消息队列
    @Bean(QUEUE_SYS_MSG)
    public Queue queue() {
        return new Queue(QUEUE_SYS_MSG);
    }

    // 绑定交换机和队列
    // 通过将 @Qualifier 注解与我们想要使用的特定 Spring bean 的名称一起进行装配，
    // Spring 框架就能从多个相同类型并满足装配要求的 bean 中找到我们想要的，避免产生歧义
    // 该注解在RabbitMQ中经常使用
    // 用来解决@Autowire会产生歧义的问题(也就是同时匹配到多个Bean)
    public Binding binding(@Qualifier(EXCHANGE_MSG) Exchange exchange,
                           @Qualifier(QUEUE_SYS_MSG) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("sys.msg.*")  // 定义路由规则（requestMapping) // 这里表示匹配routingKey = sys.msg的消息
                .noargs();
        //FIXME : * 和 #分别代表什么意思？
        // * 代表匹配一个字符
        // # 代表匹配多个字符
    }
}
