package data_center;

import jason.environment.grid.Location;
import java.util.Random;
import java.util.logging.Logger;

public class IssueGenerator implements Runnable {

  WorldModel model;
  WorldView view;
  
  private Logger logger   = Logger.getLogger("jasonTeamSimLocal.mas2j." + WorldModel.class.getName());

  protected Random random = new Random();
  
  private static final int SLEEP_TIME = 10000; 

  public IssueGenerator(WorldModel newModel, WorldView newView) {
    model = newModel;
    view = newView;
  }

  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        generateRandomIssue();
        Thread.sleep(SLEEP_TIME);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void generateRandomIssue() {
    Location l = model.getFreePos();
    if( model.hasObject(WorldModel.OBSTACLE, l.y, l.x) ) {	   
      model.add(WorldModel.ISSUE, l.y, l.x);
	    model.setInitialNbIssues(model.getInitialNbIssues()+1);		    
	    view.update(l.y, l.x);
	    view.udpateCollectedIssues();
	    logger.warning("New issue in (" + l.x + "," + l.y + ")!");
    }    
  }
}