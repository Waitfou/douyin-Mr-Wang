package com.wangguo;

import com.wangguo.intercepter.PassportInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration //配置类
public class InterceptorConfig implements WebMvcConfigurer {

    /**
     * 类似@Component , @Repository , @ Controller , @Service 这些注册Bean的注解存在局限性，只能局限作用
     * 于自己编写的类，如果是一个jar包第三方库要加入IOC容器的话，这些注解就手无缚鸡之力了，是的，@Bean注解就可以做到这一点！
     * Spring的@Bean注解用于告诉方法，产生一个Bean对象，然后这个Bean对象交给Spring管理。
     * 产生这个Bean对象的方法Spring只会调用一次，随后这个Spring将会将这个Bean对象放在自己的IOC容器中。
     * @return
     */
    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }
    /**
     * 添加要拦截的内容
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");
    }
}
