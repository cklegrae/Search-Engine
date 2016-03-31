import java.util.Scanner;

public class Main {

	// Creates Inverted Index, executes queries.
	public static void main(String[] args) {
		InvertedIndex index = InvertedIndex.getInstance();
		index.extractTerms();
		
		// Example queries
		// query.executeQuery("'thee' 'thou' > 'you' false");
		// query.executeQuery("'Verona' 'rome' 'Italy' false");
		// query.executeQuery("'falstaff' true");
		// query.executeQuery("'a rose by any other name' false");
		
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
