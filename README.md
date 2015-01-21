hpp2ts (hpp to ts. )
========

This library automatically creates TypeScript definitions for [MRAA](https://github.com/intel-iot-devkit/mraa) (`mraa.d.ts`) It has been designed as a solution to overcome the lack of support for [TypeSript](https://github.com/Microsoft/TypeScript) in [SWIG](https://github.com/swig/swig) (which should come at some point). 


Build
====

Use maven to build, the generated jar include the necessary dependencies  
```bash
mvn package
java -cp target/hpp2ts-0.0.1.jar eu.heads.project.ts.CPPHeaderScanner /home/barais/git/mraa/api/mraa/ mraa.d.ts #mraa_api_folder destfile.d.ts
```
built from https://github.com/ricardojlrufino/cplus-libparser


