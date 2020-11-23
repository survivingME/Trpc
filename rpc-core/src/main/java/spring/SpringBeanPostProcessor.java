package spring;

import annotation.RpcReference;
import annotation.RpcService;
import entity.RpcServiceProperties;
import extension.ExtensionLoader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import provider.ServiceProvider;
import provider.ServiceProviderImpl;
import proxy.RpcClientProxy;
import remoting.ClientTransport;
import utils.factory.SingletonFactory;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final ClientTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //get rpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            //build rpcService properties
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                                                        .group(rpcService.group())
                                                        .version(rpcService.version()).build();
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if(rpcReference != null) {
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                                                            .group(rpcReference.group())
                                                            .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, rpcClientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
