package de.uni_koeln.arachne.config;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.zaxxer.hikari.HikariDataSource;

@ComponentScan("de.uni_koeln.arachne")
@Configuration
@EnableWebMvc
@EnableAsync
@EnableTransactionManagement
@EnableSpringConfigured
@PropertySource("classpath:config/application.properties")
public class ContextConfiguration extends WebMvcConfigurerAdapter {

	@Inject
	private Environment environment;	
		
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new BufferedImageHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new MappingJackson2HttpMessageConverter());
	}
	
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		registry.viewResolver(resolver);
	};
	
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
	    propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("config/application.properties"));
	    return propertySourcesPlaceholderConfigurer;
    }

	@Bean
	public DataSource dataSource() {
		final HikariDataSource hikariDataSource = new HikariDataSource();
		hikariDataSource.setDriverClassName(environment.getProperty("jdbcDriverClassName"));
		hikariDataSource.setJdbcUrl(environment.getProperty("jdbcUrl"));
		hikariDataSource.setUsername(environment.getProperty("jdbcUsername"));
		hikariDataSource.setPassword(environment.getProperty("jdbcPassword"));
		// Tells Spring to bounce off the connection pool
		final LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy = new LazyConnectionDataSourceProxy(hikariDataSource);
		return lazyConnectionDataSourceProxy;
	}
	
	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		final Properties hibernateProperties = new Properties();
		hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		hibernateProperties.setProperty("hibernate.show_sql", "false");
		hibernateProperties.setProperty("hibernate.enable_lazy_load_no_trans", "true");
		
		final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		sessionFactory.setHibernateProperties(hibernateProperties);
		sessionFactory.setPackagesToScan(new String[]{"de.uni_koeln.arachne.mapping.hibernate"});
		
		return sessionFactory;
	}
	
	@Bean
	public HibernateTransactionManager transactionManager() {
		return new HibernateTransactionManager(sessionFactory().getObject());
	}
}
