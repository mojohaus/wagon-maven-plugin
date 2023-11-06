package org.codehaus.mojo.wagon.shared;

/**
 * Continuation type is used to configure the download/upload continuation
 */
public enum ContinuationType {
  /** no continuation (old default)*/
  NONE,
  /** skip existing files */
  ONLY_MISSING

}
