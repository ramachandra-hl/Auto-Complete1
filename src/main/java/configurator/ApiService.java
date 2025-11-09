package configurator;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiService {

    public static void refreshTestData(){
        BaseClass.initializeTestData();
    }

    public static Response invokePostRequestWithFile(String url, String filePath, Map<String, String> headers) {
        File payload = new File(filePath);

        return given()
                .headers(headers)
                .contentType("application/json")
                .body(payload)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }
    public static Response invokePostRequest(String url, JSONArray data, Map<String, String> headers) {

        return given()
                .headers(headers)
                .contentType("application/json")
                .body(data)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, JSONObject data, String headers) {

        return given()
                .contentType("application/json")
                .body(data)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, JSONObject data) {

        return given()
                .contentType("application/json")
                .body(data)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, JSONObject data, Map<String, String> headers) {

        return given()
                .headers(headers)
                .contentType("application/json")
                .body(data)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }
    public static Response invokePostRequest(String url, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, Map<String, Object> formData,
                                             Map<String, String> headerData, Map<String, String> cookeis) {

        return given()
                .headers(headerData)
                .contentType("application/json")
                .cookies(cookeis)
                .body(formData)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, String formData,
                                             Map<String, String> headerData, Map<String, String> cookeis) {
        return given()
                .headers(headerData)
                .contentType("application/json")
                .cookies(cookeis)
                .body(formData)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequestWithMapOfObjet(String url, Map<String, Object> formData, Map<String, String> headers, Map<String,String> cookies ) {
        return RestAssured.given()
                .headers(headers)
                .cookies(cookies)
                .contentType(ContentType.URLENC)
                .formParams(formData)
                .post(url);
    }

    public static Response invokePostRequestWithMultiPart(String url, Map<Object, Object> formData) {
        RequestSpecification request = given()
                .contentType("multipart/form-data");
        // Add each entry as a separate multiPart field
        for (Map.Entry<Object, Object> entry : formData.entrySet()) {
            request.multiPart((String) entry.getKey(), entry.getValue());
        }
        return request
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }


    public static Response invokePostRequestWithMultiPart(String url,
                                                          Map<String, Object> formData,
                                                          Map<String, String> headers,
                                                          Map<String, String> cookieData) {

        var request = RestAssured.given()
                .headers(headers)
                .cookies(cookieData)
                .contentType(ContentType.MULTIPART);

        formData.forEach((key, value) -> {
            if (value instanceof File file) {
                request.multiPart(key, file);
            } else if (value instanceof String s) {
                request.multiPart(key, s);
            } else if (value != null) {
                request.multiPart(key, value.toString());
            }
        });

        return request.post(url);
    }
    public static Response invokePostRequest(String url, String payload) {
        return given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePostRequest(String url, String payload, Map<String, String> token) {
        return given()
                .headers(token)
                .contentType("application/json")
                .body(payload)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }
  public static   Response invokePostRequestFormDataAsUrlencoded(String url, Map<String, Object> formData) {
        // Create a POST request with URL-encoded form data and return the response
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(formData) // Directly add form parameters
                .post(url)
                .then()
                .extract()
                .response();
    }
    public static Response invokePutRequest(String url, String jsonPayload, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .body(jsonPayload)
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokePutRequest(String url, List<Map<String, Object>> jsonPayload, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .body(jsonPayload)
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokeDeleteRequest(String url,  Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokeDeleteRequest(String url, JSONArray jsonPayload, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .body(jsonPayload)
                .when()
                .delete(url)
                .then()
                .extract()
                .response();
    }


    public static  Response invokeGetRequest(String url) {
        return given()
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    public static  Response invokeGetRequest(String url, Map<String, String> headers) {
        return given()
                .headers(headers)
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokeGetRequestWithHeaders(String url, Map<String, String> headers) {
        return given()
                .headers(headers)
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }
    public static Response invokePutRequestWithFile(String url, String filePath, Map<String, String> headers) {
        File payload = new File(filePath);

        return given()
                .headers(headers)
                .contentType("application/json")
                .body(payload)
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }
    public static Response invokePutRequest(String url, Map<String, String> headers) {
        return given()
                .headers(headers)
                .contentType("application/json")
                .when()
                .put(url)
                .then()
                .extract()
                .response();
    }

    public static Response invokeGetRequestWithQueryParams(String url,Map<String, String> payLoad, Map<String, String> headers, Map<String, String> cookies) {
        return given()
                .headers(headers)
                .cookies(cookies)
                .contentType("application/json")
                .queryParams(payLoad)
                .when()
                .get(url)
                .then()
                .extract()
                .response();
    }

    public static Map<String, String> buildAuthorizationHeader(String token) {
        HashMap<String, String> headerData1 = new HashMap<>();
        headerData1.put("Authorization", token);
        return headerData1;
    }

    public static Response invokePostRequestWithCookies(String url,Map<String, String> payLoad, Map<String, String> cookies) {

        return given()
                .cookies(cookies)
                .contentType("application/json")
                .body(payLoad)
                .when()
                .post(url)
                .then()
                .extract()
                .response();
    }
}

