import java.io.*;
import java.util.ArrayList;

public class SparqlQueryManager {
    private ArrayList<String> queries;
    private ArrayList<String[]> variables;

    public SparqlQueryManager() {
        queries = new ArrayList<>();
        variables = new ArrayList<>();
    }

    public void addEntry(Object entry) {
        if (entry instanceof String)
            queries.add((String) entry);
        else
            variables.add((String[]) entry);
    }

    public String retrieveQuery(int index) {
        return queries.get(index);
    }

    public String[] retrieveVariables(int index) {
        return variables.get(index);
    }

    public int getNumOfQueries() {
        return queries.size();
    }

    public int getNumOfVariables(int index) {
        return variables.get(index).length;
    }

    public void readQueriesFromFile() {
        StringBuilder currentQuery = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("./src/main/resources/sparql.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("##next")) {
                    addEntry(currentQuery.toString());
                    currentQuery.setLength(0);
                } else if (line.startsWith("##var")) {
                    addEntry(line.substring(line.indexOf(" ") + 1).split("\\s+"));
                } else
                    currentQuery.append(line).append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeQueryResultsToFile(int index, ArrayList<ArrayList<String>> results) {
        System.out.print(results);
        try {
            File file = new File(String.format("%s_%d.csv", "./src/main/resources/sparqlResults", index));
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String variable : variables.get(index))
                writer.write(variable + ",");
            writer.newLine();
            for (ArrayList<String> record : results) {
                for (String binding : record)
                    writer.write(binding + ",");
                writer.newLine();
            }
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
