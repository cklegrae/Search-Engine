import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Used for performing queries. Accesses the Inverted Index to count the words supplied as queries.
 */

public class Query {
	
	InvertedIndex invertedIndex;
	PrintWriter out;
	
	int termNumber = 0;
	
	public Query(){
		invertedIndex = InvertedIndex.getInstance();
	}
	
	// Basic query language: 'term' 'multiword phrase' > true/false
	// Only accepted operator is >, which will check if the phrases on the left of it are in greater number than the phrases on its right.
	// Boolean at the end indicates whether or not we want to retrieve plays or scenes.
	public void executeQuery(String query){
		String[] phrases;
		if(query.contains("'"))
			phrases = query.split("'");
		else
			phrases = query.split(" ");
		ArrayList<String> trimmedPhrases = new ArrayList<String>();
		// Trims the phrases set to get rid of spaces.
		for(int i = 0; i < phrases.length - 1; i++){
			if(!phrases[i].trim().isEmpty()){
				trimmedPhrases.add(phrases[i].trim().toLowerCase());
			}
		}
		
		boolean getPlay = false;
		
		if(phrases[phrases.length - 1].contains("true")){
			getPlay = true;
		}else if(!phrases[phrases.length - 1].contains("false")){
			System.out.println("Query must end with a boolean value.");
			return;
		}
		ArrayList<String> sceneResults = executeCommand(trimmedPhrases);
		printToFile(query, sceneResults, getPlay);
	}
	
	// Executes query over every scene.
	private ArrayList<String> executeCommand(ArrayList<String> phrases){
		// If operatorIndex is -1, there is no '>.' Otherwise, it divides the phrase list into a left hand and right hand side.
		int operatorIndex = -1;
		for(int i = 0; i < phrases.size(); i++){
			if(phrases.get(i).equals(">")){
				operatorIndex = i;
				phrases.remove(i);
			}
		}
		
		// Scenes that fulfill the query.
		ArrayList<String> sceneResults = new ArrayList<String>();
		
		for(String scene : invertedIndex.getScenes()){
			ArrayList<Integer> counts = new ArrayList<Integer>();
			
			for(String phrase : phrases){
				counts.add(invertedIndex.countPhrase(phrase, scene));
			}
			
			// If we're comparing two subsets of the list...
			if(operatorIndex >= 1){
				int left = sum(counts.subList(0, operatorIndex));
				int right = sum(counts.subList(operatorIndex, counts.size()));
				if(left > right){
					sceneResults.add(scene);
				}
			// Otherwise we're just interested in whether or not they appeared in this scene.
			}else{
				if(sum(counts.subList(0, counts.size())) > 0){
					sceneResults.add(scene);
				}
			}
		}
		return sceneResults;
	}
	
	// Prints the scenes/plays to file, according to the getPlay boolean.
	private void printToFile(String query, ArrayList<String> scenes, boolean getPlay){
		HashSet<String> results = new HashSet<String>();
		for(String doc : scenes){
			if(getPlay)
				doc = invertedIndex.getPlay(doc);
			results.add(doc);
		}
		String[] abcResults = results.toArray(new String[results.size()]);
		Arrays.sort(abcResults, 0, abcResults.length);
		try {
			out = new PrintWriter("./result" + termNumber + ".txt");
			out.println(query);
			for(int i = 0; i < abcResults.length; i++){
				out.println(abcResults[i]);
			}
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Result printed to result" + termNumber + ".txt.");
		termNumber++;
	}
	
	// Sums up all ints inside given lists.
	private int sum(List<Integer> counts){
		int sum = 0;
		for(int i = 0; i < counts.size(); i++){
			sum += counts.get(i);
		}
		return sum;
	}
	
}
