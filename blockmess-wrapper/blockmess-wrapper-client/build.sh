#! /bin/bash

mvn clean compile assembly:single docker:build
