/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("LoginButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      sendForm('CheckLogin', document.getElementById("ErrorMessageLogin"), e.target.closest("form"));
    } else {
    	 form.reportValidity();
    }
  });
  
  document.getElementById("RegistrationButton").addEventListener('click', (e)=> {
	var form = e.target.closest("form");
	if(form.checkValidity()){
		sendForm('CheckRegistration', document.getElementById("ErrorMessageRegistration"), e.target.closest("form"));
	}else {
    	 form.reportValidity();
    }
});


	function sendForm(servlet, error, form){
		makeCall("POST", servlet, form,
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
              	var user = JSON.parse(req.responseText);
            	sessionStorage.setItem('username', user.username);
            	sessionStorage.setItem('ID', user.ID);
            	//TODO set correct path
                window.location.href = "HomeCS.html";
                break;
              case 400: // bad request
                document.getElementById(error).textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById(error).textContent = message;
                  break;
              case 500: // server error
            	document.getElementById(error).textContent = message;
                break;
            }
          }
        }
      );
	}
		
})();