package es.us.isa.idl.idlgatewayvalidation.filters;




 

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import idlanalyzer.analyzer.Analyzer;
import idlanalyzer.analyzer.OASAnalyzer;
import idlanalyzer.configuration.IDLException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Component
@Slf4j
public class IDLValidationFilter extends AbstractGatewayFilterFactory<IDLValidationFilter.Config> {

    

    private final WebClient.Builder webClientBuilder;

    public IDLValidationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
  
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            try {
            	String SPEC_URL = null;
            	String operationPath = null;
            	String requestPath = exchange.getRequest().getPath().toString();
 
                if(requestPath.indexOf("transactions") > -1) {
                	operationPath = "/transactions/{transaction_type}/search";
                	SPEC_URL = "./src/test/resources/Yelp/openapi.yaml";
                }
                else if(requestPath.indexOf("businesses") > -1) {
                	operationPath = exchange.getRequest().getPath().subPath(2).toString();
                	SPEC_URL = "./src/test/resources/Yelp/openapi.yaml";
                }
                else if(requestPath.indexOf("location") > -1) {
                	operationPath = exchange.getRequest().getPath().subPath(4).toString();
                	SPEC_URL = "./src/test/resources/DHL/openapi.yaml";
                }
                else if(requestPath.indexOf("places") > -1) {
                	operationPath = exchange.getRequest().getPath().subPath(2).toString();
                	SPEC_URL = "./src/test/resources/Foursquare/openapi.yaml";
                }
                else {
                	System.out.println( "Path did not match:");
                }
                String operationType = exchange.getRequest().getMethodValue().toLowerCase();
                Map<String, String> paramMap = exchange.getRequest().getQueryParams().toSingleValueMap();
                Analyzer analyzer = null;

                    analyzer = new OASAnalyzer("oas", SPEC_URL, operationPath, operationType, false);
                    
                boolean valid = analyzer.isValidRequest(paramMap);

                if (!valid) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The request is not valid!");
                }

                return chain.filter(exchange);
            } catch (IDLException e) {
            	System.out.println(e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}