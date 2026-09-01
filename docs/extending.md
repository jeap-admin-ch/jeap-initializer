# Extending

The initializer's generation pipeline (see [Architecture](architecture.md)) is built entirely from Spring beans
implementing `ProjectContributor`, auto-discovered from the application context. Consumers of the
`jeap-initializer` artifact can add their own contributors without modifying the library.

## Writing a custom `ProjectContributor`

```java
@Component
public class MyCustomContributor implements ProjectContributor {

    @Override
    public void contribute(Path projectRoot, ProjectRequest projectRequest, ProjectTemplate template) throws IOException {
        // Modify files under projectRoot based on projectRequest / template
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // default; runs after all built-in contributors
    }
}
```

`ProjectContributor` is a `@FunctionalInterface` extending Spring's `Ordered`; only `contribute(...)` is required,
`getOrder()` defaults to `Ordered.LOWEST_PRECEDENCE` (last). Override it to run earlier — see the order values of
the built-in contributors in [Architecture](architecture.md) to position custom logic relative to them (for
example, before `GroupIdRenamerContributor` if a custom contributor still needs to see the template's original
group id).

Any `ProjectContributor` bean present in the `ApplicationContext` is automatically picked up by `ProjectGenerator`
— no manual registration is needed.

## Component scanning

By default, component scanning is enabled for `ch.admin.bit.jeap.initializer` and whatever package the consuming
`@SpringBootApplication` lives in. A `@Component`-annotated `ProjectContributor` in one of those packages is
picked up with no further wiring.

To contribute from a package that is **not** component-scanned — typically a shared library used by several
instances — register the beans through auto-configuration instead:

1. Put the contributor(s) on a configuration class:

   ```java
   @AutoConfiguration
   public class CustomContributorAutoConfiguration {

       @Bean
       MyCustomContributor myCustomContributor() {
           return new MyCustomContributor();
       }
   }
   ```

2. List that class in `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   (one fully-qualified class name per line):

   ```
   com.example.initializer.CustomContributorAutoConfiguration
   ```

Spring Boot then loads the configuration for every instance that has the library on its classpath, and
`ProjectGenerator` picks up the contributor beans like any other. See the Spring Boot documentation on
[developing auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
for the general mechanism.

## See also

- [Architecture](architecture.md) — the full contributor chain and generation flow
- [Template Authoring](template-authoring.md) — the markers a contributor typically looks for in template files
- [Getting Started](getting-started.md) — running the application with your extensions
