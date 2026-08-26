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
`@SpringBootApplication` lives in. To define beans in another package, register it explicitly — see the Spring
Boot documentation on
[developing auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
for the general mechanism (e.g. an additional `@ComponentScan` or `@Import`).

## See also

- [Architecture](architecture.md) — the full contributor chain and generation flow
- [Template Authoring](template-authoring.md) — the markers a contributor typically looks for in template files
- [Getting Started](getting-started.md) — running the application with your extensions
