import com.decompiler.Decompiler;
import com.decompiler.config.DecompilerConfig;
import com.decompiler.config.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class Test {
    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws IOException {
        DecompilerConfig config = new DecompilerConfig.Builder()
                .inputPath("/xxxx/code")
                .outputPath("/xxxx/output")
                .threadCount(8)
                .preserveStructure(true)
                .deleteClassFiles(true)
                .build();
        OptionsBuilder optionsBuilder = new OptionsBuilder();
        Map<String, Object> vineflowerOptions = optionsBuilder.build();
        Decompiler decompiler = new Decompiler(config, vineflowerOptions);
        long startTime = System.currentTimeMillis();
        decompiler.execute();

        long endTime = System.currentTimeMillis();
        double totalTime = (endTime - startTime) / 1000.0;
        logger.info("总耗时: " + totalTime + " 秒");
    }
}
