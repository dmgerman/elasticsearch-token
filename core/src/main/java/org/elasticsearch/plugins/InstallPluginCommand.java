begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

begin_import
import|import
name|joptsimple
operator|.
name|OptionSet
import|;
end_import

begin_import
import|import
name|joptsimple
operator|.
name|OptionSpec
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Build
import|;
end_import

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
name|bootstrap
operator|.
name|JarHell
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|Command
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|ExitCodes
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
name|cli
operator|.
name|CliTool
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
name|cli
operator|.
name|Terminal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|UserError
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
name|hash
operator|.
name|MessageDigests
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
name|FileSystemUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
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
name|URLDecoder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|DirectoryStream
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
name|StandardCopyOption
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
name|PosixFileAttributeView
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
name|PosixFilePermission
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|zip
operator|.
name|ZipEntry
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|zip
operator|.
name|ZipInputStream
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableSet
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cli
operator|.
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|set
operator|.
name|Sets
operator|.
name|newHashSet
import|;
end_import

begin_comment
comment|/**  * A command for the plugin cli to install a plugin into elasticsearch.  *  * The install command takes a plugin id, which may be any of the following:  *<ul>  *<li>An official elasticsearch plugin name</li>  *<li>Maven coordinates to a plugin zip</li>  *<li>A URL to a plugin zip</li>  *</ul>  *  * Plugins are packaged as zip files. Each packaged plugin must contain a  * plugin properties file. See {@link PluginInfo}.  *<p>  * The installation process first extracts the plugin files into a temporary  * directory in order to verify the plugin satisfies the following requirements:  *<ul>  *<li>Jar hell does not exist, either between the plugin's own jars, or with elasticsearch</li>  *<li>The plugin is not a module already provided with elasticsearch</li>  *<li>If the plugin contains extra security permissions, the policy file is validated</li>  *</ul>  *<p>  * A plugin may also contain an optional {@code bin} directory which contains scripts. The  * scripts will be installed into a subdirectory of the elasticsearch bin directory, using  * the name of the plugin, and the scripts will be marked executable.  *<p>  * A plugin may also contain an optional {@code config} directory which contains configuration  * files specific to the plugin. The config files be installed into a subdirectory of the  * elasticsearch config directory, using the name of the plugin. If any files to be installed  * already exist, they will be skipped.  */
end_comment

begin_class
DECL|class|InstallPluginCommand
class|class
name|InstallPluginCommand
extends|extends
name|Command
block|{
DECL|field|PROPERTY_SUPPORT_STAGING_URLS
specifier|private
specifier|static
specifier|final
name|String
name|PROPERTY_SUPPORT_STAGING_URLS
init|=
literal|"es.plugins.staging"
decl_stmt|;
comment|// TODO: make this a resource file generated by gradle
DECL|field|MODULES
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|MODULES
init|=
name|unmodifiableSet
argument_list|(
name|newHashSet
argument_list|(
literal|"lang-expression"
argument_list|,
literal|"lang-groovy"
argument_list|)
argument_list|)
decl_stmt|;
comment|// TODO: make this a resource file generated by gradle
DECL|field|OFFICIAL_PLUGINS
specifier|static
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|OFFICIAL_PLUGINS
init|=
name|unmodifiableSet
argument_list|(
operator|new
name|LinkedHashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"analysis-icu"
argument_list|,
literal|"analysis-kuromoji"
argument_list|,
literal|"analysis-phonetic"
argument_list|,
literal|"analysis-smartcn"
argument_list|,
literal|"analysis-stempel"
argument_list|,
literal|"delete-by-query"
argument_list|,
literal|"discovery-azure"
argument_list|,
literal|"discovery-ec2"
argument_list|,
literal|"discovery-gce"
argument_list|,
literal|"lang-javascript"
argument_list|,
literal|"lang-painless"
argument_list|,
literal|"lang-python"
argument_list|,
literal|"mapper-attachments"
argument_list|,
literal|"mapper-murmur3"
argument_list|,
literal|"mapper-size"
argument_list|,
literal|"repository-azure"
argument_list|,
literal|"repository-hdfs"
argument_list|,
literal|"repository-s3"
argument_list|,
literal|"store-smb"
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|env
specifier|private
specifier|final
name|Environment
name|env
decl_stmt|;
DECL|field|batchOption
specifier|private
specifier|final
name|OptionSpec
argument_list|<
name|Void
argument_list|>
name|batchOption
decl_stmt|;
DECL|field|arguments
specifier|private
specifier|final
name|OptionSpec
argument_list|<
name|String
argument_list|>
name|arguments
decl_stmt|;
DECL|method|InstallPluginCommand
name|InstallPluginCommand
parameter_list|(
name|Environment
name|env
parameter_list|)
block|{
name|super
argument_list|(
literal|"Install a plugin"
argument_list|)
expr_stmt|;
name|this
operator|.
name|env
operator|=
name|env
expr_stmt|;
name|this
operator|.
name|batchOption
operator|=
name|parser
operator|.
name|acceptsAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"b"
argument_list|,
literal|"batch"
argument_list|)
argument_list|,
literal|"Enable batch mode explicitly, automatic confirmation of security permission"
argument_list|)
expr_stmt|;
name|this
operator|.
name|arguments
operator|=
name|parser
operator|.
name|nonOptions
argument_list|(
literal|"plugin id"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|printAdditionalHelp
specifier|protected
name|void
name|printAdditionalHelp
parameter_list|(
name|Terminal
name|terminal
parameter_list|)
block|{
name|terminal
operator|.
name|println
argument_list|(
literal|"The following official plugins may be installed by name:"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|plugin
range|:
name|OFFICIAL_PLUGINS
control|)
block|{
name|terminal
operator|.
name|println
argument_list|(
literal|"  "
operator|+
name|plugin
argument_list|)
expr_stmt|;
block|}
name|terminal
operator|.
name|println
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|protected
name|int
name|execute
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|OptionSet
name|options
parameter_list|)
throws|throws
name|Exception
block|{
comment|// TODO: in jopt-simple 5.0 we can enforce a min/max number of positional args
name|List
argument_list|<
name|String
argument_list|>
name|args
init|=
name|arguments
operator|.
name|values
argument_list|(
name|options
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
literal|"Must supply a single plugin id argument"
argument_list|)
throw|;
block|}
name|String
name|pluginId
init|=
name|args
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|boolean
name|isBatch
init|=
name|options
operator|.
name|has
argument_list|(
name|batchOption
argument_list|)
operator|||
name|System
operator|.
name|console
argument_list|()
operator|==
literal|null
decl_stmt|;
name|execute
argument_list|(
name|terminal
argument_list|,
name|pluginId
argument_list|,
name|isBatch
argument_list|)
expr_stmt|;
return|return
name|ExitCodes
operator|.
name|OK
return|;
block|}
comment|// pkg private for testing
DECL|method|execute
name|void
name|execute
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|String
name|pluginId
parameter_list|,
name|boolean
name|isBatch
parameter_list|)
throws|throws
name|Exception
block|{
comment|// TODO: remove this leniency!! is it needed anymore?
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|terminal
operator|.
name|println
argument_list|(
literal|"Plugins directory ["
operator|+
name|env
operator|.
name|pluginsFile
argument_list|()
operator|+
literal|"] does not exist. Creating..."
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Path
name|pluginZip
init|=
name|download
argument_list|(
name|terminal
argument_list|,
name|pluginId
argument_list|,
name|env
operator|.
name|tmpFile
argument_list|()
argument_list|)
decl_stmt|;
name|Path
name|extractedZip
init|=
name|unzip
argument_list|(
name|pluginZip
argument_list|,
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
decl_stmt|;
name|install
argument_list|(
name|terminal
argument_list|,
name|isBatch
argument_list|,
name|extractedZip
argument_list|)
expr_stmt|;
block|}
comment|/** Downloads the plugin and returns the file it was downloaded to. */
DECL|method|download
specifier|private
name|Path
name|download
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|String
name|pluginId
parameter_list|,
name|Path
name|tmpDir
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|OFFICIAL_PLUGINS
operator|.
name|contains
argument_list|(
name|pluginId
argument_list|)
condition|)
block|{
specifier|final
name|String
name|version
init|=
name|Version
operator|.
name|CURRENT
operator|.
name|toString
argument_list|()
decl_stmt|;
specifier|final
name|String
name|url
decl_stmt|;
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|PROPERTY_SUPPORT_STAGING_URLS
argument_list|,
literal|"false"
argument_list|)
operator|.
name|equals
argument_list|(
literal|"true"
argument_list|)
condition|)
block|{
name|url
operator|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"https://download.elastic.co/elasticsearch/staging/%1$s-%2$s/org/elasticsearch/plugin/%3$s/%1$s/%3$s-%1$s.zip"
argument_list|,
name|version
argument_list|,
name|Build
operator|.
name|CURRENT
operator|.
name|shortHash
argument_list|()
argument_list|,
name|pluginId
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|url
operator|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"https://download.elastic.co/elasticsearch/release/org/elasticsearch/plugin/%1$s/%2$s/%1$s-%2$s.zip"
argument_list|,
name|pluginId
argument_list|,
name|version
argument_list|)
expr_stmt|;
block|}
name|terminal
operator|.
name|println
argument_list|(
literal|"-> Downloading "
operator|+
name|pluginId
operator|+
literal|" from elastic"
argument_list|)
expr_stmt|;
return|return
name|downloadZipAndChecksum
argument_list|(
name|url
argument_list|,
name|tmpDir
argument_list|)
return|;
block|}
comment|// now try as maven coordinates, a valid URL would only have a colon and slash
name|String
index|[]
name|coordinates
init|=
name|pluginId
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|coordinates
operator|.
name|length
operator|==
literal|3
operator|&&
name|pluginId
operator|.
name|contains
argument_list|(
literal|"/"
argument_list|)
operator|==
literal|false
condition|)
block|{
name|String
name|mavenUrl
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"https://repo1.maven.org/maven2/%1$s/%2$s/%3$s/%2$s-%3$s.zip"
argument_list|,
name|coordinates
index|[
literal|0
index|]
operator|.
name|replace
argument_list|(
literal|"."
argument_list|,
literal|"/"
argument_list|)
comment|/* groupId */
argument_list|,
name|coordinates
index|[
literal|1
index|]
comment|/* artifactId */
argument_list|,
name|coordinates
index|[
literal|2
index|]
comment|/* version */
argument_list|)
decl_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"-> Downloading "
operator|+
name|pluginId
operator|+
literal|" from maven central"
argument_list|)
expr_stmt|;
return|return
name|downloadZipAndChecksum
argument_list|(
name|mavenUrl
argument_list|,
name|tmpDir
argument_list|)
return|;
block|}
comment|// fall back to plain old URL
name|terminal
operator|.
name|println
argument_list|(
literal|"-> Downloading "
operator|+
name|URLDecoder
operator|.
name|decode
argument_list|(
name|pluginId
argument_list|,
literal|"UTF-8"
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|downloadZip
argument_list|(
name|pluginId
argument_list|,
name|tmpDir
argument_list|)
return|;
block|}
comment|/** Downloads a zip from the url, into a temp file under the given temp dir. */
DECL|method|downloadZip
specifier|private
name|Path
name|downloadZip
parameter_list|(
name|String
name|urlString
parameter_list|,
name|Path
name|tmpDir
parameter_list|)
throws|throws
name|IOException
block|{
name|URL
name|url
init|=
operator|new
name|URL
argument_list|(
name|urlString
argument_list|)
decl_stmt|;
name|Path
name|zip
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
name|tmpDir
argument_list|,
literal|null
argument_list|,
literal|".zip"
argument_list|)
decl_stmt|;
try|try
init|(
name|InputStream
name|in
init|=
name|url
operator|.
name|openStream
argument_list|()
init|)
block|{
comment|// must overwrite since creating the temp file above actually created the file
name|Files
operator|.
name|copy
argument_list|(
name|in
argument_list|,
name|zip
argument_list|,
name|StandardCopyOption
operator|.
name|REPLACE_EXISTING
argument_list|)
expr_stmt|;
block|}
return|return
name|zip
return|;
block|}
comment|/** Downloads a zip from the url, as well as a SHA1 checksum, and checks the checksum. */
DECL|method|downloadZipAndChecksum
specifier|private
name|Path
name|downloadZipAndChecksum
parameter_list|(
name|String
name|urlString
parameter_list|,
name|Path
name|tmpDir
parameter_list|)
throws|throws
name|Exception
block|{
name|Path
name|zip
init|=
name|downloadZip
argument_list|(
name|urlString
argument_list|,
name|tmpDir
argument_list|)
decl_stmt|;
name|URL
name|checksumUrl
init|=
operator|new
name|URL
argument_list|(
name|urlString
operator|+
literal|".sha1"
argument_list|)
decl_stmt|;
specifier|final
name|String
name|expectedChecksum
decl_stmt|;
try|try
init|(
name|InputStream
name|in
init|=
name|checksumUrl
operator|.
name|openStream
argument_list|()
init|)
block|{
name|BufferedReader
name|checksumReader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|in
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
name|expectedChecksum
operator|=
name|checksumReader
operator|.
name|readLine
argument_list|()
expr_stmt|;
if|if
condition|(
name|checksumReader
operator|.
name|readLine
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|IO_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"Invalid checksum file at "
operator|+
name|checksumUrl
argument_list|)
throw|;
block|}
block|}
name|byte
index|[]
name|zipbytes
init|=
name|Files
operator|.
name|readAllBytes
argument_list|(
name|zip
argument_list|)
decl_stmt|;
name|String
name|gotChecksum
init|=
name|MessageDigests
operator|.
name|toHexString
argument_list|(
name|MessageDigests
operator|.
name|sha1
argument_list|()
operator|.
name|digest
argument_list|(
name|zipbytes
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|expectedChecksum
operator|.
name|equals
argument_list|(
name|gotChecksum
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|IO_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"SHA1 mismatch, expected "
operator|+
name|expectedChecksum
operator|+
literal|" but got "
operator|+
name|gotChecksum
argument_list|)
throw|;
block|}
return|return
name|zip
return|;
block|}
DECL|method|unzip
specifier|private
name|Path
name|unzip
parameter_list|(
name|Path
name|zip
parameter_list|,
name|Path
name|pluginsDir
parameter_list|)
throws|throws
name|IOException
throws|,
name|UserError
block|{
comment|// unzip plugin to a staging temp dir
name|Path
name|target
init|=
name|Files
operator|.
name|createTempDirectory
argument_list|(
name|pluginsDir
argument_list|,
literal|".installing-"
argument_list|)
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|target
argument_list|)
expr_stmt|;
name|boolean
name|hasEsDir
init|=
literal|false
decl_stmt|;
comment|// TODO: we should wrap this in a try/catch and try deleting the target dir on failure?
try|try
init|(
name|ZipInputStream
name|zipInput
init|=
operator|new
name|ZipInputStream
argument_list|(
name|Files
operator|.
name|newInputStream
argument_list|(
name|zip
argument_list|)
argument_list|)
init|)
block|{
name|ZipEntry
name|entry
decl_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
literal|8192
index|]
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|zipInput
operator|.
name|getNextEntry
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|entry
operator|.
name|getName
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"elasticsearch/"
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// only extract the elasticsearch directory
continue|continue;
block|}
name|hasEsDir
operator|=
literal|true
expr_stmt|;
name|Path
name|targetFile
init|=
name|target
operator|.
name|resolve
argument_list|(
name|entry
operator|.
name|getName
argument_list|()
operator|.
name|substring
argument_list|(
literal|"elasticsearch/"
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
comment|// TODO: handle name being an absolute path
comment|// be on the safe side: do not rely on that directories are always extracted
comment|// before their children (although this makes sense, but is it guaranteed?)
name|Files
operator|.
name|createDirectories
argument_list|(
name|targetFile
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|entry
operator|.
name|isDirectory
argument_list|()
operator|==
literal|false
condition|)
block|{
try|try
init|(
name|OutputStream
name|out
init|=
name|Files
operator|.
name|newOutputStream
argument_list|(
name|targetFile
argument_list|)
init|)
block|{
name|int
name|len
decl_stmt|;
while|while
condition|(
operator|(
name|len
operator|=
name|zipInput
operator|.
name|read
argument_list|(
name|buffer
argument_list|)
operator|)
operator|>=
literal|0
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
name|buffer
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|zipInput
operator|.
name|closeEntry
argument_list|()
expr_stmt|;
block|}
block|}
name|Files
operator|.
name|delete
argument_list|(
name|zip
argument_list|)
expr_stmt|;
if|if
condition|(
name|hasEsDir
operator|==
literal|false
condition|)
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|target
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|DATA_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"`elasticsearch` directory is missing in the plugin zip"
argument_list|)
throw|;
block|}
return|return
name|target
return|;
block|}
comment|/** Load information about the plugin, and verify it can be installed with no errors. */
DECL|method|verify
specifier|private
name|PluginInfo
name|verify
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|Path
name|pluginRoot
parameter_list|,
name|boolean
name|isBatch
parameter_list|)
throws|throws
name|Exception
block|{
comment|// read and validate the plugin descriptor
name|PluginInfo
name|info
init|=
name|PluginInfo
operator|.
name|readFromProperties
argument_list|(
name|pluginRoot
argument_list|)
decl_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|VERBOSE
argument_list|,
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// don't let luser install plugin as a module...
comment|// they might be unavoidably in maven central and are packaged up the same way)
if|if
condition|(
name|MODULES
operator|.
name|contains
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|USAGE
operator|.
name|status
argument_list|()
argument_list|,
literal|"plugin '"
operator|+
name|info
operator|.
name|getName
argument_list|()
operator|+
literal|"' cannot be installed like this, it is a system module"
argument_list|)
throw|;
block|}
comment|// check for jar hell before any copying
name|jarHellCheck
argument_list|(
name|pluginRoot
argument_list|,
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|info
operator|.
name|isIsolated
argument_list|()
argument_list|)
expr_stmt|;
comment|// read optional security policy (extra permissions)
comment|// if it exists, confirm or warn the user
name|Path
name|policy
init|=
name|pluginRoot
operator|.
name|resolve
argument_list|(
name|PluginInfo
operator|.
name|ES_PLUGIN_POLICY
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|policy
argument_list|)
condition|)
block|{
name|PluginSecurity
operator|.
name|readPolicy
argument_list|(
name|policy
argument_list|,
name|terminal
argument_list|,
name|env
argument_list|,
name|isBatch
argument_list|)
expr_stmt|;
block|}
return|return
name|info
return|;
block|}
comment|/** check a candidate plugin for jar hell before installing it */
DECL|method|jarHellCheck
specifier|private
name|void
name|jarHellCheck
parameter_list|(
name|Path
name|candidate
parameter_list|,
name|Path
name|pluginsDir
parameter_list|,
name|boolean
name|isolated
parameter_list|)
throws|throws
name|Exception
block|{
comment|// create list of current jars in classpath
specifier|final
name|List
argument_list|<
name|URL
argument_list|>
name|jars
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|jars
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|JarHell
operator|.
name|parseClassPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// read existing bundles. this does some checks on the installation too.
name|List
argument_list|<
name|PluginsService
operator|.
name|Bundle
argument_list|>
name|bundles
init|=
name|PluginsService
operator|.
name|getPluginBundles
argument_list|(
name|pluginsDir
argument_list|)
decl_stmt|;
comment|// if we aren't isolated, we need to jarhellcheck against any other non-isolated plugins
comment|// that's always the first bundle
if|if
condition|(
name|isolated
operator|==
literal|false
condition|)
block|{
name|jars
operator|.
name|addAll
argument_list|(
name|bundles
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|urls
argument_list|)
expr_stmt|;
block|}
comment|// add plugin jars to the list
name|Path
name|pluginJars
index|[]
init|=
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|candidate
argument_list|,
literal|"*.jar"
argument_list|)
decl_stmt|;
for|for
control|(
name|Path
name|jar
range|:
name|pluginJars
control|)
block|{
name|jars
operator|.
name|add
argument_list|(
name|jar
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// TODO: no jars should be an error
comment|// TODO: verify the classname exists in one of the jars!
comment|// check combined (current classpath + new jars to-be-added)
name|JarHell
operator|.
name|checkJarHell
argument_list|(
name|jars
operator|.
name|toArray
argument_list|(
operator|new
name|URL
index|[
name|jars
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Installs the plugin from {@code tmpRoot} into the plugins dir.      * If the plugin has a bin dir and/or a config dir, those are copied.      */
DECL|method|install
specifier|private
name|void
name|install
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|boolean
name|isBatch
parameter_list|,
name|Path
name|tmpRoot
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|Path
argument_list|>
name|deleteOnFailure
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|deleteOnFailure
operator|.
name|add
argument_list|(
name|tmpRoot
argument_list|)
expr_stmt|;
try|try
block|{
name|PluginInfo
name|info
init|=
name|verify
argument_list|(
name|terminal
argument_list|,
name|tmpRoot
argument_list|,
name|isBatch
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|destination
init|=
name|env
operator|.
name|pluginsFile
argument_list|()
operator|.
name|resolve
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|destination
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|USAGE
operator|.
name|status
argument_list|()
argument_list|,
literal|"plugin directory "
operator|+
name|destination
operator|.
name|toAbsolutePath
argument_list|()
operator|+
literal|" already exists. To update the plugin, uninstall it first using 'remove "
operator|+
name|info
operator|.
name|getName
argument_list|()
operator|+
literal|"' command"
argument_list|)
throw|;
block|}
name|Path
name|tmpBinDir
init|=
name|tmpRoot
operator|.
name|resolve
argument_list|(
literal|"bin"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|tmpBinDir
argument_list|)
condition|)
block|{
name|Path
name|destBinDir
init|=
name|env
operator|.
name|binFile
argument_list|()
operator|.
name|resolve
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|deleteOnFailure
operator|.
name|add
argument_list|(
name|destBinDir
argument_list|)
expr_stmt|;
name|installBin
argument_list|(
name|info
argument_list|,
name|tmpBinDir
argument_list|,
name|destBinDir
argument_list|)
expr_stmt|;
block|}
name|Path
name|tmpConfigDir
init|=
name|tmpRoot
operator|.
name|resolve
argument_list|(
literal|"config"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|tmpConfigDir
argument_list|)
condition|)
block|{
comment|// some files may already exist, and we don't remove plugin config files on plugin removal,
comment|// so any installed config files are left on failure too
name|installConfig
argument_list|(
name|info
argument_list|,
name|tmpConfigDir
argument_list|,
name|env
operator|.
name|configFile
argument_list|()
operator|.
name|resolve
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Files
operator|.
name|move
argument_list|(
name|tmpRoot
argument_list|,
name|destination
argument_list|,
name|StandardCopyOption
operator|.
name|ATOMIC_MOVE
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|println
argument_list|(
literal|"-> Installed "
operator|+
name|info
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|installProblem
parameter_list|)
block|{
try|try
block|{
name|IOUtils
operator|.
name|rm
argument_list|(
name|deleteOnFailure
operator|.
name|toArray
argument_list|(
operator|new
name|Path
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exceptionWhileRemovingFiles
parameter_list|)
block|{
name|installProblem
operator|.
name|addSuppressed
argument_list|(
name|exceptionWhileRemovingFiles
argument_list|)
expr_stmt|;
block|}
throw|throw
name|installProblem
throw|;
block|}
block|}
comment|/** Copies the files from {@code tmpBinDir} into {@code destBinDir}, along with permissions from dest dirs parent. */
DECL|method|installBin
specifier|private
name|void
name|installBin
parameter_list|(
name|PluginInfo
name|info
parameter_list|,
name|Path
name|tmpBinDir
parameter_list|,
name|Path
name|destBinDir
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|tmpBinDir
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|IO_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"bin in plugin "
operator|+
name|info
operator|.
name|getName
argument_list|()
operator|+
literal|" is not a directory"
argument_list|)
throw|;
block|}
name|Files
operator|.
name|createDirectory
argument_list|(
name|destBinDir
argument_list|)
expr_stmt|;
comment|// setup file attributes for the installed files to those of the parent dir
name|Set
argument_list|<
name|PosixFilePermission
argument_list|>
name|perms
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|PosixFileAttributeView
name|binAttrs
init|=
name|Files
operator|.
name|getFileAttributeView
argument_list|(
name|destBinDir
operator|.
name|getParent
argument_list|()
argument_list|,
name|PosixFileAttributeView
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|binAttrs
operator|!=
literal|null
condition|)
block|{
name|perms
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|binAttrs
operator|.
name|readAttributes
argument_list|()
operator|.
name|permissions
argument_list|()
argument_list|)
expr_stmt|;
comment|// setting execute bits, since this just means "the file is executable", and actual execution requires read
name|perms
operator|.
name|add
argument_list|(
name|PosixFilePermission
operator|.
name|OWNER_EXECUTE
argument_list|)
expr_stmt|;
name|perms
operator|.
name|add
argument_list|(
name|PosixFilePermission
operator|.
name|GROUP_EXECUTE
argument_list|)
expr_stmt|;
name|perms
operator|.
name|add
argument_list|(
name|PosixFilePermission
operator|.
name|OTHERS_EXECUTE
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|tmpBinDir
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|srcFile
range|:
name|stream
control|)
block|{
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|srcFile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|DATA_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"Directories not allowed in bin dir for plugin "
operator|+
name|info
operator|.
name|getName
argument_list|()
operator|+
literal|", found "
operator|+
name|srcFile
operator|.
name|getFileName
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|destFile
init|=
name|destBinDir
operator|.
name|resolve
argument_list|(
name|tmpBinDir
operator|.
name|relativize
argument_list|(
name|srcFile
argument_list|)
argument_list|)
decl_stmt|;
name|Files
operator|.
name|copy
argument_list|(
name|srcFile
argument_list|,
name|destFile
argument_list|)
expr_stmt|;
if|if
condition|(
name|perms
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|PosixFileAttributeView
name|view
init|=
name|Files
operator|.
name|getFileAttributeView
argument_list|(
name|destFile
argument_list|,
name|PosixFileAttributeView
operator|.
name|class
argument_list|)
decl_stmt|;
name|view
operator|.
name|setPermissions
argument_list|(
name|perms
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|IOUtils
operator|.
name|rm
argument_list|(
name|tmpBinDir
argument_list|)
expr_stmt|;
comment|// clean up what we just copied
block|}
comment|/**      * Copies the files from {@code tmpConfigDir} into {@code destConfigDir}.      * Any files existing in both the source and destination will be skipped.      */
DECL|method|installConfig
specifier|private
name|void
name|installConfig
parameter_list|(
name|PluginInfo
name|info
parameter_list|,
name|Path
name|tmpConfigDir
parameter_list|,
name|Path
name|destConfigDir
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|tmpConfigDir
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|IO_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"config in plugin "
operator|+
name|info
operator|.
name|getName
argument_list|()
operator|+
literal|" is not a directory"
argument_list|)
throw|;
block|}
comment|// create the plugin's config dir "if necessary"
name|Files
operator|.
name|createDirectories
argument_list|(
name|destConfigDir
argument_list|)
expr_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|stream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|tmpConfigDir
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|srcFile
range|:
name|stream
control|)
block|{
if|if
condition|(
name|Files
operator|.
name|isDirectory
argument_list|(
name|srcFile
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|UserError
argument_list|(
name|CliTool
operator|.
name|ExitStatus
operator|.
name|DATA_ERROR
operator|.
name|status
argument_list|()
argument_list|,
literal|"Directories not allowed in config dir for plugin "
operator|+
name|info
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
name|Path
name|destFile
init|=
name|destConfigDir
operator|.
name|resolve
argument_list|(
name|tmpConfigDir
operator|.
name|relativize
argument_list|(
name|srcFile
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|destFile
argument_list|)
operator|==
literal|false
condition|)
block|{
name|Files
operator|.
name|copy
argument_list|(
name|srcFile
argument_list|,
name|destFile
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|IOUtils
operator|.
name|rm
argument_list|(
name|tmpConfigDir
argument_list|)
expr_stmt|;
comment|// clean up what we just copied
block|}
block|}
end_class

end_unit

