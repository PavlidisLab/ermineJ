#******************************************************************************
#  Author :Shahmil Merchant
#  Created :09/02/02
#  Revision History: $Id$
#  Description:MakeFile
#  
#                                                                                                                                                            
#*******************************************************************************

OPTIONS = -g -O

RUN_OPTIONS= -Xmx1024M

SOURCEPATH = .

#CLASSPATH =  ../bin
CLASSPATH = .
#OUTPUTDIR = ../bin
OUTPUTDIR = .

DOCDIR = ../doc

JAVAC = javac

JAVA = java

APP_PACKAGE = classScore

PACKAGES =${APP_PACKAGE}

APP_PACKAGE_PATH=.

WINDOWTITLE = 'Java 2 Platform v1.4 API Specification for Class Scores'
DOCTITLE = 'Class Scores<sup><font size="-2">TM</font></sup> v1.4 API Specification'
HEADER = '<b>CLASS_SCORES</b><br><font size="-1">Columbia</font>'
BOTTOM = '<font size="-1"><a href="mailto:pp175@columbia.edu">Submit a bug or feature request</a><br><br>Class Score is Copyright 2002 (C) Columbia Genome Center,<br>NY, U.S.A.All Rights Reserved.</font>'

CLASS_FILES= \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/classresult.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/ClassMap.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/class_correls.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/exp_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/class_pvals.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/corr_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/histogram.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/Pval_parse.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/Group_Parse.class \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/GoName_parse.class \
        ${OUTPUTDIR}/${APP_PACKAGE_PATH}/classScore/Class_Frame.class \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/util/Matrix.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/util/Stats.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/util/SpecFunc.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/util/geneNpval.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/util/ConstantStuff.class  \
        ${OUTPUTDIR}/erminecmd.class

all: class_score commandline

run: runner

tags: *.java
	etags *.java classScore/*.java


class_score: ${CLASS_FILES}


${OUTPUTDIR}/%.class	: ${SOURCEPATH}/%.java
	${JAVAC} ${OPTIONS} \
	-sourcepath ${SOURCEPATH} \
	-classpath ${CLASSPATH} \
	-d ${OUTPUTDIR} $<

runner: ${CLASS_FILES}
	${JAVA} ${RUN_OPTIONS} -classpath ${CLASSPATH} classScore.Class_Frame

doc:
	javadoc -version -author -use -d ${DOCDIR} -windowtitle ${WINDOWTITLE} -doctitle ${DOCTITLE} -header ${HEADER} -bottom ${BOTTOM} -classpath ${CLASSPATH} -sourcepath ${SOURCEPATH} ${PACKAGES}

# force this to relink.
commandline: 
	${JAVAC} ${OPTIONS} \
	-sourcepath ${SOURCEPATH} \
	-classpath ${CLASSPATH} \
	erminecmd.java

clean:
#	find ./ -name "*~" -exec rm {} \;
#	rm -rf ${OUTPUTDIR}/${APP_PACKAGE_PATH}/*.class;
#	rm -r -f ${OUTPUTDIR}/*;
#	rm -r -f ${DOCDIR}/*;
#	mkdir ${OUTPUTDIR};
#	mkdir ${DOCDIR};
