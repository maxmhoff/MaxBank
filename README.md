# MaxBank _- Android App Course Project_

## Description
As a final project for my Android App course, we were asked to do a simple bank app. This is how my made-up bank, MaxBank, came to life!
For this project I am using FireStore as the database. FireStore might not be the obvious choice for a bank app since it is NoSQL and document-based,
but I'm eager to learn and become comfortable with it. In regards to the GUI design I have been trying my best to follow the principles outlined by Material, 
which should be reflected in the color choices and most of the layout.

## Disclaimers
First and foremost: This application will have **many** features which would not be implemented in a real bank app. Fx.
the current way of creating an account could be misused. There is no limit to how many accounts you can make in one session.
It also requires way too few details - it is simply way too easy. These features are added for the sake of manual testing and for my presentation.

To avoid complexity I'm *not* using the balance of the accounts in FireStore (they have been set to zero and are not updated). 
Instead I'm calculating the balance via transactions. This way the balance is automatically updated, whenever i make a transaction.
This would be a critical mistake, if you were to make an actual bank app.

Part of the requirements was to assign users to the closest branch of the bank. The bank is supposed to have two branches. 
One at Vesterbro (Copenhagen) and another one in Odense. I could have used GPS tracking for this (which undoubtedly would lead to some weird situations) 
or added an address to the users, made some rough calculations and called it a day.
But I think this would be a terrible approach. Most people who uses a bank app already know what branch they are attached to. 
They might have bonds to that particular branch, bonds that do not rely on the sheer geographical aspect but also trust and familiarity.
And the final thing to consider is that in a real world scenario the user would probably be created through a branch and thus this feature would be redundant anyway.
As of now the user can assign him-/herself to a branch via the edit user activity.

## TO-DO
A list of things I would like to add in the future:

1. Add pay functionality

2. Add transaction functionality
    * Move money from one account to another and create a transaction in both accounts to reflect the change.

3. Implement a better architecture - this could include:
    * Using methods provided by the default fragment template fom Android Studio.
	* Adding a higher level of abstraction to the FireStoreRepo.
	* Implementing an Observer pattern that triggers the updateView() functions of the activities & fragments. _To avoid those pesky NullPointerExceptions!_

4. More visual feedback on the dialogbox forms: Disabled create button, untill fields are properly filled etc.

5. A custom login page. Right now I'm using the default one provided by the Firebase UI.

6. Add a better looking settings PopupMenu. ListPopupMenu could be a friend in need... but I have got to look into that!

7. Create a super interesting and cool icon for the app.

## Known bugs
A list of bugs that I am aware of, but have not been able to fix yet:
* If a transaction is added or modified while viewing the corresponding account (AccountFragment) the size of the item_layout stretches vertically. 
_This might be due to the decorator, but I am uncertain._

* The Create Account DialogBox associated to the Floating Action Button can be opened multiple times. _I should probably introduce a boolean._

* The Create User DialogBox can only be used in portrait mode, since the form is too big for landscape.