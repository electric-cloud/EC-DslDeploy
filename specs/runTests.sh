BASEDIR=$(dirname "$0")

# where the code should be read from to create artifacts
export CODE_DIR=../

cd $BASEDIR
./gradlew test -Pserver=${COMMANDER_SERVER} -Ppassword=${COMMANDER_PASSWORD} $@
# --tests=com.electriccloud.plugin.spec.backup
