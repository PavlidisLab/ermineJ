#******************************************************************************
#  Author :Shahmil Merchant
#  Version :1.0
#  Created :09/02/02
#  Revision History: none
#  Description:MakeFile
#  
#                                                                                                                                                            
#*******************************************************************************

OPTIONS = -g -O

RUN_OPTIONS= -Xmx1024M

SOURCEPATH = .

CLASSPATH =  ../bin

OUTPUTDIR = ../bin

DOCDIR = ../doc

JAVAC = javac

JAVA = java

APP_PACKAGE = scores

PACKAGES =${APP_PACKAGE}.class_score

APP_PACKAGE_PATH=scores

WINDOWTITLE = 'Java 2 Platform v1.4 API Specification for Class Scores'
DOCTITLE = 'Class Scores<sup><font size="-2">TM</font></sup> v1.4 API Specification'
HEADER = '<b>CLASS_SCORES</b><br><font size="-1">Columbia</font>'
BOTTOM = '<font size="-1"><a href="mailto:pp175@columbia.edu">Submit a bug or feature request</a><br><br>Class Score is the propriety of Columbia Genome Center<br>Copyright 2002 Columbia Genome Center,<br>NY, U.S.A.All Rights Reserved.</font>'

all: class_score 

run: runner

tags: etags

etags:	\
	etags *.java scores/class_score/*.java

class_score:  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/affy_go_Parse.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/class_correls.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/class_pvals.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/ConstantStuff.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/corr_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/exp_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/histogram.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Matrix.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Pval_parse.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Stats.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/testcomparator.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Ug_Parse.class \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/GoName_parse.class \
        ${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Class_Frame.class \
        ${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Final_Project.class

CLASS_FILES= \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/affy_go_Parse.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/class_correls.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/class_pvals.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/ConstantStuff.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/corr_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/exp_class_scores.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/histogram.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Matrix.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Pval_parse.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Stats.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/testcomparator.class  \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Ug_Parse.class \
	${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/GoName_parse.class \
        ${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Class_Frame.class \
        ${OUTPUTDIR}/${APP_PACKAGE_PATH}/class_score/Final_Project.class




${OUTPUTDIR}/%.class	: ${SOURCEPATH}/%.java
	${JAVAC} ${OPTIONS} \
	-sourcepath ${SOURCEPATH} \
	-classpath ${CLASSPATH} \
	-d ${OUTPUTDIR} $<

runner: ${CLASS_FILES}
	${JAVA} ${RUN_OPTIONS} -classpath ${CLASSPATH} scores.class_score.Final_Project


doc:
	javadoc -d ${DOCDIR} -windowtitle ${WINDOWTITLE} -doctitle ${DOCTITLE} -header ${HEADER} -bottom ${BOTTOM} -classpath ${CLASSPATH} -sourcepath ${SOURCEPATH} ${PACKAGES}

clean:
	find ./ -name "*~" -exec rm {} \;
	rm -rf *.class;
	rm -r -f ${OUTPUTDIR};
	rm -r -f ${DOCDIR};
	mkdir ${OUTPUTDIR};
	mkdir ${DOCDIR};
