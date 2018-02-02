function checklock(){
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() {
				if (this.readyState == 4 && this.status == 200) {
					// Typical action to be performed when the document is ready:
					if(xhttp.responseText.trim() == "locked"){
						setTimeout(checklock,2000);
					}else{
						window.location="../";
					}
				}
			};
			xhttp.open("GET", "checklock", true);
			xhttp.send();
		}
