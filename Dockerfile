FROM       eclipse-temurin:17-jre-jammy
RUN        echo alias ls=\'ls -F --color=auto\' >> ~/.bash_aliases \
        && echo alias ll=\'ls -l\'              >> ~/.bash_aliases \
        && echo alias la=\'ll -A\'              >> ~/.bash_aliases \
        && echo alias  l=\'la -C\'              >> ~/.bash_aliases
WORKDIR    /opt/anarucombot
COPY       target/anarucombot-${project.version}.jar ./
COPY       target/lib lib
ENTRYPOINT java -jar anarucombot-${project.version}.jar 2>&1 | tee -a console.log
