package org.codehaus.mojo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.mojo.PathParserUtil;
import org.codehaus.mojo.ResourceDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.plugin.MojoExecutionException;

public class PathParserUtilTest
    extends TestCase
{
    private boolean isCaseSensitive = false;

    public void testNoWildCards()
        throws Exception
    {
        assureValid( "a/b/c/", "a/b/c/", null );
        assureValid( "a/b/c", "a/b/c", null );
        assureValid( "/", "/", null );
        assureValid( "", "", null );
    }

    public void testOneWildCard()
        throws Exception
    {
        assureValid( "a/b/c/*", "a/b/c/", "*" );
        assureValid( "/*", "/", "*" );
        assureValid( "*", "", "*" );
        assureValid( "a/b/c/?", "a/b/c/", "?" );
        assureValid( "/?", "/", "?" );
        assureValid( "?", "", "?" );
    }

    public void testMixedWildCards()
        throws Exception
    {
        assureValid( "a/b/c/*.?.*", "a/b/c/", "*.?.*" );
        assureValid( "/*.?.*", "/", "*.?.*" );
        assureValid( "*.?.*", "", "*.?.*" );
        assureValid( "a/b/c/SomeText*.MoreText?.*", "a/b/c/", "SomeText*.MoreText?.*" );
        assureValid( "/SomeText*.MoreText?.*", "/", "SomeText*.MoreText?.*" );
        assureValid( "SomeText*.MoreText?.*", "", "SomeText*.MoreText?.*" );
    }

    public void testInvalidWildCards()
        throws Exception
    {
        assureInvalid( "a/b/*/c/" );
        assureInvalid( "*/c/" );
        assureInvalid( "*/" );
        assureInvalid( "*/" );
    }

    public void testValidFiles()
        throws Exception
    {
        String path = getExistingResourcePath();
        assureValidFiles( path, Collections.singleton( new File( path ) ) );

        ExistingResources res = getExistingResourcesAndWildcard();
        assureValidFiles( res.path, res.matchedFiles );
    }

    public void testCase()
        throws Exception
    {
        String path = getExistingResourcePath();
        assureValidFiles( path, Collections.singleton( new File( path ) ) );

        ExistingResources res = getExistingResourcesAndWildcard();
        assureValidFiles( res.path, res.matchedFiles );

        isCaseSensitive = true;
        String oldPath = res.path;
        res.invertPathCase();
        assertFalse( oldPath + " to " + res.path, oldPath.equals( res.path ) ); // case invertion worked

        assureInvalidFiles( res.path, res.matchedFiles );
    }

    /**
     * returns a pointer to an existing file or directory as a relative path
     */
    private String getExistingResourcePath()
    {
        File curDir = new File( "." );
        String list[] = curDir.list();
        if ( list == null || list.length == 0 )
        {
            if ( curDir.exists() )
            {
                // return the path looking like: ../parentDir/c* for curDir
                return "../" + curDir.getParentFile().getName() + '/' + curDir.getName();
            }
            else
            {
                return null;
            }
        }

        // return a path looking like: curDir/c* for childResource
        return curDir.getName() + '/' + list[0];

    }

    /**
     * returns a pointer to an existing file or directory as a wildcard
     * expression and a set of Files that will match that wildcard expression
     */
    private ExistingResources getExistingResourcesAndWildcard()
        throws IOException
    {
        File curDir = new File( "." );
        String list[] = curDir.list();
        if ( list == null || list.length == 0 )
        {
            if ( curDir.exists() )
            {
                // return the path looking like: ../parentDir/c* for curDir
                return new ExistingResources( "../" + curDir.getParentFile().getName(), toWildCard( curDir.getName() ),
                                              isCaseSensitive );
            }
            else
            {
                return null;
            }
        }

        // return a path looking like: curDir/c* for childResource
        return new ExistingResources( curDir.getName(), toWildCard( list[0] ), isCaseSensitive );
    }

    private String toWildCard( String fileName )
    {
        char array[] = fileName.toCharArray();
        String wildCard = "";
        for ( int i = 0; i < array.length; i++ )
        {
            wildCard += array[i];
            if ( Character.isLetter( array[0] ) )
            {
                break;
            }
        }
        wildCard += "*";
        return wildCard;
    }

    /**
     * makes sure that the given path will match the intended File
     */
    private void assureValidFiles( String path, Set expectedFiles )
        throws MojoExecutionException
    {
        Set set = PathParserUtil.toFiles( path, isCaseSensitive );
        assertEquals( expectedFiles, set );
    }

    private void assureInvalidFiles( String path, Set expectedFiles )
        throws MojoExecutionException
    {
        Set set = PathParserUtil.toFiles( path, isCaseSensitive );
        assertFalse( expectedFiles + " to " + set, expectedFiles.equals( set ) );
    }

    /**
     * makes sure that the given string is parsed properly into the given path
     * and given wildcard.
     */
    private void assureValid( String string, String path, String wildcard )
        throws MojoExecutionException
    {
        ResourceDescriptor descr = new ResourceDescriptor( string, isCaseSensitive );
        assertEquals( path, descr.path );
        assertEquals( wildcard, descr.wildcard );

        string = string.replace( '/', '\\' );

        descr = new ResourceDescriptor( string, isCaseSensitive );
        assertEquals( path.replace( '/', '\\' ), descr.path );
        assertEquals( wildcard, descr.wildcard );
    }

    /**
     * makes sure that the given path can not be parsed or is invalid
     */
    private void assureInvalid( String path )
    {
        try
        {
            new ResourceDescriptor( path, isCaseSensitive );
            fail( "Path is invalid: " + path );
        }
        catch ( MojoExecutionException e )
        {
            // expected
        }
    }

    private static class ExistingResources
    {
        private String path;

        private final Set matchedFiles = new HashSet();

        ExistingResources( String parentPath, String wildcard, boolean isCaseSensitive )
        {
            this.path = parentPath + '/' + wildcard;
            PathParserUtil.getMatchingChildren( new File( parentPath ), wildcard, isCaseSensitive, matchedFiles );
        }

        public void invertPathCase()
        {
            char[] array = path.toCharArray();
            for ( int i = 0; i < array.length; i++ )
            {
                if ( Character.isLetter( array[i] ) )
                {
                    if ( Character.isLowerCase( array[i] ) )
                        array[i] = Character.toUpperCase( array[i] );
                    else
                        array[i] = Character.toLowerCase( array[i] );
                }
            }
            path = new String( array );
        }
    }
}
