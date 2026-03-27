package run.xbud;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.io.IOException;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class XbudCryptoServer extends AbstractJni {

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module sportModule;
    private final DvmClass sportJniUtilsObj;

    public XbudCryptoServer() {
        emulator = AndroidEmulatorBuilder.for64Bit().setProcessName("com.xbud.run").build();
        final Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));

        vm = emulator.createDalvikVM();
        vm.setJni(this);
        vm.setVerbose(true);

        DalvikModule sportDalvikModule = vm.loadLibrary(new File("src/main/resources/libsport.so"), true);
        sportModule = sportDalvikModule.getModule();

        vm.callJNI_OnLoad(emulator, sportModule);

        sportJniUtilsObj = vm.resolveClass("com/xbud/run/sport/SportJniUtils");
    }

    public String getSecretKey(int type) {
        return sportJniUtilsObj.callStaticJniMethodObject(
                emulator,
                "getSecretKey(I)Ljava/lang/String;",
                type
        ).getValue().toString();
    }

    public String md5(String str) {
        return sportJniUtilsObj.callStaticJniMethodObject(
                emulator,
                "md5(Ljava/lang/String;)Ljava/lang/String;",
                new StringObject(vm, str)
        ).getValue().toString();
    }

    public static void main(String[] args) {
        XbudCryptoServer server = new XbudCryptoServer();
        System.out.println("Test getSecretKey(4): " + server.getSecretKey(4));

        port(5001);
        Gson gson = new Gson();

        post("/sign/body", (req, res) -> {
            res.type("application/json");
            // TODO parse body and process md5/AES
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("key", server.getSecretKey(4));
            // Just mocking the JSON response
            return result.toString();
        });

        System.out.println("Unidbg Server running at http://127.0.0.1:5001/");
    }
}
