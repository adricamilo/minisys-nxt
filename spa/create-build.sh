#!/bin/bash

#npm update

rm -rf build

npm run build

pushd build

echo Create reactapp.tar.gz file
rm -f ./reactapp.tar.gz ./build/reactapp.tar.gz
tar -zcf reactapp.tar.gz ./*

mv reactapp.tar.gz ..

popd

echo Done!!
