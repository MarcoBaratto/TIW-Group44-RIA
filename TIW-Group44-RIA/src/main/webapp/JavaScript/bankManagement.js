{ // avoid variables ending up in the global scope

	  // page components
	  let transferList, accountList, transferForm, resultTransferDiv, selectedAccount, addressBook, 
	    pageOrchestrator = new PageOrchestrator(); // main controller

	  window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "LoginRegistration.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    } // display initial content
	  }, false);


	  // Constructors of view components
	  
	  function ResultTransferDiv(_resultDiv, _transferOKDiv, _transferKODiv, _fieldsSuccess, _errorP, _closeButton, contactMessage, pageOrchestrator) {
		this.resultDiv = _resultDiv;
		this.transferOkDiv = _transferOKDiv;
		this.transferKoDiv = _transferKODiv;
		this.fieldsSuccess = _fieldsSuccess;
		this.errorP = _errorP;
		this.closeButton = _closeButton;
		this.contactMessage = contactMessage;
		this.pageOrchestrator = pageOrchestrator;
		
		this.reset = function() {
	       this.resultDiv.style.display = 'none';
	       this.contactMessage.textContent = "";
	    }
	    
	    this.closeButton.addEventListener("click", (e) => {
	          this.pageOrchestrator.refresh(selectedAccount);
	     	}, false);
		
		this.showSuccess = function(transfer) {
			this.fieldsSuccess.comments.textContent = transfer.comments;
			this.fieldsSuccess.amount.textContent = "Transfer amount: " + transfer.amount;
			this.fieldsSuccess.originT.textContent = "Origin Account ID: " + transfer.idBankAccountFrom;
			this.fieldsSuccess.ownerDestination.textContent = "Destination Owner ID: " + transfer.idOwnerTo;
			this.fieldsSuccess.destinationT.textContent = "Destination Account ID: " + transfer.idBankAccountTo;
			this.fieldsSuccess.originBalanceB.textContent = "Origin Account Balance before transfer: " + transfer.balancesBefore[0];
			this.fieldsSuccess.originBalanceA.textContent = "Origin Account Balance after transfer: " + transfer.balancesAfter[0];
			this.fieldsSuccess.destinationBalanceB.textContent = "Destination Account Balance before transfer: " + transfer.balancesBefore[1];
			this.fieldsSuccess.destinationBalanceA.textContent = "Destination Account Balance after transfer: " + transfer.balancesAfter[1];
			this.addContactButton = this.fieldsSuccess.addContactButton;
			
			this.resultDiv.style.display = 'block';
			this.transferKoDiv.style.display = 'none';
			this.transferOkDiv.style.display = 'block';
		
			
			var contactsAlreadyPresent = addressBook.get(transfer.idOwnerTo.toString());
			var array = Array.from(contactsAlreadyPresent);
			if(array.includes(transfer.idBankAccountTo)){
				this.addContactButton.style.display = 'none';
			}else
				this.addContactButton.style.display = 'block';
			
			this.addContactButton.addEventListener("click", (e)=>{
				makeCall("GET", "AddToContacts?contactId="+transfer.idOwnerTo+"&contactAccount="+transfer.idBankAccountTo, null,
				function(req){
					if (req.readyState == 4) {
		            	switch(req.status){
							case 200:
								contactMessage.textContent = "Contact ADDED";
								break;
							case 400:
							case 500:
							default:
								contactMessage.textContent = req.responseText;	
						}			
					}
				});
			});
		}
		
		this.showFailure = function(message) {
			this.errorP.textContent = message;
			this.resultDiv.style.display = 'block';
			this.transferOkDiv.style.display = 'none';
			this.transferKoDiv.style.display = 'block';
		}
			
	  }

	  function AccountList(_alert, _listcontainer, _listcontainerbody) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;

	    this.reset = function() {
	      this.listcontainer.style.display = 'none';
	    }

	    this.show = function(next) {
		  this.listcontainer.style.display = 'block';
	      var self = this;
	      makeCall("GET", "GetAccountsData", null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var accountsToShow = JSON.parse(req.responseText);
	              if (accountsToShow.length == 0) {
	                self.alert.textContent = "No accounts yet, please create a new one";
	                return;
	              }
	              self.update(accountsToShow); // self visible by closure
	              if (next) next(); // show the default element of the list if present
	            
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  window.sessionStorage.removeItem('ID');
                  }
                  else {
	            self.alert.textContent = message;
	          }}
	        }
	      );
	    };


	    this.update = function(arrayAccounts) {
	      var row, destcell, datecell, linkcell, anchor;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	      // build updated list
	      var self = this;
	      arrayAccounts.forEach(function(account) { // self visible here, not this
	        row = document.createElement("tr");
	        destcell = document.createElement("td");
	        destcell.textContent = account.id;
	        row.appendChild(destcell);
	        datecell = document.createElement("td");
	        datecell.textContent = account.balance;
	        row.appendChild(datecell);
	        linkcell = document.createElement("td");
	        anchor = document.createElement("a");
	        linkcell.appendChild(anchor);
	        linkText = document.createTextNode("Show");
	        anchor.appendChild(linkText);
	        //anchor.missionid = mission.id; // make list item clickable
	        anchor.setAttribute('accountid', account.id); // set a custom HTML attribute
	        anchor.addEventListener("click", (e) => {
	          // dependency via module parameter
	          selectedAccount = e.target.getAttribute("accountid");
	          transferList.show(e.target.getAttribute("accountid")); // the list must know the details container
	        }, false);
	        anchor.href = "#";
	        row.appendChild(linkcell);
	        self.listcontainerbody.appendChild(row);
	      });
	      this.listcontainer.style.visibility = "visible";
		  	
	    }

	    this.autoclick = function(accountId) {
	      var e = new Event("click");
	      var selector = 'a[accountid="' + accountId + '"]';
	      var anchorToClick =  // the first account or the account with id = accountId
	        (accountId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
	      if (anchorToClick) anchorToClick.dispatchEvent(e);
	    }
	  }
	  
	  function TransferList(_alert, _listcontainer, _listcontainerbody) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;

	    this.reset = function() {
	      this.listcontainer.style.display = 'none';
	    }

	    this.show = function(accountId) {
		  pageOrchestrator.getAddressBook();
		  this.listcontainer.style.display = 'block';
	      var self = this;
	      makeCall("GET", "GetTransfersData?bankAccountid=" + accountId, null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
				  document.getElementById("originAccount_id").value=accountId;
	              var transfersToShow = JSON.parse(req.responseText);
	              if (transfersToShow.length == 0) {
	                self.alert.textContent = "No transfers yet!";
	                self.listcontainerbody.innerHTML = "";
	                self.listcontainer.style.display = 'none';
	                return;
	              }else{
				  	self.update(transfersToShow); 
				  	self.listcontainer.style.display = 'block';
				  }
	              	
	            
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  window.sessionStorage.removeItem('ID');
                  }
                  else {
	            self.alert.textContent = message;
	          }}
	        }
	      );
	    };


	    this.update = function(arrayTransfers) {
	      var row, idTransferCell, amountCell, originCell, destinationCell, dateCell, commentsCell;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	      // build updated list
	      var self = this;
	      arrayTransfers.forEach(function(transfer) { // self visible here, not this
	        row = document.createElement("tr");
	        
	        idTransferCell = document.createElement("td");
	        idTransferCell.textContent = transfer.id;
	        row.appendChild(idTransferCell);
	        
	        amountCell = document.createElement("td");
	        amountCell.textContent = transfer.amount;
	        if(transfer.idBankAccountFrom == selectedAccount)
	        	amountCell.className="negative";
	        else
	        	amountCell.className="positive";
	        row.appendChild(amountCell);
	        
	        originCell = document.createElement("td");
	        originCell.textContent = transfer.idBankAccountFrom;	        
	        row.appendChild(originCell);
	        
	        destinationCell = document.createElement("td");
	        destinationCell.textContent = transfer.idBankAccountTo;	        
	        row.appendChild(destinationCell);
	        
	        dateCell = document.createElement("td");
	        dateCell.textContent = transfer.date;	        
	        row.appendChild(dateCell);
	        
	        commentsCell = document.createElement("td");
	        commentsCell.textContent = transfer.comments;	        
	        row.appendChild(commentsCell);
	        
	        self.listcontainerbody.appendChild(row);
	      });
	      this.listcontainer.style.visibility = "visible";

	    }
	  }

	  function TransferForm(pageOrchestrator){
		  var transferForm = document.getElementById("createTransferForm_id");
		  var accountDestination = document.getElementById("idDestination");
		  var originAccount = document.getElementById("originAccount_id");
		  var userDestDatalist = document.getElementById("userContacts_id");
		  var accountDestDatalist = document.getElementById("accountContacts_id");
		  var userDestination = document.getElementById("userDestination");

		  document.getElementById("createTransferButton_id").addEventListener("click", (e)=>{
			  if(transferForm.checkValidity()){
				  if(originAccount.value === accountDestination.value) {
					  transferForm.reset();
					  pageOrchestrator.showFailure("Origin account and destination can't be the same");
					  //can't check balance-amount because the value could be outdated
					  return;
				  }
				  var self = this;
				  makeCall("POST", "CreateTransfer", transferForm,
					  function (x){
						  if (x.readyState == XMLHttpRequest.DONE) {
							  var message = x.responseText;							  
							  switch (x.status) {
								  case 200:
								  	  selectedAccount = originAccount.value;	
									  pageOrchestrator.showSuccess(JSON.parse(message));
									  
									  break;
								  default:
									  pageOrchestrator.showFailure(message);
							  }
						  }
					  });
			  }
		  });
		  
		  document.getElementById("idDestination").addEventListener("click", (e)=>{
			accountDestDatalist.innerHTML="";
			if(typeof addressBook !== 'undefined'&&addressBook.has(userDestination.value)){
				set = addressBook.get(userDestination.value);
				set.forEach(function(value){
					if(value!==parseInt(selectedAccount)){
						option = document.createElement("option");
						option.text = value;
						option.value = value;
						accountDestDatalist.appendChild(option);
					}	
				})
			}
		});
		  
		  this.autoComplete = function(){
			userDestDatalist.innerHTML="";
			keys = Array.from(addressBook.keys());
			keys.forEach(function(idUser){
				option = document.createElement("option");
				option.text = idUser;
				option.value = idUser;
				userDestDatalist.appendChild(option);
			});
		}

	  }
	  
	  function CreateAccount(pageOrchestrator){
		var createAccountButton = document.getElementById("createAccount_id");
		var createAccountForm = document.getElementById("createAccountForm_id");
		 
		createAccountButton.addEventListener("click", (e)=>
		  {
			makeCall("POST", "CreateBankAccount", createAccountForm,
			function(x){
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					switch(x.status){
						case 200:
							pageOrchestrator.refresh();
							break;	
						default:
						}						  
							document.getElementById("createAccountMessage_id").textContent = message; 	
				}
			});
		});
	}



	  function PageOrchestrator() {
	    var alertContainer = document.getElementById("id_alert");
	    
	    this.start = function() {
		  document.getElementById("username_id").textContent= sessionStorage.getItem('username');
		  document.getElementById("user_id").textContent= sessionStorage.getItem('ID');

		  accountList = new AccountList(
	        alertContainer,
	        document.getElementById("accountsList_id"),
	        document.getElementById("accountsListBody_id"));
	        
	      transferList = new TransferList(
	        alertContainer,
	        document.getElementById("transferList_id"),
	        document.getElementById("transferListBody_id"));  
	        
	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	        window.sessionStorage.removeItem('ID');
	      })
	      
	      resultTransferDiv = new ResultTransferDiv(
			document.getElementById("result_id"),
			document.getElementById("transferOK_id"),
			document.getElementById("transferKO_id"),
			{ // many parameters, wrap them in an object
		        originT: document.getElementById("originT_id"),
		        originBalanceB: document.getElementById("originBalanceB_id"),
		        originBalanceA: document.getElementById("originBalanceA_id"),
		        ownerDestination: document.getElementById("ownerDestination_id"),
		        destinationT: document.getElementById("destinationT_id"),
		        destinationBalanceB: document.getElementById("destinationBalanceB_id"),
		        destinationBalanceA: document.getElementById("destinationBalanceA_id"),
		        comments: document.getElementById("commentsTransfer_id"),
		        amount: document.getElementById("amountTransfer_id"),
		        addContactButton: document.getElementById("addContactButton_id")
	        },
	        document.getElementById("transferError_id"),
	        document.getElementById("closeTransfer_id"),
	        document.getElementById("contactMessage_id"),
			this
		  );
	      
	      transferForm = new TransferForm(this);
	      
	      createBankAccountForm = new CreateAccount(this);				
	      
	    };

	    this.refresh = function(currentAccount) { // currentAccount initially null at start
	      alertContainer.textContent = "";        // not null after creation of status change
	      //accountList.reset();
	      resultTransferDiv.reset()
	      accountList.show(function() {
	        accountList.autoclick(currentAccount); 
	      }); // closure preserves visibility of this
	    };

		this.showSuccess = function (transfer){
			accountList.reset();
			transferList.reset();
			resultTransferDiv.showSuccess(transfer);
		}
		
		this.showFailure = function (message){
			accountList.reset();
			transferList.reset();
			resultTransferDiv.showFailure(message);
		}
		
		this.getAddressBook = function(){
			makeCall("GET", "GetContacts", null, 
			function (x){
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;							  
					switch (x.status) {
					case 200:
						addressBook = new Map(Object.entries(JSON.parse(message)));
						transferForm.autoComplete();
						break;
					case 400:
					case 500:	
					default:
						document.getElementById("contactsError_id").textContent = "Unable to recover contacts";
						
					}
				}
			});
		}
	  }



};
