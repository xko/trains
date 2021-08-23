# Coding Challenge
Railway dispatching system for the WunderReise GmbH.

## Problem Specification
You’re a railway scheduling expert and you design and implement such systems for a living.
A new customer, WunderReise GmbH ordered a scheduling system for their airport terminal shuttle railway.
All their terminals are connected with parallel railway lines, with one dedicated train per line. 
The trains can move back and forth, and they should pick up passengers according to their pickup requests.

Here is an example:

```
   line A |--------------------|------[train]-------|
   line B |-------[train]------|--------------------|
       Terminal A           Terminal B           Terminal C
```

They have the following requirements:
* The scheduling system should be able to handle multiple lines with a maximum of 32.
* The scheduling system should be able to handle multiple terminals with a maximum of 32.
* The scheduling system needs an interface to query the state of all trains. That should include at least the following information: current terminal and direction.
* Furthermore, an endpoint is required that allows a train to update its state within the scheduling system.
* And of course, your scheduling system needs to be able to process **pick-up** requests.
* Finally, this system should work in discrete steps (time or state steps) and an endpoint is required to move the state forward.
* Your solution should be built as an executable other systems can interact with. How that interaction is designed is up to you. It can be as simple as a CLI.
* You can ignore the train capacity in your solution if you want to.
* You can ignore intermediate positions (between terminals) of a train and just make it go directly from terminal to terminal.


**A pick-up** is an indicator of where a user is currently located (terminal) and to which terminal they want to go.

How are you going to design a system that tries to service pick-up requests as fast as possible? What data structures, interfaces, and algorithms will you need?
And of course you don’t have to overdo it. In the end, we know that you have to work on it next to your daily life. A simple solution is perfectly fine. Should you hit a road block don’t hesitate to ask us.

## Assessment Criteria
We expect that your code is well-factored, without needless duplication, follow good practices and be automatically verified.
What we will look at:
- How clean is your design and implementation, how easy it is to understand and maintain your code.
- How you verified your software, if by automated tests or some other way.
