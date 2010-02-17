begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|fs
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|Nullable
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
name|IOException
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FsStores
specifier|public
class|class
name|FsStores
block|{
DECL|field|DEFAULT_INDICES_LOCATION
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_INDICES_LOCATION
init|=
literal|"indices"
decl_stmt|;
DECL|field|MAIN_INDEX_SUFFIX
specifier|public
specifier|static
specifier|final
name|String
name|MAIN_INDEX_SUFFIX
init|=
literal|"index"
decl_stmt|;
DECL|method|createStoreFilePath
specifier|public
specifier|static
specifier|synchronized
name|File
name|createStoreFilePath
parameter_list|(
name|File
name|basePath
parameter_list|,
name|String
name|localNodeId
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|String
name|suffix
parameter_list|)
throws|throws
name|IOException
block|{
comment|// TODO we need to clean the nodeId from invalid folder characters
name|File
name|f
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|basePath
argument_list|,
name|DEFAULT_INDICES_LOCATION
argument_list|)
argument_list|,
name|localNodeId
argument_list|)
decl_stmt|;
name|f
operator|=
operator|new
name|File
argument_list|(
name|f
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|f
operator|=
operator|new
name|File
argument_list|(
name|f
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|suffix
operator|!=
literal|null
condition|)
block|{
name|f
operator|=
operator|new
name|File
argument_list|(
name|f
argument_list|,
name|suffix
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
operator|&&
name|f
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
return|return
name|f
return|;
block|}
name|boolean
name|result
init|=
literal|false
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|result
operator|=
name|f
operator|.
name|mkdirs
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
condition|)
block|{
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|result
condition|)
block|{
if|if
condition|(
name|f
operator|.
name|exists
argument_list|()
operator|&&
name|f
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
return|return
name|f
return|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to create directories for ["
operator|+
name|f
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|f
return|;
block|}
block|}
end_class

end_unit

