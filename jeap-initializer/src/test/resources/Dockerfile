FROM 211125750372.dkr.ecr.eu-central-2.amazonaws.com/jeap-runtime-coretto:21.20250917031953

# Copy application files
COPY target/jme-jeap-nivel-quadrel-project-template-web.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
