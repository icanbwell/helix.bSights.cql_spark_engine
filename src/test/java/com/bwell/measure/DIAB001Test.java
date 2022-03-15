package com.bwell.measure;

import com.bwell.core.entities.*;
import com.bwell.services.domain.*;
import com.bwell.utilities.Utilities;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.exception.CqlException;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DIAB001Test {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private static final String fhirVersion = "R4";
    private static final String modelName = "FHIR";
    private static final String fhirServerUrl = "http://localhost:3000/4_0_0";
    private static final String folder = "diab001";
    private static final String libraryName = "DIAB001";
    private static final String libraryVersion = "1.0.0";
    private static final String testResourceRelativePath = "src/test/resources";
    private static String testResourcePath = null;
    private static String terminologyPath = null;
    private static String cqlPath = null;
    private static String bundleJson = null;
    private static String bundleContainedJson = null;

    @BeforeClass
    public void setup() {
        File file = new File(testResourceRelativePath);
        testResourcePath = file.getAbsolutePath();
        System.out.printf("Test resource directory: %s%n", testResourcePath);

        terminologyPath = testResourcePath + "/" + folder + "/terminology";
        cqlPath = testResourcePath + "/" + folder + "/cql";

        bundleJson = Utilities.getBundle(testResourcePath, folder);
        bundleContainedJson = Utilities.getContainedBundle(testResourcePath, folder);
    }

    @BeforeMethod
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterMethod
    public void restoreStreams() {
        String sysOut = outContent.toString();
        String sysError = errContent.toString();

        System.setOut(originalOut);
        System.setErr(originalErr);

        System.out.println(sysOut);
        System.err.println(sysError);
    }

    @Test
    public void testDIAB001Bundle() {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.terminologyUrl = terminologyPath;
        libraryParameter.libraryUrl = cqlPath;
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleJson;

        libraries.add(libraryParameter);

        EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

        Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

        for (Map.Entry<String, Object> libraryEntry : entrySet) {

            String key = libraryEntry.getKey();
            Object value = libraryEntry.getValue();

            if (key.equals("Patient")) {

                Patient patient = (Patient) value;

                // medical record number
                String medicalRecordId = patient.getIdentifier().get(0).getValue();
                System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                assertEquals(medicalRecordId, "12345");

                // patient id
                String patientId = patient.getId();
                System.out.println(key + ": Patient ID = " + patientId);
                assertEquals(patientId, "1");

                // patient active flag
                boolean isActive = patient.getActive();
                System.out.println(key + ": Patient Active = " + isActive);
                assertTrue(isActive);
            }

            if (key.equals("InObservationCohort")) {
                Boolean isInObservationCohort = (Boolean) value;
                System.out.println(key + ": " + isInObservationCohort);
                assertTrue(isInObservationCohort);
            }

            if (key.equals("InDemographic")) {
                Boolean isInDemographic = (Boolean) value;
                System.out.println(key + ": " + isInDemographic);
                assertTrue(isInDemographic);
            }

            System.out.println(key + "=" + tempConvert(value));

        }

        System.out.println();
    }

    @Test
    public void testDIAB001BundleTerminologyFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.libraryUrl = cqlPath;
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleJson;

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if (key.equals("Patient")) {

                    Patient patient = (Patient) value;

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "1");

                }

                if (key.equals("InObservationCohort")) {
                    Boolean isInObservationCohort = (Boolean) value;
                    System.out.println(key + ": " + isInObservationCohort);
                    assertTrue(isInObservationCohort);
                }

                if (key.equals("InDemographic")) {
                    Boolean isInDemographic = (Boolean) value;
                    System.out.println(key + ": " + isInDemographic);
                    assertTrue(isInDemographic);
                }

                System.out.println(key + "=" + tempConvert(value));

            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            }
            else {
                throw e;
            }

        }

        System.out.println();
    }

    @Test
    public void testDIAB001BundleCqlFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.terminologyUrl = terminologyPath;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleJson;

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if (key.equals("Patient")) {

                    Patient patient = (Patient) value;

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "1");

                }

                if (key.equals("InObservationCohort")) {
                    Boolean isInObservationCohort = (Boolean) value;
                    System.out.println(key + ": " + isInObservationCohort);
                    assertTrue(isInObservationCohort);
                }

                if (key.equals("InDemographic")) {
                    Boolean isInDemographic = (Boolean) value;
                    System.out.println(key + ": " + isInDemographic);
                    assertTrue(isInDemographic);
                }

                System.out.println(key + "=" + tempConvert(value));

            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            }
            else {
                throw e;
            }

        }

        System.out.println();
    }

    @Test
    public void testDIAB001BundleCqlAndTerminologyFromFhirServer() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleJson;

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if (key.equals("Patient")) {

                    Patient patient = (Patient) value;

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "1");

                }

                if (key.equals("InObservationCohort")) {
                    Boolean isInObservationCohort = (Boolean) value;
                    System.out.println(key + ": " + isInObservationCohort);
                    assertTrue(isInObservationCohort);
                }

                if (key.equals("InDemographic")) {
                    Boolean isInDemographic = (Boolean) value;
                    System.out.println(key + ": " + isInDemographic);
                    assertTrue(isInDemographic);
                }

                System.out.println(key + "=" + tempConvert(value));

            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            }
            else {
                throw e;
            }

        }

        System.out.println();
    }

    @Test
    public void testDIAB001BundleCqlAndTerminologyFromFhirServerWithContainedResources() throws Exception {

        ModelParameter modelParameter = new ModelParameter();
        List<LibraryParameter> libraries = new ArrayList<>();

        LibraryParameter libraryParameter = new LibraryParameter();
        libraryParameter.terminologyUrl = fhirServerUrl;
        libraryParameter.libraryUrl = fhirServerUrl;
        libraryParameter.libraryName = libraryName;
        libraryParameter.libraryVersion = libraryVersion;
        libraryParameter.model = modelParameter;
        libraryParameter.model.modelName = modelName;
        libraryParameter.model.modelBundle = bundleContainedJson;

        libraries.add(libraryParameter);

        try {

            EvaluationResult result = new CqlService().runCqlLibrary(fhirVersion, libraries);

            Set<Map.Entry<String, Object>> entrySet = result.expressionResults.entrySet();

            for (Map.Entry<String, Object> libraryEntry : entrySet) {

                String key = libraryEntry.getKey();
                Object value = libraryEntry.getValue();

                if (key.equals("Patient")) {

                    Patient patient = (Patient) value;

                    // medical record number
                    String medicalRecordId = patient.getIdentifier().get(0).getValue();
                    System.out.println(key + ": Medical Record ID = " + medicalRecordId);
                    assertEquals(medicalRecordId, "12345");

                    // patient id
                    String patientId = patient.getId();
                    System.out.println(key + ": Patient ID = " + patientId);
                    assertEquals(patientId, "1");

                }

//                if (key.equals("InObservationCohort")) {
//                    Boolean isInObservationCohort = (Boolean) value;
//                    System.out.println(key + ": " + isInObservationCohort);
//                    assertTrue(isInObservationCohort);
//                }
//
//                if (key.equals("InDemographic")) {
//                    Boolean isInDemographic = (Boolean) value;
//                    System.out.println(key + ": " + isInDemographic);
//                    assertTrue(isInDemographic);
//                }

                System.out.println(key + "=" + tempConvert(value));

            }
        } catch (CqlException e) {
            if (Objects.equals(e.getMessage(), "Unexpected exception caught during execution: ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException: HTTP 404 Not Found")) {
                throw new Exception("NOTE: Did you run make loadfhir to load the fhir server?");
            }
            else {
                throw e;
            }

        }

        System.out.println();
    }

    private String tempConvert(Object value) {
        if (value == null) {
            return "null";
        }

        String result = "";
        if (value instanceof Iterable) {
            result += "[";
            Iterable<?> values = (Iterable<?>) value;
            for (Object o : values) {

                result += (tempConvert(o) + ", ");
            }

            if (result.length() > 1) {
                result = result.substring(0, result.length() - 2);
            }

            result += "]";
        } else if (value instanceof IBaseResource) {
            IBaseResource resource = (IBaseResource) value;
            result = resource.fhirType() + (resource.getIdElement() != null && resource.getIdElement().hasIdPart()
                    ? "(id=" + resource.getIdElement().getIdPart() + ")"
                    : "");
        } else if (value instanceof IBase) {
            result = ((IBase) value).fhirType();
        } else if (value instanceof IBaseDatatype) {
            result = ((IBaseDatatype) value).fhirType();
        } else {
            result = value.toString();
        }

        return result;
    }

}
