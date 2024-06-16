package org.dhorse.rest.component;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class DynamicBeanRegistry implements BeanDefinitionRegistryPostProcessor {
	
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ClassUtils.class);
//        BeanDefinition beanDefinition = builder.getRawBeanDefinition();
//        registry.registerBeanDefinition("classUtils", beanDefinition);
    }
 
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        
    }
}