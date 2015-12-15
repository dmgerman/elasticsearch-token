begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.smbmmapfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|smbmmapfs
package|;
end_package

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
name|LockFactory
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
name|MMapDirectory
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
name|SmbDirectoryWrapper
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
name|index
operator|.
name|IndexSettings
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
name|ShardPath
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
name|FsDirectoryService
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
name|IndexStore
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
name|nio
operator|.
name|file
operator|.
name|Path
import|;
end_import

begin_class
DECL|class|SmbMmapFsDirectoryService
specifier|public
class|class
name|SmbMmapFsDirectoryService
extends|extends
name|FsDirectoryService
block|{
DECL|method|SmbMmapFsDirectoryService
specifier|public
name|SmbMmapFsDirectoryService
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|IndexStore
name|indexStore
parameter_list|,
name|ShardPath
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|indexStore
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newFSDirectory
specifier|protected
name|Directory
name|newFSDirectory
parameter_list|(
name|Path
name|location
parameter_list|,
name|LockFactory
name|lockFactory
parameter_list|)
throws|throws
name|IOException
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"wrapping MMapDirectory for SMB"
argument_list|)
expr_stmt|;
return|return
operator|new
name|SmbDirectoryWrapper
argument_list|(
operator|new
name|MMapDirectory
argument_list|(
name|location
argument_list|,
name|buildLockFactory
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

