function toggleVisibility(elementID) {
	var divElement = document.getElementById(elementID);
	var buttonElement = document.getElementById("btn" + elementID);
	if (divElement.style.display == "block") {
		divElement.style.display = "none";
		buttonElement.innerHTML = "+";
	} else {
		divElement.style.display = "block";
		buttonElement.innerHTML = "-";
	}
}

function roundNumber(num, dec) {
	var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
	return result;
}