// Facilities Technician
team("facilities").

!start.
+!start : true
    <- ?team(T);
       .concat(T, "TeamMap", MapName);      
        lookupArtifact(MapName, MapId);
        .print("MapName: ", MapName);
        focus(MapId);
        .

{ include("base.asl") }