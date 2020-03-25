<%@page import="com.java.plm.MyWebApp.model.ConfigInput"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>PLM Audit Results</title>
<style>
table {
	border-collapse: collapse;
	width: 100%;
}

th, td {
	text-align: left;
	padding: 8px;
}

tr:nth-child(even) {
	background-color: #f2f2f2
}

th {
	background-color: #4CAF50;
	color: white;
}
</style>
</head>
<body>
	<h1>PLM Audit Results</h1>
	<hr>
	

		<table width='100%' border='1' align='left'>
			<thead>
				<tr>
					<th>DISCOUNT_ID</th>
					<th>DISCOUNT_CODE</th>
					<th>DESCRIPTION</th>
					<th>TAG</th>
					<th>DISCOUNT_START_DATE</th>
					<th>DISCOUNT_END_DATE</th>
					<th>needsOffers?</th>
					<th>CNT</th>
				</tr>
			</thead>

			<c:forEach items="${MissingOfferAudit}" var="item">
				<tr>
					<td>${item[0]}</td>
					<td>${item[1]}</td>
					<td>${item[2]}</td>
					<td>${item[3]}</td>
					<td>${item[4]}</td>
					<td>${item[5]}</td>
					<td>${item[6]}</td>
					<td>${item[7]}</td>
				</tr>
			</c:forEach>
		</table>

</body>
</html>