//package football.underground.app.api;
//
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import io.micronaut.gcp.function.http.GoogleHttpResponse;
//import io.micronaut.gcp.function.http.HttpFunction;
//import io.micronaut.http.HttpMethod;
//import io.micronaut.http.HttpStatus;
//
//class GameControllerTest {
//
//    @Test
//    public void testGet() throws Exception {
//        try (HttpFunction function = new HttpFunction()) {
//            GoogleHttpResponse response = function.invoke(HttpMethod.GET, "/game-api/v1-beta/games");
//            Assertions.assertThat(response).extracting(GoogleHttpResponse::getStatus).isEqualTo(HttpStatus.OK);
//        }
//    }
//}
