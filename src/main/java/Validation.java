
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepository;
import com.ontotext.graphdb.repository.http.GraphDBHTTPRepositoryBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.shacl.GraphDBValidationException;
import java.io.IOException;
import java.io.StringReader;


// to upload shacl shape and validate after data have already been uploaded
public class Validation {

    //private SailRepositoryConnection connection;
    private RepositoryConnection connection;
    public Validation() {
        GraphDBHTTPRepository repository = new GraphDBHTTPRepositoryBuilder()
                .withServerUrl("http://localhost:7200")
                .withRepositoryId("omh_to_fhir")
                .build();
        connection = repository.getConnection();

    }

    private void commitShaclShapesGraph() throws IOException, GraphDBValidationException {
        connection.begin();

        StringReader shaclRules = new StringReader(
        		String.join("\n", 
                	    "@prefix fhir: <http://hl7.org/fhir#> .",
                	    "@prefix fhir_instance: <http://hl7.org/fhir_instance#> .",
                	    "@prefix sh: <http://www.w3.org/ns/shacl#> .",
                	    "",
                	    "fhir_instance:ObservationShape",
                	    "    a sh:NodeShape ;",
                	    "    sh:targetClass fhir:Observation ;",
                	    "    sh:property [",
                	    "        sh:path fhir:code ;",
                	    "        sh:node fhir_instance:CodeShape ;",
                	    "    ] .",
                	    "",
                	    "fhir_instance:CodeShape",
                	    "    a sh:NodeShape ;",
                	    "    sh:property [",
                	    "        sh:path fhir:code ;",
                	    "        sh:minCount 1 ;",
                	    "        sh:class fhir:decimal;",
                	    "    ] ;",
                	    "    sh:property [",
                	    "        sh:path fhir:system ;",
                	    "        sh:minCount 1 ;",
                	    "        sh:class fhir:string ;",
                	    "    ] ;",
                	    "    sh:property [",
                	    "        sh:path fhir:display ;",
                	    "        sh:minCount 1 ;",
                	    "        sh:class fhir:string ;",
                	    "    ] ."
                	)
        );
        
        System.out.println("commit shapes graph ");
        connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void clearShaclShapesGraph() {
        System.out.println("Clear shapes graph");
        connection.begin();
        connection.clear(RDF4J.SHACL_SHAPE_GRAPH);
        connection.commit();
    }

    public void validate() {
        try {
            commitShaclShapesGraph();
        } catch (RepositoryException exception) {
            System.out.println("Exception caught");
            Throwable cause = exception.getCause();
            if (cause instanceof GraphDBValidationException) {
                Model validationReportModel = ((GraphDBValidationException) cause).validationReportAsModel();
                Rio.write(validationReportModel, System.out, RDFFormat.TURTLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //clearShaclShapesGraph();
    }

    public static void main(String[] args) {
        new Validation().validate();
        //new Validation().clearShaclShapesGraph();
    }

}

