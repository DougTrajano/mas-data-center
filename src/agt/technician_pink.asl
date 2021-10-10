// Pink Technician
{ include("$jacamoJar/templates/common-cartago.asl") }

/* beliefs */
last_dir(null). // the last technician movement
free.
score(0).
count(0).
team("pink").

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
+cell(X,Y,gold) :  not carrying_gold
    <- setGoldCell(X, Y) [artifact_id(MapId)];
       +gold(X,Y);
    .

+cell(X,Y,gold) 
    <- setGoldCell(X, Y) [artifact_id(MapId)];
       setGoldFound(X, Y) [artifact_id(MapId)];
    .

+cell(X,Y,obstacle) 
    <- setObstacleCell(X, Y) [artifact_id(MapId)];
.

+gold_found(X, Y) 
   : not gold(X,Y)
   <- +gold(X, Y);
   .

+gold_picked(X, Y)
   : .desire(handle(gold(X,Y))) &
     not picked(gold(X,Y))
   <- -gold(X, Y);
      .drop_desire(handle(gold(X,Y)));
      !ask_gold_cell;
   .

// atomic: so as not to handle another event until handle 
// Repair is initialized
@pgold[atomic] 
+gold(X,Y)
  :  not carrying_gold & free
  <- -free;
     .print("I saw server in ",gold(X,Y));
     !init_handle(gold(X,Y)).

@pgold2[atomic]
+gold(X,Y)
  :  not carrying_gold & not free &
     .desire(handle(gold(OldX,OldY))) & // I desire to handle another gold which
     pos(AgX,AgY) &
     jia.dist(X,   Y,   AgX,AgY,DNewG) &
     jia.dist(OldX,OldY,AgX,AgY,DOldG) &
     DNewG < DOldG // is farther than the one just perceived
  <- .drop_desire(handle(gold(OldX,OldY)));
     .print("Giving up the current server ",gold(OldX,OldY)," to go server in ",gold(X,Y));
     !init_handle(gold(X,Y)).

+!ensure(pick,_) : pos(X,Y) & gold(X,Y)
  <- pick;
     +picked(gold(X,Y));
     setFreeCell(X, Y) [artifact_id(MapId)];
     setGoldPicked(X, Y) [artifact_id(MapId)];
     ?carrying_gold;
     -gold(X,Y).

@pih1[atomic]
+!init_handle(Gold)
  :  .desire(near(_,_))
  <- .print("Leave desires and intentions to seek ",Gold);
     .drop_desire(near(_,_));
     !init_handle(Gold).
     
@pih2[atomic]
+!init_handle(Gold)
  :  pos(X,Y)
  <- .print("I'm going to ",Gold);
     !!handle(Gold). // must use !! to perform "handle" as not atomic

+!handle(gold(X,Y))
  :  not free & team(T)
  <- .print("I'm fixing ",gold(X,Y));
     !pos(X,Y);
     !ensure(pick,gold(X,Y));
     ?depot(_,DX,DY);
     !pos(DX,DY);
     !ensure(drop, 0);
     .print("Repair finished ",gold(X,Y));
     ?score(S);
     -+score(S+1);
     .send(leader,tell,dropped(T));
     !!ask_gold_cell.

// if ensure(pick/drop) failed, pursue another gold
-!handle(G) : G
  <- .print("Server repair failed ", G);
     .abolish(G); // ignore source
     !!ask_gold_cell.
-!handle(G) : true
  <- .print("Failed to repair ", G, ", it's not on BB.");
     !!ask_gold_cell.

+!ensure(pick,_) : pos(X,Y) & gold(X,Y)
  <- pick;
     ?carrying_gold;
     -gold(X,Y).

// fail if no gold there or not carrying_gold after pick!
// handle(G) will "catch" this failure.

+!ensure(drop, _) : carrying_gold & pos(X,Y) & depot(_,DX,DY)
  <- drop.

+!ask_gold_cell : pos(AgX, AgY)
    <- askCloserGoldCell(AgX, AgY, XG, YG);
       //.print("  [", XG, ", ", YG,"]");
       if (XG \== 100){
         setAgentGoldCell(XG, YG) [artifact_id(MapId)];
         !!handle(gold(XG, YG));
       } else {
         -+free;
       }
       . 

+winning(A,S)[source(leader)] : .my_name(A)
   <-  -winning(A,S);
       .print("I'm the best!!!").

+winning(A,S)[source(leader)] : true
   <-  -winning(A,S).

+end_of_simulation(S,_) : true
  <- .drop_all_desires;
     .abolish(gold(_,_));
     .abolish(picked(_));
     -+free;
     .print("- END ", S, " -").