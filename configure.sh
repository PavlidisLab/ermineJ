#!/usr/bin/sh
# Paul pavlidis
# Make this directory belong to cvs as its own module.

# get the new name for the module
modulename=$1
oldname=`pwd`
if [ $1 = "" ]
    then
    echo "Enter a name for the new module"
    exit
fi

echo "Preparing ${1} from ${oldname}. Continue? (y/n)\c"
read response
if [ $response = "y" ] 
    then
#    cd ..
#    echo `pwd`
#    echo "cp -r $oldname ${oldname}.safe"
#    cp -r $oldname ${oldname}.safe
#    cd $oldname

    echo `pwd`
    echo "    rm -r ./CVS"
    rm -r ./CVS
    echo "    rm -r ./doc/CVS"
    rm -r ./doc/CVS
    echo "    rm -r ./bin/CVS"
    rm -r ./bin/CVS
    echo "    rm -r ./src/CVS"
    rm -r ./src/CVS
    echo "    rm -r ./web/CVS"
    rm -r ./web/CVS
    echo "    rm -r ./web/images/CVS"
    rm -r ./web/images/CVS
    echo "    rm -r ./test/CVS"
    rm -r ./test/CVS
    echo "    rm -r ./web/css/CVS"
    rm -r ./web/css/CVS
    
    echo "    scvs home import -m \"Initialized ${modulename}\" ${modulename} start"
    scvs home import -m "Initialized ${modulename}" ${modulename} start
    echo "cd .."
    cd ..
    echo `pwd`
    echo "scvs home co ${modulename}"
    scvs home co ${modulename}
    echo "Don't forget to remove this old directory"
else
    echo "Never mind"
fi
