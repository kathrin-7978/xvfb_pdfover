#!/bin/sh
BASEDIR=`dirname $0`
java -jar "$BASEDIR/../lib/pdf-over-install-helper-1.0.0.jar"
exec java -cp "$BASEDIR/../lib/*" at.asit.pdfover.gui.Main "$@"
