// Orange Technician
team("orange").

!start.
+!start : true
    <- ?team(T);
       .concat(T, "TeamMap", MapName);
        lookupArtifact(MapName, MapId);
        .print("MapName: ", MapName);
        .

{ include("technician.asl") }