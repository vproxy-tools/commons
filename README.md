# vproxy commons

This module is copied and derived `vproxy-base`.

This module uses the same module name as `io.vproxy.base` and contains a subset of signatures of vproxy base classes.  
If you want to use `vproxy-base`, you'll have to exclude the dependency of `commons`.

## use

**gradle**

```groovy
implementation 'io.vproxy:commons:1.0.1'
```

**maven**

```xml
<dependency>
  <groupId>io.vproxy</groupId>
  <artifactId>commons</artifactId>
  <version>1.0.1</version>
</dependency>
```

**to exclude dependency using gradle**

```groovy
implementation(group: 'io.vproxy', name: 'vfx', version: '1.3.0') {
  exclude group: 'io.vproxy', module: 'commons'
}
```

**to exclude dependency using maven**

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.vproxy</groupId>
      <artifactId>vfx</artifactId>
      <version>1.3.0</version>
      <exclusions>
        <exclusion>
          <groupId>io.vproxy</groupId>
          <artifactId>commons</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
  </dependencies>
</dependencyManagement>
```
