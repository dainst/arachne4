package de.uni_koeln.arachne.util.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserAccess {
	enum Restrictions {
		writeprotected
	}
	
	Restrictions value() default Restrictions.writeprotected; 
}
