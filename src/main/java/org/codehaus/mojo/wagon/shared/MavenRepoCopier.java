package org.codehaus.mojo.wagon.shared;

import java.io.IOException;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

public interface MavenRepoCopier
{
    String ROLE = MavenRepoCopier.class.getName();

    String IN_PROCESS_MARKER = ".rip";

    String MD5 = "md5";

    String SHA1 = "sha1";

    String MAVEN_METADATA = "maven-metadata.xml";
    
    void copy( Wagon fromWagon, Wagon toWagon, Log logger )
        throws WagonException, IOException;
}
