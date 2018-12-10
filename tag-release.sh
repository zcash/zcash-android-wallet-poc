#! /bin/bash

echo git tag -a \"v$(grep -oE 'versionName.*' zcash-android-wallet-app/app/build.gradle | sed 's/[^0-9]*\([0-9].*\w\).*/\1/')\" -m \""Released on $(date)"\"
echo
read -p "TAG the release as above? Press y to tag ENTER to skip: [N] "

if [[ $REPLY =~ ^[Yy]$ ]]
then
   git tag -a v$(grep -oE 'versionName.*' zcash-android-wallet-app/app/build.gradle | sed 's/[^0-9]*\([0-9].*\w\).*/\1/') -m "Release on $(date)"
   echo "build tagged"
else
   echo "tag not created!"   
fi

echo

read -p "PUSH TAG and TRIGGER BUILD to the wallet-team-members slack channel? [N] " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]
then
   echo
   git push origin --tags
   echo "Tags pushed, build triggered. Opening bitrise to verify..."
   xdg-open "https://app.bitrise.io/app/3f9040b242d98534#/builds"
else
   echo "tag not pushed!"
fi

echo


