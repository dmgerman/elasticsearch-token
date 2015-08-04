begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|SuppressForbidden
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|PathUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URL
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URLClassLoader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|FileVisitResult
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|SimpleFileVisitor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|attribute
operator|.
name|BasicFileAttributes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|JarFile
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|jar
operator|.
name|Manifest
import|;
end_import

begin_comment
comment|/** Simple check for duplicate class files across the classpath */
end_comment

begin_class
DECL|class|JarHell
specifier|public
class|class
name|JarHell
block|{
comment|/** Simple driver class, can be used eg. from builds. Returns non-zero on jar-hell */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"command line tool"
argument_list|)
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"checking for jar hell..."
argument_list|)
expr_stmt|;
name|checkJarHell
argument_list|()
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"no jar hell found"
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks the current classloader for duplicate classes      * @throws IllegalStateException if jar hell was found      */
DECL|method|checkJarHell
specifier|public
specifier|static
name|void
name|checkJarHell
parameter_list|()
throws|throws
name|Exception
block|{
name|ClassLoader
name|loader
init|=
name|JarHell
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|loader
operator|instanceof
name|URLClassLoader
operator|==
literal|false
condition|)
block|{
return|return;
block|}
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|JarHell
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"java.class.path: {}"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.class.path"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"sun.boot.class.path: {}"
argument_list|,
name|System
operator|.
name|getProperty
argument_list|(
literal|"sun.boot.class.path"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"classloader urls: {}"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|loader
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|checkJarHell
argument_list|(
operator|(
operator|(
name|URLClassLoader
operator|)
name|loader
operator|)
operator|.
name|getURLs
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks the set of URLs for duplicate classes      * @throws IllegalStateException if jar hell was found      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"needs JarFile for speed, just reading entries"
argument_list|)
DECL|method|checkJarHell
specifier|public
specifier|static
name|void
name|checkJarHell
parameter_list|(
name|URL
name|urls
index|[]
parameter_list|)
throws|throws
name|Exception
block|{
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|JarHell
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// we don't try to be sneaky and use deprecated/internal/not portable stuff
comment|// like sun.boot.class.path, and with jigsaw we don't yet have a way to get
comment|// a "list" at all. So just exclude any elements underneath the java home
name|String
name|javaHome
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.home"
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"java.home: {}"
argument_list|,
name|javaHome
argument_list|)
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|clazzes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|32768
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|Path
argument_list|>
name|seenJars
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|URL
name|url
range|:
name|urls
control|)
block|{
specifier|final
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|url
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
comment|// exclude system resources
if|if
condition|(
name|path
operator|.
name|startsWith
argument_list|(
name|javaHome
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"excluding system resource: {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|path
operator|.
name|toString
argument_list|()
operator|.
name|endsWith
argument_list|(
literal|".jar"
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|seenJars
operator|.
name|add
argument_list|(
name|path
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"excluding duplicate classpath element: {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
continue|continue;
comment|// we can't fail because of sheistiness with joda-time
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"examining jar: {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
try|try
init|(
name|JarFile
name|file
init|=
operator|new
name|JarFile
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
init|)
block|{
name|Manifest
name|manifest
init|=
name|file
operator|.
name|getManifest
argument_list|()
decl_stmt|;
if|if
condition|(
name|manifest
operator|!=
literal|null
condition|)
block|{
name|checkManifest
argument_list|(
name|manifest
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
comment|// inspect entries
name|Enumeration
argument_list|<
name|JarEntry
argument_list|>
name|elements
init|=
name|file
operator|.
name|entries
argument_list|()
decl_stmt|;
while|while
condition|(
name|elements
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|String
name|entry
init|=
name|elements
operator|.
name|nextElement
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|endsWith
argument_list|(
literal|".class"
argument_list|)
condition|)
block|{
comment|// for jar format, the separator is defined as /
name|entry
operator|=
name|entry
operator|.
name|replace
argument_list|(
literal|'/'
argument_list|,
literal|'.'
argument_list|)
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|entry
operator|.
name|length
argument_list|()
operator|-
literal|6
argument_list|)
expr_stmt|;
name|checkClass
argument_list|(
name|clazzes
argument_list|,
name|entry
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"examining directory: {}"
argument_list|,
name|path
argument_list|)
expr_stmt|;
comment|// case for tests: where we have class files in the classpath
specifier|final
name|Path
name|root
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|url
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|sep
init|=
name|root
operator|.
name|getFileSystem
argument_list|()
operator|.
name|getSeparator
argument_list|()
decl_stmt|;
name|Files
operator|.
name|walkFileTree
argument_list|(
name|root
argument_list|,
operator|new
name|SimpleFileVisitor
argument_list|<
name|Path
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|FileVisitResult
name|visitFile
parameter_list|(
name|Path
name|file
parameter_list|,
name|BasicFileAttributes
name|attrs
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|entry
init|=
name|root
operator|.
name|relativize
argument_list|(
name|file
argument_list|)
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|entry
operator|.
name|endsWith
argument_list|(
literal|".class"
argument_list|)
condition|)
block|{
comment|// normalize with the os separator
name|entry
operator|=
name|entry
operator|.
name|replace
argument_list|(
name|sep
argument_list|,
literal|"."
argument_list|)
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|entry
operator|.
name|length
argument_list|()
operator|-
literal|6
argument_list|)
expr_stmt|;
name|checkClass
argument_list|(
name|clazzes
argument_list|,
name|entry
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
return|return
name|super
operator|.
name|visitFile
argument_list|(
name|file
argument_list|,
name|attrs
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/** inspect manifest for sure incompatibilities */
DECL|method|checkManifest
specifier|static
name|void
name|checkManifest
parameter_list|(
name|Manifest
name|manifest
parameter_list|,
name|Path
name|jar
parameter_list|)
block|{
comment|// give a nice error if jar requires a newer java version
name|String
name|targetVersion
init|=
name|manifest
operator|.
name|getMainAttributes
argument_list|()
operator|.
name|getValue
argument_list|(
literal|"X-Compile-Target-JDK"
argument_list|)
decl_stmt|;
if|if
condition|(
name|targetVersion
operator|!=
literal|null
condition|)
block|{
name|checkJavaVersion
argument_list|(
name|jar
operator|.
name|toString
argument_list|()
argument_list|,
name|targetVersion
argument_list|)
expr_stmt|;
block|}
comment|// give a nice error if jar is compiled against different es version
name|String
name|systemESVersion
init|=
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|targetESVersion
init|=
name|manifest
operator|.
name|getMainAttributes
argument_list|()
operator|.
name|getValue
argument_list|(
literal|"X-Compile-Elasticsearch-Version"
argument_list|)
decl_stmt|;
if|if
condition|(
name|targetESVersion
operator|!=
literal|null
operator|&&
name|targetESVersion
operator|.
name|equals
argument_list|(
name|systemESVersion
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|jar
operator|+
literal|" requires Elasticsearch "
operator|+
name|targetESVersion
operator|+
literal|", your system: "
operator|+
name|systemESVersion
argument_list|)
throw|;
block|}
block|}
comment|/**      * Checks that the java specification version {@code targetVersion}      * required by {@code resource} is compatible with the current installation.      */
DECL|method|checkJavaVersion
specifier|public
specifier|static
name|void
name|checkJavaVersion
parameter_list|(
name|String
name|resource
parameter_list|,
name|String
name|targetVersion
parameter_list|)
block|{
name|String
name|systemVersion
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.specification.version"
argument_list|)
decl_stmt|;
name|float
name|current
init|=
name|Float
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|float
name|target
init|=
name|Float
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
try|try
block|{
name|current
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|systemVersion
argument_list|)
expr_stmt|;
name|target
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|targetVersion
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
comment|// some spec changed, time for a more complex parser
block|}
if|if
condition|(
name|current
operator|<
name|target
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|resource
operator|+
literal|" requires Java "
operator|+
name|targetVersion
operator|+
literal|", your system: "
operator|+
name|systemVersion
argument_list|)
throw|;
block|}
block|}
DECL|method|checkClass
specifier|static
name|void
name|checkClass
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Path
argument_list|>
name|clazzes
parameter_list|,
name|String
name|clazz
parameter_list|,
name|Path
name|jarpath
parameter_list|)
block|{
name|Path
name|previous
init|=
name|clazzes
operator|.
name|put
argument_list|(
name|clazz
argument_list|,
name|jarpath
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|previous
operator|.
name|equals
argument_list|(
name|jarpath
argument_list|)
condition|)
block|{
if|if
condition|(
name|clazz
operator|.
name|startsWith
argument_list|(
literal|"org.apache.xmlbeans"
argument_list|)
condition|)
block|{
return|return;
comment|// https://issues.apache.org/jira/browse/XMLBEANS-499
block|}
comment|// throw a better exception in this ridiculous case.
comment|// unfortunately the zip file format allows this buggy possibility
comment|// UweSays: It can, but should be considered as bug :-)
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"jar hell!"
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"class: "
operator|+
name|clazz
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"exists multiple times in jar: "
operator|+
name|jarpath
operator|+
literal|" !!!!!!!!!"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|clazz
operator|.
name|startsWith
argument_list|(
literal|"org.apache.log4j"
argument_list|)
condition|)
block|{
return|return;
comment|// go figure, jar hell for what should be System.out.println...
block|}
if|if
condition|(
name|clazz
operator|.
name|equals
argument_list|(
literal|"org.joda.time.base.BaseDateTime"
argument_list|)
condition|)
block|{
return|return;
comment|// apparently this is intentional... clean this up
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"jar hell!"
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"class: "
operator|+
name|clazz
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"jar1: "
operator|+
name|previous
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"jar2: "
operator|+
name|jarpath
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

