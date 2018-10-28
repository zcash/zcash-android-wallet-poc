#!/usr/bin/zsh

# check for cargo installation
if ! type -p ~/.cargo/bin/cargo > /dev/null; then
	echo "Cargo appears to be missing.\nTry installing it with the following command:\n    curl https://sh.rustup.rs -sSf | sh"
	echo "and then run this script again."
	exit 1
fi
echo "Cargo found!"

# check for android targets
installed_android_target_count=$(~/.cargo/bin/rustup target list | grep android | grep installed | wc -l)
if [ "$installed_android_target_count" -lt "3" ]; then
	echo "The android targets do not appear to be installed."
        echo "attempting to install them...\n    ~/.cargo/bin/rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android"
	~/.cargo/bin/rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android
	[[ $(~/.cargo/bin/rustup target list | grep android | grep installed | wc -l) -lt 3 ]] && echo "install failed. Aborting!" && exit 1
	echo "Done."
fi

echo "Android targets found!" 

# check for standalone  NDK
./build-ndk-standalone.sh


echo "Building..."
cargo build --target aarch64-linux-android --release
cargo build --target i686-linux-android --release
cargo build --target armv7-linux-androideabi --release

echo "Done."
