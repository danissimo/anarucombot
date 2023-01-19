FROM       eclipse-temurin:17-jre-jammy
ENV        TAG=1.1.0
RUN        echo alias ls=\'ls -F --color=auto\' >> ~/.bash_aliases \
        && echo alias ll=\'ls -l\'              >> ~/.bash_aliases \
        && echo alias la=\'ll -A\'              >> ~/.bash_aliases \
        && echo alias  l=\'la -C\'              >> ~/.bash_aliases
WORKDIR    /opt/anarucombot
COPY       target/anarucombot-${TAG}.jar ./
COPY       target/lib lib
COPY       src/docker/container/keep-running.sh ./
RUN        chmod +x keep-running.sh
ENTRYPOINT ./keep-running.sh "$TAG"
