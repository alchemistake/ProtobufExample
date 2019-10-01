#Simple Record Store

## How to run
Start the server by calling this in project root: `docker-compose down && docker-compose build && docker-compose up`. In my opinion this is the cleanest way to re-build, and re-up projects rather than `docker-compose up --build`

Then you can send POST requests with json body of `{"name":<str>, "id":<long>}`
You can use postman with the collection included, `Test Call for Simple Record Store.postman_collection.json`.
## Accessing the files after server is closed
You can go to `~/recordStore` file, a.k.a. `<your-home-diretory>/recordStore` and there will be files with file extension `.proto.bin`.They are the files generated by the server.