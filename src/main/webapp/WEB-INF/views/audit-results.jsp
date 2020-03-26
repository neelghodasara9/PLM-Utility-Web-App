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
	<br>
	<br> ${MailBody }

	<!--
	<c:forEach var="auditName" items="${Headers}">
		${auditName.key} <br>
		<table border='1'>
			<c:set var="temp" scope="application" value="${auditName.key}" />

			<tr>
				<c:forEach var="data" items="${auditName.value}">
					<th>${data}</th>
				</c:forEach>
			</tr>


			<c:forEach var="row" items="${MaterialImpacts.IcomsPlmEndDateAudit}">
				<tr>
					<c:forEach var="item" items="${row}">
						<td>${item}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
		<br>
		<br>
		<br>
	</c:forEach>
	!-->


</body>
</html>