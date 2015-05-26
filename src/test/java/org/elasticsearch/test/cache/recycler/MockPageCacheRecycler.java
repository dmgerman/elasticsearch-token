begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.cache.recycler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|cache
operator|.
name|recycler
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
name|base
operator|.
name|Predicate
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
name|collect
operator|.
name|Maps
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
name|collect
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
name|cache
operator|.
name|recycler
operator|.
name|PageCacheRecycler
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
name|recycler
operator|.
name|Recycler
operator|.
name|V
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
name|test
operator|.
name|ElasticsearchTestCase
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
name|InternalTestCluster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Array
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
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

begin_class
DECL|class|MockPageCacheRecycler
specifier|public
class|class
name|MockPageCacheRecycler
extends|extends
name|PageCacheRecycler
block|{
DECL|field|ACQUIRED_PAGES
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|Object
argument_list|,
name|Throwable
argument_list|>
name|ACQUIRED_PAGES
init|=
name|Maps
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|ensureAllPagesAreReleased
specifier|public
specifier|static
name|void
name|ensureAllPagesAreReleased
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|Throwable
argument_list|>
name|masterCopy
init|=
name|Maps
operator|.
name|newHashMap
argument_list|(
name|ACQUIRED_PAGES
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|masterCopy
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// not empty, we might be executing on a shared cluster that keeps on obtaining
comment|// and releasing pages, lets make sure that after a reasonable timeout, all master
comment|// copy (snapshot) have been released
name|boolean
name|success
init|=
name|ElasticsearchTestCase
operator|.
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|input
parameter_list|)
block|{
return|return
name|Sets
operator|.
name|intersection
argument_list|(
name|masterCopy
operator|.
name|keySet
argument_list|()
argument_list|,
name|ACQUIRED_PAGES
operator|.
name|keySet
argument_list|()
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
name|masterCopy
operator|.
name|keySet
argument_list|()
operator|.
name|retainAll
argument_list|(
name|ACQUIRED_PAGES
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|ACQUIRED_PAGES
operator|.
name|keySet
argument_list|()
operator|.
name|removeAll
argument_list|(
name|masterCopy
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
comment|// remove all existing master copy we will report on
if|if
condition|(
operator|!
name|masterCopy
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|Throwable
name|t
init|=
name|masterCopy
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|masterCopy
operator|.
name|size
argument_list|()
operator|+
literal|" pages have not been released"
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
block|}
block|}
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockPageCacheRecycler
specifier|public
name|MockPageCacheRecycler
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
specifier|final
name|long
name|seed
init|=
name|settings
operator|.
name|getAsLong
argument_list|(
name|InternalTestCluster
operator|.
name|SETTING_CLUSTER_NODE_SEED
argument_list|,
literal|0L
argument_list|)
decl_stmt|;
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
DECL|method|wrap
specifier|private
parameter_list|<
name|T
parameter_list|>
name|V
argument_list|<
name|T
argument_list|>
name|wrap
parameter_list|(
specifier|final
name|V
argument_list|<
name|T
argument_list|>
name|v
parameter_list|)
block|{
name|ACQUIRED_PAGES
operator|.
name|put
argument_list|(
name|v
argument_list|,
operator|new
name|Throwable
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|V
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
specifier|final
name|Throwable
name|t
init|=
name|ACQUIRED_PAGES
operator|.
name|remove
argument_list|(
name|v
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Releasing a page that has not been acquired"
argument_list|)
throw|;
block|}
specifier|final
name|T
name|ref
init|=
name|v
argument_list|()
decl_stmt|;
if|if
condition|(
name|ref
operator|instanceof
name|Object
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|Object
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|instanceof
name|byte
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
operator|(
name|byte
operator|)
name|random
operator|.
name|nextInt
argument_list|(
literal|256
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|instanceof
name|long
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|long
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|instanceof
name|int
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|int
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|instanceof
name|double
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|double
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
name|random
operator|.
name|nextDouble
argument_list|()
operator|-
literal|0.5
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|instanceof
name|float
index|[]
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
operator|(
name|float
index|[]
operator|)
name|ref
argument_list|,
literal|0
argument_list|,
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
argument_list|,
name|random
operator|.
name|nextFloat
argument_list|()
operator|-
literal|0.5f
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|Array
operator|.
name|getLength
argument_list|(
name|ref
argument_list|)
condition|;
operator|++
name|i
control|)
block|{
name|Array
operator|.
name|set
argument_list|(
name|ref
argument_list|,
name|i
argument_list|,
operator|(
name|byte
operator|)
name|random
operator|.
name|nextInt
argument_list|(
literal|256
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|v
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|T
name|v
parameter_list|()
block|{
return|return
name|v
operator|.
name|v
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRecycled
parameter_list|()
block|{
return|return
name|v
operator|.
name|isRecycled
argument_list|()
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|bytePage
specifier|public
name|V
argument_list|<
name|byte
index|[]
argument_list|>
name|bytePage
parameter_list|(
name|boolean
name|clear
parameter_list|)
block|{
specifier|final
name|V
argument_list|<
name|byte
index|[]
argument_list|>
name|page
init|=
name|super
operator|.
name|bytePage
argument_list|(
name|clear
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|clear
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|page
operator|.
name|v
argument_list|()
argument_list|,
literal|0
argument_list|,
name|page
operator|.
name|v
argument_list|()
operator|.
name|length
argument_list|,
operator|(
name|byte
operator|)
name|random
operator|.
name|nextInt
argument_list|(
literal|1
operator|<<
literal|8
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|wrap
argument_list|(
name|page
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|intPage
specifier|public
name|V
argument_list|<
name|int
index|[]
argument_list|>
name|intPage
parameter_list|(
name|boolean
name|clear
parameter_list|)
block|{
specifier|final
name|V
argument_list|<
name|int
index|[]
argument_list|>
name|page
init|=
name|super
operator|.
name|intPage
argument_list|(
name|clear
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|clear
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|page
operator|.
name|v
argument_list|()
argument_list|,
literal|0
argument_list|,
name|page
operator|.
name|v
argument_list|()
operator|.
name|length
argument_list|,
name|random
operator|.
name|nextInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|wrap
argument_list|(
name|page
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|longPage
specifier|public
name|V
argument_list|<
name|long
index|[]
argument_list|>
name|longPage
parameter_list|(
name|boolean
name|clear
parameter_list|)
block|{
specifier|final
name|V
argument_list|<
name|long
index|[]
argument_list|>
name|page
init|=
name|super
operator|.
name|longPage
argument_list|(
name|clear
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|clear
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|page
operator|.
name|v
argument_list|()
argument_list|,
literal|0
argument_list|,
name|page
operator|.
name|v
argument_list|()
operator|.
name|length
argument_list|,
name|random
operator|.
name|nextLong
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|wrap
argument_list|(
name|page
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|objectPage
specifier|public
name|V
argument_list|<
name|Object
index|[]
argument_list|>
name|objectPage
parameter_list|()
block|{
return|return
name|wrap
argument_list|(
name|super
operator|.
name|objectPage
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

