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
name|elasticsearch
operator|.
name|ElasticsearchWrapperException
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
name|shard
operator|.
name|IndexShardException
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RecoverFilesRecoveryException
specifier|public
class|class
name|RecoverFilesRecoveryException
extends|extends
name|IndexShardException
implements|implements
name|ElasticsearchWrapperException
block|{
DECL|field|numberOfFiles
specifier|private
specifier|final
name|int
name|numberOfFiles
decl_stmt|;
DECL|field|totalFilesSize
specifier|private
specifier|final
name|ByteSizeValue
name|totalFilesSize
decl_stmt|;
DECL|method|RecoverFilesRecoveryException
specifier|public
name|RecoverFilesRecoveryException
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|int
name|numberOfFiles
parameter_list|,
name|ByteSizeValue
name|totalFilesSize
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
literal|"Failed to transfer ["
operator|+
name|numberOfFiles
operator|+
literal|"] files with total size of ["
operator|+
name|totalFilesSize
operator|+
literal|"]"
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|this
operator|.
name|numberOfFiles
operator|=
name|numberOfFiles
expr_stmt|;
name|this
operator|.
name|totalFilesSize
operator|=
name|totalFilesSize
expr_stmt|;
block|}
DECL|method|numberOfFiles
specifier|public
name|int
name|numberOfFiles
parameter_list|()
block|{
return|return
name|numberOfFiles
return|;
block|}
DECL|method|totalFilesSize
specifier|public
name|ByteSizeValue
name|totalFilesSize
parameter_list|()
block|{
return|return
name|totalFilesSize
return|;
block|}
block|}
end_class

end_unit
