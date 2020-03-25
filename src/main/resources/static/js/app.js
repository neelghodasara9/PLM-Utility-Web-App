function validate(){
	var name = document.getElementById("name").value;
	//alert("var->" + name);

	if(name==""){
		alert("Please enter a valid name.");
		return false;
	}
	else{
		return true;
	}
}