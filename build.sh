set -e
cd /home/claude/prefixblock
SDK=/usr/lib/android-sdk/platforms/android-23/android.jar
rm -rf build && mkdir -p build/stubs build/classes build/dex
javac -nowarn -source 8 -target 8 -bootclasspath $SDK -d build/stubs $(find stubs -name '*.java') 2>&1 | grep -v 'warning' || true
javac -nowarn -source 8 -target 8 -bootclasspath build/stubs:$SDK -d build/classes $(find src -name '*.java') 2>&1 | grep -v 'warning' || true
ls build/classes/com/safae/prefixblock/MainActivity.class >/dev/null
dalvik-exchange --dex --min-sdk-version=26 --output=build/dex/classes.dex build/classes
aapt package -f -M AndroidManifest.xml -S res -I $SDK -F build/unsigned.apk
(cd build/dex && aapt add ../unsigned.apk classes.dex >/dev/null)
zipalign -f -p 4 build/unsigned.apk build/aligned.apk
[ -f release.keystore ] || keytool -genkeypair -keystore release.keystore -storepass prefixblock -keypass prefixblock -alias key -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Prefix Blocker" >/dev/null 2>&1
apksigner sign --v2-signing-enabled true --ks release.keystore --ks-pass pass:prefixblock --ks-key-alias key --out build/PrefixBlocker.apk build/aligned.apk
apksigner verify --verbose build/PrefixBlocker.apk | head -5
