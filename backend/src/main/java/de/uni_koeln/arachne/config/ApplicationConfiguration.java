package de.uni_koeln.arachne.config;

import com.zaxxer.hikari.HikariDataSource;
import de.uni_koeln.arachne.viewResolvers.CsvViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class holds the application configuration. It configures message converters, view resolvers, datasources, etc.
 * It replaces the old Spring XML config files. All interaction with this class is automatically done by Spring.
 *
 * @author Reimar Grabowski
 * @author Patrick Jominet
 */
@ComponentScan("de.uni_koeln.arachne")
@Configuration
@EnableWebMvc
@EnableAsync
@EnableTransactionManagement
@EnableSpringConfigured
@PropertySource("classpath:config/application.properties")
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {

    @Inject
    private Environment environment;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new ByteArrayHttpMessageConverter());
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        registry.viewResolver(resolver);
    }

    // TODO: check which resolver to use correctly? (which is better?)

    /**
     * Configure ContentNegotiationManager
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON).favorPathExtension(true);
    }

    /**
     * Configure ContentNegotiatingViewResolver
     *
     * @return An appropriate view resolver
     */
    @Bean
    public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
        resolver.setContentNegotiationManager(manager);

        // Define all possible view resolvers
        List<ViewResolver> resolvers = new ArrayList<>();

        resolvers.add(csvViewResolver());

        resolver.setViewResolvers(resolvers);
        return resolver;
    }

    /**
     * Configure View resolver to provide CSV output using Super CSV library to
     * generate CSV output for an object content
     *
     * @return A CSV view resolver
     */
    @Bean
    public ViewResolver csvViewResolver() {
        return new CsvViewResolver();
    }

    /**
     * Sets the properties file 'application.properties'.
     *
     * @return A property sources place holder configurer.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("config/application.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    /**
     * Configures the JDBC datasource (connection to the DB). A Hikari connection pool is utilized.
     *
     * @return The configured datasource.
     */
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        final HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(environment.getProperty("jdbcDriverClassName"));
        hikariDataSource.setJdbcUrl(environment.getProperty("jdbcUrl"));
        hikariDataSource.setUsername(environment.getProperty("jdbcUsername"));
        hikariDataSource.setPassword(environment.getProperty("jdbcPassword"));
        hikariDataSource.setAutoCommit(false);
        hikariDataSource.setLeakDetectionThreshold(20000);

        // Tells Spring to bounce off the connection pool
        return new LazyConnectionDataSourceProxy(hikariDataSource) {
            @SuppressWarnings("unused")
            public void close() throws SQLException {
                HikariDataSource datasource = (HikariDataSource) super.getTargetDataSource();
                datasource.close();
            }
        };
    }

    /**
     * Configures a hibernate session factory.
     *
     * @return The configured session factory.
     */
    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        hibernateProperties.setProperty("hibernate.show_sql", "false");
        hibernateProperties.setProperty("hibernate.id.new_generator_mappings", "false");
        hibernateProperties.setProperty("hibernate.connection.autocommit", "false");

        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setHibernateProperties(hibernateProperties);
        sessionFactory.setPackagesToScan("de.uni_koeln.arachne.mapping.hibernate");

        return sessionFactory;
    }

    /**
     * Creates a hibernate transaction manager.
     *
     * @return A new transaction manager.
     */
    @Bean
    public HibernateTransactionManager transactionManager() {
        return new HibernateTransactionManager(sessionFactory().getObject());
    }
}
