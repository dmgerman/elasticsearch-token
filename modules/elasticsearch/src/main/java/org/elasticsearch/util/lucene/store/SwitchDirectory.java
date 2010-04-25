begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.lucene.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|store
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|store
operator|.
name|Directory
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
name|store
operator|.
name|IndexInput
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
name|store
operator|.
name|IndexOutput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|support
operator|.
name|ForceSyncDirectory
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
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * A Directory instance that switches files between  * two other Directory instances.  *  *<p>Files with the specified extensions are placed in the  * primary directory; others are placed in the secondary  * directory.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SwitchDirectory
specifier|public
class|class
name|SwitchDirectory
extends|extends
name|Directory
implements|implements
name|ForceSyncDirectory
block|{
DECL|field|secondaryDir
specifier|private
specifier|final
name|Directory
name|secondaryDir
decl_stmt|;
DECL|field|primaryDir
specifier|private
specifier|final
name|Directory
name|primaryDir
decl_stmt|;
DECL|field|primaryExtensions
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|primaryExtensions
decl_stmt|;
DECL|field|doClose
specifier|private
name|boolean
name|doClose
decl_stmt|;
DECL|method|SwitchDirectory
specifier|public
name|SwitchDirectory
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|primaryExtensions
parameter_list|,
name|Directory
name|primaryDir
parameter_list|,
name|Directory
name|secondaryDir
parameter_list|,
name|boolean
name|doClose
parameter_list|)
block|{
name|this
operator|.
name|primaryExtensions
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|primaryExtensions
argument_list|)
expr_stmt|;
name|this
operator|.
name|primaryDir
operator|=
name|primaryDir
expr_stmt|;
name|this
operator|.
name|secondaryDir
operator|=
name|secondaryDir
expr_stmt|;
name|this
operator|.
name|doClose
operator|=
name|doClose
expr_stmt|;
name|this
operator|.
name|lockFactory
operator|=
name|primaryDir
operator|.
name|getLockFactory
argument_list|()
expr_stmt|;
block|}
DECL|method|primaryExtensions
specifier|public
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|primaryExtensions
parameter_list|()
block|{
return|return
name|primaryExtensions
return|;
block|}
comment|/**      * Return the primary directory      */
DECL|method|primaryDir
specifier|public
name|Directory
name|primaryDir
parameter_list|()
block|{
return|return
name|primaryDir
return|;
block|}
comment|/**      * Return the secondary directory      */
DECL|method|secondaryDir
specifier|public
name|Directory
name|secondaryDir
parameter_list|()
block|{
return|return
name|secondaryDir
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|doClose
condition|)
block|{
try|try
block|{
name|secondaryDir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|primaryDir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|doClose
operator|=
literal|false
expr_stmt|;
block|}
block|}
DECL|method|listAll
annotation|@
name|Override
specifier|public
name|String
index|[]
name|listAll
parameter_list|()
throws|throws
name|IOException
block|{
name|String
index|[]
name|primaryFiles
init|=
name|primaryDir
operator|.
name|listAll
argument_list|()
decl_stmt|;
name|String
index|[]
name|secondaryFiles
init|=
name|secondaryDir
operator|.
name|listAll
argument_list|()
decl_stmt|;
name|String
index|[]
name|files
init|=
operator|new
name|String
index|[
name|primaryFiles
operator|.
name|length
operator|+
name|secondaryFiles
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|primaryFiles
argument_list|,
literal|0
argument_list|,
name|files
argument_list|,
literal|0
argument_list|,
name|primaryFiles
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|secondaryFiles
argument_list|,
literal|0
argument_list|,
name|files
argument_list|,
name|primaryFiles
operator|.
name|length
argument_list|,
name|secondaryFiles
operator|.
name|length
argument_list|)
expr_stmt|;
return|return
name|files
return|;
block|}
comment|/**      * Utility method to return a file's extension.      */
DECL|method|getExtension
specifier|public
specifier|static
name|String
name|getExtension
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|int
name|i
init|=
name|name
operator|.
name|lastIndexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|""
return|;
block|}
return|return
name|name
operator|.
name|substring
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|name
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getDirectory
specifier|private
name|Directory
name|getDirectory
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|String
name|ext
init|=
name|getExtension
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|primaryExtensions
operator|.
name|contains
argument_list|(
name|ext
argument_list|)
condition|)
block|{
return|return
name|primaryDir
return|;
block|}
else|else
block|{
return|return
name|secondaryDir
return|;
block|}
block|}
DECL|method|fileExists
annotation|@
name|Override
specifier|public
name|boolean
name|fileExists
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|fileExists
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|fileModified
annotation|@
name|Override
specifier|public
name|long
name|fileModified
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|fileModified
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|touchFile
annotation|@
name|Override
specifier|public
name|void
name|touchFile
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|touchFile
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteFile
annotation|@
name|Override
specifier|public
name|void
name|deleteFile
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|deleteFile
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|fileLength
annotation|@
name|Override
specifier|public
name|long
name|fileLength
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|fileLength
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|createOutput
annotation|@
name|Override
specifier|public
name|IndexOutput
name|createOutput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|createOutput
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|sync
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|sync
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|forceSync
annotation|@
name|Override
specifier|public
name|void
name|forceSync
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|Directory
name|dir
init|=
name|getDirectory
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|dir
operator|instanceof
name|ForceSyncDirectory
condition|)
block|{
operator|(
operator|(
name|ForceSyncDirectory
operator|)
name|dir
operator|)
operator|.
name|forceSync
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|dir
operator|.
name|sync
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|openInput
annotation|@
name|Override
specifier|public
name|IndexInput
name|openInput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getDirectory
argument_list|(
name|name
argument_list|)
operator|.
name|openInput
argument_list|(
name|name
argument_list|)
return|;
block|}
block|}
end_class

end_unit

