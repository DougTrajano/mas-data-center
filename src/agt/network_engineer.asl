// Network Engineer
team("network").

!start.
+!start : true
    <- ?team(T);
       .concat(T, "TeamMap", MapName);
        lookupArtifact(MapName, MapId);
        .print("MapName: ", MapName);
        .

{ include("base.asl") }