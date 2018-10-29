package wizard.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

/**
 * Copyright (C) 2006-2017  AdMaster Co.Ltd.
 * All right reserved.
 *
 * @author: whitelilis@gmail.com on 18/10/29
 */
public class CommonUtil {
    public static void copyURLToFileForTmp(String targetDir, URL sourceURL) throws IOException {
        File orginFile = new File(sourceURL.getFile());
        File targetFile = new File(targetDir + File.separator + orginFile.getName());
        if (targetFile.exists()) {
            targetFile.delete();
        }
        FileUtils.copyURLToFile(sourceURL, targetFile);

        targetFile.deleteOnExit();
    }

}
