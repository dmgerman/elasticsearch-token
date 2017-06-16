begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
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
name|util
operator|.
name|Counter
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

begin_class
DECL|class|TranslogDeletionPolicy
specifier|public
class|class
name|TranslogDeletionPolicy
block|{
comment|/**      * Records how many views are held against each      * translog generation      */
DECL|field|translogRefCounts
specifier|private
specifier|final
name|Map
argument_list|<
name|Long
argument_list|,
name|Counter
argument_list|>
name|translogRefCounts
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * the translog generation that is requires to properly recover from the oldest non deleted      * {@link org.apache.lucene.index.IndexCommit}.      */
DECL|field|minTranslogGenerationForRecovery
specifier|private
name|long
name|minTranslogGenerationForRecovery
init|=
literal|1
decl_stmt|;
DECL|field|retentionSizeInBytes
specifier|private
name|long
name|retentionSizeInBytes
decl_stmt|;
DECL|field|retentionAgeInMillis
specifier|private
name|long
name|retentionAgeInMillis
decl_stmt|;
DECL|method|TranslogDeletionPolicy
specifier|public
name|TranslogDeletionPolicy
parameter_list|(
name|long
name|retentionSizeInBytes
parameter_list|,
name|long
name|retentionAgeInMillis
parameter_list|)
block|{
name|this
operator|.
name|retentionSizeInBytes
operator|=
name|retentionSizeInBytes
expr_stmt|;
name|this
operator|.
name|retentionAgeInMillis
operator|=
name|retentionAgeInMillis
expr_stmt|;
block|}
DECL|method|setMinTranslogGenerationForRecovery
specifier|public
specifier|synchronized
name|void
name|setMinTranslogGenerationForRecovery
parameter_list|(
name|long
name|newGen
parameter_list|)
block|{
if|if
condition|(
name|newGen
operator|<
name|minTranslogGenerationForRecovery
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"minTranslogGenerationForRecovery can't go backwards. new ["
operator|+
name|newGen
operator|+
literal|"] current ["
operator|+
name|minTranslogGenerationForRecovery
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|minTranslogGenerationForRecovery
operator|=
name|newGen
expr_stmt|;
block|}
DECL|method|setRetentionSizeInBytes
specifier|public
specifier|synchronized
name|void
name|setRetentionSizeInBytes
parameter_list|(
name|long
name|bytes
parameter_list|)
block|{
name|retentionSizeInBytes
operator|=
name|bytes
expr_stmt|;
block|}
DECL|method|setRetentionAgeInMillis
specifier|public
specifier|synchronized
name|void
name|setRetentionAgeInMillis
parameter_list|(
name|long
name|ageInMillis
parameter_list|)
block|{
name|retentionAgeInMillis
operator|=
name|ageInMillis
expr_stmt|;
block|}
comment|/**      * acquires the basis generation for a new view. Any translog generation above, and including, the returned generation      * will not be deleted until a corresponding call to {@link #releaseTranslogGenView(long)} is called.      */
DECL|method|acquireTranslogGenForView
specifier|synchronized
name|long
name|acquireTranslogGenForView
parameter_list|()
block|{
name|translogRefCounts
operator|.
name|computeIfAbsent
argument_list|(
name|minTranslogGenerationForRecovery
argument_list|,
name|l
lambda|->
name|Counter
operator|.
name|newCounter
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|addAndGet
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|minTranslogGenerationForRecovery
return|;
block|}
comment|/** returns the number of generations that were acquired for views */
DECL|method|pendingViewsCount
specifier|synchronized
name|int
name|pendingViewsCount
parameter_list|()
block|{
return|return
name|translogRefCounts
operator|.
name|size
argument_list|()
return|;
block|}
comment|/**      * releases a generation that was acquired by {@link #acquireTranslogGenForView()}      */
DECL|method|releaseTranslogGenView
specifier|synchronized
name|void
name|releaseTranslogGenView
parameter_list|(
name|long
name|translogGen
parameter_list|)
block|{
name|Counter
name|current
init|=
name|translogRefCounts
operator|.
name|get
argument_list|(
name|translogGen
argument_list|)
decl_stmt|;
if|if
condition|(
name|current
operator|==
literal|null
operator|||
name|current
operator|.
name|get
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"translog gen ["
operator|+
name|translogGen
operator|+
literal|"] wasn't acquired"
argument_list|)
throw|;
block|}
if|if
condition|(
name|current
operator|.
name|addAndGet
argument_list|(
operator|-
literal|1
argument_list|)
operator|==
literal|0
condition|)
block|{
name|translogRefCounts
operator|.
name|remove
argument_list|(
name|translogGen
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * returns the minimum translog generation that is still required by the system. Any generation below      * the returned value may be safely deleted      *      * @param readers current translog readers      * @param writer  current translog writer      */
DECL|method|minTranslogGenRequired
specifier|synchronized
name|long
name|minTranslogGenRequired
parameter_list|(
name|List
argument_list|<
name|TranslogReader
argument_list|>
name|readers
parameter_list|,
name|TranslogWriter
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|minByView
init|=
name|getMinTranslogGenRequiredByViews
argument_list|()
decl_stmt|;
name|long
name|minByAge
init|=
name|getMinTranslogGenByAge
argument_list|(
name|readers
argument_list|,
name|writer
argument_list|,
name|retentionAgeInMillis
argument_list|,
name|currentTime
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|minBySize
init|=
name|getMinTranslogGenBySize
argument_list|(
name|readers
argument_list|,
name|writer
argument_list|,
name|retentionSizeInBytes
argument_list|)
decl_stmt|;
specifier|final
name|long
name|minByAgeAndSize
decl_stmt|;
if|if
condition|(
name|minBySize
operator|==
name|Long
operator|.
name|MIN_VALUE
operator|&&
name|minByAge
operator|==
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
comment|// both size and age are disabled;
name|minByAgeAndSize
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
else|else
block|{
name|minByAgeAndSize
operator|=
name|Math
operator|.
name|max
argument_list|(
name|minByAge
argument_list|,
name|minBySize
argument_list|)
expr_stmt|;
block|}
return|return
name|Math
operator|.
name|min
argument_list|(
name|minByAgeAndSize
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|minByView
argument_list|,
name|minTranslogGenerationForRecovery
argument_list|)
argument_list|)
return|;
block|}
DECL|method|getMinTranslogGenBySize
specifier|static
name|long
name|getMinTranslogGenBySize
parameter_list|(
name|List
argument_list|<
name|TranslogReader
argument_list|>
name|readers
parameter_list|,
name|TranslogWriter
name|writer
parameter_list|,
name|long
name|retentionSizeInBytes
parameter_list|)
block|{
if|if
condition|(
name|retentionSizeInBytes
operator|>=
literal|0
condition|)
block|{
name|long
name|totalSize
init|=
name|writer
operator|.
name|sizeInBytes
argument_list|()
decl_stmt|;
name|long
name|minGen
init|=
name|writer
operator|.
name|getGeneration
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|readers
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
operator|&&
name|totalSize
operator|<
name|retentionSizeInBytes
condition|;
name|i
operator|--
control|)
block|{
specifier|final
name|TranslogReader
name|reader
init|=
name|readers
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|totalSize
operator|+=
name|reader
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
name|minGen
operator|=
name|reader
operator|.
name|getGeneration
argument_list|()
expr_stmt|;
block|}
return|return
name|minGen
return|;
block|}
else|else
block|{
return|return
name|Long
operator|.
name|MIN_VALUE
return|;
block|}
block|}
DECL|method|getMinTranslogGenByAge
specifier|static
name|long
name|getMinTranslogGenByAge
parameter_list|(
name|List
argument_list|<
name|TranslogReader
argument_list|>
name|readers
parameter_list|,
name|TranslogWriter
name|writer
parameter_list|,
name|long
name|maxRetentionAgeInMillis
parameter_list|,
name|long
name|now
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|maxRetentionAgeInMillis
operator|>=
literal|0
condition|)
block|{
for|for
control|(
name|TranslogReader
name|reader
range|:
name|readers
control|)
block|{
if|if
condition|(
name|now
operator|-
name|reader
operator|.
name|getLastModifiedTime
argument_list|()
operator|<=
name|maxRetentionAgeInMillis
condition|)
block|{
return|return
name|reader
operator|.
name|getGeneration
argument_list|()
return|;
block|}
block|}
return|return
name|writer
operator|.
name|getGeneration
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|Long
operator|.
name|MIN_VALUE
return|;
block|}
block|}
DECL|method|currentTime
specifier|protected
name|long
name|currentTime
parameter_list|()
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
return|;
block|}
DECL|method|getMinTranslogGenRequiredByViews
specifier|private
name|long
name|getMinTranslogGenRequiredByViews
parameter_list|()
block|{
return|return
name|translogRefCounts
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|reduce
argument_list|(
name|Math
operator|::
name|min
argument_list|)
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
comment|/** returns the translog generation that will be used as a basis of a future store/peer recovery */
DECL|method|getMinTranslogGenerationForRecovery
specifier|public
specifier|synchronized
name|long
name|getMinTranslogGenerationForRecovery
parameter_list|()
block|{
return|return
name|minTranslogGenerationForRecovery
return|;
block|}
block|}
end_class

end_unit

