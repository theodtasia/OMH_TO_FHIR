# Open mHealth (OMH) to FHIR Mapping Tool

## Overview

The OMH to FHIR Mapping Tool is a robust utility designed in Java to facilitate the seamless integration of health-related data by translating Open mHealth (OMH) data schemas to the Fast Healthcare Interoperability Resources (FHIR) standard. This tool provides a comprehensive solution for harmonizing and standardizing health data, ensuring interoperability, and adherence to industry standards.

## Features

- **OMH to FHIR Mapping:** Effortlessly translate OMH data schemas to FHIR, ensuring compatibility and consistency.
- **User-Friendly Interface:** An intuitive menu-driven interface guides users through the mapping process, making it accessible to both technical and non-technical users.
- **Schema Selection:** Users can handpick specific schemas tailored to distinct health data types, enabling a customized mapping experience.
- **Validation with SHACL:** Utilizes the Shapes Constraint Language (SHACL) for data validation, ensuring adherence to predefined standards.
- **RDF Output:** Generates individual Turtle files for each mapped data, encapsulating the information in a standardized RDF format compatible with FHIR.
- **Graph Database Integration:** Seamlessly uploads RDF data to a GraphDB for efficient storage and retrieval.

## Getting Started

### Prerequisites

- [Java](https://www.java.com/en/) installed on your system.
- [Maven](https://maven.apache.org/download.cgi) installed on your system.

### Example Mapping

Heart Rate schema
``` json
{
    "heart_rate": {
        "value": 75,
        "unit": "beats/min"
    },
    "id": "Jonh_K_12",
    "descriptive_statistic": "average",
    "temporal_relationship_to_physical_activity": "before exercise",
    "temporal_relationship_to_sleep": "before sleeping"
}
```

TOOL RDF OUTPUT in FHIR

```
@prefix fhir: <http://hl7.org/fhir/> .
@prefix fhir_instance: <http://hl7.org/fhir_instances/> .
@prefix schema: <http://schema.org/> .

fhir_instance:heart_rate_Jonh_K_12 a fhir:Observation;
  fhir:Observation.code fhir_instance:Coding_8867-4, fhir_instance:code_descriptive_statistic_2;
  fhir:Observation.valueQuantity fhir_instance:Quantity0;
  fhir:Observation.subject fhir_instance:Patient_Jonh_K_12;
  fhir:Observation.component fhir_instance:component_temporal_relationship_to_physical_activity_3,
    fhir_instance:component_temporal_relationship_to_sleep_4 .

fhir_instance:8867-4 a fhir:code .

fhir_instance:loinc a fhir:string .

fhir_instance:Heart_rate a fhir:string .

fhir_instance:Coding_8867-4 fhir:code fhir_instance:8867-4;
  fhir:display fhir_instance:Heart_rate;
  fhir:system fhir_instance:loinc .

fhir_instance:75 a fhir:decimal .

fhir_instance:Quantity0 a fhir:Quantity;
  fhir:Quantity.value fhir_instance:75;
  fhir:Quantity.unit fhir_instance:beats%2Fmin .

fhir_instance:beats%2Fmin a fhir:string .

fhir_instance:Patient_Jonh_K_12 a fhir:Patient;
  fhir:Patient.identifier fhir_instance:Jonh_K_12 .

fhir_instance:code_descriptive_statistic_2 fhir:coding fhir_instance:Coding_average .

fhir_instance:average a fhir:code .

fhir_instance:AVERAGE a fhir:string .

fhir_instance:omh_fhir_observation_codes a fhir:string .

fhir_instance:Coding_average fhir:Coding.system fhir_instance:omh_fhir_observation_codes;
  fhir:Coding.code fhir_instance:average;
  fhir:Coding.display fhir_instance:AVERAGE .

fhir_instance:component_temporal_relationship_to_physical_activity_3 fhir:code fhir_instance:Code_temporal_relationship_to_physical_activity_3;
  fhir:valueCodeableConcept fhir_instance:CodeableConcept_temporal_relationship_to_physical_activity_3 .

fhir_instance:Code_temporal_relationship_to_physical_activity_3 fhir:code fhir_instance:Coding_temporal_relationship_to_physical_activity_3 .

fhir_instance:SNOMEDCT a fhir:string .

fhir_instance:Before_exercise a fhir:string .

fhir_instance:307166007 a fhir:code .

fhir_instance:Coding_temporal_relationship_to_physical_activity_3 fhir:code fhir_instance:307166007;
  fhir:display fhir_instance:Before_exercise;
  fhir:system fhir_instance:SNOMEDCT .

fhir_instance:CodeableConcept_temporal_relationship_to_physical_activity_3 fhir:coding
    fhir_instance:CodeableConcept_Coding_temporal_relationship_to_physical_activity_3;
  fhir:text fhir_instance:BEFORE_EXERCISE .

fhir_instance:CodeableConcept_Coding_temporal_relationship_to_physical_activity_3
  fhir:code fhir_instance:307166007;
  fhir:display fhir_instance:Before_exercise;
  fhir:system fhir_instance:SNOMEDCT .

fhir_instance:component_temporal_relationship_to_sleep_4 fhir:code fhir_instance:Code_temporal_relationship_to_sleep_4;
  fhir:valueCodeableConcept fhir_instance:CodeableConcept_temporal_relationship_to_sleep_4 .

fhir_instance:Code_temporal_relationship_to_sleep_4 fhir:code fhir_instance:Coding_temporal_relationship_to_sleep_4 .

fhir_instance:Before_sleeping a fhir:string .

fhir_instance:307155000 a fhir:code .

fhir_instance:Coding_temporal_relationship_to_sleep_4 fhir:code fhir_instance:307155000;
  fhir:display fhir_instance:Before_sleeping;
  fhir:system fhir_instance:SNOMEDCT .

fhir_instance:CodeableConcept_temporal_relationship_to_sleep_4 fhir:coding fhir_instance:CodeableConcept_Coding_temporal_relationship_to_sleep_4;
  fhir:text fhir_instance:BEFORE_SLEEPING .

fhir_instance:CodeableConcept_Coding_temporal_relationship_to_sleep_4 fhir:code fhir_instance:307155000;
  fhir:display fhir_instance:Before_sleeping;
  fhir:system fhir_instance:SNOMEDCT .
```
