package com.yang.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-1)
public class SpringHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("-------init ApplicationContextAware-------");
        if (SpringHelper.applicationContext == null) {
            SpringHelper.applicationContext = applicationContext;
            System.out.println("初始化setApplicationContext：" + applicationContext);
        }
    }

    //获取applicationContext  
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.  
    public static Object getBean(String name) {
        System.out.println("通过name获取 Bean:" + name);
        Object bean = getApplicationContext().getBean(name);
        System.out.println("result:" + bean);
        return bean;
    }

    //通过class获取Bean.  
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}  