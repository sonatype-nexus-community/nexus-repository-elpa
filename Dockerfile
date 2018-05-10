# To build an image from this Docker file:
# docker build --rm=true --tag=sonatype/nexus3-elpa .
# To run, binding port 8081 to the host:
# docker run -d -p 8081:8081 --name nexus sonatype/nexus3-elpa

ARG NEXUS_VERSION=3.10.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.10.0
ARG NEXUS_BUILD=04

COPY . /nexus-repository-elpa/
RUN cd /nexus-repository-elpa/; sed -i "s/3.10.0-04/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.10.0
ARG NEXUS_BUILD=04

ARG ELPA_VERSION=1.0.0
ARG ELPA_TARGET=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-elpa/${ELPA_VERSION}/
USER root
RUN mkdir -p ${ELPA_TARGET}; \
    sed -i 's@nexus-repository-maven</feature>@nexus-repository-maven</feature>\n        <feature prerequisite="false" dependency="false" version="1.0.0">nexus-repository-elpa</feature>@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-maven"@<feature name="nexus-repository-elpa" description="org.sonatype.nexus.plugins:nexus-repository-elpa" version="1.0.0">\n        <details>org.sonatype.nexus.plugins:nexus-repository-elpa</details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-elpa/1.0.0</bundle>\n        <bundle>mvn:org.apache.commons/commons-compress/1.11</bundle>\n    </feature>\n    <feature name="nexus-repository-maven"@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-elpa/target/nexus-repository-elpa-${ELPA_VERSION}.jar ${ELPA_TARGET}
USER nexus
