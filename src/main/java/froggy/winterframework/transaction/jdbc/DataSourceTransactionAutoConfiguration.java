package froggy.winterframework.transaction.jdbc;

import froggy.winterframework.beans.factory.annotation.Autowired;
import froggy.winterframework.context.ApplicationContext;
import froggy.winterframework.context.annotation.Bean;
import froggy.winterframework.context.annotation.Configuration;
import froggy.winterframework.jdbc.datasource.DriverManagerDataSource;
import froggy.winterframework.transaction.TransactionManager;
import javax.sql.DataSource;

@Configuration
public class DataSourceTransactionAutoConfiguration {

    private static final String DATA_SOURCE_BEAN_NAME = "dataSource";

    private final ApplicationContext context;

    @Autowired
    public DataSourceTransactionAutoConfiguration(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public DataSource dataSource() {
        // TODO: datasource 조건부 등록 지원 후 설정이 있을 때만 DB infra bean을 등록한다.
        return new DriverManagerDataSource(
            getProperty("winter.datasource.url"),
            getProperty("winter.datasource.username"),
            getProperty("winter.datasource.password")
        );
    }

    @Bean
    public TransactionManager transactionManager() {
        // TODO: @Bean method parameter DI 지원 후 DataSource parameter 주입으로 변경한다.
        DataSource dataSource = context.getBeanFactory().getBean(DATA_SOURCE_BEAN_NAME, DataSource.class);
        return new DataSourceTransactionManager(dataSource);
    }

    private String getProperty(String key) {
        return context.getEnvironment().getPropertySource().getSource().get(key);
    }
}
