package de.uni_koeln.arachne.testconfig;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class WebContextTestExecutionListener extends AbstractTestExecutionListener {

	@Override
	public void prepareTestInstance(final TestContext testContext) {

		if (testContext.getApplicationContext() instanceof GenericApplicationContext) {
			final GenericApplicationContext context = (GenericApplicationContext) testContext.getApplicationContext();
			final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			beanFactory.registerScope("request", new SimpleThreadScope());
			beanFactory.registerScope("session", new SimpleThreadScope());
		}
	}

}
