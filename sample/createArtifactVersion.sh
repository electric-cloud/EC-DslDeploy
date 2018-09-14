#! /bin/bash
#  ectool --server XXXX login admin yyyyy

# Read buildCounter
if [ ! -f ./buildCounter ]; then
  echo "Creating buildCounter"
  echo 0 > buildCounter
fi
buildCounter=`cat buildCounter`
buildCounter=`expr $buildCounter + 1`
echo "[INFO] - Incrementing AV number to $buildCounter...";
echo $buildCounter > buildCounter

ectool publishArtifactVersion \
  --version 1.0.$buildCounter \
  --artifactName com.electriccloud:EC-dslDeploy \
  --includePatterns "**/*" \
  --fromDirectory repo \
  --repositoryName default
