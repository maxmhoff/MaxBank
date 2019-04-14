# MaxBank

## Thoughts:
To avoid complexity I'm *not* using the balance of the accounts in FireStore (they have been set to zero and are not updated). 
Instead I'm calculating the balance via transactions. This way the balance is automatically updated, whenever i make a transaction.
It would be a terrible approach, if you were to make an actual bank app. But... this is just a mock-up.

The current way of creating an account could be misused. There is no limit to how many accounts you can make in one session.
It also requires way too few details. In fact maybe accounts should only be created by employees at the bank?



## TO-DO:
1. Implement login functionality 
	* Add a login activity
	* Add a logout button

2. Add pay functionality

3. Add transaction functionality

## Known bugs
* If a transaction is added or modified while viewing the corresponding account (AccountFragment) the size of the item_layout stretches.
