FROM ascdc/jdk8
MAINTAINER jjyy yangyangyangya@qq.com
RUN mkdir -p /var/yang \
 && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
 && echo "Asia/Shanghai" > /etc/timezone
COPY target/*.jar /var/yang/app.jar
WORKDIR /var/yang
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8010:8010
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
#CMD ["--spring.profiles.active=dev"]