// Technician
{ include("$jacamoJar/templates/common-cartago.asl") }

/* Beliefs */
last_dir(null). // the last agent movement
free.
score(0).
count(0).

// Rules
// this agent program doesn't have any rules */

// When the agents are free, they are wandering around
// This is applied with a plan that is actioned when the agents become free
// (which happens initially because of the "free" belief above,
// but it can also happen during the agent's execution).

// The plan takes two random numbers within the scope of the grid
// (using jia.random function) and then calls the go_near subgoal.

// Once the agent is close to the desired position, be free,
// he deletes and adds the free atom to its belief base, which will trigger the plan to go to a random location again.

+free : gsize(_,W,H) & jia.random(RX,W-1) & jia.random(RY,H-1)
   <- .print("I am near to [",RX,",",RY,"]");  
   !go_near(RX,RY).
+free  // gsize is unknown yet
   <- .wait(100); -+free.

// When the agent comes to believe it's near to a position,
// and it's still free, it updates the "free" atom so it can be trigger the plan to go to a random location again.
+near(X,Y) : free 
	<- .wait(100);
	-+free.

// I'm near some place if I'm near it.
+!near(X,Y) 
	: (pos(AgX,AgY) 
	& jia.neighbour(AgX,AgY,X,Y))
   <- .print ("I am ", "[", AgX, ",", AgY, "]", " that is near to [", X, ",", Y , "]"); 
      +near(X,Y).

// I'm near somewhere if the last action was jumping to that place.
// (significando que não há caminhos para lá)
+!near(X,Y) 
	: pos(AgX,AgY) 
	& last_dir(skip)
   <- .print ("I am ", "[", AgX, ",", AgY, "]", " and I cannot go to '[", X, ",", Y, "]"); 
      +near(X,Y).

+!near(X,Y) 
	: not near(X,Y)
   <- !next_step(X,Y);
      !near(X,Y).
+!near(X,Y) 
	: true
   <- !near(X,Y).

// The following plans encode how an agent should get close to a given location (X, Y).
// As the location may not be reachable, the plans are successful
// if the agent is close to the location, given by the action internal jia.neighbour,
// or if the last action was to jump, which happens when the destination is not reachable,
// provided by the plan next_step, which is called by the action internal jia.get_direction.
// These plans are used only in the exploration of the network, since it is not very important
// to reach the exact location.
+!go_near(X,Y) : free
  <- -near(_,_);
     -last_dir(_);
     !near(X,Y).

// I am near to some location if I am near it or the last action was skip (meaning that there are no paths to there)
+!near(X,Y) : (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y)) | last_dir(skip)
   <- +near(X,Y).
+!near(X,Y) : not near(X,Y)
   <- !next_step(X,Y);
      !near(X,Y).
+!near(X,Y) : true
   <- !near(X,Y).

// The following plans executes a step towards X, Y.
// They are used by the plans go_near up and near down.
// It uses the internal action jia.get_direction that implement a search algorithm.
+!next_step(X,Y) : pos(AgX,AgY) // I already know my position
   <- jia.get_direction(AgX, AgY, X, Y, D);
      -+last_dir(D);
      D.
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true // failure handling -> start again!
   <- -+last_dir(null);
      !next_step(X,Y).

// The following plans encode how an agent should get close to a specific location (X, Y).
// Unlike the plans for getting close to a location, this one assumes that the location is reachable.
// If the location is not reachable, it will loop forever.
+!pos(X,Y) : pos(X,Y)
   <- .print("I reached (",X,"x",Y,")!").
+!pos(X,Y) : not pos(X,Y)
   <- !next_step(X,Y);
      !pos(X,Y). 

// Plans to find issues

// The following plan encodes how an agent should handle a newly found issue
// when it is not carrying part and is free.
// The first step changes the belief so that the agent no longer believes he is free.
// Then, it adds the belief that there is gold in the position X, Y and
// prints a message. Finally, it calls a plan to handle that issue.

// The perceived issue is included as self-belief
// (to not be removed once it is no longer seen)
+cell(X,Y,issue) <- +issue(X,Y).

// atomic: to avoid handle other event until the issue is handled
@pissue[atomic] 
+issue(X,Y)
  :  not carrying_part & free
  <- -free;
     .print("I saw server in: ",issue(X,Y));
     !init_handle(issue(X,Y)).

// new plans for event +issue(_,_) */
@pcell[atomic] // atomic: to avoid handle other event until the issue is handled
+issue(X,Y)
   :  not carrying_part & free
   <- -free;
      .print("I saw a server error in: ",issue(X,Y));
      !init_handle(issue(X,Y)).

// If the agent see gold and isn't free, but he is not carrying gold yet,
// abort the identifier (gold) and pick the one that is closer to him.
@pcell2[atomic]
+issue(X,Y)
  :  not carrying_part & not free &
     .desire(handle(issue(OldX,OldY))) & // I desire to handle another issue which
     pos(AgX,AgY) &
     jia.dist(X,   Y,   AgX,AgY,DNewG) &
     jia.dist(OldX,OldY,AgX,AgY,DOldG) &
     DNewG < DOldG // is farther than the one just perceived
  <- .drop_desire(handle(issue(OldX,OldY)));
     .print("Giving up the current server ",issue(OldX,OldY)," to go for ",issue(X,Y)," that is close to me!");
     !init_handle(issue(X,Y)).

// The following plans encodes how an agent should handle an issue.

// Eliminate the desire to be near any location
@pih1[atomic]
+!init_handle(Issue)
  :  .desire(near(_,_))
  <- .print("Leave desires and intentions to seek ",Issue);
     .drop_desire(near(_,_));
     !init_handle(Issue).

// Call the goal to handle the issue
@pih2[atomic]
+!init_handle(Issue)
  :  pos(X,Y)
  <- .print("I'm going to ",Issue);
     !!handle(Issue).

// Call the goal to handle the issue
+!handle(issue(X,Y))
  :  not free
  <- .print("I'm fixing ",issue(X,Y));
     !pos(X,Y);
     !ensure(pick,issue(X,Y));
     ?depot(_,DX,DY);
     !pos(DX,DY); // !pos(0,0);
     !ensure(drop, 0);
     .print("Fix finished ",issue(X,Y));
     ?score(S);
     -+score(S+1);
     .send(leader,tell,dropped);
     !!choose_issue.

// If ensure(pick/drop) fails, search for another issue
-!handle(I) : I
  <- .print("Failed in the server repair ",I);
     .abolish(I); // ignore source
     !!choose_issue.
-!handle(I) : true
  <- .print("Failed to repair ",I,", it's not on BB.");
     !!choose_issue.

/* The following plans encodes the actions of picking and dropping gold. */
+!ensure(pick,_) : pos(X,Y) & issue(X,Y)
  <- pick;
     ?carrying_part;
     -issue(X,Y).
// Fail if there's no gold there or don't carry part after picking
// handle(G) will catch this failure

+!ensure(drop, _) : carrying_part & pos(X,Y) & depot(_,X,Y)
  <- drop.

// The next plans encode how an agent should choose the next gold to follow (the closest one)
+!choose_issue
  :  not issue(_,_)
  <- -+free.

// Issue finished, but there's other issues find the nearest one from the list of issues
+!choose_issue
  :  issue(_,_)
  <- .findall(issue(X,Y),issue(X,Y),LG);
     !calc_issue_distance(LG,LD);
     .length(LD,LLD); LLD > 0;
     .print("Server distance: ",LD,LLD);
     .min(LD,d(_,NewG));
     .print("Next server is ",NewG);
     !!handle(NewG).
-!choose_issue <- -+free.

+!calc_issue_distance([],[]).
+!calc_issue_distance([issue(GX,GY)|R],[d(D,issue(GX,GY))|RD])
  :  pos(IX,IY)
  <- jia.dist(IX,IY,GX,GY,D);
     !calc_issue_distance(R,RD).
+!calc_issue_distance([_|R],RD)
  <- !calc_issue_distance(R,RD).
  
// Winner
+winning(A,S)[source(leader)] : .my_name(A)
   <-  -winning(A,S);
       .print("I'm the best!").

+winning(A,S)[source(leader)] : true
   <-  -winning(A,S).

// End simulation
+end_of_simulation(S,_) : true
  <- .drop_all_desires;
     .abolish(issue(_,_));
     .abolish(picked(_));
     -+free;
     .print("- END ",S," -").