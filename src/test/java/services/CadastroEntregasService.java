package services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import model.EntregaModel;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import static io.restassured.RestAssured.given;

public class CadastroEntregasService {

    final EntregaModel entregaModel = new EntregaModel();
    public final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    public Response response;
    String baseUrl = "http://localhost:8080";

    // Declaração da variável idDelivery
    private String idDelivery;

    public void setFieldsDelivery(String field, String value) {
        switch (field) {
            case "numeroPedido" -> entregaModel.setNumeroPedido(Integer.parseInt(value));
            case "nomeEntregador" -> entregaModel.setNomeEntregador(value);
            case "statusEntrega" -> entregaModel.setStatusEntrega(value);
            case "dataEntrega" -> entregaModel.setDataEntrega(value);
            default -> throw new IllegalStateException("Unexpected field: " + field);
        }
    }

    public void createDelivery(String endPoint) {
        String url = baseUrl + endPoint;
        String bodyToSend = gson.toJson(entregaModel);
        response = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyToSend)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public void retrieveIdDelivery() {
        // Armazenar o ID da entrega
        idDelivery = String.valueOf(gson.fromJson(response.jsonPath().prettify(), EntregaModel.class).getNumeroEntrega());
    }

    public void deleteDelivery(String endPoint) {
        // Verificar se o idDelivery foi recuperado antes de tentar deletar
        if (idDelivery == null || idDelivery.isEmpty()) {
            throw new IllegalStateException("ID da entrega não recuperado. Certifique-se de que retrieveIdDelivery() foi chamado.");
        }

        String url = String.format("%s%s/%s", baseUrl, endPoint, idDelivery);
        response = given()
                .accept(ContentType.JSON)
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }
    String schemasPath = "src/test/resources/schemas/";
    JSONObject jsonSchema;
    private final ObjectMapper mapper = new ObjectMapper();

    private JSONObject loadJsonFromFile(String filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            JSONTokener tokener = new JSONTokener(inputStream);
            return new JSONObject(tokener);
        }
    }
    public void setContract(String contract) throws IOException {
        switch (contract) {
            case "Cadastro bem-sucedido de entrega" -> jsonSchema = loadJsonFromFile(schemasPath + "cadastro-bem-sucedido-de-entrega.json");
            default -> throw new IllegalStateException("Unexpected contract" + contract);
        }
    }
    public Set<ValidationMessage> validateResponseAgainstSchema() throws IOException
    {
        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        JsonSchema schema = schemaFactory.getSchema(jsonSchema.toString());
        JsonNode jsonResponseNode = mapper.readTree(jsonResponse.toString());
        Set<ValidationMessage> schemaValidationErrors = schema.validate(jsonResponseNode);
        return schemaValidationErrors;
    }

}



