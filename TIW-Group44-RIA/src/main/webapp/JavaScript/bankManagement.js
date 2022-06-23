{ // avoid variables ending up in the global scope

	  // page components
	  let transferList, accountList, transferForm, resultTransferDiv, selectedAccount, addressBook, 
	    pageOrchestrator = new PageOrchestrator(); // main controller

	  //Actions done when the home page is loaded
	  window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "LoginRegistration.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    }
	  }, false);


	  // Constructors of view components
	  //The div that handles all the possible result of a transfer
	  function ResultTransferDiv(_resultDiv, _transferOKDiv, _transferKODiv, _fieldsSuccess, _errorP, _closeButton, contactMessage, pageOrchestrator) {
		this.resultDiv = _resultDiv;
		this.transferOkDiv = _transferOKDiv;
		this.transferKoDiv = _transferKODiv;
		this.fieldsSuccess = _fieldsSuccess;
		this.errorP = _errorP;
		this.closeButton = _closeButton;
		this.contactMessage = contactMessage;
		this.pageOrchestrator = pageOrchestrator;
		
		//Hide the div and empty the message about the possibility of add the bank account to the address book
		this.reset = function() {
	       this.resultDiv.style.display = 'none';
	       this.contactMessage.textContent = "";
	    }
	    
	    //When the close button is clicked the home page is refreshed
	    this.closeButton.addEventListener("click", (e) => {
	          this.pageOrchestrator.refresh(selectedAccount);
	     	}, false);
		
		//If a transfer has gone well all the info about this are shown
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
			this.addContactButton.style.display = 'block';
		
		    //Check if the add button has to be shown
			if(addressBook !== undefined && addressBook.has(transfer.idOwnerTo.toString())) {
				var contactsAlreadyPresent = addressBook.get(transfer.idOwnerTo.toString());
				var array = Array.from(contactsAlreadyPresent);
				if(array.includes(transfer.idBankAccountTo))
					this.addContactButton.style.display = 'none';
			}else
				this.addContactButton.style.display = 'block';
			
			var self = this;
			
			//an AJAX call is done to add the contact to the address book
			this.addContactButton.addEventListener("click", (e)=>{
				makeCall("GET", "AddToContacts?contactId="+transfer.idOwnerTo+"&contactAccount="+transfer.idBankAccountTo, null,
				function(req){
					if (req.readyState == 4) {
		            	switch(req.status){
							//in every case a message is shown and the button hidden
							case 200:
								contactMessage.textContent = "Contact ADDED";
								break;
							case 400:
							case 500:
							default:
								contactMessage.textContent = req.responseText;	
						}
						self.addContactButton.style.display = 'none';			
					}
				});
			});
		}
		
		//If a transfer hasn't gone well the reason is shown
		this.showFailure = function(message) {
			this.errorP.textContent = message;
			this.resultDiv.style.display = 'block';
			this.transferOkDiv.style.display = 'none';
			this.transferKoDiv.style.display = 'block';
		}
			
	  }
	  
	  //The List containing all the accounts of the user in the session
	  function AccountList(_alert, _listcontainer, _listcontainerbody, _listaccounttable) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;
	    this.listaccounttable = _listaccounttable;

		//Hide the account list
	    this.reset = function() {
	      this.listcontainer.style.display = 'none';
	    }

		//Display the account list
	    this.show = function(next) {
	      this.listcontainer.style.display = 'block';
	      var self = this;
	      
	      //an AJAX call is done to get the accounts
	      makeCall("GET", "GetAccountsData", null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
				  //If everything goes well the account list is updated
	              var accountsToShow = JSON.parse(req.responseText);
	              self.update(accountsToShow); 
	              // show the default element of the list if present
	              if (next) next(); 
	   
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
	    
		//Updates the account List with alle the accounts given by input
	    this.update = function(arrayAccounts) {
	      var row, destcell, datecell, linkcell, anchor;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	      var self = this;
	      
	      //If there aren't accounts a message is shown and the table is hidden
	      if (arrayAccounts.length == 0) {
                this.alert.textContent = "No accounts yet, please create a new one";
                this.listaccounttable.style.display = 'none';
                document.getElementById("listTransfersTable_id").style.display = 'none';
                return;
	      }
	      this.listcontainer.style.display = 'block';
	      this.listaccounttable.style.display = 'block';
	      //Add every account in a row of the table
	      arrayAccounts.forEach(function(account) { 
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
	        anchor.setAttribute('accountid', account.id); // set a custom HTML attribute
	        
	        //Add the listener to the link for every account
	        anchor.addEventListener("click", (e) => {
	          pageOrchestrator.refreshAlert();
	          selectedAccount = e.target.getAttribute("accountid");
	          transferList.show(e.target.getAttribute("accountid"));
	        }, false);
	        anchor.href = "#";
	        row.appendChild(linkcell);
	        self.listcontainerbody.appendChild(row);
	      });
	      this.listcontainer.style.visibility = "visible";
	    }

		//When the page is loaded/reloaded the transfers of the first or the selected bank account are shown
	    this.autoclick = function(accountId) {
	      var e = new Event("click");
	      var selector = 'a[accountid="' + accountId + '"]';
	      var anchorToClick =  // the first account or the account with id = accountId
	        (accountId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
	      if (anchorToClick) anchorToClick.dispatchEvent(e);
	    }
	  }
	  
	  //The List containing all the transfers of the selected bank account
	  function TransferList(_alert, _listcontainer, _listcontainerbody, _listtransfertable) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;
	    this.listtransfertable = _listtransfertable;

		//Hide the transfer list
	    this.reset = function() {
	      this.listcontainer.style.display = 'none';
	    }

		//Display the transfer list
	    this.show = function(accountId) {
		  //get the address book
		  pageOrchestrator.getAddressBook();
		  this.listcontainer.style.display = 'block';
	      var self = this;
	      
	      //an AJAX call is done to get the transfers
	      makeCall("GET", "GetTransfersData?bankAccountid=" + accountId, null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
				  //If everything has gone well the transfers are shown
	              var transfersToShow = JSON.parse(req.responseText);
				  self.update(transfersToShow); 	            
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

		//Build updated transfer list
	    this.update = function(arrayTransfers) {
	      var row, idTransferCell, amountCell, originCell, destinationCell, dateCell, commentsCell;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	     
	      //If there aren't transfers a message is shown and the table is hidden
	      if (arrayTransfers.length == 0) {
                this.alert.textContent = "No transfers yet!";
                this.listtransfertable.style.display = 'none';
                return;
	      }  
	      this.listcontainer.style.display = 'block';
	      this.listtransfertable.style.display = 'block';
	      let p = document.querySelector("#listTransfersTable_id p");
          p.innerHTML = "Selected Account: " + selectedAccount;        
	      var self = this;
	      //Add every transfer in a row of the table
	      arrayTransfers.forEach(function(transfer) {
	        row = document.createElement("tr");
	        
	        idTransferCell = document.createElement("td");
	        idTransferCell.textContent = transfer.id;
	        row.appendChild(idTransferCell);
	        
	        amountCell = document.createElement("td");
	        amountCell.textContent = transfer.amount;
	        
	        //set the cell's class based on the transfer's origin and destination
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

	  //The Form where a transfer can be created
	  function TransferForm(pageOrchestrator){
		  var transferForm = document.getElementById("createTransferForm_id");
		  var accountDestination = document.getElementById("idDestination");
		  var userDestDatalist = document.getElementById("userContacts_id");
		  var accountDestDatalist = document.getElementById("accountContacts_id");
		  var userDestination = document.getElementById("userDestination");
		  var amount = document.getElementById("amountT_id");
			
		  transferForm.addEventListener("submit", (e)=>{
			e.preventDefault();
		  });	
		  
		  //A listener when the submit button is clicked that handles the creation of a transfer
		  document.getElementById("createTransferButton_id").addEventListener("click", (e)=>{
			  pageOrchestrator.refreshAlert();
			  if(transferForm.checkValidity()){
				  //Some check
				  if(selectedAccount === accountDestination.value) {
					  pageOrchestrator.showFailure("Origin account and destination can't be the same");
					  //can't check balance-amount because the value could be outdated
					  return;
				  }
				  if(amount.value<=0){
				      pageOrchestrator.showFailure("Amount must be greater than 0");
				      return;
				  }
				  
				  var formData = new FormData(transferForm);
				  formData.append("bankAccountidOrigin", selectedAccount);
		
				  //an AJAX call is done to create the transfer
				  makeCall("POST", "CreateTransfer", formData,
					  function (x){
						  if (x.readyState == XMLHttpRequest.DONE) {
							  var message = x.responseText;
							  transferForm.reset();						  
							  switch (x.status) {
								  case 200:
								  	  //If the transfer is ok the success div is shown	
									  pageOrchestrator.showSuccess(JSON.parse(message));
									  break;
								  default:
								  	  //If the transfer isn't' ok the failure div is shown	
									  pageOrchestrator.showFailure(message);
							  }
						  }
					  });
			  }
		  });
		  
		  //When this field all the bank accounts corresponding to the user inserted in the userDestination form are loaded
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
		  
		  //Insert the data of the address book in the datalist of the user destination to allow the autoComplete
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
	  
	  //The Form where a bank account can be created
	  function CreateAccount(pageOrchestrator){
		var createAccountButton = document.getElementById("createAccount_id");
		var createAccountForm = document.getElementById("createAccountForm_id");
		
		createAccountForm.addEventListener("submit", (e)=>{
			e.preventDefault();
		});
		 
		//Listener that handles the creation of a bank account 
		createAccountButton.addEventListener("click", (e)=>
		  {
			//an AJAX call is done to create the bank account
			makeCall("POST", "CreateBankAccount", createAccountForm,
			function(x){
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;
					switch(x.status){
						case 200:
							//If ok, the page is refresh, allowing to see the new bank account
							pageOrchestrator.refresh();
							break;	
						default:
						}						  
							document.getElementById("createAccountMessage_id").textContent = message; 	
				}
			});
		});
	}

	  //The page Orchestrator handles all the components
	  function PageOrchestrator() {
	    var alertContainer = document.getElementById("id_alert");
	  	var alertList = [
			document.getElementById("id_alert"),
			document.getElementById("createAccountMessage_id"),
			document.getElementById("contactsError_id"),
			document.getElementById("contactMessage_id"),
			document.getElementById("transferError_id")
		  ];
	    
	    //Start creates all the component of the page
	    this.start = function() {
		  document.getElementById("username_id").textContent= sessionStorage.getItem('username');
		  document.getElementById("user_id").textContent= sessionStorage.getItem('ID');

		  accountList = new AccountList(
	        alertContainer,
	        document.getElementById("accountsList_id"),
	        document.getElementById("accountsListBody_id"),
	        document.getElementById("listAccountsTable_id"));
	        
	      transferList = new TransferList(
	        alertContainer,
	        document.getElementById("transferList_id"),
	        document.getElementById("transferListBody_id"),
	        document.getElementById("listTransfersTable_id"));  
	        
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

		//The page is refreshed and are shown the account list, the transfer list , the bank account creation form and the transfer creation form
	    this.refresh = function(currentAccount) { // currentAccount initially null at start
	      alertContainer.textContent = "";        // not null after creation of status change
	      resultTransferDiv.reset();
	      this.refreshAlert();
	      accountList.show(function() {
	        accountList.autoclick(currentAccount); 
	      }); 
	    };

		//Method called when a transfer is ok to show the correct div
		this.showSuccess = function (transfer){
			accountList.reset();
			transferList.reset();
			resultTransferDiv.showSuccess(transfer);
		}
		
		//Method called when a transfer isn't ok' to show the correct div
		this.showFailure = function (message){
			accountList.reset();
			transferList.reset();
			resultTransferDiv.showFailure(message);
		}
		
		//Get the address book
		this.getAddressBook = function(){
			//an AJAX call is done to get the address book 
			makeCall("GET", "GetContacts", null, 
			function (x){
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;							  
					switch (x.status) {
					case 200:
						//if ok the address book is transformed into a map and the info are loaded to allow the autoComplete
						addressBook = new Map(Object.entries(JSON.parse(message)));
						transferForm.autoComplete();
						break;
					case 400:
						break;
					case 500:	
					default:
						document.getElementById("contactsError_id").textContent = "Unable to recover contacts";
					}
				}
			});
		}
		
		//All the alert component are cleared
		this.refreshAlert = function() {
			alertList.forEach(function(alert) {
				alert.textContent = "";
			})
		}
	  }



};
