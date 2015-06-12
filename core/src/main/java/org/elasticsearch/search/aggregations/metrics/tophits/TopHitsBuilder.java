begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Nullable
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
name|xcontent
operator|.
name|XContentBuilder
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
name|query
operator|.
name|QueryBuilder
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
name|Script
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
name|aggregations
operator|.
name|AbstractAggregationBuilder
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|highlight
operator|.
name|HighlightBuilder
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
name|sort
operator|.
name|SortBuilder
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
name|sort
operator|.
name|SortOrder
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Builder for the {@link TopHits} aggregation.  */
end_comment

begin_class
DECL|class|TopHitsBuilder
specifier|public
class|class
name|TopHitsBuilder
extends|extends
name|AbstractAggregationBuilder
block|{
DECL|field|sourceBuilder
specifier|private
name|SearchSourceBuilder
name|sourceBuilder
decl_stmt|;
comment|/**      * Sole constructor.      */
DECL|method|TopHitsBuilder
specifier|public
name|TopHitsBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalTopHits
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * The index to start to return hits from. Defaults to<tt>0</tt>.      */
DECL|method|setFrom
specifier|public
name|TopHitsBuilder
name|setFrom
parameter_list|(
name|int
name|from
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|from
argument_list|(
name|from
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The number of search hits to return. Defaults to<tt>10</tt>.      */
DECL|method|setSize
specifier|public
name|TopHitsBuilder
name|setSize
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|size
argument_list|(
name|size
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Applies when sorting, and controls if scores will be tracked as well. Defaults to      *<tt>false</tt>.      */
DECL|method|setTrackScores
specifier|public
name|TopHitsBuilder
name|setTrackScores
parameter_list|(
name|boolean
name|trackScores
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|trackScores
argument_list|(
name|trackScores
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should each {@link org.elasticsearch.search.SearchHit} be returned with an      * explanation of the hit (ranking).      */
DECL|method|setExplain
specifier|public
name|TopHitsBuilder
name|setExplain
parameter_list|(
name|boolean
name|explain
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|explain
argument_list|(
name|explain
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should each {@link org.elasticsearch.search.SearchHit} be returned with its      * version.      */
DECL|method|setVersion
specifier|public
name|TopHitsBuilder
name|setVersion
parameter_list|(
name|boolean
name|version
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets no fields to be loaded, resulting in only id and type to be returned per field.      */
DECL|method|setNoFields
specifier|public
name|TopHitsBuilder
name|setNoFields
parameter_list|()
block|{
name|sourceBuilder
argument_list|()
operator|.
name|noFields
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicates whether the response should contain the stored _source for every hit      */
DECL|method|setFetchSource
specifier|public
name|TopHitsBuilder
name|setFetchSource
parameter_list|(
name|boolean
name|fetch
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|fetch
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard      * elements.      *      * @param include An optional include (optionally wildcarded) pattern to filter the returned _source      * @param exclude An optional exclude (optionally wildcarded) pattern to filter the returned _source      */
DECL|method|setFetchSource
specifier|public
name|TopHitsBuilder
name|setFetchSource
parameter_list|(
annotation|@
name|Nullable
name|String
name|include
parameter_list|,
annotation|@
name|Nullable
name|String
name|exclude
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|include
argument_list|,
name|exclude
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Indicate that _source should be returned with every hit, with an "include" and/or "exclude" set which can include simple wildcard      * elements.      *      * @param includes An optional list of include (optionally wildcarded) pattern to filter the returned _source      * @param excludes An optional list of exclude (optionally wildcarded) pattern to filter the returned _source      */
DECL|method|setFetchSource
specifier|public
name|TopHitsBuilder
name|setFetchSource
parameter_list|(
annotation|@
name|Nullable
name|String
index|[]
name|includes
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|excludes
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fetchSource
argument_list|(
name|includes
argument_list|,
name|excludes
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field data based field to load and return. The field does not have to be stored,      * but its recommended to use non analyzed or numeric fields.      *      * @param name The field to get from the field data cache      */
DECL|method|addFieldDataField
specifier|public
name|TopHitsBuilder
name|addFieldDataField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|fieldDataField
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a script based field to load and return. The field does not have to be stored,      * but its recommended to use non analyzed or numeric fields.      *      * @param name   The name that will represent this value in the return hit      * @param script The script to use      */
DECL|method|addScriptField
specifier|public
name|TopHitsBuilder
name|addScriptField
parameter_list|(
name|String
name|name
parameter_list|,
name|Script
name|script
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|scriptField
argument_list|(
name|name
argument_list|,
name|script
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a sort against the given field name and the sort ordering.      *      * @param field The name of the field      * @param order The sort ordering      */
DECL|method|addSort
specifier|public
name|TopHitsBuilder
name|addSort
parameter_list|(
name|String
name|field
parameter_list|,
name|SortOrder
name|order
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|sort
argument_list|(
name|field
argument_list|,
name|order
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a generic sort builder.      *      * @see org.elasticsearch.search.sort.SortBuilders      */
DECL|method|addSort
specifier|public
name|TopHitsBuilder
name|addSort
parameter_list|(
name|SortBuilder
name|sort
parameter_list|)
block|{
name|sourceBuilder
argument_list|()
operator|.
name|sort
argument_list|(
name|sort
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with default fragment size of 100 characters, and      * default number of fragments of 5.      *      * @param name The field to highlight      */
DECL|method|addHighlightedField
specifier|public
name|TopHitsBuilder
name|addHighlightedField
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters), and      * default number of fragments of 5.      *      * @param name         The field to highlight      * @param fragmentSize The size of a fragment in characters      */
DECL|method|addHighlightedField
specifier|public
name|TopHitsBuilder
name|addHighlightedField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|fragmentSize
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|name
argument_list|,
name|fragmentSize
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters), and      * a provided (maximum) number of fragments.      *      * @param name              The field to highlight      * @param fragmentSize      The size of a fragment in characters      * @param numberOfFragments The (maximum) number of fragments      */
DECL|method|addHighlightedField
specifier|public
name|TopHitsBuilder
name|addHighlightedField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|fragmentSize
parameter_list|,
name|int
name|numberOfFragments
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|name
argument_list|,
name|fragmentSize
argument_list|,
name|numberOfFragments
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters),      * a provided (maximum) number of fragments and an offset for the highlight.      *      * @param name              The field to highlight      * @param fragmentSize      The size of a fragment in characters      * @param numberOfFragments The (maximum) number of fragments      */
DECL|method|addHighlightedField
specifier|public
name|TopHitsBuilder
name|addHighlightedField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|fragmentSize
parameter_list|,
name|int
name|numberOfFragments
parameter_list|,
name|int
name|fragmentOffset
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|name
argument_list|,
name|fragmentSize
argument_list|,
name|numberOfFragments
argument_list|,
name|fragmentOffset
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a highlighted field.      */
DECL|method|addHighlightedField
specifier|public
name|TopHitsBuilder
name|addHighlightedField
parameter_list|(
name|HighlightBuilder
operator|.
name|Field
name|field
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set a tag scheme that encapsulates a built in pre and post tags. The allows schemes      * are<tt>styled</tt> and<tt>default</tt>.      *      * @param schemaName The tag scheme name      */
DECL|method|setHighlighterTagsSchema
specifier|public
name|TopHitsBuilder
name|setHighlighterTagsSchema
parameter_list|(
name|String
name|schemaName
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|tagsSchema
argument_list|(
name|schemaName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterFragmentSize
specifier|public
name|TopHitsBuilder
name|setHighlighterFragmentSize
parameter_list|(
name|Integer
name|fragmentSize
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|fragmentSize
argument_list|(
name|fragmentSize
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterNumOfFragments
specifier|public
name|TopHitsBuilder
name|setHighlighterNumOfFragments
parameter_list|(
name|Integer
name|numOfFragments
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|numOfFragments
argument_list|(
name|numOfFragments
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterFilter
specifier|public
name|TopHitsBuilder
name|setHighlighterFilter
parameter_list|(
name|Boolean
name|highlightFilter
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|highlightFilter
argument_list|(
name|highlightFilter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The encoder to set for highlighting      */
DECL|method|setHighlighterEncoder
specifier|public
name|TopHitsBuilder
name|setHighlighterEncoder
parameter_list|(
name|String
name|encoder
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|encoder
argument_list|(
name|encoder
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Explicitly set the pre tags that will be used for highlighting.      */
DECL|method|setHighlighterPreTags
specifier|public
name|TopHitsBuilder
name|setHighlighterPreTags
parameter_list|(
name|String
modifier|...
name|preTags
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|preTags
argument_list|(
name|preTags
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Explicitly set the post tags that will be used for highlighting.      */
DECL|method|setHighlighterPostTags
specifier|public
name|TopHitsBuilder
name|setHighlighterPostTags
parameter_list|(
name|String
modifier|...
name|postTags
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|postTags
argument_list|(
name|postTags
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The order of fragments per field. By default, ordered by the order in the      * highlighted text. Can be<tt>score</tt>, which then it will be ordered      * by score of the fragments.      */
DECL|method|setHighlighterOrder
specifier|public
name|TopHitsBuilder
name|setHighlighterOrder
parameter_list|(
name|String
name|order
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|order
argument_list|(
name|order
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterRequireFieldMatch
specifier|public
name|TopHitsBuilder
name|setHighlighterRequireFieldMatch
parameter_list|(
name|boolean
name|requireFieldMatch
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|requireFieldMatch
argument_list|(
name|requireFieldMatch
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterBoundaryMaxScan
specifier|public
name|TopHitsBuilder
name|setHighlighterBoundaryMaxScan
parameter_list|(
name|Integer
name|boundaryMaxScan
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|boundaryMaxScan
argument_list|(
name|boundaryMaxScan
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterBoundaryChars
specifier|public
name|TopHitsBuilder
name|setHighlighterBoundaryChars
parameter_list|(
name|char
index|[]
name|boundaryChars
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|boundaryChars
argument_list|(
name|boundaryChars
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The highlighter type to use.      */
DECL|method|setHighlighterType
specifier|public
name|TopHitsBuilder
name|setHighlighterType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|highlighterType
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterFragmenter
specifier|public
name|TopHitsBuilder
name|setHighlighterFragmenter
parameter_list|(
name|String
name|fragmenter
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|fragmenter
argument_list|(
name|fragmenter
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a query to be used for highlighting all fields instead of the search query.      */
DECL|method|setHighlighterQuery
specifier|public
name|TopHitsBuilder
name|setHighlighterQuery
parameter_list|(
name|QueryBuilder
name|highlightQuery
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|highlightQuery
argument_list|(
name|highlightQuery
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the size of the fragment to return from the beginning of the field if there are no matches to      * highlight and the field doesn't also define noMatchSize.      * @param noMatchSize integer to set or null to leave out of request.  default is null.      * @return this builder for chaining      */
DECL|method|setHighlighterNoMatchSize
specifier|public
name|TopHitsBuilder
name|setHighlighterNoMatchSize
parameter_list|(
name|Integer
name|noMatchSize
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|noMatchSize
argument_list|(
name|noMatchSize
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the maximum number of phrases the fvh will consider if the field doesn't also define phraseLimit.      */
DECL|method|setHighlighterPhraseLimit
specifier|public
name|TopHitsBuilder
name|setHighlighterPhraseLimit
parameter_list|(
name|Integer
name|phraseLimit
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|phraseLimit
argument_list|(
name|phraseLimit
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|setHighlighterOptions
specifier|public
name|TopHitsBuilder
name|setHighlighterOptions
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
parameter_list|)
block|{
name|highlightBuilder
argument_list|()
operator|.
name|options
argument_list|(
name|options
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|getName
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|sourceBuilder
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
DECL|method|sourceBuilder
specifier|private
name|SearchSourceBuilder
name|sourceBuilder
parameter_list|()
block|{
if|if
condition|(
name|sourceBuilder
operator|==
literal|null
condition|)
block|{
name|sourceBuilder
operator|=
operator|new
name|SearchSourceBuilder
argument_list|()
expr_stmt|;
block|}
return|return
name|sourceBuilder
return|;
block|}
DECL|method|highlightBuilder
specifier|public
name|HighlightBuilder
name|highlightBuilder
parameter_list|()
block|{
return|return
name|sourceBuilder
argument_list|()
operator|.
name|highlighter
argument_list|()
return|;
block|}
block|}
end_class

end_unit

