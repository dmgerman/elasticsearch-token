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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|ByteStreams
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|StringHelper
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
name|BufferedInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
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
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStreamWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Writer
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
name|NoSuchFileException
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
name|HashSet
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

begin_comment
comment|/**   * Initializes securitymanager with necessary permissions.  *<p>  * We use a template file (the one we test with), and add additional   * permissions based on the environment (data paths, etc)  */
end_comment

begin_class
DECL|class|Security
class|class
name|Security
block|{
comment|/** template policy file, the one used in tests */
DECL|field|POLICY_RESOURCE
specifier|static
specifier|final
name|String
name|POLICY_RESOURCE
init|=
literal|"security.policy"
decl_stmt|;
comment|/**       * Initializes securitymanager for the environment      * Can only happen once!      */
DECL|method|configure
specifier|static
name|void
name|configure
parameter_list|(
name|Environment
name|environment
parameter_list|)
throws|throws
name|IOException
block|{
comment|// init lucene random seed. it will use /dev/urandom where available.
name|StringHelper
operator|.
name|randomId
argument_list|()
expr_stmt|;
name|InputStream
name|config
init|=
name|Security
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
name|POLICY_RESOURCE
argument_list|)
decl_stmt|;
if|if
condition|(
name|config
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
name|POLICY_RESOURCE
argument_list|)
throw|;
block|}
name|Path
name|newConfig
init|=
name|processTemplate
argument_list|(
name|config
argument_list|,
name|environment
argument_list|)
decl_stmt|;
name|System
operator|.
name|setProperty
argument_list|(
literal|"java.security.policy"
argument_list|,
name|newConfig
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|System
operator|.
name|setSecurityManager
argument_list|(
operator|new
name|SecurityManager
argument_list|()
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|deleteFilesIgnoringExceptions
argument_list|(
name|newConfig
argument_list|)
expr_stmt|;
comment|// TODO: maybe log something if it fails?
block|}
comment|// package-private for testing
DECL|method|processTemplate
specifier|static
name|Path
name|processTemplate
parameter_list|(
name|InputStream
name|template
parameter_list|,
name|Environment
name|environment
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
name|processed
init|=
name|Files
operator|.
name|createTempFile
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
try|try
init|(
name|OutputStream
name|output
init|=
operator|new
name|BufferedOutputStream
argument_list|(
name|Files
operator|.
name|newOutputStream
argument_list|(
name|processed
argument_list|)
argument_list|)
init|)
block|{
comment|// copy the template as-is.
try|try
init|(
name|InputStream
name|in
init|=
operator|new
name|BufferedInputStream
argument_list|(
name|template
argument_list|)
init|)
block|{
name|ByteStreams
operator|.
name|copy
argument_list|(
name|in
argument_list|,
name|output
argument_list|)
expr_stmt|;
block|}
comment|// add permissions for all configured paths.
name|Set
argument_list|<
name|Path
argument_list|>
name|paths
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|homeFile
argument_list|()
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|configFile
argument_list|()
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|logsFile
argument_list|()
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|pluginsFile
argument_list|()
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|workFile
argument_list|()
argument_list|)
expr_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|environment
operator|.
name|workWithClusterFile
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|environment
operator|.
name|dataFiles
argument_list|()
control|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Path
name|path
range|:
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
control|)
block|{
name|paths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|output
operator|.
name|write
argument_list|(
name|createPermissions
argument_list|(
name|paths
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|processed
return|;
block|}
comment|// package private for testing
DECL|method|createPermissions
specifier|static
name|byte
index|[]
name|createPermissions
parameter_list|(
name|Set
argument_list|<
name|Path
argument_list|>
name|paths
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteArrayOutputStream
name|stream
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
comment|// all policy files are UTF-8:
comment|//  https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html
try|try
init|(
name|Writer
name|writer
init|=
operator|new
name|OutputStreamWriter
argument_list|(
name|stream
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
init|)
block|{
name|writer
operator|.
name|write
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
literal|"grant {"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|paths
control|)
block|{
comment|// data paths actually may not exist yet.
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
argument_list|)
expr_stmt|;
comment|// add each path twice: once for itself, again for files underneath it
name|addPath
argument_list|(
name|writer
argument_list|,
name|encode
argument_list|(
name|path
argument_list|)
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
name|addRecursivePath
argument_list|(
name|writer
argument_list|,
name|encode
argument_list|(
name|path
argument_list|)
argument_list|,
literal|"read,readlink,write,delete"
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|write
argument_list|(
literal|"};"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|stream
operator|.
name|toByteArray
argument_list|()
return|;
block|}
DECL|method|addPath
specifier|static
name|void
name|addPath
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|String
name|path
parameter_list|,
name|String
name|permissions
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
literal|"permission java.io.FilePermission \""
operator|+
name|path
operator|+
literal|"\", \""
operator|+
name|permissions
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|addRecursivePath
specifier|static
name|void
name|addRecursivePath
parameter_list|(
name|Writer
name|writer
parameter_list|,
name|String
name|path
parameter_list|,
name|String
name|permissions
parameter_list|)
throws|throws
name|IOException
block|{
name|writer
operator|.
name|write
argument_list|(
literal|"permission java.io.FilePermission \""
operator|+
name|path
operator|+
literal|"${/}-\", \""
operator|+
name|permissions
operator|+
literal|"\";"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|write
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Any backslashes in paths must be escaped, because it is the escape character when parsing.
comment|// See "Note Regarding File Path Specifications on Windows Systems".
comment|// https://docs.oracle.com/javase/7/docs/technotes/guides/security/PolicyFiles.html
DECL|method|encode
specifier|static
name|String
name|encode
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|encode
argument_list|(
name|path
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
DECL|method|encode
specifier|static
name|String
name|encode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
return|return
name|path
operator|.
name|replace
argument_list|(
literal|"\\"
argument_list|,
literal|"\\\\"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

