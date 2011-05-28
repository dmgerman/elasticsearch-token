begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
name|collect
operator|.
name|Tuple
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
name|http
operator|.
name|client
operator|.
name|HttpDownloadHelper
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
name|common
operator|.
name|io
operator|.
name|Streams
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
name|settings
operator|.
name|Settings
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
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
operator|.
name|InternalSettingsPerparer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileOutputStream
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
name|ZipFile
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|PluginManager
specifier|public
class|class
name|PluginManager
block|{
DECL|field|environment
specifier|private
specifier|final
name|Environment
name|environment
decl_stmt|;
DECL|field|url
specifier|private
name|String
name|url
decl_stmt|;
DECL|method|PluginManager
specifier|public
name|PluginManager
parameter_list|(
name|Environment
name|environment
parameter_list|,
name|String
name|url
parameter_list|)
block|{
name|this
operator|.
name|environment
operator|=
name|environment
expr_stmt|;
name|this
operator|.
name|url
operator|=
name|url
expr_stmt|;
block|}
DECL|method|downloadAndExtract
specifier|public
name|void
name|downloadAndExtract
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|HttpDownloadHelper
name|downloadHelper
init|=
operator|new
name|HttpDownloadHelper
argument_list|()
decl_stmt|;
name|File
name|pluginFile
init|=
operator|new
name|File
argument_list|(
name|url
operator|+
literal|"/"
operator|+
name|name
operator|+
literal|"/elasticsearch-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|Version
operator|.
name|number
argument_list|()
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
name|boolean
name|downloaded
init|=
literal|false
decl_stmt|;
name|String
name|filterZipName
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|pluginFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|pluginFile
operator|=
operator|new
name|File
argument_list|(
name|url
operator|+
literal|"/elasticsearch-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|Version
operator|.
name|number
argument_list|()
operator|+
literal|".zip"
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|pluginFile
operator|.
name|exists
argument_list|()
condition|)
block|{
name|pluginFile
operator|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
operator|+
literal|".zip"
argument_list|)
expr_stmt|;
if|if
condition|(
name|url
operator|!=
literal|null
condition|)
block|{
name|URL
name|pluginUrl
init|=
operator|new
name|URL
argument_list|(
name|url
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
else|else
block|{
name|url
operator|=
literal|"http://elasticsearch.googlecode.com/svn/plugins"
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|downloaded
condition|)
block|{
if|if
condition|(
name|name
operator|.
name|indexOf
argument_list|(
literal|'/'
argument_list|)
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// github repo
name|String
index|[]
name|elements
init|=
name|name
operator|.
name|split
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
name|String
name|userName
init|=
name|elements
index|[
literal|0
index|]
decl_stmt|;
name|String
name|repoName
init|=
name|elements
index|[
literal|1
index|]
decl_stmt|;
name|String
name|version
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|elements
operator|.
name|length
operator|>
literal|2
condition|)
block|{
name|version
operator|=
name|elements
index|[
literal|2
index|]
expr_stmt|;
block|}
name|filterZipName
operator|=
name|userName
operator|+
literal|"-"
operator|+
name|repoName
expr_stmt|;
comment|// the installation file should not include the userName, just the repoName
name|name
operator|=
name|repoName
expr_stmt|;
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"elasticsearch-"
argument_list|)
condition|)
block|{
comment|// remove elasticsearch- prefix
name|name
operator|=
name|name
operator|.
name|substring
argument_list|(
literal|"elasticsearch-"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|.
name|startsWith
argument_list|(
literal|"es-"
argument_list|)
condition|)
block|{
comment|// remove es- prefix
name|name
operator|=
name|name
operator|.
name|substring
argument_list|(
literal|"es-"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|pluginFile
operator|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
operator|+
literal|".zip"
argument_list|)
expr_stmt|;
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
comment|// try with ES version from downloads
name|URL
name|pluginUrl
init|=
operator|new
name|URL
argument_list|(
literal|"http://github.com/downloads/"
operator|+
name|userName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"-"
operator|+
name|Version
operator|.
name|number
argument_list|()
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// try a tag with ES version
name|pluginUrl
operator|=
operator|new
name|URL
argument_list|(
literal|"http://github.com/"
operator|+
name|userName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"/zipball/v"
operator|+
name|Version
operator|.
name|number
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// download master
name|pluginUrl
operator|=
operator|new
name|URL
argument_list|(
literal|"http://github.com/"
operator|+
name|userName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"/zipball/master"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e2
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
else|else
block|{
comment|// download explicit version
name|URL
name|pluginUrl
init|=
operator|new
name|URL
argument_list|(
literal|"http://github.com/downloads/"
operator|+
name|userName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"-"
operator|+
name|version
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// try a tag with ES version
name|pluginUrl
operator|=
operator|new
name|URL
argument_list|(
literal|"http://github.com/"
operator|+
name|userName
operator|+
literal|"/"
operator|+
name|repoName
operator|+
literal|"/zipball/v"
operator|+
name|version
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
else|else
block|{
name|URL
name|pluginUrl
init|=
operator|new
name|URL
argument_list|(
name|url
operator|+
literal|"/"
operator|+
name|name
operator|+
literal|"/elasticsearch-"
operator|+
name|name
operator|+
literal|"-"
operator|+
name|Version
operator|.
name|number
argument_list|()
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Trying "
operator|+
name|pluginUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|downloadHelper
operator|.
name|download
argument_list|(
name|pluginUrl
argument_list|,
name|pluginFile
argument_list|,
operator|new
name|HttpDownloadHelper
operator|.
name|VerboseProgress
argument_list|(
name|System
operator|.
name|out
argument_list|)
argument_list|)
expr_stmt|;
name|downloaded
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Using plugin from local fs: "
operator|+
name|pluginFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Using plugin from local fs: "
operator|+
name|pluginFile
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|downloaded
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"failed to download"
argument_list|)
throw|;
block|}
comment|// extract the plugin
name|File
name|extractLocation
init|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|ZipFile
name|zipFile
init|=
literal|null
decl_stmt|;
try|try
block|{
name|zipFile
operator|=
operator|new
name|ZipFile
argument_list|(
name|pluginFile
argument_list|)
expr_stmt|;
name|Enumeration
argument_list|<
name|?
extends|extends
name|ZipEntry
argument_list|>
name|zipEntries
init|=
name|zipFile
operator|.
name|entries
argument_list|()
decl_stmt|;
while|while
condition|(
name|zipEntries
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|ZipEntry
name|zipEntry
init|=
name|zipEntries
operator|.
name|nextElement
argument_list|()
decl_stmt|;
if|if
condition|(
name|zipEntry
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|String
name|zipName
init|=
name|zipEntry
operator|.
name|getName
argument_list|()
operator|.
name|replace
argument_list|(
literal|'\\'
argument_list|,
literal|'/'
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterZipName
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|zipName
operator|.
name|startsWith
argument_list|(
name|filterZipName
argument_list|)
condition|)
block|{
name|zipName
operator|=
name|zipName
operator|.
name|substring
argument_list|(
name|zipName
operator|.
name|indexOf
argument_list|(
literal|'/'
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|File
name|target
init|=
operator|new
name|File
argument_list|(
name|extractLocation
argument_list|,
name|zipName
argument_list|)
decl_stmt|;
name|target
operator|.
name|getParentFile
argument_list|()
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|Streams
operator|.
name|copy
argument_list|(
name|zipFile
operator|.
name|getInputStream
argument_list|(
name|zipEntry
argument_list|)
argument_list|,
operator|new
name|FileOutputStream
argument_list|(
name|target
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"failed to extract plugin ["
operator|+
name|pluginFile
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|zipFile
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|zipFile
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|pluginFile
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
comment|// try and identify the plugin type, see if it has no .class or .jar files in it
comment|// so its probably a _site, and it it does not have a _site in it, move everything to _site
if|if
condition|(
operator|!
operator|new
name|File
argument_list|(
name|extractLocation
argument_list|,
literal|"_site"
argument_list|)
operator|.
name|exists
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|FileSystemUtils
operator|.
name|hasExtensions
argument_list|(
name|extractLocation
argument_list|,
literal|".class"
argument_list|,
literal|".jar"
argument_list|)
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Identified as a _site plugin, moving to _site structure ..."
argument_list|)
expr_stmt|;
name|File
name|site
init|=
operator|new
name|File
argument_list|(
name|extractLocation
argument_list|,
literal|"_site"
argument_list|)
decl_stmt|;
name|File
name|tmpLocation
init|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
operator|+
literal|".tmp"
argument_list|)
decl_stmt|;
name|extractLocation
operator|.
name|renameTo
argument_list|(
name|tmpLocation
argument_list|)
expr_stmt|;
name|extractLocation
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
name|tmpLocation
operator|.
name|renameTo
argument_list|(
name|site
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|removePlugin
specifier|public
name|void
name|removePlugin
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|File
name|pluginToDelete
init|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|pluginToDelete
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|pluginToDelete
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|pluginToDelete
operator|=
operator|new
name|File
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|name
operator|+
literal|".zip"
argument_list|)
expr_stmt|;
if|if
condition|(
name|pluginToDelete
operator|.
name|exists
argument_list|()
condition|)
block|{
name|pluginToDelete
operator|.
name|delete
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|initialSettings
init|=
name|InternalSettingsPerparer
operator|.
name|prepareSettings
argument_list|(
name|EMPTY_SETTINGS
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|initialSettings
operator|.
name|v2
argument_list|()
operator|.
name|pluginsFile
argument_list|()
operator|.
name|exists
argument_list|()
condition|)
block|{
name|initialSettings
operator|.
name|v2
argument_list|()
operator|.
name|pluginsFile
argument_list|()
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
block|}
name|String
name|url
init|=
literal|null
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
literal|"url"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
operator|||
literal|"-url"
operator|.
name|equals
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
condition|)
block|{
name|url
operator|=
name|args
index|[
name|i
operator|+
literal|1
index|]
expr_stmt|;
break|break;
block|}
block|}
name|PluginManager
name|pluginManager
init|=
operator|new
name|PluginManager
argument_list|(
name|initialSettings
operator|.
name|v2
argument_list|()
argument_list|,
name|url
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Usage:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    -url     [plugins location]  : Set URL to download plugins from"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    -install [plugin name]       : Downloads and installs listed plugins"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"    -remove  [plugin name]       : Removes listed plugins"
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|c
init|=
literal|0
init|;
name|c
operator|<
name|args
operator|.
name|length
condition|;
name|c
operator|++
control|)
block|{
name|String
name|command
init|=
name|args
index|[
name|c
index|]
decl_stmt|;
if|if
condition|(
name|command
operator|.
name|equals
argument_list|(
literal|"install"
argument_list|)
operator|||
name|command
operator|.
name|equals
argument_list|(
literal|"-install"
argument_list|)
condition|)
block|{
name|String
name|pluginName
init|=
name|args
index|[
operator|++
name|c
index|]
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"-> Installing "
operator|+
name|pluginName
operator|+
literal|"..."
argument_list|)
expr_stmt|;
try|try
block|{
name|pluginManager
operator|.
name|downloadAndExtract
argument_list|(
name|pluginName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to install "
operator|+
name|pluginName
operator|+
literal|", reason: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|command
operator|.
name|equals
argument_list|(
literal|"remove"
argument_list|)
operator|||
name|command
operator|.
name|equals
argument_list|(
literal|"-remove"
argument_list|)
condition|)
block|{
name|String
name|pluginName
init|=
name|args
index|[
operator|++
name|c
index|]
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"-> Removing "
operator|+
name|pluginName
operator|+
literal|" "
argument_list|)
expr_stmt|;
try|try
block|{
name|pluginManager
operator|.
name|removePlugin
argument_list|(
name|pluginName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Failed to remove "
operator|+
name|pluginName
operator|+
literal|", reason: "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// not install or remove, continue
name|c
operator|++
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

