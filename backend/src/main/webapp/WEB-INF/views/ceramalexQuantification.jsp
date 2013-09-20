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
	<br/>
	<br/>
	<b>${message}</b>
	<br/>
	<br/>
	<c:choose>
    	<c:when test="${containsContent == true}">
			<table border="4">
				<tr>
					<th colspan="6" align="left">Results of aggregational computations:</th>
				</tr>
				<tr>
					<td colspan="6" align="left">Aggregated counts</td>
					
				</tr>
				<tr>
					<td>Rims</td>
					<td>Handles</td>
					<td>Bases</td>
					<td>Body sherds</td>
					<td>Others</td>
					<td>Total number of sherds</td>
				</tr>
				<tr>
					<td>${RimCount}</td>
					<td>${HandleCount}</td>
					<td>${BaseCount}</td>
					<td>${BodySherdCount}</td>
					<td>${OthersCount}</td>
					<td>${TotalSherds}</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
				</tr>
				<tr>
					<td colspan="6" align="left">Aggregated Weights</td>
				</tr>
				<tr>
					<td width="100">Rims</td>
					<td width="100">Handles</td>
					<td width="100">Bases</td>
					<td width="100">Body sherds</td>
					<td width="100">Others</td>
					<td width="200">Aggregated weight of sherds</td>
				</tr>
				<tr>
					<td width="100">${RimWeight}</td>
					<td width="100">${HandleWeight}</td>
					<td width="100">${BaseWeight}</td>
					<td width="100">${BodySherdWeight}</td>
					<td width="100">${OthersWeight}</td>
					<td width="200">${TotalWeight}</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
				</tr>
				<tr>
					<td colspan="6" align="left">Additional parameters</td>
				</tr>
				<tr>
					<td colspan="2">MNI</td>
					<td colspan="2">MNI Weighted</td>
					<td colspan="2">MXI</td>
				</tr>
				<tr>
					<td colspan="2">${mni}</td>
					<td colspan="2">${mniWeightd}</td>
					<td colspan="2">${mxi}</td>
				</tr>
			
			</table>  
      	</c:when>
      	<c:otherwise>
	      	<br/>
	      		EMPTY RESULT
	      	<br/>
      	</c:otherwise>
	</c:choose>
</body>
</html>