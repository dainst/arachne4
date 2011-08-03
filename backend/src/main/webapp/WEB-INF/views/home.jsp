<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	Query Result:
</h1>
<c:forEach items="${buildingList}" var="building">
	<td>id : ${building.id}</td>
	<br/>
	<td>entityGroup : ${building.entityGroupBuilding}</td>
	<br/>
	<td>architect : ${building.architect}</td>
	<br/>
	<td>excavation : ${building.excavation}</td>
	<br/>
	<td>buildingRegulation : ${building.buildingRegulation}</td>
	<br/>
	<td>shortDescription : ${building.shortDescription}</td>
	<br/>
	------------------------------------------
	<br/>
</c:forEach>
</body>
</html>
