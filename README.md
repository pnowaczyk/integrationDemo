# Integration demo

prosta apka do ćwiczeń na testami integracyjnymi

## wymagania

- elasticsearch -- nazwa cluster'a z konfiguracji powinna trafić do `application.yml`

## jak uruchomić?

```
./gradlew bootRun
```

testy integracyjne:

```
./gradlew test
```


## co? gdzie?

- endpoint: <http://localhost:8080/document>
- wyszukiwarka : <http://localhost:9200/integration/documents/_search?q=*>
