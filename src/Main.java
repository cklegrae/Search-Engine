import java.util.Scanner;

public class Main {

	// Creates Inverted Index, executes queries.
	public static void main(String[] args) {
		InvertedIndex index = InvertedIndex.getInstance();
		index.buildIndex();
		
//		Example queries:
//		query.executeQuery("the king queen royalty ql");
//		query.executeQuery("rosencrantz guildenstern");
//		query.executeQuery("hamlet > polonius");
		
		Query query = new Query();
		Scanner scanner = new Scanner(System.in);
		String userQuery = scanner.nextLine();
		while(!userQuery.equals("end")){
			query.executeQuery(userQuery);
			userQuery = scanner.nextLine();
		}
		scanner.close();
	}

}
