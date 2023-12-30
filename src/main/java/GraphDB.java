import java.io.*;

import com.ontotext.graphdb.repository.http.GraphDBHTTPRepository;
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepositoryBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.shacl.GraphDBValidationException;

public class GraphDB {

    private RepositoryManager repoManager;
    private RepositoryConnection connection;

    public GraphDB(String fileNameObservation, String observation) {
        try {
            System.out.print(fileNameObservation);
            // create repository
            repoManager = RepositoryProvider.getRepositoryManager("http://localhost:7200/");
            repoManager.init();
            // upload data and run queries
            connection = startConnection();
            commitShaclShapesGraph(observation);
            uploadDataAndValidate(fileNameObservation);

            if (connection.isOpen())
                connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RepositoryConnection startConnection() {
        GraphDBHTTPRepository repository = new GraphDBHTTPRepositoryBuilder()
                .withServerUrl("http://localhost:7200")
                .withRepositoryId("omh_to_fhir")
                .build();
        return repository.getConnection();
    }
    

    private String readShaclRulesFromFile(String filePath) throws IOException {
        // Read the contents of the SHACL rules file
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        }
    }
    
    private void commitShaclShapesGraph(String observation) throws IOException, GraphDBValidationException {
    	String inputDirectory = "src/main/resources/validation/";
	    String inputFile = inputDirectory + observation + ".txt";
	    
        connection.begin();

        // Read SHACL rules from the file
        String shaclRules = readShaclRulesFromFile(inputFile);

        // Commit SHACL rules
        System.out.println("Commit Shaql");
        connection.add(new StringReader(shaclRules), "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void clearShaclShapesGraph() {
        System.out.println("Clear shapes graph");
        connection.begin();
        connection.clear(RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

 // ./src/main/resources/data/heart_rate
    public void uploadDataAndValidate(String fileName) throws FileNotFoundException {        
        try {
            // Start a new transaction for validation
            connection.begin();
            // Validate data
            System.out.println("Upload Data");
            connection.add(new FileInputStream(fileName), "urn:base", RDFFormat.TURTLE);
            connection.commit();
        } catch (RepositoryException | GraphDBValidationException exception) {
            System.out.println("Exception caught");
            Throwable cause = exception.getCause();
            if (cause instanceof GraphDBValidationException) {
                Model validationReportModel = ((GraphDBValidationException) cause).validationReportAsModel();
                try (OutputStream report = new FileOutputStream(
                        "./src/main/resources/data/validationOutput/validation_report.ttl")) {
                    Rio.write(validationReportModel, report, RDFFormat.TURTLE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            clearShaclShapesGraph();
        } catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }



}
