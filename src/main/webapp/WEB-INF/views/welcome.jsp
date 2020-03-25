<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="/resources/static/css/style.css">
<script type="text/javascript" src="/resources/static/js/app.js"></script>
<title>PLM Audits</title>
</head>
<body>
	<h1>PLM Audits</h1>
	<hr>
	<div class="form">
		<form:form method="POST" action="results" modelAttribute="config"
			onsubmit="return validate()">
			<br>
			<br>
			<table>
				<tr>
					<td><label id="ncInputPath">Normalized Campaign Input
							path </label></td>
					<td><form:textarea path="ncInputpath" rows="3" cols="50"></form:textarea></td>
				</tr>
				<tr>
					<td><label id="futureWindow">Material Impact future
							window (in days) path </label></td>
					<td><form:input path="futureWindow" type="text"
							name="futureWindow" pattern="[0-9]+" title="Numeric only" /></td>
				</tr>
			</table>
			<br>
			<fieldset>
				<legend>Audits to run?</legend>
				<form:checkbox path="runIcomsAudit" value="YES" />
				Icoms-Plm end date audit
				<form:checkbox path="runMissingOffersAudit" value="YES" />
				Missing offers audit
				<form:checkbox path="runNormalizedCampAudit" value="YES" />
				Normalized campaign audit <br>
				<br>
				<br>
			</fieldset>
			<br>
			<br>
			<fieldset>
				<legend>Mail</legend>
				<form:checkbox path="triggerMail" value="YES" />
				TriggerMail?
				<br>
				<label id="recipientsList">Mailing Recipients List</label>
				<form:textarea path="recipientsList" rows="3" cols="50"></form:textarea>
			</fieldset>
			<br><br><br>
			<input type="submit" value="Run Audits" />
		</form:form>
	</div>
</body>
</html>