/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  document.getElementById("LoginButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
		makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
              	var user = JSON.parse(x.responseText);
            	sessionStorage.setItem('username', user.username);
            	sessionStorage.setItem('ID', user.id);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errorMessageLogin").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errorMessageLogin").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errorMessageLogin").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });
  
  document.getElementById("RegistrationButton").addEventListener('click', (e)=> {
	var form = e.target.closest("form");
	if(form.checkValidity()){
		sendForm('CheckRegistration', document.getElementById("errorMessageRegistration"), e.target.closest("form"));
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
              	error.textContent = message;
                break;
              case 400: // bad request
                error.textContent = message;
                break;
              case 401: // unauthorized
                  error.textContent = message;
                  break;
              case 500: // server error
            	error.textContent = message;
                break;
            }
          }
        }
      );
	}
		
})();