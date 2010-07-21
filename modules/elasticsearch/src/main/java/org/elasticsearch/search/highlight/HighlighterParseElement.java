begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|collect
operator|.
name|Lists
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
name|XContentParser
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
name|SearchParseElement
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
name|SearchParseException
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
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *<pre>  * highlight : {  *  tags_schema : "styled",  *  pre_Tags : ["tag1", "tag2"],  *  post_tags : ["tag1", "tag2"],  *  order : "score",  *  fields : {  *      field1 : {  }  *      field2 : { fragment_size : 100, num_of_fragments : 2 }  *  }  * }  *</pre>  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|HighlighterParseElement
specifier|public
class|class
name|HighlighterParseElement
implements|implements
name|SearchParseElement
block|{
DECL|field|DEFAULT_PRE_TAGS
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|DEFAULT_PRE_TAGS
init|=
operator|new
name|String
index|[]
block|{
literal|"<em>"
block|}
decl_stmt|;
DECL|field|DEFAULT_POST_TAGS
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|DEFAULT_POST_TAGS
init|=
operator|new
name|String
index|[]
block|{
literal|"</em>"
block|}
decl_stmt|;
DECL|field|STYLED_PRE_TAG
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|STYLED_PRE_TAG
init|=
block|{
literal|"<em class=\"hlt1\">"
block|,
literal|"<em class=\"hlt2\">"
block|,
literal|"<em class=\"hlt3\">"
block|,
literal|"<em class=\"hlt4\">"
block|,
literal|"<em class=\"hlt5\">"
block|,
literal|"<em class=\"hlt6\">"
block|,
literal|"<em class=\"hlt7\">"
block|,
literal|"<em class=\"hlt8\">"
block|,
literal|"<em class=\"hlt9\">"
block|,
literal|"<em class=\"hlt10\">"
block|}
decl_stmt|;
DECL|field|STYLED_POST_TAGS
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|STYLED_POST_TAGS
init|=
block|{
literal|"</em>"
block|}
decl_stmt|;
DECL|method|parse
annotation|@
name|Override
specifier|public
name|void
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|topLevelFieldName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|SearchContextHighlight
operator|.
name|ParsedHighlightField
argument_list|>
name|fields
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|String
index|[]
name|preTags
init|=
name|DEFAULT_PRE_TAGS
decl_stmt|;
name|String
index|[]
name|postTags
init|=
name|DEFAULT_POST_TAGS
decl_stmt|;
name|boolean
name|scoreOrdered
init|=
literal|false
decl_stmt|;
name|boolean
name|highlightFilter
init|=
literal|true
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|topLevelFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
literal|"pre_tags"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
operator|||
literal|"preTags"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|preTagsList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|preTagsList
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|preTags
operator|=
name|preTagsList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|preTagsList
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"post_tags"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
operator|||
literal|"postTags"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|postTagsList
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|postTagsList
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|postTags
operator|=
name|postTagsList
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|postTagsList
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"order"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|scoreOrdered
operator|=
literal|"score"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"tags_schema"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
operator|||
literal|"tagsSchema"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|String
name|schema
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"styled"
operator|.
name|equals
argument_list|(
name|schema
argument_list|)
condition|)
block|{
name|preTags
operator|=
name|STYLED_PRE_TAG
expr_stmt|;
name|postTags
operator|=
name|STYLED_POST_TAGS
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
literal|"highlight_filter"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
operator|||
literal|"highlightFilter"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|highlightFilter
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"fields"
operator|.
name|equals
argument_list|(
name|topLevelFieldName
argument_list|)
condition|)
block|{
name|String
name|highlightFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|highlightFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|int
name|fragmentSize
init|=
literal|100
decl_stmt|;
name|int
name|numOfFragments
init|=
literal|5
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"fragment_size"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"fragmentSize"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|fragmentSize
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"number_of_fragments"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
operator|||
literal|"numberOfFragments"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|numOfFragments
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|fields
operator|.
name|add
argument_list|(
operator|new
name|SearchContextHighlight
operator|.
name|ParsedHighlightField
argument_list|(
name|highlightFieldName
argument_list|,
name|fragmentSize
argument_list|,
name|numOfFragments
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
if|if
condition|(
name|preTags
operator|!=
literal|null
operator|&&
name|postTags
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Highlighter preTags are set, but postTags are not set"
argument_list|)
throw|;
block|}
name|context
operator|.
name|highlight
argument_list|(
operator|new
name|SearchContextHighlight
argument_list|(
name|fields
argument_list|,
name|preTags
argument_list|,
name|postTags
argument_list|,
name|scoreOrdered
argument_list|,
name|highlightFilter
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

