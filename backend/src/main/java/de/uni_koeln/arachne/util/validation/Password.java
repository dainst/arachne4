/**
 * 
 */
package de.uni_koeln.arachne.util.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Sven Ole Clemens
 *
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface Password {

	String message() default "{Invalid password, valid password : one Upper case, one special char, one number, total length 6 to 20 chars}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
}
