<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Arachne welcome page.</title>
</head>
<body>
<h1>
	Welcome to Arachne4 beta
</h1>
<c:forEach items="${infoList}" var="info">
	<td>username : ${infoList.username}</td>
	<br/>
</c:forEach>
</body>
</html>
