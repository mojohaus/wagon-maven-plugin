package org.codehaus.mojo.wagon.shared;

import org.codehaus.plexus.util.StringUtils;

public class WagonUtils {

    public static WagonFileSet getWagonFileSet(
            String fromDir, String includes, String excludes, boolean isCaseSensitive, String toDir) {
        WagonFileSet fileSet = new WagonFileSet();
        fileSet.setDirectory(fromDir);

        if (!StringUtils.isBlank(includes)) {
            fileSet.setIncludes(StringUtils.split(includes, ","));
        }

        if (!StringUtils.isBlank(excludes)) {
            fileSet.setExcludes(StringUtils.split(excludes, ","));
        }

        fileSet.setCaseSensitive(isCaseSensitive);

        fileSet.setOutputDirectory(toDir);

        return fileSet;
    }
}
