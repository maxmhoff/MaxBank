# MaxBank _- Android App Course Project_

## Description
As a final project for my Android App course, we were asked to do a simple bank app. This is how my made-up bank, MaxBank, came to life!
For this project I am using FireStore as the database. FireStore might not be the obvious choice for a bank app since it is NoSQL and document-based,
but I'm eager to learn and become comfortable with it. In regards to the GUI design I have been trying my best to follow the principles outlined by Material, 
which should be reflected in the color choices and most of the layout.

## Usage
You can clone the repo right here from GitHub. You should be able to import the project straight into Android Studio.
Once you get the app up and running on the android phone or emulator of your choice (not really though...advice: Use a new device!), you can safely create a new user. For testing purposes you will be provided with a private account that has 10.000 dkk in it.
As long as you can read danish, the GUI should be relatively intuitive. If you want to test money transfers between accounts that are not your own, you can use: ZQNJCe5nPYdCXTs7A79G as the account number. _This way you'll help me build up a nice and healthy pension account!_

## Disclaimers
First and foremost: This application will have **many** features which would not be implemented in a real bank app. Fx.
the current way of creating an account could be misused. There is no limit to how many accounts you can make in one session.
It also requires way too few details - it is simply way too easy. These features are added for the sake of manual testing and for my presentation.

To avoid complexity I'm *not* using the balance of the accounts in FireStore (they have been set to zero and are not updated). 
Instead I'm calculating the balance via transactions. This way the balance is automatically updated, whenever i make a transaction.
This would be a critical mistake, if you were to make an actual bank app.

I have not in any way been paying attention to the Required API. So you probably want to use the newest device/emulator. Oops.

Part of the requirements was to assign users to the closest branch of the bank. The bank is supposed to have two branches. 
One at Vesterbro (Copenhagen) and another one in Odense. I could have used GPS tracking for this (which undoubtedly would lead to some weird situations) 
or added an address to the users, made some rough calculations and called it a day.
But I think this would be a terrible approach. Most people who uses a bank app already know what branch they are attached to. 
They might have bonds to that particular branch, bonds that do not rely on the sheer geographical aspect but also trust and familiarity.
And the final thing to consider is that in a real world scenario the user would probably be created through a branch and thus this feature would be redundant anyway.
As of now the user can assign him-/herself to a branch via the edit user activity.

Another requirement was to add fixed transfers. I've added the views for it, but it has to be handled server-side, and therefore it is not properly implemented yet.

## TO-DO
A list of things I would like to add in the future:

1. Add a higher level of abstraction to the FireStoreRepo by using listeners.

2. Some of dialog boxes could use some optimizing: Input validation and improved visuals etc. 

3. A custom login page. Right now I'm using the default one provided by the Firebase UI.

4. Add a better looking "settings PopupMenu". ListPopupMenu could be a friend in need... but I have got to look into that!

5. Create a super interesting and cool icon for the app.

## Known bugs
A list of bugs that I am aware of, but have not been able to fix yet:
* If a transaction is added or modified while viewing the corresponding account (AccountFragment) the size of the item_layout stretches vertically. 
_This might be due to the decorator, but I am uncertain._

* The Create Account DialogBox associated to the Floating Action Button can be opened multiple times. _I should probably introduce a boolean or make it into a Dialog Fragment!_

* Once in a while the account_balance view fails to get refreshed when a new account is added.
I suspect this has something to do with the callback from the firestore, but I'm not quite sure.

* The Create Account DialogBox does not survive state changes. The solution is to change it into a dialog fragment.

* If orientation changes while Exposed Dropdown is active the arrayadapter loses all other entries than the one currently selected.
