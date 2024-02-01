FROM mariadb
ENV MYSQL_ROOT_PASSWORD 1234
ENV MYSQL_DATABASE mydb
COPY ./mysql_data/data /var/lib/mysql
EXPOSE 3307
CMD ["--user=root"]