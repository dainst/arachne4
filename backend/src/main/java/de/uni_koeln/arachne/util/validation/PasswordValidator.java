/**
 * 
 */
package de.uni_koeln.arachne.util.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Sven Ole Clemens
 *
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

	/**
     * Regular expression for password validation. The rule is given below.
     * <ul>
     * <li>1. The total length of password should be 6 to 20 characters.</li>
     * <li>2. One special character like @,#,$,%</li>
     * <li>3. One upper case and one lower case</li>
     * <li>4. One numeric digit.</li>
     * </ul>
     */
    private final static String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

    /**
     * (non-Javadoc)
     * 
     * @see
     * javax.validation.ConstraintValidator#initialize(java.lang.annotation.
     * Annotation)
     */
	@Override
	public void initialize(final Password password) {
		// do nothing...
	}

	/**
     * (non-Javadoc)
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
	@Override
	public boolean isValid(final String str, final ConstraintValidatorContext ctx) {
		return validate(str);
	}

	/**
     * This method is used to validate a password based on the regular
     * expression given above.
     * 
     * @param password
     *            of type String
     * @return boolean value either true or false
     */
	private boolean validate(final String password) {
		Matcher matcher;
		final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(password);
		return matcher.matches();
	}

}
