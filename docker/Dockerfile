FROM debian:trixie-slim

LABEL maintainer="Schwaggot <schwaggot@kellerkompanie.com>"
LABEL description="Build environment for keko-arma3sync Debian package"

# Install build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    default-jdk \
    dpkg-dev \
    debhelper \
    fakeroot \
    lintian \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle 8.5
ENV GRADLE_VERSION=8.5
ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"

RUN curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && mv "/opt/gradle-${GRADLE_VERSION}" "${GRADLE_HOME}" \
    && rm /tmp/gradle.zip \
    && gradle --version

WORKDIR /build

# Default command: build the package
CMD ["dpkg-buildpackage", "-us", "-uc", "-b"]
