package data_center;

import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

import java.util.logging.Logger;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

public class DataCenterPlanet extends Artifact {

    private static Logger logger = Logger.getLogger(DataCenterPlanet.class.getName());

    static WorldModel  model = null;
    static WorldView   view;
    
    // Gerador de ouro
    Thread issueGenerator;

    static int     simId    = 5; // type of environment
    static int     sleep    = 200;
    static boolean hasGUI   = true;
    
    int     agId     = -1;

    public enum Move {
        UP, DOWN, RIGHT, LEFT
    };

    @OPERATION
    public void init(int scenario, int agId) {
        this.agId = agId;
        initWorld(scenario);
    }

    public int getSimId() {
        return simId;
    }

    public void setSleep(int s) {
        sleep = s;
    }

    @OPERATION void up() throws Exception {     move(Move.UP);    }
    @OPERATION void down() throws Exception {   move(Move.DOWN);  }
    @OPERATION void right() throws Exception {  move(Move.RIGHT); }
    @OPERATION void left() throws Exception {   move(Move.LEFT);  }
    
    void move(Move m) throws Exception {
        if (sleep > 0) await_time(sleep);
        model.move(m, agId);
        updateAgPercept();
    }

    @OPERATION void pick() throws Exception {
        if (sleep > 0) await_time(sleep);
        model.pick(agId);
        updateAgPercept();
    }
    @OPERATION void drop() throws Exception {
        if (sleep > 0) await_time(sleep);
        model.drop(agId);
        view.udpateCollectedIssues();
        updateAgPercept();
    }
    @OPERATION void skip() {
        if (sleep > 0) await_time(sleep);
        updateAgPercept();
    }

    public synchronized void initWorld(int w) {
        simId = w;
        try {
            if (model == null) {
                switch (w) {
                case 1: model = WorldModel.world1(); break;
                default:
                    logger.info("Invalid index!");
                    return;
                }
                if (hasGUI) {
                    view = new WorldView(model);
                    view.setEnv(this);
                    view.udpateCollectedIssues();
                }
            }
            defineObsProperty("gsize", simId, model.getWidth(), model.getHeight());
            defineObsProperty("depot", simId, model.getDepot().x, model.getDepot().y);
            defineObsProperty("pos", -1, -1);
            updateAgPercept();
        } catch (Exception e) {
            logger.warning("Error creating world "+e);
            e.printStackTrace();
        }
    }

    public void endSimulation() {
        defineObsProperty("end_of_simulation", simId, 0);
        if (view != null) view.setVisible(false);
        WorldModel.destroy();
    }

    private void updateAgPercept() {
        // its location
        Location l = model.getAgPos(agId);
        ObsProperty p = getObsProperty("pos");
        p.updateValue(0, l.x);
        p.updateValue(1, l.y);

        if (model.isCarryingPart(agId)) {
            if (!hasObsProperty("carrying_part"))
                defineObsProperty("carrying_part");
        } else try {
            removeObsProperty("carrying_part");
        } catch (IllegalArgumentException e) {}

        // what's around
        updateAgPercept(l.x - 1, l.y - 1);
        updateAgPercept(l.x - 1, l.y);
        updateAgPercept(l.x - 1, l.y + 1);
        updateAgPercept(l.x, l.y - 1);
        updateAgPercept(l.x, l.y);
        updateAgPercept(l.x, l.y + 1);
        updateAgPercept(l.x + 1, l.y - 1);
        updateAgPercept(l.x + 1, l.y);
        updateAgPercept(l.x + 1, l.y + 1);
    }

    private static Term issue     = new Atom("issue");
    private static Term swissue   = new Atom("swissue");
    private static Term obstacle  = new Atom("obstacle");

    private void updateAgPercept(int x, int y) {
        if (model == null || !model.inGrid(x,y)) return;

        // remove all first
        try {
            removeObsPropertyByTemplate("cell", null, null, null);
        } catch (IllegalArgumentException e) {}

        if ( model.hasObject(WorldModel.ISSUE, x, y) ) {
            defineObsProperty("cell", x, y, issue);
        } else if ( model.hasObject(WorldModel.SWISSUE, x, y) ) {
            defineObsProperty("cell", x, y, swissue);
        } else if ( model.hasObject(WorldModel.OBSTACLE, x, y) ) {
            defineObsProperty("cell", x, y, obstacle);
        }
    }
}