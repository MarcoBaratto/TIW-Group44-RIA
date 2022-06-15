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
              case 401: // unauthorized
              case 500: // server error
              default:
            	document.getElementById("errorMessageLogin").textContent = message;
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
	var errorMessage = document.getElementById("errorMessageRegistration_id");
	
	var mail = document.getElementById("mail_id");
	var psw = document.getElementById("psw_id");
	var psw2 = document.getElementById("psw2_id");

	var pattern = new RegExp('^(.+)@(\\S+)$');
	
	if(!pattern.test(mail.value)){
		errorMessage.textContent = "Invalid mail address";
	}
	else if(psw.value!==psw2.value){
		errorMessage.textContent = "Passwords don't match";
	}
	else if(form.checkValidity()){
		sendForm('CheckRegistration', document.getElementById("errorMessageRegistration"), form);
	}
	else {
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