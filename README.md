# MaxBank
### Description
As a final project for my Android App course, we were asked to do a simple bank app. This is how my made-up bank, MaxBank, came to life!

### Disclaimers
To avoid complexity I'm *not* using the balance of the accounts in FireStore (they have been set to zero and are not updated). 
Instead I'm calculating the balance via transactions. This way the balance is automatically updated, whenever i make a transaction.
It would be a terrible approach, if you were to make an actual bank app. But... this is just a mock-up.

The current way of creating an account could be misused. There is no limit to how many accounts you can make in one session.
It also requires way too few details. In fact maybe accounts should only be created by employees at the bank?

### TO-DO
1. Implement login functionality: 
	* Add a login activity
	* Add a logout button (should be attached to the MainActivity view, somewhere)

2. Add pay functionality

3. Add transaction functionality

4. Implement a better architecture - this could include:
	* Create a repository for Firestore
	* Create ViewModel(s) for either activites & fragments or firestore
	* Implement an Observer pattern that triggers the updateView() functions of the activities & fragments 

### Known bugs
* If a transaction is added or modified while viewing the corresponding account (AccountFragment) the size of the item_layout stretches.
