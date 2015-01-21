hpp2ts (hpp to ts. )
========

This library has been designed to create mraa.d.ts automaticallya nd enable typescript support for MRAA project in waiting support of typescript in swig. 


Build
====

Use maven to build, the generated jar include the necessary dependencies  
```bash
mvn package
java -cp target/hpp2ts-0.0.1.jar eu.heads.project.ts.CPPHeaderScanner /home/barais/git/mraa/api/mraa/ mraa.d.ts #mraa_api_folder destfile.d.ts
```
built from https://github.com/ricardojlrufino/cplus-libparser


