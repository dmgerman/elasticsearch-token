begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.env
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|env
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterName
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
name|settings
operator|.
name|Setting
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
name|Setting
operator|.
name|Property
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
name|UncheckedIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|MalformedURLException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URISyntaxException
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
name|nio
operator|.
name|file
operator|.
name|FileStore
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
name|Collections
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
name|List
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
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|Strings
operator|.
name|cleanPath
import|;
end_import

begin_comment
comment|/**  * The environment of where things exists.  */
end_comment

begin_class
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"configures paths for the system"
argument_list|)
comment|// TODO: move PathUtils to be package-private here instead of
comment|// public+forbidden api!
DECL|class|Environment
specifier|public
class|class
name|Environment
block|{
DECL|field|PATH_HOME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PATH_HOME_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"path.home"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_PATH_CONF_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|DEFAULT_PATH_CONF_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"default.path.conf"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_CONF_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PATH_CONF_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"path.conf"
argument_list|,
name|DEFAULT_PATH_CONF_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_SCRIPTS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PATH_SCRIPTS_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"path.scripts"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Deprecated
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_PATH_DATA_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|DEFAULT_PATH_DATA_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"default.path.data"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_DATA_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|PATH_DATA_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"path.data"
argument_list|,
name|DEFAULT_PATH_DATA_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_PATH_LOGS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|DEFAULT_PATH_LOGS_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"default.path.logs"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_LOGS_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PATH_LOGS_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"path.logs"
argument_list|,
name|DEFAULT_PATH_LOGS_SETTING
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_REPO_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|List
argument_list|<
name|String
argument_list|>
argument_list|>
name|PATH_REPO_SETTING
init|=
name|Setting
operator|.
name|listSetting
argument_list|(
literal|"path.repo"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PATH_SHARED_DATA_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PATH_SHARED_DATA_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"path.shared_data"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PIDFILE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PIDFILE_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"pidfile"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|configExtension
specifier|private
specifier|final
name|String
name|configExtension
decl_stmt|;
DECL|field|dataFiles
specifier|private
specifier|final
name|Path
index|[]
name|dataFiles
decl_stmt|;
DECL|field|dataWithClusterFiles
specifier|private
specifier|final
name|Path
index|[]
name|dataWithClusterFiles
decl_stmt|;
DECL|field|repoFiles
specifier|private
specifier|final
name|Path
index|[]
name|repoFiles
decl_stmt|;
DECL|field|configFile
specifier|private
specifier|final
name|Path
name|configFile
decl_stmt|;
DECL|field|scriptsFile
specifier|private
specifier|final
name|Path
name|scriptsFile
decl_stmt|;
DECL|field|pluginsFile
specifier|private
specifier|final
name|Path
name|pluginsFile
decl_stmt|;
DECL|field|modulesFile
specifier|private
specifier|final
name|Path
name|modulesFile
decl_stmt|;
DECL|field|sharedDataFile
specifier|private
specifier|final
name|Path
name|sharedDataFile
decl_stmt|;
comment|/** location of bin/, used by plugin manager */
DECL|field|binFile
specifier|private
specifier|final
name|Path
name|binFile
decl_stmt|;
comment|/** location of lib/, */
DECL|field|libFile
specifier|private
specifier|final
name|Path
name|libFile
decl_stmt|;
DECL|field|logsFile
specifier|private
specifier|final
name|Path
name|logsFile
decl_stmt|;
comment|/** Path to the PID file (can be null if no PID file is configured) **/
DECL|field|pidFile
specifier|private
specifier|final
name|Path
name|pidFile
decl_stmt|;
comment|/** Path to the temporary file directory used by the JDK */
DECL|field|tmpFile
specifier|private
specifier|final
name|Path
name|tmpFile
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
literal|"java.io.tmpdir"
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|Environment
specifier|public
name|Environment
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
argument_list|(
name|settings
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|// Note: Do not use this ctor, it is for correct deprecation logging in 5.5 and will be removed
DECL|method|Environment
specifier|public
name|Environment
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|String
name|configExtension
parameter_list|)
block|{
name|this
operator|.
name|configExtension
operator|=
name|configExtension
expr_stmt|;
specifier|final
name|Path
name|homeFile
decl_stmt|;
if|if
condition|(
name|PATH_HOME_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|homeFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PATH_HOME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|" is not configured"
argument_list|)
throw|;
block|}
comment|// this is trappy, Setting#get(Settings) will get a fallback setting yet return false for Settings#exists(Settings)
if|if
condition|(
name|PATH_CONF_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|DEFAULT_PATH_CONF_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|configFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PATH_CONF_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|configFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"config"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|PATH_SCRIPTS_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|scriptsFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PATH_SCRIPTS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scriptsFile
operator|=
name|configFile
operator|.
name|resolve
argument_list|(
literal|"scripts"
argument_list|)
expr_stmt|;
block|}
name|pluginsFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"plugins"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dataPaths
init|=
name|PATH_DATA_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
specifier|final
name|ClusterName
name|clusterName
init|=
name|ClusterName
operator|.
name|CLUSTER_NAME_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|dataPaths
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|dataFiles
operator|=
operator|new
name|Path
index|[
name|dataPaths
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|dataWithClusterFiles
operator|=
operator|new
name|Path
index|[
name|dataPaths
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|dataPaths
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|dataFiles
index|[
name|i
index|]
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|dataPaths
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|dataWithClusterFiles
index|[
name|i
index|]
operator|=
name|dataFiles
index|[
name|i
index|]
operator|.
name|resolve
argument_list|(
name|clusterName
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|dataFiles
operator|=
operator|new
name|Path
index|[]
block|{
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"data"
argument_list|)
block|}
expr_stmt|;
name|dataWithClusterFiles
operator|=
operator|new
name|Path
index|[]
block|{
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"data"
argument_list|)
operator|.
name|resolve
argument_list|(
name|clusterName
operator|.
name|value
argument_list|()
argument_list|)
block|}
expr_stmt|;
block|}
if|if
condition|(
name|PATH_SHARED_DATA_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|sharedDataFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PATH_SHARED_DATA_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sharedDataFile
operator|=
literal|null
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|repoPaths
init|=
name|PATH_REPO_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|repoPaths
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|repoFiles
operator|=
operator|new
name|Path
index|[
name|repoPaths
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|repoPaths
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|repoFiles
index|[
name|i
index|]
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|repoPaths
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|repoFiles
operator|=
operator|new
name|Path
index|[
literal|0
index|]
expr_stmt|;
block|}
comment|// this is trappy, Setting#get(Settings) will get a fallback setting yet return false for Settings#exists(Settings)
if|if
condition|(
name|PATH_LOGS_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|||
name|DEFAULT_PATH_LOGS_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|logsFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PATH_LOGS_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logsFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"logs"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|PIDFILE_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|pidFile
operator|=
name|PathUtils
operator|.
name|get
argument_list|(
name|cleanPath
argument_list|(
name|PIDFILE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pidFile
operator|=
literal|null
expr_stmt|;
block|}
name|binFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"bin"
argument_list|)
expr_stmt|;
name|libFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"lib"
argument_list|)
expr_stmt|;
name|modulesFile
operator|=
name|homeFile
operator|.
name|resolve
argument_list|(
literal|"modules"
argument_list|)
expr_stmt|;
name|Settings
operator|.
name|Builder
name|finalSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|finalSettings
operator|.
name|put
argument_list|(
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|homeFile
argument_list|)
expr_stmt|;
if|if
condition|(
name|PATH_DATA_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|finalSettings
operator|.
name|putArray
argument_list|(
name|PATH_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|dataPaths
argument_list|)
expr_stmt|;
block|}
name|finalSettings
operator|.
name|put
argument_list|(
name|PATH_LOGS_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|logsFile
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|finalSettings
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|/**      * The settings used to build this environment.      */
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|this
operator|.
name|settings
return|;
block|}
comment|/**      * The data location.      */
DECL|method|dataFiles
specifier|public
name|Path
index|[]
name|dataFiles
parameter_list|()
block|{
return|return
name|dataFiles
return|;
block|}
comment|/**      * The shared data location      */
DECL|method|sharedDataFile
specifier|public
name|Path
name|sharedDataFile
parameter_list|()
block|{
return|return
name|sharedDataFile
return|;
block|}
comment|/**      * The data location with the cluster name as a sub directory.      *      * @deprecated Used to upgrade old data paths to new ones that do not include the cluster name, should not be used to write files to and      * will be removed in ES 6.0      */
annotation|@
name|Deprecated
DECL|method|dataWithClusterFiles
specifier|public
name|Path
index|[]
name|dataWithClusterFiles
parameter_list|()
block|{
return|return
name|dataWithClusterFiles
return|;
block|}
comment|/**      * The shared filesystem repo locations.      */
DECL|method|repoFiles
specifier|public
name|Path
index|[]
name|repoFiles
parameter_list|()
block|{
return|return
name|repoFiles
return|;
block|}
comment|/**      * Resolves the specified location against the list of configured repository roots      *      * If the specified location doesn't match any of the roots, returns null.      */
DECL|method|resolveRepoFile
specifier|public
name|Path
name|resolveRepoFile
parameter_list|(
name|String
name|location
parameter_list|)
block|{
return|return
name|PathUtils
operator|.
name|get
argument_list|(
name|repoFiles
argument_list|,
name|location
argument_list|)
return|;
block|}
comment|/**      * Checks if the specified URL is pointing to the local file system and if it does, resolves the specified url      * against the list of configured repository roots      *      * If the specified url doesn't match any of the roots, returns null.      */
DECL|method|resolveRepoURL
specifier|public
name|URL
name|resolveRepoURL
parameter_list|(
name|URL
name|url
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
literal|"file"
operator|.
name|equalsIgnoreCase
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|url
operator|.
name|getHost
argument_list|()
operator|==
literal|null
operator|||
literal|""
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getHost
argument_list|()
argument_list|)
condition|)
block|{
comment|// only local file urls are supported
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|repoFiles
argument_list|,
name|url
operator|.
name|toURI
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|path
operator|==
literal|null
condition|)
block|{
comment|// Couldn't resolve against known repo locations
return|return
literal|null
return|;
block|}
comment|// Normalize URL
return|return
name|path
operator|.
name|toUri
argument_list|()
operator|.
name|toURL
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
literal|"jar"
operator|.
name|equals
argument_list|(
name|url
operator|.
name|getProtocol
argument_list|()
argument_list|)
condition|)
block|{
name|String
name|file
init|=
name|url
operator|.
name|getFile
argument_list|()
decl_stmt|;
name|int
name|pos
init|=
name|file
operator|.
name|indexOf
argument_list|(
literal|"!/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|pos
operator|<
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|String
name|jarTail
init|=
name|file
operator|.
name|substring
argument_list|(
name|pos
argument_list|)
decl_stmt|;
name|String
name|filePath
init|=
name|file
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|pos
argument_list|)
decl_stmt|;
name|URL
name|internalUrl
init|=
operator|new
name|URL
argument_list|(
name|filePath
argument_list|)
decl_stmt|;
name|URL
name|normalizedUrl
init|=
name|resolveRepoURL
argument_list|(
name|internalUrl
argument_list|)
decl_stmt|;
if|if
condition|(
name|normalizedUrl
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|URL
argument_list|(
literal|"jar"
argument_list|,
literal|""
argument_list|,
name|normalizedUrl
operator|.
name|toExternalForm
argument_list|()
operator|+
name|jarTail
argument_list|)
return|;
block|}
else|else
block|{
comment|// It's not file or jar url and it didn't match the white list - reject
return|return
literal|null
return|;
block|}
block|}
catch|catch
parameter_list|(
name|MalformedURLException
name|ex
parameter_list|)
block|{
comment|// cannot make sense of this file url
return|return
literal|null
return|;
block|}
catch|catch
parameter_list|(
name|URISyntaxException
name|ex
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
comment|/** Return then extension of the config file that was loaded, or*/
DECL|method|configExtension
specifier|public
name|String
name|configExtension
parameter_list|()
block|{
return|return
name|configExtension
return|;
block|}
comment|// TODO: rename all these "file" methods to "dir"
comment|/**      * The config directory.      */
DECL|method|configFile
specifier|public
name|Path
name|configFile
parameter_list|()
block|{
return|return
name|configFile
return|;
block|}
comment|/**      * Location of on-disk scripts      */
DECL|method|scriptsFile
specifier|public
name|Path
name|scriptsFile
parameter_list|()
block|{
return|return
name|scriptsFile
return|;
block|}
DECL|method|pluginsFile
specifier|public
name|Path
name|pluginsFile
parameter_list|()
block|{
return|return
name|pluginsFile
return|;
block|}
DECL|method|binFile
specifier|public
name|Path
name|binFile
parameter_list|()
block|{
return|return
name|binFile
return|;
block|}
DECL|method|libFile
specifier|public
name|Path
name|libFile
parameter_list|()
block|{
return|return
name|libFile
return|;
block|}
DECL|method|modulesFile
specifier|public
name|Path
name|modulesFile
parameter_list|()
block|{
return|return
name|modulesFile
return|;
block|}
DECL|method|logsFile
specifier|public
name|Path
name|logsFile
parameter_list|()
block|{
return|return
name|logsFile
return|;
block|}
comment|/**      * The PID file location (can be null if no PID file is configured)      */
DECL|method|pidFile
specifier|public
name|Path
name|pidFile
parameter_list|()
block|{
return|return
name|pidFile
return|;
block|}
comment|/** Path to the default temp directory used by the JDK */
DECL|method|tmpFile
specifier|public
name|Path
name|tmpFile
parameter_list|()
block|{
return|return
name|tmpFile
return|;
block|}
DECL|method|getFileStore
specifier|public
specifier|static
name|FileStore
name|getFileStore
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|ESFileStore
argument_list|(
name|Files
operator|.
name|getFileStore
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * asserts that the two environments are equivalent for all things the environment cares about (i.e., all but the setting      * object which may contain different setting)      */
DECL|method|assertEquivalent
specifier|public
specifier|static
name|void
name|assertEquivalent
parameter_list|(
name|Environment
name|actual
parameter_list|,
name|Environment
name|expected
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|actual
operator|.
name|dataWithClusterFiles
argument_list|()
argument_list|,
name|expected
operator|.
name|dataWithClusterFiles
argument_list|()
argument_list|,
literal|"dataWithClusterFiles"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|repoFiles
argument_list|()
argument_list|,
name|expected
operator|.
name|repoFiles
argument_list|()
argument_list|,
literal|"repoFiles"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|configFile
argument_list|()
argument_list|,
name|expected
operator|.
name|configFile
argument_list|()
argument_list|,
literal|"configFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|scriptsFile
argument_list|()
argument_list|,
name|expected
operator|.
name|scriptsFile
argument_list|()
argument_list|,
literal|"scriptsFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|pluginsFile
argument_list|()
argument_list|,
name|expected
operator|.
name|pluginsFile
argument_list|()
argument_list|,
literal|"pluginsFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|binFile
argument_list|()
argument_list|,
name|expected
operator|.
name|binFile
argument_list|()
argument_list|,
literal|"binFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|libFile
argument_list|()
argument_list|,
name|expected
operator|.
name|libFile
argument_list|()
argument_list|,
literal|"libFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|modulesFile
argument_list|()
argument_list|,
name|expected
operator|.
name|modulesFile
argument_list|()
argument_list|,
literal|"modulesFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|logsFile
argument_list|()
argument_list|,
name|expected
operator|.
name|logsFile
argument_list|()
argument_list|,
literal|"logsFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|pidFile
argument_list|()
argument_list|,
name|expected
operator|.
name|pidFile
argument_list|()
argument_list|,
literal|"pidFile"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|actual
operator|.
name|tmpFile
argument_list|()
argument_list|,
name|expected
operator|.
name|tmpFile
argument_list|()
argument_list|,
literal|"tmpFile"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertEquals
specifier|private
specifier|static
name|void
name|assertEquals
parameter_list|(
name|Object
name|actual
parameter_list|,
name|Object
name|expected
parameter_list|,
name|String
name|name
parameter_list|)
block|{
assert|assert
name|Objects
operator|.
name|deepEquals
argument_list|(
name|actual
argument_list|,
name|expected
argument_list|)
operator|:
literal|"actual "
operator|+
name|name
operator|+
literal|" ["
operator|+
name|actual
operator|+
literal|"] is different than [ "
operator|+
name|expected
operator|+
literal|"]"
assert|;
block|}
block|}
end_class

end_unit

