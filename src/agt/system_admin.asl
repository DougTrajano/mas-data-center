// System Admin
{ include("$jacamoJar/templates/common-cartago.asl") }

/* beliefs */
last_dir(null). // the last agent movement
free.
score(0).
count(0).
team("system").

!start.
+!start 
   : true
    <-  ?team(T);
        ?pos(AgX, AgY);
        !setFreeCellsAround(AgX, AgY);
        
        .concat(T, "TeamMap", MapName);
        lookupArtifact(MapName, MapId);
        .print("MapName: ", MapName);        
        .

+free
   <-   askUnknownCell(RX, RY) [artifact_id(MapId)];
        if (RX == 100) {
         .print("There's a free cell.");
         -free;
        } else {
         !go_near(RX,RY);
        }
   .

+free <- .wait(100); -+free.

+near(X,Y) : free <- !ask_gold_cell.

+!setFreeCellsAround(X, Y)
    <- !setFreeCells(X, Y);
       !setFreeCells(X, Y+1);
       !setFreeCells(X, Y-1);
       !setFreeCells(X+1, Y);
       !setFreeCells(X+1, Y+1);
       !setFreeCells(X+1, Y-1);
       !setFreeCells(X-1, Y);
       !setFreeCells(X-1, Y+1);
       !setFreeCells(X-1, Y-1);
       .

+!setFreeCells(X, Y)
    <-  askCellValue(X, Y, V);
        if(V == "?") {
            setFreeCell(X, Y) [artifact_id(MapId)];
        }
        .

+!go_near(X,Y) : free
  <- -near(_,_);
     -last_dir(_);
     !near(X,Y).

+!near(X,Y) : (pos(AgX,AgY) & jia.neighbour(AgX,AgY,X,Y))
   <- .print("I am ", "(",AgX,",", AgY,")", " that is near to (",X,",", Y,")");
      +near(X,Y).

+!near(X,Y) : pos(AgX,AgY) & last_dir(skip)
   <- .print("I am ", "(",AgX,",", AgY,")", " and I cannot go to ' (",X,",", Y,")");
      +near(X,Y).

+!near(X,Y) : not near(X,Y)
   <- !next_step(X,Y);
      !near(X,Y).
+!near(X,Y) : true
   <- !near(X,Y).

// I already know my position
+!next_step(X,Y) : pos(AgX,AgY) 
   <- !setFreeCellsAround(AgX, AgY);
      jia.get_direction(AgX, AgY, X, Y, D);
      -+last_dir(D);
      D.
+!next_step(X,Y) : not pos(_,_) // I still do not know my position
   <- !next_step(X,Y).
-!next_step(X,Y) : true  // failure handling -> start again!
   <- -+last_dir(null);
      !next_step(X,Y).

+!pos(X,Y) : pos(X,Y)
   <- .print("I reached (",X,"x",Y,")!").
+!pos(X,Y) : not pos(X,Y)
   <- !next_step(X,Y);
      !pos(X,Y).

/* Plans to fix servers */
+cell(X,Y,issue) :  not carrying_part
    <- setIssueCell(X, Y) [artifact_id(MapId)];
       +issue(X,Y);
    .

+cell(X,Y,issue) 
    <- setIssueCell(X, Y) [artifact_id(MapId)];
       setIssueFound(X, Y) [artifact_id(MapId)];
    .

+cell(X,Y,obstacle) 
    <- setObstacleCell(X, Y) [artifact_id(MapId)];
.

+issue_found(X, Y) 
   : not issue(X,Y)
   <- +issue(X, Y);
   .

+part_picked(X, Y)
   : .desire(handle(issue(X,Y))) &
     not picked(issue(X,Y))
   <- -issue(X, Y);
      .drop_desire(handle(issue(X,Y)));
      !ask_issue_cell;
   .

// atomic: so as not to handle another event until handle 
// Repair is initialized
@pgold[atomic] 
+issue(X,Y)
  :  not carrying_part & free
  <- -free;
     .print("I saw a server error in ",issue(X,Y));
     !init_handle(issue(X,Y)).

@pissue2[atomic]
+issue(X,Y)
  :  not carrying_part & not free &
     .desire(handle(part(OldX,OldY))) & // I desire to handle another issue which
     pos(AgX,AgY) &
     jia.dist(X,   Y,   AgX,AgY,DNewG) &
     jia.dist(OldX,OldY,AgX,AgY,DOldG) &
     DNewG < DOldG // is farther than the one just perceived
  <- .drop_desire(handle(issue(OldX,OldY)));
     .print("Giving up the current server ",issue(OldX,OldY)," to go server in ",issue(X,Y));
     !init_handle(issue(X,Y)).

+!ensure(pick,_) : pos(X,Y) & issue(X,Y)
  <- pick;
     +picked(issue(X,Y));
     setFreeCell(X, Y) [artifact_id(MapId)];
     setIssuePicked(X, Y) [artifact_id(MapId)];
     ?carrying_issue;
     -issue(X,Y).

@pih1[atomic]
+!init_handle(Issue)
  :  .desire(near(_,_))
  <- .print("Leave desires and intentions to seek ",Issue);
     .drop_desire(near(_,_));
     !init_handle(Issue).
     
@pih2[atomic]
+!init_handle(Issue)
  :  pos(X,Y)
  <- .print("I'm going to ",Issue);
     !!handle(Issue). // must use !! to perform "handle" as not atomic

+!handle(issue(X,Y))
  :  not free & team(T)
  <- .print("I'm fixing ",issue(X,Y));
     !pos(X,Y);
     !ensure(pick,issue(X,Y));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     .print("Repair finished ",issue(X,Y));
     ?score(S);
     -+score(S+1);
     .send(leader,tell,dropped(T));
     !!ask_issue_cell.

// if ensure(pick/drop) failed, pursue another issue
-!handle(I) : I
  <- .print("Server repair failed ", I);
     .abolish(I); // ignore source
     !!ask_issue_cell.
-!handle(I) : true
  <- .print("Failed to repair ", I, ", it's not on BB.");
     !!ask_issue_cell.

+!ensure(pick,_) : pos(X,Y) & issue(X,Y)
  <- pick;
     ?carrying_part;
     -issue(X,Y).

// fail if no issue there or not carrying_part after repair!
// handle(I) will "catch" this failure.

+!ensure(drop, _) : carrying_part & pos(X,Y) & depot(_,DX,DY)
  <- drop.

+!ask_issue_cell : pos(AgX, AgY)
    <- askCloserIssueCell(AgX, AgY, XG, YG);
       //.print("  [", XG, ", ", YG,"]");
       if (XG \== 100){
         setAgentIssueCell(XG, YG) [artifact_id(MapId)];
         !!handle(issue(XG, YG));
       } else {
         -+free;
       }
       . 

+winning(A,S)[source(leader)] : .my_name(A)
   <-  -winning(A,S);
       .print("I'm the best!").

+winning(A,S)[source(leader)] : true
   <-  -winning(A,S).

+end_of_simulation(S,_) : true
  <- .drop_all_desires;
     .abolish(issue(_,_));
     .abolish(picked(_));
     -+free;
     .print("- END ", S, " -").