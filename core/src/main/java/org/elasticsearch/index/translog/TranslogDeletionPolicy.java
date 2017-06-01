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
name|Map
import|;
end_import

begin_class
DECL|class|TranslogDeletionPolicy
specifier|public
class|class
name|TranslogDeletionPolicy
block|{
comment|/** Records how many views are held against each      *  translog generation */
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
comment|/**      * returns the minimum translog generation that is still required by the system. Any generation below      * the returned value may be safely deleted      */
DECL|method|minTranslogGenRequired
specifier|synchronized
name|long
name|minTranslogGenRequired
parameter_list|()
block|{
name|long
name|viewRefs
init|=
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
decl_stmt|;
return|return
name|Math
operator|.
name|min
argument_list|(
name|viewRefs
argument_list|,
name|minTranslogGenerationForRecovery
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

