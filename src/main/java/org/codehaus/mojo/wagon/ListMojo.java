package org.codehaus.mojo.wagon;

import java.util.List;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

/**
 * Lists the content of the specified directory (remotePath) under a specified repository (url).
 */
@Mojo(name = "list", requiresProject = false)
public class ListMojo extends AbstractWagonListMojo {

    @Override
    protected void execute(Wagon wagon) throws WagonException {
        List files = wagonDownload.getFileList(wagon, this.getWagonFileSet(), this.getLog());

        for (Object file1 : files) {
            String file = (String) file1;
            getLog().info("\t" + file);
        }
    }
}
