## Docget 0.1.0

This tool is intended to help you open the javadoc from you
dependencies.

## Features

- Open a given local jar
- Download and open a jar for given url (with local cache)
- Retrieve from Central and open a jar for given organization:artifact:version, using ivy.

## How to use?

```
Usage: docget [-p port] path

port is a number
path is either:
	- a path to a local file
	- a http URL to a remote file
	- an artifact shaped as {organization}:{artifact}:{version}
```
