begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.strings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|strings
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
name|collect
operator|.
name|ImmutableSet
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
name|AtomicReaderContext
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
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
name|fielddata
operator|.
name|BytesValues
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
name|fielddata
operator|.
name|IndexFieldData
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
name|mapper
operator|.
name|FieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
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
name|facet
operator|.
name|FacetExecutor
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
name|facet
operator|.
name|InternalFacet
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
name|internal
operator|.
name|SearchContext
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
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FieldsTermsStringFacetExecutor
specifier|public
class|class
name|FieldsTermsStringFacetExecutor
extends|extends
name|FacetExecutor
block|{
DECL|field|comparatorType
specifier|private
specifier|final
name|InternalStringTermsFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|indexFieldDatas
specifier|private
specifier|final
name|IndexFieldData
index|[]
name|indexFieldDatas
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|aggregator
specifier|private
specifier|final
name|HashedAggregator
name|aggregator
decl_stmt|;
DECL|field|missing
name|long
name|missing
decl_stmt|;
DECL|field|total
name|long
name|total
decl_stmt|;
DECL|method|FieldsTermsStringFacetExecutor
specifier|public
name|FieldsTermsStringFacetExecutor
parameter_list|(
name|String
name|facetName
parameter_list|,
name|FieldMapper
index|[]
name|fieldMappers
parameter_list|,
name|int
name|size
parameter_list|,
name|InternalStringTermsFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|boolean
name|allTerms
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ImmutableSet
argument_list|<
name|BytesRef
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|SearchScript
name|script
parameter_list|)
block|{
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|indexFieldDatas
operator|=
operator|new
name|IndexFieldData
index|[
name|fieldMappers
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldMappers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|FieldMapper
name|mapper
init|=
name|fieldMappers
index|[
name|i
index|]
decl_stmt|;
name|indexFieldDatas
index|[
name|i
index|]
operator|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|excluded
operator|.
name|isEmpty
argument_list|()
operator|&&
name|pattern
operator|==
literal|null
operator|&&
name|script
operator|==
literal|null
condition|)
block|{
name|aggregator
operator|=
operator|new
name|HashedAggregator
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|aggregator
operator|=
operator|new
name|HashedScriptAggregator
argument_list|(
name|excluded
argument_list|,
name|pattern
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allTerms
condition|)
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
name|fieldMappers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TermsStringFacetExecutor
operator|.
name|loadAllTerms
argument_list|(
name|context
argument_list|,
name|indexFieldDatas
index|[
name|i
index|]
argument_list|,
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|collector
specifier|public
name|Collector
name|collector
parameter_list|()
block|{
return|return
operator|new
name|Collector
argument_list|(
name|aggregator
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildFacet
specifier|public
name|InternalFacet
name|buildFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
try|try
block|{
return|return
name|HashedAggregator
operator|.
name|buildFacet
argument_list|(
name|facetName
argument_list|,
name|size
argument_list|,
name|missing
argument_list|,
name|total
argument_list|,
name|comparatorType
argument_list|,
name|aggregator
argument_list|)
return|;
block|}
finally|finally
block|{
name|aggregator
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Collector
class|class
name|Collector
extends|extends
name|FacetExecutor
operator|.
name|Collector
block|{
DECL|field|aggregator
specifier|private
specifier|final
name|HashedAggregator
name|aggregator
decl_stmt|;
DECL|field|values
specifier|private
name|BytesValues
index|[]
name|values
decl_stmt|;
DECL|method|Collector
specifier|public
name|Collector
parameter_list|(
name|HashedAggregator
name|aggregator
parameter_list|)
block|{
name|values
operator|=
operator|new
name|BytesValues
index|[
name|indexFieldDatas
operator|.
name|length
index|]
expr_stmt|;
name|this
operator|.
name|aggregator
operator|=
name|aggregator
expr_stmt|;
block|}
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
block|{
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|script
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
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
name|indexFieldDatas
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|indexFieldDatas
index|[
name|i
index|]
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|script
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|collect
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
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|aggregator
operator|.
name|onDoc
argument_list|(
name|doc
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|postCollection
specifier|public
name|void
name|postCollection
parameter_list|()
block|{
name|FieldsTermsStringFacetExecutor
operator|.
name|this
operator|.
name|missing
operator|=
name|aggregator
operator|.
name|missing
argument_list|()
expr_stmt|;
name|FieldsTermsStringFacetExecutor
operator|.
name|this
operator|.
name|total
operator|=
name|aggregator
operator|.
name|total
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

