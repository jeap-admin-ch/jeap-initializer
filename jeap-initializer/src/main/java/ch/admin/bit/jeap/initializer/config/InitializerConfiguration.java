package ch.admin.bit.jeap.initializer.config;

import ch.admin.bit.jeap.web.configuration.HttpHeaderFilterPostProcessor;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;

import java.util.Map;

@Configuration
@EnableCaching
@PropertySource("classpath:/jeap-initializer-default.properties")
public class InitializerConfiguration {

    @Bean
    public Caffeine<Object, Object> caffeineConfig(JeapInitializerProperties properties) {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.getTemplateCacheDuration());
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

    @Bean
    public HttpHeaderFilterPostProcessor httpHeaderFilterPostProcessor() {
        return new HttpHeaderFilterPostProcessor() {
            /** Not using an SPA, assets and websites should not be cached as their path does not include a hash */
            @Override
            public void postProcessHeaders(Map<String, String> headers, String method, String path) {
                headers.remove(HttpHeaders.CACHE_CONTROL);
                headers.remove(HttpHeaders.EXPIRES);
            }
        };
    }
}
