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
	
	private HashSet<Scene> scenes;
	
	private InvertedIndex(){
		termPostings = new HashMap<String, ArrayList<Posting>>();
		scenes = new HashSet<Scene>();
	}
	
	public static InvertedIndex getInstance(){
		if(instance == null)
			instance = new InvertedIndex();
		return instance;
	}
	
	/** Builds the Inverted Index. */
	public void buildIndex(){
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
				
				Scene sceneObject = new Scene(sceneID, playID, words);
				scenes.add(sceneObject);
				
				for(int q = 0; q < words.length; q++){
					createTerm(words[q], sceneID, q + 1);
				}
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/** Associates the term with a new/updated posting list. */
	private void createTerm(String term, String sceneID, int position){
		ArrayList<Posting> postings = termPostings.get(term);
		if(postings == null)
			postings = new ArrayList<Posting>();
		int index = findPosting(postings, sceneID);
		if(index == -1)
			postings.add(new Posting(sceneID, position));
		else
			postings.get(index).addPosition(position);
		termPostings.put(term, postings);
	}

	/** Counts the occurrences of this phrase in the given scene. */
	public int countPhrase(String phrase, String scene){
		int count = 0;
		String[] terms = phrase.split("\\s+");
		ArrayList<Posting> postings = new ArrayList<Posting>();
		
		for(int i = 0; i < terms.length; i++){
			ArrayList<Posting> postingList = termPostings.get(terms[i]);
			int index = findPosting(postingList, scene);
			if(index < 0)
				return 0;
			postings.add(postingList.get(index));
		}

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
	
	/** Finds the posting that corresponds to the current scene. */
	private int findPosting(ArrayList<Posting> postings, String sceneID){
		if(postings == null)
			return -1;
		for(int i = 0; i < postings.size(); i++){
			if(postings.get(i).getID().equals(sceneID))
				return i;
		}
		return -1;
	}
	
	/** Gets a list of Postings for the given term. */
	public ArrayList<Posting> getPostings(String term){
		String[] terms = term.split("\\s+");
		ArrayList<Posting> result = termPostings.get(terms[0]);
		for(int i = 1; i < terms.length; i++)
			result.addAll(termPostings.get(terms[i]));
		return result;
	}
	
	/** Returns the number of times a term appears in the collection. */
	public int getTermAppearanceCount(String term){
		int count = 0;
		for(Posting posting : getPostings(term)){
			count += posting.getPositions().size();
		}
		return count;
	}
	
	/** Returns the size of the collection. */
	public int getCollectionSize(){
		int count = 0;
		for(Scene scene : scenes){
			count += scene.getDocLength();
		}
		return count;
	}
	
	/** Gets the average document length of the collection's scenes. */
	public double getAverageLength(){
		int count = getCollectionSize();
		double divisor = scenes.size();
		return count / divisor;
	}
	
	public HashSet<Scene> getScenes(){
		return scenes;
	}
	
}
