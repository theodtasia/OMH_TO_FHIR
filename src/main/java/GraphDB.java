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
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import java.util.ArrayList;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.eclipse.rdf4j.common.exception.ValidationException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.results.ValidationReport;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
public class GraphDB {

    private RepositoryManager repoManager;
    private RepositoryConnection connection;

    public GraphDB(String fileNameObservation, String observation) {
        try {
            System.out.print(fileNameObservation);
            // create repository
            repoManager = RepositoryProvider.getRepositoryManager("http://localhost:7200/");
            repoManager.init();
            createRepository();
            printRepos();
            repoManager.shutDown();

            // upload data and run queries
            connection = startConnection();
            commitShaclShapesGraph(observation);
            uploadDataAndValidate(fileNameObservation);
            runQueries();

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
    	Validation shaclValidation = new Validation();
        shaclValidation.validate();

    }

    public void clearShaclShapesGraph() {
        System.out.println("Clear shapes graph");
        connection.begin();
        connection.clear(RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

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

    private void createRepository() throws Exception{
        System.out.println("Create Repository");

        if (repoExists()) // remove and create again. Alt return
            repoManager.removeRepository("omh_to_fhir");

        TreeModel graph = new TreeModel();

        InputStream config = new FileInputStream("./src/main/resources/graphConfig.ttl");
        RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(graph));
        rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
        config.close();

        Resource repositoryNode = Models.subject(graph.filter(null, RDF.TYPE,
                RepositoryConfigSchema.REPOSITORY)).orElse(null);

        graph.add(repositoryNode, RepositoryConfigSchema.REPOSITORYID,
                SimpleValueFactory.getInstance().createLiteral("omh_to_fhir"));

        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph, repositoryNode);
        repoManager.addRepositoryConfig(repositoryConfig);
    }
    
    public void runQueries() {
        System.out.println("Run Queries");
        SparqlQueryManager queries = new SparqlQueryManager();
        queries.readQueriesFromFile();

        for(int i=0; i< queries.getNumOfQueries(); ++i) {
            ArrayList<ArrayList<String>> records = new ArrayList<>();
            TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL,
                    queries.retrieveQuery(i)).evaluate();

            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                ArrayList<String> bindings = new ArrayList<>();

                for (String var : queries.retrieveVariables(i)) {
                    Object binding = bindingSet.getBinding(var).getValue();
                    if (binding instanceof IRI)
                        binding = ((IRI) binding).getLocalName();
                    bindings.add(binding.toString());
                }
                records.add(bindings);
            }
            result.close();
            queries.writeQueryResultsToFile(i, records);
        }
    }

    private void printRepos() {
        System.out.println(repoManager.getRepositoryIDs());
    }
    private boolean repoExists() {
        return repoManager.hasRepositoryConfig("");
    }
}
