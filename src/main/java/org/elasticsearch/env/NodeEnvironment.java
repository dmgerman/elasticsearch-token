begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Ints
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
name|Lock
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
name|NativeFSLockFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|common
operator|.
name|unit
operator|.
name|ByteSizeValue
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
name|Index
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
name|shard
operator|.
name|ShardId
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
name|Set
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NodeEnvironment
specifier|public
class|class
name|NodeEnvironment
extends|extends
name|AbstractComponent
block|{
DECL|field|nodeFiles
specifier|private
specifier|final
name|File
index|[]
name|nodeFiles
decl_stmt|;
DECL|field|nodeIndicesLocations
specifier|private
specifier|final
name|File
index|[]
name|nodeIndicesLocations
decl_stmt|;
DECL|field|locks
specifier|private
specifier|final
name|Lock
index|[]
name|locks
decl_stmt|;
DECL|field|localNodeId
specifier|private
specifier|final
name|int
name|localNodeId
decl_stmt|;
annotation|@
name|Inject
DECL|method|NodeEnvironment
specifier|public
name|NodeEnvironment
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Environment
name|environment
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|DiscoveryNode
operator|.
name|nodeRequiresLocalStorage
argument_list|(
name|settings
argument_list|)
condition|)
block|{
name|nodeFiles
operator|=
literal|null
expr_stmt|;
name|nodeIndicesLocations
operator|=
literal|null
expr_stmt|;
name|locks
operator|=
literal|null
expr_stmt|;
name|localNodeId
operator|=
operator|-
literal|1
expr_stmt|;
return|return;
block|}
name|File
index|[]
name|nodesFiles
init|=
operator|new
name|File
index|[
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
name|Lock
index|[]
name|locks
init|=
operator|new
name|Lock
index|[
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
operator|.
name|length
index|]
decl_stmt|;
name|int
name|localNodeId
init|=
operator|-
literal|1
decl_stmt|;
name|IOException
name|lastException
init|=
literal|null
decl_stmt|;
name|int
name|maxLocalStorageNodes
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"node.max_local_storage_nodes"
argument_list|,
literal|50
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|possibleLockId
init|=
literal|0
init|;
name|possibleLockId
operator|<
name|maxLocalStorageNodes
condition|;
name|possibleLockId
operator|++
control|)
block|{
for|for
control|(
name|int
name|dirIndex
init|=
literal|0
init|;
name|dirIndex
operator|<
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
operator|.
name|length
condition|;
name|dirIndex
operator|++
control|)
block|{
name|File
name|dir
init|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
index|[
name|dirIndex
index|]
argument_list|,
literal|"nodes"
argument_list|)
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|possibleLockId
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|dir
operator|.
name|exists
argument_list|()
condition|)
block|{
name|FileSystemUtils
operator|.
name|mkdirs
argument_list|(
name|dir
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"obtaining node lock on {} ..."
argument_list|,
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|NativeFSLockFactory
name|lockFactory
init|=
operator|new
name|NativeFSLockFactory
argument_list|(
name|dir
argument_list|)
decl_stmt|;
name|Lock
name|tmpLock
init|=
name|lockFactory
operator|.
name|makeLock
argument_list|(
literal|"node.lock"
argument_list|)
decl_stmt|;
name|boolean
name|obtained
init|=
name|tmpLock
operator|.
name|obtain
argument_list|()
decl_stmt|;
if|if
condition|(
name|obtained
condition|)
block|{
name|locks
index|[
name|dirIndex
index|]
operator|=
name|tmpLock
expr_stmt|;
name|nodesFiles
index|[
name|dirIndex
index|]
operator|=
name|dir
expr_stmt|;
name|localNodeId
operator|=
name|possibleLockId
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to obtain node lock on {}"
argument_list|,
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
comment|// release all the ones that were obtained up until now
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locks
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|locks
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|locks
index|[
name|i
index|]
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|locks
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
break|break;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to obtain node lock on {}"
argument_list|,
name|e
argument_list|,
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
expr_stmt|;
name|lastException
operator|=
operator|new
name|IOException
argument_list|(
literal|"failed to obtain lock on "
operator|+
name|dir
operator|.
name|getAbsolutePath
argument_list|()
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// release all the ones that were obtained up until now
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|locks
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|locks
index|[
name|i
index|]
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|locks
index|[
name|i
index|]
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|locks
index|[
name|i
index|]
operator|=
literal|null
expr_stmt|;
block|}
break|break;
block|}
block|}
if|if
condition|(
name|locks
index|[
literal|0
index|]
operator|!=
literal|null
condition|)
block|{
comment|// we found a lock, break
break|break;
block|}
block|}
if|if
condition|(
name|locks
index|[
literal|0
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Failed to obtain node lock, is the following location writable?: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|environment
operator|.
name|dataWithClusterFiles
argument_list|()
argument_list|)
argument_list|,
name|lastException
argument_list|)
throw|;
block|}
name|this
operator|.
name|localNodeId
operator|=
name|localNodeId
expr_stmt|;
name|this
operator|.
name|locks
operator|=
name|locks
expr_stmt|;
name|this
operator|.
name|nodeFiles
operator|=
name|nodesFiles
expr_stmt|;
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
literal|"using node location [{}], local_node_id [{}]"
argument_list|,
name|nodesFiles
argument_list|,
name|localNodeId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"node data locations details:\n"
argument_list|)
decl_stmt|;
for|for
control|(
name|File
name|file
range|:
name|nodesFiles
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" -> "
argument_list|)
operator|.
name|append
argument_list|(
name|file
operator|.
name|getAbsolutePath
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", free_space ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|file
operator|.
name|getFreeSpace
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"], usable_space ["
argument_list|)
operator|.
name|append
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|file
operator|.
name|getUsableSpace
argument_list|()
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|"]\n"
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|nodeIndicesLocations
operator|=
operator|new
name|File
index|[
name|nodeFiles
operator|.
name|length
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
name|nodeFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|nodeIndicesLocations
index|[
name|i
index|]
operator|=
operator|new
name|File
argument_list|(
name|nodeFiles
index|[
name|i
index|]
argument_list|,
literal|"indices"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|localNodeId
specifier|public
name|int
name|localNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|localNodeId
return|;
block|}
DECL|method|hasNodeFile
specifier|public
name|boolean
name|hasNodeFile
parameter_list|()
block|{
return|return
name|nodeFiles
operator|!=
literal|null
operator|&&
name|locks
operator|!=
literal|null
return|;
block|}
DECL|method|nodeDataLocations
specifier|public
name|File
index|[]
name|nodeDataLocations
parameter_list|()
block|{
if|if
condition|(
name|nodeFiles
operator|==
literal|null
operator|||
name|locks
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"node is not configured to store local location"
argument_list|)
throw|;
block|}
return|return
name|nodeFiles
return|;
block|}
DECL|method|indicesLocations
specifier|public
name|File
index|[]
name|indicesLocations
parameter_list|()
block|{
return|return
name|nodeIndicesLocations
return|;
block|}
DECL|method|indexLocations
specifier|public
name|File
index|[]
name|indexLocations
parameter_list|(
name|Index
name|index
parameter_list|)
block|{
name|File
index|[]
name|indexLocations
init|=
operator|new
name|File
index|[
name|nodeFiles
operator|.
name|length
index|]
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
name|nodeFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|indexLocations
index|[
name|i
index|]
operator|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|nodeFiles
index|[
name|i
index|]
argument_list|,
literal|"indices"
argument_list|)
argument_list|,
name|index
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|indexLocations
return|;
block|}
DECL|method|shardLocations
specifier|public
name|File
index|[]
name|shardLocations
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|File
index|[]
name|shardLocations
init|=
operator|new
name|File
index|[
name|nodeFiles
operator|.
name|length
index|]
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
name|nodeFiles
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|shardLocations
index|[
name|i
index|]
operator|=
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
operator|new
name|File
argument_list|(
name|nodeFiles
index|[
name|i
index|]
argument_list|,
literal|"indices"
argument_list|)
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
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
block|}
return|return
name|shardLocations
return|;
block|}
DECL|method|findAllIndices
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|findAllIndices
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|nodeFiles
operator|==
literal|null
operator|||
name|locks
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"node is not configured to store local location"
argument_list|)
throw|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|indicesLocation
range|:
name|nodeIndicesLocations
control|)
block|{
name|File
index|[]
name|indicesList
init|=
name|indicesLocation
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|indicesList
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|indexLocation
range|:
name|indicesList
control|)
block|{
if|if
condition|(
name|indexLocation
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|indices
operator|.
name|add
argument_list|(
name|indexLocation
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|indices
return|;
block|}
DECL|method|findAllShardIds
specifier|public
name|Set
argument_list|<
name|ShardId
argument_list|>
name|findAllShardIds
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|nodeFiles
operator|==
literal|null
operator|||
name|locks
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"node is not configured to store local location"
argument_list|)
throw|;
block|}
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shardIds
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|File
name|indicesLocation
range|:
name|nodeIndicesLocations
control|)
block|{
name|File
index|[]
name|indicesList
init|=
name|indicesLocation
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|indicesList
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|indexLocation
range|:
name|indicesList
control|)
block|{
if|if
condition|(
operator|!
name|indexLocation
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|String
name|indexName
init|=
name|indexLocation
operator|.
name|getName
argument_list|()
decl_stmt|;
name|File
index|[]
name|shardsList
init|=
name|indexLocation
operator|.
name|listFiles
argument_list|()
decl_stmt|;
if|if
condition|(
name|shardsList
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
for|for
control|(
name|File
name|shardLocation
range|:
name|shardsList
control|)
block|{
if|if
condition|(
operator|!
name|shardLocation
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|Integer
name|shardId
init|=
name|Ints
operator|.
name|tryParse
argument_list|(
name|shardLocation
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
name|shardIds
operator|.
name|add
argument_list|(
operator|new
name|ShardId
argument_list|(
name|indexName
argument_list|,
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|shardIds
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|locks
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Lock
name|lock
range|:
name|locks
control|)
block|{
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"releasing lock [{}]"
argument_list|,
name|lock
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to release lock [{}]"
argument_list|,
name|e
argument_list|,
name|lock
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

