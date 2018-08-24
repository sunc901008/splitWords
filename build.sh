#!/bin/bash

echo "start build module fsearch.."

debug=${1}

filepath=$(cd "$(dirname "$0")"; pwd)
cd ${filepath}
echo ${filepath}

binpath=${filepath}/../bin
initpath=${filepath}/../init
libpath=${binpath}/lib

mkdir -p ${binpath}
mkdir -p ${initpath}
mkdir -p ${libpath}

#mod fsearch code
gradle clean
gradle war

rm -rf ${binpath}/search
mkdir -p ${binpath}/search
cp ${filepath}/build/libs/search.war ${binpath}/search/ROOT.war -rf
if [ $? -ne 0 ];then
    echo "cp search prj fail."
    exit 1
fi

cp ${filepath}/commitlog ${binpath}/search/ -rf
if [ $? -ne 0 ];then
    echo "cp commit log file fail."
    exit 1
fi

cp ${filepath}/version.json ${binpath}/search/ -rf
if [ $? -ne 0 ];then
    echo "cp version file fail."
    exit 1
fi

cp ${filepath}/initDatabase.sql ${initpath}/init_search.sql -rf
if [ $? -ne 0 ];then
    echo "cp init.sql fail."
    exit 1
fi


echo "end build module fsearch.."
