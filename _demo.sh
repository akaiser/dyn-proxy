#!/bin/bash

rm -r _demo/*
./mvnw -version
./mvnw clean package

mv target/dyn-proxy-1.0.0-SNAPSHOT-jar-with-dependencies.jar _demo/fat.jar
