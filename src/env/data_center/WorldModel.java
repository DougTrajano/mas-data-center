package data_center;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import data_center.DataCenterPlanet.Move;

public class WorldModel extends GridWorldModel {

    public static final int   ISSUE = 16;
    public static final int   DEPOT = 32;

    Location                  depot;
    Set<Integer>              agWithPart; // which agent is carrying part
    int                       issuesInDepot   = 0;
    int                       initialNbIssues = 0;

    private Logger            logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

    private String            id = "WorldModel";

    // singleton pattern
    protected static WorldModel model = null;

    synchronized public static WorldModel create(int w, int h, int nbAgs) {
        if (model == null) {
            model = new WorldModel(w, h, nbAgs);
        }
        return model;
    }

    public static WorldModel get() {
        return model;
    }

    public static void destroy() {
        model = null;
    }

    private WorldModel(int w, int h, int nbAgs) {
        super(w, h, nbAgs);
        agWithPart = new HashSet<Integer>();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String toString() {
        return id;
    }

    public Location getDepot() {
        return depot;
    }

    public int getIssuesInDepot() {
        return issuesInDepot;
    }

    public boolean isAllIssuesCollected() {
        return issuesInDepot == initialNbIssues;
    }

    public void setInitialNbIssues(int i) {
        initialNbIssues = i;
    }

    public int getInitialNbIssues() {
        return initialNbIssues;
    }

    public boolean isCarryingPart(int ag) {
        return agWithPart.contains(ag);
    }

    public void setDepot(int x, int y) {
        depot = new Location(x, y);
        data[x][y] = DEPOT;
    }

    public void setAgCarryingPart(int ag) {
        agWithPart.add(ag);
    }
    public void setAgNotCarryingPart(int ag) {
        agWithPart.remove(ag);
    }

    /** Actions **/
    public Location getFreePos() {
        for (int i=0; i<(getWidth() * getHeight() * 5); i++) {
          int x = random.nextInt(getWidth());
          int y = random.nextInt(getHeight());
          Location l = new Location(x, y);
          if (isFree(l)) {
            return l;
          }
        }
        return null;
      }

    boolean move(Move dir, int ag) throws Exception {
        Location l = getAgPos(ag);
        switch (dir) {
        case UP:
            if (isFree(l.x, l.y - 1)) {
                setAgPos(ag, l.x, l.y - 1);
            }
            break;
        case DOWN:
            if (isFree(l.x, l.y + 1)) {
                setAgPos(ag, l.x, l.y + 1);
            }
            break;
        case RIGHT:
            if (isFree(l.x + 1, l.y)) {
                setAgPos(ag, l.x + 1, l.y);
            }
            break;
        case LEFT:
            if (isFree(l.x - 1, l.y)) {
                setAgPos(ag, l.x - 1, l.y);
            }
            break;
        }
        return true;
    }

    boolean pick(int ag) {
        Location l = getAgPos(ag);
        if (hasObject(WorldModel.ISSUE, l.x, l.y)) {
            if (!isCarryingPart(ag)) {
                remove(WorldModel.ISSUE, l.x, l.y);
                add(WorldModel.OBSTACLE, l.x, l.y);
                setAgCarryingPart(ag);
                return true;
            } else {
                logger.warning("Agent " + (ag + 1) + " is trying to fix the server error, but he already has a part.");
            }
        } else {
            logger.warning("Agent " + (ag + 1) + " is trying to fix the server error, but there's no issue in " + l.x + "x" + l.y + ".");
        }
        return false;
    }

    boolean drop(int ag) {
        Location l = getAgPos(ag);
        if (isCarryingPart(ag)) {
            if (l.equals(getDepot())) {
                issuesInDepot++;
                logger.info("Agent " + (ag + 1) + " is carrying part to the depot.");
            } else {
                add(WorldModel.ISSUE, l.x, l.y);
            }
            setAgNotCarryingPart(ag);
            return true;
        }
        return false;
    }

    /* Data Center (35x35) with 40 racks */
    static WorldModel world1() throws Exception {
        WorldModel model = WorldModel.create(35, 35, 4);
        model.setId("Scenario 1");
        model.setDepot(16, 16);
        model.setAgPos(0, 0, 0);
        model.setAgPos(1, 30, 0);
        model.setAgPos(2, 3, 33);
        model.setAgPos(3, 27, 26);

        // rack 1
        model.add(WorldModel.OBSTACLE, 1, 1);
        model.add(WorldModel.ISSUE, 1, 2);
        model.add(WorldModel.OBSTACLE, 1, 3);
        model.add(WorldModel.OBSTACLE, 1, 4);
        model.add(WorldModel.OBSTACLE, 1, 5);
        model.add(WorldModel.OBSTACLE, 1, 6);
        model.add(WorldModel.OBSTACLE, 2, 1);
        model.add(WorldModel.ISSUE, 2, 2);
        model.add(WorldModel.OBSTACLE, 2, 3);
        model.add(WorldModel.OBSTACLE, 2, 4);
        model.add(WorldModel.OBSTACLE, 2, 5);
        model.add(WorldModel.OBSTACLE, 2, 6);

        // rack 2
        model.add(WorldModel.OBSTACLE, 5, 1);
        model.add(WorldModel.OBSTACLE, 5, 2);
        model.add(WorldModel.OBSTACLE, 5, 3);
        model.add(WorldModel.OBSTACLE, 5, 4);
        model.add(WorldModel.OBSTACLE, 5, 5);
        model.add(WorldModel.OBSTACLE, 5, 6);
        model.add(WorldModel.OBSTACLE, 6, 1);
        model.add(WorldModel.OBSTACLE, 6, 2);
        model.add(WorldModel.OBSTACLE, 6, 3);
        model.add(WorldModel.OBSTACLE, 6, 4);
        model.add(WorldModel.OBSTACLE, 6, 5);
        model.add(WorldModel.OBSTACLE, 6, 6);

        // rack 3
        model.add(WorldModel.OBSTACLE, 9, 1);
        model.add(WorldModel.OBSTACLE, 9, 2);
        model.add(WorldModel.OBSTACLE, 9, 3);
        model.add(WorldModel.OBSTACLE, 9, 4);
        model.add(WorldModel.OBSTACLE, 9, 5);
        model.add(WorldModel.ISSUE, 9, 6);
        model.add(WorldModel.OBSTACLE, 10, 1);
        model.add(WorldModel.OBSTACLE, 10, 2);
        model.add(WorldModel.OBSTACLE, 10, 3);
        model.add(WorldModel.OBSTACLE, 10, 4);
        model.add(WorldModel.OBSTACLE, 10, 5);
        model.add(WorldModel.OBSTACLE, 10, 6);

        // rack 4
        model.add(WorldModel.OBSTACLE, 13, 1);
        model.add(WorldModel.ISSUE, 13, 2);
        model.add(WorldModel.OBSTACLE, 13, 3);
        model.add(WorldModel.OBSTACLE, 13, 4);
        model.add(WorldModel.OBSTACLE, 13, 5);
        model.add(WorldModel.OBSTACLE, 13, 6);
        model.add(WorldModel.OBSTACLE, 14, 1);
        model.add(WorldModel.OBSTACLE, 14, 2);
        model.add(WorldModel.OBSTACLE, 14, 3);
        model.add(WorldModel.OBSTACLE, 14, 4);
        model.add(WorldModel.OBSTACLE, 14, 5);
        model.add(WorldModel.OBSTACLE, 14, 6);

        // rack 5
        model.add(WorldModel.ISSUE, 16, 1);
        model.add(WorldModel.OBSTACLE, 16, 2);
        model.add(WorldModel.OBSTACLE, 17, 1);
        model.add(WorldModel.OBSTACLE, 17, 2);
        model.add(WorldModel.OBSTACLE, 18, 1);
        model.add(WorldModel.OBSTACLE, 18, 2);

        // rack 6
        model.add(WorldModel.OBSTACLE, 16, 5);
        model.add(WorldModel.OBSTACLE, 16, 6);
        model.add(WorldModel.OBSTACLE, 17, 5);
        model.add(WorldModel.OBSTACLE, 17, 6);
        model.add(WorldModel.OBSTACLE, 18, 5);
        model.add(WorldModel.ISSUE, 18, 6);

        // rack 7
        model.add(WorldModel.OBSTACLE, 20, 1);
        model.add(WorldModel.OBSTACLE, 20, 2);
        model.add(WorldModel.OBSTACLE, 20, 3);
        model.add(WorldModel.OBSTACLE, 20, 4);
        model.add(WorldModel.OBSTACLE, 20, 5);
        model.add(WorldModel.OBSTACLE, 20, 6);
        model.add(WorldModel.OBSTACLE, 21, 1);
        model.add(WorldModel.ISSUE, 21, 2);
        model.add(WorldModel.OBSTACLE, 21, 3);
        model.add(WorldModel.OBSTACLE, 21, 4);
        model.add(WorldModel.OBSTACLE, 21, 5);
        model.add(WorldModel.OBSTACLE, 21, 6);

        // rack 8
        model.add(WorldModel.OBSTACLE, 24, 1);
        model.add(WorldModel.OBSTACLE, 24, 2);
        model.add(WorldModel.OBSTACLE, 24, 3);
        model.add(WorldModel.OBSTACLE, 24, 4);
        model.add(WorldModel.OBSTACLE, 24, 5);
        model.add(WorldModel.OBSTACLE, 24, 6);
        model.add(WorldModel.OBSTACLE, 25, 1);
        model.add(WorldModel.OBSTACLE, 25, 2);
        model.add(WorldModel.OBSTACLE, 25, 3);
        model.add(WorldModel.OBSTACLE, 25, 4);
        model.add(WorldModel.OBSTACLE, 25, 5);
        model.add(WorldModel.OBSTACLE, 25, 6);

        // rack 9
        model.add(WorldModel.OBSTACLE, 28, 1);
        model.add(WorldModel.OBSTACLE, 28, 2);
        model.add(WorldModel.OBSTACLE, 28, 3);
        model.add(WorldModel.OBSTACLE, 28, 4);
        model.add(WorldModel.OBSTACLE, 28, 5);
        model.add(WorldModel.OBSTACLE, 28, 6);
        model.add(WorldModel.OBSTACLE, 29, 1);
        model.add(WorldModel.OBSTACLE, 29, 2);
        model.add(WorldModel.OBSTACLE, 29, 3);
        model.add(WorldModel.OBSTACLE, 29, 4);
        model.add(WorldModel.OBSTACLE, 29, 5);
        model.add(WorldModel.OBSTACLE, 29, 6);

        // rack 10
        model.add(WorldModel.OBSTACLE, 1, 9);
        model.add(WorldModel.OBSTACLE, 1, 10);
        model.add(WorldModel.OBSTACLE, 1, 11);
        model.add(WorldModel.OBSTACLE, 1, 12);
        model.add(WorldModel.OBSTACLE, 1, 13);
        model.add(WorldModel.OBSTACLE, 1, 14);
        model.add(WorldModel.OBSTACLE, 2, 9);
        model.add(WorldModel.OBSTACLE, 2, 10);
        model.add(WorldModel.OBSTACLE, 2, 11);
        model.add(WorldModel.OBSTACLE, 2, 12);
        model.add(WorldModel.OBSTACLE, 2, 13);
        model.add(WorldModel.OBSTACLE, 2, 14);

        // rack 11
        model.add(WorldModel.OBSTACLE, 5, 9);
        model.add(WorldModel.OBSTACLE, 5, 10);
        model.add(WorldModel.OBSTACLE, 5, 11);
        model.add(WorldModel.OBSTACLE, 5, 12);
        model.add(WorldModel.OBSTACLE, 5, 13);
        model.add(WorldModel.OBSTACLE, 5, 14);
        model.add(WorldModel.OBSTACLE, 6, 9);
        model.add(WorldModel.OBSTACLE, 6, 10);
        model.add(WorldModel.OBSTACLE, 6, 11);
        model.add(WorldModel.OBSTACLE, 6, 12);
        model.add(WorldModel.OBSTACLE, 6, 13);
        model.add(WorldModel.OBSTACLE, 6, 14);

        // rack 12
        model.add(WorldModel.OBSTACLE, 9, 9);
        model.add(WorldModel.OBSTACLE, 9, 10);
        model.add(WorldModel.OBSTACLE, 9, 11);
        model.add(WorldModel.OBSTACLE, 9, 12);
        model.add(WorldModel.OBSTACLE, 9, 13);
        model.add(WorldModel.OBSTACLE, 9, 14);
        model.add(WorldModel.OBSTACLE, 10, 9);
        model.add(WorldModel.OBSTACLE, 10, 10);
        model.add(WorldModel.OBSTACLE, 10, 11);
        model.add(WorldModel.OBSTACLE, 10, 12);
        model.add(WorldModel.OBSTACLE, 10, 13);
        model.add(WorldModel.OBSTACLE, 10, 14);

        // rack 13
        model.add(WorldModel.OBSTACLE, 13, 9);
        model.add(WorldModel.OBSTACLE, 13, 10);
        model.add(WorldModel.OBSTACLE, 13, 11);
        model.add(WorldModel.OBSTACLE, 13, 12);
        model.add(WorldModel.OBSTACLE, 13, 13);
        model.add(WorldModel.OBSTACLE, 13, 14);
        model.add(WorldModel.OBSTACLE, 14, 9);
        model.add(WorldModel.OBSTACLE, 14, 10);
        model.add(WorldModel.OBSTACLE, 14, 11);
        model.add(WorldModel.OBSTACLE, 14, 12);
        model.add(WorldModel.OBSTACLE, 14, 13);
        model.add(WorldModel.OBSTACLE, 14, 14);

        // rack 14
        model.add(WorldModel.OBSTACLE, 16, 9);
        model.add(WorldModel.OBSTACLE, 16, 10);
        model.add(WorldModel.OBSTACLE, 17, 9);
        model.add(WorldModel.OBSTACLE, 17, 10);
        model.add(WorldModel.OBSTACLE, 18, 9);
        model.add(WorldModel.OBSTACLE, 18, 10);

        // rack 15
        model.add(WorldModel.OBSTACLE, 16, 13);
        model.add(WorldModel.ISSUE, 16, 14);
        model.add(WorldModel.OBSTACLE, 17, 13);
        model.add(WorldModel.OBSTACLE, 17, 14);
        model.add(WorldModel.OBSTACLE, 18, 13);
        model.add(WorldModel.OBSTACLE, 18, 14);

        // rack 16
        model.add(WorldModel.OBSTACLE, 20, 9);
        model.add(WorldModel.OBSTACLE, 20, 10);
        model.add(WorldModel.OBSTACLE, 20, 11);
        model.add(WorldModel.OBSTACLE, 20, 12);
        model.add(WorldModel.ISSUE, 20, 13);
        model.add(WorldModel.OBSTACLE, 20, 14);
        model.add(WorldModel.OBSTACLE, 21, 9);
        model.add(WorldModel.OBSTACLE, 21, 10);
        model.add(WorldModel.OBSTACLE, 21, 11);
        model.add(WorldModel.OBSTACLE, 21, 12);
        model.add(WorldModel.OBSTACLE, 21, 13);
        model.add(WorldModel.OBSTACLE, 21, 14);

        // rack 17
        model.add(WorldModel.OBSTACLE, 24, 9);
        model.add(WorldModel.OBSTACLE, 24, 10);
        model.add(WorldModel.OBSTACLE, 24, 11);
        model.add(WorldModel.OBSTACLE, 24, 12);
        model.add(WorldModel.ISSUE, 24, 13);
        model.add(WorldModel.OBSTACLE, 24, 14);
        model.add(WorldModel.OBSTACLE, 25, 9);
        model.add(WorldModel.OBSTACLE, 25, 10);
        model.add(WorldModel.OBSTACLE, 25, 11);
        model.add(WorldModel.OBSTACLE, 25, 12);
        model.add(WorldModel.OBSTACLE, 25, 13);
        model.add(WorldModel.OBSTACLE, 25, 14);

        // rack 18
        model.add(WorldModel.OBSTACLE, 28, 9);
        model.add(WorldModel.OBSTACLE, 28, 10);
        model.add(WorldModel.OBSTACLE, 28, 11);
        model.add(WorldModel.OBSTACLE, 28, 12);
        model.add(WorldModel.OBSTACLE, 28, 13);
        model.add(WorldModel.OBSTACLE, 28, 14);
        model.add(WorldModel.OBSTACLE, 29, 9);
        model.add(WorldModel.OBSTACLE, 29, 10);
        model.add(WorldModel.OBSTACLE, 29, 11);
        model.add(WorldModel.OBSTACLE, 29, 12);
        model.add(WorldModel.OBSTACLE, 29, 13);
        model.add(WorldModel.OBSTACLE, 29, 14);

        // rack 19
        model.add(WorldModel.OBSTACLE, 32, 1);
        model.add(WorldModel.ISSUE, 32, 2);
        model.add(WorldModel.OBSTACLE, 32, 3);
        model.add(WorldModel.OBSTACLE, 32, 4);
        model.add(WorldModel.ISSUE, 32, 5);
        model.add(WorldModel.OBSTACLE, 32, 6);
        model.add(WorldModel.OBSTACLE, 33, 1);
        model.add(WorldModel.ISSUE, 33, 2);
        model.add(WorldModel.OBSTACLE, 33, 3);
        model.add(WorldModel.OBSTACLE, 33, 4);
        model.add(WorldModel.OBSTACLE, 33, 5);
        model.add(WorldModel.OBSTACLE, 33, 6);


        // rack 20
        model.add(WorldModel.OBSTACLE, 32, 9);
        model.add(WorldModel.OBSTACLE, 32, 10);
        model.add(WorldModel.ISSUE, 32, 11);
        model.add(WorldModel.OBSTACLE, 32, 12);
        model.add(WorldModel.OBSTACLE, 32, 13);
        model.add(WorldModel.OBSTACLE, 32, 14);
        model.add(WorldModel.OBSTACLE, 33, 9);
        model.add(WorldModel.OBSTACLE, 33, 10);
        model.add(WorldModel.ISSUE, 33, 11);
        model.add(WorldModel.OBSTACLE, 33, 12);
        model.add(WorldModel.OBSTACLE, 33, 13);
        model.add(WorldModel.OBSTACLE, 33, 14);

        // rack 21
        model.add(WorldModel.OBSTACLE, 1, 20);
        model.add(WorldModel.OBSTACLE, 1, 21);
        model.add(WorldModel.OBSTACLE, 1, 22);
        model.add(WorldModel.OBSTACLE, 1, 23);
        model.add(WorldModel.OBSTACLE, 1, 24);
        model.add(WorldModel.ISSUE, 1, 25);
        model.add(WorldModel.OBSTACLE, 2, 20);
        model.add(WorldModel.OBSTACLE, 2, 21);
        model.add(WorldModel.OBSTACLE, 2, 22);
        model.add(WorldModel.OBSTACLE, 2, 23);
        model.add(WorldModel.OBSTACLE, 2, 24);
        model.add(WorldModel.OBSTACLE, 2, 25);

        // rack 22
        model.add(WorldModel.OBSTACLE, 5, 20);
        model.add(WorldModel.OBSTACLE, 5, 21);
        model.add(WorldModel.OBSTACLE, 5, 22);
        model.add(WorldModel.OBSTACLE, 5, 23);
        model.add(WorldModel.OBSTACLE, 5, 24);
        model.add(WorldModel.OBSTACLE, 5, 25);
        model.add(WorldModel.OBSTACLE, 6, 20);
        model.add(WorldModel.OBSTACLE, 6, 21);
        model.add(WorldModel.OBSTACLE, 6, 22);
        model.add(WorldModel.OBSTACLE, 6, 23);
        model.add(WorldModel.OBSTACLE, 6, 24);
        model.add(WorldModel.OBSTACLE, 6, 25);

        // rack 23
        model.add(WorldModel.OBSTACLE, 9, 20);
        model.add(WorldModel.OBSTACLE, 9, 21);
        model.add(WorldModel.OBSTACLE, 9, 22);
        model.add(WorldModel.OBSTACLE, 9, 23);
        model.add(WorldModel.OBSTACLE, 9, 24);
        model.add(WorldModel.OBSTACLE, 9, 25);
        model.add(WorldModel.OBSTACLE, 10, 20);
        model.add(WorldModel.OBSTACLE, 10, 21);
        model.add(WorldModel.OBSTACLE, 10, 22);
        model.add(WorldModel.OBSTACLE, 10, 23);
        model.add(WorldModel.OBSTACLE, 10, 24);
        model.add(WorldModel.OBSTACLE, 10, 25);

        // rack 24
        model.add(WorldModel.OBSTACLE, 13, 20);
        model.add(WorldModel.OBSTACLE, 13, 21);
        model.add(WorldModel.OBSTACLE, 13, 22);
        model.add(WorldModel.OBSTACLE, 13, 23);
        model.add(WorldModel.OBSTACLE, 13, 24);
        model.add(WorldModel.OBSTACLE, 13, 25);
        model.add(WorldModel.OBSTACLE, 14, 20);
        model.add(WorldModel.OBSTACLE, 14, 21);
        model.add(WorldModel.OBSTACLE, 14, 22);
        model.add(WorldModel.OBSTACLE, 14, 23);
        model.add(WorldModel.OBSTACLE, 14, 24);
        model.add(WorldModel.OBSTACLE, 14, 25);

        // rack 25
        model.add(WorldModel.OBSTACLE, 16, 20);
        model.add(WorldModel.OBSTACLE, 16, 21);
        model.add(WorldModel.ISSUE, 17, 20);
        model.add(WorldModel.OBSTACLE, 17, 21);
        model.add(WorldModel.OBSTACLE, 18, 20);
        model.add(WorldModel.OBSTACLE, 18, 21);

        // rack 26
        model.add(WorldModel.OBSTACLE, 16, 24);
        model.add(WorldModel.OBSTACLE, 16, 25);
        model.add(WorldModel.OBSTACLE, 17, 24);
        model.add(WorldModel.OBSTACLE, 17, 25);
        model.add(WorldModel.OBSTACLE, 18, 24);
        model.add(WorldModel.OBSTACLE, 18, 25);

        // rack 27
        model.add(WorldModel.OBSTACLE, 20, 20);
        model.add(WorldModel.OBSTACLE, 20, 21);
        model.add(WorldModel.OBSTACLE, 20, 22);
        model.add(WorldModel.OBSTACLE, 20, 23);
        model.add(WorldModel.OBSTACLE, 20, 24);
        model.add(WorldModel.OBSTACLE, 20, 25);
        model.add(WorldModel.OBSTACLE, 21, 20);
        model.add(WorldModel.OBSTACLE, 21, 21);
        model.add(WorldModel.OBSTACLE, 21, 22);
        model.add(WorldModel.OBSTACLE, 21, 23);
        model.add(WorldModel.OBSTACLE, 21, 24);
        model.add(WorldModel.OBSTACLE, 21, 25);

        // rack 28
        model.add(WorldModel.OBSTACLE, 24, 20);
        model.add(WorldModel.OBSTACLE, 24, 21);
        model.add(WorldModel.OBSTACLE, 24, 22);
        model.add(WorldModel.OBSTACLE, 24, 23);
        model.add(WorldModel.OBSTACLE, 24, 24);
        model.add(WorldModel.OBSTACLE, 24, 25);
        model.add(WorldModel.OBSTACLE, 25, 20);
        model.add(WorldModel.OBSTACLE, 25, 21);
        model.add(WorldModel.OBSTACLE, 25, 22);
        model.add(WorldModel.ISSUE, 25, 23);
        model.add(WorldModel.OBSTACLE, 25, 24);
        model.add(WorldModel.OBSTACLE, 25, 25);

        // rack 29
        model.add(WorldModel.OBSTACLE, 28, 20);
        model.add(WorldModel.OBSTACLE, 28, 21);
        model.add(WorldModel.OBSTACLE, 28, 22);
        model.add(WorldModel.OBSTACLE, 28, 23);
        model.add(WorldModel.OBSTACLE, 28, 24);
        model.add(WorldModel.OBSTACLE, 28, 25);
        model.add(WorldModel.OBSTACLE, 29, 20);
        model.add(WorldModel.OBSTACLE, 29, 21);
        model.add(WorldModel.OBSTACLE, 29, 22);
        model.add(WorldModel.OBSTACLE, 29, 23);
        model.add(WorldModel.OBSTACLE, 29, 24);
        model.add(WorldModel.OBSTACLE, 29, 25);

        // rack 30
        model.add(WorldModel.OBSTACLE, 32, 20);
        model.add(WorldModel.OBSTACLE, 32, 21);
        model.add(WorldModel.OBSTACLE, 32, 22);
        model.add(WorldModel.OBSTACLE, 32, 23);
        model.add(WorldModel.OBSTACLE, 32, 24);
        model.add(WorldModel.OBSTACLE, 32, 25);
        model.add(WorldModel.OBSTACLE, 33, 20);
        model.add(WorldModel.OBSTACLE, 33, 21);
        model.add(WorldModel.OBSTACLE, 33, 22);
        model.add(WorldModel.OBSTACLE, 33, 23);
        model.add(WorldModel.OBSTACLE, 33, 24);
        model.add(WorldModel.OBSTACLE, 33, 25);

        // rack 31
        model.add(WorldModel.OBSTACLE, 1, 28);
        model.add(WorldModel.OBSTACLE, 1, 29);
        model.add(WorldModel.OBSTACLE, 1, 30);
        model.add(WorldModel.OBSTACLE, 1, 31);
        model.add(WorldModel.OBSTACLE, 1, 32);
        model.add(WorldModel.OBSTACLE, 1, 33);
        model.add(WorldModel.OBSTACLE, 2, 28);
        model.add(WorldModel.OBSTACLE, 2, 29);
        model.add(WorldModel.OBSTACLE, 2, 30);
        model.add(WorldModel.OBSTACLE, 2, 31);
        model.add(WorldModel.OBSTACLE, 2, 32);
        model.add(WorldModel.OBSTACLE, 2, 33);

        // rack 32
        model.add(WorldModel.ISSUE, 5, 28);
        model.add(WorldModel.OBSTACLE, 5, 29);
        model.add(WorldModel.OBSTACLE, 5, 30);
        model.add(WorldModel.OBSTACLE, 5, 31);
        model.add(WorldModel.OBSTACLE, 5, 32);
        model.add(WorldModel.OBSTACLE, 5, 33);
        model.add(WorldModel.OBSTACLE, 6, 28);
        model.add(WorldModel.OBSTACLE, 6, 29);
        model.add(WorldModel.OBSTACLE, 6, 30);
        model.add(WorldModel.OBSTACLE, 6, 31);
        model.add(WorldModel.OBSTACLE, 6, 32);
        model.add(WorldModel.OBSTACLE, 6, 33);

        // rack 33
        model.add(WorldModel.OBSTACLE, 9, 28);
        model.add(WorldModel.OBSTACLE, 9, 29);
        model.add(WorldModel.OBSTACLE, 9, 30);
        model.add(WorldModel.OBSTACLE, 9, 31);
        model.add(WorldModel.OBSTACLE, 9, 32);
        model.add(WorldModel.OBSTACLE, 9, 33);
        model.add(WorldModel.OBSTACLE, 10, 28);
        model.add(WorldModel.OBSTACLE, 10, 29);
        model.add(WorldModel.OBSTACLE, 10, 30);
        model.add(WorldModel.OBSTACLE, 10, 31);
        model.add(WorldModel.OBSTACLE, 10, 32);
        model.add(WorldModel.OBSTACLE, 10, 33);

        // rack 34
        model.add(WorldModel.OBSTACLE, 13, 28);
        model.add(WorldModel.OBSTACLE, 13, 29);
        model.add(WorldModel.OBSTACLE, 13, 30);
        model.add(WorldModel.OBSTACLE, 13, 31);
        model.add(WorldModel.OBSTACLE, 13, 32);
        model.add(WorldModel.OBSTACLE, 13, 33);
        model.add(WorldModel.OBSTACLE, 14, 28);
        model.add(WorldModel.OBSTACLE, 14, 29);
        model.add(WorldModel.OBSTACLE, 14, 30);
        model.add(WorldModel.OBSTACLE, 14, 31);
        model.add(WorldModel.OBSTACLE, 14, 32);
        model.add(WorldModel.OBSTACLE, 14, 33);

        // rack 35
        model.add(WorldModel.OBSTACLE, 16, 28);
        model.add(WorldModel.OBSTACLE, 16, 29);
        model.add(WorldModel.OBSTACLE, 17, 28);
        model.add(WorldModel.OBSTACLE, 17, 29);
        model.add(WorldModel.OBSTACLE, 18, 28);
        model.add(WorldModel.ISSUE, 18, 29);

        // rack 36
        model.add(WorldModel.OBSTACLE, 16, 32);
        model.add(WorldModel.OBSTACLE, 16, 33);
        model.add(WorldModel.OBSTACLE, 17, 32);
        model.add(WorldModel.OBSTACLE, 17, 33);
        model.add(WorldModel.OBSTACLE, 18, 32);
        model.add(WorldModel.OBSTACLE, 18, 33);

        // rack 37
        model.add(WorldModel.OBSTACLE, 20, 28);
        model.add(WorldModel.OBSTACLE, 20, 29);
        model.add(WorldModel.OBSTACLE, 20, 30);
        model.add(WorldModel.OBSTACLE, 20, 31);
        model.add(WorldModel.OBSTACLE, 20, 32);
        model.add(WorldModel.OBSTACLE, 20, 33);
        model.add(WorldModel.OBSTACLE, 21, 28);
        model.add(WorldModel.OBSTACLE, 21, 29);
        model.add(WorldModel.OBSTACLE, 21, 30);
        model.add(WorldModel.OBSTACLE, 21, 31);
        model.add(WorldModel.OBSTACLE, 21, 32);
        model.add(WorldModel.OBSTACLE, 21, 33);

        // rack 38
        model.add(WorldModel.ISSUE, 24, 28);
        model.add(WorldModel.OBSTACLE, 24, 29);
        model.add(WorldModel.OBSTACLE, 24, 30);
        model.add(WorldModel.OBSTACLE, 24, 31);
        model.add(WorldModel.OBSTACLE, 24, 32);
        model.add(WorldModel.OBSTACLE, 24, 33);
        model.add(WorldModel.OBSTACLE, 25, 28);
        model.add(WorldModel.OBSTACLE, 25, 29);
        model.add(WorldModel.OBSTACLE, 25, 30);
        model.add(WorldModel.OBSTACLE, 25, 31);
        model.add(WorldModel.OBSTACLE, 25, 32);
        model.add(WorldModel.OBSTACLE, 25, 33);

        // rack 39
        model.add(WorldModel.OBSTACLE, 28, 28);
        model.add(WorldModel.OBSTACLE, 28, 29);
        model.add(WorldModel.OBSTACLE, 28, 30);
        model.add(WorldModel.OBSTACLE, 28, 31);
        model.add(WorldModel.OBSTACLE, 28, 32);
        model.add(WorldModel.OBSTACLE, 28, 33);
        model.add(WorldModel.OBSTACLE, 29, 28);
        model.add(WorldModel.OBSTACLE, 29, 29);
        model.add(WorldModel.OBSTACLE, 29, 30);
        model.add(WorldModel.OBSTACLE, 29, 31);
        model.add(WorldModel.OBSTACLE, 29, 32);
        model.add(WorldModel.OBSTACLE, 29, 33);

        // rack 40
        model.add(WorldModel.OBSTACLE, 32, 28);
        model.add(WorldModel.ISSUE, 32, 29);
        model.add(WorldModel.OBSTACLE, 32, 30);
        model.add(WorldModel.OBSTACLE, 32, 31);
        model.add(WorldModel.OBSTACLE, 32, 32);
        model.add(WorldModel.OBSTACLE, 32, 33);
        model.add(WorldModel.OBSTACLE, 33, 28);
        model.add(WorldModel.OBSTACLE, 33, 29);
        model.add(WorldModel.OBSTACLE, 33, 30);
        model.add(WorldModel.OBSTACLE, 33, 31);
        model.add(WorldModel.OBSTACLE, 33, 32);
        model.add(WorldModel.OBSTACLE, 33, 33);

        model.setInitialNbIssues(model.countObjects(WorldModel.ISSUE));
        return model;
    }

}
