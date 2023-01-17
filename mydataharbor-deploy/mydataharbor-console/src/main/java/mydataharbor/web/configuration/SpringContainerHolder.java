package mydataharbor.web.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author wangzhibo
 * @description
 * @since 2022-12-26 14:58
 */
@Component
public class SpringContainerHolder implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    public SpringContainerHolder() {
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContainerHolder.applicationContext = applicationContext;
    }

    public static <T> T lookup(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static Object lookup(String name) {
        return applicationContext.getBean(name);
    }
}
