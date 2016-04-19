import java.util.ArrayList;
import java.util.HashMap;

/** 
 * Performs document relevance ranking computations on scenes. 
 */
public class Ranking {

	/** Performs the BM25 algorithm on the given scenes in relation to the given queries. */
	public static void performBM25(HashMap<String, Integer> queries, ArrayList<Scene> scenes){
		InvertedIndex index = InvertedIndex.getInstance();
		double k1 = 1.2;
		double k2 = 100;
		double b = 0.75;
		double N = index.getScenes().size();
		double avgLength = index.getAverageLength();
		for(Scene scene : scenes){
			double score = 0;
			for(String queryTerm : queries.keySet()){
				double docLength = scene.getDocLength();
				double K = k1 * ((1 - b) + b * (docLength / avgLength));
				int queryFrequency = queries.get(queryTerm);
				int termFrequency = index.countPhrase(queryTerm, scene.getScene());
				// If the word doesn't ever appear, skip it.
				if(termFrequency == 0)
					continue;
				// Number of postings = number of documents the word appears in.
				int numDocuments = index.getPostings(queryTerm).size();
				// Log(x) / Log(2) = lg(x).
				double termEvaluation = Math.log(1 / ((numDocuments + 0.5) / (N - numDocuments + 0.5))) / Math.log(2);
				termEvaluation *= ((k1 + 1) * termFrequency) / (K + termFrequency);
				termEvaluation *= ((k2 + 1) * queryFrequency) / (k2 + queryFrequency);
				score += termEvaluation;
			}
			scene.setScore(score);
		}
	}
	
	/** Performs query likelihood ranking on the given scenes in relation to the given queries. */
	public static void performQL(HashMap<String, Integer> queries, ArrayList<Scene> scenes){
		InvertedIndex index = InvertedIndex.getInstance();
		double lambda = 0.8;
		double collectionSize = index.getCollectionSize();
		for(Scene scene : scenes){
			double score = 0;
			for(String queryTerm : queries.keySet()){
				int termFrequency = index.countPhrase(queryTerm, scene.getScene());
				if(termFrequency == 0)
					continue;
				int collectionFrequency = index.getTermAppearanceCount(queryTerm);
				double docLength = scene.getDocLength();
				double termEvaluation = (1 - lambda) * (termFrequency / docLength) + lambda * (collectionFrequency / collectionSize);
				termEvaluation = Math.log(termEvaluation) / Math.log(2);
				score += termEvaluation;
			}
			scene.setScore(score);
		}
	}
}
