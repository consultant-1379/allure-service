#!/usr/bin/env bash
cd ../allure-service-core
echo "Grepping through allure-service source code for properties and adding them to the configuration markdown file"
grep -r @Value * | awk '{print $2}'| awk -F "[{}]" '{print "* " $2}' >> ../allure-user-docs/src/site/markdown/configuration.md
