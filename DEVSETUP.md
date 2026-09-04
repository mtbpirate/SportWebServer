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
.\DB_createTables_script.ps1
```
