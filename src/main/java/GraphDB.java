import com.ontotext.graphdb.repository.http.GraphDBHTTPRepository;
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepositoryBuilder;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.shacl.GraphDBValidationException;
import java.io.*;

public class GraphDB {

    private RepositoryManager repoManager;
    private RepositoryConnection connection;


    public GraphDB(boolean validate) {
        try {
            // create repository
            repoManager = RepositoryProvider.getRepositoryManager("http://localhost:7200/");
            repoManager.init();
            // upload data and run queries
            connection = startConnection();
            if(validate) {
                commitShaclShapesGraph();
                uploadDataAndValidate();
            }else
                uploadData();

            if(connection. isOpen())
                connection.close();

        }catch (Exception e){
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

    private void commitShaclShapesGraph() throws IOException, GraphDBValidationException {
        connection.begin();
        // test rule
        StringReader shaclRules = new StringReader(String.join("\n",
                "PREFIX fhir_instance: <http://hl7.org/fhir_instances/>",
                "PREFIX fhir: <http://hl7.org/fhir/>",
                "PREFIX sh: <http://www.w3.org/ns/shacl#>",
                "fhir_instance:TestShape",
                "    a sh:NodeShape ;",
                "    sh:targetClass fhir:Observation ;",
                "    sh:property [",
                "        sh:path fhir:component ;",
                "        sh:node fhir_instance:ComponentShape",
                "    ] ;",
                "    sh:property [",
                "        sh:path fhir:value ;",
                "        sh:node fhir_instance:ValueShape",
                "    ] ;",
                "    sh:property [",
                "        sh:path fhir:effectiveDateTime ;",
                "        sh:node fhir_instance:DateTimeShape",
                "    ] ;",
                "    sh:property [",
                "        sh:path fhir:code ;",
                "        sh:node fhir_instance:CodeShape",
                "    ] ."
        ));
        System.out.println("commit shapes graph ");
        connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void uploadData() throws IOException {
        System.out.println(">>> Upload Data");
        // When adding data we need to start a transaction
        connection.begin();
        connection.add(new FileInputStream("./src/main/resources/output/orientation_SEE.ttl"), "urn:base", RDFFormat.TURTLE);
        // Committing the transaction persists the data
        connection.commit();
    }

    public void uploadDataAndValidate() throws FileNotFoundException {
    	try {
    	    connection.begin();
    	    connection.add(new FileInputStream("./src/main/resources/output/orientation_SEE.ttl"), "urn:base", RDFFormat.TURTLE);
    	    // Committing the transaction persists the data
    	    connection.commit();

    	    // Create an empty model for success
    	    Model successReportModel = new LinkedHashModel();

    	    // Use the SHACL namespace to create a literal with a boolean value
    	    ValueFactory vf = connection.getValueFactory(); // Obtain the ValueFactory from the RepositoryConnection
    	    IRI shaclNamespace = vf.createIRI("http://www.w3.org/ns/shacl#", "");
    	    IRI conformsIRI = vf.createIRI(shaclNamespace.stringValue() + "conforms");
    	    BNode bNode = vf.createBNode();
    	    Literal literal = vf.createLiteral(true);
    	    
    	    // Use the ValueFactory to create a statement
    	    Statement statement = vf.createStatement(bNode, conformsIRI, literal);

    	    // Add the statement to the success report model
    	    successReportModel.add(statement);

    	    // Write the success report to a file
    	    OutputStream successReport = new FileOutputStream("./src/main/resources/output/success_report.ttl");
    	    Rio.write(successReportModel, successReport, RDFFormat.TURTLE);

    	} catch (RepositoryException | IOException exception) {
    	    System.out.println("Exception caught");
    	    Throwable cause = exception.getCause();
    	    if (cause instanceof GraphDBValidationException) {
    	        Model validationReportModel = ((GraphDBValidationException) cause).validationReportAsModel();
    	        OutputStream report = new FileOutputStream("./src/main/resources/output/validation_report.ttl");
    	        Rio.write(validationReportModel, report, RDFFormat.TURTLE);
    	    }
    	System.out.println("Success");

        //clearShaclShapesGraph();
    }
    }
}