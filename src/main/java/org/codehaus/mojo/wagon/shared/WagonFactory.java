package org.codehaus.mojo.wagon.shared;

import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;

public interface WagonFactory {
    Wagon create(String url, String serverId, Settings settings) throws WagonException;
}
