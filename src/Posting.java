import java.util.HashSet;

/**
 * Creates a posting to populate a posting list. Pairs document IDs with integer positions the associated word appears at.
 */

public class Posting {
	
	private String docID;
	private HashSet<Integer> positions;
	
	public Posting(String docID, int position){
		this.docID = docID;
		positions = new HashSet<Integer>();
		positions.add(position);
	}
	
	public String getID(){
		return docID;
	}
	
	public void addPosition(int position){
		positions.add(position);
	}
	
	public HashSet<Integer> getPositions(){
		return positions;
	}
	
	// Used when counting multi-word phrases. The argument will always indicate the position of the previous word in the phrase + 1.
	public boolean containsPosition(int position){
		return positions.contains(position);
	}
	
}
