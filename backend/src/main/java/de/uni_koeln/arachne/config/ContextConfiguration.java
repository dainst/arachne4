package de.uni_koeln.arachne.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@PropertySource("classpath:config/application.properties")
public class ContextConfiguration {

	@Inject
	private Environment environment;	
		
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
