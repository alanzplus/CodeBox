#!/bin/bash

atGroupId="org.zlambda.maven.archetype"
atArtifact="terminal-app"
atVersion="1.0.0-SNAPSHOT"

pjGrouId="org.zlambda.sandbox.commons"
pjArtifact="commons"
pjVersion="1.0.0-SNAPSHOT"

mvn archetype:generate -B -DarchetypeGroupId=${atGroupId} -DarchetypeArtifactId=${atArtifact} -DarchetypeVersion=${atVersion} -DgroupId=${pjGrouId} -DartifactId=${pjArtifact} -Dversion=${pjVersion}
