// Hardware Engineer
team("hardware").

!start.
+!start : true
    <- .wait(1);
       ?team(T);
        .concat(T, "TeamMap", MapName);
        lookupArtifact(MapName, MapId);
        .print("MapName: ", MapName);  
        .

{ include("base.asl") }