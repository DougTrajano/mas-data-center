// IT Manager

winning(none,0).

score("pink",0).
score("orange",0).
score("red",0).
score("green",0).
 
+dropped(T)[source(A)] : score(T,S) & winning(L,SL) & S+1>SL
   <- -score(T,S);
      +score(T,S+1);
      -dropped(T)[source(A)];
      -+winning(T,S+1);
      .print(T," is winning with ",S+1," fixed servers.");
      .broadcast(tell,winning(T,S+1)).
      
+dropped(T)[source(A)] : score(T,S) & winning(L,SL) & S+1=SL
   <- -score(T,S);
      +score(T,S+1);
      -dropped(T)[source(A)];
      -+winning(T,S+1);
      .print(T," and ", L, " tied with ", S+1," fixed servers.");
      .broadcast(tell,winning(T,S+1)).
      
+dropped(T)[source(A)] : score(T,S)
   <- -score(T,S);
      +score(T,S+1);
      -dropped(T)[source(A)];
      .print(A," delivered ",S+1," servers.").
      
{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }