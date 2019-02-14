package gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.function.Function;

// tag::code[]
@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // tag::route-locator[]
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
        String httpUri = uriConfiguration.getHttpbin();

        Function<PredicateSpec, Route.AsyncBuilder> fn1 = predicateSpec -> predicateSpec.path("/get").filters(f -> f.addRequestHeader("Hello",
                                                                                                                                      "World")).uri(httpUri);

        Function<PredicateSpec, Route.AsyncBuilder> fn2 = p -> p.host("*.hystrix.com").filters(f -> f.hystrix(config -> config.setName("mycmd").setFallbackUri("forward:/fallback"))).uri(httpUri);


        Function<PredicateSpec, Route.AsyncBuilder> fn3 = predicateSpec -> predicateSpec.path("/traceBackend/**").filters(f -> f.stripPrefix(1)).uri("lb://EDS-TRACER-DEMO.EDU-STD");


        Function<PredicateSpec, Route.AsyncBuilder> fn4 = predicateSpec -> predicateSpec.host("echo.fjd.com").uri("lb://EDS-TRACER-DEMO.EDU-STD");

        return builder.routes().route(fn1).route(fn2).route(fn3).route(fn4).build();

    }
    // end::route-locator[]

    // tag::fallback[]
    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }
    // end::fallback[]
}

// tag::uri-configuration[]
@ConfigurationProperties
class UriConfiguration {

    private String httpbin = "http://httpbin.org:80";

    public String getHttpbin() {
        return httpbin;
    }

    public void setHttpbin(String httpbin) {
        this.httpbin = httpbin;
    }
}
// end::uri-configuration[]
// end::code[]
