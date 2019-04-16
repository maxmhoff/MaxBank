# MaxBank _- Android App Course Project_
### Description
As a final project for my Android App course, we were asked to do a simple bank app. This is how my made-up bank, MaxBank, came to life!
For this project I am using FireStore as the database. FireStore might not be the obvious choice for a bank app since it is NoSQL and document-based,
but I'm eager to learn and become comfortable with it. In regards to the GUI design I have been trying my best to follow the principles outlined by Material, 
which should be reflected in the color choices and some of the layout.

### Disclaimers
This application will have features which would not be implemented in a real bank app, fx:
The current way of creating an account could be misused. There is no limit to how many accounts you can make in one session.
It also requires way too few details - it is simply way too easy. These features are added for the sake of manual testing and for my presentation.

To avoid complexity I'm *not* using the balance of the accounts in FireStore (they have been set to zero and are not updated). 
Instead I'm calculating the balance via transactions. This way the balance is automatically updated, whenever i make a transaction.
It would be a terrible approach, if you were to make an actual bank app.

### TO-DO
A list of things I would like to add in the future:
1. Implement login functionality: 
	* Add a login activity
	* Add a logout button (should be attached to the MainActivity view, somewhere)

2. Add pay functionality

3. Add transaction functionality
    * Move money from one account to another and create a transaction in both accounts to reflect the change.

4. Implement a better architecture - this could include:
    * Using methods provided by the default fragment template fom Android Studio
	* Adding a higher level of abstraction to the FireStoreRepo
	* Creating ViewModel(s) for either activites & fragments or firestore
	* Implementing an Observer pattern that triggers the updateView() functions of the activities & fragments 

### Known bugs
* If a transaction is added or modified while viewing the corresponding account (AccountFragment) the size of the item_layout stretches vertically. 
_This might be due to the decorator, but I am uncertain._
