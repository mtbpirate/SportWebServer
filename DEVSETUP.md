# Database
Start database (start Rancher Desktop before)

```shell script
docker run -d --name mysql8 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=raptor -e MYSQL_DATABASE=swstest mysql:8.0
```

# Tabellen anzeigen

```shell script
docker exec -it mysql8 mysql -u root -praptor swstest -e "SHOW TABLES;"
```



# Tabellen anlegen

```shell script
@echo off

echo "CREATE TABLE IF NOT EXISTS verein (" >> temp.sql
echo   "id BIGINT AUTO_INCREMENT PRIMARY KEY," >> temp.sql
echo   "name VARCHAR(100) NOT NULL," >> temp.sql
echo   "ort VARCHAR(100)" >> temp.sql
echo   ");" >> temp.sql

cat .\temp.sql | docker exec -i mysql8 mysql -u root -praptor swstest


del temp.sql

docker exec -it mysql8 mysql -u root -praptor swstest -e "SHOW TABLES;"

echo Fertig.
```