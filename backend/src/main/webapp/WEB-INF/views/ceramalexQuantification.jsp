<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>CERAMALEX-Quantities-Result</title>
</head>
<body>
	<table border="4">
		<tr>
			<th colspan="2" align="left">Your search:</th>
		</tr>
		<tr>
			<td>Search Term:</td>
			<td><b>${searchParam}</b></td>
		</tr>
		<tr>
			<td>Facets:</td>
			<td><b>${facets}</b></td>
		</tr>
	</table>

	<b>${message}</b>
	<br>
	<br>
	<c:if test="${containsContent == false}">
		JUCHU FALSE
	</c:if>
	<c:if test="${containsContent == true}">
		JUCHU TRUE
	</c:if>
</body>
</html>