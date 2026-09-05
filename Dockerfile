# Build Stage

FROM eclipse-temurin:25-jdk-ubi10-minimal AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml to the working directory
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download Maven dependencies to the local repository
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

# Copy the source code to the working directory
COPY src/ src/

# Build the application and package it into a JAR file
RUN ./mvnw package -DskipTests -B


# Runtime Stage

FROM eclipse-temurin:25-jre-ubi10-minimal

WORKDIR /app

# Create non-root application user
RUN groupadd --system hirers && \
    useradd --system --gid hirers hirers

# Copy the built JAR file from the builder stage to the runtime stage
COPY --from=builder /app/target/*.jar /app/hirers.jar

# Set ownership of the application directory to the non-root user
RUN chown -R hirers:hirers /app

# Switch to the non-root user for running the application
USER hirers

EXPOSE 8080

# Set Java options for memory management and error handling
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Set the entry point to run the application with the specified Java options
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/hirers.jar"]