import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Used for performing queries. Accesses the Inverted Index to count the words supplied as queries.
 */

public class Query {
	
	InvertedIndex invertedIndex;
	PrintWriter out;
	
	public Query(){
		invertedIndex = InvertedIndex.getInstance();
	}
	
	/** Executes query with format: [terms] ['multiple terms'] (optional operator) [ranking method] [getPlay] */
	public void executeQuery(String query){
		String[] phrases;
		if(query.contains("'"))
			phrases = query.split("'");
		else
			phrases = query.split(" ");
		
		if(phrases.length < 1)
			return;
		
		// Query phrase frequency used in ranking algorithms.
		HashMap<String, Integer> phraseCount = new HashMap<String, Integer>();
		ArrayList<String> trimmedPhrases = new ArrayList<String>();

		boolean getPlay = false;
		boolean qlRank = false;

		for(int i = 0; i < phrases.length; i++){
			if(!phrases[i].trim().isEmpty()){
				String phrase = phrases[i].trim().toLowerCase();
				if(phrase.equals("ql")){
					qlRank = true;
				}else if(phrase.equals("getplay")){
					getPlay = true;
				}else{
					Integer count = phraseCount.get(phrase);
					if(count == null)
						count = 0;
					phraseCount.put(phrase, count + 1);
					trimmedPhrases.add(phrase);
				}
			}
		}
		
		ArrayList<Scene> sceneResults = executeCommand(trimmedPhrases);
		rank(phraseCount, sceneResults, qlRank);
		printToFile(query, sceneResults, getPlay, qlRank);
	}
	
	/** Executes query over every scene. */
	private ArrayList<Scene> executeCommand(ArrayList<String> phrases){
		int operatorIndex = -1;
		for(int i = 0; i < phrases.size(); i++){
			if(phrases.get(i).equals(">")){
				operatorIndex = i;
				phrases.remove(i);
				break;
			}
		}
		
		ArrayList<Scene> sceneResults = new ArrayList<Scene>();
		
		for(Scene scene : invertedIndex.getScenes()){
			ArrayList<Integer> counts = new ArrayList<Integer>();
			
			for(String phrase : phrases){
				counts.add(invertedIndex.countPhrase(phrase, scene.getScene()));
			}
			
			// Separates the list in two to perform the greater than operation.
			if(operatorIndex >= 1){
				int left = sum(counts.subList(0, operatorIndex));
				int right = sum(counts.subList(operatorIndex, counts.size()));
				if(left > right){
					sceneResults.add(scene);
				}
			}else{
				if(sum(counts.subList(0, counts.size())) > 0){
					sceneResults.add(scene);
				}
			}
		}

		return sceneResults;
	}
	
	/** Picks ranking algorithm to use based on whether or not 'ql' appeared in query. */
	private void rank(HashMap<String, Integer> phrases, ArrayList<Scene> results, boolean qlRank){
		if(qlRank)
			Ranking.performQL(phrases, results);
		else
			Ranking.performBM25(phrases, results);
	}
	
	/** Prints the scenes/plays to file, according to the getPlay and qlRank booleans. */
	private void printToFile(String query, ArrayList<Scene> scenes, boolean getPlay, boolean qlRank){
		Collections.sort(scenes);
		String algorithmID = "bm25";
		if(qlRank)
			algorithmID = "ql";
		HashSet<String> results = new HashSet<String>();
		for(int i = 0; i < scenes.size() && results.size() < 5; i++){
			String id = scenes.get(i).getScene();
			if(getPlay)
				id = scenes.get(i).getPlay();
			// Avoid duplicate plays being printed if multiple scenes in same play.
			if(results.add(id)){
				float rank = (float) scenes.get(i).getScore();
				System.out.println(String.format("%d. %s with %s rank %f.", results.size(), id, algorithmID, rank));
			}
		}
	}
	
	/** Sums up values in list. */
	private int sum(List<Integer> counts){
		int sum = 0;
		for(int i = 0; i < counts.size(); i++){
			sum += counts.get(i);
		}
		return sum;
	}
	
}
