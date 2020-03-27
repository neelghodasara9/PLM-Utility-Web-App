function validate(){
	var name = document.getElementByName("ncInputpath").value;
	alert("var->" + name);

	if(name==""){
		alert("Please enter valid input path for Normalized campaign goldensource sheet.");
		return false;
	}
	else{
		return true;
	}
}

function selectAll() {
	var items = document.getElementsByName("runIcomsAudit");
	items.checked = true;

}