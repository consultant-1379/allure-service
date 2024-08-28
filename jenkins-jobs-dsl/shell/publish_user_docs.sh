#!/usr/bin/env bash
cd allure-service-api/target/
rm -rf site/
unzip site.zip

export targetDir="/proj/PDU_OSS_CI_TAF/taflanding/allure-service/"
if [ ! -d "$targetDir" ]; then
  echo "$targetDir not present, it will be created"
  mkdir -m u+w $targetDir
else
    echo "$targetDir present, clearing contents"
    rm -rf $targetDir/*
fi
echo "Copying Allure Service docs content to $targetDir"
cp -r * $targetDir
