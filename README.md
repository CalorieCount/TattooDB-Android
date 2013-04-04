TattooDB
========

A simple, locally stored, document database for Android. 

```
TattooDB db = new TattooDB(context);

MyModel model = new MyModel();
model.name = "Joel";

// store a record
db.put("1", model);

// retrieve a record;
MyModel retrieved = db.get("1", MyModel.class);
Log.i(TAG, retrieved.name); // "Joel"

// delete a record
db.delete("1", MyModel.class);
```

This was designed to reduce the number of concepts a developer would have to understand in order to store data locally.
All you have to worry about is three methods: `put`, `get`, `delete`. You can store any Java class, and there is no setup or configuration needed.
TattooDB uses SQLite as the underlying storage mechanism by default, but this can be overridden and a custom implementation provided if you would rather use something else (such as manually writing to disk).

As this is a document database, you will have to be mindful about how you design your object model. There are many "NoSQL" 
database design resources on the web, such as this one: http://ayende.com/blog/3897/designing-a-document-database

*Installation*
Currently, you can use this library by simply cloning (or downloading) the repository and adding it to your workspace.
Then you can add the GSON 2.2.2 dependency (which you can find in this repository in `/dependencies`). Once the project matures, we will provide a jar and make it available via maven.
