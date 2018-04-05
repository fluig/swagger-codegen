package io.swagger.codegen.languages;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidFluigSDKClientCodegen extends DefaultCodegen implements CodegenConfig {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidFluigSDKClientCodegen.class);
    
    protected String utilsPackage = "sdk.fluig.com.api.utils";
    
    protected String projectFolder = "src/main";
    
    protected String sourceFolder = projectFolder + "/java";
    
    protected Boolean serializableModel = false;

    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    public AndroidFluigSDKClientCodegen() {
        super();
        outputFolder = "generated-code/fluig";
        modelTemplateFiles.put("model.mustache", ".java");
        apiTemplateFiles.put("api.mustache", ".java");
        embeddedTemplateDir = templateDir = "android-fluig-sdk";
        apiTestTemplateFiles.put("api_test.mustache", ".java");

        //apiPackage = "sdk.fluig.com.api.rest.v1";
        //modelPackage = "sdk.fluig.com.apireturns.pojos.v1";

        apiPackage = "sdk.fluig.com.api.rest";
        modelPackage = "sdk.fluig.com.apireturns.pojos";
        testPackage = "test.sdk.fluig.com.api.rest";
        
        setReservedWordsLowerCase(
                Arrays.asList(
                    // local variable names used in API methods (endpoints)
                    "localVarPostBody", "localVarPath", "localVarQueryParams", "localVarHeaderParams",
                    "localVarFormParams", "localVarContentTypes", "localVarContentType",
                    "localVarResponse", "localVarBuilder", "authNames", "basePath", "apiMapperUtils",

                    // due to namespace collusion
                    "Object",

                    // android reserved words
                    "abstract", "continue", "for", "new", "switch", "assert",
                    "default", "if", "package", "synchronized", "boolean", "do", "goto", "private",
                    "this", "break", "double", "implements", "protected", "throw", "byte", "else",
                    "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
                    "catch", "extends", "int", "short", "try", "char", "final", "interface", "static",
                    "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
                    "native", "super", "while", "null")
        );

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "String",
                        "boolean",
                        "Boolean",
                        "Double",
                        "Integer",
                        "Long",
                        "Float",
                        "byte[]",
                        "Object")
        );
        instantiationTypes.put("array", "ArrayList");
        instantiationTypes.put("map", "HashMap");
        typeMapping.put("date", "Date");
        typeMapping.put("file", "File");

        cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.INVOKER_PACKAGE, CodegenConstants.INVOKER_PACKAGE_DESC));
        cliOptions.add(new CliOption(CodegenConstants.GROUP_ID, "groupId for use in the generated build.gradle and pom.xml"));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_ID, "artifactId for use in the generated build.gradle and pom.xml"));
        cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_VERSION, "artifact version for use in the generated build.gradle and pom.xml"));
        cliOptions.add(new CliOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC));

        cliOptions.add(CliOption.newBoolean(CodegenConstants.SERIALIZABLE_MODEL, CodegenConstants.SERIALIZABLE_MODEL_DESC));

        cliOptions.add(new CliOption(CodegenConstants.SOURCE_FOLDER, CodegenConstants.SOURCE_FOLDER_DESC));

        supportedLibraries.put("volley", "HTTP client: Volley 1.0.19 (default)");
        supportedLibraries.put("httpclient", "HTTP client: Apache HttpClient 4.3.6. JSON processing: Gson 2.3.1. IMPORTANT: Android client using HttpClient is not actively maintained and will be depecreated in the next major release.");
        CliOption library = new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
        library.setEnum(supportedLibraries);
        cliOptions.add(library);
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "fluig";
    }

    @Override
    public String getHelp() {
        return "Generates an Android client library.";
    }

    @Override
    public String escapeReservedWord(String name) {           
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace( '/', File.separatorChar );
    }

    @Override
    public String modelDocFileFolder() {
        return ( outputFolder + "/" + modelDocPath ).replace( '/', File.separatorChar );
    }

    @Override
    public String toApiDocFilename( String name ) {
        return toApiName( name );
    }

    @Override
    public String toModelDocFilename( String name ) {
        return toModelName( name );
    }

    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();

            return getSwaggerType(p) + "<String, " + getTypeDeclaration(inner) + ">";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type) || type.indexOf(".") >= 0 ||
                type.equals("Map") || type.equals("List") ||
                type.equals("File") || type.equals("Date")) {
                return type;
            }
        } else {
            type = swaggerType;
        }
        return toModelName(type);
    }

    @Override
    public String toVarName(String name) {
        // sanitize name
        name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);

        // for reserved word or word starting with number, append _
        if (isReservedWord(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        // add prefix, suffix if needed
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        name = camelize(sanitizeName(name));

        // model name cannot use reserved keyword, e.g. return
        if (isReservedWord(name)) {
            String modelName = "Model" + name;
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            String modelName = "Model" + name; // e.g. 200Response => Model200Response (after camelize)
            LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + modelName);
            return modelName;
        }

        return name;
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        String example;

        if (p.defaultValue == null) {
            example = p.example;
        } else {
            example = p.defaultValue;
        }

        String type = p.baseType;
        if (type == null) {
            type = p.dataType;
        }

        if ("String".equals(type)) {
            if (example == null) {
                example = p.paramName + "_example";
            }
            example = "\"" + escapeText(example) + "\"";
        } else if ("Integer".equals(type) || "Short".equals(type)) {
            if (example == null) {
                example = "56";
            }
        } else if ("Long".equals(type)) {
            if (example == null) {
                example = "56";
            }
            example = example + "L";
        } else if ("Float".equals(type)) {
            if (example == null) {
                example = "3.4";
            }
            example = example + "F";
        } else if ("Double".equals(type)) {
            example = "3.4";
            example = example + "D";
        } else if ("Boolean".equals(type)) {
            if (example == null) {
                example = "true";
            }
        } else if ("File".equals(type)) {
            if (example == null) {
                example = "/path/to/file";
            }
            example = "new File(\"" + escapeText(example) + "\")";
        } else if ("Date".equals(type)) {
            example = "new Date()";
        } else if (!languageSpecificPrimitives.contains(type)) {
            // type is a model class, e.g. User
            example = "new " + type + "()";
        }

        if (example == null) {
            example = "null";
        } else if (Boolean.TRUE.equals(p.isListContainer)) {
            example = "Arrays.asList(" + example + ")";
        } else if (Boolean.TRUE.equals(p.isMapContainer)) {
            example = "new HashMap()";
        }

        p.example = example;
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        operationId = camelize(sanitizeName(operationId), true);

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            String newOperationId = camelize("call_" + operationId, true);
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + newOperationId);
            return newOperationId;
        }

        return operationId;
    }

    @Override
    public void processOpts() {
    	
        super.processOpts();

        if (additionalProperties.containsKey("version")) {
        	apiPackage = apiPackage + "." + additionalProperties.get("version");
        	modelPackage = modelPackage + "." + additionalProperties.get("version");
        	testPackage = testPackage + "." + additionalProperties.get("version");
        }
        
        supportingFiles.add(new SupportingFile("apiMapperUtils.mustache",
                (sourceFolder + File.separator + utilsPackage).replace(".", File.separator), "ApiMapperUtils.java"));

    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public void setSerializableModel(Boolean serializableModel) {
        this.serializableModel = serializableModel;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

}
