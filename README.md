# DSM Maven Plugin
A Maven Plugin that generates a DSM (Dependency Structure Matrix) HTML report.
It visualizes dependency cycles among Java project packages which might be a sign that the project design is too complicated.
## Setup

### Prerequisites

* JDK 1.8 - tools.jar is used by the plugin
* Maven (ofcourse)

### Installation

To use this plugin, install it locally with Maven:

```
mvn install
```
In case you experience issues installing the project you might need to specify an absolute location of your JDK tools.jar inside dsm-maven-plugin\pom.xml
* Locate the line `<toolsjar>${java.home}/../lib/tools.jar</toolsjar>`
* Replace it with an absolute path similar to (windows) `<toolsjar>C:/Program Files/jdk/lib/tools.jar</toolsjar>`

## Usage
Invoke an execution of the plugin in the pom of the Java project you would like to analyze:

```
<plugin>
    <groupId>com.nonapa</groupId>
    <artifactId>dsm-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <executions>
        <execution>
            <id>Generate DSM report</id>
            <goals>
                <goal>dsm</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

An HTML report file will be generated under target/dsm directory of your project!

