package froggy.winterframework.transaction.jdbc;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import froggy.winterframework.beans.factory.support.BeanFactory;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.ConfigurationClassPostProcessor;
import froggy.winterframework.core.PropertySource;
import froggy.winterframework.core.env.Environment;
import froggy.winterframework.jdbc.datasource.DriverManagerDataSource;
import froggy.winterframework.transaction.TransactionManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;

public class DataSourceTransactionAutoConfigurationTest {

    private ApplicationContext context;
    private BeanFactory beanFactory;

    @Before
    public void setUp() throws IOException {
        context = new ApplicationContext();
        beanFactory = context.getBeanFactory();

        Environment environment = new Environment();
        environment.getPropertySource().mergePropertySource(new PropertySource("test", datasourceProperties()));

        context.addEnvironment(environment);
        beanFactory.addEnvironment(environment);
        beanFactory.registerResolvableDependency(ApplicationContext.class, context);
        beanFactory.registerBeanDefinition(DataSourceTransactionAutoConfiguration.class);

        new ConfigurationClassPostProcessor().postProcessBeanDefinitionRegistry(beanFactory);
    }

    @Test
    public void dataSource_bean을_등록한다() {
        // Given

        // When
        DataSource dataSource = beanFactory.getBean("dataSource", DataSource.class);

        // Then
        assertTrue(dataSource instanceof DriverManagerDataSource);
    }

    @Test
    public void transactionManager_bean을_등록한다() {
        // Given

        // When
        TransactionManager transactionManager = beanFactory.getBean("transactionManager", TransactionManager.class);

        // Then
        assertTrue(transactionManager instanceof DataSourceTransactionManager);
    }

    @Test
    public void transactionManager는_등록된_dataSource를_사용한다() throws Exception {
        // Given
        DataSource dataSource = beanFactory.getBean("dataSource", DataSource.class);

        // When
        DataSourceTransactionManager transactionManager = beanFactory.getBean(
            "transactionManager",
            DataSourceTransactionManager.class
        );

        // Then
        assertSame(dataSource, getDataSourceFrom(transactionManager));
    }

    private Map<String, String> datasourceProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("winter.datasource.url", "jdbc:test");
        properties.put("winter.datasource.username", "user");
        properties.put("winter.datasource.password", "password");
        return properties;
    }

    private DataSource getDataSourceFrom(DataSourceTransactionManager transactionManager) throws Exception {
        Field field = DataSourceTransactionManager.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        return (DataSource) field.get(transactionManager);
    }
}
