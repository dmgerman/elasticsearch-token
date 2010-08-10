begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|support
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
name|IndexReader
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
name|collect
operator|.
name|ImmutableList
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
name|DocSet
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
name|DocSets
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
name|AndFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
operator|.
name|collector
operator|.
name|FacetCollector
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractFacetCollector
specifier|public
specifier|abstract
class|class
name|AbstractFacetCollector
extends|extends
name|FacetCollector
block|{
DECL|field|facetName
specifier|protected
specifier|final
name|String
name|facetName
decl_stmt|;
DECL|field|filter
specifier|protected
name|Filter
name|filter
decl_stmt|;
DECL|field|docSet
specifier|private
name|DocSet
name|docSet
init|=
literal|null
decl_stmt|;
DECL|method|AbstractFacetCollector
specifier|public
name|AbstractFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
name|this
operator|.
name|facetName
operator|=
name|facetName
expr_stmt|;
block|}
DECL|method|setFilter
annotation|@
name|Override
specifier|public
name|void
name|setFilter
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|filter
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|filter
operator|=
operator|new
name|AndFilter
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|filter
argument_list|,
name|this
operator|.
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|setScorer
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
comment|// usually, there is nothing to do here
block|}
DECL|method|acceptsDocsOutOfOrder
annotation|@
name|Override
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
comment|// when working on FieldData, docs can be out of order
block|}
DECL|method|setNextReader
annotation|@
name|Override
specifier|public
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|docSet
operator|=
name|DocSets
operator|.
name|convert
argument_list|(
name|reader
argument_list|,
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|doSetNextReader
argument_list|(
name|reader
argument_list|,
name|docBase
argument_list|)
expr_stmt|;
block|}
DECL|method|doSetNextReader
specifier|protected
specifier|abstract
name|void
name|doSetNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|collect
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
if|if
condition|(
name|docSet
operator|==
literal|null
condition|)
block|{
name|doCollect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|docSet
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|doCollect
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doCollect
specifier|protected
specifier|abstract
name|void
name|doCollect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

