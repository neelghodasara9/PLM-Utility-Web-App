<%@page import="com.java.plm.MyWebApp.model.ConfigInput"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>PLM Audit History List</title>
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
	<h1>PLM Audit History</h1>
	<hr>
	<br>
	<!--	<form:form action="downloadCSV" method="post" id="downloadCSV">
			 <input  id="submitId" type="submit" value="Downlaod All">
	</form:form> -->


	<c:forEach var="datetime" items="${timestamp}">
		${datetime} 
	</c:forEach>
	!-->


</body>
</html>