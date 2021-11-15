// CArtAgO artifact code for project Data Center

package data_center;

import cartago.*;
import java.util.*;

public class WorldMap extends Artifact {

	int rows = 35;
	int cols = 35;

	String team_color;

	HashMap<Integer, String> world_map = new HashMap<Integer, String>();

	void init(String team) {

		this.team_color = team;

		for (int i = 0; i < rows*cols; i++){
			world_map.put(i, "?");
		}
		
		defineObsProperty("team_color", team_color);
		defineObsProperty("world_map", world_map);
	}

	private static Set<Integer> getKeys(HashMap<Integer, String> map, String value) {

		Set<Integer> result = new HashSet<>();
		if (map.containsValue(value)) {
			for (Map.Entry<Integer, String> entry : map.entrySet()) {
				if (Objects.equals(entry.getValue(), value)) {
					result.add(entry.getKey());
				}
			}
		}
		return result;
	}

	@OPERATION
	void setFreeCell(int X, int Y) {
		int my_key = cols*X+Y;
		try{
			world_map.replace(my_key, "F");
		}
		catch (NullPointerException npe){}
	}

	@OPERATION
	void setObstacleCell(int X, int Y) {
		int my_key = cols*X+Y;
		world_map.replace(my_key, "O");
	}

	@OPERATION
	void setIssueCell(int X, int Y) {
		int my_key = cols*X+Y;
		world_map.replace(my_key, "I");
	}

	@OPERATION
	void setSWIssueCell(int X, int Y) {
		int my_key = cols*X+Y;
		world_map.replace(my_key, "S");
	}

	@OPERATION
	void setIssueFound(int X, int Y) {
		signal("issue_found", X, Y);
	}

	@OPERATION
	void setIssuePicked(int X, int Y) {
		signal("issue_picked", X, Y);
	}

	@OPERATION
	void setAgentIssueCell(int X, int Y) {
		int my_key = cols*X+Y;
		world_map.replace(my_key, "A");
	}

	@OPERATION
	void askCellValue(int X, int Y, OpFeedbackParam<String> V) {
		int my_key = cols*X+Y;
		String my_value = world_map.get(my_key);
		V.set(my_value);
	}
	

	@OPERATION
	void askCloserIssueCell(int X, int Y, OpFeedbackParam<Integer> XG, OpFeedbackParam<Integer> YG) {
		int min_dist = 100;
		int min_x_issue = 100;
		int min_y_issue = 100;
		int x_issue;
		int y_issue;
		for (Integer key : getKeys(world_map, "I")) {
			if (key < 45){
				x_issue = 0;
				y_issue = key;
			} else {
				y_issue = key % 45;
				x_issue = (key - y_issue)/45;
			}
			int dist = (X - x_issue) + (Y - y_issue);
			if (dist < min_dist){
				min_dist = dist;
				min_x_issue = x_issue;
				min_y_issue = y_issue;
			}
		}
		XG.set(min_x_issue);
		YG.set(min_y_issue);
	}

	@OPERATION
	void askUnknownCell(OpFeedbackParam<Integer> X, OpFeedbackParam<Integer> Y) {
		List<Integer> list_unknown = new ArrayList<>();
		for (Integer key : getKeys(world_map, "?")) {
			list_unknown.add(key);
		}
		Random rand = new Random();
		if(list_unknown.size() > 0){
			int cell = list_unknown.get(rand.nextInt(list_unknown.size()));
			int x;
			int y;
			if (cell < 45){
				x = 0;
				y = cell;
			} else {
				y = cell % 45;
				x = (cell - y)/45;
			}
			X.set(x);
			Y.set(y);
		} else{
			X.set(100);
			Y.set(100);
		}
	}
}