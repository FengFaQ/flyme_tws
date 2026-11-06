"""CI verification for the built APK.

Checks that the APK is a REAL LSPosed modern-Xposed module:
  1. META-INF/xposed/{java_init.list,module.prop,scope.list} are present.
  2. java_init.list names a class ACTUALLY DEFINED in classes.dex.
  3. The real framework API (io.github.libxposed.api.*) is compileOnly:
     its class DEFINITIONS are absent from the dex. A plain string grep is NOT
     sufficient -- "Lio/github/libxposed/api/XposedModule;" always appears as a
     type reference because ModuleMain extends XposedModule.
  4. Bundled service classes and the hook classes ARE present.

Only the dex class_defs table counts as a real class definition.
"""
import struct
import sys
import zipfile

REQUIRED_METADATA = (
    "META-INF/xposed/java_init.list",
    "META-INF/xposed/module.prop",
    "META-INF/xposed/scope.list",
)
REQUIRED_PRESENT = (
    "Lgithub/forest/tool/tws/io/ModuleMain;",
    "Lgithub/forest/tool/tws/io/hook/TWS/BluetoothHook;",
    "Lgithub/forest/tool/tws/io/hook/TWS/EarbudHook;",
    "Lio/github/libxposed/service/XposedService;",
    "Lio/github/libxposed/service/XposedProvider;",
)
REQUIRED_ABSENT = (
    "Lio/github/libxposed/api/XposedModule;",
    "Lio/github/libxposed/api/XposedInterfaceWrapper;",
    "Lio/github/libxposed/api/XposedInterface;",
)


def dex_class_definitions(dex):
    u32 = lambda o: struct.unpack_from("<I", dex, o)[0]
    string_ids_off = u32(0x3C)
    type_ids_off = u32(0x44)
    class_defs_size = u32(0x60)
    class_defs_off = u32(0x64)

    def uleb(off):
        result = shift = 0
        while True:
            b = dex[off]
            off += 1
            result |= (b & 0x7F) << shift
            if not (b & 0x80):
                return result, off
            shift += 7

    def string_at(idx):
        n, p = uleb(u32(string_ids_off + 4 * idx))
        return dex[p:p + n].decode("utf-8", "replace")

    def type_name(idx):
        return string_at(u32(type_ids_off + 4 * idx))

    return {type_name(u32(class_defs_off + 32 * k))
            for k in range(class_defs_size)}


def main(apk_path):
    ok = True
    with zipfile.ZipFile(apk_path) as z:
        names = set(z.namelist())

        print("=== LSPosed module metadata ===")
        for meta in REQUIRED_METADATA:
            present = meta in names
            print(("  OK   " if present else "  FAIL ") + meta)
            ok &= present
            if present:
                body = z.read(meta).decode("utf-8", "replace").strip()
                print("        " + body.replace("\n", " | "))

        print("=== classes.dex class definitions ===")
        defs = dex_class_definitions(z.read("classes.dex"))
        print("  class_defs count:", len(defs))

        for d in REQUIRED_PRESENT:
            present = d in defs
            print(("  OK   present: " if present else "  FAIL missing: ") + d)
            ok &= present

        for d in REQUIRED_ABSENT:
            present = d in defs
            print(("  FAIL bundled: " if present else "  OK   absent : ") + d)
            ok &= not present

        entry = z.read("META-INF/xposed/java_init.list").decode().strip().splitlines()[0].strip()
        desc = "L" + entry.replace(".", "/") + ";"
        present = desc in defs
        print(("  OK   entry class defined: " if present else "  FAIL entry class missing: ") + entry)
        ok &= present

    if not ok:
        print("::error::APK does not satisfy the real-LSPosed-module contract")
        return 1
    print("PASS: real framework API is compileOnly (definitions absent); "
          "module entry + hooks + service are present.")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("usage: verify_module.py <unsigned.apk>")
        sys.exit(2)
    sys.exit(main(sys.argv[1]))
