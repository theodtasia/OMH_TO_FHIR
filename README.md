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

