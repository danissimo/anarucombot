FROM eclipse-temurin:17-jre-jammy
RUN  echo alias ls=\'ls -F --color=auto\' >> ~/.bash_aliases \
  && echo alias ll=\'ls -l\' >> ~/.bash_aliases \
  && echo alias la=\'ll -A\' >> ~/.bash_aliases \
  && echo alias l=\'la -C\' >> ~/.bash_aliases
WORKDIR /opt/app
COPY target/anarucombot-1.0.0.jar ./
COPY target/lib lib
CMD java -jar anarucombot-1.0.0.jar
