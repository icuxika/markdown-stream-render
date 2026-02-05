发布到 `Sonatype Nexus Repository`
==========

## `settings-nexus.xml`配置

```xml
<?xml version="1.0" encoding="UTF-8"?>

<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
    <pluginGroups>
    </pluginGroups>

    <proxies>
    </proxies>

    <servers>
        <server>
            <id>nexus</id>
            <username>admin</username>
            <password>Nexus 密码</password>
            <filePermissions>664</filePermissions>
            <directoryPermissions>775</directoryPermissions>
            <configuration></configuration>
        </server>
    </servers>

    <mirrors>
        <mirror>
            <id>nexus</id>
            <mirrorOf>*</mirrorOf>
            <name>nexus</name>
            <url>http://localhost:8081/repository/maven-public</url>
        </mirror>
    </mirrors>

    <profiles>
    </profiles>
</settings>
```

## 发布命令

```
mvn -s settings-nexus.xml clean compile deploy
```