/** 
 * Contains all scene information.
 */
public class Scene implements Comparable<Scene>{

	private String scene;
	private String play;
	private String[] text;
	private double score;
	
	public Scene(String scene, String play, String[] text){
		this.scene = scene;
		this.play = play;
		this.text = text;
	}
	
	public void setScore(double score){
		this.score = score;
	}
	
	public String getScene(){
		return scene;
	}
	
	public String getPlay(){
		return play;
	}
	
	public String[] getText(){
		return text;
	}

	public int getDocLength(){
		return text.length;
	}
	
	public double getScore(){
		return score;
	}

	@Override
	public int compareTo(Scene arg0) {
		if(getScore() > arg0.getScore())
			return -1;
		else if(getScore() < arg0.getScore())
			return 1;
		return 0;
	}
}
