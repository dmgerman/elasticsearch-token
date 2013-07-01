begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.highlight
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
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
name|xcontent
operator|.
name|ToXContent
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

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|newArrayList
import|;
end_import

begin_comment
comment|/**  * A builder for search highlighting.  *  * @see org.elasticsearch.search.builder.SearchSourceBuilder#highlight()  */
end_comment

begin_class
DECL|class|HighlightBuilder
specifier|public
class|class
name|HighlightBuilder
implements|implements
name|ToXContent
block|{
DECL|field|fields
specifier|private
name|List
argument_list|<
name|Field
argument_list|>
name|fields
decl_stmt|;
DECL|field|tagsSchema
specifier|private
name|String
name|tagsSchema
decl_stmt|;
DECL|field|preTags
specifier|private
name|String
index|[]
name|preTags
decl_stmt|;
DECL|field|postTags
specifier|private
name|String
index|[]
name|postTags
decl_stmt|;
DECL|field|order
specifier|private
name|String
name|order
decl_stmt|;
DECL|field|encoder
specifier|private
name|String
name|encoder
decl_stmt|;
DECL|field|requireFieldMatch
specifier|private
name|Boolean
name|requireFieldMatch
decl_stmt|;
DECL|field|highlighterType
specifier|private
name|String
name|highlighterType
decl_stmt|;
DECL|field|fragmenter
specifier|private
name|String
name|fragmenter
decl_stmt|;
DECL|field|options
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
decl_stmt|;
comment|/**      * Adds a field to be highlighted with default fragment size of 100 characters, and      * default number of fragments of 5 using the default encoder      *      * @param name The field to highlight      */
DECL|method|field
specifier|public
name|HighlightBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters), and      * default number of fragments of 5.      *      * @param name         The field to highlight      * @param fragmentSize The size of a fragment in characters      */
DECL|method|field
specifier|public
name|HighlightBuilder
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|fragmentSize
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|name
argument_list|)
operator|.
name|fragmentSize
argument_list|(
name|fragmentSize
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters), and      * a provided (maximum) number of fragments.      *      * @param name              The field to highlight      * @param fragmentSize      The size of a fragment in characters      * @param numberOfFragments The (maximum) number of fragments      */
DECL|method|field
specifier|public
name|HighlightBuilder
name|field
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
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|name
argument_list|)
operator|.
name|fragmentSize
argument_list|(
name|fragmentSize
argument_list|)
operator|.
name|numOfFragments
argument_list|(
name|numberOfFragments
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a field to be highlighted with a provided fragment size (in characters), and      * a provided (maximum) number of fragments.      *      * @param name              The field to highlight      * @param fragmentSize      The size of a fragment in characters      * @param numberOfFragments The (maximum) number of fragments      * @param fragmentOffset    The offset from the start of the fragment to the start of the highlight      */
DECL|method|field
specifier|public
name|HighlightBuilder
name|field
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
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
operator|new
name|Field
argument_list|(
name|name
argument_list|)
operator|.
name|fragmentSize
argument_list|(
name|fragmentSize
argument_list|)
operator|.
name|numOfFragments
argument_list|(
name|numberOfFragments
argument_list|)
operator|.
name|fragmentOffset
argument_list|(
name|fragmentOffset
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|field
specifier|public
name|HighlightBuilder
name|field
parameter_list|(
name|Field
name|field
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|fields
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set a tag scheme that encapsulates a built in pre and post tags. The allows schemes      * are<tt>styled</tt> and<tt>default</tt>.      *      * @param schemaName The tag scheme name      */
DECL|method|tagsSchema
specifier|public
name|HighlightBuilder
name|tagsSchema
parameter_list|(
name|String
name|schemaName
parameter_list|)
block|{
name|this
operator|.
name|tagsSchema
operator|=
name|schemaName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set encoder for the highlighting      * are<tt>styled</tt> and<tt>default</tt>.      *      * @param encoder name      */
DECL|method|encoder
specifier|public
name|HighlightBuilder
name|encoder
parameter_list|(
name|String
name|encoder
parameter_list|)
block|{
name|this
operator|.
name|encoder
operator|=
name|encoder
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Explicitly set the pre tags that will be used for highlighting.      */
DECL|method|preTags
specifier|public
name|HighlightBuilder
name|preTags
parameter_list|(
name|String
modifier|...
name|preTags
parameter_list|)
block|{
name|this
operator|.
name|preTags
operator|=
name|preTags
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Explicitly set the post tags that will be used for highlighting.      */
DECL|method|postTags
specifier|public
name|HighlightBuilder
name|postTags
parameter_list|(
name|String
modifier|...
name|postTags
parameter_list|)
block|{
name|this
operator|.
name|postTags
operator|=
name|postTags
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The order of fragments per field. By default, ordered by the order in the      * highlighted text. Can be<tt>score</tt>, which then it will be ordered      * by score of the fragments.      */
DECL|method|order
specifier|public
name|HighlightBuilder
name|order
parameter_list|(
name|String
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|requireFieldMatch
specifier|public
name|HighlightBuilder
name|requireFieldMatch
parameter_list|(
name|boolean
name|requireFieldMatch
parameter_list|)
block|{
name|this
operator|.
name|requireFieldMatch
operator|=
name|requireFieldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set type of highlighter to use. Supported types      * are<tt>highlighter</tt> and<tt>fast-vector-highlighter</tt>.      */
DECL|method|highlighterType
specifier|public
name|HighlightBuilder
name|highlighterType
parameter_list|(
name|String
name|highlighterType
parameter_list|)
block|{
name|this
operator|.
name|highlighterType
operator|=
name|highlighterType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets what fragmenter to use to break up text that is eligible for highlighting.      * This option is only applicable when using plain / normal highlighter.      */
DECL|method|fragmenter
specifier|public
name|HighlightBuilder
name|fragmenter
parameter_list|(
name|String
name|fragmenter
parameter_list|)
block|{
name|this
operator|.
name|fragmenter
operator|=
name|fragmenter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Allows to set custom options for custom highlighters.      */
DECL|method|options
specifier|public
name|HighlightBuilder
name|options
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
name|this
operator|.
name|options
operator|=
name|options
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
literal|"highlight"
argument_list|)
expr_stmt|;
if|if
condition|(
name|tagsSchema
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"tags_schema"
argument_list|,
name|tagsSchema
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|preTags
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|array
argument_list|(
literal|"pre_tags"
argument_list|,
name|preTags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|postTags
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|array
argument_list|(
literal|"post_tags"
argument_list|,
name|postTags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|order
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
name|order
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|encoder
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"encoder"
argument_list|,
name|encoder
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|requireFieldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"require_field_match"
argument_list|,
name|requireFieldMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|highlighterType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|highlighterType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fragmenter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fragmenter"
argument_list|,
name|fragmenter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|options
operator|!=
literal|null
operator|&&
name|options
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"options"
argument_list|,
name|options
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|fields
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|field
operator|.
name|preTags
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"pre_tags"
argument_list|,
name|field
operator|.
name|preTags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|postTags
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"post_tags"
argument_list|,
name|field
operator|.
name|postTags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|fragmentSize
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fragment_size"
argument_list|,
name|field
operator|.
name|fragmentSize
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|numOfFragments
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"number_of_fragments"
argument_list|,
name|field
operator|.
name|numOfFragments
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|fragmentOffset
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fragment_offset"
argument_list|,
name|field
operator|.
name|fragmentOffset
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|highlightFilter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"highlight_filter"
argument_list|,
name|field
operator|.
name|highlightFilter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|order
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
name|field
operator|.
name|order
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|requireFieldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"require_field_match"
argument_list|,
name|field
operator|.
name|requireFieldMatch
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|boundaryMaxScan
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boundary_max_scan"
argument_list|,
name|field
operator|.
name|boundaryMaxScan
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|boundaryChars
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boundary_chars"
argument_list|,
name|field
operator|.
name|boundaryChars
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|highlighterType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|field
operator|.
name|highlighterType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|fragmenter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"fragmenter"
argument_list|,
name|field
operator|.
name|fragmenter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|field
operator|.
name|options
operator|!=
literal|null
operator|&&
name|field
operator|.
name|options
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"options"
argument_list|,
name|field
operator|.
name|options
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|class|Field
specifier|public
specifier|static
class|class
name|Field
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|preTags
name|String
index|[]
name|preTags
decl_stmt|;
DECL|field|postTags
name|String
index|[]
name|postTags
decl_stmt|;
DECL|field|fragmentSize
name|int
name|fragmentSize
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|fragmentOffset
name|int
name|fragmentOffset
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|numOfFragments
name|int
name|numOfFragments
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|highlightFilter
name|Boolean
name|highlightFilter
decl_stmt|;
DECL|field|order
name|String
name|order
decl_stmt|;
DECL|field|requireFieldMatch
name|Boolean
name|requireFieldMatch
decl_stmt|;
DECL|field|boundaryMaxScan
name|int
name|boundaryMaxScan
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boundaryChars
name|char
index|[]
name|boundaryChars
decl_stmt|;
DECL|field|highlighterType
name|String
name|highlighterType
decl_stmt|;
DECL|field|fragmenter
name|String
name|fragmenter
decl_stmt|;
DECL|field|options
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|options
decl_stmt|;
DECL|method|Field
specifier|public
name|Field
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/**          * Explicitly set the pre tags for this field that will be used for highlighting.          * This overrides global settings set by {@link HighlightBuilder#preTags(String...)}.          */
DECL|method|preTags
specifier|public
name|Field
name|preTags
parameter_list|(
name|String
modifier|...
name|preTags
parameter_list|)
block|{
name|this
operator|.
name|preTags
operator|=
name|preTags
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Explicitly set the post tags for this field that will be used for highlighting.          * This overrides global settings set by {@link HighlightBuilder#postTags(String...)}.          */
DECL|method|postTags
specifier|public
name|Field
name|postTags
parameter_list|(
name|String
modifier|...
name|postTags
parameter_list|)
block|{
name|this
operator|.
name|postTags
operator|=
name|postTags
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fragmentSize
specifier|public
name|Field
name|fragmentSize
parameter_list|(
name|int
name|fragmentSize
parameter_list|)
block|{
name|this
operator|.
name|fragmentSize
operator|=
name|fragmentSize
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fragmentOffset
specifier|public
name|Field
name|fragmentOffset
parameter_list|(
name|int
name|fragmentOffset
parameter_list|)
block|{
name|this
operator|.
name|fragmentOffset
operator|=
name|fragmentOffset
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|numOfFragments
specifier|public
name|Field
name|numOfFragments
parameter_list|(
name|int
name|numOfFragments
parameter_list|)
block|{
name|this
operator|.
name|numOfFragments
operator|=
name|numOfFragments
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|highlightFilter
specifier|public
name|Field
name|highlightFilter
parameter_list|(
name|boolean
name|highlightFilter
parameter_list|)
block|{
name|this
operator|.
name|highlightFilter
operator|=
name|highlightFilter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * The order of fragments per field. By default, ordered by the order in the          * highlighted text. Can be<tt>score</tt>, which then it will be ordered          * by score of the fragments.          * This overrides global settings set by {@link HighlightBuilder#order(String)}.          */
DECL|method|order
specifier|public
name|Field
name|order
parameter_list|(
name|String
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|requireFieldMatch
specifier|public
name|Field
name|requireFieldMatch
parameter_list|(
name|boolean
name|requireFieldMatch
parameter_list|)
block|{
name|this
operator|.
name|requireFieldMatch
operator|=
name|requireFieldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boundaryMaxScan
specifier|public
name|Field
name|boundaryMaxScan
parameter_list|(
name|int
name|boundaryMaxScan
parameter_list|)
block|{
name|this
operator|.
name|boundaryMaxScan
operator|=
name|boundaryMaxScan
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|boundaryChars
specifier|public
name|Field
name|boundaryChars
parameter_list|(
name|char
index|[]
name|boundaryChars
parameter_list|)
block|{
name|this
operator|.
name|boundaryChars
operator|=
name|boundaryChars
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Set type of highlighter to use. Supported types          * are<tt>highlighter</tt> and<tt>fast-vector-highlighter</tt>.          * This overrides global settings set by {@link HighlightBuilder#highlighterType(String)}.          */
DECL|method|highlighterType
specifier|public
name|Field
name|highlighterType
parameter_list|(
name|String
name|highlighterType
parameter_list|)
block|{
name|this
operator|.
name|highlighterType
operator|=
name|highlighterType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Sets what fragmenter to use to break up text that is eligible for highlighting.          * This option is only applicable when using plain / normal highlighter.          * This overrides global settings set by {@link HighlightBuilder#fragmenter(String)}.          */
DECL|method|fragmenter
specifier|public
name|Field
name|fragmenter
parameter_list|(
name|String
name|fragmenter
parameter_list|)
block|{
name|this
operator|.
name|fragmenter
operator|=
name|fragmenter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Allows to set custom options for custom highlighters.          * This overrides global settings set by {@link HighlightBuilder#options(Map<String, Object>)}.          */
DECL|method|options
specifier|public
name|Field
name|options
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
name|this
operator|.
name|options
operator|=
name|options
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
block|}
end_class

end_unit

