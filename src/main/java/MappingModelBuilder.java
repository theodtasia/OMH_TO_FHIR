import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.io.File;
import java.util.Scanner;

public class MappingModelBuilder {
	public static int counter = 0;
	public static String observation = "";
	public static String observationInstanceName = "";
	public static boolean descriptive_statistic_exists = false;

	public static String fhir = "http://hl7.org/fhir/";
	public static String fhirInstance = "http://hl7.org/fhir_instances/";
	
	public static ModelBuilder builder = new ModelBuilder();

	static String fieldName = "";

	static String[] componentsCodeable = { "temporal_relationship_to_physical_activity", "temporal_relationship_to_sleep", "temporal_relationship_to_meal", "body_posture"};
	static String[] componentsWithValue = {"supplemental_oxygen_flow_rate", "diastolic_blood_pressure", "systolic_blood_pressure", "angular_velocity_x", "angular_velocity_y", "angular_velocity_z", "acceleration_x", "acceleration_y", "acceleration_z" ,"distance"};
	static String[] codesConcepts = { "measurement_method", "descriptive_statistic", "descriptive_statistic_denominator", "activity_name", "oxygen_therapy_mode_of_administration", "system"};

	
	public static void main(String[] args) throws IOException {
				
        List<String> generalObservationNames = new ArrayList<>(List.of(
                "heart_rate", "kcal_burned", "step_count", "speed", "oxygen_saturation",
                "body_fat_percentage", "body_weight", "body_mass_index", "blood_glucose", "body_temperature"
        ));
        
		List<String> componentCodeableConcepts = new ArrayList<>();
		List<String> componentsWithValues = new ArrayList<>();
		List<String> codesWithStringValues = new ArrayList<>();

		
		for (String element : componentsCodeable) {
			componentCodeableConcepts.add(element);
		}
		for (String element : componentsWithValue) {
			componentsWithValues.add(element);
		}
		for (String element : codesConcepts) {
			codesWithStringValues.add(element);
		}
		String configurationPath = "src/main/resources/data/codes_info.json";
		
		
        String observation = UserInput.getUserChoice();

        String folderPath = UserInput.getUserFolder();

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        // Read JSON configuration file
        JsonNode configurationCodes = objectMapper.readTree(new File(configurationPath));

        File observationFolderPath = new File(folderPath);

        // Check if the given path is a directory
        if (!observationFolderPath.isDirectory()) {
            System.out.println("The provided path is not a directory.");
            return;
        }

        // Get a list of files in the directory
        File[] files = observationFolderPath.listFiles();

        // Check if there are any files in the directory
        if (files == null || files.length == 0) {
            System.out.println("The directory is empty.");
            return;
        }

        for (File observationFilePath : files) {
		try {
			// Create an ObjectMapper
			ObjectMapper objectMapperFile = new ObjectMapper();

			// Read and parse the JSON file
			JsonNode observationJson = objectMapper.readTree(observationFilePath);
 
			// Get the patientId
            String patientId = observationJson.get("id").asText();
			observationInstanceName =  observation+"_"+patientId;

			// Create an RDF model to accumulate statements
			builder.setNamespace("fhir", "http://hl7.org/fhir/");
			builder.setNamespace("fhir_instance", "http://hl7.org/fhir_instances/");
			builder.setNamespace("schema", "http://schema.org/");

			// Iterate through the JSON fields and extract their values dynamically
			Iterator<Map.Entry<String, JsonNode>> fields = observationJson.fields();
		    
			if (observation=="blood_pressure") {
				observationNameUtil(observation, observationInstanceName, configurationCodes);
			}
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
			    String fieldName = field.getKey();
				JsonNode fieldValue = field.getValue();
				if(generalObservationNames.contains(fieldName)){
						observationNameWithValueUtil(observation, observationInstanceName, configurationCodes, fieldValue);								
				}
				else {
					// Check if the field is "effective_time_frame" and process its keys
					JsonNode effectiveTimeFrameNode = observationJson.get("effective_time_frame");
					if ("effective_time_frame" == fieldName) {

						if (effectiveTimeFrameNode != null && effectiveTimeFrameNode.isObject()) {
							// Navigate to the "time_interval" field
							JsonNode timeIntervalNode = effectiveTimeFrameNode.get("time_interval");
							
							
							//effectiveTimeFrame.time_interval
							if (timeIntervalNode != null && timeIntervalNode.isObject()) {
								JsonNode startDateTimeNode = timeIntervalNode.get("start_date_time");
								JsonNode endDateTimeNode = timeIntervalNode.get("end_date_time");
								JsonNode duration = timeIntervalNode.get("duration");
								// Get the "start_date_time" and "end_date_time" fields
								// Check if the fields exist and are not null
								
								//effectiveTimeFrame.time_interval.duration
								if (duration != null && !duration.isNull()) {

									Float durationValue = (float) duration.get("value").asDouble();
									String durationUnit = duration.get("unit").asText();
									builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.effectivePeriod"),
											Values.iri(fhirInstance + "Period" + counter));

									builder.subject(fhirInstance + "Period" + counter).add(Values.iri(fhir + "Observation.effectiveDuration"),
									Values.iri(fhirInstance + "Duration" + counter));
									
									String encodedParameter = URLEncoder.encode(durationUnit, "UTF-8");

									builder.subject(fhirInstance + encodedParameter)
									.add(RDF.TYPE, Values.iri(fhir + "string"));
								
									builder.subject(fhirInstance + durationValue)
									.add(RDF.TYPE, Values.iri(fhir + "decimal"));
								
									
									builder.subject(fhirInstance + "Duration" + counter)
											.add(RDF.TYPE, Values.iri(fhir + "Duration"))
											.add(Values.iri(fhir + "value"),
													Values.iri(fhirInstance + durationValue))
											.add(Values.iri(fhir + "unit"),
													Values.iri(fhirInstance + durationUnit));
										

									if (startDateTimeNode != null && !startDateTimeNode.isNull()) {
										builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.effectivePeriod"),
												Values.iri(fhirInstance + "Period" + counter));
										String startDateTime = startDateTimeNode.asText();
										builder.subject(fhirInstance + startDateTime).add(RDF.TYPE,
												Values.iri(fhir + "dateTime"));
										builder.subject(fhirInstance + "Period" + counter)
												.add(RDF.TYPE, Values.iri(fhir + "Period"))
												.add(Values.iri(fhir + "Observation.effectivePeriod.start"),
														Values.iri(fhirInstance + startDateTime));
									}
									if (endDateTimeNode != null && !endDateTimeNode.isNull()) {
										builder.subject(fhirInstance + observationInstanceName).add(fhir + "Observation.effectivePeriod",
												Values.iri(fhirInstance + "Period" + counter));
										String endDateTime = endDateTimeNode.asText();
										builder.subject(fhirInstance + endDateTime).add(RDF.TYPE,
												Values.iri(fhir + "dateTime"));
										builder.subject(fhirInstance + "Period" + counter)
												.add(RDF.TYPE, Values.iri(fhir + "Period"))
												.add(Values.iri(fhir +"Observation.effectivePeriod.end"),
														Values.iri(fhirInstance + endDateTime));

									}
								} else {
									if (startDateTimeNode != null && !startDateTimeNode.isNull()) {
										builder.subject(fhirInstance + observationInstanceName).add(fhir + "Observation.effectivePeriod",
												Values.iri(fhirInstance + "Period" + counter));
										String startDateTime = startDateTimeNode.asText();
										builder.subject(fhirInstance + startDateTime).add(RDF.TYPE,
												Values.iri(fhir + "dateTime"));
										builder.subject(fhirInstance + "Period" + counter)
												.add(RDF.TYPE, Values.iri(fhir + "Period"))
												.add(Values.iri(fhir + "Observation.effectivePeriod.start"),
														Values.iri(fhirInstance + startDateTime));
									}
									if (endDateTimeNode != null && !endDateTimeNode.isNull()) {
										builder.subject(fhirInstance + observationInstanceName).add(fhir + "Observation.effectivePeriod",
												Values.iri(fhirInstance + "Period" + counter));
										String endDateTime = endDateTimeNode.asText();
										builder.subject(fhirInstance + endDateTime).add(RDF.TYPE,
												Values.iri(fhir + "dateTime"));
										builder.subject(fhirInstance + "Period" + counter).add(
												Values.iri(fhir + "Observation.effectivePeriod.end"),
												Values.iri(fhirInstance + endDateTime));

									}
								}
							} else {
								
								JsonNode effectiveTimeNode = effectiveTimeFrameNode.get("date_time");

								//effective_time_frame.dateTime
								if (effectiveTimeNode != null) {
									builder.subject(fhirInstance + observationInstanceName).add(fhir + "Observation.effectiveDateTime",
											Values.iri(fhirInstance + effectiveTimeNode.asText()));
									
									builder.subject(fhirInstance + effectiveTimeNode.asText()).add(RDF.TYPE,
											Values.iri(fhir+"dateTime"));
								}
								
								else {
									builder.subject(fhirInstance + observationInstanceName).add(fhir + "Observation.effectiveDateTime",
											Values.iri(fhirInstance + "Period" + counter));
									JsonNode startDateTimeNode = effectiveTimeFrameNode.get("start_date_time");
									JsonNode endDateTimeNode = effectiveTimeFrameNode.get("end_date_time");
									
									//effective_time_frame.start_date_time
									if (startDateTimeNode != null && !startDateTimeNode.isNull()) {
										String startDateTime = startDateTimeNode.asText();
										builder.subject(fhirInstance + "Period" + counter)
												.add(Values.iri(fhir + "Observation.effectivePeriodPeriod.start"), startDateTime);

									}
									
									//effective_time_frame.end_date_time
									if (endDateTimeNode != null && !endDateTimeNode.isNull()) {
										String endDateTime = endDateTimeNode.asText();
										builder.subject(fhirInstance + "Observation.effectivePeriodPeriod" + counter)
												.add(Values.iri(fhir + "Observation.effectivePeriod.end"), endDateTime);

									}	
								}
							}
						}
					} else if (componentCodeableConcepts.contains(fieldName)) {
						componentMappingUtil(observationInstanceName, configurationCodes,  fieldValue.asText().replace(" ", "_"), fieldName.replace(" ", "_"));
					} else if (componentsWithValues.contains(fieldName)) {
						componentMappingWithValueUtil(observationInstanceName, configurationCodes,  fieldValue, fieldName.replace(" ", "_"));
					}else if (fieldName == "measurement_location" || fieldName == "sensor_body_location") {
						bodySiteUtil(observationInstanceName, configurationCodes, fieldName, fieldValue.asText()); 
					}else if (codesWithStringValues.contains(fieldName)) {
						codeUtil(observationInstanceName, fieldValue.asText().replace(" ", "_"), fieldName);
					}else if (fieldName == "specimen_source" ) {
						builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.specimen"),
								Values.iri(fhirInstance + "Reference_" + fieldValue.asText().replace(" ", "_")));
						
						builder.subject(fhirInstance + "Reference_" + fieldName.toString()).add(RDF.TYPE,Values.iri(fhir + "Reference"));
						
						builder.subject(fhirInstance + "Reference_" + fieldValue.toString().replace(" ", "_")).add(fhir+"Reference.display",Values.iri( fhirInstance +patientId));
						builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("system").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));
						builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("display").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));		
						builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("code").asText()).add(RDF.TYPE, Values.iri(fhir + "code"));

						builder.subject(fhirInstance + "Reference_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
								Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("code").asText()));
						
						builder.subject(fhirInstance + "Reference_" + fieldName + "_" + counter).add(Values.iri(fhir + "display"),
								Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("display").asText()));
						
						builder.subject(fhirInstance + "Reference_" + fieldName + "_" + counter).add(Values.iri(fhir + "system"),
								Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue.asText().replace(" ", "_")).get("system").asText()));
						
						
					
					}
					else if (fieldName == "id") {
						builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.subject"),
								Values.iri(fhirInstance + "Patient_" + patientId));
						
						builder.subject(fhirInstance + "Patient_" + patientId).add(RDF.TYPE,Values.iri(fhir + "Patient"));
						builder.subject(fhirInstance + "Patient_" + patientId).add(fhir+"Patient.identifier",Values.iri( fhirInstance +patientId));
											}else {
				        System.out.println(fieldName + ": not supported");
						continue;
				    }

			}
		    counter++;
		  }
		// Build the RDF model
		Model model = builder.build();

		OutputStream outputStream = System.out;
		Rio.write(model, outputStream, RDFFormat.TURTLE);

        // Call the createTurtleFile method
        GenerateFiles.createTurtleFile(model, observationInstanceName);
  
		} catch (IOException e) {
			e.printStackTrace();
		}	
		String fileNameObservation;
		String outputDirectory = "src/main/resources/output/";
	    fileNameObservation = outputDirectory + observationInstanceName + ".ttl";
	     
		new GraphDB(fileNameObservation, observation);
        }
	}
	
	static void observationNameUtil(String observation, String observationInstanceName, JsonNode configurationCodes) {
		builder.subject(fhirInstance + observationInstanceName).add(RDF.TYPE, Values.iri(fhir + "Observation"))
		.add(Values.iri(fhir + "Observation.code"),
				Values.iri(fhirInstance + "Coding_" + configurationCodes.get("observation_codes").get(observation).get("code").asText()));
		
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(observation).get("code").asText())
		.add(RDF.TYPE,Values.iri(fhir + "code"));

		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(observation).get("system").asText())
		.add(RDF.TYPE,Values.iri(fhir + "string"));
	
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(observation).get("display").asText())
		.add(RDF.TYPE,Values.iri(fhir + "string"));

		builder.subject(fhirInstance + "Coding_" + configurationCodes.get("observation_codes").get(observation).get("code").asText())
					.add(Values.iri(fhir + "code"),
							Values.iri(fhirInstance, configurationCodes.get("observation_codes").get(observation).get("code").asText()))
					.add(Values.iri(fhir + "display"),
							Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(observation).get("display").asText()))
					.add(Values.iri(fhir + "system"), Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(observation).get("system").asText()));
			

    }
	
	
	static void observationNameWithValueUtil(String observation, String observationInstanceName, JsonNode configurationCodes, JsonNode fieldValue) {

		observationNameUtil(observation, observationInstanceName, configurationCodes);
		builder.subject(fhirInstance + observationInstanceName).add(RDF.TYPE, Values.iri(fhir + "Observation"));
		
		builder.subject(fhirInstance + observationInstanceName)
		.add(Values.iri(fhir + "Observation.valueQuantity"), Values.iri(fhirInstance + "Quantity" + counter));

		quantityMappingUtil(observationInstanceName, configurationCodes, fieldValue);
		
    }
	
	static void codeUtil(String observationInstanceName, String fieldValue, String fieldName) {

		builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.code"),
				Values.iri(fhirInstance + "code_" + fieldName + "_" + counter));


		builder.subject(fhirInstance + "code_" + fieldName + "_" + counter).add(Values.iri(fhir + "coding"),
				Values.iri(fhirInstance + "Coding_" + fieldValue));

		builder.subject(fhirInstance + fieldValue).add(RDF.TYPE,
				Values.iri(fhir + "code"));

		builder.subject(fhirInstance + fieldValue.toUpperCase()).add(RDF.TYPE,
				Values.iri(fhir + "string"));
		
		builder.subject(fhirInstance + "omh_fhir_observation_codes").add(RDF.TYPE,
				Values.iri(fhir + "string"));
		
		builder.subject(fhirInstance+ "Coding_" + fieldValue)
				.add(Values.iri(fhir + "Coding.system"), Values.iri(fhirInstance +  "omh_fhir_observation_codes"))
				.add(Values.iri(fhir + "Coding.code"),
						Values.iri(fhirInstance + fieldValue))
				.add(Values.iri(fhir + "Coding.display"),
						Values.iri(fhirInstance +fieldValue.toUpperCase()));
		
    }
	
	static void quantityMappingUtil(String observationInstanceName,JsonNode configurationCodes, JsonNode fieldValue) {
				
		fieldValue.fields().forEachRemaining(entry -> {
		String fieldName2 = entry.getKey();
		JsonNode fieldValue2 = entry.getValue();
		
		if (fieldName2 == "unit") {
            try {
				String encodedParameter = URLEncoder.encode(fieldValue2.asText(), "UTF-8");

				builder.subject(fhirInstance + encodedParameter)
				.add(RDF.TYPE, Values.iri(fhir + "string"));
				
            	
				builder.subject(fhirInstance + "Quantity" + counter)
						.add(RDF.TYPE, Values.iri(fhir + "Quantity"))
						.add(Values.iri(fhir + "Quantity." + fieldName2),
								Values.iri(fhirInstance + encodedParameter));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else {
			builder.subject(fhirInstance + fieldValue2)
			.add(RDF.TYPE, Values.iri(fhir + "decimal"));
			
			builder.subject(fhirInstance + "Quantity" + counter)
					.add(Values.iri(fhir + "Quantity." + fieldName2),
							Values.iri(fhirInstance + fieldValue2));
		}
	});		
  }
	
	static void componentMappingUtil(String observationInstanceName, JsonNode configurationCodes, String fieldValue, String fieldName) {

		//component
		builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.component"),
				Values.iri(fhirInstance + "component_" + fieldName + "_" + counter));
		
		
		//component.code
		builder.subject(fhirInstance + "component_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + "Code_" + fieldName + "_" + counter));
		
										
		//component.code.coding
		builder.subject(fhirInstance + "Code_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + "Coding_" + fieldName + "_" + counter));

		// ./src/main/resources/data/body_temperature
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("system").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("display").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));		
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("code").asText()).add(RDF.TYPE, Values.iri(fhir + "code"));

		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("code").asText()));
		
		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "display"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("display").asText()));
		
		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "system"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("system").asText()));
		
		
		//component.valueCodeableConcept
		builder.subject(fhirInstance + "component_" + fieldName + "_" + counter).add(Values.iri(fhir + "valueCodeableConcept"),
				Values.iri(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter));
		
		
		codeableConceptUtil(observationInstanceName, configurationCodes,  fieldValue,  fieldName);
	}
	
	
	static void codeableConceptUtil(String observationInstanceName, JsonNode configurationCodes, String fieldValue, String fieldName) {

		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("system").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("display").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));		
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("code").asText()).add(RDF.TYPE, Values.iri(fhir + "code"));
	
		//valueCodeableConcept.coding
		builder.subject(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter).add(Values.iri(fhir + "coding"),
						Values.iri(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter));
	
		builder.subject(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("code").asText()));
		
		builder.subject(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "display"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("display").asText()));
		
		builder.subject(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "system"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("system").asText()));

		//component.valueCodeableConcept.coding
		builder.subject(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter).add(Values.iri(fhir + "text"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("options").get(fieldValue).get("display").asText().toUpperCase()));

    }
	
		
	static void componentMappingWithValueUtil(String observationInstanceName, JsonNode configurationCodes, JsonNode fieldValue, String fieldName) {

		//component
		builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.component"),
				Values.iri(fhirInstance + "component_" + fieldName + "_" + counter));
		
		
		//component.code
		builder.subject(fhirInstance + "component_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + "Code_" + fieldName + "_" + counter));
		
										
		//component.code.coding
		builder.subject(fhirInstance + "Code_" + fieldName + "_" + counter).add(Values.iri(fhir + "coding"),
				Values.iri(fhirInstance + "Coding_" + fieldName + "_" + counter));
		
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("system").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("display").asText()).add(RDF.TYPE, Values.iri(fhir + "string"));		
		builder.subject(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("code").asText()).add(RDF.TYPE, Values.iri(fhir + "code"));

		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("code").asText()));
		
		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "display"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("display").asText()));
		
		builder.subject(fhirInstance + "Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "system"),
				Values.iri(fhirInstance + configurationCodes.get("observation_codes").get(fieldName).get("system").asText()));
	
	
		builder.subject(fhirInstance + "component_" + fieldName + "_" + counter)
		.add(Values.iri(fhir + "component.valueQuantity"), Values.iri(fhirInstance + "Quantity" + counter));

		//component.valueQuantity
		quantityMappingUtil(observationInstanceName, configurationCodes, fieldValue);

    }
	
	static void bodySiteUtil(String observationInstanceName, JsonNode configurationCodes, String fieldName, String fieldValue) {
		builder.subject(fhirInstance + "CodeableConcept_" + fieldName)
		.add(Values.iri(fhir + "text"),
				Values.iri(fhirInstance + fieldValue.replace(" ", "_").toUpperCase()));
		
		builder.subject(fhirInstance + "CodeSystem_body_location")
		.add(RDF.TYPE,Values.iri(fhir+"string"));

		builder.subject(fhirInstance +  fieldValue.toString().replace(" ", "_"))
		.add(RDF.TYPE,Values.iri(fhir+"code"));
		
		builder.subject(fhirInstance + fieldValue.toString().replace(" ", "_").toUpperCase())
		.add(RDF.TYPE,Values.iri(fhir+"string"));
		
		
		//bodySite
		builder.subject(fhirInstance + observationInstanceName).add(Values.iri(fhir + "Observation.bodySite"),
				Values.iri(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter));
				
		//bodySite.coding
		builder.subject(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter).add(Values.iri(fhir + "coding"),
						Values.iri(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter));
	
		builder.subject(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "code"),
				Values.iri(fhirInstance +  fieldValue.toString().replace(" ", "_")));
			
		builder.subject(fhirInstance + "CodeableConcept_Coding_" + fieldName + "_" + counter).add(Values.iri(fhir + "system"),
				Values.iri(fhirInstance + "CodeSystem_body_location"));
		
		builder.subject(fhirInstance + "CodeableConcept_" + fieldName + "_" + counter).add(Values.iri(fhir + "display"),
				Values.iri(fhirInstance + fieldValue.toString().replace(" ", "_").toUpperCase()));


		
    }

}
