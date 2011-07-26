begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RecoveryStatus
specifier|public
class|class
name|RecoveryStatus
block|{
DECL|enum|Stage
specifier|public
specifier|static
enum|enum
name|Stage
block|{
DECL|enum constant|INIT
name|INIT
block|,
DECL|enum constant|INDEX
name|INDEX
block|,
DECL|enum constant|TRANSLOG
name|TRANSLOG
block|,
DECL|enum constant|FINALIZE
name|FINALIZE
block|,
DECL|enum constant|DONE
name|DONE
block|}
DECL|field|openIndexOutputs
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
name|openIndexOutputs
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|checksums
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|checksums
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|startTime
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
DECL|field|time
name|long
name|time
decl_stmt|;
DECL|field|phase1FileNames
name|List
argument_list|<
name|String
argument_list|>
name|phase1FileNames
decl_stmt|;
DECL|field|phase1FileSizes
name|List
argument_list|<
name|Long
argument_list|>
name|phase1FileSizes
decl_stmt|;
DECL|field|phase1ExistingFileNames
name|List
argument_list|<
name|String
argument_list|>
name|phase1ExistingFileNames
decl_stmt|;
DECL|field|phase1ExistingFileSizes
name|List
argument_list|<
name|Long
argument_list|>
name|phase1ExistingFileSizes
decl_stmt|;
DECL|field|phase1TotalSize
name|long
name|phase1TotalSize
decl_stmt|;
DECL|field|phase1ExistingTotalSize
name|long
name|phase1ExistingTotalSize
decl_stmt|;
DECL|field|stage
specifier|volatile
name|Stage
name|stage
init|=
name|Stage
operator|.
name|INIT
decl_stmt|;
DECL|field|currentTranslogOperations
specifier|volatile
name|long
name|currentTranslogOperations
init|=
literal|0
decl_stmt|;
DECL|field|currentFilesSize
name|AtomicLong
name|currentFilesSize
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|method|startTime
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
DECL|method|time
specifier|public
name|long
name|time
parameter_list|()
block|{
return|return
name|this
operator|.
name|time
return|;
block|}
DECL|method|phase1TotalSize
specifier|public
name|long
name|phase1TotalSize
parameter_list|()
block|{
return|return
name|phase1TotalSize
return|;
block|}
DECL|method|phase1ExistingTotalSize
specifier|public
name|long
name|phase1ExistingTotalSize
parameter_list|()
block|{
return|return
name|phase1ExistingTotalSize
return|;
block|}
DECL|method|stage
specifier|public
name|Stage
name|stage
parameter_list|()
block|{
return|return
name|stage
return|;
block|}
DECL|method|currentTranslogOperations
specifier|public
name|long
name|currentTranslogOperations
parameter_list|()
block|{
return|return
name|currentTranslogOperations
return|;
block|}
DECL|method|currentFilesSize
specifier|public
name|long
name|currentFilesSize
parameter_list|()
block|{
return|return
name|currentFilesSize
operator|.
name|get
argument_list|()
return|;
block|}
block|}
end_class

end_unit

