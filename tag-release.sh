#! /bin/bash

echo git tag -a \"v$(grep -oE 'versionName.*' zcash-android-wallet-app/app/build.gradle | sed 's/[^0-9]*\([0-9].*\w\).*/\1/')\" -m \""Released on $(date)"\"
echo
echo "Press ENTER to tag the release as above. Press CTRL+C to cancel."

read

git tag -a v$(grep -oE 'versionName.*' zcash-android-wallet-app/app/build.gradle | sed 's/[^0-9]*\([0-9].*\w\).*/\1/') -m "Release on $(date)"
