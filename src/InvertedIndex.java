import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The Inverted Index associates words/terms with document/position pairs. Using this information you can quickly access information regarding terms.
 */

public class InvertedIndex {
	
	private static InvertedIndex instance;
	
	// Map to find a term's posting list (postings contain scene/positional information).
	private HashMap<String, ArrayList<Posting>> termPostings;
	
	// Map to find the play a scene belongs to.
	private HashMap<String, String> scenePlays;
	
	// Set of unique scenes.
	private HashSet<String> scenes;
	
	private InvertedIndex(){
		termPostings = new HashMap<String, ArrayList<Posting>>();
		scenes = new HashSet<String>();
		scenePlays = new HashMap<String, String>();
	}
	
	// Gets the single instance of the II.
	public static InvertedIndex getInstance(){
		if(instance == null)
			instance = new InvertedIndex();
		return instance;
	}
	
	// Builds the Inverted Index.
	public void extractTerms(){
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(new BufferedReader(new FileReader("shakespeare-scenes.json")));
			JSONArray jsonArray = (JSONArray) json.get("corpus");
			// Every json object inside the array has a unique sceneID, a unique sceneNum, a non-unique playID and text.
			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject scene = (JSONObject) parser.parse(jsonArray.get(i).toString());

				String playID = scene.get("playId").toString();
				String sceneID = scene.get("sceneId").toString();
				String[] words = scene.get("text").toString().split("\\s+");
				
				scenes.add(sceneID);
				scenePlays.put(sceneID, playID);
				
				// For each word in the scene's text, create a term.
				for(int q = 0; q < words.length; q++){
					createTerm(words[q], sceneID, q + 1);
				}
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	}
	
	// Associates the term with an new/updated posting list.
	private void createTerm(String term, String sceneID, int position){
		ArrayList<Posting> postings = termPostings.get(term);
		if(postings == null)
			postings = new ArrayList<Posting>();
		updatePosting(postings, sceneID, position);
		termPostings.put(term, postings);
	}
	
	// Updates a term's posting list with a new scene/positions pair.
	private void updatePosting(ArrayList<Posting> postings, String sceneID, int position){
		int index = findPosting(postings, sceneID);
		if(index == -1){
			postings.add(new Posting(sceneID, position));
		}else{
			// Update the found posting with a new position.
			postings.get(index).addPosition(position);
		}
	}
	
	// Finds the posting that corresponds to the current scene.
	private int findPosting(ArrayList<Posting> postings, String sceneID){
		if(postings == null)
			return -1;
		for(int i = 0; i < postings.size(); i++){
			if(postings.get(i).getID().equals(sceneID))
				return i;
		}
		return -1;
	}
	
	// Count the occurrences of this phrase in the scene. 
	public int countPhrase(String phrase, String scene){
		int count = 0;
		String[] terms = phrase.split("\\s+");
		ArrayList<Posting> postings = new ArrayList<Posting>();
		
		// Get all relevant postings.
		for(int i = 0; i < terms.length; i++){
			ArrayList<Posting> postingList = termPostings.get(terms[i]);
			int index = findPosting(postingList, scene);
			// If even a single term in the phrase doesn't have a scene-related posting, this phrase can't appear in this scene.
			if(index < 0)
				return 0;
			postings.add(postingList.get(index));
		}

		// All possible occurrences of the phrase must start from one of these indexes.
		HashSet<Integer> startPositions = postings.get(0).getPositions();
		for(int pos : startPositions){
			boolean phraseValidity = true;
			for(int z = 1; z < postings.size(); z++){
				// If this posting doesn't occur at pos + 1, then the words aren't in proper order and this specific starting point is invalid.
				if(!postings.get(z).containsPosition(++pos)){
					phraseValidity = false;
					break;
				}
			}
			if(phraseValidity)
				count++;
		}
		return count;
	}
	
	public HashSet<String> getScenes(){
		return scenes;
	}
	
	public String getPlay(String scene){
		return scenePlays.get(scene);
	}	
	
}
