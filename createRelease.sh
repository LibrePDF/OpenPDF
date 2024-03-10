#!/bin/bash

if [ "x$LIBREPDF_ORIGIN" == "x" ]
then
    echo "Please set LIBREPDF_ORIGIN to the origin-name of the librepdf repository on github."
    echo "This could be 'origin' or any name you declared to be that origin."
    exit
fi

if [ $# -ne 1 ]
then
    echo "Please give the NEXT SNAPSHOT version number (without -SNAPSHOT)"
    exit
fi

VERSION=$(grep "<version>" pom.xml | head -1 | sed "s/.*<version>\([0-9.]*\)\(-SNAPSHOT\)\{0,1\}<\/version>.*/\1/")
NEW_VERSION=${1}-SNAPSHOT

cat << EOD
Will set Version to ${VERSION} and next Snapshot to ${NEW_VERSION}.

Things to do before you continue:
- Update the README.md, so the new Release is mentioned there
- Create a ChangeLog for this release
- Update the contributors and credits

Press ENTER to continue. Ctrl-C to stop.
EOD
read ignored

echo "#1. Setting release version: ${VERSION}"
mvn versions:set -DnewVersion="${VERSION}"

echo "#2. Test build"
mvn clean install
RC=$?
if [ ${RC} -ne 0 ]
then
    echo "Errors. Exiting."
    exit
fi

echo "#3. Commit changes and tag"
git commit -a -m "Set version to ${VERSION}" && git tag "${VERSION}"

echo "#4. Make a staging release"
mvn clean deploy -Prelease
RC=$?
if [ $RC -eq 0 ]
then
    cat << EOD
        Staging Release seems to be created. Please review it at https://oss.sonatype.org/#stagingRepositories
        if everything seems fine, hit ENTER. Else break this script with Ctrl-C.
EOD
    read pause
else
    echo "Something went wrong. Please switch to manual deploy."
    echo "(look at https://github.com/LibrePDF/OpenPDF/wiki/Release-Process)"
    exit
fi

echo "#5. Deploy release."
# Workaround for https://issues.sonatype.org/browse/OSSRH-66257
export MAVEN_OPTS="
--add-opens=java.base/java.util=ALL-UNNAMED
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED
--add-opens=java.base/java.text=ALL-UNNAMED
--add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
mvn nexus-staging:release

echo "#6. Finishing."
echo " - set next SNAPSHOT to $NEW_VERSION"
mvn versions:set -DnewVersion="$NEW_VERSION"
echo " - commit changes"
git commit -a -m "Set new Snapshot to $NEW_VERSION"
echo " - push all to github"
git push "${LIBREPDF_ORIGIN}" --tags
