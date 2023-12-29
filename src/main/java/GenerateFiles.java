import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GenerateFiles {

    public static void createTurtleFile(Model model, String name) {
    	 // Construct the file name with the format name.ttl in the output directory
        String outputDirectory = "src/main/resources/output/";
        String fileName = outputDirectory + name + ".ttl";

        // Create the output directory if it doesn't exist
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Error creating output directory: " + outputDirectory);
                return;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
            // Write the RDF model to the TTL file
            // Note: Adapt this part based on how RDF4J handles writing models to files
            // For RDF4J, you may need to use Rio to serialize the model to Turtle format.
            // Example: Rio.write(model, fos, RDFFormat.TURTLE);
            Rio.write(model, fos, RDFFormat.TURTLE);
            System.out.println("Turtle file created: " + fileName);

        } catch (IOException e) {
            System.out.println("SSEE ");
            System.err.println("Error creating Turtle file: " + e.getMessage());
        }
    }
}
