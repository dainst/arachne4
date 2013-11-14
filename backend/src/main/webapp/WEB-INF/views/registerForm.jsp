<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Register</title>
</head>
<body>
<h1>Registrieren</h1>

        <form action="register" method="post">
        <%
          ReCaptcha c = ReCaptchaFactory.newReCaptcha("6LfANeoSAAAAAGtsGXdgE-aljSLllsqUJ-8XgvxS", "6LfANeoSAAAAAAtryQ12lfb55jkQ6qDIVIdvURbH", false);
          out.print(c.createRecaptchaHtml(null, null));
        %>
        <input type="submit" value="submit" />
        </form>
      </body>
       <script type="text/javascript">
 	console.log(RecaptchaState);
 	console.log(RecaptchaState.challenge);
 </script>
    </html>