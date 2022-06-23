/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  //Manage the login form
  document.getElementById("LoginButton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    
    form.addEventListener("submit", (e)=>{
		e.preventDefault();
	});
	
	//The form is checked, if everything is ok an AJAX call is done
    if (form.checkValidity()) {
		makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
              //if ok the user is added to the session storage and the home page is shown
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
  
  //Manage the registration form
  document.getElementById("RegistrationButton").addEventListener('click', (e)=> {
	var form = e.target.closest("form");
	var alert = document.getElementById("messageRegistration_id");
	
	form.addEventListener("submit", (e)=>{
		e.preventDefault();
	});
	
	var mail = document.getElementById("mail_id");
	var psw = document.getElementById("psw_id");
	var psw2 = document.getElementById("psw2_id");

	var pattern = new RegExp('^(.+)@(\\S+)$');
	
	//The form is checked, if everything is ok an AJAX call is done
	if(!pattern.test(mail.value)){
		alert.textContent = "Invalid mail address";
	}
	else if(psw.value!==psw2.value){
		alert.textContent = "Passwords don't match";
	}
	else if(form.checkValidity()){
		makeCall("POST" ,'CheckRegistration', form, function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
			  //in every case a message is shown
              case 200:
              	alert.textContent = message;
                break;
              case 400: // bad request
                alert.textContent = message;
                break;
              case 401: // unauthorized
                alert.textContent = message;
                break;
              case 500: // server error
            	alert.textContent = message;
                break;
            }
          }
        });
	}
	else {
    	 form.reportValidity();
    }
	});
})();