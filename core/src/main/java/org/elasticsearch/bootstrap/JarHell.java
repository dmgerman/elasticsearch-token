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
class|class
name|JarHell
block|{
comment|/**      * Checks the current classloader for duplicate classes      * @throws IllegalStateException if jar hell was found      */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"needs JarFile for speed, just reading entries"
argument_list|)
DECL|method|checkJarHell
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
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|URL
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
name|String
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
operator|(
operator|(
name|URLClassLoader
operator|)
name|loader
operator|)
operator|.
name|getURLs
argument_list|()
control|)
block|{
name|String
name|path
init|=
name|url
operator|.
name|getPath
argument_list|()
decl_stmt|;
if|if
condition|(
name|path
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
continue|continue;
comment|// we can't fail because of sheistiness with joda-time
block|}
try|try
init|(
name|JarFile
name|file
init|=
operator|new
name|JarFile
argument_list|(
name|url
operator|.
name|getPath
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
comment|// inspect Manifest: give a nice error if jar requires a newer java version
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
name|path
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
name|url
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
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
name|url
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
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"proper use of URL to reduce noise"
argument_list|)
DECL|method|checkClass
specifier|static
name|void
name|checkClass
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|URL
argument_list|>
name|clazzes
parameter_list|,
name|String
name|clazz
parameter_list|,
name|URL
name|url
parameter_list|)
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
name|URL
name|previous
init|=
name|clazzes
operator|.
name|put
argument_list|(
name|clazz
argument_list|,
name|url
argument_list|)
decl_stmt|;
if|if
condition|(
name|previous
operator|!=
literal|null
condition|)
block|{
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
operator|.
name|getPath
argument_list|()
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
operator|+
literal|"jar2: "
operator|+
name|url
operator|.
name|getPath
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit
