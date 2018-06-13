package cn.daycode.spring.boot.autoconfigure;

import cn.daycode.core.annotation.Repository;
import cn.daycode.core.mapping.Mapper;
import cn.daycode.spring.repository.ClassPathRepositoryScanner;
import cn.daycode.spring.repository.RepositoryFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

/**
 * durex jdbc 自动化配置类
 *
 * @author zch
 * @since 2018/6/12
 */
@Configuration
@EnableConfigurationProperties(DurexJdbcProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ConditionalOnSingleCandidate(DataSource.class)
public class DurexJdbcAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DurexJdbcAutoConfiguration.class);

    private final DurexJdbcProperties properties;

    public DurexJdbcAutoConfiguration(DurexJdbcProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void loadSql() {
        Mapper.loadSql(properties.getDirectory(), properties.getSuffix());
    }

    @Bean(value = "jdbcTemplate")
    @ConditionalOnMissingBean
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }


    // *********************** 自动扫描注册 Repository ******************************


    /**
     * This will just scan the same base package as Spring Boot does.
     * If you want more power, you can explicitly use
     */
    public static class AutoConfiguredRepositoryScannerRegistrar
            implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
            logger.debug("Searching for Repositories annotated with @Repository");

            // 创建扫描类
            ClassPathRepositoryScanner scanner = new ClassPathRepositoryScanner(registry);

            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }

                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (logger.isDebugEnabled()) {
                    packages.forEach(pkg -> logger.debug("Using auto-configuration base package '{}'", pkg));
                }

                scanner.setAnnotationClass(Repository.class);
                scanner.registerFilters();
                scanner.doScan(StringUtils.toStringArray(packages));
            } catch (IllegalStateException ex) {
                logger.debug("Could not determine auto-configuration package, " +
                        "automatic Repository scanning disabled.", ex);
            }

        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

    @Configuration
    @Import({AutoConfiguredRepositoryScannerRegistrar.class})
    @ConditionalOnMissingBean(RepositoryFactoryBean.class)
    public static class RepositoryScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void afterPropertiesSet() {
            logger.debug("No {} found.", RepositoryFactoryBean.class.getName());
        }
    }
}
