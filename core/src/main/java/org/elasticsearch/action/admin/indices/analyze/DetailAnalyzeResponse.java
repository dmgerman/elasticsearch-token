begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.analyze
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|analyze
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
name|Strings
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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

begin_class
DECL|class|DetailAnalyzeResponse
specifier|public
class|class
name|DetailAnalyzeResponse
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|method|DetailAnalyzeResponse
name|DetailAnalyzeResponse
parameter_list|()
block|{     }
DECL|field|customAnalyzer
specifier|private
name|boolean
name|customAnalyzer
init|=
literal|false
decl_stmt|;
DECL|field|analyzer
specifier|private
name|AnalyzeTokenList
name|analyzer
decl_stmt|;
DECL|field|charfilters
specifier|private
name|CharFilteredText
index|[]
name|charfilters
decl_stmt|;
DECL|field|tokenizer
specifier|private
name|AnalyzeTokenList
name|tokenizer
decl_stmt|;
DECL|field|tokenfilters
specifier|private
name|AnalyzeTokenList
index|[]
name|tokenfilters
decl_stmt|;
DECL|method|DetailAnalyzeResponse
specifier|public
name|DetailAnalyzeResponse
parameter_list|(
name|AnalyzeTokenList
name|analyzer
parameter_list|)
block|{
name|this
argument_list|(
literal|false
argument_list|,
name|analyzer
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|DetailAnalyzeResponse
specifier|public
name|DetailAnalyzeResponse
parameter_list|(
name|CharFilteredText
index|[]
name|charfilters
parameter_list|,
name|AnalyzeTokenList
name|tokenizer
parameter_list|,
name|AnalyzeTokenList
index|[]
name|tokenfilters
parameter_list|)
block|{
name|this
argument_list|(
literal|true
argument_list|,
literal|null
argument_list|,
name|charfilters
argument_list|,
name|tokenizer
argument_list|,
name|tokenfilters
argument_list|)
expr_stmt|;
block|}
DECL|method|DetailAnalyzeResponse
specifier|public
name|DetailAnalyzeResponse
parameter_list|(
name|boolean
name|customAnalyzer
parameter_list|,
name|AnalyzeTokenList
name|analyzer
parameter_list|,
name|CharFilteredText
index|[]
name|charfilters
parameter_list|,
name|AnalyzeTokenList
name|tokenizer
parameter_list|,
name|AnalyzeTokenList
index|[]
name|tokenfilters
parameter_list|)
block|{
name|this
operator|.
name|customAnalyzer
operator|=
name|customAnalyzer
expr_stmt|;
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
name|this
operator|.
name|charfilters
operator|=
name|charfilters
expr_stmt|;
name|this
operator|.
name|tokenizer
operator|=
name|tokenizer
expr_stmt|;
name|this
operator|.
name|tokenfilters
operator|=
name|tokenfilters
expr_stmt|;
block|}
DECL|method|analyzer
specifier|public
name|AnalyzeTokenList
name|analyzer
parameter_list|()
block|{
return|return
name|this
operator|.
name|analyzer
return|;
block|}
DECL|method|analyzer
specifier|public
name|DetailAnalyzeResponse
name|analyzer
parameter_list|(
name|AnalyzeTokenList
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|charfilters
specifier|public
name|CharFilteredText
index|[]
name|charfilters
parameter_list|()
block|{
return|return
name|this
operator|.
name|charfilters
return|;
block|}
DECL|method|charfilters
specifier|public
name|DetailAnalyzeResponse
name|charfilters
parameter_list|(
name|CharFilteredText
index|[]
name|charfilters
parameter_list|)
block|{
name|this
operator|.
name|charfilters
operator|=
name|charfilters
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|tokenizer
specifier|public
name|AnalyzeTokenList
name|tokenizer
parameter_list|()
block|{
return|return
name|tokenizer
return|;
block|}
DECL|method|tokenizer
specifier|public
name|DetailAnalyzeResponse
name|tokenizer
parameter_list|(
name|AnalyzeTokenList
name|tokenizer
parameter_list|)
block|{
name|this
operator|.
name|tokenizer
operator|=
name|tokenizer
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|tokenfilters
specifier|public
name|AnalyzeTokenList
index|[]
name|tokenfilters
parameter_list|()
block|{
return|return
name|tokenfilters
return|;
block|}
DECL|method|tokenfilters
specifier|public
name|DetailAnalyzeResponse
name|tokenfilters
parameter_list|(
name|AnalyzeTokenList
index|[]
name|tokenfilters
parameter_list|)
block|{
name|this
operator|.
name|tokenfilters
operator|=
name|tokenfilters
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
name|field
argument_list|(
name|Fields
operator|.
name|CUSTOM_ANALYZER
argument_list|,
name|customAnalyzer
argument_list|)
expr_stmt|;
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|ANALYZER
argument_list|)
expr_stmt|;
name|analyzer
operator|.
name|toXContentWithoutObject
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|charfilters
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|CHARFILTERS
argument_list|)
expr_stmt|;
for|for
control|(
name|CharFilteredText
name|charfilter
range|:
name|charfilters
control|)
block|{
name|charfilter
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|tokenizer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|TOKENIZER
argument_list|)
expr_stmt|;
name|tokenizer
operator|.
name|toXContentWithoutObject
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|tokenfilters
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|TOKENFILTERS
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeTokenList
name|tokenfilter
range|:
name|tokenfilters
control|)
block|{
name|tokenfilter
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|NAME
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"name"
decl_stmt|;
DECL|field|FILTERED_TEXT
specifier|static
specifier|final
name|String
name|FILTERED_TEXT
init|=
literal|"filtered_text"
decl_stmt|;
DECL|field|CUSTOM_ANALYZER
specifier|static
specifier|final
name|String
name|CUSTOM_ANALYZER
init|=
literal|"custom_analyzer"
decl_stmt|;
DECL|field|ANALYZER
specifier|static
specifier|final
name|String
name|ANALYZER
init|=
literal|"analyzer"
decl_stmt|;
DECL|field|CHARFILTERS
specifier|static
specifier|final
name|String
name|CHARFILTERS
init|=
literal|"charfilters"
decl_stmt|;
DECL|field|TOKENIZER
specifier|static
specifier|final
name|String
name|TOKENIZER
init|=
literal|"tokenizer"
decl_stmt|;
DECL|field|TOKENFILTERS
specifier|static
specifier|final
name|String
name|TOKENFILTERS
init|=
literal|"tokenfilters"
decl_stmt|;
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
name|this
operator|.
name|customAnalyzer
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|customAnalyzer
condition|)
block|{
name|tokenizer
operator|=
name|AnalyzeTokenList
operator|.
name|readAnalyzeTokenList
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|charfilters
operator|=
operator|new
name|CharFilteredText
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|charfilters
index|[
name|i
index|]
operator|=
name|CharFilteredText
operator|.
name|readCharFilteredText
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
name|size
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|tokenfilters
operator|=
operator|new
name|AnalyzeTokenList
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|tokenfilters
index|[
name|i
index|]
operator|=
name|AnalyzeTokenList
operator|.
name|readAnalyzeTokenList
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|analyzer
operator|=
name|AnalyzeTokenList
operator|.
name|readAnalyzeTokenList
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
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
name|out
operator|.
name|writeBoolean
argument_list|(
name|customAnalyzer
argument_list|)
expr_stmt|;
if|if
condition|(
name|customAnalyzer
condition|)
block|{
name|tokenizer
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|charfilters
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|charfilters
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|CharFilteredText
name|charfilter
range|:
name|charfilters
control|)
block|{
name|charfilter
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|tokenfilters
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|tokenfilters
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeTokenList
name|tokenfilter
range|:
name|tokenfilters
control|)
block|{
name|tokenfilter
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|analyzer
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|AnalyzeTokenList
specifier|public
specifier|static
class|class
name|AnalyzeTokenList
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|tokens
specifier|private
name|AnalyzeResponse
operator|.
name|AnalyzeToken
index|[]
name|tokens
decl_stmt|;
DECL|method|AnalyzeTokenList
name|AnalyzeTokenList
parameter_list|()
block|{         }
DECL|method|AnalyzeTokenList
specifier|public
name|AnalyzeTokenList
parameter_list|(
name|String
name|name
parameter_list|,
name|AnalyzeResponse
operator|.
name|AnalyzeToken
index|[]
name|tokens
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|tokens
operator|=
name|tokens
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getTokens
specifier|public
name|AnalyzeResponse
operator|.
name|AnalyzeToken
index|[]
name|getTokens
parameter_list|()
block|{
return|return
name|tokens
return|;
block|}
DECL|method|readAnalyzeTokenList
specifier|public
specifier|static
name|AnalyzeTokenList
name|readAnalyzeTokenList
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|AnalyzeTokenList
name|list
init|=
operator|new
name|AnalyzeTokenList
argument_list|()
decl_stmt|;
name|list
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
DECL|method|toXContentWithoutObject
specifier|public
name|XContentBuilder
name|toXContentWithoutObject
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
name|field
argument_list|(
name|Fields
operator|.
name|NAME
argument_list|,
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|AnalyzeResponse
operator|.
name|Fields
operator|.
name|TOKENS
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeResponse
operator|.
name|AnalyzeToken
name|token
range|:
name|tokens
control|)
block|{
name|token
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
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
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NAME
argument_list|,
name|this
operator|.
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|AnalyzeResponse
operator|.
name|Fields
operator|.
name|TOKENS
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeResponse
operator|.
name|AnalyzeToken
name|token
range|:
name|tokens
control|)
block|{
name|token
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
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
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|tokens
operator|=
operator|new
name|AnalyzeResponse
operator|.
name|AnalyzeToken
index|[
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|tokens
index|[
name|i
index|]
operator|=
name|AnalyzeResponse
operator|.
name|AnalyzeToken
operator|.
name|readAnalyzeToken
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
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
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|tokens
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|tokens
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|AnalyzeResponse
operator|.
name|AnalyzeToken
name|token
range|:
name|tokens
control|)
block|{
name|token
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|CharFilteredText
specifier|public
specifier|static
class|class
name|CharFilteredText
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|texts
specifier|private
name|String
index|[]
name|texts
decl_stmt|;
DECL|method|CharFilteredText
name|CharFilteredText
parameter_list|()
block|{         }
DECL|method|CharFilteredText
specifier|public
name|CharFilteredText
parameter_list|(
name|String
name|name
parameter_list|,
name|String
index|[]
name|texts
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
if|if
condition|(
name|texts
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|texts
operator|=
name|texts
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|texts
operator|=
name|Strings
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|getTexts
specifier|public
name|String
index|[]
name|getTexts
parameter_list|()
block|{
return|return
name|texts
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
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NAME
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|array
argument_list|(
name|Fields
operator|.
name|FILTERED_TEXT
argument_list|,
name|texts
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|readCharFilteredText
specifier|public
specifier|static
name|CharFilteredText
name|readCharFilteredText
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|CharFilteredText
name|text
init|=
operator|new
name|CharFilteredText
argument_list|()
decl_stmt|;
name|text
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|text
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
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|texts
operator|=
name|in
operator|.
name|readStringArray
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
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArray
argument_list|(
name|texts
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

