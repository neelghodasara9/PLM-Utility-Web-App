<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="/resources/css/style.css">
<script type="text/javascript" src="/resources/js/app.js"></script>
<title>PLM Audits</title>
</head>
<body class="form">
	<h1>PLM Audits</h1>
	<hr>
	<div class="form">
		<form:form method="POST" action="results" modelAttribute="config"
			onsubmitonsu="return validate()">
			<table>
				<tr>
					<td><label id="ncInputPath">Normalized Campaign Input
							path </label></td>
					<td><form:textarea path="ncInputpath" name="ncInputpath"
							rows="3" cols="50" required="required"></form:textarea></td>
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
				<input type="checkbox" name="select-all" id="select-all"
					onclick="selectAll()" /> <br>
				<form:checkbox path="runIcomsAudit" name="runIcomsAudit" value="YES"/>
				Icoms-Plm end date audit <br>
				<form:checkbox path="runMissingOffersAudit" value="YES" />
				Missing offers audit <br>
				<form:checkbox path="runNormalizedCampAudit" value="YES" />
				Normalized campaign audit <br>
				<form:checkbox path="runOfferCampEndDateAudit" value="YES" />
				Active Plm offer-campaign end date audit<br>
				<form:checkbox path="runOfferCampSiteAudit" value="YES" />
				Active Plm offer-campaign site audit<br>
				<form:checkbox path="runPlmPpAudit" value="YES" />
				Plm-PP offer audit <br>


				<table>
					<tr>
						<td><label id="ncInputPath">Plm offer-campaign End
								date audit - Intakes to ignore</label></td>
						<td><form:textarea path="offerCampEndDateIgnoreIntakes"
								name="offerCampEndDateIgnoreIntakes" rows="3" cols="50"></form:textarea></td>
					</tr>
					<tr>
						<td><label id="ncInputPath">Plm offer-campaign End
								date audit - Keywords to ignore</label></td>
						<td><form:textarea path="offerCampEndDateIgnoreKeywords"
								name="offerCampEndDateIgnoreKeywords" rows="3" cols="50"></form:textarea></td>
					</tr>
					<tr>
						<td><label id="ncInputPath">Plm offer-campaign site
								audit - Intakes to ignore</label></td>
						<td><form:textarea path="offerCampSiteIgnoreIntakes"
								name="offerCampSiteIgnoreIntakes" rows="3" cols="50"></form:textarea></td>
					</tr>
					<tr>
						<td><label id="ncInputPath">Plm offer-campaign site
								audit - Keywords to ignore</label></td>
						<td><form:textarea path="offerCampSiteIgnoreKeywords"
								name="offerCampSiteIgnoreKeywords" rows="3" cols="50"></form:textarea></td>
					</tr>
					<tr>
						<td><label id="ncInputPath">Plm-PP Columns to compare</label></td>
						<td><form:textarea path="plmPpColsToCompare"
								name="plmPpColsToCompare" rows="3" cols="50"></form:textarea></td>
					</tr>
					<tr>
						<td><label id="ncInputPath">Plm-PP Columns to view</label></td>
						<td><form:textarea path="plmPpColsToView"
								name="plmPpColsToView" rows="3" cols="50"></form:textarea></td>
					</tr>
				</table>

			</fieldset>
			<br>
			<fieldset>
				<legend>Mail</legend>
				<form:checkbox path="triggerMail" value="YES" />
				TriggerMail? <br> <label id="recipientsList">Mailing
					Recipients List</label>
				<form:textarea path="recipientsList" rows="3" cols="50"></form:textarea>
			</fieldset>
			<br>

			<input type="submit" value="Run Audits" />
		</form:form>
	</div>
</body>
</html>