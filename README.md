# Meson
### Distributed ID generator inspired by BSON's ObjectID and Snowflake

> In particle physics, mesons (/ˈmiːzɒnz/ or /ˈmɛzɒnz/) are hadronic subatomic particles composed of one quark and one antiquark, bound together by strong interactions. - [Wikipedia](https://en.wikipedia.org/wiki/Meson)


[![Maven Central](https://img.shields.io/maven-central/v/com.rfksystems/meson.svg?style=flat-square)](http://mvnrepository.com/artifact/com.rfksystems/meson)

API documentation is available [here](http://www.javadoc.io/doc/com.rfksystems/meson/)

### ID composition:

```
 48 bits | 6 bytes: Unix-time in millis as 48 bit unsigned integer 
 32 bits | 4 bytes: Generator identifier (hash)
 32 bits | 4 bytes: Generator-instance incremental sequence number (32 bit unsigned integer)
______________________________________________________________________________________________
Total 112 bits, or 14 bytes, or, 28 or 30 characters long, depending on the format
```

### Implementation details

1. Timestamps are expressed as 48 bit unsigned integers with max. value of `281474976710655`. This is well beyond what would in theory be required to store timestamps. In fact, the topmost timestamp you could store in a 48 bit int is August 2nd year 10889.
2. Timestamps have millisecond precession and all the identifier is Big-Endian exclusively. This makes Meson naturally sortable by time and in sequence within the same instance. Sorting order beyond millis for ID's generated in different instances is not defined, yet given the millisecond precession, it should not a be a problem.
3. Generator identifier is a CRC32 of parts defined, and makes a collision between hosts of the same ORG pretty near improbable:
    - `/proc/1/cgroup` contents if the file is present
    - Hostname of the "127.0.0.1" address
    - Process ID (PID)
    - Every MAC address in the system
    - Every public IPv6/IPv4 address in the system
4. Linked to Jackson's DataBind by default (at least for now), so serialization/deserialization to JSON and back should be trivial.
    
See:
- https://stackoverflow.com/a/42947044/1653859
- https://github.com/moby/moby/issues/22174
- https://tuhrig.de/how-to-know-you-are-inside-a-docker-container/
- https://docs.mongodb.com/manual/reference/method/ObjectId/
 
### Requirements

Java 8+.

### Installation

Meson is available in Maven Central. Adding dependency definition to your build config should do the trick.

#### Maven

```xml
<dependency>
    <groupId>com.rfksystems</groupId>
    <artifactId>meson</artifactId>
    <version>VERSION</version>
</dependency>
```

#### Gradle

```groovy
    compile group: 'com.rfksystems', name: 'meson', version: 'VERSION'
```

### Usage

Either create new instance of `com.rfksystems.meson.Meson` with appropriate or no parameters, or call one of
`com.rfksystems.meson.Meson#directTo*` methods to create a Meson identifier directly without creating
a Meson object. See [API documentation](http://www.javadoc.io/doc/com.rfksystems/meson/) for more of what methods are available.

### Performance

... is not the main goal of the project, but as it stands, Meson is on par or quicker with BSON's ObjectID
in the limited number of tests I have performed. 

On my modest Core i5 Skylake laptop, Meson is able to generate 100m ID's on single thread in under 3 seconds -
exactly on par with BSON's ObjectId.

### Sample ID's

This should give a nice example on how the hex formatted Meson ID's look like.

#### Compact HEX string (prefered format)

```
0162915be2e1900035c91a2a5d33
0162915be2e1900035c91a2a5d34
0162915be2e1900035c91a2a5d35
0162915be2e1900035c91a2a5d36
0162915be2e1900035c91a2a5d37
0162915be2e1900035c91a2a5d38
0162915be2e1900035c91a2a5d39
0162915be2e1900035c91a2a5d3a
0162915be2e1900035c91a2a5d3b
0162915be2e1900035c91a2a5d3c
```

#### Format HEX string (display ID's)

```
0162915be2da-900035c9-1a2a5d29
0162915be2e1-900035c9-1a2a5d2a
0162915be2e1-900035c9-1a2a5d2b
0162915be2e1-900035c9-1a2a5d2c
0162915be2e1-900035c9-1a2a5d2d
0162915be2e1-900035c9-1a2a5d2e
0162915be2e1-900035c9-1a2a5d2f
0162915be2e1-900035c9-1a2a5d30
0162915be2e1-900035c9-1a2a5d31
0162915be2e1-900035c9-1a2a5d32

```

### Byte values from Java's `byte[]` output

```
[1, 98, -111, 91, -30, -31, -112, 0, 53, -55, 26, 42, 93, 61]
[1, 98, -111, 91, -30, -31, -112, 0, 53, -55, 26, 42, 93, 62]
[1, 98, -111, 91, -30, -31, -112, 0, 53, -55, 26, 42, 93, 63]
[1, 98, -111, 91, -30, -31, -112, 0, 53, -55, 26, 42, 93, 64]
[1, 98, -111, 91, -30, -31, -112, 0, 53, -55, 26, 42, 93, 65]
[1, 98, -111, 91, -30, -30, -112, 0, 53, -55, 26, 42, 93, 66]
[1, 98, -111, 91, -30, -30, -112, 0, 53, -55, 26, 42, 93, 67]
[1, 98, -111, 91, -30, -30, -112, 0, 53, -55, 26, 42, 93, 68]
[1, 98, -111, 91, -30, -30, -112, 0, 53, -55, 26, 42, 93, 69]
[1, 98, -111, 91, -30, -30, -112, 0, 53, -55, 26, 42, 93, 70]
```

## License

Apache License, Version 2.0
