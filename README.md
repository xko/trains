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

## The implementation 

### Algorithm
Overall goal of the system is to achieve the idle state (when there are no more pickups to serve) as soon as possible.

The [scheduler](src/main/scala/wunderreise/Scheduler.scala) behavior is: 
 1. at each step unassign all not yet boarded pickups from the trains and combine with pickups requested at the current stage
 2. for each pickup find the best train and ETA
    - ETA = the time when train would go idle, after serving its already assigned queue + this pickup
    - ETA is found by fully simulating train behavior
 3. assign the pickup with the best ETA to the matching train
 4. repeat from (2) with remaining pickups
 5. when there are no more pickups to assign advance all the trains to the next step

The individual [train](src/main/scala/wunderreise/Train.scala) optimizes its route by choosing direction at each step:
 - find the farthest one of the terminals it has to visit to the left and to the right
 - pick the closest of them, and go towards it
 - where points to visit are:
   - drop-offs of already boarded passengers
   - for pickups to the right where destination is farther to the right - destination point
   - for remaining pickups to the right - pickup point
   - analogously for pickups to the left

Limitations:
- the terminals are discrete positions of the trains, identified by numbers, distance between terminals is ignored
- train capacity is ignored
- the system does not account for the stop durations, i.e. we assume passengers jump in and out on the go :)
- the algorithm is suboptimal
  - although it performs reasonably well in the tests, there are cases with known better routes 
    (see ignored testcases in [SingleTrainSpec](src/test/scala/wunderreise/SingleTrainSpec.scala)

### Building and testing

The project is sbt-based, the usual `sbt test` will compile it and run all the test suites. 
There's also [sbt native packager](https://github.com/sbt/sbt-native-packager), which upon invoking 
`sbt stage` will generate an executable  `target/universal/stage/bin/wunder-reise`, needed to run the CLI

### Command line interface

The CLI is rather primitive and designed more to interact with other systems, as requirement says, 
then with humans. The executable accepts the initial positions of the trains as positional parameters:
```bash
target/universal/stage/bin/wunder-reise 3 8 11 2
```
will start the system with the trains at 3rd, 8th, 11th and 2nd terminals.

Pickups are read from stdin as space-separated pairs; pairs are separated by comma (extra whitespace is trimmed)
Each input line advances the system one step, empty line advances without requesting any new pickups: 
```
21 20, 2   0,12 2 
3 4, 5 6

15 1
```
will request `21->20`,`2->0`,`12->2` at the 1st sep; `3->4`,`5->6` - at the second, will simply advance at 3rd, and will request `31->1` at 4th 

Before each step it reports the system state to stdout. Train locations are followed by current direction of each train
(`<`left, `>`right, `-`idle ), so the output for the above  would look like:
```
  3 - |  8 - | 11 - |  2 -
  3 - |  9 > | 12 < |  1 <
  4 > | 10 > | 11 < |  0 >
  5 > | 11 > | 10 < |  0 -
  6 > | 12 > | 11 > |  0 -
  6 > | 12 > | 11 > |  0 -
  6 - | 13 > | 12 > |  0 -
  6 - | 14 > | 13 > |  0 -
  6 - | 15 > | 14 > |  0 -
  6 - | 16 > | 15 < |  0 -
  6 - | 17 > | 14 < |  0 -
  6 - | 18 > | 13 < |  0 -
  6 - | 19 > | 12 < |  0 -
  6 - | 20 > | 11 < |  0 -
  6 - | 21 < | 10 < |  0 -
  6 - | 20 > |  9 < |  0 -
  6 - | 20 - |  8 < |  0 -
  6 - | 20 - |  7 < |  0 -
  6 - | 20 - |  6 < |  0 -
  6 - | 20 - |  5 < |  0 -
  6 - | 20 - |  4 < |  0 -
  6 - | 20 - |  3 < |  0 -
  6 - | 20 - |  2 > |  0 -
  6 - | 20 - |  1 > |  0 -
```

After the input ends (EOF, or Ctrl-D in interactive mode), it continues to report the status till it reaches 
idle state 
