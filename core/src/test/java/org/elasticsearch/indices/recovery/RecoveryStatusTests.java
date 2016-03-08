begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|codecs
operator|.
name|CodecUtil
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
name|IOContext
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
name|Version
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
name|transport
operator|.
name|LocalTransportAddress
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
name|util
operator|.
name|set
operator|.
name|Sets
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
name|IndexService
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
name|IndexShard
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
name|Store
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
name|StoreFileMetaData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESSingleNodeTestCase
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|RecoveryStatusTests
specifier|public
class|class
name|RecoveryStatusTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testRenameTempFiles
specifier|public
name|void
name|testRenameTempFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexService
name|service
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|service
operator|.
name|getShardOrNull
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"bar"
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|RecoveryTarget
name|status
init|=
operator|new
name|RecoveryTarget
argument_list|(
name|indexShard
argument_list|,
name|node
argument_list|,
operator|new
name|RecoveryTargetService
operator|.
name|RecoveryListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryDone
parameter_list|(
name|RecoveryState
name|state
parameter_list|)
block|{             }
annotation|@
name|Override
specifier|public
name|void
name|onRecoveryFailure
parameter_list|(
name|RecoveryState
name|state
parameter_list|,
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
block|{             }
block|}
argument_list|)
decl_stmt|;
try|try
init|(
name|IndexOutput
name|indexOutput
init|=
name|status
operator|.
name|openAndPutIndexOutput
argument_list|(
literal|"foo.bar"
argument_list|,
operator|new
name|StoreFileMetaData
argument_list|(
literal|"foo.bar"
argument_list|,
literal|8
operator|+
name|CodecUtil
operator|.
name|footerLength
argument_list|()
argument_list|,
literal|"9z51nw"
argument_list|)
argument_list|,
name|status
operator|.
name|store
argument_list|()
argument_list|)
init|)
block|{
name|indexOutput
operator|.
name|writeInt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|IndexOutput
name|openIndexOutput
init|=
name|status
operator|.
name|getOpenIndexOutput
argument_list|(
literal|"foo.bar"
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|openIndexOutput
argument_list|,
name|indexOutput
argument_list|)
expr_stmt|;
name|openIndexOutput
operator|.
name|writeInt
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|CodecUtil
operator|.
name|writeFooter
argument_list|(
name|indexOutput
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|status
operator|.
name|openAndPutIndexOutput
argument_list|(
literal|"foo.bar"
argument_list|,
operator|new
name|StoreFileMetaData
argument_list|(
literal|"foo.bar"
argument_list|,
literal|8
operator|+
name|CodecUtil
operator|.
name|footerLength
argument_list|()
argument_list|,
literal|"9z51nw"
argument_list|)
argument_list|,
name|status
operator|.
name|store
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"file foo.bar is already opened and registered"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"output for file [foo.bar] has already been created"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
comment|// all well = it's already registered
block|}
name|status
operator|.
name|removeOpenIndexOutputs
argument_list|(
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|strings
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|status
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|expectedFile
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|file
range|:
name|strings
control|)
block|{
if|if
condition|(
name|Pattern
operator|.
name|compile
argument_list|(
literal|"recovery[.]\\d+[.]foo[.]bar"
argument_list|)
operator|.
name|matcher
argument_list|(
name|file
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|expectedFile
operator|=
name|file
expr_stmt|;
break|break;
block|}
block|}
name|assertNotNull
argument_list|(
name|expectedFile
argument_list|)
expr_stmt|;
name|indexShard
operator|.
name|close
argument_list|(
literal|"foo"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// we have to close it here otherwise rename fails since the write.lock is held by the engine
name|status
operator|.
name|renameAllTempFiles
argument_list|()
expr_stmt|;
name|strings
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|status
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|strings
operator|.
name|toString
argument_list|()
argument_list|,
name|strings
operator|.
name|contains
argument_list|(
literal|"foo.bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|strings
operator|.
name|toString
argument_list|()
argument_list|,
name|strings
operator|.
name|contains
argument_list|(
name|expectedFile
argument_list|)
argument_list|)
expr_stmt|;
comment|// we must fail the recovery because marking it as done will try to move the shard to POST_RECOVERY, which will fail because it's started
name|status
operator|.
name|fail
argument_list|(
operator|new
name|RecoveryFailedException
argument_list|(
name|status
operator|.
name|state
argument_list|()
argument_list|,
literal|"end of test. OK."
argument_list|,
literal|null
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

