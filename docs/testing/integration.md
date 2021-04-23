# Integration testing

In `nl.mediquest.zorgrank.integration.core` you find integration tests that do actual requests on a running system. Therefore it's necessary to have a running database with a fixed set of test data.

Do the following when you want to insert or update data in this database:

- run the test database with `make setup-development-db`
- change or add data
- change or add integration tests
