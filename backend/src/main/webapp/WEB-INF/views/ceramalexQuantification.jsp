<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<link rel="stylesheet" type="text/css"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" />
<link rel="stylesheet" type="text/css"
	href="static/stylesheets/overrides_bootstrap.css" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" type="text/css"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap-responsive.css" />
<script data-main="build/main" src="static/lib/require-jquery.js"
	type="text/javascript"></script>
<link rel="shortcut icon"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/ico/favicon.ico">
<link rel="apple-touch-icon" sizes="144x144"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/ico/apple-touch-icon-144.png">
<link rel="apple-touch-icon" sizes="114x114"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/ico/apple-touch-icon-114.png">
<link rel="apple-touch-icon" sizes="72x72"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/ico/apple-touch-icon-72.png">
<link rel="apple-touch-icon"
	href="http://arachne.uni-koeln.de/archaeostrap/assets/ico/apple-touch-icon-57.png">
<title>Ceramalex Quantification</title>
</head>
<body>
<div class="row-fluid">
	<div class="span12"></div>
	<div class="span10 offset1">
	<h3>Ceramalex Quantification</h3>
	<hr class="bs-docs-separator">
	<p class="lead">Your Search:</p>
	<table class="table table-bordered">
		<tbody>
			<tr>
				<td>Search Term:</td>
				<td><strong>${searchParam}</strong></td>
			</tr>
			<tr>
				<td>Facets:</td>
				<td><strong>${facets}</strong></td>
			</tr>
		</tbody>
	</table>
	<p class="lead">${message}</p>

	<c:choose>
		<c:when test="${containsContent == true}">
			<hr class="bs-docs-separator">
			<p class="lead">Results of aggregational computations:</p>
			<table class="table table-striped table-bordered table-hover">
				<thead>
					<tr>
						<td>#</td>
						<td>Total Count</td>
						<td>Total Weight</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Rims</td>
						<td>${rimCount}</td>
						<td>${rimWeight}g</td>
					</tr>
					<tr>
						<td>Handles</td>
						<td>${handleCount}</td>
						<td>${handleWeight}g</td>
					</tr>
					<tr>
						<td>Bases</td>
						<td>${baseCount}</td>
						<td>${baseWeight}g</td>
					</tr>
					<tr>
						<td>Body sherds</td>
						<td>${bodySherdCount}</td>
						<td>${bodySherdWeight}g</td>
					</tr>
					<tr>
						<td>Others</td>
						<td>${othersCount}</td>
						<td>${othersWeight}g</td>
					</tr>
					<tr>
						<td>Summed sherds</td>
						<td>${totalSherds}</td>
						<td>${totalWeight}g</td>
					</tr>
				</tbody>
			</table>
			<p class="lead">Additional parameters:</p>
			<table class="table table-condensed table-bordered table-striped">
				<thead>
					<tr>
						<td>MNI</td>
						<td>MNI Weighted</td>
						<td>MXI</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>${mni}</td>
						<td>${mxi}</td>
						<td>${mniWeighted}</td>
					</tr>
				</tbody>
			</table>
		</c:when>
	</c:choose>
</div>
</div>
</body>
</html>