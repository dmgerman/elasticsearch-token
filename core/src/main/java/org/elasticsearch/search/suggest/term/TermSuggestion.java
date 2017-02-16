begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.term
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|ParseField
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|text
operator|.
name|Text
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
name|ConstructingObjectParser
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
name|ObjectParser
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
name|suggest
operator|.
name|SortBy
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
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
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
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
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
name|Comparator
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
name|xcontent
operator|.
name|ConstructingObjectParser
operator|.
name|constructorArg
import|;
end_import

begin_comment
comment|/**  * The suggestion responses corresponding with the suggestions in the request.  */
end_comment

begin_class
DECL|class|TermSuggestion
specifier|public
class|class
name|TermSuggestion
extends|extends
name|Suggestion
argument_list|<
name|TermSuggestion
operator|.
name|Entry
argument_list|>
block|{
DECL|field|NAME
specifier|private
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"term"
decl_stmt|;
DECL|field|SCORE
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|SCORE
init|=
operator|new
name|Score
argument_list|()
decl_stmt|;
DECL|field|FREQUENCY
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
name|FREQUENCY
init|=
operator|new
name|Frequency
argument_list|()
decl_stmt|;
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|int
name|TYPE
init|=
literal|1
decl_stmt|;
DECL|field|sort
specifier|private
name|SortBy
name|sort
decl_stmt|;
DECL|method|TermSuggestion
specifier|public
name|TermSuggestion
parameter_list|()
block|{     }
DECL|method|TermSuggestion
specifier|public
name|TermSuggestion
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|size
parameter_list|,
name|SortBy
name|sort
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|size
argument_list|)
expr_stmt|;
name|this
operator|.
name|sort
operator|=
name|sort
expr_stmt|;
block|}
comment|// Same behaviour as comparators in suggest module, but for SuggestedWord
comment|// Highest score first, then highest freq first, then lowest term first
DECL|class|Score
specifier|public
specifier|static
class|class
name|Score
implements|implements
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
name|first
parameter_list|,
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
name|second
parameter_list|)
block|{
comment|// first criteria: the distance
name|int
name|cmp
init|=
name|Float
operator|.
name|compare
argument_list|(
name|second
operator|.
name|getScore
argument_list|()
argument_list|,
name|first
operator|.
name|getScore
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
return|return
name|FREQUENCY
operator|.
name|compare
argument_list|(
name|first
argument_list|,
name|second
argument_list|)
return|;
block|}
block|}
comment|// Same behaviour as comparators in suggest module, but for SuggestedWord
comment|// Highest freq first, then highest score first, then lowest term first
DECL|class|Frequency
specifier|public
specifier|static
class|class
name|Frequency
implements|implements
name|Comparator
argument_list|<
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
name|first
parameter_list|,
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
name|second
parameter_list|)
block|{
comment|// first criteria: the popularity
name|int
name|cmp
init|=
operator|(
operator|(
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
operator|)
name|second
operator|)
operator|.
name|getFreq
argument_list|()
operator|-
operator|(
operator|(
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
operator|)
name|first
operator|)
operator|.
name|getFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// second criteria (if first criteria is equal): the distance
name|cmp
operator|=
name|Float
operator|.
name|compare
argument_list|(
name|second
operator|.
name|getScore
argument_list|()
argument_list|,
name|first
operator|.
name|getScore
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
comment|// third criteria: term text
return|return
name|first
operator|.
name|getText
argument_list|()
operator|.
name|compareTo
argument_list|(
name|second
operator|.
name|getText
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getWriteableType
specifier|public
name|int
name|getWriteableType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|protected
name|String
name|getType
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|sortComparator
specifier|protected
name|Comparator
argument_list|<
name|Option
argument_list|>
name|sortComparator
parameter_list|()
block|{
switch|switch
condition|(
name|sort
condition|)
block|{
case|case
name|SCORE
case|:
return|return
name|SCORE
return|;
case|case
name|FREQUENCY
case|:
return|return
name|FREQUENCY
return|;
default|default:
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Could not resolve comparator for sort key: ["
operator|+
name|sort
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|innerReadFrom
specifier|protected
name|void
name|innerReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|innerReadFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|sort
operator|=
name|SortBy
operator|.
name|readFromStream
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerWriteTo
specifier|public
name|void
name|innerWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|innerWriteTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|sort
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newEntry
specifier|protected
name|Entry
name|newEntry
parameter_list|()
block|{
return|return
operator|new
name|Entry
argument_list|()
return|;
block|}
comment|/**      * Represents a part from the suggest text with suggested options.      */
DECL|class|Entry
specifier|public
specifier|static
class|class
name|Entry
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
argument_list|<
name|TermSuggestion
operator|.
name|Entry
operator|.
name|Option
argument_list|>
block|{
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|Text
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
DECL|method|Entry
name|Entry
parameter_list|()
block|{         }
annotation|@
name|Override
DECL|method|newOption
specifier|protected
name|Option
name|newOption
parameter_list|()
block|{
return|return
operator|new
name|Option
argument_list|()
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
name|ObjectParser
argument_list|<
name|Entry
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"TermSuggestionEntryParser"
argument_list|,
literal|true
argument_list|,
name|Entry
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareCommonFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObjectArray
argument_list|(
name|Entry
operator|::
name|addOptions
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|Option
operator|.
name|fromXContent
argument_list|(
name|p
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|OPTIONS
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|Entry
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
return|return
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
return|;
block|}
comment|/**          * Contains the suggested text with its document frequency and score.          */
DECL|class|Option
specifier|public
specifier|static
class|class
name|Option
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|Suggest
operator|.
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
block|{
DECL|field|FREQ
specifier|public
specifier|static
specifier|final
name|ParseField
name|FREQ
init|=
operator|new
name|ParseField
argument_list|(
literal|"freq"
argument_list|)
decl_stmt|;
DECL|field|freq
specifier|private
name|int
name|freq
decl_stmt|;
DECL|method|Option
specifier|public
name|Option
parameter_list|(
name|Text
name|text
parameter_list|,
name|int
name|freq
parameter_list|,
name|float
name|score
parameter_list|)
block|{
name|super
argument_list|(
name|text
argument_list|,
name|score
argument_list|)
expr_stmt|;
name|this
operator|.
name|freq
operator|=
name|freq
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|mergeInto
specifier|protected
name|void
name|mergeInto
parameter_list|(
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
name|otherOption
parameter_list|)
block|{
name|super
operator|.
name|mergeInto
argument_list|(
name|otherOption
argument_list|)
expr_stmt|;
name|freq
operator|+=
operator|(
operator|(
name|Option
operator|)
name|otherOption
operator|)
operator|.
name|freq
expr_stmt|;
block|}
DECL|method|Option
specifier|protected
name|Option
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|setFreq
specifier|public
name|void
name|setFreq
parameter_list|(
name|int
name|freq
parameter_list|)
block|{
name|this
operator|.
name|freq
operator|=
name|freq
expr_stmt|;
block|}
comment|/**              * @return How often this suggested text appears in the index.              */
DECL|method|getFreq
specifier|public
name|int
name|getFreq
parameter_list|()
block|{
return|return
name|freq
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|freq
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|freq
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|XContentBuilder
name|innerToXContent
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
operator|=
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|FREQ
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|freq
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
specifier|final
name|ConstructingObjectParser
argument_list|<
name|Option
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ConstructingObjectParser
argument_list|<>
argument_list|(
literal|"TermSuggestionOptionParser"
argument_list|,
literal|true
argument_list|,
name|args
lambda|->
block|{
name|Text
name|text
init|=
operator|new
name|Text
argument_list|(
operator|(
name|String
operator|)
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
name|int
name|freq
init|=
operator|(
name|Integer
operator|)
name|args
index|[
literal|1
index|]
decl_stmt|;
name|float
name|score
init|=
operator|(
name|Float
operator|)
name|args
index|[
literal|2
index|]
decl_stmt|;
return|return
operator|new
name|Option
argument_list|(
name|text
argument_list|,
name|freq
argument_list|,
name|score
argument_list|)
return|;
block|}
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareString
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
operator|.
name|TEXT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareInt
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
name|FREQ
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareFloat
argument_list|(
name|constructorArg
argument_list|()
argument_list|,
name|Suggestion
operator|.
name|Entry
operator|.
name|Option
operator|.
name|SCORE
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
specifier|final
name|Option
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
block|{
return|return
name|PARSER
operator|.
name|apply
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

