begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.merge.policy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|merge
operator|.
name|policy
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexWriter
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
name|index
operator|.
name|LogByteSizeMergePolicy
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
name|index
operator|.
name|LogMergePolicy
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
name|AbstractIndexShardComponent
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
name|IndexShardLifecycle
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
name|util
operator|.
name|Preconditions
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
name|SizeUnit
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
name|SizeValue
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|IndexShardLifecycle
DECL|class|BalancedSegmentMergePolicyProvider
specifier|public
class|class
name|BalancedSegmentMergePolicyProvider
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|MergePolicyProvider
argument_list|<
name|LogByteSizeMergePolicy
argument_list|>
block|{
DECL|field|minMergeSize
specifier|private
specifier|final
name|SizeValue
name|minMergeSize
decl_stmt|;
DECL|field|maxMergeSize
specifier|private
specifier|final
name|SizeValue
name|maxMergeSize
decl_stmt|;
DECL|field|mergeFactor
specifier|private
specifier|final
name|int
name|mergeFactor
decl_stmt|;
DECL|field|maxMergeDocs
specifier|private
specifier|final
name|int
name|maxMergeDocs
decl_stmt|;
DECL|field|numLargeSegments
specifier|private
specifier|final
name|int
name|numLargeSegments
decl_stmt|;
DECL|field|maxSmallSegments
specifier|private
specifier|final
name|int
name|maxSmallSegments
decl_stmt|;
DECL|field|useCompoundFile
specifier|private
specifier|final
name|Boolean
name|useCompoundFile
decl_stmt|;
DECL|method|BalancedSegmentMergePolicyProvider
annotation|@
name|Inject
specifier|public
name|BalancedSegmentMergePolicyProvider
parameter_list|(
name|Store
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|store
operator|.
name|shardId
argument_list|()
argument_list|,
name|store
operator|.
name|indexSettings
argument_list|()
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|store
argument_list|,
literal|"Store must be provided to merge policy"
argument_list|)
expr_stmt|;
name|this
operator|.
name|minMergeSize
operator|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"minMergeSize"
argument_list|,
operator|new
name|SizeValue
argument_list|(
operator|(
name|long
operator|)
name|LogByteSizeMergePolicy
operator|.
name|DEFAULT_MIN_MERGE_MB
operator|*
literal|1024
operator|*
literal|1024
argument_list|,
name|SizeUnit
operator|.
name|BYTES
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMergeSize
operator|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"maxMergeSize"
argument_list|,
operator|new
name|SizeValue
argument_list|(
operator|(
name|long
operator|)
name|LogByteSizeMergePolicy
operator|.
name|DEFAULT_MAX_MERGE_MB
argument_list|,
name|SizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|mergeFactor
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"mergeFactor"
argument_list|,
name|LogByteSizeMergePolicy
operator|.
name|DEFAULT_MERGE_FACTOR
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxMergeDocs
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"maxMergeDocs"
argument_list|,
name|LogByteSizeMergePolicy
operator|.
name|DEFAULT_MAX_MERGE_DOCS
argument_list|)
expr_stmt|;
name|this
operator|.
name|numLargeSegments
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"numLargeSegments"
argument_list|,
name|BalancedSegmentMergePolicy
operator|.
name|DEFAULT_NUM_LARGE_SEGMENTS
argument_list|)
expr_stmt|;
name|this
operator|.
name|maxSmallSegments
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"maxSmallSegments"
argument_list|,
literal|2
operator|*
name|LogMergePolicy
operator|.
name|DEFAULT_MERGE_FACTOR
argument_list|)
expr_stmt|;
name|this
operator|.
name|useCompoundFile
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"useCompoundFile"
argument_list|,
name|store
operator|==
literal|null
operator|||
name|store
operator|.
name|suggestUseCompoundFile
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using [Balanced] merge policy with mergeFactor[{}], minMergeSize[{}], maxMergeSize[{}], maxMergeDocs[{}] useCompoundFile[{}]"
argument_list|,
operator|new
name|Object
index|[]
block|{
name|mergeFactor
block|,
name|minMergeSize
block|,
name|maxMergeSize
block|,
name|maxMergeDocs
block|,
name|useCompoundFile
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|newMergePolicy
annotation|@
name|Override
specifier|public
name|BalancedSegmentMergePolicy
name|newMergePolicy
parameter_list|(
name|IndexWriter
name|indexWriter
parameter_list|)
block|{
name|BalancedSegmentMergePolicy
name|mergePolicy
init|=
operator|new
name|BalancedSegmentMergePolicy
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|mergePolicy
operator|.
name|setMinMergeMB
argument_list|(
name|minMergeSize
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergeMB
argument_list|(
name|maxMergeSize
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMergeFactor
argument_list|(
name|mergeFactor
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxMergeDocs
argument_list|(
name|maxMergeDocs
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setUseCompoundFile
argument_list|(
name|useCompoundFile
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setUseCompoundDocStore
argument_list|(
name|useCompoundFile
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setMaxSmallSegments
argument_list|(
name|maxSmallSegments
argument_list|)
expr_stmt|;
name|mergePolicy
operator|.
name|setNumLargeSegments
argument_list|(
name|numLargeSegments
argument_list|)
expr_stmt|;
return|return
name|mergePolicy
return|;
block|}
block|}
end_class

end_unit

