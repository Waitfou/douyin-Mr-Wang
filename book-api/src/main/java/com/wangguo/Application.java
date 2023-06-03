package com.wangguo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * 附注：在项目启动时，springboot 会自动扫描 Application 启动类所在的当前目录以及下一级目录，
 * 一般 Application 启动类都放在根目录，所以在单一项目下，只要是在 Java 类上添加了注解，
 * 都能够默认被 springboot 扫描到，并被添加到 springboot 的容器中，一般不需要特别用
 * @ComponentScan 去指定 springboot 要扫描哪些目录。
 */
@SpringBootApplication
@MapperScan(basePackages = "com.wangguo.mapper") //在application启动类上添加mapper扫描注解，表示要扫描到mapper接口
//默认情况下，启动器只能扫描到启动类所在包下的所有类，如果不在此包下面的，要使用@ComponentScan写明要扫描的类
@ComponentScan(basePackages = {"com.wangguo", "org.n3r.idworker"})
@EnableMongoRepositories // 开启mongodb的使用
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
