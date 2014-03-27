begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
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
name|search
operator|.
name|DocIdSet
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
name|DocIdSetIterator
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
name|Filter
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
name|Scorer
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
name|AndDocIdSet
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
name|ContextDocIdSet
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
name|search
operator|.
name|XCollector
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
name|ArrayList
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

begin_comment
comment|/**  * A facet processor ends up actually executing the relevant facet for a specific  * search request.  *<p/>  * The facet executor requires at least the {@link #collector()} method to be implemented,  * with an optional {@link #post()} implementation if specific optimizations can be done.  */
end_comment

begin_class
DECL|class|FacetExecutor
specifier|public
specifier|abstract
class|class
name|FacetExecutor
block|{
comment|/**      * A post class extends this class to implement post hits processing.      */
DECL|class|Post
specifier|public
specifier|static
specifier|abstract
class|class
name|Post
block|{
DECL|method|executePost
specifier|public
specifier|abstract
name|void
name|executePost
parameter_list|(
name|List
argument_list|<
name|ContextDocIdSet
argument_list|>
name|docSets
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**          * A filtered post execution.          */
DECL|class|Filtered
specifier|public
specifier|static
class|class
name|Filtered
extends|extends
name|Post
block|{
DECL|field|post
specifier|private
specifier|final
name|Post
name|post
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|Filter
name|filter
decl_stmt|;
DECL|method|Filtered
specifier|public
name|Filtered
parameter_list|(
name|Post
name|post
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|this
operator|.
name|post
operator|=
name|post
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
DECL|method|executePost
specifier|public
name|void
name|executePost
parameter_list|(
name|List
argument_list|<
name|ContextDocIdSet
argument_list|>
name|docSets
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|ContextDocIdSet
argument_list|>
name|filteredEntries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|docSets
operator|.
name|size
argument_list|()
argument_list|)
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
name|docSets
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ContextDocIdSet
name|entry
init|=
name|docSets
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|DocIdSet
name|filteredSet
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|entry
operator|.
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|filteredSet
operator|!=
literal|null
condition|)
block|{
name|filteredEntries
operator|.
name|add
argument_list|(
operator|new
name|ContextDocIdSet
argument_list|(
name|entry
operator|.
name|context
argument_list|,
comment|// TODO: can we be smart here, maybe AndDocIdSet is not always fastest?
operator|new
name|AndDocIdSet
argument_list|(
operator|new
name|DocIdSet
index|[]
block|{
name|entry
operator|.
name|docSet
block|,
name|filteredSet
block|}
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|post
operator|.
name|executePost
argument_list|(
name|filteredEntries
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**          * A {@link FacetExecutor.Collector} based post.          */
DECL|class|Collector
specifier|public
specifier|static
class|class
name|Collector
extends|extends
name|Post
block|{
DECL|field|collector
specifier|private
specifier|final
name|FacetExecutor
operator|.
name|Collector
name|collector
decl_stmt|;
DECL|method|Collector
specifier|public
name|Collector
parameter_list|(
name|FacetExecutor
operator|.
name|Collector
name|collector
parameter_list|)
block|{
name|this
operator|.
name|collector
operator|=
name|collector
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executePost
specifier|public
name|void
name|executePost
parameter_list|(
name|List
argument_list|<
name|ContextDocIdSet
argument_list|>
name|docSets
parameter_list|)
throws|throws
name|IOException
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
name|docSets
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ContextDocIdSet
name|docSet
init|=
name|docSets
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|collector
operator|.
name|setNextReader
argument_list|(
name|docSet
operator|.
name|context
argument_list|)
expr_stmt|;
name|DocIdSetIterator
name|it
init|=
name|docSet
operator|.
name|docSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|int
name|doc
decl_stmt|;
while|while
condition|(
operator|(
name|doc
operator|=
name|it
operator|.
name|nextDoc
argument_list|()
operator|)
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|)
block|{
name|collector
operator|.
name|collect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
name|collector
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Simple extension to {@link XCollector} that implements methods that are typically      * not needed when doing collector based faceting.      */
DECL|class|Collector
specifier|public
specifier|static
specifier|abstract
class|class
name|Collector
extends|extends
name|XCollector
block|{
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{         }
annotation|@
name|Override
DECL|method|acceptsDocsOutOfOrder
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|postCollection
specifier|public
specifier|abstract
name|void
name|postCollection
parameter_list|()
function_decl|;
block|}
comment|/**      * The mode of the execution.      */
DECL|enum|Mode
specifier|public
specifier|static
enum|enum
name|Mode
block|{
comment|/**          * Collector mode, maps to {@link #collector()}.          */
DECL|enum constant|COLLECTOR
name|COLLECTOR
block|,
comment|/**          * Post mode, maps to {@link #post()}.          */
DECL|enum constant|POST
name|POST
block|}
comment|/**      * Builds the facet.      */
DECL|method|buildFacet
specifier|public
specifier|abstract
name|InternalFacet
name|buildFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
function_decl|;
comment|/**      * A collector based facet implementation, collection the facet as hits match.      */
DECL|method|collector
specifier|public
specifier|abstract
name|Collector
name|collector
parameter_list|()
function_decl|;
comment|/**      * A post based facet that executes the facet using the aggregated docs. By default      * uses the {@link Post.Collector} based implementation.      *<p/>      * Can be overridden if a more optimized non collector based implementation can be implemented.      */
DECL|method|post
specifier|public
name|Post
name|post
parameter_list|()
block|{
return|return
operator|new
name|Post
operator|.
name|Collector
argument_list|(
name|collector
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

