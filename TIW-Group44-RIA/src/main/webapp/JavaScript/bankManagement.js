{ // avoid variables ending up in the global scope

	  // page components
	  let transferList, accountList, createTransferForm, resultTransferDiv, selectedAccount, 
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
	  
	  function ResultTransferDiv(_resultDiv, _transferOKDiv, _transferKODiv, _fieldsSuccess, _errorP, _closeButton, pageOrchestrator) {
		this.resultDiv = _resultDiv;
		this.transferOkDiv = _transferOKDiv;
		this.transferKoDiv = _transferKODiv;
		this.fieldsSuccess = _fieldsSuccess;
		this.errorP = _errorP;
		this.closeButton = _closeButton;
		this.pageOrchestrator = pageOrchestrator;
		
		this.reset = function() {
	       this.resultDiv.style.display = 'none';
	    }
	    
	    this.closeButton.addEventListener("click", (e) => {
	          this.pageOrchestrator.refresh(selectedAccount);
	     	}, false);
		
		this.showSuccess = function(transfer) {
			this.fieldsSuccess.comments.textContent = transfer.comments;
			this.fieldsSuccess.amount.textContent = "Transfer amount: " + transfer.amount;
			this.fieldsSuccess.originT.textContent = "Origin Account ID: " + transfer.idBankAccountFrom;
			this.fieldsSuccess.destinationT.textContent = "Destination Account ID: " + transfer.idBankAccountTo;
			this.fieldsSuccess.originBalanceB.textContent = "Origin Account Balance before transfer: " + transfer.balancesBefore[0];
			this.fieldsSuccess.originBalanceA.textContent = "Origin Account Balance after transfer: " + transfer.balancesAfter[0];
			this.fieldsSuccess.destinationBalanceB.textContent = "Destination Account Balance before transfer: " + transfer.balancesBefore[1];
			this.fieldsSuccess.destinationBalanceA.textContent = "Destination Account Balance after transfer: " + transfer.balancesAfter[1];
			this.resultDiv.style.display = 'block';
			this.transferKoDiv.style.display = 'none';
			
		}
		
		this.showFailure = function(message) {
			this.errorP.textContent = message;
			this.resultDiv.style.display = 'block';
			this.transferOkDiv.style.display = 'none';
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
	                return;
	              }
	              self.update(transfersToShow); // self visible by closure
	              //if (next) next(); // show the default element of the list if present
	            
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

	  function CreateTransferForm(pageOrchestrator){
		  this.transferForm = document.getElementById("createTransferForm_id");
		  this.accountDestination = document.getElementById("idDestination");
		  this.originAccount = document.getElementById("originAccount_id");

		  document.getElementById("createTransferButton_id").addEventListener("click", (e)=>{
			  if(transferForm.checkValidity()){
				  if(originAccount.value === accountDestination.value) {
					  transferForm.reset();
					  transferForm.showFailure("Origin account and destination can't be the same");
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
								  	  selectedAccount = self.originAccount.value;	
									  pageOrchestrator.showSuccess(JSON.parse(message));
									  
									  break;
								  default:
									  pageOrchestrator.showFailure(message);
							  }
						  }
					  });
			  }
		  })

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
		        destinationT: document.getElementById("destinationT_id"),
		        destinationBalanceB: document.getElementById("destinationBalanceB_id"),
		        destinationBalanceA: document.getElementById("destinationBalanceA_id"),
		        comments: document.getElementById("commentsTransfer_id"),
		        amount: document.getElementById("amountTransfer_id")
	        },
	        document.getElementById("transferError_id"),
	        document.getElementById("closeTransfer_id"),
			this
		  );
	      
	      createTransferForm = CreateTransferForm(this);
/*
	      missionDetails.registerEvents(this); // the orchestrator passes itself --this-- so that the wizard can call its refresh function after updating a mission

	      wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
	      wizard.registerEvents(this);  // the orchestrator passes itself --this-- so that the wizard can call its refresh function after creating a mission

	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })
	      	    */
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
	  }



	/*
    function accountDetails(options) {
      this.alert = options['alert'];
      this.detailcontainer = options['detailcontainer'];
      this.expensecontainer = options['expensecontainer'];
      this.expenseform = options['expenseform'];
      this.closeform = options['closeform'];
      this.date = options['date'];
      this.destination = options['destination'];
      this.status = options['status'];
      this.description = options['description'];
      this.country = options['country'];
      this.province = options['province'];
      this.city = options['city'];
      this.fund = options['fund'];
      this.food = options['food'];
      this.accomodation = options['accomodation'];
      this.travel = options['transportation'];

      this.registerEvents = function(orchestrator) {
        this.expenseform.querySelector("input[type='button']").addEventListener('click', (e) => {
          var form = e.target.closest("form");
          if (form.checkValidity()) {
            var self = this,
              missionToReport = form.querySelector("input[type = 'hidden']").value;
            makeCall("POST", 'CreateExpensesReport', form,
              function(req) {
                if (req.readyState == 4) {
                  var message = req.responseText;
                  if (req.status == 200) {
                    orchestrator.refresh(missionToReport);
                  } else if (req.status == 403) {
                window.location.href = req.getResponseHeader("Location");
                window.sessionStorage.removeItem('username');
                }
                else {
                    self.alert.textContent = message;
                  }
                }
              }
            );
          } else {
            form.reportValidity();
          }
        });

        this.closeform.querySelector("input[type='button']").addEventListener('click', (event) => {
          var self = this,
            form = event.target.closest("form"),
            missionToClose = form.querySelector("input[type = 'hidden']").value;
          makeCall("POST", 'CloseMission', form,
            function(req) {
              if (req.readyState == 4) {
                var message = req.responseText;
                if (req.status == 200) {
                  orchestrator.refresh(missionToClose);
                } else if (req.status == 403) {
                    window.location.href = req.getResponseHeader("Location");
                    window.sessionStorage.removeItem('username');
                }
                else {
                  self.alert.textContent = message;
                }
              }
            }
          );
        });
      }

      this.show = function(missionid) {
        var self = this;
        makeCall("GET", "GetMissionDetailsData?missionid=" + missionid, null,
          function(req) {
            if (req.readyState == 4) {
              var message = req.responseText;
              if (req.status == 200) {
                var mission = JSON.parse(req.responseText);
                self.update(mission); // self is the object on which the function
                // is applied
                self.detailcontainer.style.visibility = "visible";
                switch (mission.status) {
                  case "OPEN":
                    self.expensecontainer.style.visibility = "hidden";
                    self.expenseform.style.visibility = "visible";
                    self.expenseform.missionid.value = mission.id;
                    self.closeform.style.visibility = "hidden";
                    break;
                  case "REPORTED":
                    self.expensecontainer.style.visibility = "visible";
                    self.expenseform.style.visibility = "hidden";
                    self.closeform.missionid.value = mission.id;
                    self.closeform.style.visibility = "visible";
                    break;
                  case "CLOSED":
                    self.expensecontainer.style.visibility = "visible";
                    self.expenseform.style.visibility = "hidden";
                    self.closeform.style.visibility = "hidden";
                    break;
                }
              } else if (req.status == 403) {
                window.location.href = req.getResponseHeader("Location");
                window.sessionStorage.removeItem('username');
                }
                else {
                self.alert.textContent = message;

              }
            }
          }
        );
      };

      this.reset = function() {
        this.detailcontainer.style.visibility = "hidden";
        this.expensecontainer.style.visibility = "hidden";
        this.expenseform.style.visibility = "hidden";
        this.closeform.style.visibility = "hidden";
      }

      this.update = function(m) {
        this.date.textContent = m.startDate;
        this.destination.textContent = m.destination;
        this.status.textContent = m.status;
        this.description.textContent = m.description;
        this.country.textContent = m.country;
        this.province.textContent = m.province;
        this.city.textContent = m.city;
        this.fund.textContent = m.fund;
        this.food.textContent = m.expenses.food;
        this.accomodation.textContent = m.expenses.accomodation;
        this.travel.textContent = m.expenses.transportation;
      }
    }

    function Wizard(wizardId, alert) {
      // minimum date the user can choose, in this case now and in the future
      var now = new Date(),
        formattedDate = now.toISOString().substring(0, 10);
      this.wizard = wizardId;
      this.alert = alert;

      this.wizard.querySelector('input[type="date"]').setAttribute("min", formattedDate);

      this.registerEvents = function(orchestrator) {
        // Manage previous and next buttons
        Array.from(this.wizard.querySelectorAll("input[type='button'].next,  input[type='button'].prev")).forEach(b => {
          b.addEventListener("click", (e) => { // arrow function preserve the
            // visibility of this
            var eventfieldset = e.target.closest("fieldset"),
              valid = true;
            if (e.target.className == "next") {
              for (i = 0; i < eventfieldset.elements.length; i++) {
                if (!eventfieldset.elements[i].checkValidity()) {
                  eventfieldset.elements[i].reportValidity();
                  valid = false;
                  break;
                }
              }
            }
            if (valid) {
              this.changeStep(e.target.parentNode, (e.target.className === "next") ? e.target.parentNode.nextElementSibling : e.target.parentNode.previousElementSibling);
            }
          }, false);
        });

        // Manage submit button
        this.wizard.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
          var eventfieldset = e.target.closest("fieldset"),
            valid = true;
          for (i = 0; i < eventfieldset.elements.length; i++) {
            if (!eventfieldset.elements[i].checkValidity()) {
              eventfieldset.elements[i].reportValidity();
              valid = false;
              break;
            }
          }

          if (valid) {
            var self = this;
            makeCall("POST", 'CreateMission', e.target.closest("form"),
              function(req) {
                if (req.readyState == XMLHttpRequest.DONE) {
                  var message = req.responseText; // error message or mission id
                  if (req.status == 200) {
                    orchestrator.refresh(message); // id of the new mission passed
                  } else if (req.status == 403) {
                    window.location.href = req.getResponseHeader("Location");
                    window.sessionStorage.removeItem('username');
                }
                else {
                    self.alert.textContent = message;
                    self.reset();
                  }
                }
              }
            );
          }
        });
        // Manage cancel button
        this.wizard.querySelector("input[type='button'].cancel").addEventListener('click', (e) => {
          e.target.closest('form').reset();
          this.reset();
        });
      };

      this.reset = function() {
        var fieldsets = document.querySelectorAll("#" + this.wizard.id + " fieldset");
        fieldsets[0].hidden = false;
        fieldsets[1].hidden = true;
        fieldsets[2].hidden = true;

      }

      this.changeStep = function(origin, destination) {
        origin.hidden = true;
        destination.hidden = false;
      }
    }
*/
};
