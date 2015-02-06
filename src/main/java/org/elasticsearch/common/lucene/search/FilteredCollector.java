begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
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
name|index
operator|.
name|LeafReaderContext
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
name|search
operator|.
name|*
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
name|util
operator|.
name|Bits
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
comment|/**  *  */
end_comment

begin_class
DECL|class|FilteredCollector
specifier|public
class|class
name|FilteredCollector
implements|implements
name|XCollector
block|{
DECL|field|collector
specifier|private
specifier|final
name|Collector
name|collector
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|method|FilteredCollector
specifier|public
name|FilteredCollector
parameter_list|(
name|Collector
name|collector
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|collector
operator|=
name|collector
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|postCollection
specifier|public
name|void
name|postCollection
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|collector
operator|instanceof
name|XCollector
condition|)
block|{
operator|(
operator|(
name|XCollector
operator|)
name|collector
operator|)
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|DocIdSet
name|set
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
specifier|final
name|LeafCollector
name|in
init|=
name|collector
operator|.
name|getLeafCollector
argument_list|(
name|context
argument_list|)
decl_stmt|;
specifier|final
name|Bits
name|bits
init|=
name|set
operator|==
literal|null
condition|?
literal|null
else|:
name|set
operator|.
name|bits
argument_list|()
decl_stmt|;
if|if
condition|(
name|bits
operator|!=
literal|null
condition|)
block|{
comment|// the filter supports random-access
return|return
operator|new
name|FilterLeafCollector
argument_list|(
name|in
argument_list|)
block|{
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bits
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|in
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
comment|// No random-access support, use the iterator and force in-order scoring
specifier|final
name|DocIdSetIterator
name|iterator
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|set
argument_list|)
condition|)
block|{
name|iterator
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
comment|// DIS.iterator might still return null here
name|iterator
operator|=
name|set
operator|.
name|iterator
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|iterator
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|FilterLeafCollector
argument_list|(
name|in
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no-op
block|}
block|}
return|;
block|}
return|return
operator|new
name|FilterLeafCollector
argument_list|(
name|in
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|itDoc
init|=
name|iterator
operator|.
name|docID
argument_list|()
decl_stmt|;
if|if
condition|(
name|itDoc
operator|>
name|doc
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|itDoc
operator|<
name|doc
condition|)
block|{
if|if
condition|(
name|iterator
operator|.
name|advance
argument_list|(
name|doc
argument_list|)
operator|==
name|doc
condition|)
block|{
name|in
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|in
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
name|collector
operator|.
name|needsScores
argument_list|()
return|;
block|}
block|}
end_class

end_unit

