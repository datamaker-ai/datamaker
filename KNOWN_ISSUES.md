- [ ] Please note that using an index on randomly generated data will result on poor performance once there are millions of rows in a table. The reason is that the cache behavior is very bad with randomly distributed data. This is a problem for any database system.
https://tomharrisonjr.com/uuid-or-guid-as-primary-keys-be-careful-7b2aa3dcb439

- [ ] Length limit on all String field (ex: first name max length? strip if it exceed)

- [ ] POI library skip empty rows / empty cells automatically
    - Watch out for skip rows parameter
    - Test if it's fixed with for loop numrows
    
- [ ] Generation.IDENTITY performance drawback: https://thoughts-on-java.org/jpa-generate-primary-keys/

- [x] Manage default admin account creation
    
- [ ] Fix Google Analytics Avro Schema

- [x] Kafka output sink producer exception not catch properly (ex: timeout)

- Field detector service should not parse numeric in certain scenarios:
-- explicit json strings
-- explicit csv string (quoted)